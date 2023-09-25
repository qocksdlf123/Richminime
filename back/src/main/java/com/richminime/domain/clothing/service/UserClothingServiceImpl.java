package com.richminime.domain.clothing.service;

import com.richminime.domain.bankBook.constant.TransactionType;
import com.richminime.domain.bankBook.dao.BankBookRepository;
import com.richminime.domain.bankBook.domain.BankBook;
import com.richminime.domain.clothing.constant.ClothingType;
import com.richminime.domain.clothing.dao.ClothingRepository;
import com.richminime.domain.clothing.dao.UserClothingRepository;
import com.richminime.domain.clothing.domain.Clothing;
import com.richminime.domain.clothing.domain.UserClothing;
import com.richminime.domain.clothing.dto.UserClothingReqDto;
import com.richminime.domain.clothing.dto.UserClothingResDto;
import com.richminime.domain.clothing.exception.ClothingNotFoundException;
import com.richminime.domain.user.domain.User;
import com.richminime.domain.user.exception.UserNotFoundException;
import com.richminime.domain.user.repository.UserRepository;
import com.richminime.global.exception.InsufficientBalanceException;
import com.richminime.global.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.richminime.domain.clothing.constant.ClothingExceptionMessage.CLOTHING_NOT_FOUND;
import static com.richminime.domain.user.exception.UserExceptionMessage.USER_NOT_FOUND;
import static com.richminime.global.constant.ExceptionMessage.INSUFFICINET_BALANCE;

@Service
@RequiredArgsConstructor
public class UserClothingServiceImpl implements UserClothingService {

    private final UserClothingRepository userClothingRepository;
    private final ClothingRepository clothingRepository;
    private final UserRepository userRepository;
    private final BankBookRepository bankBookRepository;


    @Transactional
    @Override
    public void addMyClothing(UserClothingReqDto userClothingReqDto) {

        String loggedInUserEmail = SecurityUtils.getLoggedInUserEmail();

        User user = userRepository.findByEmail(loggedInUserEmail)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND.getMessage()));

        long clothingId = userClothingReqDto.getClothingId();
        Clothing clothing = clothingRepository.findById(clothingId)
                .orElseThrow(() -> new ClothingNotFoundException(CLOTHING_NOT_FOUND.getMessage()));

        long newBalance = user.getBalance() - clothing.getPrice();

        //잔액부족
        if (newBalance < 0)
            throw new InsufficientBalanceException(INSUFFICINET_BALANCE.getMessage());

        BankBook bankBook = BankBook.builder()
                .userId(user.getUserId())
                .amount(clothing.getPrice())
                .date(LocalDate.now())
                .balance(newBalance)
                .transactionType(TransactionType.getTransactionType("구매"))
                .summary(clothing.getClothingInfo())
                .build();

        bankBookRepository.save(bankBook);
        user.updateBalance(newBalance);

        UserClothing userClothing = UserClothing.builder()
                .clothing(clothing)
                .user(user)
                .build();

        userClothingRepository.save(userClothing);
    }

    @Transactional
    @Override
    public void deleteMyClothing(Long userClothingId) {
        UserClothing userClothing = userClothingRepository.findById(userClothingId)
                .orElseThrow(() -> new ClothingNotFoundException(CLOTHING_NOT_FOUND.getMessage()));

        User user = userClothing.getUser();
        long saleAmount = Math.round(userClothing.getClothing().getPrice() * 0.4);
        long newBalance = user.getBalance() + saleAmount;
        user.updateBalance(newBalance);

        BankBook bankBook = BankBook.builder()
                .userId(user.getUserId())
                .amount(saleAmount)
                .date(LocalDate.now())
                .balance(newBalance)
                .transactionType(TransactionType.getTransactionType("판매"))
                .summary(userClothing.getClothing().getClothingInfo())
                .build();

        bankBookRepository.save(bankBook);

        userClothingRepository.delete(userClothing);
    }

    @Transactional
    @Override
    public List<UserClothingResDto> findAllMyClothingByType(ClothingType clothingType) {
        if (clothingType == null) {
            List<UserClothing> userClothingList = userClothingRepository.findAll();
            List<UserClothingResDto> userClothingResDtoList = new ArrayList<>();
            for (UserClothing userClothing : userClothingList) {
                UserClothingResDto dto = UserClothingResDto.entityToDto(userClothing);
                userClothingResDtoList.add(dto);
            }
            return userClothingResDtoList;
        } else {
            List<UserClothing> userClothingListByType = userClothingRepository.findAllByClothing_ClothingType(clothingType);
            List<UserClothingResDto> userClothingResDtoListByType = new ArrayList<>();
            for (UserClothing userClothing : userClothingListByType) {
                UserClothingResDto dto = UserClothingResDto.entityToDto(userClothing);
                userClothingResDtoListByType.add(dto);
            }
            return userClothingResDtoListByType;
        }
    }
}
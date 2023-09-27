package com.richminime.domain.item.service;

import com.richminime.domain.item.domain.Item;
import com.richminime.domain.item.domain.ItemType;
import com.richminime.domain.item.dto.ItemReqDto;
import com.richminime.domain.item.dto.ItemResDto;
import com.richminime.domain.item.dto.ItemSearchCondition;
import com.richminime.domain.item.dto.ItemUpdateReqDto;
import com.richminime.domain.item.repository.ItemRepository;
import com.richminime.domain.user.domain.User;
import com.richminime.domain.user.domain.UserType;
import com.richminime.domain.user.repository.UserRepository;
import com.richminime.global.util.SecurityUtils;
import com.richminime.global.util.jwt.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    /**
     * 상점에 등록된 아이템 전체 조회
     * 테마 상점에 등록된 아이템 전체를 조회합니다.
     * @return
     */
    @Transactional
    @Override
    public List<ItemResDto> findAllItem() {
        log.info("[테마 상점 전체 조회] 테마 상점에 등록된 테마 전체 조회");

        return itemRepository.findAll().stream()
                .map(item -> ItemResDto.entityToDto(item))
                .collect(Collectors.toList());
    }

    /**
     * 상점에 등록된 테마 카테고리별 조회
     * 사용자가 선택한 카테고리에 맞는 테마 리스트만 조회됩니다.
     * ※ 카테고리가 null일 경우, 전체 조회 메서드가 실행됩니다.
     * @param itemType
     * @return
     */
    @Transactional
    @Override
    public List<ItemResDto> findAllItemByType(ItemType itemType) {
        if(itemType == null)
            return findAllItem();

        log.info("[테마 상점 카테고리별 조회] 테마 카테고리별 조회");

        return itemRepository.findAllByItemType(itemType).stream()
                .map(item -> ItemResDto.entityToDto(item))
                .collect(Collectors.toList());
    }

    /**
     * 상점에 등록된 아이템 상세 조회
     * 사용자가 선택한 테마를 상세 조회하여 반환합니다.
     * 미리보기 기능이 활성화됩니다.
     * @param itemId
     * @return
     */
    @Transactional
    @Override
    public ItemResDto findItem(Long itemId) {
        log.info("[테마 상점 상세 조회] 테마 상점에 등록된 테마 상세 조회 요청. itemId : {}", itemId);

        Item item = itemRepository.findItemByItemId(itemId)
                .orElseThrow(() -> {
                    log.error("[테마 상점 상세 조회] 테마를 찾을 수 없습니다.");
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "테마를 찾을 수 없습니다.");
                });

        log.info("[테마 상점 상세 조회] 테마 상세 조회 완료.");
        return ItemResDto.entityToDto(item);
    }

    /**
     * 상점에 등록된 테마 조건별 조회
     * 사용자가 선택한 조건에 맞는 테마 리스트만 조회됩니다.
     *
     * 확장 기능입니다.
     * @param condition
     * @return
     */
    @Transactional
    @Override
    public List<ItemResDto> findAllItemByCondition(ItemSearchCondition condition) {
        log.info("[테마 상점 조건별 조회] 기능 확장 예정입니다.");
        return null;
    }

    /**
     * 로그인 유저가 관리자인지 확인하는 메서드
     * 관리자면 true 반환 / 일반회원이면 false 반환
     */
    public boolean isAdmin() {
        Long userId = securityUtils.getUserNo();
        log.info("[아이템 서비스] userId : {}", userId);

        User loginUser = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("[아이템 서비스] 로그인 유저를 찾을 수 없습니다.");
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "로그인 유저를 찾을 수 없습니다.");
                });

        if(loginUser.getUserType().equals(UserType.ROLE_ADMIN))
            return true;

        return false;
    }

    /**
     * 테마 상점에 테마 등록
     * 로그인 유저가 관리자일 경우에만 등록이 가능
     * @param itemReqDto
     * @return
     */
    @Transactional
    @Override
    public ItemResDto addItem(ItemReqDto itemReqDto) {
        log.info("[테마 상점 테마 등록] 테마 상점에 새로운 테마 등록 요청");

        // 관리자 유저인지 확인
        if(!isAdmin()){
            log.error("[테마 상점 테마 등록] 관리자 회원만 테마를 등록할 수 있습니다.");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "등록 권한이 없습니다.");
        }

        Item item = Item.builder()
                .itemName(itemReqDto.getItemName())
                .itemType(ItemType.getItemType(itemReqDto.getItemType()))
                .itemImg(itemReqDto.getItemImg())
                .itemInfo(itemReqDto.getItemInfo())
                .price(itemReqDto.getPrice())
                .build();

        itemRepository.save(item);

        log.info("[테마 상점 테마 등록] itemId : {}", item.getItemId());
        log.info("[테마 상점 테마 등록] 테마 등록 완료");

        return ItemResDto.entityToDto(item);
    }

    /**
     * 테마 상점에 등록된 테마 삭제
     * 로그인 유저가 관리자일 경우에만 삭제 가능
     * @param itemId
     */
    @Transactional
    @Override
    public void deleteItem(Long itemId) {
        log.info("[테마 상점 테마 삭제] 테마 상점에 등록된 테마 삭제 요청. itemId : {}", itemId);
        
        // 관리자 유저인지 확인
        if(!isAdmin()){
            log.error("[테마 상점 테마 삭제] 관리자 회원만 테마를 삭제할 수 있습니다.");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "삭제 권한이 없습니다.");
        }
        
        Item item = itemRepository.findItemByItemId(itemId)
                .orElseThrow(() -> {
                    log.error("[테마 상점 테마 삭제] 테마를 찾을 수 없습니다.");
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "테마를 찾을 수 없습니다.");
                });

        itemRepository.delete(item);
        log.info("[테마 상점 테마 삭제] 테마 상점에 등록된 테마 삭제 완료.");
    }

    /**
     * 테마 상점에 등록된 테마 정보 수정
     * 로그인 유저가 관리자일 경우에만 수정 가능
     * itemName, itemImg, itemInfo, price만 변경 가능합니다.(itemId, itemType은 변경 불가)
     * @param itemReqDto
     * @return
     */
    @Transactional
    @Override
    public ItemResDto updateItem(Long itemId, ItemUpdateReqDto itemReqDto) {
        log.info("[테마 상점 테마 수정] 테마 상점에 등록된 테마 수정 요청. itemId : {}", itemId);

        // 관리자 유저 확인
        if(!isAdmin()){
            log.error("[테마 상점 테마 수정] 관리자 회원만 테마를 수정할 수 있습니다.");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "수정 권한이 없습니다.");
        }
        
        Item item = itemRepository.findItemByItemId(itemId)
                .orElseThrow(() -> {
                    log.error("[테마 상점 테마 수정] 테마를 찾을 수 없습니다.");
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "테마를 찾을 수 없습니다.");
                });

        item.updateItem(itemReqDto);
        itemRepository.save(item);

        log.info("[테마 상점 테마 수정] 테마 상점에 등록된 테마 수정 완료.");
        return ItemResDto.entityToDto(item);
    }
}

package com.richminime.domain.item.service;

import com.richminime.domain.item.domain.Item;
import com.richminime.domain.item.domain.ItemType;
import com.richminime.domain.item.dto.ItemReqDto;
import com.richminime.domain.item.dto.ItemResDto;
import com.richminime.domain.item.dto.ItemSearchCondition;
import com.richminime.domain.item.dto.ItemUpdateReqDto;
import com.richminime.domain.item.repository.ItemRepository;
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

    /*
    itemId
    itemName
    itemType
    itemImg
    itemInfo
    price
     */

    private final ItemRepository itemRepository;

    /**
     * 상점에 등록된 아이템 전체 조회
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
     * 상점에 등록된 아이템 상세 조회
     * 사용자가 선택한 테마를 상세 조회하여 반환합니다.
     * 미리보기 기능이 활성화됩니다.
     * @param itemId
     * @param token
     * @return
     */
    @Transactional
    @Override
    public ItemResDto findItem(Long itemId, String token) {
        log.info("[테마 상점 상세 조회] 테마 상점에 등록된 테마 상세 조회 요청. itemId : {}, token : {}", itemId, token);

        Item item = itemRepository.findItemByItemId(itemId)
                .orElseThrow(() -> {
                    log.error("[테마 상점 상세 조회] 테마를 찾을 수 없습니다.");
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "테마를 찾을 수 없습니다.");
                });

        log.info("[테마 상점 상세 조회] 테마 상세 조회 완료.");
        return ItemResDto.entityToDto(item);
    }

    /**
     * 상점에 등록된 테마 카테고리별 조회
     * 사용자가 선택한 카테고리에 맞는 테마 리스트만 조회됩니다.
     * @param itemType
     * @param token
     * @return
     */
    @Transactional
    @Override
    public List<ItemResDto> findAllItemByType(ItemType itemType, String token) {
        log.info("[테마 상점 카테고리별 조회] 테마 카테고리별 조회");

        return itemRepository.findAllByItemType(itemType).stream()
                .map(item -> ItemResDto.entityToDto(item))
                .collect(Collectors.toList());
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
     * 테마 상점에 테마 등록
     * 로그인 유저가 관리자일 경우에만 등록이 가능
     * @param itemReqDto
     * @param token
     * @return
     */
    @Transactional
    @Override
    public ItemResDto addItem(ItemReqDto itemReqDto, String token) {
        log.info("[테마 상점 테마 등록] 테마 상점에 새로운 테마 등록 요청");
        // token => 관리자 유저인지 확인

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
     * @param token
     */
    @Transactional
    @Override
    public void deleteItem(Long itemId, String token) {
        log.info("[테마 상점 테마 삭제] 테마 상점에 등록된 테마 삭제 요청. itemId : {}, token : []", itemId, token);

        // 관리자 확인

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
     * @param token
     * @return
     */
    @Transactional
    @Override
    public ItemResDto updateItem(Long itemId, ItemUpdateReqDto itemReqDto, String token) {
        log.info("[테마 상점 테마 수정] 테마 상점에 등록된 테마 수정 요청. itemId : {}, token : {}", itemId, token);

        // 관리자 유저 확인

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

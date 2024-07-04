package com.back.takeeat.service;

import com.back.takeeat.common.exception.OtherMarketMenuException;
import com.back.takeeat.domain.cart.Cart;
import com.back.takeeat.domain.cart.CartMenu;
import com.back.takeeat.domain.market.Market;
import com.back.takeeat.domain.menu.Menu;
import com.back.takeeat.domain.option.Option;
import com.back.takeeat.dto.cart.request.AddToCartRequest;
import com.back.takeeat.dto.cart.response.CartListResponse;
import com.back.takeeat.dto.cart.response.CartMenuResponse;
import com.back.takeeat.dto.cart.response.CartOptionResponse;
import com.back.takeeat.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final MarketRepository marketRepository;
    private final MenuRepository menuRepository;
    private final OptionRepository optionRepository;
    private final CartRepository cartRepository;
    private final CartMenuRepository cartMenuRepository;
    private final CartOptionRepository cartOptionRepository;

    @Transactional(readOnly = true)
    public CartListResponse getList(Long memberId) {

        Cart cart = cartRepository.findByMemberIdWithMenu(memberId);

        List<CartMenuResponse> cartMenuResponses = new ArrayList<>();
        for (CartMenu cartMenu : cart.getCartMenus()) {
            cartMenuResponses.add(CartMenuResponse.create(cartMenu.getId(), cartMenu.getCartQuantity(),
                    cartMenu.getCartMenuPrice(), cartMenu.getMenu().getMenuName()));
        }

        List<CartOptionResponse> cartOptionResponses = cartMenuRepository.findByCartIdWithOption(cart.getId());

        Map<Long, List<CartOptionResponse>> cartOptionMapByCartMenuId = new HashMap<>();
        for (CartOptionResponse cartOption : cartOptionResponses) {
            if (!cartOptionMapByCartMenuId.containsKey(cartOption.getCartMenuId())) {
                cartOptionMapByCartMenuId.put(cartOption.getCartMenuId(), new ArrayList<>(List.of(cartOption)));
            } else {
                int flag = 0;
                for (CartOptionResponse cartOptionForIdCheck : cartOptionMapByCartMenuId.get(cartOption.getCartMenuId())) {
                    if (cartOption.getOptionCategoryId().equals(cartOptionForIdCheck.getOptionCategoryId())) {
                        flag = 1;
                        break;
                    }
                }
                if (flag == 0) {
                    cartOptionMapByCartMenuId.get(cartOption.getCartMenuId()).add(cartOption);
                }
            }
        }

        Map<Long, List<CartOptionResponse>> cartOptionMapByOptionCategoryId = cartOptionResponses.stream()
                .collect(Collectors.groupingBy(CartOptionResponse::getOptionCategoryId));;

        return CartListResponse.create((cart.getMarket() == null? null : cart.getMarket().getId()), (cart.getMarket() == null? null : cart.getMarket().getMarketName()),
                cartMenuResponses, cartOptionMapByCartMenuId, cartOptionMapByOptionCategoryId);
    }

    @Transactional
    public void add(AddToCartRequest addToCartRequest) throws OtherMarketMenuException {

        Cart cart = cartRepository.findByMemberId(addToCartRequest.getMemberId());
        //다른 가게의 메뉴가 이미 장바구니에 담겨 있는 경우
        if (cart.getMarket() != null && !cart.getMarket().getId().equals(addToCartRequest.getMarketId())) {
            throw new OtherMarketMenuException();
        }

        Market market = marketRepository.findById(addToCartRequest.getMarketId())
                .orElseThrow(NoSuchElementException::new);

        //장바구니에 첫 번째 메뉴를 담았다면 가게 정보 저장
        if (cart.getMarket() == null) {
            cart.addFirstMenu(market);
        }

        //메뉴 저장
        Menu menu = menuRepository.findById(addToCartRequest.getMenuId())
                .orElseThrow(NoSuchElementException::new);
        CartMenu cartMenu = cartMenuRepository.save(addToCartRequest.toCartMenu(cart, menu));

        //옵션 저장
        for (Long optionId : addToCartRequest.getOptionIds()) {
            Option option = optionRepository.findById(optionId)
                    .orElseThrow(NoSuchElementException::new);

            cartOptionRepository.save(addToCartRequest.toCartOption(cartMenu, option));
        }

    }

    @Transactional
    public void updateQuantity(Long cartMenuId, int quantity) {

        CartMenu cartMenu = cartMenuRepository.findById(cartMenuId)
                .orElseThrow(NoSuchElementException::new);

        cartMenu.updateQuantity(quantity);
    }
}
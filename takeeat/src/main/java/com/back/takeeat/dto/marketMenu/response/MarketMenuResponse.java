package com.back.takeeat.dto.marketMenu.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketMenuResponse {

    private MarketResponse marketResponse;

    private List<Long> menuCategoryIds;
    private Map<Long, MenuCategoryResponse> menuCategoryMapById;
    private Map<Long, List<MenuResponse>> menuMapByMenuCategoryId;

    private List<Long> menuIds;
    private Map<Long, MenuResponse> menuMapById;
    private Map<Long, List<OptionCategoryResponse>> optionCategoryMapByMenuId;
    private Map<Long, List<OptionResponse>> optionMapByOptionCategoryId;

    public static MarketMenuResponse create(MarketResponse marketResponse, List<Long> menuCategoryIds, Map<Long, MenuCategoryResponse> menuCategoryMapById,
                                            Map<Long, List<MenuResponse>> menuMapByMenuCategoryId, List<Long> menuIds, Map<Long, MenuResponse> menuMapById,
                                            Map<Long, List<OptionCategoryResponse>> optionCategoryMapByMenuId, Map<Long, List<OptionResponse>> optionMapByOptionCategoryId) {
        return MarketMenuResponse.builder()
                .marketResponse(marketResponse)
                .menuCategoryIds(menuCategoryIds)
                .menuCategoryMapById(menuCategoryMapById)
                .menuMapByMenuCategoryId(menuMapByMenuCategoryId)
                .menuIds(menuIds)
                .menuMapById(menuMapById)
                .optionCategoryMapByMenuId(optionCategoryMapByMenuId)
                .optionMapByOptionCategoryId(optionMapByOptionCategoryId)
                .build();
    }

    public MenuCategoryResponse getMenuCategoryResponse(Long menuCategoryId) {
        return menuCategoryMapById.get(menuCategoryId);
    }

    public List<MenuResponse> getMenuResponses(Long menuCategoryId) {
        return menuMapByMenuCategoryId.get(menuCategoryId);
    }

    public MenuResponse getMenuResponse(Long menuId) {
        return menuMapById.get(menuId);
    }

    public List<OptionCategoryResponse> getOptionCategoryResponses(Long menuId) {
        return optionCategoryMapByMenuId.get(menuId);
    }

    public List<OptionResponse> getOptionResponses(Long optionCategoryId) {
        return optionMapByOptionCategoryId.get(optionCategoryId);
    }
}

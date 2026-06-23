package com.wtu.syserver.common.constants;

import java.util.Map;

public class CityConstant {
    // cityNo -> CityItem 映射Map
    public static final Map<String, CityItem> CITY_NO_ITEM_MAP = Map.ofEntries(
            Map.entry("10119", new CityItem("10119", "北京", 1)),
            Map.entry("10621", new CityItem("10621", "上海", 1)),
            Map.entry("10458", new CityItem("10458", "武汉", 2)),
            Map.entry("10216", new CityItem("10216", "杭州", 2)),
            Map.entry("10455", new CityItem("10455", "荆州", 3))
    );

    /**
     * 城市内部实体常量类
     *
     * @param cityNo getter
     */
    public record CityItem(String cityNo, String cityName, int cityType) {

    }
}

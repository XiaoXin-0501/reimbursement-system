package com.wtu.syserver.common.constants;

import java.math.BigDecimal;
import java.util.Map;

public class SubsidyConstant {
    /**
     * 餐费补助：key=城市等级 1一线/2二线/3三线，value=每日补助金额
     */
    public static final Map<Integer, BigDecimal> MEAL_SUBSIDY_MAP = Map.ofEntries(
            Map.entry(1, new BigDecimal("100")),
            Map.entry(2, new BigDecimal("80")),
            Map.entry(3, new BigDecimal("50"))
    );

    /**
     * 交通补助：统一标准，所有城市 40 元/天
     */
    public static final BigDecimal TRAFFIC_SUBSIDY = new BigDecimal("40");

    /**
     * 通讯补助：统一标准，所有城市 40 元/天
     */
    public static final BigDecimal COMMUNICATION_SUBSIDY = new BigDecimal("40");
}

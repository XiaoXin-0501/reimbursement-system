package com.wtu.syserver.common.constants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 报销公司常量映射
 * key: reimCompanyId
 * value: reimCompanyName
 */
public class ReimCompanyConstant {
    // 全局不可变常量Map
    public static final Map<String, String> REIM_COMPANY_ID_NAME_MAP;

    static {
        Map<String, String> tempMap = new HashMap<>();
        tempMap.put("1C54557F1782E000", "胜意科技北京分公司");
        tempMap.put("19218A262C976000", "胜意科技上海分公司");
        tempMap.put("1C61686865DA8000", "胜意科技武汉分公司");
        tempMap.put("1717271D1DA15000", "胜意科技杭州分公司");
        tempMap.put("16AE93CC7EF92002", "胜意科技荆州分公司");
        // 包装为不可变Map，防止外部修改
        REIM_COMPANY_ID_NAME_MAP = Collections.unmodifiableMap(tempMap);
    }
}

package com.wtu.syserver.common.constants;

import java.util.Map;

/**
 * 报销部门控件常量
 * key: reimDepartmentId
 * value: reimDepartmentName
 */
public class ReimDepartmentConstant {
    public static final Map<String, String> DEPARTMENT_ID_NAME_MAP = Map.ofEntries(
            Map.entry("13AB8D7B52A9B002", "客户成功事业部"),
            Map.entry("13BFD31C6029A002", "企业消费事业部"),
            Map.entry("14515BB4BFB92003", "企业费控事业部"),
            Map.entry("19206611C47A6000", "集采事业部"),
            Map.entry("19D32F9FE9647000", "航旅事业部"),
            Map.entry("13C7E2BAE0393001", "运营事业部"),
            Map.entry("14055D22BB808001", "营销事业部")
    );
}

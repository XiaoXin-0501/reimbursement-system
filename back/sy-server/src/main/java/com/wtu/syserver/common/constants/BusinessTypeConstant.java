package com.wtu.syserver.common.constants;

import java.util.Map;

public class BusinessTypeConstant {
    public static final Map<String, String> BUSINESS_TYPE_ID_NAME_MAP = Map.ofEntries(
            Map.entry("18F0916A8C2C4000", "员工差旅活动"),
            Map.entry("18F091913EEC4000", "境内出差"),
            Map.entry("1B5FEB7DD4396000", "项目出差"),
            Map.entry("1A92E43082EFC000", "市场拓展出差"),
            Map.entry("13AB3A4138008001", "境外出差"),
            Map.entry("13AB3A4248008002", "国外考察"),
            Map.entry("13AB3A4154008001", "售后维护出差"),
            Map.entry("13AB3A4172008001", "人力资源"),
            Map.entry("13AB3A418F808001", "个人团队培训"),
            Map.entry("13AB3A41AC408001", "招聘会"),
            Map.entry("13AB3A41CD808002", "员工福利"),
            Map.entry("13AB3A41ED408002", "员工旅游"),
            Map.entry("13AB3A420CC08002", "员工团建"),
            Map.entry("13AB3A422A808001", "员工体检")
    );
}

package com.wtu.syserver.common.constants;

import java.util.Map;

/**
 * 项目控件常量
 * key: projectId
 * value: projectName
 */
public class ProjectConstant {
    public static final Map<String, String> PROJECT_ID_NAME_MAP = Map.ofEntries(
            Map.entry("12BC248B25083001", "非项目类费用归集"),
            Map.entry("1C811ABF96195000", "华中客户定制化项目"),
            Map.entry("1C5931735AC4A000", "华南客户定制化项目"),
            Map.entry("1771EC45F2443000", "华北客户定制化项目"),
            Map.entry("1762792DB4E9A002", "华东客户定制化项目"),
            Map.entry("17071065FC29A002", "西南客户定制化项目"),
            Map.entry("162664EBE9ABE001", "西北客户定制化项目"),
            Map.entry("162664B8526BE002", "东北客户定制化项目")
    );
}

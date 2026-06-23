package com.wtu.syserver;

import com.wtu.syserver.convert.ReimbursementConvert;
import com.wtu.syserver.dto.ReimbursementDTO;
import com.wtu.syserver.entity.Reimbursement;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class MapStructTest {

    @Autowired
    private ReimbursementConvert reimbursementConvert;

    @Test
    void testDTOToEntity() {

        // 1. 构造 DTO
        ReimbursementDTO dto = new ReimbursementDTO();
        dto.setTitle("差旅报销测试");
        dto.setReimburserId("U1001");
        dto.setReimburserName("张三");
        dto.setReimDepartmentId("D01");
        dto.setReimDepartmentName("研发部");
        dto.setReimCompanyId("C01");
        dto.setReimCompanyName("WTU公司");
        dto.setBusinessTypeId("B01");
        dto.setBusinessTypeName("出差");
        dto.setReason("武汉出差会议");
        dto.setRemarks("测试MapStruct");

        // 2. 转换
        Reimbursement entity = reimbursementConvert.toEntity(dto);

        // 3. 打印结果（方便你观察）
        System.out.println("===== MapStruct 转换结果 =====");
        System.out.println("title = " + entity.getTitle());
        System.out.println("name = " + entity.getReimburserName());
        System.out.println("dept = " + entity.getReimDepartmentName());
        System.out.println("company = " + entity.getReimCompanyName());

        // 4. 断言（自动验证）
        assertNotNull(entity);
        assertEquals("差旅报销测试", entity.getTitle());
        assertEquals("张三", entity.getReimburserName());
        assertEquals("研发部", entity.getReimDepartmentName());
        assertEquals("WTU公司", entity.getReimCompanyName());
    }

    @Test
    void testBeanInjection() {
        // 只验证是否成功注入（非常关键）
        assertNotNull(reimbursementConvert);
    }
}
package com.wtu.syserver.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * JDK17 + SpringBoot3 Jackson全局日期序列化/反序列化配置
 * 1. 全局默认格式：yyyy-MM-dd 仅年月日
 * 2. 字段添加@JsonFormat可局部覆盖全局，实现 yyyy-MM-dd HH:mm:ss 带时分秒
 * 3. 统一时区GMT+8，解决时差8小时问题
 * 4. 同时兼容 java.util.Date、Java8+ LocalDate、LocalDateTime
 * 5. 忽略前端多余未知字段，避免JSON解析报错
 */
@Configuration
public class GlobalJacksonDateConfig {

    // 全局默认：仅年月日
    private static final String GLOBAL_DATE_PATTERN = "yyyy-MM-dd";
    // 局部自定义：年月日+时分秒（用于注解覆盖）
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();

        // ========== 1、全局 java.util.Date 配置 ==========
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(GLOBAL_DATE_PATTERN);
        // 设置东八区时区
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        // Date全局序列化、反序列化统一为 yyyy-MM-dd
        objectMapper.setDateFormat(simpleDateFormat);

        // ========== 2、Java17 新时间类型 LocalDate / LocalDateTime 适配 ==========
        SimpleModule java8TimeModule = new SimpleModule();
        DateTimeFormatter globalDateFormatter = DateTimeFormatter.ofPattern(GLOBAL_DATE_PATTERN);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);

        // LocalDate 全局：yyyy-MM-dd
        java8TimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(globalDateFormatter));
        java8TimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(globalDateFormatter));

        // LocalDateTime 全局默认也按 yyyy-MM-dd 解析序列化
        java8TimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(globalDateFormatter));
        java8TimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(globalDateFormatter));

        objectMapper.registerModule(java8TimeModule);

        // ========== 3、通用Jackson全局优化配置 ==========
        // 忽略JSON存在、DTO不存在的字段，防止 Unrecognized field 异常
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 禁止将日期序列化为时间戳数字
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        // 序列化时自动忽略值为null的字段（按需删除此配置）
        objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);

        return objectMapper;
    }
}

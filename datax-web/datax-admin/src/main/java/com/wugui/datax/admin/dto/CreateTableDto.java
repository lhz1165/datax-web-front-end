package com.wugui.datax.admin.dto;

import lombok.Data;

import java.util.List;

/**
 * 创建表请求DTO
 */
@Data
public class CreateTableDto {
    /**
     * 数据源ID
     */
    private Long datasourceId;

    /**
     * 表名
     */
    private String tableName;

    /**
     * 表注释
     */
    private String tableComment;

    /**
     * Schema（PostgreSQL需要）
     */
    private String tableSchema;

    /**
     * 字段列表
     */
    private List<ColumnDto> columns;

    /**
     * 字段定义
     */
    @Data
    public static class ColumnDto {
        /**
         * 字段名
         */
        private String name;

        /**
         * 数据类型
         */
        private String type;

        /**
         * 长度
         */
        private Integer length;

        /**
         * 是否可为空
         */
        private Boolean nullable;

        /**
         * 是否主键
         */
        private Boolean primaryKey;

        /**
         * 是否自增
         */
        private Boolean autoIncrement;

        /**
         * 默认值
         */
        private String defaultValue;

        /**
         * 注释
         */
        private String comment;
    }
}

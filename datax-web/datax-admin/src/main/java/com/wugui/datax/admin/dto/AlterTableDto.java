package com.wugui.datax.admin.dto;

import lombok.Data;

import java.util.List;

/**
 * 修改表结构DTO
 */
@Data
public class AlterTableDto {

    /**
     * 数据源ID
     */
    private Long datasourceId;

    /**
     * 表名
     */
    private String tableName;

    /**
     * Schema（PostgreSQL使用）
     */
    private String tableSchema;

    /**
     * 修改类型：ADD_COLUMNS, DROP_COLUMN, MODIFY_COLUMN, MODIFY_COMMENT
     */
    private String alterType;

    /**
     * 字段名（用于删除或修改字段）
     */
    private String columnName;

    /**
     * 新字段名（用于重命名字段）
     */
    private String newName;

    /**
     * 新数据类型（用于修改字段）
     */
    private String newType;

    /**
     * 新长度（用于修改字段）
     */
    private Integer newLength;

    /**
     * 新注释（用于修改字段或表注释）
     */
    private String newComment;

    /**
     * 表注释（用于修改表注释）
     */
    private String tableComment;

    /**
     * 新增字段列表
     */
    private List<ColumnDto> columns;

    @Data
    public static class ColumnDto {
        private String name;
        private String type;
        private Integer length;
        private Boolean nullable;
        private String defaultValue;
        private String comment;
    }
}

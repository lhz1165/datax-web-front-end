package com.wugui.datax.admin.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * API 发布配置实体类（job_api）
 *
 * 用于描述基于数据源/任务发布出来的 HTTP API 元数据。
 */
@Data
@ApiModel("API发布配置")
@TableName("job_api")
public class JobApi extends Model<JobApi> {

    /**
     * 自增主键
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty("自增主键")
    private Long id;

    /**
     * API 名称
     */
    @ApiModelProperty("API名称")
    private String name;

    /**
     * 请求路径（RESTful Path）
     * 例如：/api/demo/user/list
     */
    @ApiModelProperty("请求路径(Path)")
    private String path;

    /**
     * HTTP 方法：GET/POST/PUT/DELETE
     */
    @ApiModelProperty("请求方式(GET/POST/PUT/DELETE)")
    private String method;

    /**
     * 返回类型，例如：application/json
     */
    @ApiModelProperty("返回类型，例如：application/json")
    private String responseType;

    /**
     * 绑定的数据源ID（可选）
     */
    @ApiModelProperty("关联的数据源ID")
    private Long datasourceId;

    /**
     * 数据源名称（非表字段，查询时填充）
     */
    @TableField(exist = false)
    @ApiModelProperty("数据源名称")
    private String datasourceName;

    /**
     * 数据源类型（非表字段，查询时填充，如：mysql、postgresql等）
     */
    @TableField(exist = false)
    @ApiModelProperty("数据源类型")
    private String dataSource;

    /**
     * 所属项目ID（可选），与 JobInfo 的 projectId 对应
     */
    @ApiModelProperty("所属项目ID")
    private Integer projectId;

    /**
     * 描述信息
     */
    @ApiModelProperty("描述信息")
    private String description;

    /**
     * 是否启用：2已发布 1待发布，0禁用
     */
    @ApiModelProperty("状态：1待发布，0禁用 ")
    private Integer status;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建人", hidden = true)
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @JSONField(format = "yyyy/MM/dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间", hidden = true)
    private Date createDate;


    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JSONField(format = "yyyy/MM/dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "更新时间", hidden = true)
    private Date updateDate;

    /**
     * 备注
     */
    @ApiModelProperty("备注")
    private String comments;


    @ApiModelProperty("参数配置")
    private String config;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }
}

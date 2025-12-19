package com.wugui.datax.admin.controller;

import com.baomidou.mybatisplus.extension.api.R;
import com.wugui.datax.admin.dto.AlterTableDto;
import com.wugui.datax.admin.dto.CreateTableDto;
import com.wugui.datax.admin.service.DatasourceQueryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.wugui.datax.admin.tool.database.ColumnInfo;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * 查询数据库表名，字段的控制器
 *
 * @author jingwk
 * @ClassName MetadataController
 * @Version 2.1.2
 * @since 2020/05/31 20:48
 */
@RestController
@RequestMapping("api/metadata")
@Api(tags = "jdbc数据库查询控制器")
public class MetadataController extends BaseController {

    @Autowired
    private DatasourceQueryService datasourceQueryService;

    /**
     * 根据数据源id获取mongo库名
     *
     * @param datasourceId
     * @return
     */
    @GetMapping("/getDBs")
    @ApiOperation("根据数据源id获取mongo库名")
    public R<List<String>> getDBs(Long datasourceId) throws IOException {
        return success(datasourceQueryService.getDBs(datasourceId));
    }


    /**
     * 根据数据源id,dbname获取CollectionNames
     *
     * @param datasourceId
     * @return
     */
    @GetMapping("/collectionNames")
    @ApiOperation("根据数据源id,dbname获取CollectionNames")
    public R<List<String>> getCollectionNames(Long datasourceId,String dbName) throws IOException {
        return success(datasourceQueryService.getCollectionNames(datasourceId,dbName));
    }

    /**
     * 获取PG table schema
     *
     * @param datasourceId
     * @return
     */
    @GetMapping("/getDBSchema")
    @ApiOperation("根据数据源id获取 db schema")
    public R<List<String>> getTableSchema(Long datasourceId) {
        return success(datasourceQueryService.getTableSchema(datasourceId));
    }

    /**
     * 根据数据源id获取可用表名
     *
     * @param datasourceId
     * @return
     */
    @GetMapping("/getTables")
    @ApiOperation("根据数据源id获取可用表名")
    public R<List<String>> getTableNames(Long datasourceId,String tableSchema) throws IOException {
        List<String> tables = datasourceQueryService.getTables(datasourceId, tableSchema);
        return success(tables);
    }

    /**
     * 根据数据源id和表名获取所有字段
     *
     * @param datasourceId 数据源id
     * @param tableName    表名
     * @return
     */
    @GetMapping("/getColumns")
    @ApiOperation("根据数据源id和表名获取所有字段")
    public R<List<String>> getColumns(Long datasourceId, String tableName) throws IOException {
        return success(datasourceQueryService.getColumns(datasourceId, tableName));
    }

    /**
     * 根据数据源id和sql语句获取所有字段
     *
     * @param datasourceId 数据源id
     * @param querySql     表名
     * @return
     */
    @GetMapping("/getColumnsByQuerySql")
    @ApiOperation("根据数据源id和sql语句获取所有字段")
    public R<List<String>> getColumnsByQuerySql(Long datasourceId, String querySql) throws SQLException {
        return success(datasourceQueryService.getColumnsByQuerySql(datasourceId, querySql));
    }

    /**
     * 根据数据源id和表名获取所有字段的详细信息
     *
     * @param datasourceId 数据源id
     * @param tableName    表名
     * @param tableSchema  Schema（PostgreSQL需要）
     * @return 字段详细信息列表（包含名称、类型、备注、是否可空、是否主键等）
     */
    @GetMapping("/getColumnsInfo")
    @ApiOperation("根据数据源id和表名获取所有字段的详细信息")
    public R<List<ColumnInfo>> getColumnsInfo(Long datasourceId, String tableName, String tableSchema) throws IOException {
        return success(datasourceQueryService.getColumnsInfo(datasourceId, tableName, tableSchema));
    }

    /**
     * 创建数据库表
     *
     * @param dto 创建表请求参数
     * @return 是否创建成功
     */
    @PostMapping("/createTable")
    @ApiOperation("创建数据库表（支持PostgreSQL和MySQL）")
    public R<Boolean> createTable(@RequestBody CreateTableDto dto) throws SQLException {
        return success(datasourceQueryService.createTable(dto));
    }

    /**
     * 删除数据库表
     *
     * @param datasourceId 数据源ID
     * @param tableName    表名
     * @param tableSchema  Schema（PostgreSQL需要）
     * @return 是否删除成功
     */
    @DeleteMapping("/dropTable")
    @ApiOperation("删除数据库表（支持PostgreSQL和MySQL）")
    public R<Boolean> dropTable(Long datasourceId, String tableName, String tableSchema) throws SQLException {
        return success(datasourceQueryService.dropTable(datasourceId, tableName, tableSchema));
    }

    /**
     * 修改表结构
     *
     * @param dto 修改表请求参数
     * @return 是否修改成功
     */
    @PostMapping("/alterTable")
    @ApiOperation("修改表结构（添加字段、删除字段、修改字段、修改表注释）")
    public R<Boolean> alterTable(@RequestBody AlterTableDto dto) throws SQLException {
        return success(datasourceQueryService.alterTable(dto));
    }

    /**
     * 获取视图列表
     *
     * @param datasourceId 数据源ID
     * @param tableSchema  Schema（PostgreSQL需要）
     * @return 视图名称列表
     */
    @GetMapping("/getViews")
    @ApiOperation("根据数据源id获取视图列表")
    public R<List<String>> getViews(Long datasourceId, String tableSchema) throws IOException {
        return success(datasourceQueryService.getViews(datasourceId, tableSchema));
    }

    /**
     * 创建视图
     *
     * @param datasourceId 数据源ID
     * @param viewSql      视图SQL（包含 CREATE VIEW）
     * @return 是否创建成功
     */
    @PostMapping("/createView")
    @ApiOperation("创建视图")
    public R<Boolean> createView(@RequestBody com.fasterxml.jackson.databind.node.ObjectNode body) throws SQLException {
        Long datasourceId = body.get("datasourceId").asLong();
        String viewSql = body.get("viewSql").asText();
        return success(datasourceQueryService.createView(datasourceId, viewSql));
    }

    /**
     * 获取索引列表
     *
     * @param datasourceId 数据源ID
     * @param tableName    表名/视图名
     * @param tableSchema  Schema（PostgreSQL需要）
     * @return 索引列表
     */
    @GetMapping("/getIndexes")
    @ApiOperation("根据数据源id和表名获取索引列表")
    public R<List<java.util.Map<String, Object>>> getIndexes(Long datasourceId, String tableName, String tableSchema) throws IOException {
        return success(datasourceQueryService.getIndexes(datasourceId, tableName, tableSchema));
    }

    /**
     * 创建索引
     *
     * @param body JSON 包含 datasourceId, indexSql
     * @return 是否成功
     */
    @PostMapping("/createIndex")
    @ApiOperation("创建索引")
    public R<Boolean> createIndex(@RequestBody com.fasterxml.jackson.databind.node.ObjectNode body) throws SQLException {
        Long datasourceId = body.get("datasourceId").asLong();
        String indexSql = body.get("indexSql").asText();
        return success(datasourceQueryService.createIndex(datasourceId, indexSql));
    }
}

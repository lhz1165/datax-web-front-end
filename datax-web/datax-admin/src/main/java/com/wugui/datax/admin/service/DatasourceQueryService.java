package com.wugui.datax.admin.service;

import com.wugui.datax.admin.dto.AlterTableDto;
import com.wugui.datax.admin.dto.CreateTableDto;
import com.wugui.datax.admin.tool.database.ColumnInfo;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 数据库查询服务
 *
 * @author zhouhongfa@gz-yibo.com
 * @ClassName JdbcDatasourceQueryService
 * @Version 1.0
 * @since 2019/7/31 20:50
 */
public interface DatasourceQueryService {

    /**
     * 获取db列表
     * @param id
     * @return
     */
    List<String> getDBs(Long id) throws IOException;

    /**
     * 根据数据源表id查询出可用的表
     *
     * @param id
     * @return
     */
    List<String> getTables(Long id,String tableSchema) throws IOException;

    /**
     * 获取CollectionNames
     * @param dbName
     * @return
     */
    List<String> getCollectionNames(long id,String dbName) throws IOException;

    /**
     * 根据数据源id，表名查询出该表所有字段
     *
     * @param id
     * @return
     */
    List<String> getColumns(Long id, String tableName) throws IOException;

    /**
     * 根据 sql 语句获取字段
     *
     * @param datasourceId
     * @param querySql
     * @return
     */
    List<String> getColumnsByQuerySql(Long datasourceId, String querySql) throws SQLException;

    /**
     * 获取PG table schema
     * @param id
     * @return
     */
    List<String> getTableSchema(Long id);

    /**
     * 根据数据源id，表名查询出该表所有字段的详细信息
     *
     * @param id        数据源id
     * @param tableName 表名
     * @param tableSchema Schema（PostgreSQL需要）
     * @return 字段详细信息列表
     */
    List<ColumnInfo> getColumnsInfo(Long id, String tableName, String tableSchema) throws IOException;

    /**
     * 创建数据库表
     *
     * @param dto 创建表请求参数
     * @return 是否创建成功
     */
    Boolean createTable(CreateTableDto dto) throws SQLException;

    /**
     * 获取视图列表
     *
     * @param id          数据源ID
     * @param tableSchema Schema（PostgreSQL需要）
     * @return 视图名称列表
     */
    List<String> getViews(Long id, String tableSchema) throws IOException;

    /**
     * 创建视图（执行视图 SQL）
     *
     * @param datasourceId 数据源ID
     * @param viewSql      视图SQL（包含 CREATE VIEW）
     * @return 是否创建成功
     */
    Boolean createView(Long datasourceId, String viewSql) throws SQLException;

    /**
     * 获取索引列表
     *
     * @param datasourceId 数据源ID
     * @param tableName    表/视图名
     * @param tableSchema  schema（PostgreSQL需要）
     * @return 索引列表
     */
    List<Map<String, Object>> getIndexes(Long datasourceId, String tableName, String tableSchema) throws IOException;

    /**
     * 创建索引（执行 SQL）
     *
     * @param datasourceId 数据源ID
     * @param indexSql     索引SQL（CREATE INDEX ...）
     * @return 是否成功
     */
    Boolean createIndex(Long datasourceId, String indexSql) throws SQLException;

    /**
     * 删除数据库表
     *
     * @param datasourceId 数据源ID
     * @param tableName    表名
     * @param tableSchema  Schema（PostgreSQL需要）
     * @return 是否删除成功
     */
    Boolean dropTable(Long datasourceId, String tableName, String tableSchema) throws SQLException;

    /**
     * 修改表结构
     */
    Boolean alterTable(AlterTableDto dto) throws SQLException;
}

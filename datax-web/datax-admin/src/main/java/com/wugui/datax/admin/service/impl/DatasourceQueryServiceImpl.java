package com.wugui.datax.admin.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.google.common.collect.Lists;
import com.wugui.datax.admin.dto.AlterTableDto;
import com.wugui.datax.admin.dto.CreateTableDto;
import com.wugui.datax.admin.entity.JobDatasource;
import com.wugui.datax.admin.service.DatasourceQueryService;
import com.wugui.datax.admin.service.JobDatasourceService;
import com.wugui.datax.admin.tool.database.ColumnInfo;
import com.wugui.datax.admin.tool.query.*;
import com.wugui.datax.admin.util.JdbcConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * datasource query
 *
 * @author zhouhongfa@gz-yibo.com
 * @ClassName JdbcDatasourceQueryServiceImpl
 * @Version 1.0
 * @since 2019/7/31 20:51
 */
@Service
public class DatasourceQueryServiceImpl implements DatasourceQueryService {

    @Autowired
    private JobDatasourceService jobDatasourceService;

    @Override
    public List<String> getDBs(Long id) throws IOException {
        //获取数据源对象
        JobDatasource datasource = jobDatasourceService.getById(id);
        return new MongoDBQueryTool(datasource).getDBNames();
    }


    @Override
    public List<String> getTables(Long id, String tableSchema) throws IOException {
        //获取数据源对象
        JobDatasource datasource = jobDatasourceService.getById(id);
        //queryTool组装
        if (ObjectUtil.isNull(datasource)) {
            return Lists.newArrayList();
        }
        if (JdbcConstants.HBASE.equals(datasource.getDatasource())) {
            return new HBaseQueryTool(datasource).getTableNames();
        } else if (JdbcConstants.MONGODB.equals(datasource.getDatasource())) {
            return new MongoDBQueryTool(datasource).getCollectionNames(datasource.getDatabaseName());
        } else {
            BaseQueryTool qTool = QueryToolFactory.getByDbType(datasource);
            if(StringUtils.isBlank(tableSchema)){
                return qTool.getTableNames();
            }else{
                return qTool.getTableNames(tableSchema);
            }
        }
    }

    @Override
    public List<String> getTableSchema(Long id) {
        //获取数据源对象
        JobDatasource datasource = jobDatasourceService.getById(id);
        //queryTool组装
        if (ObjectUtil.isNull(datasource)) {
            return Lists.newArrayList();
        }
        BaseQueryTool qTool = QueryToolFactory.getByDbType(datasource);
        return qTool.getTableSchema();
    }

    @Override
    public List<String> getCollectionNames(long id, String dbName) throws IOException {
        //获取数据源对象
        JobDatasource datasource = jobDatasourceService.getById(id);
        //queryTool组装
        if (ObjectUtil.isNull(datasource)) {
            return Lists.newArrayList();
        }
        return new MongoDBQueryTool(datasource).getCollectionNames(dbName);
    }


    @Override
    public List<String> getColumns(Long id, String tableName) throws IOException {
        //获取数据源对象
        JobDatasource datasource = jobDatasourceService.getById(id);
        //queryTool组装
        if (ObjectUtil.isNull(datasource)) {
            return Lists.newArrayList();
        }
        if (JdbcConstants.HBASE.equals(datasource.getDatasource())) {
            return new HBaseQueryTool(datasource).getColumns(tableName);
        } else if (JdbcConstants.MONGODB.equals(datasource.getDatasource())) {
            return new MongoDBQueryTool(datasource).getColumns(tableName);
        } else {
            BaseQueryTool queryTool = QueryToolFactory.getByDbType(datasource);
            return queryTool.getColumnNames(tableName, datasource.getDatasource());
        }
    }

    @Override
    public List<String> getColumnsByQuerySql(Long datasourceId, String querySql) throws SQLException {
        //获取数据源对象
        JobDatasource jdbcDatasource = jobDatasourceService.getById(datasourceId);
        //queryTool组装
        if (ObjectUtil.isNull(jdbcDatasource)) {
            return Lists.newArrayList();
        }
        BaseQueryTool queryTool = QueryToolFactory.getByDbType(jdbcDatasource);
        return queryTool.getColumnsByQuerySql(querySql);
    }

    @Override
    public List<ColumnInfo> getColumnsInfo(Long id, String tableName) throws IOException {
        //获取数据源对象
        JobDatasource datasource = jobDatasourceService.getById(id);
        //queryTool组装
        if (ObjectUtil.isNull(datasource)) {
            return Lists.newArrayList();
        }
        BaseQueryTool queryTool = QueryToolFactory.getByDbType(datasource);
        return queryTool.getColumns(tableName);
    }

    @Override
    public Boolean createTable(CreateTableDto dto) throws SQLException {
        //获取数据源对象
        JobDatasource datasource = jobDatasourceService.getById(dto.getDatasourceId());
        if (ObjectUtil.isNull(datasource)) {
            throw new RuntimeException("数据源不存在");
        }

        //生成建表SQL
        String createTableSql = generateCreateTableSql(datasource.getDatasource(), dto);

        //执行建表SQL
        BaseQueryTool queryTool = QueryToolFactory.getByDbType(datasource);
        queryTool.executeCreateTableSql(createTableSql);

        return true;
    }

    @Override
    public List<String> getViews(Long id, String tableSchema) throws IOException {
        //获取数据源对象
        JobDatasource datasource = jobDatasourceService.getById(id);
        if (ObjectUtil.isNull(datasource)) {
            return Lists.newArrayList();
        }
        // 目前仅支持 MySQL / PostgreSQL 视图查询
        if (JdbcConstants.MYSQL.equals(datasource.getDatasource()) ||
            JdbcConstants.POSTGRESQL.equals(datasource.getDatasource())) {
            BaseQueryTool qTool = QueryToolFactory.getByDbType(datasource);
            return qTool.getViews(tableSchema);
        }
        return Lists.newArrayList();
    }

    @Override
    public Boolean createView(Long datasourceId, String viewSql) throws SQLException {
        //获取数据源对象
        JobDatasource datasource = jobDatasourceService.getById(datasourceId);
        if (ObjectUtil.isNull(datasource)) {
            throw new RuntimeException("数据源不存在");
        }
        if (StringUtils.isBlank(viewSql)) {
            throw new RuntimeException("视图SQL不能为空");
        }
        BaseQueryTool queryTool = QueryToolFactory.getByDbType(datasource);
        queryTool.executeCreateTableSql(viewSql);
        return true;
    }

    @Override
    public List<Map<String, Object>> getIndexes(Long datasourceId, String tableName, String tableSchema) throws IOException {
        JobDatasource datasource = jobDatasourceService.getById(datasourceId);
        if (ObjectUtil.isNull(datasource)) {
            return Lists.newArrayList();
        }
        BaseQueryTool queryTool = QueryToolFactory.getByDbType(datasource);
        return queryTool.getIndexes(tableName, tableSchema);
    }

    @Override
    public Boolean createIndex(Long datasourceId, String indexSql) throws SQLException {
        JobDatasource datasource = jobDatasourceService.getById(datasourceId);
        if (ObjectUtil.isNull(datasource)) {
            throw new RuntimeException("数据源不存在");
        }
        if (StringUtils.isBlank(indexSql)) {
            throw new RuntimeException("索引SQL不能为空");
        }
        BaseQueryTool queryTool = QueryToolFactory.getByDbType(datasource);
        queryTool.executeCreateTableSql(indexSql);
        return true;
    }

    /**
     * 根据数据库类型生成建表SQL
     */
    private String generateCreateTableSql(String dbType, CreateTableDto dto) {
        if (JdbcConstants.POSTGRESQL.equals(dbType)) {
            return generatePostgresqlCreateTableSql(dto);
        } else if (JdbcConstants.MYSQL.equals(dbType)) {
            return generateMysqlCreateTableSql(dto);
        } else {
            throw new RuntimeException("暂不支持该数据库类型: " + dbType);
        }
    }

    /**
     * 生成PostgreSQL建表SQL
     */
    private String generatePostgresqlCreateTableSql(CreateTableDto dto) {
        StringBuilder sql = new StringBuilder();
        String tableName = StringUtils.isNotBlank(dto.getTableSchema())
                ? "\"" + dto.getTableSchema() + "\".\"" + dto.getTableName() + "\""
                : "\"" + dto.getTableName() + "\"";

        sql.append("CREATE TABLE ").append(tableName).append(" (\n");

        // 字段定义
        List<String> columnDefs = Lists.newArrayList();
        List<String> primaryKeys = Lists.newArrayList();

        for (CreateTableDto.ColumnDto col : dto.getColumns()) {
            StringBuilder colDef = new StringBuilder();
            colDef.append("  \"").append(col.getName()).append("\" ");

            // PostgreSQL自增使用SERIAL/BIGSERIAL类型
            if (col.getAutoIncrement() != null && col.getAutoIncrement()) {
                String typeLower = col.getType().toLowerCase();
                if (typeLower.equals("bigint") || typeLower.equals("int8")) {
                    colDef.append("BIGSERIAL");
                } else {
                    colDef.append("SERIAL");
                }
            } else {
                colDef.append(col.getType());
                // 添加长度
                if (col.getLength() != null && col.getLength() > 0) {
                    String typeLower = col.getType().toLowerCase();
                    if (typeLower.equals("varchar") || typeLower.equals("char") ||
                        typeLower.equals("decimal") || typeLower.equals("numeric")) {
                        colDef.append("(").append(col.getLength()).append(")");
                    }
                }
            }

            // 是否可为空
            if (col.getNullable() == null || !col.getNullable()) {
                colDef.append(" NOT NULL");
            }

            // 默认值（自增字段不需要默认值）
            if (StringUtils.isNotBlank(col.getDefaultValue()) && 
                (col.getAutoIncrement() == null || !col.getAutoIncrement())) {
                colDef.append(" DEFAULT ").append(col.getDefaultValue());
            }

            columnDefs.add(colDef.toString());

            // 收集主键
            if (col.getPrimaryKey() != null && col.getPrimaryKey()) {
                primaryKeys.add("\"" + col.getName() + "\"");
            }
        }

        sql.append(String.join(",\n", columnDefs));

        // 添加主键约束
        if (!primaryKeys.isEmpty()) {
            sql.append(",\n  PRIMARY KEY (").append(String.join(", ", primaryKeys)).append(")");
        }

        sql.append("\n);");

        // 添加表注释
        if (StringUtils.isNotBlank(dto.getTableComment())) {
            sql.append("\nCOMMENT ON TABLE ").append(tableName)
               .append(" IS '").append(dto.getTableComment()).append("';");
        }

        // 添加字段注释
        for (CreateTableDto.ColumnDto col : dto.getColumns()) {
            if (StringUtils.isNotBlank(col.getComment())) {
                sql.append("\nCOMMENT ON COLUMN ").append(tableName).append(".\"").append(col.getName())
                   .append("\" IS '").append(col.getComment()).append("';");
            }
        }

        return sql.toString();
    }

    /**
     * 生成MySQL建表SQL
     */
    private String generateMysqlCreateTableSql(CreateTableDto dto) {
        StringBuilder sql = new StringBuilder();
        String tableName = "`" + dto.getTableName() + "`";

        sql.append("CREATE TABLE ").append(tableName).append(" (\n");

        // 字段定义
        List<String> columnDefs = Lists.newArrayList();
        List<String> primaryKeys = Lists.newArrayList();

        for (CreateTableDto.ColumnDto col : dto.getColumns()) {
            StringBuilder colDef = new StringBuilder();
            colDef.append("  `").append(col.getName()).append("` ").append(col.getType());

            // 添加长度
            if (col.getLength() != null && col.getLength() > 0) {
                String typeLower = col.getType().toLowerCase();
                if (typeLower.equals("varchar") || typeLower.equals("char") ||
                    typeLower.equals("decimal") || typeLower.equals("numeric") ||
                    typeLower.equals("int") || typeLower.equals("bigint") ||
                    typeLower.equals("smallint") || typeLower.equals("tinyint")) {
                    colDef.append("(").append(col.getLength()).append(")");
                }
            }

            // 是否可为空
            if (col.getNullable() == null || !col.getNullable()) {
                colDef.append(" NOT NULL");
            }

            // MySQL自增使用AUTO_INCREMENT
            if (col.getAutoIncrement() != null && col.getAutoIncrement()) {
                colDef.append(" AUTO_INCREMENT");
            }

            // 默认值（自增字段不需要默认值）
            if (StringUtils.isNotBlank(col.getDefaultValue()) && 
                (col.getAutoIncrement() == null || !col.getAutoIncrement())) {
                colDef.append(" DEFAULT ").append(col.getDefaultValue());
            }

            // 字段注释
            if (StringUtils.isNotBlank(col.getComment())) {
                colDef.append(" COMMENT '").append(col.getComment()).append("'");
            }

            columnDefs.add(colDef.toString());

            // 收集主键
            if (col.getPrimaryKey() != null && col.getPrimaryKey()) {
                primaryKeys.add("`" + col.getName() + "`");
            }
        }

        sql.append(String.join(",\n", columnDefs));

        // 添加主键约束
        if (!primaryKeys.isEmpty()) {
            sql.append(",\n  PRIMARY KEY (").append(String.join(", ", primaryKeys)).append(")");
        }

        sql.append("\n)");

        // 添加引擎和字符集设置
        sql.append(" ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

        // 添加表注释
        if (StringUtils.isNotBlank(dto.getTableComment())) {
            sql.append(" COMMENT='").append(dto.getTableComment()).append("'");
        }

        sql.append(";");

        return sql.toString();
    }

    @Override
    public Boolean dropTable(Long datasourceId, String tableName, String tableSchema) throws SQLException {
        //获取数据源对象
        JobDatasource datasource = jobDatasourceService.getById(datasourceId);
        if (ObjectUtil.isNull(datasource)) {
            throw new RuntimeException("数据源不存在");
        }

        //生成删表SQL
        String dropTableSql = generateDropTableSql(datasource.getDatasource(), tableName, tableSchema);

        //执行删表SQL
        BaseQueryTool queryTool = QueryToolFactory.getByDbType(datasource);
        queryTool.executeCreateTableSql(dropTableSql);

        return true;
    }

    /**
     * 生成删表SQL
     */
    private String generateDropTableSql(String dbType, String tableName, String tableSchema) {
        if (JdbcConstants.POSTGRESQL.equals(dbType)) {
            // PostgreSQL: 处理schema.table格式
            String fullTableName;
            if (StringUtils.isNotBlank(tableSchema)) {
                // 如果tableName已包含schema前缀，直接使用
                if (tableName.contains(".")) {
                    fullTableName = "\"" + tableName.replace(".", "\".\"") + "\"";
                } else {
                    fullTableName = "\"" + tableSchema + "\".\"" + tableName + "\"";
                }
            } else {
                fullTableName = "\"" + tableName + "\"";
            }
            return "DROP TABLE IF EXISTS " + fullTableName + ";";
        } else if (JdbcConstants.MYSQL.equals(dbType)) {
            return "DROP TABLE IF EXISTS `" + tableName + "`;";
        } else {
            throw new RuntimeException("暂不支持该数据库类型: " + dbType);
        }
    }

    @Override
    public Boolean alterTable(AlterTableDto dto) throws SQLException {
        //获取数据源对象
        JobDatasource datasource = jobDatasourceService.getById(dto.getDatasourceId());
        if (ObjectUtil.isNull(datasource)) {
            throw new RuntimeException("数据源不存在");
        }

        String dbType = datasource.getDatasource();
        String alterType = dto.getAlterType();

        //生成修改表SQL
        String alterSql;
        switch (alterType) {
            case "ADD_COLUMNS":
                alterSql = generateAddColumnsSql(dbType, dto);
                break;
            case "DROP_COLUMN":
                alterSql = generateDropColumnSql(dbType, dto);
                break;
            case "MODIFY_COLUMN":
                alterSql = generateModifyColumnSql(dbType, dto);
                break;
            case "MODIFY_COMMENT":
                alterSql = generateModifyTableCommentSql(dbType, dto);
                break;
            default:
                throw new RuntimeException("不支持的修改类型: " + alterType);
        }

        //执行修改表SQL
        BaseQueryTool queryTool = QueryToolFactory.getByDbType(datasource);
        queryTool.executeCreateTableSql(alterSql);

        return true;
    }

    /**
     * 生成添加字段SQL
     */
    private String generateAddColumnsSql(String dbType, AlterTableDto dto) {
        StringBuilder sql = new StringBuilder();
        String tableName = getFullTableName(dbType, dto.getTableName(), dto.getTableSchema());

        for (AlterTableDto.ColumnDto col : dto.getColumns()) {
            if (JdbcConstants.POSTGRESQL.equals(dbType)) {
                sql.append("ALTER TABLE ").append(tableName).append(" ADD COLUMN \"")
                   .append(col.getName()).append("\" ").append(col.getType());

                if (col.getLength() != null && col.getLength() > 0) {
                    String typeLower = col.getType().toLowerCase();
                    if (typeLower.equals("varchar") || typeLower.equals("char") ||
                        typeLower.equals("decimal") || typeLower.equals("numeric")) {
                        sql.append("(").append(col.getLength()).append(")");
                    }
                }

                if (col.getNullable() == null || !col.getNullable()) {
                    sql.append(" NOT NULL");
                }

                if (StringUtils.isNotBlank(col.getDefaultValue())) {
                    sql.append(" DEFAULT ").append(col.getDefaultValue());
                }

                sql.append(";\n");

                // PostgreSQL字段注释
                if (StringUtils.isNotBlank(col.getComment())) {
                    sql.append("COMMENT ON COLUMN ").append(tableName).append(".\"")
                       .append(col.getName()).append("\" IS '").append(col.getComment()).append("';\n");
                }
            } else if (JdbcConstants.MYSQL.equals(dbType)) {
                sql.append("ALTER TABLE ").append(tableName).append(" ADD COLUMN `")
                   .append(col.getName()).append("` ").append(col.getType());

                if (col.getLength() != null && col.getLength() > 0) {
                    String typeLower = col.getType().toLowerCase();
                    if (typeLower.equals("varchar") || typeLower.equals("char") ||
                        typeLower.equals("decimal") || typeLower.equals("numeric") ||
                        typeLower.equals("int") || typeLower.equals("bigint") ||
                        typeLower.equals("smallint") || typeLower.equals("tinyint")) {
                        sql.append("(").append(col.getLength()).append(")");
                    }
                }

                if (col.getNullable() == null || !col.getNullable()) {
                    sql.append(" NOT NULL");
                }

                if (StringUtils.isNotBlank(col.getDefaultValue())) {
                    sql.append(" DEFAULT ").append(col.getDefaultValue());
                }

                if (StringUtils.isNotBlank(col.getComment())) {
                    sql.append(" COMMENT '").append(col.getComment()).append("'");
                }

                sql.append(";\n");
            }
        }

        return sql.toString();
    }

    /**
     * 生成删除字段SQL
     */
    private String generateDropColumnSql(String dbType, AlterTableDto dto) {
        String tableName = getFullTableName(dbType, dto.getTableName(), dto.getTableSchema());
        String quote = JdbcConstants.POSTGRESQL.equals(dbType) ? "\"" : "`";
        return "ALTER TABLE " + tableName + " DROP COLUMN " + quote + dto.getColumnName() + quote + ";";
    }

    /**
     * 生成修改字段SQL
     */
    private String generateModifyColumnSql(String dbType, AlterTableDto dto) {
        StringBuilder sql = new StringBuilder();
        String tableName = getFullTableName(dbType, dto.getTableName(), dto.getTableSchema());
        String oldColumnName = dto.getColumnName();
        String newColumnName = StringUtils.isNotBlank(dto.getNewName()) ? dto.getNewName() : oldColumnName;
        boolean isRename = !oldColumnName.equals(newColumnName);

        if (JdbcConstants.POSTGRESQL.equals(dbType)) {
            // PostgreSQL重命名字段
            if (isRename) {
                sql.append("ALTER TABLE ").append(tableName).append(" RENAME COLUMN \"")
                   .append(oldColumnName).append("\" TO \"").append(newColumnName).append("\";\n");
            }
            // PostgreSQL修改字段类型
            if (StringUtils.isNotBlank(dto.getNewType())) {
                String typeWithLength = dto.getNewType();
                if (dto.getNewLength() != null && dto.getNewLength() > 0) {
                    String typeLower = dto.getNewType().toLowerCase();
                    if (typeLower.equals("varchar") || typeLower.equals("char") ||
                        typeLower.equals("decimal") || typeLower.equals("numeric")) {
                        typeWithLength = dto.getNewType() + "(" + dto.getNewLength() + ")";
                    }
                }
                sql.append("ALTER TABLE ").append(tableName).append(" ALTER COLUMN \"")
                   .append(newColumnName).append("\" TYPE ").append(typeWithLength).append(";\n");
            }
            // PostgreSQL修改字段注释
            if (StringUtils.isNotBlank(dto.getNewComment())) {
                sql.append("COMMENT ON COLUMN ").append(tableName).append(".\"")
                   .append(newColumnName).append("\" IS '").append(dto.getNewComment()).append("';\n");
            }
        } else if (JdbcConstants.MYSQL.equals(dbType)) {
            // MySQL使用CHANGE COLUMN（支持重命名）
            sql.append("ALTER TABLE ").append(tableName).append(" CHANGE COLUMN `")
               .append(oldColumnName).append("` `").append(newColumnName).append("` ").append(dto.getNewType());

            if (dto.getNewLength() != null && dto.getNewLength() > 0) {
                String typeLower = dto.getNewType().toLowerCase();
                if (typeLower.equals("varchar") || typeLower.equals("char") ||
                    typeLower.equals("decimal") || typeLower.equals("numeric") ||
                    typeLower.equals("int") || typeLower.equals("bigint") ||
                    typeLower.equals("smallint") || typeLower.equals("tinyint")) {
                    sql.append("(").append(dto.getNewLength()).append(")");
                }
            }

            if (StringUtils.isNotBlank(dto.getNewComment())) {
                sql.append(" COMMENT '").append(dto.getNewComment()).append("'");
            }

            sql.append(";");
        }

        return sql.toString();
    }

    /**
     * 生成修改表注释SQL
     */
    private String generateModifyTableCommentSql(String dbType, AlterTableDto dto) {
        String tableName = getFullTableName(dbType, dto.getTableName(), dto.getTableSchema());

        if (JdbcConstants.POSTGRESQL.equals(dbType)) {
            return "COMMENT ON TABLE " + tableName + " IS '" + dto.getTableComment() + "';";
        } else if (JdbcConstants.MYSQL.equals(dbType)) {
            return "ALTER TABLE " + tableName + " COMMENT='" + dto.getTableComment() + "';";
        } else {
            throw new RuntimeException("暂不支持该数据库类型: " + dbType);
        }
    }

    /**
     * 获取完整表名
     */
    private String getFullTableName(String dbType, String tableName, String tableSchema) {
        if (JdbcConstants.POSTGRESQL.equals(dbType)) {
            String schema = tableSchema;
            String realTableName = tableName;

            // 如果tableName本身已经是 schema.table 的形式，则优先从中拆分，避免出现 public.public.xxx
            if (tableName != null && tableName.contains(".")) {
                String[] parts = tableName.split("\\.", 2);
                if (parts.length == 2) {
                    schema = parts[0];
                    realTableName = parts[1];
                }
            }

            if (StringUtils.isNotBlank(schema)) {
                return "\"" + schema + "\".\"" + realTableName + "\"";
            } else {
                return "\"" + realTableName + "\"";
            }
        } else {
            return "`" + tableName + "`";
        }
    }
}

package com.wugui.datax.admin.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.R;
import com.wugui.datax.admin.entity.JobApi;
import com.wugui.datax.admin.entity.JobDatasource;
import com.wugui.datax.admin.service.JobApiService;
import com.wugui.datax.admin.service.JobDatasourceService;
import com.wugui.datax.admin.util.AESUtil;
import com.wugui.datax.admin.util.JdbcConstants;
import com.wugui.datax.admin.util.JdbcUtils;
import com.zaxxer.hikari.HikariDataSource;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;

/**
 * 动态 API 接口控制器
 * 用于处理以 /api/ds/v1/ 开头的动态 API 请求
 */
@Slf4j
@RestController
@Api(tags = "动态API接口")
public class DynamicApiController extends BaseController {

    @Autowired
    private JobApiService jobApiService;

    @Autowired
    private JobDatasourceService jobDatasourceService;

    /**
     * 通用接口，接收所有以 /api/ds/v1/ 开头的请求
     * 支持 GET、POST、PUT、DELETE 方法
     */
    @RequestMapping(value = "/api/ds/v1/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    @ApiOperation("动态API接口")
    public R<Map<String, Object>> handleDynamicApi(
            HttpServletRequest request,
            @RequestBody(required = false) Map<String, Object> requestBody,
            @RequestParam(required = false) Map<String, String> queryParams) {
        
        try {
            // 获取请求路径和方法
            String requestPath = request.getRequestURI();
            String method = request.getMethod();

            log.info("=== 动态API请求开始 ===");
            log.info("请求方法: {}", method);
            log.info("请求路径: {}", requestPath);
            log.info("请求URI: {}", request.getRequestURI());
            log.info("请求URL: {}", request.getRequestURL());
            log.info("上下文路径: {}", request.getContextPath());
            log.info("Servlet路径: {}", request.getServletPath());
            log.info("路径信息: {}", request.getPathInfo());
            
            // 查询对应的 JobApi 配置
            QueryWrapper<JobApi> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("path", requestPath);
            queryWrapper.eq("method", method);
            queryWrapper.eq("status", 2); // 只查询已发布的API
            
            JobApi jobApi = jobApiService.getOne(queryWrapper);
            
            if (jobApi == null) {
                log.warn("未找到对应的API配置: {} {}", method, requestPath);
                return R.failed("API不存在或未发布");
            }
            
            // 解析 config JSON
            String configStr = jobApi.getConfig();
            if (configStr == null || configStr.trim().isEmpty()) {
                log.warn("API配置为空: {}", jobApi.getId());
                return R.failed("API配置为空");
            }
            
            JSONObject config = JSON.parseObject(configStr);
            
            // 解析请求参数
            List<Map<String, Object>> requestParams = null;
            if (config.containsKey("requestParam")) {
                requestParams = (List<Map<String, Object>>) config.get("requestParam");
                log.info("解析到请求参数: {}", requestParams);
            }
            
            // 解析响应参数
            List<Map<String, Object>> responseParams = null;
            if (config.containsKey("responseParam")) {
                responseParams = (List<Map<String, Object>>) config.get("responseParam");
                log.info("解析到响应参数: {}", responseParams);
            }
            
            // 解析基本信息
            String targetType = config.getString("targetType");
            String targetName = config.getString("targetName");
            String tableName = config.getString("tableName");
            String tableSchema = config.getString("tableSchema");
            
            // 如果没有 tableName，使用 targetName
            if (StringUtils.isBlank(tableName)) {
                tableName = targetName;
            }
            
            log.info("API配置信息 - 目标类型: {}, 目标名称: {}, 表名: {}, Schema: {}", targetType, targetName, tableName, tableSchema);
            
            // 获取数据源
            Long datasourceId = jobApi.getDatasourceId();
            if (datasourceId == null) {
                log.warn("API未配置数据源: {}", jobApi.getId());
                return R.failed("API未配置数据源");
            }
            
            JobDatasource jobDatasource = jobDatasourceService.getById(datasourceId);
            if (jobDatasource == null) {
                log.warn("数据源不存在: {}", datasourceId);
                return R.failed("数据源不存在");
            }
            
            // 构建 SQL 查询
            String sql = buildSelectSql(requestParams, responseParams, tableName, tableSchema, 
                    jobDatasource.getDatasource(), requestBody, queryParams);
            
            log.info("构建的SQL: {}", sql);
            
            // 获取数据源并执行查询
            DataSource dataSource = getDataSource(jobDatasource);
            List<Object> sqlParams = new ArrayList<>();
            String finalSql = buildSqlWithParams(sql, requestParams, requestBody, queryParams, sqlParams, 
                    jobDatasource.getDatasource());
            
            log.info("最终SQL: {}, 参数: {}", finalSql, sqlParams);
            
            List<Map<String, Object>> queryResult = JdbcUtils.executeQuery(dataSource, finalSql, sqlParams);
            
            // 构建响应数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("message", "查询成功");
            responseData.put("data", queryResult);
            responseData.put("total", queryResult.size());
            
            return R.ok(responseData);
            
        } catch (Exception e) {
            log.error("处理动态API请求失败", e);
            return R.failed("处理请求失败: " + e.getMessage());
        }
    }

    /**
     * 构建 SELECT SQL 语句
     */
    private String buildSelectSql(List<Map<String, Object>> requestParams,
                                   List<Map<String, Object>> responseParams,
                                   String tableName,
                                   String tableSchema,
                                   String datasourceType,
                                   Map<String, Object> requestBody,
                                   Map<String, String> queryParams) {
        StringBuilder sql = new StringBuilder("SELECT ");
        
        // 构建 SELECT 字段列表
        if (responseParams != null && !responseParams.isEmpty()) {
            List<String> selectFields = new ArrayList<>();
            for (Map<String, Object> param : responseParams) {
                String field = (String) param.get("field");
                if (StringUtils.isNotBlank(field)) {
                    selectFields.add(escapeIdentifier(field, datasourceType));
                }
            }
            if (selectFields.isEmpty()) {
                sql.append("*");
            } else {
                sql.append(String.join(", ", selectFields));
            }
        } else {
            sql.append("*");
        }
        
        // 构建 FROM 子句
        sql.append(" FROM ");
        String fullTableName = buildFullTableName(tableName, tableSchema, datasourceType);
        sql.append(fullTableName);
        
        return sql.toString();
    }

    /**
     * 构建带 WHERE 条件的完整 SQL
     */
    private String buildSqlWithParams(String baseSql,
                                       List<Map<String, Object>> requestParams,
                                       Map<String, Object> requestBody,
                                       Map<String, String> queryParams,
                                       List<Object> sqlParams,
                                       String datasourceType) {
        if (requestParams == null || requestParams.isEmpty()) {
            return baseSql;
        }
        
        // 合并请求参数（优先使用 requestBody，其次使用 queryParams）
        Map<String, Object> paramValues = new HashMap<>();
        if (requestBody != null) {
            paramValues.putAll(requestBody);
        }
        if (queryParams != null) {
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                if (!paramValues.containsKey(entry.getKey())) {
                    paramValues.put(entry.getKey(), entry.getValue());
                }
            }
        }
        
        List<String> whereConditions = new ArrayList<>();
        
        for (Map<String, Object> param : requestParams) {
            String field = (String) param.get("field");
            String operator = (String) param.get("operator");
            String type = (String) param.get("type");
            
            if (StringUtils.isBlank(field)) {
                continue;
            }
            
            Object value = paramValues.get(field);
            if (value == null) {
                continue; // 如果请求中没有该参数，跳过
            }
            
            // 根据操作符构建 WHERE 条件
            String condition = buildWhereCondition(field, operator, type, value, sqlParams, datasourceType);
            if (StringUtils.isNotBlank(condition)) {
                whereConditions.add(condition);
            }
        }
        
        if (!whereConditions.isEmpty()) {
            return baseSql + " WHERE " + String.join(" AND ", whereConditions);
        }
        
        return baseSql;
    }

    /**
     * 构建 WHERE 条件
     */
    private String buildWhereCondition(String field, String operator, String type, Object value, 
                                       List<Object> sqlParams, String datasourceType) {
        if (StringUtils.isBlank(operator)) {
            operator = "equal";
        }
        
        String fieldName = escapeIdentifier(field, datasourceType);
        
        switch (operator.toLowerCase()) {
            case "equal":
            case "=":
                sqlParams.add(convertValue(value, type));
                return fieldName + " = ?";
                
            case "notequal":
            case "!=":
            case "<>":
                sqlParams.add(convertValue(value, type));
                return fieldName + " != ?";
                
            case "greater":
            case ">":
                sqlParams.add(convertValue(value, type));
                return fieldName + " > ?";
                
            case "greaterorequal":
            case ">=":
                sqlParams.add(convertValue(value, type));
                return fieldName + " >= ?";
                
            case "less":
            case "<":
                sqlParams.add(convertValue(value, type));
                return fieldName + " < ?";
                
            case "lessorequal":
            case "<=":
                sqlParams.add(convertValue(value, type));
                return fieldName + " <= ?";
                
            case "like":
                sqlParams.add("%" + value.toString() + "%");
                return fieldName + " LIKE ?";
                
            case "startswith":
                sqlParams.add(value.toString() + "%");
                return fieldName + " LIKE ?";
                
            case "endswith":
                sqlParams.add("%" + value.toString());
                return fieldName + " LIKE ?";
                
            case "in":
                // IN 操作符处理
                List<Object> inValues = parseInValue(value);
                if (inValues.isEmpty()) {
                    return null;
                }
                String placeholders = String.join(", ", Collections.nCopies(inValues.size(), "?"));
                sqlParams.addAll(inValues);
                return fieldName + " IN (" + placeholders + ")";
                
            case "notin":
                List<Object> notInValues = parseInValue(value);
                if (notInValues.isEmpty()) {
                    return null;
                }
                String notInPlaceholders = String.join(", ", Collections.nCopies(notInValues.size(), "?"));
                sqlParams.addAll(notInValues);
                return fieldName + " NOT IN (" + notInPlaceholders + ")";
                
            case "isnull":
                return fieldName + " IS NULL";
                
            case "isnotnull":
                return fieldName + " IS NOT NULL";
                
            default:
                // 默认使用等于
                sqlParams.add(convertValue(value, type));
                return fieldName + " = ?";
        }
    }

    /**
     * 解析 IN 操作符的值（支持数组和逗号分隔的字符串）
     */
    @SuppressWarnings("unchecked")
    private List<Object> parseInValue(Object value) {
        List<Object> result = new ArrayList<>();
        
        if (value == null) {
            return result;
        }
        
        if (value instanceof List) {
            result.addAll((List<Object>) value);
        } else if (value instanceof Object[]) {
            result.addAll(Arrays.asList((Object[]) value));
        } else if (value instanceof String) {
            String strValue = (String) value;
            // 尝试解析 JSON 数组
            if (strValue.trim().startsWith("[")) {
                try {
                    List<Object> jsonList = JSON.parseArray(strValue, Object.class);
                    result.addAll(jsonList);
                } catch (Exception e) {
                    // 如果不是 JSON，按逗号分隔
                    String[] parts = strValue.split(",");
                    for (String part : parts) {
                        result.add(part.trim());
                    }
                }
            } else {
                // 按逗号分隔
                String[] parts = strValue.split(",");
                for (String part : parts) {
                    result.add(part.trim());
                }
            }
        } else {
            result.add(value);
        }
        
        return result;
    }

    /**
     * 转换值类型
     */
    private Object convertValue(Object value, String type) {
        if (value == null) {
            return null;
        }
        
        if (StringUtils.isBlank(type)) {
            return value;
        }
        
        try {
            switch (type.toLowerCase()) {
                case "int":
                case "integer":
                    if (value instanceof Number) {
                        return ((Number) value).intValue();
                    }
                    return Integer.parseInt(value.toString());
                    
                case "long":
                    if (value instanceof Number) {
                        return ((Number) value).longValue();
                    }
                    return Long.parseLong(value.toString());
                    
                case "double":
                case "float":
                    if (value instanceof Number) {
                        return ((Number) value).doubleValue();
                    }
                    return Double.parseDouble(value.toString());
                    
                case "bool":
                case "boolean":
                    if (value instanceof Boolean) {
                        return value;
                    }
                    return Boolean.parseBoolean(value.toString());
                    
                case "date":
                case "datetime":
                case "timestamp":
                    // 日期类型保持字符串格式，由数据库处理
                    return value.toString();
                    
                default:
                    return value.toString();
            }
        } catch (Exception e) {
            log.warn("转换值类型失败: {} -> {}, 使用原值", value, type, e);
            return value;
        }
    }

    /**
     * 构建完整的表名（包含 schema）
     */
    private String buildFullTableName(String tableName, String tableSchema, String datasourceType) {
        if (StringUtils.isBlank(tableName)) {
            throw new IllegalArgumentException("表名不能为空");
        }
        
        // PostgreSQL 需要 schema.table 格式
        if (JdbcConstants.POSTGRESQL.equalsIgnoreCase(datasourceType)) {
            String pureTableName = tableName;
            String resolvedSchemaName = tableSchema;
            
            // 如果 tableName 已经包含 schema（包含点号），先提取纯表名和 schema
            if (tableName.contains(".")) {
                String[] parts = tableName.split("\\.", 2);
                if (parts.length == 2) {
                    // 如果 tableName 已经包含 schema，优先使用 tableName 中的 schema
                    resolvedSchemaName = parts[0];
                    pureTableName = parts[1];
                }
            }
            
            // 构建完整的表名
            if (StringUtils.isNotBlank(resolvedSchemaName)) {
                return escapeIdentifier(resolvedSchemaName, datasourceType) + "." + escapeIdentifier(pureTableName, datasourceType);
            } else {
                // 如果没有 schema，直接使用表名
                return escapeIdentifier(pureTableName, datasourceType);
            }
        }
        
        // 其他数据库直接使用表名（如果包含 schema，也提取纯表名）
        if (tableName.contains(".")) {
            String[] parts = tableName.split("\\.", 2);
            return escapeIdentifier(parts[parts.length - 1], datasourceType);
        }
        return escapeIdentifier(tableName, datasourceType);
    }

    /**
     * 转义标识符（表名、字段名）
     */
    private String escapeIdentifier(String identifier, String datasourceType) {
        if (StringUtils.isBlank(identifier)) {
            return identifier;
        }
        
        // PostgreSQL 使用双引号
        if (JdbcConstants.POSTGRESQL.equalsIgnoreCase(datasourceType)) {
            return "\"" + identifier.replace("\"", "\"\"") + "\"";
        }
        
        // MySQL 使用反引号
        if (JdbcConstants.MYSQL.equalsIgnoreCase(datasourceType)) {
            return "`" + identifier.replace("`", "``") + "`";
        }
        
        // SQL Server 使用方括号
        if (JdbcConstants.SQL_SERVER.equalsIgnoreCase(datasourceType)) {
            return "[" + identifier.replace("]", "]]") + "]";
        }
        
        // 默认不转义
        return identifier;
    }

    /**
     * 获取数据源
     */
    private DataSource getDataSource(JobDatasource jobDatasource) throws SQLException {
        String userName = AESUtil.decrypt(jobDatasource.getJdbcUsername());
        
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setUsername(userName);
        dataSource.setPassword(AESUtil.decrypt(jobDatasource.getJdbcPassword()));
        
        String jdbcUrl = jobDatasource.getJdbcUrl();
        if (JdbcConstants.MYSQL.equals(jobDatasource.getDatasource())) {
            if (!jdbcUrl.contains("characterEncoding")) {
                jdbcUrl = jdbcUrl + (jdbcUrl.contains("?") ? "&" : "?") + "characterEncoding=UTF-8&useUnicode=true";
            }
        }
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setDriverClassName(jobDatasource.getJdbcDriverClass());
        dataSource.setMaximumPoolSize(5);
        dataSource.setMinimumIdle(1);
        dataSource.setConnectionTimeout(30000);
        dataSource.setIdleTimeout(600000);
        dataSource.setMaxLifetime(1800000);
        
        return dataSource;
    }
}


package com.wugui.datax.admin.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.R;
import com.wugui.datax.admin.entity.JobApi;
import com.wugui.datax.admin.service.JobApiService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            String tableSchema = config.getString("tableSchema");
            
            log.info("API配置信息 - 目标类型: {}, 目标名称: {}, Schema: {}", targetType, targetName, tableSchema);
            
            // 构建响应数据（暂时只返回成功）
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("message", "请求处理成功");
            responseData.put("requestParams", requestParams);
            responseData.put("responseParams", responseParams);
            responseData.put("targetType", targetType);
            responseData.put("targetName", targetName);
            responseData.put("tableSchema", tableSchema);
            
            return R.ok(responseData);
            
        } catch (Exception e) {
            log.error("处理动态API请求失败", e);
            return R.failed("处理请求失败: " + e.getMessage());
        }
    }
}


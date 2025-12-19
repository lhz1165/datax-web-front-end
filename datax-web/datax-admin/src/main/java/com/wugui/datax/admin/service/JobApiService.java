package com.wugui.datax.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wugui.datax.admin.entity.JobApi;

/**
 * API 发布配置 Service
 */
public interface JobApiService extends IService<JobApi> {

    /**
     * 分页查询 API 列表
     *
     * @param pageSize     每页条数
     * @param pageNo       页码（从 1 开始）
     * @param searchName   按名称模糊查询
     * @param datasourceId 按数据源过滤（可选）
     * @param method       按 HTTP 方法过滤（可选）
     * @return 分页结果
     */
    IPage<JobApi> pageList(Integer pageSize,
                           Integer pageNo,
                           String searchName,
                           Long datasourceId,
                           String method);
}



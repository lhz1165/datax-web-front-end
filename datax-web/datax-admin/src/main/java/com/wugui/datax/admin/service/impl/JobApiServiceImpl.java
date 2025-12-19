package com.wugui.datax.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wugui.datax.admin.entity.JobApi;
import com.wugui.datax.admin.entity.JobDatasource;
import com.wugui.datax.admin.mapper.JobApiMapper;
import com.wugui.datax.admin.mapper.JobDatasourceMapper;
import com.wugui.datax.admin.service.JobApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JobApiService 实现
 */
@Service
public class JobApiServiceImpl extends ServiceImpl<JobApiMapper, JobApi> implements JobApiService {

    @Autowired
    private JobDatasourceMapper jobDatasourceMapper;

    @Override
    public IPage<JobApi> pageList(Integer pageSize,
                                  Integer pageNo,
                                  String searchName,
                                  Long datasourceId,
                                  String method) {
        if (pageSize == null || pageSize <= 0) {
            pageSize = 10;
        }
        if (pageNo == null || pageNo <= 0) {
            pageNo = 1;
        }
        Page<JobApi> page = new Page<>(pageNo, pageSize);

        QueryWrapper<JobApi> query = new QueryWrapper<>();
        // 逻辑未删除
        //query.eq("status", 1);

        if (StringUtils.hasText(searchName)) {
            query.like("name", searchName);
        }
        if (datasourceId != null) {
            query.eq("datasource_id", datasourceId);
        }
        if (StringUtils.hasText(method)) {
            query.eq("method", method);
        }

        query.orderByDesc("id");
        IPage<JobApi> result = this.page(page, query);

        // 补充数据源名称和数据源类型
        List<JobApi> records = result.getRecords();
        if (records != null && !records.isEmpty()) {
            Set<Long> dsIds = records.stream()
                    .map(JobApi::getDatasourceId)
                    .filter(id -> id != null && id > 0)
                    .collect(Collectors.toSet());
            if (!dsIds.isEmpty()) {
                List<JobDatasource> dsList = jobDatasourceMapper.selectBatchIds(dsIds);
                Map<Long, String> dsNameMap = dsList.stream()
                        .collect(Collectors.toMap(JobDatasource::getId,
                                d -> StringUtils.hasText(d.getDatasourceName()) ? d.getDatasourceName() : d.getDatasource(),
                                (a, b) -> a));
                Map<Long, String> dsTypeMap = dsList.stream()
                        .collect(Collectors.toMap(JobDatasource::getId,
                                JobDatasource::getDatasource,
                                (a, b) -> a));
                records.forEach(item -> {
                    item.setDatasourceName(dsNameMap.get(item.getDatasourceId()));
                    item.setDataSource(dsTypeMap.get(item.getDatasourceId()));
                });
            }
        }

        return result;
    }
}



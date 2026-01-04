package com.wugui.datax.admin.service.impl;

import com.alibaba.fastjson.JSON;
import com.wugui.datax.admin.dto.DataXBatchJsonBuildDto;
import com.wugui.datax.admin.dto.DataXJsonBuildDto;
import com.wugui.datax.admin.entity.JobDatasource;
import com.wugui.datax.admin.entity.JobInfo;
import com.wugui.datax.admin.entity.JobTemplate;
import com.wugui.datax.admin.mapper.JobInfoMapper;
import com.wugui.datax.admin.mapper.JobTemplateMapper;
import com.wugui.datax.admin.service.DatasourceQueryService;
import com.wugui.datax.admin.service.DataxJsonService;
import com.wugui.datax.admin.service.JobDatasourceService;
import com.wugui.datax.admin.tool.datax.DataxJsonHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * com.wugui.datax json构建实现类
 *
 * @author jingwk
 * @ClassName DataxJsonServiceImpl
 * @Version 2.0
 * @since 2020/01/11 17:15
 */
@Service
public class DataxJsonServiceImpl implements DataxJsonService {

    @Autowired
    private JobDatasourceService jobJdbcDatasourceService;

    @Autowired
    private JobInfoMapper jobInfoMapper;

    @Override
    public String buildJobJson(DataXJsonBuildDto dataXJsonBuildDto) {
        DataxJsonHelper dataxJsonHelper = new DataxJsonHelper();
        
        // 如果是增量同步（type=1），查询 job_info 表获取最大 jobId，然后加1
        if (dataXJsonBuildDto.getType() != null && dataXJsonBuildDto.getType() == 1) {
            Long maxJobId = jobInfoMapper.findMaxId();
            // 设置 jobId 为最大 id + 1（如果 maxJobId 为 null，则默认为 1）
            dataxJsonHelper.setJobId(String.valueOf(maxJobId != null ? maxJobId + 1 : 1));
        }
        
        // reader
        JobDatasource readerDatasource = jobJdbcDatasourceService.getById(dataXJsonBuildDto.getReaderDatasourceId());
        // reader plugin init
        dataxJsonHelper.initReader(dataXJsonBuildDto, readerDatasource);
        JobDatasource writerDatasource = jobJdbcDatasourceService.getById(dataXJsonBuildDto.getWriterDatasourceId());
        dataxJsonHelper.initWriter(dataXJsonBuildDto, writerDatasource);

        return JSON.toJSONString(dataxJsonHelper.buildJob());
    }
}
package com.wugui.datax.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.api.R;
import com.wugui.datax.admin.entity.JobApi;
import com.wugui.datax.admin.service.JobApiService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.List;

/**
 * API 发布配置管理接口
 *
 * 注意：为避免与现有的 JobApiController（执行器回调接口）冲突，
 * 这里使用 JobApiConfigController 作为类名，路径为 /api/jobApi。
 */
@RestController
@RequestMapping("/api/jobApi")
@Api(tags = "API发布管理")
public class JobApiConfigController extends BaseController {

    @Autowired
    private JobApiService jobApiService;

    /**
     * 分页查询 API 列表
     */
    @GetMapping
    @ApiOperation("分页查询 API 列表")
    public R<IPage<JobApi>> pageList(@RequestParam(value = "searchVal", required = false) String searchVal,
                                     @RequestParam("pageSize") Integer pageSize,
                                     @RequestParam("pageNo") Integer pageNo,
                                     @RequestParam(value = "datasourceId", required = false) Long datasourceId,
                                     @RequestParam(value = "method", required = false) String method) {
        return success(jobApiService.pageList(pageSize, pageNo, searchVal, datasourceId, method));
    }

    /**
     * 获取全部 API 列表（不分页）
     */
    @GetMapping("/list")
    @ApiOperation("获取全部 API 列表")
    public R<List<JobApi>> listAll() {
        QueryWrapper<JobApi> query = new QueryWrapper<>();
        query.eq("status", 1);
        return success(jobApiService.list(query));
    }

    /**
     * 通过主键查询单条数据
     */
    @GetMapping("{id}")
    @ApiOperation("通过主键查询单条数据")
    public R<JobApi> getById(@PathVariable Serializable id) {
        return success(jobApiService.getById(id));
    }

    /**
     * 新增 API
     */
    @PostMapping
    @ApiOperation("新增 API")
    public R<Boolean> create(HttpServletRequest request, @RequestBody JobApi entity) {
        // 默认启用
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        return success(jobApiService.save(entity));
    }

    /**
     * 修改 API
     */
    @PutMapping
    @ApiOperation("修改 API")
    public R<Boolean> update(@RequestBody JobApi entity) {
        return success(jobApiService.updateById(entity));
    }

    /**
     * 删除（逻辑删除）API
     */
    @DeleteMapping
    @ApiOperation("删除 API")
    public R<Boolean> delete(@RequestParam("idList") List<Long> idList) {
        return success(jobApiService.removeByIds(idList));
    }

    /**
     * 修改API状态  0禁用 1待发布 2已发布
     * @param entity
     * @return
     */
    @PostMapping("/status")
    @ApiOperation("修改 API状态")
    public R<Boolean> updateStatus(@RequestBody JobApi entity) {
        if (entity.getId() == null) {
            return R.failed("API ID不能为空");
        }
        if (entity.getStatus() == null) {
            return R.failed("状态不能为空");
        }

        JobApi jobApi = new JobApi();
        jobApi.setId(entity.getId());
        jobApi.setStatus(entity.getStatus());
        if (jobApi == null) {
            return R.failed("API不存在");
        }
        jobApi.setStatus(entity.getStatus());
        return success(jobApiService.updateById(jobApi));
    }
}



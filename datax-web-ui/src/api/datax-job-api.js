import request from '@/utils/request'

// API 发布配置相关接口

// 分页查询
export function getPageList(params) {
  return request({
    url: '/api/jobApi',
    method: 'get',
    params
  })
}

// 获取全部列表（不分页）
export function getAllList(params) {
  return request({
    url: '/api/jobApi/list',
    method: 'get',
    params
  })
}

// 根据 ID 获取详情
export function getDetail(id) {
  return request({
    url: `/api/jobApi/${id}`,
    method: 'get'
  })
}

// 创建 API
export function createApi(data) {
  return request({
    url: '/api/jobApi',
    method: 'post',
    data
  })
}

// 更新 API
export function updateApi(data) {
  return request({
    url: '/api/jobApi',
    method: 'put',
    data
  })
}

// 删除 API（支持批量）
export function deleteApi(idList) {
  return request({
    url: '/api/jobApi',
    method: 'delete',
    params: { idList },
    // 自定义序列化，生成 ?idList=1&idList=2 这种格式，方便后端用 @RequestParam("idList") List<Long> 接收
    paramsSerializer: params => {
      const searchParams = new URLSearchParams()
      if (Array.isArray(params.idList)) {
        params.idList.forEach(v => searchParams.append('idList', v))
      } else if (params.idList !== undefined && params.idList !== null) {
        searchParams.append('idList', params.idList)
      }
      return searchParams.toString()
    }
  })
}

// 更新 API 状态
export function updateStatus(data) {
  return request({
    url: '/api/jobApi/status',
    method: 'post',
    data
  })
}


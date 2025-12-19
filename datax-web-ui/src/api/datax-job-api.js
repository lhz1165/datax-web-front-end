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
    params: { idList }
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


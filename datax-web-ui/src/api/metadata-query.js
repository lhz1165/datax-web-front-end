import request from '@/utils/request'

// 数据库信息api

// 获取表名
export function getTables(params) {
  return request({
    url: '/api/metadata/getTables',
    method: 'get',
    params
  })
}

// 获取schema
export function getTableSchema(params) {
  return request({
    url: '/api/metadata/getDBSchema',
    method: 'get',
    params
  })
}

// 获取字段
export function getColumns(params) {
  return request({
    url: '/api/metadata/getColumns',
    method: 'get',
    params
  })
}

// 获取字段详细信息（包含名称、类型、备注、是否可空、是否主键等）
export function getColumnsInfo(params) {
  return request({
    url: '/api/metadata/getColumnsInfo',
    method: 'get',
    params
  })
}

// 根据sql获取字段
export function getColumnsByQuerySql(params) {
  return request({
    url: '/api/metadata/getColumnsByQuerySql',
    method: 'get',
    params
  })
}

// 根据datasourceID、tablename创建表【目标端】
export function createTable(params) {
  return request({
    url: '/api/metadata/createTable',
    method: 'post',
    params
  })
}

// 创建数据库表（支持PostgreSQL和MySQL）
export function createTableWithColumns(data) {
  return request({
    url: '/api/metadata/createTable',
    method: 'post',
    data
  })
}

// 删除数据库表（支持PostgreSQL和MySQL）
export function dropTable(params) {
  return request({
    url: '/api/metadata/dropTable',
    method: 'delete',
    params
  })
}
// 判断字段是否存在，存在，即更新值，否则添加字段
export function updateColumnsValue(query) {
  return request({
    url: '/api/metadata/updateColumnsValue',
    method: 'post',
    data: query
  })
}

// 修改表结构（添加字段、删除字段、修改字段、修改表注释）
export function alterTable(data) {
  return request({
    url: '/api/metadata/alterTable',
    method: 'post',
    data
  })
}

<template>
  <div class="app-container">
    <el-row :gutter="12" class="full-height">
      <el-col :span="7" class="full-height">
        <el-card class="full-height" shadow="never">
          <div slot="header" class="card-header">
            <span>接口列表</span>
            <el-button size="mini" type="primary" @click="loadSpec">刷新</el-button>
          </div>

          <el-form :inline="true" class="filter-form">
            <el-form-item label="数据源">
              <el-select
                v-model="selectedDatasourceId"
                placeholder="请选择数据源"
                clearable
                filterable
                style="width: 220px;"
                @change="handleDatasourceChange"
              >
                <el-option
                  v-for="item in datasourceList"
                  :key="item.id"
                  :label="item.datasourceName"
                  :value="item.id"
                >
                  <span>{{ item.datasourceName }}</span>
                  <span style="color: #8492a6; font-size: 12px; margin-left: 8px;">{{ item.datasource }}</span>
                </el-option>
              </el-select>
            </el-form-item>
          </el-form>

          <el-input
            v-model="filterText"
            size="small"
            clearable
            placeholder="搜索分类，例如 todos"
            class="mb8"
          />

          <div class="group-caption">表名称</div>

          <el-scrollbar class="endpoints-scroll">
            <el-menu :default-active="activeKey" class="endpoints-menu" @select="handleSelect">
              <el-submenu
                v-for="group in filteredGroups"
                :key="group.name"
                :index="group.name"
              >
                <template slot="title">
                  <span class="group-title">{{ group.name }}</span>
                </template>

                <el-menu-item
                  v-for="item in group.items"
                  :key="item.key"
                  :index="item.key"
                >
                  <span class="method-tag" :class="'m-' + item.method">{{ item.method.toUpperCase() }}</span>
                  <span class="path-text">{{ item.path }}</span>
                </el-menu-item>
              </el-submenu>
            </el-menu>
          </el-scrollbar>
        </el-card>
      </el-col>

      <el-col :span="17" class="full-height">
        <el-card class="full-height" shadow="never">
          <div slot="header" class="card-header">
            <span>请求</span>
            <div class="header-right">
              <el-input v-model="requestBaseUrl" size="mini" placeholder="请求 Base URL，例如 http://localhost:3000" class="base-url" />
              <el-button size="mini" type="success" :loading="sending" :disabled="!selected || sending" @click="sendRequest">发送</el-button>
            </div>
          </div>

          <div v-if="loading" class="loading">加载中...</div>
          <div v-else-if="!spec" class="hint">未加载接口描述（PostgREST Swagger2 JSON）</div>

          <div v-else class="content">
            <div v-if="selected" class="selected-info">
              <div class="selected-line">
                <span class="method-tag" :class="'m-' + selected.method">{{ selected.method.toUpperCase() }}</span>
                <span class="path-text">{{ selected.path }}</span>
              </div>
              <div class="summary" v-if="selected.operation && selected.operation.summary">{{ selected.operation.summary }}</div>
            </div>

            <el-form v-if="selected" label-width="120px" size="small">
              <el-divider content-position="left">Query 参数</el-divider>
              <div v-if="queryParams.length === 0" class="hint">无</div>
              <el-form-item v-for="p in queryParams" :key="p.name" :label="p.name">
                <el-input v-model="paramValues.query[p.name]" :placeholder="p.description || ''" />
              </el-form-item>

              <el-divider content-position="left">Header 参数</el-divider>
              <div v-if="headerParams.length === 0" class="hint">无</div>
              <el-form-item v-for="p in headerParams" :key="p.name" :label="p.name">
                <el-input v-model="paramValues.header[p.name]" :placeholder="p.description || ''" />
              </el-form-item>

              <el-divider content-position="left">Body</el-divider>
              <div v-if="!bodyParam" class="hint">无</div>
              <el-form-item v-else :label="bodyParam.name">
                <el-input
                  v-model="paramValues.body"
                  type="textarea"
                  :rows="8"
                  placeholder='请输入 JSON，例如 {"id":1}'
                />
              </el-form-item>
            </el-form>

            <el-divider content-position="left">响应</el-divider>
            <div v-if="!lastResponse" class="hint">暂无请求</div>
            <div v-else class="response">
              <div class="resp-meta">
                <span>HTTP {{ lastResponse.status }}</span>
                <span class="ml12">{{ lastResponse.statusText }}</span>
              </div>
              <el-divider content-position="left">Headers</el-divider>
              <el-input v-model="lastResponseHeadersText" type="textarea" :rows="6" readonly />
              <el-divider content-position="left">Body</el-divider>
              <el-input v-model="lastResponseText" type="textarea" :rows="12" readonly />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import axios from 'axios'
import { list as getDatasourceList } from '@/api/datax-jdbcDatasource'

export default {
  name: 'DbQuery',
  data() {
    return {
      datasourceList: [],
      selectedDatasourceId: null,
      datasourceQuery: {
        current: 1,
        size: 200
      },
      loading: false,
      sending: false,
      spec: null,
      filterText: '',
      activeKey: '',
      selected: null,
      requestBaseUrl: process.env.VUE_APP_SWAGGER_URL || 'http://localhost:3000/',
      paramValues: {
        query: {},
        header: {},
        body: ''
      },
      lastResponse: null,
      lastResponseHeadersText: '',
      lastResponseText: ''
    }
  },
  mounted() {
    this.loadDatasourceList()
    this.loadSpec()
  },
  methods: {
    // 加载数据源列表（仅用于选择，不影响当前查询逻辑，待后续设计）
    loadDatasourceList() {
      getDatasourceList(this.datasourceQuery).then(response => {
        const { records } = response
        this.datasourceList = (records || []).filter(item => item.datasource === 'postgresql' || item.datasource === 'mysql')
      }).catch(() => {
        this.datasourceList = []
      })
    },

    handleDatasourceChange() {
      // 占位：后续在这里根据数据源切换 requestBaseUrl/spec 等
    },

    async loadSpec() {
      this.loading = true
      this.lastResponse = null
      this.lastResponseText = ''
      try {
        const baseUrl = process.env.VUE_APP_SWAGGER_URL || 'http://localhost:3000/'
        const { data: spec } = await axios.get(baseUrl, {
          headers: {
            Accept: 'application/openapi+json, application/json'
          }
        })

        this.spec = spec
        if (!this.requestBaseUrl) {
          this.requestBaseUrl = baseUrl
        }

        const first = this.endpoints[0]
        if (first) {
          this.activeKey = first.key
          this.selectEndpoint(first)
        }
      } finally {
        this.loading = false
      }
    },

    handleSelect(key) {
      this.activeKey = key
      const item = this.endpoints.find(e => e.key === key)
      if (item) this.selectEndpoint(item)
    },

    selectEndpoint(item) {
      this.selected = item
      this.paramValues = {
        query: {},
        header: {},
        body: ''
      }
    },

    resolveParam(p) {
      if (!p) return null
      if (p.$ref && this.spec && this.spec.parameters) {
        const key = String(p.$ref).replace('#/parameters/', '')
        return this.spec.parameters[key] || p
      }
      return p
    },

    async sendRequest() {
      if (!this.selected) return

      const base = (this.requestBaseUrl || '').replace(/\/+$/, '')
      const url = base + this.selected.path
      const method = this.selected.method

      const headers = {}
      for (const p of this.headerParams) {
        const val = this.paramValues.header[p.name]
        if (val !== undefined && val !== null && String(val).trim() !== '') {
          headers[p.name] = val
        }
      }

      const params = {}
      for (const p of this.queryParams) {
        const val = this.paramValues.query[p.name]
        if (val !== undefined && val !== null && String(val).trim() !== '') {
          params[p.name] = val
        }
      }

      let data
      if (this.bodyParam) {
        const raw = (this.paramValues.body || '').trim()
        if (raw) {
          data = JSON.parse(raw)
        }
      }

      this.lastResponse = null
      this.lastResponseHeadersText = ''
      this.lastResponseText = ''
      try {
        this.sending = true
        const resp = await axios({
          url,
          method,
          headers,
          params,
          data,
          validateStatus: () => true
        })

        this.lastResponse = {
          status: resp.status,
          statusText: resp.statusText
        }
        this.lastResponseHeadersText = JSON.stringify(resp.headers || {}, null, 2)
        const pretty = typeof resp.data === 'string' ? resp.data : JSON.stringify(resp.data, null, 2)
        this.lastResponseText = pretty
      } catch (e) {
        this.lastResponse = {
          status: 0,
          statusText: 'REQUEST_FAILED'
        }
        this.lastResponseHeadersText = ''
        this.lastResponseText = e && e.message ? e.message : String(e)
      } finally {
        this.sending = false
      }
    }
  },
  computed: {
    endpoints() {
      if (!this.spec || !this.spec.paths) return []
      const list = []
      const paths = this.spec.paths
      Object.keys(paths).forEach(path => {
        if (path === '/') return
        const ops = paths[path] || {}
        Object.keys(ops).forEach(method => {
          const m = method.toLowerCase()
          if (!['get', 'post', 'put', 'patch', 'delete', 'head', 'options'].includes(m)) return
          list.push({
            key: `${m} ${path}`,
            path,
            method: m,
            operation: ops[method]
          })
        })
      })
      return list
    },

    groups() {
      const map = new Map()
      const add = (name, item) => {
        if (!map.has(name)) map.set(name, [])
        map.get(name).push(item)
      }

      this.endpoints.forEach(item => {
        const tags = item.operation && item.operation.tags
        const tag = Array.isArray(tags) && tags.length ? tags[0] : ''
        let groupName = tag
        if (!groupName) {
          const firstSeg = String(item.path || '').split('/').filter(Boolean)[0]
          groupName = firstSeg || '默认'
        }
        add(groupName, item)
      })

      const result = Array.from(map.entries()).map(([name, items]) => {
        items.sort((a, b) => {
          const ap = `${a.path}`
          const bp = `${b.path}`
          if (ap !== bp) return ap.localeCompare(bp)
          return `${a.method}`.localeCompare(`${b.method}`)
        })
        return { name, items }
      })

      result.sort((a, b) => a.name.localeCompare(b.name))
      return result
    },

    filteredGroups() {
      const ft = (this.filterText || '').trim().toLowerCase()
      if (!ft) return this.groups
      return this.groups
        .filter(g => g.name.toLowerCase().includes(ft))
        .map(g => ({ name: g.name, items: g.items }))
    },

    currentParams() {
      if (!this.selected || !this.selected.operation) return []
      const params = (this.selected.operation.parameters || []).map(this.resolveParam).filter(Boolean)
      return params
    },

    queryParams() {
      return this.currentParams.filter(p => p.in === 'query')
    },

    headerParams() {
      return this.currentParams.filter(p => p.in === 'header')
    },

    bodyParam() {
      return this.currentParams.find(p => p.in === 'body') || null
    }
  }
}
</script>

<style scoped>
.app-container {
  height: calc(100vh - 84px);
  padding: 0;
}

.full-height {
  height: 100%;
}

::v-deep .full-height > .el-card {
  height: 100%;
}

::v-deep .full-height > .el-card {
  display: flex;
  flex-direction: column;
}

::v-deep .full-height > .el-card > .el-card__header {
  flex: 0 0 auto;
}

::v-deep .full-height > .el-card > .el-card__body {
  flex: 1 1 auto;
  overflow: auto;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.base-url {
  width: 320px;
}

.mb8 {
  margin-bottom: 8px;
}

.filter-form {
  margin-bottom: 8px;
}

.endpoints-scroll {
  height: auto;
}

.endpoints-menu {
  border-right: 0;
}

.group-caption {
  margin: 4px 0 8px;
  font-size: 12px;
  color: #909399;
}

.group-title {
  font-weight: 600;
}

.method-tag {
  display: inline-block;
  min-width: 56px;
  text-align: center;
  padding: 2px 6px;
  border-radius: 3px;
  margin-right: 8px;
  color: #fff;
  font-size: 12px;
}

.m-get { background: #67c23a; }
.m-post { background: #409eff; }
.m-put { background: #e6a23c; }
.m-patch { background: #e6a23c; }
.m-delete { background: #f56c6c; }

.path-text {
  font-family: monospace;
}

.loading {
  padding: 12px;
}

.hint {
  padding: 12px;
  color: #909399;
}

.content {
  height: auto;
  overflow: visible;
}

.selected-info {
  padding: 8px 0;
}

.selected-line {
  display: flex;
  align-items: center;
}

.summary {
  margin-top: 6px;
  color: #606266;
}

.response {
  padding-bottom: 12px;
}

.resp-meta {
  margin-bottom: 8px;
  color: #606266;
}

.ml12 {
  margin-left: 12px;
}
</style>

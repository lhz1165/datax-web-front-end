<template>
  <div class="api-edit-page">
    <div class="page-header">
      <div class="title-area">
        <span class="title">API 参数配置</span>
        <span v-if="apiId" class="sub">ID: {{ apiId }}</span>
      </div>
      <div class="actions">
        <el-button @click="handleBack">返回</el-button>
        <el-button @click="handleSave" type="primary">保存</el-button>
        <el-button v-if="currentStatus === 2" @click="handlePublish" type="warning">取消发布</el-button>
        <el-button v-else @click="handlePublish" type="success">发布</el-button>
      </div>
    </div>

    <el-card shadow="never" class="card-body">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="1. 数据源与对象" name="basic">
          <div class="form-row">
            <el-form :model="basicForm" label-width="100px" inline>
              <el-form-item label="数据源">
                <el-select
                  v-model="basicForm.datasourceId"
                  placeholder="选择数据源"
                  filterable
                  style="width: 260px"
                  @change="handleDatasourceChange"
                  :disabled="!!routeDatasourceId"
                >
                  <el-option
                    v-for="item in datasourceList"
                    :key="item.id"
                    :label="item.datasourceName || item.datasource"
                    :value="item.id"
                  />
                </el-select>
              </el-form-item>
              <el-form-item v-if="isPostgreSQL" label="Schema">
                <el-select
                  v-model="basicForm.tableSchema"
                  placeholder="请选择Schema"
                  filterable
                  style="width: 260px"
                  :loading="loadingSchemas"
                  @change="handleSchemaChange"
                >
                  <el-option
                    v-for="item in schemaList"
                    :key="item"
                    :label="item"
                    :value="item"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="对象类型">
                <el-radio-group v-model="basicForm.targetType" @change="handleTargetTypeChange">
                  <el-radio-button label="table">表</el-radio-button>
                  <el-radio-button label="view">视图</el-radio-button>
                </el-radio-group>
              </el-form-item>
              <el-form-item :label="basicForm.targetType === 'table' ? '选择表' : '选择视图'">
                <el-select
                  v-model="basicForm.targetName"
                  placeholder="请选择"
                  filterable
                  style="width: 260px"
                  :loading="loadingTargets"
                  @change="handleTargetChange"
                >
                  <el-option
                    v-for="item in targetList"
                    :key="item.name"
                    :label="item.name"
                    :value="item.name"
                  />
                </el-select>
              </el-form-item>
            </el-form>
          </div>
          <div class="table-wrapper">
            <div class="table-head">字段列表</div>
            <el-table :data="columnList" border size="small" height="360px" v-loading="loadingColumns">
              <el-table-column prop="name" label="字段" width="160" />
              <el-table-column prop="type" label="类型" width="140" />
              <el-table-column prop="comment" label="描述" />
              <el-table-column label="请求参数" width="120" align="center">
                <template slot-scope="scope">
                  <el-checkbox
                    :value="isRequestSelected(scope.row.name)"
                    @change="toggleRequestParam(scope.row, $event)"
                  />
                </template>
              </el-table-column>
              <el-table-column label="返回参数" width="120" align="center">
                <template slot-scope="scope">
                  <el-checkbox
                    :value="isResponseSelected(scope.row.name)"
                    @change="toggleResponseParam(scope.row, $event)"
                  />
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <el-tab-pane label="2. 参数选择" name="select">
          <div class="table-split">
            <div class="table-block">
              <div class="table-head">请求参数</div>
              <el-table :data="requestParams" border size="small" height="320px">
                <el-table-column prop="name" label="字段" width="160" />
                <el-table-column prop="type" label="类型" width="140" />
                <el-table-column prop="comment" label="描述" />
                <el-table-column label="操作" width="120">
                  <template slot-scope="scope">
                    <el-button type="text" size="mini" @click="removeRequestParam(scope.row.name)">移除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            <div class="table-block">
              <div class="table-head">返回参数</div>
              <el-table :data="responseParams" border size="small" height="320px">
                <el-table-column prop="name" label="字段" width="160" />
                <el-table-column prop="type" label="类型" width="140" />
                <el-table-column prop="comment" label="描述" />
                <el-table-column label="操作" width="120">
                  <template slot-scope="scope">
                    <el-button type="text" size="mini" @click="removeResponseParam(scope.row.name)">移除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="3. 配置请求参数" name="requestConfig">
          <el-table :data="requestParams" border size="small" height="380px">
            <el-table-column prop="name" label="参数名" width="160" />
            <el-table-column label="类型" width="140">
              <template slot-scope="scope">
                <el-select v-model="scope.row.paramType" size="mini" style="width: 120px">
                  <el-option v-for="opt in paramTypeOptions" :key="opt" :label="opt" :value="opt" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="操作符" width="140">
              <template slot-scope="scope">
                <el-select v-model="scope.row.operator" size="mini" style="width: 120px">
                  <el-option v-for="opt in operatorOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="描述">
              <template slot-scope="scope">
                <el-input v-model="scope.row.description" size="mini" placeholder="参数说明" />
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="4. 配置返回参数" name="responseConfig">
          <el-table :data="responseParams" border size="small" height="380px">
            <el-table-column prop="name" label="字段" width="160" />
            <el-table-column label="类型" width="140">
              <template slot-scope="scope">
                <el-select v-model="scope.row.paramType" size="mini" style="width: 120px">
                  <el-option v-for="opt in paramTypeOptions" :key="opt" :label="opt" :value="opt" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="描述">
              <template slot-scope="scope">
                <el-input v-model="scope.row.description" size="mini" placeholder="返回字段说明" />
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="5. 测试API" name="test">
          <div class="test-api-container">
            <div class="test-api-header">
              <div class="api-info">
                <el-select v-model="apiMethod" class="method-select" size="small">
                  <el-option label="GET" value="GET" />
                  <el-option label="POST" value="POST" />
                  <el-option label="PUT" value="PUT" />
                  <el-option label="DELETE" value="DELETE" />
                </el-select>
                <el-input
                  v-model="apiPath"
                  class="path-input"
                  placeholder="/api/ds/v1/..."
                  size="small"
                />
                <el-button type="primary" @click="handleTestApi" :loading="testing" size="small">发送</el-button>
              </div>
            </div>

            <div class="test-body-container">
              <div class="test-request-section">
                <div class="section-header">
                  <span class="section-title">请求体 (Body)</span>
                  <el-button
                    v-if="requestParams.length > 0"
                    type="text"
                    size="mini"
                    @click="generateSampleJson"
                  >
                    生成示例
                  </el-button>
                </div>
                <div class="json-editor-wrapper">
                  <el-input
                    v-model="requestBodyJson"
                    type="textarea"
                    :rows="15"
                    placeholder='请输入JSON格式的请求参数，例如：\n{\n  "param1": "value1",\n  "param2": 123\n}'
                    class="json-textarea"
                  />
                </div>
                <div v-if="requestParams.length > 0" class="params-hint">
                  <div class="hint-title">可用参数：</div>
                  <div class="params-list">
                    <el-tag
                      v-for="param in requestParams"
                      :key="param.name"
                      size="mini"
                      class="param-tag"
                    >
                      {{ param.name }} ({{ param.paramType }})
                    </el-tag>
                  </div>
                </div>
              </div>

              <div class="test-response-section">
                <div class="section-header">
                  <span class="section-title">响应</span>
                  <span v-if="responseTime !== null" class="response-time">耗时: {{ responseTime }}ms</span>
                </div>
                <div v-if="!testResponse" class="empty-response">
                  <i class="el-icon-info" />
                  <span>点击"发送"按钮测试API</span>
                </div>
                <div v-else class="json-editor-wrapper">
                  <el-input
                    :value="formatResponse(testResponse)"
                    type="textarea"
                    :rows="15"
                    readonly
                    class="json-textarea response-textarea"
                  />
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
import * as datasourceApi from '@/api/datax-jdbcDatasource'
import * as metadataApi from '@/api/metadata-query'
import * as jobApi from '@/api/datax-job-api'
import axios from 'axios'

export default {
  name: 'ApiPublishEdit',
  data() {
    return {
      apiId: this.$route.query.id,
      routeDatasourceId: this.$route.query.datasourceId,
      routeDataSource: this.$route.query.dataSource,
      activeTab: 'basic',
      datasourceList: [],
      basicForm: {
        datasourceId: undefined,
        targetType: 'table',
        targetName: '',
        tableSchema: ''
      },
      targetList: [],
      columnList: [],
      requestParams: [],
      responseParams: [],
      paramTypeOptions: ['string', 'int', 'long', 'double', 'bool', 'date'],
      operatorOptions: [
        { label: '=', value: 'equal' },
        { label: '!=', value: 'notEqual' },
        { label: '>', value: 'greaterThan' },
        { label: '<', value: 'lessThan' },
        { label: '>=', value: 'greaterThanOrEqual' },
        { label: '<=', value: 'lessThanOrEqual' },
        { label: 'LIKE', value: 'like' },
        { label: 'IN', value: 'in' }
      ],
      // 操作符显示值到实际值的映射
      operatorValueMap: {
        '=': 'equal',
        '!=': 'notEqual',
        '>': 'greaterThan',
        '<': 'lessThan',
        '>=': 'greaterThanOrEqual',
        '<=': 'lessThanOrEqual',
        'LIKE': 'like',
        'IN': 'in'
      },
      loadingTargets: false,
      loadingColumns: false,
      schemaList: [],
      loadingSchemas: false,
      currentStatus: 1, // 当前API状态：0删除，1待发布，2已发布
      apiPath: '', // API路径
      apiMethod: 'GET', // API方法
      requestBodyJson: '{}', // 请求体JSON字符串
      testResponse: null, // 测试响应
      responseTime: null, // 响应时间（毫秒）
      testing: false // 是否正在测试
    }
  },
  computed: {
    isPostgreSQL() {
      return this.routeDataSource === 'postgresql' || this.currentDataSource === 'postgresql'
    },
    currentDataSource() {
      if (this.routeDataSource) {
        return this.routeDataSource
      }
      // 如果没有从路由传递，尝试从数据源列表中查找
      const ds = this.datasourceList.find(item => item.id === this.basicForm.datasourceId)
      return ds ? ds.datasource : ''
    }
  },
  watch: {
    activeTab(newVal) {
      // 切换到测试标签页时，自动生成JSON结构
      if (newVal === 'test' && this.requestParams.length > 0) {
        this.generateEmptyJson()
      }
    },
    requestParams: {
      handler() {
        // 当请求参数变化时，如果当前在测试标签页，重新生成JSON
        if (this.activeTab === 'test' && this.requestParams.length > 0) {
          this.generateEmptyJson()
        }
      },
      deep: true
    }
  },
  created() {
    this.getDatasourceList()
  },
  methods: {
    handleBack() {
      this.$router.back()
    },
    handleSave() {
      // 组装请求参数和响应参数为 JSON 格式
      const requestParam = this.requestParams.map(param => {
        // 如果 operator 是显示值（如 '='），转换为对应的值（如 'equal'）
        let operatorValue = param.operator || 'equal'
        if (this.operatorValueMap[operatorValue]) {
          operatorValue = this.operatorValueMap[operatorValue]
        }
        return {
          field: param.name,
          type: param.paramType || param.type,
          desc: param.description || param.comment || '',
          operator: operatorValue
        }
      })

      const responseParam = this.responseParams.map(param => ({
        field: param.name,
        type: param.paramType || param.type,
        desc: param.description || param.comment || ''
      }))

      const config = {
        targetType: this.basicForm.targetType,
        targetName: this.basicForm.targetName,
        tableSchema: this.basicForm.tableSchema || '',
        requestParam,
        responseParam
      }

      // 构建更新数据
      const updateData = {
        id: this.apiId,
        datasourceId: this.basicForm.datasourceId,
        targetType: this.basicForm.targetType,
        targetName: this.basicForm.targetName,
        tableSchema: this.basicForm.tableSchema,
        config: JSON.stringify(config)
      }

      // 调用更新接口
      jobApi.updateApi(updateData).then(() => {
        this.$message.success('保存成功')
      }).catch(() => {
        this.$message.error('保存失败')
      })
    },
    handlePublish() {
      if (!this.apiId) {
        this.$message.warning('请先保存API配置')
        return
      }
      
      // 根据当前状态切换：1待发布 -> 2已发布，2已发布 -> 1待发布
      const newStatus = this.currentStatus === 2 ? 1 : 2
      const action = newStatus === 2 ? '发布' : '取消发布'
      
      jobApi.updateStatus({
        id: this.apiId,
        status: newStatus
      }).then(() => {
        this.currentStatus = newStatus
        this.$message.success(`${action}成功`)
      }).catch(() => {
        this.$message.error(`${action}失败`)
      })
    },
    getDatasourceList() {
      datasourceApi.getDataSourceList().then(res => {
        this.datasourceList = res || []
        this.initDatasourceFromRoute()
        // 如果有 API ID，加载详情并解析 config
        if (this.apiId) {
          this.loadApiDetail()
        }
      }).catch(() => {
        this.datasourceList = []
        this.initDatasourceFromRoute()
        // 如果有 API ID，加载详情并解析 config
        if (this.apiId) {
          this.loadApiDetail()
        }
      })
    },
    loadApiDetail() {
      jobApi.getDetail(this.apiId).then(res => {
        if (res) {
          // 保存当前状态
          this.currentStatus = res.status || 1

          // 保存API路径和方法
          this.apiPath = res.path || ''
          this.apiMethod = res.method || 'GET'

          // 填充基本信息
          if (res.datasourceId) {
            this.basicForm.datasourceId = res.datasourceId
          }

          // 解析 config JSON
          if (res.config) {
            try {
              const config = typeof res.config === 'string' ? JSON.parse(res.config) : res.config

              // 从 config 中解析基本信息
              if (config.targetType) {
                this.basicForm.targetType = config.targetType
              }
              if (config.targetName) {
                this.basicForm.targetName = config.targetName
              }
              if (config.tableSchema) {
                this.basicForm.tableSchema = config.tableSchema
              }

              // 解析请求参数
              if (config.requestParam && Array.isArray(config.requestParam)) {
                this.requestParams = config.requestParam.map(param => ({
                  name: param.field,
                  type: param.type,
                  paramType: param.type,
                  comment: param.desc || '',
                  description: param.desc || '',
                  operator: param.operator || 'equal'
                }))
              }

              // 解析响应参数
              if (config.responseParam && Array.isArray(config.responseParam)) {
                this.responseParams = config.responseParam.map(param => ({
                  name: param.field,
                  type: param.type,
                  paramType: param.type,
                  comment: param.desc || '',
                  description: param.desc || ''
                }))
              }

              // 如果已配置了表/视图，加载字段列表以便回显选中状态
              // 使用 $nextTick 确保在数据源列表加载完成后再加载
              this.$nextTick(() => {
                if (this.basicForm.datasourceId && this.basicForm.targetName) {
                  if (this.isPostgreSQL && this.basicForm.tableSchema) {
                    this.loadSchemaList().then(() => {
                      this.loadTargetList().then(() => {
                        this.loadColumnList()
                      })
                    })
                  } else {
                    this.loadTargetList().then(() => {
                      this.loadColumnList()
                    })
                  }
                }
              })
            } catch (e) {
              console.error('解析 config 失败:', e)
              this.$message.warning('配置解析失败，请重新配置')
            }
          }
        }
      }).catch(() => {
        this.$message.error('加载 API 详情失败')
      })
    },
    initDatasourceFromRoute() {
      if (this.routeDatasourceId) {
        this.basicForm.datasourceId = Number(this.routeDatasourceId)
        if (this.isPostgreSQL) {
          this.loadSchemaList()
        } else {
          this.loadTargetList()
        }
      } else if (!this.basicForm.datasourceId && this.datasourceList.length) {
        this.basicForm.datasourceId = this.datasourceList[0].id
        if (this.isPostgreSQL) {
          this.loadSchemaList()
        } else {
          this.loadTargetList()
        }
      }
    },
    loadSchemaList() {
      return new Promise((resolve) => {
        if (!this.basicForm.datasourceId) {
          this.schemaList = []
          resolve()
          return
        }
        this.loadingSchemas = true
        const params = {
          datasourceId: this.basicForm.datasourceId
        }
        metadataApi.getTableSchema(params).then(response => {
          this.schemaList = response || []
          this.loadingSchemas = false
          // 如果有数据且当前没有选中，自动选中第一个（通常是 public）
          if (this.schemaList.length > 0 && !this.basicForm.tableSchema) {
            this.basicForm.tableSchema = this.schemaList[0]
            this.loadTargetList().then(() => resolve())
          } else if (this.schemaList.length === 0) {
            // 如果没有 schema，直接加载表/视图
            this.loadTargetList().then(() => resolve())
          } else {
            resolve()
          }
        }).catch(() => {
          this.schemaList = []
          this.loadingSchemas = false
          // 出错时也尝试加载表/视图
          this.loadTargetList().then(() => resolve())
        })
      })
    },
    handleSchemaChange() {
      this.basicForm.targetName = ''
      this.columnList = []
      this.clearSelectedParams()
      this.loadTargetList()
    },
    loadTargetList() {
      return new Promise((resolve) => {
        if (!this.basicForm.datasourceId) {
          this.targetList = []
          resolve()
          return
        }
        // PostgreSQL 需要先选择 schema
        if (this.isPostgreSQL && !this.basicForm.tableSchema) {
          this.targetList = []
          resolve()
          return
        }
        this.loadingTargets = true
        const params = {
          datasourceId: this.basicForm.datasourceId
        }
        // PostgreSQL 需要传递 tableSchema
        if (this.isPostgreSQL && this.basicForm.tableSchema) {
          params.tableSchema = this.basicForm.tableSchema
        }
        const apiMethod = this.basicForm.targetType === 'table' ? metadataApi.getTables : metadataApi.getViews
        apiMethod(params).then(response => {
          // 接口返回字符串数组，转换为对象数组
          this.targetList = (response || []).map(name => ({ name }))
          this.loadingTargets = false
          // 如果有数据且当前没有选中，自动选中第一个
          if (this.targetList.length > 0 && !this.basicForm.targetName) {
            this.basicForm.targetName = this.targetList[0].name
            this.loadColumnList()
          }
          resolve()
        }).catch(() => {
          this.targetList = []
          this.loadingTargets = false
          resolve()
        })
      })
    },
    loadColumnList() {
      if (!this.basicForm.datasourceId || !this.basicForm.targetName) {
        this.columnList = []
        return
      }
      // PostgreSQL 需要先选择 schema
      if (this.isPostgreSQL && !this.basicForm.tableSchema) {
        this.columnList = []
        return
      }
      this.loadingColumns = true
      const params = {
        datasourceId: this.basicForm.datasourceId,
        tableName: this.basicForm.targetName
      }
      // PostgreSQL 需要传递 tableSchema
      if (this.isPostgreSQL && this.basicForm.tableSchema) {
        params.tableSchema = this.basicForm.tableSchema
      }
      metadataApi.getColumnsInfo(params).then(response => {
        // 接口返回 ColumnInfo 对象列表: { name, type, comment, ifPrimaryKey, isnull, length }
        this.columnList = (response || []).map(col => ({
          name: col.name,
          type: col.type || '-',
          comment: col.comment || ''
        }))
        this.loadingColumns = false
      }).catch(() => {
        this.columnList = []
        this.loadingColumns = false
      })
    },
    handleDatasourceChange() {
      this.basicForm.targetName = ''
      this.basicForm.tableSchema = ''
      this.columnList = []
      this.targetList = []
      this.schemaList = []
      this.clearSelectedParams()
      if (this.isPostgreSQL) {
        this.loadSchemaList()
      } else {
        this.loadTargetList()
      }
    },
    handleTargetTypeChange() {
      this.basicForm.targetName = ''
      this.columnList = []
      this.clearSelectedParams()
      this.loadTargetList()
    },
    handleTargetChange() {
      this.columnList = []
      this.clearSelectedParams()
      this.loadColumnList()
    },
    addRequestParam(row) {
      if (this.requestParams.find(item => item.name === row.name)) return
      this.requestParams.push({
        name: row.name,
        type: row.type,
        comment: row.comment,
        paramType: this.mapType(row.type),
        operator: 'equal',
        description: row.comment || ''
      })
    },
    addResponseParam(row) {
      if (this.responseParams.find(item => item.name === row.name)) return
      this.responseParams.push({
        name: row.name,
        type: row.type,
        comment: row.comment,
        paramType: this.mapType(row.type),
        description: row.comment || ''
      })
    },
    toggleRequestParam(row, checked) {
      if (checked) {
        this.addRequestParam(row)
      } else {
        this.removeRequestParam(row.name)
      }
    },
    toggleResponseParam(row, checked) {
      if (checked) {
        this.addResponseParam(row)
      } else {
        this.removeResponseParam(row.name)
      }
    },
    isRequestSelected(name) {
      return this.requestParams.some(item => item.name === name)
    },
    isResponseSelected(name) {
      return this.responseParams.some(item => item.name === name)
    },
    removeRequestParam(name) {
      this.requestParams = this.requestParams.filter(item => item.name !== name)
    },
    removeResponseParam(name) {
      this.responseParams = this.responseParams.filter(item => item.name !== name)
    },
    clearSelectedParams() {
      this.requestParams = []
      this.responseParams = []
    },
    mapType(dbType) {
      const t = (dbType || '').toLowerCase()
      if (t.includes('int')) return 'int'
      if (t.includes('bigint')) return 'long'
      if (t.includes('double') || t.includes('float') || t.includes('decimal')) return 'double'
      if (t.includes('date') || t.includes('time')) return 'date'
      return 'string'
    },
    handleTestApi() {
      if (!this.apiPath) {
        this.$message.warning('API路径未配置')
        return
      }

      // 解析JSON请求体
      let requestData = {}
      try {
        if (this.requestBodyJson && this.requestBodyJson.trim()) {
          requestData = JSON.parse(this.requestBodyJson)
        }
      } catch (e) {
        this.$message.error('请求体JSON格式错误：' + e.message)
        return
      }

      this.testing = true
      this.testResponse = null
      this.responseTime = null

      const startTime = Date.now()

      // 根据请求方法发送请求，端口固定为 8180
      const baseUrl = 'http://localhost:8180'
      const config = {
        url: baseUrl + this.apiPath,
        method: this.apiMethod.toLowerCase(),
        headers: {
          'Content-Type': 'application/json'
        }
      }

      // GET 请求使用 params，其他使用 data
      if (this.apiMethod.toUpperCase() === 'GET') {
        config.params = requestData
      } else {
        config.data = requestData
      }

      axios(config)
        .then(response => {
          const endTime = Date.now()
          this.responseTime = endTime - startTime
          this.testResponse = response.data
        })
        .catch(error => {
          const endTime = Date.now()
          this.responseTime = endTime - startTime
          if (error.response) {
            this.testResponse = {
              error: true,
              status: error.response.status,
              message: (error.response.data && error.response.data.message) || error.message,
              data: error.response.data
            }
          } else {
            this.testResponse = {
              error: true,
              message: error.message || '请求失败'
            }
          }
        })
        .finally(() => {
          this.testing = false
        })
    },
    generateEmptyJson() {
      // 根据请求参数生成JSON结构，值留空
      const json = {}
      this.requestParams.forEach(param => {
        // 根据类型设置空值
        switch (param.paramType) {
          case 'int':
          case 'long':
            json[param.name] = null
            break
          case 'double':
            json[param.name] = null
            break
          case 'bool':
            json[param.name] = null
            break
          case 'date':
            json[param.name] = ''
            break
          default:
            json[param.name] = ''
        }
      })
      this.requestBodyJson = JSON.stringify(json, null, 2)
    },
    generateSampleJson() {
      // 根据请求参数生成示例JSON（带默认值）
      const sample = {}
      this.requestParams.forEach(param => {
        switch (param.paramType) {
          case 'int':
            sample[param.name] = 0
            break
          case 'long':
            sample[param.name] = 0
            break
          case 'double':
            sample[param.name] = 0.0
            break
          case 'bool':
            sample[param.name] = false
            break
          case 'date':
            sample[param.name] = new Date().toISOString()
            break
          default:
            sample[param.name] = ''
        }
      })
      this.requestBodyJson = JSON.stringify(sample, null, 2)
    },
    formatResponse(response) {
      if (typeof response === 'string') {
        try {
          return JSON.stringify(JSON.parse(response), null, 2)
        } catch (e) {
          return response
        }
      }
      return JSON.stringify(response, null, 2)
    }
  }
}
</script>

<style scoped>
.api-edit-page {
  padding: 16px 20px 24px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.title-area {
  display: flex;
  align-items: center;
  gap: 10px;
}

.title {
  font-size: 18px;
  font-weight: 600;
}

.sub {
  color: #909399;
  font-size: 12px;
}

.actions > .el-button + .el-button {
  margin-left: 8px;
}

.card-body {
  padding: 10px 12px 4px;
}

.form-row {
  margin-bottom: 12px;
}

.table-wrapper,
.table-block {
  margin-top: 6px;
}

.table-head {
  font-weight: 600;
  margin-bottom: 6px;
}

.table-split {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

@media (max-width: 1100px) {
  .table-split {
    grid-template-columns: 1fr;
  }
}

/* 测试API样式 - Postman风格 */
.test-api-container {
  padding: 0;
  background: #fff;
}

.test-api-header {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: #f7f7f7;
  border-bottom: 1px solid #e4e7ed;
  margin-bottom: 0;
}

.api-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.method-select {
  width: 100px;
}

.method-select .el-input__inner {
  font-weight: 600;
  text-align: center;
}

.path-input {
  flex: 1;
}

.path-input .el-input__inner {
  font-family: 'Courier New', monospace;
  font-size: 13px;
}

.test-body-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0;
  height: calc(100vh - 300px);
  min-height: 600px;
}

.test-request-section,
.test-response-section {
  display: flex;
  flex-direction: column;
  border-right: 1px solid #e4e7ed;
  background: #fff;
}

.test-response-section {
  border-right: none;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #fafafa;
  border-bottom: 1px solid #e4e7ed;
}

.section-title {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.response-time {
  font-size: 12px;
  color: #909399;
  font-weight: normal;
}

.json-editor-wrapper {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.json-textarea {
  flex: 1;
  border: none;
  border-radius: 0;
}

.json-textarea .el-textarea__inner {
  border: none;
  border-radius: 0;
  font-family: 'Courier New', 'Consolas', monospace;
  font-size: 13px;
  line-height: 1.6;
  padding: 16px;
  resize: none;
  background: #fff;
  color: #303133;
}

.response-textarea .el-textarea__inner {
  background: #1e1e1e;
  color: #d4d4d4;
}

.empty-response {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 14px;
}

.empty-response i {
  font-size: 48px;
  margin-bottom: 12px;
  color: #c0c4cc;
}

.params-hint {
  padding: 12px 16px;
  background: #f5f7fa;
  border-top: 1px solid #e4e7ed;
}

.hint-title {
  font-size: 12px;
  color: #606266;
  margin-bottom: 8px;
  font-weight: 600;
}

.params-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.param-tag {
  font-family: 'Courier New', monospace;
  font-size: 11px;
}
</style>


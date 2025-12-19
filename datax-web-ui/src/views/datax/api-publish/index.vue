<template>
  <div class="app-container">
    <div class="filter-container">
      <el-select
        v-model="query.datasourceId"
        placeholder="数据源"
        class="filter-item"
        clearable
        style="width: 220px;"
      >
        <el-option
          v-for="item in datasourceList"
          :key="item.id"
          :label="item.datasourceName || item.datasource"
          :value="item.id"
        />
      </el-select>

      <el-select
        v-model="query.method"
        placeholder="请求方式"
        class="filter-item"
        clearable
        style="width: 200px;"
      >
        <el-option
          v-for="item in apiTypes"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>

      <el-button
        class="filter-item"
        type="primary"
        icon="el-icon-search"
        @click="fetchData"
      >
        搜索
      </el-button>

      <el-button
        class="filter-item"
        type="primary"
        icon="el-icon-plus"
        @click="handleCreate"
      >
        创建API
      </el-button>
    </div>

    <el-table
      v-loading="listLoading"
      :data="list"
      border
      fit
      highlight-current-row
      style="width: 100%"
      size="small"
    >
      <el-table-column label="名称" prop="name" min-width="140" align="center" />
      <el-table-column label="数据源" prop="datasourceName" min-width="160" align="center" :show-overflow-tooltip="true" />
      <el-table-column label="Path" prop="path" min-width="180" align="center" :show-overflow-tooltip="true" />
      <el-table-column label="请求方式" prop="method" width="90" align="center">
        <template slot-scope="scope">
          {{ formatMethod(scope.row.method) }}
        </template>
      </el-table-column>
      <el-table-column label="返回类型" prop="responseType" width="130" align="center">
        <template slot-scope="scope">
          {{ scope.row.responseType || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="状态" prop="status" width="100" align="center">
        <template slot-scope="scope">
          <el-tag :type="statusTagType(scope.row.status)" size="mini">
            {{ formatStatus(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createDate" width="160" align="center" />
      <el-table-column label="描述" prop="description" min-width="180" :show-overflow-tooltip="true" />
      <el-table-column label="操作" width="220" align="center">
        <template slot-scope="scope">
          <el-button type="text" size="mini" @click="handleView(scope.row)">查看</el-button>
          <el-button type="text" size="mini" @click="handleEdit(scope.row)">配置</el-button>
          <el-button 
            type="text" 
            size="mini" 
            :disabled="scope.row.status !== 0"
            @click="handleEnable(scope.row)"
          >
            启用
          </el-button>
          <el-button 
            type="text" 
            size="mini" 
            :disabled="scope.row.status === 0"
            @click="handleDisable(scope.row)"
          >
            禁用
          </el-button>
          <el-button type="text" size="mini" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination
        :current-page="query.pageNo"
        :page-size="query.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 创建/查看 API 弹窗 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="createDialogVisible"
      width="600px"
    >
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        label-width="90px"
      >
        <el-form-item label="数据源" prop="datasourceId">
          <el-select v-model="createForm.datasourceId" placeholder="请选择数据源" filterable>
            <el-option
              v-for="item in datasourceList"
              :key="item.id"
              :label="item.datasourceName || item.datasource"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="createForm.name" placeholder="请输入API名称" />
        </el-form-item>
        <el-form-item label="Path" prop="path">
          <el-input v-model="createForm.path" placeholder="例如：/demo/user/list">
            <template slot="prepend">/api/ds/v1</template>
          </el-input>
        </el-form-item>
        <el-form-item label="请求方式" prop="method">
          <el-select v-model="createForm.method" placeholder="请选择请求方式">
            <el-option
              v-for="item in apiTypes"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="返回类型" prop="responseType">
          <el-input v-model="createForm.responseType" placeholder="例如：application/json" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="createForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入API描述"
          />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="createDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleCreateConfirm">{{ isEditMode ? '更 新' : '确 定' }}</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import * as datasourceApi from '@/api/datax-jdbcDatasource'
import * as jobApi from '@/api/datax-job-api'

export default {
  name: 'ApiPublish',
  data() {
    return {
      list: [],
      listLoading: false,
      total: 0,
      query: {
        datasourceId: undefined,
        method: undefined,
        searchVal: undefined,
        pageNo: 1,
        pageSize: 20
      },
      datasourceList: [],
      apiTypes: [
        { value: 'GET', label: 'GET' },
        { value: 'POST', label: 'POST' },
        { value: 'PUT', label: 'PUT' },
        { value: 'DELETE', label: 'DELETE' }
      ],
      createDialogVisible: false,
      isEditMode: false,
      currentEditId: null,
      createForm: {
        datasourceId: undefined,
        name: '',
        path: '',
        method: '',
        responseType: 'application/json',
        description: '',
        status: 1
      },
      createRules: {
        datasourceId: [{ required: true, message: '请选择数据源', trigger: 'change' }],
        name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
        path: [{ required: true, message: '请输入Path', trigger: 'blur' }],
        method: [{ required: true, message: '请选择请求方式', trigger: 'change' }],
        responseType: [{ required: true, message: '请输入返回类型', trigger: 'blur' }]
      }
    }
  },
  computed: {
    dialogTitle() {
      return this.isEditMode ? '查看API' : '创建API'
    }
  },
  created() {
    this.getDataSourceList()
    this.fetchData()
  },
  methods: {
    getDataSourceList() {
      // 复用已有数据源接口
      datasourceApi.getDataSourceList().then(response => {
        this.datasourceList = response || []
      }).catch(() => {
        this.datasourceList = []
      })
    },
    fetchData() {
      this.listLoading = true
      const params = {
        pageNo: this.query.pageNo,
        pageSize: this.query.pageSize,
        searchVal: this.query.searchVal,
        datasourceId: this.query.datasourceId,
        method: this.query.method
      }
      jobApi.getPageList(params).then(res => {
        // 后端返回为 mybatis-plus IPage 结构时，通常为 { records, total, ... }
        const data = res && res.data ? res.data : res
        const records = data.records || []
        this.list = this.attachDatasourceName(records)
        this.total = data.total || 0
      }).catch(() => {
        this.list = []
      }).finally(() => {
        this.listLoading = false
      })
    },
    formatMethod(val) {
      const found = this.apiTypes.find(t => t.value === val)
      return found ? found.label : val || '-'
    },
    formatStatus(val) {
      if (val === 0 || val === '0') return '禁用'
      if (val === 1 || val === '1') return '待发布'
      if (val === 2 || val === '2') return '已发布'
      return '未知'
    },
    statusTagType(val) {
      if (val === 0 || val === '0') return 'info'
      if (val === 1 || val === '1') return 'warning'
      if (val === 2 || val === '2') return 'success'
      return 'info'
    },
    handleCreate() {
      this.isEditMode = false
      this.currentEditId = null
      this.createDialogVisible = true
      this.$nextTick(() => {
        if (this.$refs.createFormRef) {
          this.$refs.createFormRef.resetFields()
        }
        // 重置表单数据
        this.createForm = {
          datasourceId: undefined,
          name: '',
          path: '',
          method: '',
          responseType: 'application/json',
          description: '',
          status: 1
        }
      })
    },
    handleCreateConfirm() {
      this.$refs.createFormRef.validate(valid => {
        if (!valid) return
        // 拼接Path前缀
        let fullPath = this.createForm.path
        if (fullPath && !fullPath.startsWith('/api/ds/v1')) {
          // 确保路径以 / 开头
          if (!fullPath.startsWith('/')) {
            fullPath = '/' + fullPath
          }
          fullPath = '/api/ds/v1' + fullPath
        } else if (!fullPath) {
          fullPath = '/api/ds/v1'
        }
        
        const payload = {
          datasourceId: this.createForm.datasourceId,
          name: this.createForm.name,
          path: fullPath,
          method: this.createForm.method,
          responseType: this.createForm.responseType,
          description: this.createForm.description,
          status: this.createForm.status || 1
        }
        
        // 如果是编辑模式，调用更新接口
        if (this.isEditMode && this.currentEditId) {
          payload.id = this.currentEditId
          jobApi.updateApi(payload).then(() => {
            this.$message.success('更新API成功')
            this.createDialogVisible = false
            this.fetchData()
          }).catch(() => {
            this.$message.error('更新API失败')
          })
        } else {
          // 创建模式
          jobApi.createApi(payload).then(() => {
            this.$message.success('创建API成功')
            this.createDialogVisible = false
            this.fetchData()
          }).catch(() => {
            this.$message.error('创建API失败')
          })
        }
      })
    },
    handleView(row) {
      // 调用详情接口获取完整数据
      jobApi.getDetail(row.id).then(res => {
        if (res) {
          this.isEditMode = true
          this.currentEditId = row.id
          // 处理Path，去掉前缀显示
          let displayPath = res.path || ''
          if (displayPath.startsWith('/api/ds/v1')) {
            displayPath = displayPath.substring('/api/ds/v1'.length)
          }
          // 填充表单数据
          this.createForm = {
            datasourceId: res.datasourceId,
            name: res.name || '',
            path: displayPath,
            method: res.method || '',
            responseType: res.responseType || 'application/json',
            description: res.description || '',
            status: res.status || 1
          }
          this.createDialogVisible = true
          // 清除表单验证状态
          this.$nextTick(() => {
            if (this.$refs.createFormRef) {
              this.$refs.createFormRef.clearValidate()
            }
          })
        }
      }).catch(() => {
        this.$message.error('加载API详情失败')
      })
    },
    handleEdit(row) {
      this.$router.push({
        path: '/datax/integration/apiPublish/edit',
        query: { id: row.id, datasourceId: row.datasourceId, dataSource: row.dataSource }
      })
    },
    handleEnable(row) {
      this.$confirm(`确认启用 API「${row.name}」吗？`, '提示', {
        type: 'warning'
      }).then(() => {
        jobApi.updateStatus({
          id: row.id,
          status: 1
        }).then(() => {
          this.$message.success('启用API成功')
          this.fetchData()
        }).catch(() => {
          this.$message.error('启用API失败')
        })
      }).catch(() => {})
    },
    handleDisable(row) {
      this.$confirm(`确认禁用 API「${row.name}」吗？`, '提示', {
        type: 'warning'
      }).then(() => {
        jobApi.updateStatus({
          id: row.id,
          status: 0
        }).then(() => {
          this.$message.success('禁用API成功')
          this.fetchData()
        }).catch(() => {
          this.$message.error('禁用API失败')
        })
      }).catch(() => {})
    },
    handleDelete(row) {
      this.$confirm(`确认删除 API「${row.name}」吗？`, '提示', {
        type: 'warning'
      }).then(() => {
        jobApi.deleteApi([row.id]).then(() => {
          this.$message.success('删除API成功')
          this.fetchData()
        }).catch(() => {
          this.$message.error('删除API失败')
        })
      }).catch(() => {})
    },
    handleSizeChange(size) {
      this.query.pageSize = size
      this.query.pageNo = 1
      this.fetchData()
    },
    handleCurrentChange(page) {
      this.query.pageNo = page
      this.fetchData()
    },
    attachDatasourceName(records) {
      if (!records || !records.length) return []
      const dsMap = this.datasourceList.reduce((acc, cur) => {
        acc[cur.id] = cur.datasourceName || cur.datasource
        return acc
      }, {})
      return records.map(item => ({
        ...item,
        datasourceName: item.datasourceName || dsMap[item.datasourceId] || ''
      }))
    }
  }
}
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.filter-container {
  margin-bottom: 20px;
}

.filter-item {
  margin-right: 10px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style>

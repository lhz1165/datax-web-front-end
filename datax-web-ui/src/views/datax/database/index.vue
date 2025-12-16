<template>
  <div class="app-container">
    <h2>数据库管理</h2>

    <!-- 数据源选择区域 -->
    <el-card class="filter-card">
      <el-form :inline="true" class="filter-form">
        <el-form-item label="数据源">
          <el-select
            v-model="selectedDatasourceId"
            placeholder="请选择数据源"
            clearable
            filterable
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
        <el-form-item v-if="showSchemaSelect" label="Schema">
          <el-select
            v-model="selectedSchema"
            placeholder="请选择Schema"
            clearable
            filterable
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
        <el-form-item>
          <el-button
            type="primary"
            icon="el-icon-plus"
            :disabled="!selectedDatasourceId || (showSchemaSelect && !selectedSchema)"
            @click="handleCreateTable"
          >
            创建表
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 主内容区域 -->
    <div class="main-content">
      <!-- 左侧表列表 -->
      <el-card class="table-list-card">
        <div slot="header" class="card-header">
          <span>表列表</span>
          <el-input
            v-model="tableSearchKey"
            placeholder="搜索表名"
            size="small"
            clearable
            style="width: 150px;"
          >
            <i slot="prefix" class="el-input__icon el-icon-search" />
          </el-input>
        </div>
        <div class="table-list">
          <div
            v-for="table in filteredTableList"
            :key="table"
            :class="['table-item', { active: selectedTable === table }]"
            @click="handleTableClick(table)"
          >
            <i class="el-icon-s-grid" />
            <span>{{ table }}</span>
          </div>
          <el-empty v-if="filteredTableList.length === 0" description="暂无数据" :image-size="60" />
        </div>
      </el-card>

      <!-- 右侧表结构 -->
      <el-card class="table-structure-card">
        <div slot="header" class="card-header">
          <span>表结构</span>
          <div v-if="selectedTable" class="table-header-right">
            <span class="table-name">{{ selectedTable }}</span>
            <el-button
              type="primary"
              size="mini"
              icon="el-icon-edit"
              @click="handleAlterTable"
            >
              修改表
            </el-button>
            <el-button
              type="danger"
              size="mini"
              icon="el-icon-delete"
              @click="handleDropTable"
            >
              删除表
            </el-button>
          </div>
        </div>
        <el-table
          v-loading="columnsLoading"
          :data="columnList"
          border
          stripe
          size="small"
          style="width: 100%"
        >
          <el-table-column prop="index" label="序号" width="60" align="center" />
          <el-table-column prop="name" label="字段名" min-width="200" />
          <el-table-column v-if="hasDetailInfo" prop="type" label="数据类型" width="100" />
          <el-table-column v-if="hasDetailInfo" prop="length" label="长度" width="100" align="center">
            <template slot-scope="scope">
              {{ scope.row.length || '-' }}
            </template>
          </el-table-column>
          <el-table-column v-if="hasDetailInfo" prop="nullable" label="可为空" width="80" align="center">
            <template slot-scope="scope">
              <el-tag v-if="scope.row.nullable !== undefined" :type="scope.row.nullable ? 'success' : 'danger'" size="mini">
                {{ scope.row.nullable ? '是' : '否' }}
              </el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column v-if="hasDetailInfo" prop="primaryKey" label="主键" width="80" align="center">
            <template slot-scope="scope">
              <el-tag v-if="scope.row.primaryKey" type="warning" size="mini">PK</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column v-if="hasDetailInfo" prop="comment" label="备注" min-width="150">
            <template slot-scope="scope">
              {{ scope.row.comment || '-' }}
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!selectedTable && columnList.length === 0" description="请选择表查看结构" />
      </el-card>
    </div>
  </div>
</template>

<script>
import { list as getDatasourceList } from '@/api/datax-jdbcDatasource'
import * as metadataApi from '@/api/metadata-query'

export default {
  name: 'Database',
  data() {
    return {
      // 数据源相关
      datasourceList: [],
      selectedDatasourceId: null,
      currentDatasource: null,

      // Schema相关
      schemaList: [],
      selectedSchema: null,
      showSchemaSelect: false,

      // 表相关
      tableList: [],
      selectedTable: null,
      tableSearchKey: '',

      // 字段相关
      columnList: [],
      columnsLoading: false,

      // 查询参数
      datasourceQuery: {
        current: 1,
        size: 200
      }
    }
  },
  computed: {
    filteredTableList() {
      if (!this.tableSearchKey) {
        return this.tableList
      }
      const key = this.tableSearchKey.toLowerCase()
      return this.tableList.filter(table => table.toLowerCase().includes(key))
    },
    // 判断是否有详细字段信息（类型、可空等）
    hasDetailInfo() {
      return this.columnList.length > 0 && this.columnList[0].type !== undefined
    }
  },
  watch: {
    // 监听路由变化，刷新表列表
    '$route.query.refresh': {
      handler(val) {
        if (val && this.selectedDatasourceId) {
          this.loadTableList()
          // 如果已有选中表，同步刷新字段列表
          if (this.selectedTable) {
            this.loadColumnList()
          }
        }
      }
    },
    // 监听路由参数 datasourceId 的变化
    '$route.query.datasourceId': {
      handler(val) {
        if (val) {
          const datasourceId = parseInt(val)
          if (datasourceId !== this.selectedDatasourceId) {
            this.selectedDatasourceId = datasourceId
            // 如果数据源列表已加载，自动触发选择
            if (this.datasourceList.length > 0) {
              this.$nextTick(() => {
                this.handleDatasourceChange(datasourceId)
              })
            }
          }
        }
      },
      immediate: false
    }
  },
  created() {
    this.loadDatasourceList()
    // 如果路由参数中有 datasourceId，自动选择该数据源
    if (this.$route.query.datasourceId) {
      this.selectedDatasourceId = parseInt(this.$route.query.datasourceId)
    }
  },
  methods: {
    // 加载数据源列表
    loadDatasourceList() {
      getDatasourceList(this.datasourceQuery).then(response => {
        const { records } = response
        // 显示PostgreSQL和MySQL数据源
        this.datasourceList = records.filter(item => item.datasource === 'postgresql' || item.datasource === 'mysql')
        // 如果已设置了 selectedDatasourceId，自动触发选择
        if (this.selectedDatasourceId) {
          this.$nextTick(() => {
            this.handleDatasourceChange(this.selectedDatasourceId)
          })
        }
      }).catch(() => {
        // 接口未接入时使用模拟数据
        this.datasourceList = [
          { id: 1, datasourceName: '测试PG数据库', datasource: 'postgresql' },
          { id: 2, datasourceName: '生产PG数据库', datasource: 'postgresql' },
          { id: 3, datasourceName: '测试MySQL数据库', datasource: 'mysql' }
        ]
        // 如果已设置了 selectedDatasourceId，自动触发选择
        if (this.selectedDatasourceId) {
          this.$nextTick(() => {
            this.handleDatasourceChange(this.selectedDatasourceId)
          })
        }
      })
    },

    // 数据源变更
    handleDatasourceChange(val) {
      // 清空之前的选择
      this.selectedSchema = null
      this.tableList = []
      this.selectedTable = null
      this.columnList = []

      if (!val) {
        this.showSchemaSelect = false
        return
      }

      // 找到当前选中的数据源
      this.currentDatasource = this.datasourceList.find(item => item.id === val)

      // PostgreSQL需要先选择Schema
      if (this.currentDatasource && this.currentDatasource.datasource === 'postgresql') {
        this.showSchemaSelect = true
        this.loadSchemaList()
      } else {
        this.showSchemaSelect = false
        this.loadTableList()
      }
    },

    // 加载Schema列表
    loadSchemaList() {
      metadataApi.getTableSchema({ datasourceId: this.selectedDatasourceId }).then(response => {
        this.schemaList = response || []
      }).catch(() => {
        // 模拟数据
        this.schemaList = ['public', 'pg_catalog', 'information_schema']
      })
    },

    // Schema变更
    handleSchemaChange(val) {
      this.tableList = []
      this.selectedTable = null
      this.columnList = []

      if (val) {
        this.loadTableList()
      }
    },

    // 加载表列表
    loadTableList() {
      const params = {
        datasourceId: this.selectedDatasourceId
      }
      if (this.showSchemaSelect && this.selectedSchema) {
        params.tableSchema = this.selectedSchema
      }

      metadataApi.getTables(params).then(response => {
        this.tableList = response || []
      }).catch(() => {
        // 模拟数据
        this.tableList = [
          'users',
          'orders',
          'products',
          'categories',
          'order_items',
          'customers',
          'inventory',
          'suppliers'
        ]
      })
    },

    // 点击表
    handleTableClick(tableName) {
      this.selectedTable = tableName
      this.loadColumnList()
    },

    // 加载字段列表（使用新接口获取详细信息）
    loadColumnList() {
      this.columnsLoading = true
      const params = {
        datasourceId: this.selectedDatasourceId,
        tableName: this.selectedTable
      }

      metadataApi.getColumnsInfo(params).then(response => {
        // 接口返回 ColumnInfo 对象列表: { name, type, comment, ifPrimaryKey, isnull, length }
        this.columnList = (response || []).map((col, index) => ({
          index: index + 1,
          name: col.name,
          type: col.type || '-',
          length: col.length,
          nullable: col.isnull === 1, // 0 不可为空, 1 可以为null
          primaryKey: col.ifPrimaryKey || false,
          comment: col.comment || ''
        }))
        this.columnsLoading = false
      }).catch(() => {
        // 模拟数据
        this.columnList = [
          { index: 1, name: 'id', type: 'bigint', length: 20, nullable: false, primaryKey: true, comment: '主键ID' },
          { index: 2, name: 'name', type: 'varchar', length: 100, nullable: false, primaryKey: false, comment: '名称' },
          { index: 3, name: 'description', type: 'text', length: 65535, nullable: true, primaryKey: false, comment: '描述' },
          { index: 4, name: 'status', type: 'smallint', length: 6, nullable: false, primaryKey: false, comment: '状态' },
          { index: 5, name: 'created_at', type: 'timestamp', length: 26, nullable: false, primaryKey: false, comment: '创建时间' },
          { index: 6, name: 'updated_at', type: 'timestamp', length: 26, nullable: true, primaryKey: false, comment: '更新时间' }
        ]
        this.columnsLoading = false
      })
    },

    // 跳转到创建表页面
    handleCreateTable() {
      const query = {
        datasourceId: this.selectedDatasourceId,
        datasourceName: this.currentDatasource ? this.currentDatasource.datasourceName : '',
        datasourceType: this.currentDatasource ? this.currentDatasource.datasource : ''
      }
      if (this.showSchemaSelect && this.selectedSchema) {
        query.schema = this.selectedSchema
      }
      this.$router.push({
        path: '/datax/db/database/create-table',
        query
      })
    },

    // 跳转到修改表页面
    handleAlterTable() {
      const query = {
        datasourceId: this.selectedDatasourceId,
        datasourceName: this.currentDatasource ? this.currentDatasource.datasourceName : '',
        datasourceType: this.currentDatasource ? this.currentDatasource.datasource : '',
        tableName: this.selectedTable
      }
      if (this.showSchemaSelect && this.selectedSchema) {
        query.schema = this.selectedSchema
      }
      this.$router.push({
        path: '/datax/db/database/alter-table',
        query
      })
    },

    // 删除表
    handleDropTable() {
      this.$confirm(`确认删除表 "${this.selectedTable}" 吗？此操作不可恢复！`, '警告', {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }).then(() => {
        const params = {
          datasourceId: this.selectedDatasourceId,
          tableName: this.selectedTable
        }
        if (this.showSchemaSelect && this.selectedSchema) {
          params.tableSchema = this.selectedSchema
        }
        metadataApi.dropTable(params).then(() => {
          this.$message.success('表删除成功')
          // 清空选中状态并刷新表列表
          this.selectedTable = null
          this.columnList = []
          this.loadTableList()
        }).catch(err => {
          this.$message.error('删除失败: ' + (err.message || '未知错误'))
        })
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-form {
  margin-bottom: -18px;
}

.main-content {
  display: flex;
  gap: 20px;
  height: calc(100vh - 220px);
  min-height: 400px;
}

.table-list-card {
  width: 280px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
}

.table-list-card >>> .el-card__body {
  flex: 1;
  overflow: hidden;
  padding: 0;
}

.table-structure-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.table-structure-card >>> .el-card__body {
  flex: 1;
  overflow: auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-name {
  color: #409EFF;
  font-weight: bold;
}

.table-header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.table-list {
  height: 100%;
  overflow-y: auto;
  padding: 10px 0;
}

.table-item {
  padding: 10px 15px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s;
  border-left: 3px solid transparent;
}

.table-item:hover {
  background-color: #f5f7fa;
}

.table-item.active {
  background-color: #ecf5ff;
  border-left-color: #409EFF;
  color: #409EFF;
}

.table-item i {
  color: #909399;
}

.table-item.active i {
  color: #409EFF;
}
</style>

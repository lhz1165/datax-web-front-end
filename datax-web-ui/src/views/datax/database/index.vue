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
          <el-button
            type="primary"
            icon="el-icon-view"
            style="margin-left: 10px;"
            :disabled="!selectedDatasourceId || (showSchemaSelect && !selectedSchema)"
            @click="handleCreateView"
          >
            创建视图
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 主内容区域 -->
    <div class="main-content">
      <!-- 左侧表/视图列表 -->
      <el-card class="table-list-card">
        <div slot="header" class="card-header">
          <span>对象列表</span>
        </div>
        <div class="list-search">
          <el-input
            v-model="searchKey"
            placeholder="搜索表/视图名称"
            size="small"
            clearable
            prefix-icon="el-icon-search"
          />
        </div>
        <div class="table-list">
          <!-- 表节点 -->
          <div class="list-section-header" @click="showTables = !showTables">
            <div class="section-left">
              <i class="el-icon-s-grid section-icon table-icon" />
              <span class="section-title">表</span>
              <el-tag size="mini" effect="plain" type="info">{{ filteredTableList.length }}</el-tag>
            </div>
            <i :class="['el-icon-arrow-down','section-arrow', { 'is-collapsed': !showTables }]" />
          </div>
          <transition name="el-zoom-in-top">
            <div v-show="showTables" class="list-section-body">
          <div
            v-for="table in filteredTableList"
            :key="table"
                :class="['table-item', { active: selectedType === 'table' && selectedTable === table }]"
            @click="handleTableClick(table)"
          >
            <i class="el-icon-s-grid" />
            <span>{{ table }}</span>
          </div>
          <el-empty v-if="filteredTableList.length === 0" description="暂无数据" :image-size="60" />
            </div>
          </transition>

          <!-- 视图节点 -->
          <div class="list-section-header" style="margin-top: 16px;" @click="showViews = !showViews">
            <div class="section-left">
              <i class="el-icon-view section-icon view-icon" />
              <span class="section-title">视图</span>
              <el-tag size="mini" effect="plain" type="info">{{ filteredViewList.length }}</el-tag>
            </div>
            <i :class="['el-icon-arrow-down','section-arrow', { 'is-collapsed': !showViews }]" />
          </div>
          <transition name="el-zoom-in-top">
            <div v-show="showViews" class="list-section-body">
              <div
                v-for="view in filteredViewList"
                :key="view"
                :class="['table-item', { active: selectedType === 'view' && selectedView === view }]"
                @click="handleViewClick(view)"
              >
                <i class="el-icon-view" />
                <span>{{ view }}</span>
              </div>
              <el-empty v-if="filteredViewList.length === 0" description="暂无视图" :image-size="60" />
            </div>
          </transition>
        </div>
      </el-card>

      <!-- 右侧表结构 + 索引（上下布局） -->
      <div class="structure-pane">
        <!-- 表结构 -->
        <el-card class="table-structure-card structure-main">
        <div slot="header" class="card-header">
            <span>结构</span>
            <div v-if="selectedTable || selectedView" class="table-header-right">
              <span class="table-name">{{ selectedType === 'table' ? selectedTable : selectedView }}</span>
              <template v-if="selectedType === 'table'">
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
              </template>
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
          <el-empty v-if="!selectedTable && !selectedView && columnList.length === 0" description="请选择表或视图查看结构" />
        </el-card>

        <!-- 索引列表 -->
        <el-card class="table-structure-card index-card">
          <div slot="header" class="card-header">
            <span>索引</span>
            <div>
              <el-button
                v-if="selectedType === 'table'"
                type="primary"
                size="mini"
                icon="el-icon-plus"
                @click="openCreateIndex"
              >
                创建索引
              </el-button>
            </div>
          </div>
          <el-table
            v-loading="indexesLoading"
            :data="indexList"
            border
            stripe
            size="small"
            style="width: 100%"
            :max-height="220"
            empty-text="暂无索引"
          >
            <el-table-column prop="indexName" label="索引名" min-width="140" :show-overflow-tooltip="true" />
            <el-table-column prop="columns" label="列" min-width="160" :show-overflow-tooltip="true" />
            <el-table-column prop="indexType" label="类型" width="120" v-if="indexList && indexList.length && indexList[0].indexType !== undefined" />
            <el-table-column prop="nonUnique" label="是否唯一" width="100" align="center" v-if="indexList && indexList.length && indexList[0].nonUnique !== undefined">
              <template slot-scope="scope">
                <el-tag size="mini" :type="scope.row.nonUnique ? 'info' : 'success'">
                  {{ scope.row.nonUnique ? '否' : '是' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="definition" label="定义" min-width="260" v-if="indexList && indexList.length && indexList[0].definition !== undefined" />
          </el-table>
      </el-card>

      <!-- 创建索引弹窗 -->
      <el-dialog
        title="创建索引"
        :visible.sync="createIndexDialogVisible"
        width="700px"
      >
        <el-form label-width="80px">
          <el-form-item label="SQL">
            <el-input
              v-model="createIndexSql"
              type="textarea"
              :rows="8"
              placeholder="请输入创建索引的SQL，例如：CREATE INDEX idx_name ON table(column1, column2);"
            />
          </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
          <el-button @click="createIndexDialogVisible = false">取消</el-button>
          <el-button type="primary" :disabled="!createIndexSql" @click="handleCreateIndex">创建</el-button>
        </span>
      </el-dialog>

      <!-- 创建视图弹窗 -->
      <el-dialog
        title="创建视图"
        :visible.sync="createViewDialogVisible"
        width="700px"
      >
        <el-form label-width="80px">
          <el-form-item label="SQL">
            <el-input
              v-model="createViewSql"
              type="textarea"
              :rows="10"
              placeholder="请输入创建视图的SQL，例如：CREATE VIEW view_name AS SELECT * FROM table WHERE condition;"
            />
          </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
          <el-button @click="createViewDialogVisible = false">取消</el-button>
          <el-button type="primary" :disabled="!createViewSql" @click="handleCreateViewSubmit">创建</el-button>
        </span>
      </el-dialog>
      </div>
    </div>
  </div>
</template>

<script>
import { list as getDatasourceList } from '@/api/datax-jdbcDatasource'
import * as metadataApi from '@/api/metadata-query'
import { createIndex } from '@/api/metadata-query'

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

      // 视图相关
      viewList: [],
      selectedView: null,

      // 通用搜索
      searchKey: '',

      // 折叠状态（默认收起）
      showTables: false,
      showViews: false,

      // 当前选中类型：table | view
      selectedType: 'table',

      // 字段相关
      columnList: [],
      columnsLoading: false,

      // 索引相关
      indexList: [],
      indexesLoading: false,
      createIndexDialogVisible: false,
      createIndexSql: '',

      // 视图创建相关
      createViewDialogVisible: false,
      createViewSql: '',

      // 查询参数
      datasourceQuery: {
        current: 1,
        size: 200
      }
    }
  },
  computed: {
    filteredTableList() {
      if (!this.searchKey) {
        return this.tableList
      }
      const key = this.searchKey.toLowerCase()
      return this.tableList.filter(table => table.toLowerCase().includes(key))
    },
    // 视图过滤
    filteredViewList() {
      if (!this.searchKey) {
        return this.viewList
      }
      const key = this.searchKey.toLowerCase()
      return this.viewList.filter(view => view.toLowerCase().includes(key))
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
    },
    // 搜索框输入后，如有结果自动展开表/视图列表
    searchKey(val) {
      if (val) {
        if (this.filteredTableList.length > 0) {
          this.showTables = true
        }
        if (this.filteredViewList.length > 0) {
          this.showViews = true
        }
      }
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
      this.viewList = []
      this.selectedView = null
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
        this.loadViewList()
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
      this.viewList = []
      this.selectedView = null
      this.columnList = []

      if (val) {
        this.loadTableList()
        this.loadViewList()
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

    // 加载视图列表
    loadViewList() {
      const params = {
        datasourceId: this.selectedDatasourceId
      }
      if (this.showSchemaSelect && this.selectedSchema) {
        params.tableSchema = this.selectedSchema
      }
      metadataApi.getViews(params).then(response => {
        this.viewList = response || []
      }).catch(() => {
        // 默认无视图
        this.viewList = []
      })
    },

    // 点击表
    handleTableClick(tableName) {
      this.selectedType = 'table'
      this.selectedTable = tableName
      this.selectedView = null
      this.loadColumnList()
      this.loadIndexList()
    },

    // 点击视图
    handleViewClick(viewName) {
      this.selectedType = 'view'
      this.selectedView = viewName
      this.selectedTable = null
      this.loadColumnList()
      this.loadIndexList()
    },

    // 加载字段列表（使用新接口获取详细信息）
    loadColumnList() {
      this.columnsLoading = true
      const params = {
        datasourceId: this.selectedDatasourceId,
        tableName: this.selectedType === 'table' ? this.selectedTable : this.selectedView
      }
      if (this.showSchemaSelect && this.selectedSchema) {
        params.tableSchema = this.selectedSchema
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

    // 加载索引列表
    loadIndexList() {
      if (this.selectedType !== 'table') {
        this.indexList = []
        this.indexesLoading = false
        return
      }
      this.indexesLoading = true
      const params = {
        datasourceId: this.selectedDatasourceId,
        tableName: this.selectedType === 'table' ? this.selectedTable : this.selectedView
      }
      if (this.showSchemaSelect && this.selectedSchema) {
        params.tableSchema = this.selectedSchema
      }
      metadataApi.getIndexes(params).then(response => {
        this.indexList = response || []
        this.indexesLoading = false
      }).catch(() => {
        this.indexList = []
        this.indexesLoading = false
      })
    },

    openCreateIndex() {
      this.createIndexSql = ''
      this.createIndexDialogVisible = true
    },

    handleCreateIndex() {
      if (!this.createIndexSql) {
        this.$message.warning('请输入索引SQL')
        return
      }
      createIndex({
        datasourceId: this.selectedDatasourceId,
        indexSql: this.createIndexSql
      }).then(() => {
        this.$message.success('索引创建成功')
        this.createIndexDialogVisible = false
        this.loadIndexList()
      }).catch(err => {
        this.$message.error('创建失败：' + (err.message || '未知错误'))
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
        path: '/datax/integration/database/create-table',
        query
      })
    },

    // 打开创建视图弹窗
    handleCreateView() {
      this.createViewSql = ''
      this.createViewDialogVisible = true
    },

    // 提交创建视图（带二次确认）
    handleCreateViewSubmit() {
      if (!this.createViewSql) {
        this.$message.warning('请输入视图SQL')
        return
      }
      // 二次确认
      this.$confirm('确认创建该视图吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        metadataApi.createView({
          datasourceId: this.selectedDatasourceId,
          viewSql: this.createViewSql
        }).then(() => {
          this.$message.success('视图创建成功')
          this.createViewDialogVisible = false
          this.createViewSql = ''
          // 刷新视图列表
          this.loadViewList()
        }).catch(err => {
          this.$message.error('创建失败：' + (err.message || '未知错误'))
        })
      }).catch(() => {
        // 用户取消，不做任何操作
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
        path: '/datax/integration/database/alter-table',
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
  min-width: 0;
}

.table-list-card {
  width: 280px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
}

.table-list-card >>> .el-card__body {
  flex: 1;
  overflow: hidden;
  padding: 0;
}

.table-structure-card {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  min-width: 0;
}

.table-structure-card >>> .el-card__body {
  flex: 1;
  overflow: auto;
}

.structure-pane {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-width: 0;
}

.structure-main {
  flex: 1 1 auto;
  min-height: 320px; /* 约 8 行高度 */
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.index-card {
  width: 100%;
  flex: 0 0 auto;
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
  background: #fafafa;
}

.list-search {
  padding: 10px 12px 0 12px;
  background: #fafafa;
}

.table-item {
  padding: 10px 15px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s;
  border-left: 3px solid transparent;
  border-bottom: 1px solid #f0f0f0;
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

.list-section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 12px;
  background: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
}

.section-left {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  color: #606266;
}

.section-title {
  font-size: 13px;
}

.section-icon {
  font-size: 14px;
  color: #909399;
}

.table-icon {
  color: #67c23a;
}

.view-icon {
  color: #409EFF;
}

.list-section-body {
  padding: 4px 0;
}

.section-arrow {
  font-size: 14px;
  color: #909399;
  transition: transform 0.2s;
}

.section-arrow.is-collapsed {
  transform: rotate(-90deg);
}
</style>

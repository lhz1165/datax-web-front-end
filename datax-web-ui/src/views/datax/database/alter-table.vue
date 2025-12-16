<template>
  <div class="app-container">
    <!-- 头部信息 -->
    <el-page-header @back="goBack" content="修改表">
      <template slot="content">
        <span>修改表</span>
        <el-tag size="small" style="margin-left: 10px;">{{ datasourceName }}</el-tag>
        <el-tag v-if="schema" size="small" type="info" style="margin-left: 5px;">{{ schema }}</el-tag>
        <el-tag size="small" type="warning" style="margin-left: 5px;">{{ tableName }}</el-tag>
      </template>
    </el-page-header>

    <!-- 表基本信息 -->
    <el-card class="form-card">
      <div slot="header">
        <span>表基本信息</span>
      </div>
      <el-form ref="tableForm" :model="tableForm" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="表名">
              <el-input v-model="tableForm.tableName" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label="表注释">
              <el-input v-model="tableForm.tableComment" placeholder="请输入表注释" />
              <el-button type="text" size="small" @click="updateTableComment">修改注释</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 现有字段列表 -->
    <el-card class="form-card">
      <div slot="header" class="card-header">
        <span>现有字段</span>
      </div>
      <el-table :data="existingColumns" border stripe size="small" style="width: 100%" v-loading="loading">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="name" label="字段名" min-width="150" />
        <el-table-column prop="type" label="数据类型" width="120" />
        <el-table-column prop="length" label="长度" width="80" align="center">
          <template slot-scope="scope">
            {{ scope.row.length || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="nullable" label="可为空" width="80" align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row.nullable ? 'success' : 'danger'" size="mini">
              {{ scope.row.nullable ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="primaryKey" label="主键" width="80" align="center">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.primaryKey" type="warning" size="mini">是</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="comment" label="注释" min-width="150" />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button
              type="text"
              size="small"
              icon="el-icon-edit"
              @click="editColumn(scope.row)"
            >修改</el-button>
            <el-button
              type="text"
              size="small"
              icon="el-icon-delete"
              style="color: #F56C6C;"
              :disabled="scope.row.primaryKey"
              @click="dropColumn(scope.row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加新字段 -->
    <el-card class="form-card">
      <div slot="header" class="card-header">
        <span>添加新字段</span>
        <el-button type="primary" size="small" icon="el-icon-plus" @click="addNewColumn">添加字段</el-button>
      </div>
      <el-table v-if="newColumns.length > 0" :data="newColumns" border stripe size="small" style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="name" label="字段名" min-width="150">
          <template slot-scope="scope">
            <el-input v-model="scope.row.name" size="small" placeholder="字段名" />
          </template>
        </el-table-column>
        <el-table-column prop="type" label="数据类型" width="140">
          <template slot-scope="scope">
            <el-select v-model="scope.row.type" size="small" filterable allow-create placeholder="类型">
              <el-option-group v-for="group in dataTypeGroups" :key="group.label" :label="group.label">
                <el-option
                  v-for="item in group.options"
                  :key="item"
                  :label="item"
                  :value="item"
                />
              </el-option-group>
            </el-select>
          </template>
        </el-table-column>
        <el-table-column prop="length" label="长度" width="100">
          <template slot-scope="scope">
            <el-input-number
              v-model="scope.row.length"
              size="small"
              :min="0"
              :controls="false"
              placeholder="长度"
            />
          </template>
        </el-table-column>
        <el-table-column prop="nullable" label="可为空" width="80" align="center">
          <template slot-scope="scope">
            <el-checkbox v-model="scope.row.nullable" />
          </template>
        </el-table-column>
        <el-table-column prop="defaultValue" label="默认值" width="120">
          <template slot-scope="scope">
            <el-input v-model="scope.row.defaultValue" size="small" placeholder="默认值" />
          </template>
        </el-table-column>
        <el-table-column prop="comment" label="注释" min-width="150">
          <template slot-scope="scope">
            <el-input v-model="scope.row.comment" size="small" placeholder="注释" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button
              type="text"
              size="small"
              icon="el-icon-delete"
              style="color: #F56C6C;"
              @click="removeNewColumn(scope.$index)"
            />
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="暂无新增字段" :image-size="60" />
    </el-card>

    <!-- 底部操作按钮 -->
    <div class="footer-actions">
      <el-button @click="goBack">返回</el-button>
      <el-button type="info" @click="previewSQL" :disabled="newColumns.length === 0">预览SQL</el-button>
      <el-button type="primary" :loading="submitting" :disabled="newColumns.length === 0" @click="handleSubmit">执行修改</el-button>
    </div>

    <!-- SQL预览弹窗 -->
    <el-dialog title="修改表SQL预览" :visible.sync="sqlDialogVisible" width="600px">
      <pre class="sql-preview">{{ generatedSQL }}</pre>
      <span slot="footer" class="dialog-footer">
        <el-button size="small" @click="sqlDialogVisible = false">关闭</el-button>
        <el-button size="small" type="primary" @click="copySQL">复制SQL</el-button>
      </span>
    </el-dialog>

    <!-- 修改字段弹窗 -->
    <el-dialog :title="'修改字段: ' + editingColumn.name" :visible.sync="editColumnDialogVisible" width="500px">
      <el-form :model="editingColumn" label-width="80px">
        <el-form-item label="字段名">
          <el-input v-model="editingColumn.newName" :placeholder="editingColumn.name" />
        </el-form-item>
        <el-form-item label="数据类型">
          <el-select v-model="editingColumn.newType" filterable allow-create placeholder="类型" style="width: 100%">
            <el-option v-for="item in dataTypeList" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="长度">
          <el-input-number v-model="editingColumn.newLength" :min="0" :controls="false" style="width: 100%" />
        </el-form-item>
        <el-form-item label="注释">
          <el-input v-model="editingColumn.newComment" placeholder="字段注释" />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button size="small" @click="editColumnDialogVisible = false">取消</el-button>
        <el-button size="small" type="primary" :loading="editColumnLoading" @click="submitEditColumn">确定修改</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getColumnsInfo, alterTable } from '@/api/metadata-query'

export default {
  name: 'AlterTable',
  data() {
    return {
      // 数据源信息
      datasourceId: null,
      datasourceName: '',
      datasourceType: '',
      schema: '',
      tableName: '',

      // 表信息
      tableForm: {
        tableName: '',
        tableComment: ''
      },

      // 现有字段
      existingColumns: [],
      loading: false,

      // 新增字段
      newColumns: [],

      // 数据类型分组（用于添加新字段）
      dataTypeGroups: [
        {
          label: '数值类型',
          options: ['int', 'bigint', 'smallint', 'tinyint', 'decimal', 'numeric', 'float', 'double', 'real']
        },
        {
          label: '字符类型',
          options: ['varchar', 'char', 'text', 'longtext', 'mediumtext', 'tinytext']
        },
        {
          label: '日期时间',
          options: ['date', 'time', 'datetime', 'timestamp', 'year']
        },
        {
          label: '其他类型',
          options: ['boolean', 'json', 'jsonb', 'uuid', 'bytea', 'blob']
        }
      ],

      // 数据类型列表（用于修改字段弹窗）
      dataTypeList: [
        'int', 'bigint', 'smallint', 'tinyint', 'decimal', 'numeric', 'float', 'double', 'real',
        'varchar', 'char', 'text', 'longtext', 'mediumtext', 'tinytext',
        'date', 'time', 'datetime', 'timestamp', 'year',
        'boolean', 'json', 'jsonb', 'uuid', 'bytea', 'blob'
      ],

      // 状态
      submitting: false,
      sqlDialogVisible: false,
      generatedSQL: '',

      // 修改字段弹窗
      editColumnDialogVisible: false,
      editColumnLoading: false,
      editingColumn: {
        name: '',
        type: '',
        newName: '',
        newType: '',
        newLength: null,
        newComment: ''
      }
    }
  },
  created() {
    this.initFromRoute()
  },
  watch: {
    $route(to) {
      if (to && to.query) {
        this.initFromRoute()
      }
    }
  },
  methods: {
    // 根据路由参数初始化
    initFromRoute() {
      this.datasourceId = Number(this.$route.query.datasourceId)
      this.datasourceName = this.$route.query.datasourceName || '未知数据源'
      this.datasourceType = this.$route.query.datasourceType || ''
      this.schema = this.$route.query.schema || ''
      this.tableName = this.$route.query.tableName || ''
      this.tableForm.tableName = this.tableName
      this.loadExistingColumns()
    },

    // 返回上一页
    goBack() {
      this.$router.back()
    },

    // 加载现有字段
    loadExistingColumns() {
      if (!this.datasourceId || !this.tableName) return
      this.loading = true
      const params = {
        datasourceId: this.datasourceId,
        tableName: this.tableName
      }
      getColumnsInfo(params).then(response => {
        this.existingColumns = (response || []).map(col => ({
          name: col.name,
          type: col.type || '',
          length: col.length,
          nullable: col.isnull === 1,
          primaryKey: col.ifPrimaryKey || false,
          comment: col.comment || ''
        }))
        this.loading = false
      }).catch(() => {
        this.loading = false
        this.$message.error('加载字段信息失败')
      })
    },

    // 添加新字段
    addNewColumn() {
      this.newColumns.push({
        name: '',
        type: 'varchar',
        length: 255,
        nullable: true,
        defaultValue: '',
        comment: ''
      })
    },

    // 移除新字段
    removeNewColumn(index) {
      this.newColumns.splice(index, 1)
    },

    // 编辑现有字段
    editColumn(row) {
      this.editingColumn = {
        name: row.name,
        type: row.type,
        newName: row.name,
        newType: row.type,
        newLength: row.length,
        newComment: row.comment
      }
      this.editColumnDialogVisible = true
    },

    // 提交修改字段
    submitEditColumn() {
      if (!this.editingColumn.newName) {
        this.$message.warning('请输入字段名')
        return
      }
      if (!this.editingColumn.newType) {
        this.$message.warning('请选择数据类型')
        return
      }
      this.editColumnLoading = true
      const params = {
        datasourceId: this.datasourceId,
        tableName: this.tableName,
        tableSchema: this.schema || null,
        alterType: 'MODIFY_COLUMN',
        columnName: this.editingColumn.name,
        newName: this.editingColumn.newName,
        newType: this.editingColumn.newType,
        newLength: this.editingColumn.newLength,
        newComment: this.editingColumn.newComment
      }
      alterTable(params).then(() => {
        this.$message.success('修改字段成功')
        this.editColumnDialogVisible = false
        this.editColumnLoading = false
        this.loadExistingColumns()
        // 返回列表页时刷新表结构
        this.$router.push({
          path: '/datax/db/database',
          query: { refresh: Date.now() }
        })
      }).catch(err => {
        this.editColumnLoading = false
        this.$message.error(err.msg || '修改字段失败')
      })
    },

    // 删除字段
    dropColumn(row) {
      this.$confirm(`确认删除字段 "${row.name}" 吗？此操作不可恢复！`, '警告', {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }).then(() => {
        const params = {
          datasourceId: this.datasourceId,
          tableName: this.tableName,
          tableSchema: this.schema || null,
          alterType: 'DROP_COLUMN',
          columnName: row.name
        }
        alterTable(params).then(() => {
          this.$message.success('删除字段成功')
          this.loadExistingColumns()
          // 返回列表页时刷新表结构
          this.$router.push({
            path: '/datax/db/database',
            query: { refresh: Date.now() }
          })
        }).catch(err => {
          this.$message.error(err.msg || '删除字段失败')
        })
      }).catch(() => {})
    },

    // 修改表注释
    updateTableComment() {
      const params = {
        datasourceId: this.datasourceId,
        tableName: this.tableName,
        tableSchema: this.schema || null,
        alterType: 'MODIFY_COMMENT',
        tableComment: this.tableForm.tableComment
      }
      alterTable(params).then(() => {
        this.$message.success('修改表注释成功')
        // 返回列表页时刷新表结构
        this.$router.push({
          path: '/datax/db/database',
          query: { refresh: Date.now() }
        })
      }).catch(err => {
        this.$message.error(err.msg || '修改表注释失败')
      })
    },

    // 生成SQL
    generateSQL() {
      const isPostgres = this.datasourceType === 'postgresql'
      const isMysql = this.datasourceType === 'mysql'

      let tableName
      if (isPostgres) {
        tableName = this.schema
          ? `"${this.schema}"."${this.tableName}"`
          : `"${this.tableName}"`
      } else {
        tableName = `\`${this.tableName}\``
      }

      const sqls = []

      // 添加新字段的SQL
      this.newColumns.forEach(col => {
        if (!col.name) return
        const quote = isPostgres ? '"' : '`'
        let colDef = `${quote}${col.name}${quote} ${col.type}`

        if (col.length && ['varchar', 'char', 'decimal', 'numeric'].includes(col.type.toLowerCase())) {
          colDef += `(${col.length})`
        }
        if (isMysql && col.length && ['int', 'bigint', 'smallint', 'tinyint'].includes(col.type.toLowerCase())) {
          colDef += `(${col.length})`
        }

        if (!col.nullable) {
          colDef += ' NOT NULL'
        }

        if (col.defaultValue) {
          colDef += ` DEFAULT ${col.defaultValue}`
        }

        if (isMysql && col.comment) {
          colDef += ` COMMENT '${col.comment}'`
        }

        sqls.push(`ALTER TABLE ${tableName} ADD COLUMN ${colDef};`)

        // PostgreSQL字段注释
        if (isPostgres && col.comment) {
          sqls.push(`COMMENT ON COLUMN ${tableName}.${quote}${col.name}${quote} IS '${col.comment}';`)
        }
      })

      return sqls.join('\n\n')
    },

    // 预览SQL
    previewSQL() {
      if (this.newColumns.length === 0) {
        this.$message.warning('请先添加新字段')
        return
      }
      this.generatedSQL = this.generateSQL()
      this.sqlDialogVisible = true
    },

    // 复制SQL
    copySQL() {
      navigator.clipboard.writeText(this.generatedSQL).then(() => {
        this.$message.success('SQL已复制到剪贴板')
      }).catch(() => {
        this.$message.error('复制失败')
      })
    },

    // 提交添加新字段
    handleSubmit() {
      if (this.newColumns.length === 0) {
        this.$message.warning('请先添加新字段')
        return
      }

      // 验证新字段
      for (const col of this.newColumns) {
        if (!col.name) {
          this.$message.warning('字段名不能为空')
          return
        }
        if (!col.type) {
          this.$message.warning('请选择数据类型')
          return
        }
      }

      this.submitting = true
      const params = {
        datasourceId: this.datasourceId,
        tableName: this.tableName,
        tableSchema: this.schema || null,
        alterType: 'ADD_COLUMNS',
        columns: this.newColumns.map(col => ({
          name: col.name,
          type: col.type,
          length: col.length,
          nullable: col.nullable,
          defaultValue: col.defaultValue,
          comment: col.comment
        }))
      }

      alterTable(params).then(() => {
        this.$message.success('添加字段成功')
        this.submitting = false
        this.newColumns = []
        this.loadExistingColumns()
        // 返回列表页时刷新表结构
        this.$router.push({
          path: '/datax/db/database',
          query: { refresh: Date.now() }
        })
      }).catch(err => {
        this.submitting = false
        this.$message.error(err.msg || '添加字段失败')
      })
    }
  }
}
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.form-card {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.footer-actions {
  margin-top: 20px;
  text-align: center;
}

.sql-preview {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 13px;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 400px;
  overflow-y: auto;
}
</style>

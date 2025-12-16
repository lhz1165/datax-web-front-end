<template>
  <div class="app-container">
    <!-- 头部信息 -->
    <el-page-header @back="goBack" content="创建表">
      <template slot="content">
        <span>创建表</span>
        <el-tag size="small" style="margin-left: 10px;">{{ datasourceName }}</el-tag>
        <el-tag v-if="schema" size="small" type="info" style="margin-left: 5px;">{{ schema }}</el-tag>
      </template>
    </el-page-header>

    <!-- 表基本信息 -->
    <el-card class="form-card">
      <div slot="header">
        <span>表基本信息</span>
      </div>
      <el-form ref="tableForm" :model="tableForm" :rules="tableRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="表名" prop="tableName">
              <el-input v-model="tableForm.tableName" placeholder="请输入表名" />
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label="表注释" prop="tableComment">
              <el-input v-model="tableForm.tableComment" placeholder="请输入表注释" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 字段列表 -->
    <el-card class="form-card">
      <div slot="header" class="card-header">
        <span>字段列表</span>
        <el-button type="primary" size="small" icon="el-icon-plus" @click="addColumn">添加字段</el-button>
      </div>
      <el-table :data="columns" border stripe size="small" style="width: 100%">
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
        <el-table-column prop="primaryKey" label="主键" width="80" align="center">
          <template slot-scope="scope">
            <el-checkbox v-model="scope.row.primaryKey" @change="handlePrimaryKeyChange(scope.$index)" />
          </template>
        </el-table-column>
        <el-table-column prop="autoIncrement" label="自增" width="80" align="center">
          <template slot-scope="scope">
            <el-checkbox v-model="scope.row.autoIncrement" :disabled="!scope.row.primaryKey" />
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
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button
              type="text"
              size="small"
              icon="el-icon-arrow-up"
              :disabled="scope.$index === 0"
              @click="moveColumn(scope.$index, -1)"
            />
            <el-button
              type="text"
              size="small"
              icon="el-icon-arrow-down"
              :disabled="scope.$index === columns.length - 1"
              @click="moveColumn(scope.$index, 1)"
            />
            <el-button
              type="text"
              size="small"
              icon="el-icon-delete"
              style="color: #F56C6C;"
              @click="removeColumn(scope.$index)"
            />
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 底部操作按钮 -->
    <div class="footer-actions">
      <el-button @click="goBack">取消</el-button>
      <el-button type="info" @click="previewSQL">预览SQL</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">创建表</el-button>
    </div>

    <!-- SQL预览弹窗 -->
    <el-dialog title="创建表SQL预览" :visible.sync="sqlDialogVisible" width="600px">
      <pre class="sql-preview">{{ generatedSQL }}</pre>
      <span slot="footer" class="dialog-footer">
        <el-button size="small" @click="sqlDialogVisible = false">关闭</el-button>
        <el-button size="small" type="primary" @click="copySQL">复制SQL</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { createTableWithColumns } from '@/api/metadata-query'

export default {
  name: 'CreateTable',
  data() {
    return {
      // 数据源信息
      datasourceId: null,
      datasourceName: '',
      datasourceType: '',
      schema: '',

      // 表单数据
      tableForm: {
        tableName: '',
        tableComment: ''
      },
      tableRules: {
        tableName: [
          { required: true, message: '请输入表名', trigger: 'blur' },
          { pattern: /^[a-zA-Z_][a-zA-Z0-9_]*$/, message: '表名只能包含字母、数字和下划线，且不能以数字开头', trigger: 'blur' }
        ]
      },

      // 字段列表
      columns: [
        { name: 'id', type: 'bigint', length: null, nullable: false, primaryKey: true, autoIncrement: true, defaultValue: '', comment: '主键ID' }
      ],

      // 数据类型分组
      dataTypeGroups: [
        {
          label: '数值类型',
          options: ['int', 'bigint', 'smallint', 'tinyint', 'decimal', 'numeric', 'float', 'double', 'real']
        },
        {
          label: '字符类型',
          options: ['varchar', 'char', 'text', 'longtext', 'mediumtext']
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

      // 状态
      submitting: false,
      sqlDialogVisible: false,
      generatedSQL: ''
    }
  },
  created() {
    // 初始化路由参数
    this.initFromRoute()
  },
  watch: {
    // 监听路由变化，当从列表页切换数据源后再次进入创建表页时更新数据源信息
    $route(to) {
      if (to && to.query) {
        this.initFromRoute()
      }
    }
  },
  methods: {
    // 根据路由参数初始化数据源信息
    initFromRoute() {
      this.datasourceId = Number(this.$route.query.datasourceId)
      this.datasourceName = this.$route.query.datasourceName || '未知数据源'
      this.datasourceType = this.$route.query.datasourceType || ''
      this.schema = this.$route.query.schema || ''
    },
    // 返回上一页
    goBack() {
      this.$router.back()
    },

    // 添加字段
    addColumn() {
      this.columns.push({
        name: '',
        type: 'varchar',
        length: 255,
        nullable: true,
        primaryKey: false,
        autoIncrement: false,
        defaultValue: '',
        comment: ''
      })
    },

    // 移除字段
    removeColumn(index) {
      if (this.columns.length <= 1) {
        this.$message.warning('至少保留一个字段')
        return
      }
      this.columns.splice(index, 1)
    },

    // 移动字段
    moveColumn(index, direction) {
      const newIndex = index + direction
      const temp = this.columns[index]
      this.$set(this.columns, index, this.columns[newIndex])
      this.$set(this.columns, newIndex, temp)
    },

    // 主键变更处理（确保只有一个主键）
    handlePrimaryKeyChange(index) {
      if (this.columns[index].primaryKey) {
        // 取消其他主键
        this.columns.forEach((col, i) => {
          if (i !== index) {
            col.primaryKey = false
          }
        })
        // 主键不能为空
        this.columns[index].nullable = false
      }
    },

    // 生成SQL
    generateSQL() {
      const isPostgres = this.datasourceType === 'postgresql'
      const isMysql = this.datasourceType === 'mysql'

      let tableName
      if (isPostgres) {
        tableName = this.schema
          ? `"${this.schema}"."${this.tableForm.tableName}"`
          : `"${this.tableForm.tableName}"`
      } else {
        tableName = `\`${this.tableForm.tableName}\``
      }

      let sql = `CREATE TABLE ${tableName} (\n`

      const columnDefs = this.columns.map(col => {
        const quote = isPostgres ? '"' : '`'
        let def = `  ${quote}${col.name}${quote} `

        // 处理自增类型
        if (col.autoIncrement && isPostgres) {
          // PostgreSQL使用SERIAL/BIGSERIAL
          def += col.type.toLowerCase() === 'bigint' ? 'BIGSERIAL' : 'SERIAL'
        } else {
          def += col.type
          if (col.length && ['varchar', 'char', 'decimal', 'numeric'].includes(col.type.toLowerCase())) {
            def += `(${col.length})`
          }
          if (isMysql && col.length && ['int', 'bigint', 'smallint', 'tinyint'].includes(col.type.toLowerCase())) {
            def += `(${col.length})`
          }
        }

        if (!col.nullable) {
          def += ' NOT NULL'
        }

        // MySQL自增
        if (col.autoIncrement && isMysql) {
          def += ' AUTO_INCREMENT'
        }

        // 默认值（自增字段不需要）
        if (col.defaultValue && !col.autoIncrement) {
          def += ` DEFAULT ${col.defaultValue}`
        }

        // MySQL字段注释
        if (isMysql && col.comment) {
          def += ` COMMENT '${col.comment}'`
        }

        return def
      })

      sql += columnDefs.join(',\n')

      // 添加主键约束
      const quote = isPostgres ? '"' : '`'
      const pkColumns = this.columns.filter(col => col.primaryKey).map(col => `${quote}${col.name}${quote}`)
      if (pkColumns.length > 0) {
        sql += `,\n  PRIMARY KEY (${pkColumns.join(', ')})`
      }

      sql += '\n)'

      // MySQL引擎和字符集设置
      if (isMysql) {
        sql += ' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci'
        // MySQL表注释
        if (this.tableForm.tableComment) {
          sql += ` COMMENT='${this.tableForm.tableComment}'`
        }
      }

      sql += ';'

      // PostgreSQL表注释
      if (isPostgres && this.tableForm.tableComment) {
        sql += `\n\nCOMMENT ON TABLE ${tableName} IS '${this.tableForm.tableComment}';`
      }

      // PostgreSQL字段注释
      if (isPostgres) {
        const columnComments = this.columns
          .filter(col => col.comment)
          .map(col => `COMMENT ON COLUMN ${tableName}."${col.name}" IS '${col.comment}';`)

        if (columnComments.length > 0) {
          sql += '\n\n' + columnComments.join('\n')
        }
      }

      return sql
    },

    // 预览SQL
    previewSQL() {
      if (!this.validateForm()) return
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

    // 表单验证
    validateForm() {
      if (!this.tableForm.tableName) {
        this.$message.warning('请输入表名')
        return false
      }
      if (this.columns.length === 0) {
        this.$message.warning('请至少添加一个字段')
        return false
      }
      const emptyNameColumn = this.columns.find(col => !col.name)
      if (emptyNameColumn) {
        this.$message.warning('字段名不能为空')
        return false
      }
      const emptyTypeColumn = this.columns.find(col => !col.type)
      if (emptyTypeColumn) {
        this.$message.warning('字段类型不能为空')
        return false
      }
      return true
    },

    // 提交创建
    handleSubmit() {
      console.log('handleSubmit called')
      console.log('datasourceId:', this.datasourceId)
      console.log('tableName:', this.tableForm.tableName)
      console.log('columns:', this.columns)
      if (!this.validateForm()) {
        console.log('validateForm failed')
        return
      }
      console.log('validateForm passed')

      this.$confirm('确认创建该表吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        console.log('confirm dialog confirmed')
        this.submitting = true
        // 构建请求参数
        const data = {
          datasourceId: this.datasourceId,
          tableName: this.tableForm.tableName,
          tableComment: this.tableForm.tableComment,
          tableSchema: this.schema,
          columns: this.columns.map(col => ({
            name: col.name,
            type: col.type,
            length: col.length,
            nullable: col.nullable,
            primaryKey: col.primaryKey,
            autoIncrement: col.autoIncrement,
            defaultValue: col.defaultValue,
            comment: col.comment
          }))
        }
        console.log('request data:', JSON.stringify(data))
        // 调用后端接口创建表
        createTableWithColumns(data).then(res => {
          console.log('createTable success:', res)
          this.submitting = false
          this.$message.success('表创建成功')
          // 返回并刷新表列表
          this.$router.push({
            path: '/datax/db/database',
            query: { refresh: Date.now() }
          })
        }).catch(err => {
          console.log('createTable error:', err)
          this.submitting = false
          this.$message.error('创建失败: ' + (err.message || '未知错误'))
        })
      }).catch(() => {
        console.log('confirm dialog cancelled')
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
  padding: 20px 0;
}

.sql-preview {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  font-family: 'Courier New', Courier, monospace;
  font-size: 13px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 400px;
  overflow-y: auto;
}
</style>

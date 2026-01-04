<template>
  <div class="app-container">
    <el-form label-position="right" label-width="120px" :model="readerForm" :rules="rules">
      <el-form-item label="数据库源：" prop="datasourceId">
        <el-select v-model="readerForm.datasourceId" filterable style="width: 300px" @change="rDsChange">
          <el-option
            v-for="item in rDsList"
            :key="item.id"
            :label="item.datasourceName"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item v-show="dataSource==='postgresql' || dataSource==='oracle' ||dataSource==='sqlserver'" label="Schema：" prop="tableSchema">
        <el-select v-model="readerForm.tableSchema" allow-create default-first-option filterable style="width: 300px" @change="schemaChange">
          <el-option
            v-for="item in schemaList"
            :key="item"
            :label="item"
            :value="item"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="数据库表名：" prop="tableName">
        <el-select v-model="readerForm.tableName" allow-create default-first-option filterable style="width: 300px" @change="rTbChange">
          <el-option v-for="item in rTbList" :key="item" :label="item" :value="item" />
        </el-select>
      </el-form-item>
      <el-form-item label="SQL语句：">
        <el-input v-model="readerForm.querySql" :autosize="{ minRows: 3, maxRows: 20}" type="textarea" placeholder="sql查询，一般用于多表关联查询时才用" style="width: 42%" />
        <el-button type="primary" @click.prevent="getColumns('reader')">解析字段</el-button>
      </el-form-item>
      <el-form-item label="切分字段：">
        <el-input v-model="readerForm.splitPk" placeholder="切分主键" style="width: 13%" />
      </el-form-item>
      <el-form-item label="表所有字段：">
        <el-checkbox
          v-model="readerForm.checkAll"
          :indeterminate="readerForm.isIndeterminate"
          @change="rHandleCheckAllChange"
        >全选
        </el-checkbox>
        <div style="margin: 15px 0;" />
        <el-checkbox-group v-model="readerForm.columns" @change="rHandleCheckedChange">
          <el-checkbox v-for="c in rColumnList" :key="c" :label="c">{{ c }}</el-checkbox>
        </el-checkbox-group>
      </el-form-item>
      <el-form-item label="where条件：" prop="where">
        <el-input v-model="readerForm.where" placeholder="where条件，不需要再加where" type="textarea" style="width: 42%" />
      </el-form-item>
      <el-form-item v-if="taskType === 1" label="slotName：">
        <el-input v-model="localSlotName" placeholder="请输入 slotName" style="width: 300px" @input="updateSlotName" />
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import * as dsQueryApi from '@/api/metadata-query'
import { list as jdbcDsList } from '@/api/datax-jdbcDatasource'

export default {
  name: 'RDBMSReader',
  props: {
    taskType: {
      type: Number,
      default: 0
    },
    slotName: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      jdbcDsQuery: {
        current: 1,
        size: 200,
        ascs: 'datasource_name'
      },
      rDsList: [],
      rTbList: [],
      schemaList: [],
      rColumnList: [],
      loading: false,
      active: 1,
      customFields: '',
      customType: '',
      customValue: '',
      dataSource: '',
      localSlotName: '',
      readerForm: {
        datasourceId: undefined,
        tableName: '',
        columns: [],
        where: '',
        querySql: '',
        checkAll: false,
        isIndeterminate: true,
        splitPk: '',
        tableSchema: ''
      },
      rules: {
        datasourceId: [{ required: true, message: 'this is required', trigger: 'change' }],
        tableName: [{ required: true, message: 'this is required', trigger: 'change' }],
        tableSchema: [{ required: true, message: 'this is required', trigger: 'change' }]
      }
    }
  },
  watch: {
    slotName: {
      immediate: true,
      handler(newVal) {
        this.localSlotName = newVal
      }
    }
  },
  watch: {
    'readerForm.datasourceId': function(newVal, oldVal) {
      // 如果数据源ID改变，且 dataSource 已设置，则判断是否需要获取 schema
      if (newVal && newVal !== oldVal && this.dataSource) {
        if (this.dataSource === 'postgresql' || this.dataSource === 'oracle' || this.dataSource === 'sqlserver') {
          // 需要 schema 的数据库，先获取 schema（如果还没有获取过）
          if (this.schemaList.length === 0) {
            this.getSchema()
          }
        } else {
          // 不需要 schema 的数据库，直接获取表
          this.getTables('rdbmsReader')
        }
      }
    }
  },
  created() {
    this.getJdbcDs()
  },
  methods: {
    // 获取可用数据源
    getJdbcDs(type) {
      this.loading = true
      jdbcDsList(this.jdbcDsQuery).then(response => {
        const { records } = response
        this.rDsList = records
        this.loading = false
      })
    },
    // 获取表名
    getTables(type) {
      if (type === 'rdbmsReader') {
        let obj = {}
        if (this.dataSource === 'postgresql' || this.dataSource === 'oracle' || this.dataSource === 'sqlserver') {
          obj = {
            datasourceId: this.readerForm.datasourceId,
            tableSchema: this.readerForm.tableSchema
          }
        } else {
          obj = {
            datasourceId: this.readerForm.datasourceId
          }
        }
        // 组装
        dsQueryApi.getTables(obj).then(response => {
          if (response) {
            this.rTbList = response
          }
        })
      }
    },
    getSchema() {
      const obj = {
        datasourceId: this.readerForm.datasourceId
      }
      dsQueryApi.getTableSchema(obj).then(response => {
        this.schemaList = response
      })
    },
    // schema 切换
    schemaChange(e) {
      this.readerForm.tableSchema = e
      // 获取可用表
      this.getTables('rdbmsReader')
    },
    // reader 数据源切换
    rDsChange(e) {
      // 清空
      this.readerForm.tableName = ''
      this.readerForm.tableSchema = ''
      this.readerForm.datasourceId = e
      this.rTbList = []
      this.schemaList = []
      this.rDsList.find((item) => {
        if (item.id === e) {
          this.dataSource = item.datasource
        }
      })
      // 移除 Bus.dataSourceId 的设置，不再自动传递数据源ID
      this.$emit('selectDataSource', this.dataSource)
      // 判断是否需要先获取 schema
      if (this.dataSource === 'postgresql' || this.dataSource === 'oracle' || this.dataSource === 'sqlserver') {
        // 需要 schema 的数据库，先获取 schema
        this.getSchema()
      } else {
        // 不需要 schema 的数据库，直接获取表
        this.getTables('rdbmsReader')
      }
    },
    getTableColumns() {
      const obj = {
        datasourceId: this.readerForm.datasourceId,
        tableName: this.readerForm.tableName
      }
      dsQueryApi.getColumns(obj).then(response => {
        this.rColumnList = response
        this.readerForm.columns = response
        this.readerForm.checkAll = true
        this.readerForm.isIndeterminate = false
      })
    },
    getColumnsByQuerySql() {
      const obj = {
        datasourceId: this.readerForm.datasourceId,
        querySql: this.readerForm.querySql
      }
      dsQueryApi.getColumnsByQuerySql(obj).then(response => {
        this.rColumnList = response
        this.readerForm.columns = response
        this.readerForm.checkAll = true
        this.readerForm.isIndeterminate = false
      })
    },
    // 获取表字段
    getColumns(type) {
      if (type === 'reader') {
        if (this.readerForm.querySql !== '') {
          this.getColumnsByQuerySql()
        } else {
          this.getTableColumns()
        }
      }
    },
    // 表切换
    rTbChange(t) {
      this.readerForm.tableName = t
      this.rColumnList = []
      this.readerForm.columns = []
      this.getColumns('reader')
    },
    rHandleCheckAllChange(val) {
      this.readerForm.columns = val ? this.rColumnList : []
      this.readerForm.isIndeterminate = false
    },
    rHandleCheckedChange(value) {
      const checkedCount = value.length
      this.readerForm.checkAll = checkedCount === this.rColumnList.length
      this.readerForm.isIndeterminate = checkedCount > 0 && checkedCount < this.rColumnList.length
    },
    updateSlotName(value) {
      this.localSlotName = value
      this.$emit('update:slotName', value)
    },
    getData() {
      // 移除自动选择数据源的逻辑，必须手动选择
      const data = { ...this.readerForm }
      if (this.taskType === 1) {
        data.slotName = this.localSlotName
      }
      return data
    }
  }
}
</script>

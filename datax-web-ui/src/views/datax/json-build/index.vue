<template>
  <div class="app-container">
    <div class="build-container">
      <el-steps :active="active" finish-status="success">
        <el-step title="步骤 1" description="构建reader">1</el-step>
        <el-step title="步骤 2" description="构建writer">2</el-step>
        <el-step title="字段映射" description="选择字段并建立映射关系">3</el-step>
        <el-step title="步骤 4" description="构建">4</el-step>
      </el-steps>

      <div v-show="active===1" class="step1">
        <Reader ref="reader" />
      </div>
      <div v-show="active===2" class="step2">
        <Writer ref="writer" />
      </div>
      <div v-show="active===3" class="step3">
        <Mapper ref="mapper" />
      </div>
      <div v-show="active===4" class="step4">
        <el-button type="primary" @click="buildJson">1.构建</el-button>
        <el-button type="primary" @click="handleGoToJobInfo" :disabled="!configJson">2.添加任务</el-button>
        <el-button type="info" @click="handleCopy(inputData,$event)">复制json</el-button>
        (步骤：构建->添加任务)
        <div style="margin-bottom: 20px;" />
        <json-editor v-show="active===4" ref="jsonEditor" v-model="configJson" />
      </div>

      <el-button :disabled="active===1" style="margin-top: 12px;" @click="last">上一步</el-button>
      <el-button type="primary" style="margin-top: 12px;margin-bottom: 12px;" @click="next">下一步</el-button>
    </div>

    <!-- 创建任务对话框 -->
    <el-dialog title="添加任务" :visible.sync="dialogFormVisible" width="1000px" :before-close="handleClose">
      <el-form ref="dataForm" :rules="rules" :model="temp" label-position="left" label-width="110px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="执行器" prop="jobGroup">
              <el-select v-model="temp.jobGroup" placeholder="请选择执行器">
                <el-option v-for="item in executorList" :key="item.id" :label="item.title" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="任务名称" prop="jobDesc">
              <el-input v-model="temp.jobDesc" size="medium" placeholder="请输入任务描述" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="路由策略" prop="executorRouteStrategy">
              <el-select v-model="temp.executorRouteStrategy" placeholder="请选择路由策略">
                <el-option v-for="item in routeStrategies" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-dialog
              title="提示"
              :visible.sync="showCronBox"
              width="60%"
              append-to-body
            >
              <cron v-model="temp.jobCron" />
              <span slot="footer" class="dialog-footer">
                <el-button @click="showCronBox = false;">关闭</el-button>
                <el-button type="primary" @click="showCronBox = false">确 定</el-button>
              </span>
            </el-dialog>
            <el-form-item label="Cron" prop="jobCron">
              <el-input v-model="temp.jobCron" auto-complete="off" placeholder="请输入Cron表达式">
                <el-button v-if="!showCronBox" slot="append" icon="el-icon-turn-off" title="打开图形配置" @click="showCronBox = true" />
                <el-button v-else slot="append" icon="el-icon-open" title="关闭图形配置" @click="showCronBox = false" />
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="阻塞处理" prop="executorBlockStrategy">
              <el-select v-model="temp.executorBlockStrategy" placeholder="请选择阻塞处理策略">
                <el-option v-for="item in blockStrategies" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="报警邮件">
              <el-input v-model="temp.alarmEmail" placeholder="请输入报警邮件，多个用逗号分隔" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="任务类型" prop="glueType">
              <el-select v-model="temp.glueType" placeholder="任务脚本类型" disabled>
                <el-option v-for="item in glueTypes" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="失败重试次数">
              <el-input-number v-model="temp.executorFailRetryCount" :min="0" :max="20" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属项目" prop="projectId">
              <el-select v-model="temp.projectId" placeholder="所属项目" class="filter-item">
                <el-option v-for="item in jobProjectList" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="超时时间(分钟)">
              <el-input-number v-model="temp.executorTimeout" :min="0" :max="120" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="子任务">
              <el-select v-model="temp.childJobId" multiple placeholder="子任务" value-key="id">
                <el-option v-for="item in jobIdList" :key="item.id" :label="item.jobDesc" :value="item" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12" />
        </el-row>
        <el-row v-if="temp.glueType==='BEAN'" :gutter="20">
          <el-col :span="12">
            <el-form-item label="辅助参数" prop="incrementType">
              <el-select v-model="temp.incrementType" placeholder="请选择参数类型" value="">
                <el-option v-for="item in incrementTypes" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row v-if="temp.glueType==='BEAN' && temp.incrementType === 1" :gutter="20">
          <el-col :span="12">
            <el-form-item label="增量主键开始ID" prop="incStartId">
              <el-input v-model="temp.incStartId" placeholder="首次增量使用" style="width: 56%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="ID增量参数" prop="replaceParam">
              <el-input v-model="temp.replaceParam" placeholder="-DstartId='%s' -DendId='%s'" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="reader数据源" prop="datasourceId">
              <el-select v-model="temp.datasourceId" placeholder="reader数据源" class="filter-item">
                <el-option v-for="item in dataSourceList" :key="item.id" :label="item.datasourceName" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="7">
            <el-form-item label="reader表" prop="readerTable">
              <el-input v-model="temp.readerTable" placeholder="读表的表名" />
            </el-form-item>
          </el-col>
          <el-col :span="5">
            <el-form-item label="主键" label-width="40px" prop="primaryKey">
              <el-input v-model="temp.primaryKey" placeholder="请填写主键字段名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row v-if="temp.glueType==='BEAN' && temp.incrementType === 2" :gutter="20">
          <el-col :span="12">
            <el-form-item label="增量开始时间" prop="incStartTime">
              <el-date-picker
                v-model="temp.incStartTime"
                type="datetime"
                placeholder="首次增量使用"
                format="yyyy-MM-dd HH:mm:ss"
                style="width: 57%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="增量时间字段" prop="replaceParam">
              <el-input v-model="temp.replaceParam" placeholder="-DlastTime='%s' -DcurrentTime='%s'" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="增量时间格式" prop="replaceParamType">
              <el-select v-model="temp.replaceParamType" placeholder="增量时间格式" @change="incStartTimeFormat">
                <el-option v-for="item in replaceFormatTypes" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row v-if="temp.glueType==='BEAN' && temp.incrementType === 3" :gutter="20">
          <el-col :span="12">
            <el-form-item label="分区字段" prop="partitionField">
              <el-input v-model="partitionField" placeholder="请输入分区字段" style="width: 56%" />
            </el-form-item>
          </el-col>
          <el-col :span="7">
            <el-form-item label="分区时间">
              <el-select v-model="timeFormatType" placeholder="分区时间格式">
                <el-option v-for="item in timeFormatTypes" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="5">
            <el-input-number v-model="timeOffset" :min="-20" :max="0" style="width: 65%" />
          </el-col>
        </el-row>
        <el-row v-if="temp.glueType==='BEAN'" :gutter="20">
          <el-col :span="24">
            <el-form-item label="JVM启动参数">
              <el-input v-model="temp.jvmParam" placeholder="-Xms1024m -Xmx1024m -XX:+HeapDumpOnOutOfMemoryError" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <json-editor v-if="temp.glueType==='BEAN'" ref="createJobJsonEditor" v-model="jobJson" />
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false">
          取消
        </el-button>
        <el-button type="primary" @click="createData">
          确定
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import * as dataxJsonApi from '@/api/datax-json'
import * as jobTemplate from '@/api/datax-job-template'
import * as job from '@/api/datax-job-info'
import * as datasourceApi from '@/api/datax-jdbcDatasource'
import * as jobProjectApi from '@/api/datax-job-project'
import Pagination from '@/components/Pagination'
import JsonEditor from '@/components/JsonEditor'
import Cron from '@/components/Cron'
import Reader from './reader'
import Writer from './writer'
import clip from '@/utils/clipboard'
import Mapper from './mapper'
import { isJSON } from '@/utils/validate'

export default {
  name: 'JsonBuild',
  components: { Reader, Writer, Pagination, JsonEditor, Mapper, Cron },
  data() {
    const validateIncParam = (rule, value, callback) => {
      if (!value) {
        callback(new Error('Increment parameters is required'))
      }
      callback()
    }
    const validatePartitionParam = (rule, value, callback) => {
      if (!this.partitionField) {
        callback(new Error('Partition parameters is required'))
      }
      callback()
    }
    return {
      configJson: '',
      active: 1,
      jobTemplate: '',
      jobTemplateSelectDrawer: false,
      list: null,
      currentRow: null,
      listLoading: true,
      total: 0,
      listQuery: {
        current: 1,
        size: 10,
        jobGroup: 0,
        triggerStatus: -1,
        jobDesc: '',
        executorHandler: '',
        userId: 0
      },
      blockStrategies: [
        { value: 'SERIAL_EXECUTION', label: '单机串行' },
        { value: 'DISCARD_LATER', label: '丢弃后续调度' },
        { value: 'COVER_EARLY', label: '覆盖之前调度' }
      ],
      routeStrategies: [
        { value: 'FIRST', label: '第一个' },
        { value: 'LAST', label: '最后一个' },
        { value: 'ROUND', label: '轮询' },
        { value: 'RANDOM', label: '随机' },
        { value: 'CONSISTENT_HASH', label: '一致性HASH' },
        { value: 'LEAST_FREQUENTLY_USED', label: '最不经常使用' },
        { value: 'LEAST_RECENTLY_USED', label: '最近最久未使用' },
        { value: 'FAILOVER', label: '故障转移' },
        { value: 'BUSYOVER', label: '忙碌转移' }
        // { value: 'SHARDING_BROADCAST', label: '分片广播' }
      ],
      triggerNextTimes: '',
      registerNode: [],
      jobJson: '',
      dialogFormVisible: false,
      showCronBox: false,
      executorList: [],
      jobIdList: [],
      jobProjectList: [],
      dataSourceList: [],
      glueTypes: [
        { value: 'BEAN', label: 'DataX任务' },
        { value: 'GLUE_SHELL', label: 'Shell任务' },
        { value: 'GLUE_PYTHON', label: 'Python任务' },
        { value: 'GLUE_POWERSHELL', label: 'PowerShell任务' }
      ],
      incrementTypes: [
        { value: 0, label: '无' },
        { value: 1, label: '主键自增' },
        { value: 2, label: '时间自增' },
        { value: 3, label: 'HIVE分区' }
      ],
      timeOffset: 0,
      timeFormatType: 'yyyy-MM-dd',
      partitionField: '',
      timeFormatTypes: [
        { value: 'yyyy-MM-dd', label: 'yyyy-MM-dd' },
        { value: 'yyyyMMdd', label: 'yyyyMMdd' },
        { value: 'yyyy/MM/dd', label: 'yyyy/MM/dd' }
      ],
      replaceFormatTypes: [
        { value: 'yyyy/MM/dd', label: 'yyyy/MM/dd' },
        { value: 'yyyy-MM-dd', label: 'yyyy-MM-dd' },
        { value: 'HH:mm:ss', label: 'HH:mm:ss' },
        { value: 'yyyy/MM/dd HH:mm:ss', label: 'yyyy/MM/dd HH:mm:ss' },
        { value: 'yyyy-MM-dd HH:mm:ss', label: 'yyyy-MM-dd HH:mm:ss' },
        { value: 'Timestamp', label: '时间戳' }
      ],
      rules: {
        jobGroup: [{ required: true, message: 'jobGroup is required', trigger: 'change' }],
        executorRouteStrategy: [{ required: true, message: 'executorRouteStrategy is required', trigger: 'change' }],
        executorBlockStrategy: [{ required: true, message: 'executorBlockStrategy is required', trigger: 'change' }],
        glueType: [{ required: true, message: 'jobType is required', trigger: 'change' }],
        projectId: [{ required: true, message: 'projectId is required', trigger: 'change' }],
        jobDesc: [{ required: true, message: 'jobDesc is required', trigger: 'blur' }],
        jobCron: [{ required: true, message: 'jobCron is required', trigger: 'blur' }],
        incStartId: [{ trigger: 'blur', validator: validateIncParam }],
        replaceParam: [{ trigger: 'blur', validator: validateIncParam }],
        primaryKey: [{ trigger: 'blur', validator: validateIncParam }],
        incStartTime: [{ trigger: 'change', validator: validateIncParam }],
        replaceParamType: [{ trigger: 'change', validator: validateIncParam }],
        partitionField: [{ trigger: 'blur', validator: validatePartitionParam }],
        datasourceId: [{ trigger: 'change', validator: validateIncParam }],
        readerTable: [{ trigger: 'blur', validator: validateIncParam }]
      },
      temp: {
        id: undefined,
        jobGroup: '',
        jobCron: '',
        jobDesc: '',
        executorRouteStrategy: '',
        executorBlockStrategy: '',
        childJobId: '',
        executorFailRetryCount: '',
        alarmEmail: '',
        executorTimeout: '',
        userId: 0,
        jobConfigId: '',
        executorHandler: 'executorJobHandler',
        glueType: 'BEAN',
        glueSource: '',
        jobJson: '',
        executorParam: '',
        replaceParam: '',
        replaceParamType: 'Timestamp',
        jvmParam: '',
        incStartTime: '',
        partitionInfo: '',
        incrementType: 0,
        incStartId: '',
        primaryKey: '',
        projectId: '',
        datasourceId: '',
        readerTable: ''
      }
    }
  },
  created() {
    // this.getJdbcDs()
  },
  watch: {
    dialogFormVisible(val) {
      if (val) {
        this.getExecutor()
        this.getJobIdList()
        this.getJobProject()
        this.getDataSourceList()
      }
    }
  },
  methods: {
    next() {
      const fromColumnList = this.$refs.reader.getData().columns
      const toColumnsList = this.$refs.writer.getData().columns
      // const fromTableName = this.$refs.reader.getData().tableName
      // 第一步 reader 判断是否已选字段
      if (this.active === 1) {
        // 实现第一步骤读取的表和字段直接带到第二步骤
        // this.$refs.writer.sendTableNameAndColumns(fromTableName, fromColumnList)
        // 取子组件的数据
        // console.info(this.$refs.reader.getData())
        this.active++
      } else {
        // 将第一步和第二步得到的字段名字发送到第三步
        if (this.active === 2) {
          this.$refs.mapper.sendColumns(fromColumnList, toColumnsList)
        }
        if (this.active === 3) {
          const lColumns = this.$refs.mapper.getLColumns()
          const rColumns = this.$refs.mapper.getRColumns()
          if (lColumns.length === 0 || rColumns.length === 0) {
            this.$message({
              message: '请映射源表和目标表的字段',
              type: 'warning'
            })
            return
          }
        }
        if (this.active === 4) {
          this.temp.jobJson = this.configJson
          job.createJob(this.temp).then(() => {
            this.$notify({
              title: 'Success',
              message: 'Created Successfully',
              type: 'success',
              duration: 2000
            })
            // 切回第一步
            this.active = 1
          })
        } else {
          this.active++
        }
      }
    },
    last() {
      if (this.active > 1) {
        this.active--
      }
    },
    // 构建json
    buildJson() {
      const readerData = this.$refs.reader.getData()
      const writeData = this.$refs.writer.getData()
      const readerColumns = this.$refs.mapper.getLColumns()
      const writerColumns = this.$refs.mapper.getRColumns()
      const hiveReader = {
        readerPath: readerData.path,
        readerDefaultFS: readerData.defaultFS,
        readerFileType: readerData.fileType,
        readerFieldDelimiter: readerData.fieldDelimiter,
        readerSkipHeader: readerData.skipHeader
      }
      const hiveWriter = {
        writerDefaultFS: writeData.defaultFS,
        writerFileType: writeData.fileType,
        writerPath: writeData.path,
        writerFileName: writeData.fileName,
        writeMode: writeData.writeMode,
        writeFieldDelimiter: writeData.fieldDelimiter
      }
      const hbaseReader = {
        readerMode: readerData.mode,
        readerMaxVersion: readerData.maxVersion,
        readerRange: readerData.range
      }
      const hbaseWriter = {
        writerMode: writeData.mode,
        writerRowkeyColumn: writeData.rowkeyColumn,
        writerVersionColumn: writeData.versionColumn,
        writeNullMode: writeData.nullMode
      }
      const mongoDBReader = {}
      const mongoDBWriter = {
        upsertInfo: writeData.upsertInfo
      }
      const rdbmsReader = {
        readerSplitPk: readerData.splitPk,
        whereParams: readerData.where,
        querySql: readerData.querySql
      }
      const rdbmsWriter = {
        preSql: writeData.preSql,
        postSql: writeData.postSql
      }
      const obj = {
        readerDatasourceId: readerData.datasourceId,
        readerTables: [readerData.tableName],
        readerColumns: readerColumns,
        writerDatasourceId: writeData.datasourceId,
        writerTables: [writeData.tableName],
        writerColumns: writerColumns,
        hiveReader: hiveReader,
        hiveWriter: hiveWriter,
        rdbmsReader: rdbmsReader,
        rdbmsWriter: rdbmsWriter,
        hbaseReader: hbaseReader,
        hbaseWriter: hbaseWriter,
        mongoDBReader: mongoDBReader,
        mongoDBWriter: mongoDBWriter
      }
      // 调api
      dataxJsonApi.buildJobJson(obj).then(response => {
        this.configJson = JSON.parse(response)
      })
    },
    handleCopy(text, event) {
      clip(this.configJson, event)
      this.$message({
        message: 'copy success',
        type: 'success'
      })
    },
    handleJobTemplateSelectDrawer() {
      this.jobTemplateSelectDrawer = !this.jobTemplateSelectDrawer
      if (this.jobTemplateSelectDrawer) {
        this.fetchData()
        this.getExecutor()
      }
    },
    getReaderData() {
      return this.$refs.reader.getData()
    },
    getExecutor() {
      jobTemplate.getExecutorList().then(response => {
        const { content } = response
        this.executorList = content
      })
    },
    fetchData() {
      this.listLoading = true
      jobTemplate.getList(this.listQuery).then(response => {
        const { content } = response
        this.total = content.recordsTotal
        this.list = content.data
        this.listLoading = false
      })
    },
    handleCurrentChange(val) {
      this.temp = Object.assign({}, val)
      this.temp.id = undefined
      this.temp.jobDesc = this.getReaderData().tableName
      this.$refs.jobTemplateSelectDrawer.closeDrawer()
      this.jobTemplate = val.id + '(' + val.jobDesc + ')'
    },
    handleGoToJobInfo() {
      if (!this.configJson) {
        this.$message({
          message: '请先构建JSON',
          type: 'warning'
        })
        return
      }
      // 重置表单
      this.resetTemp()
      // 填充 JSON
      this.jobJson = typeof this.configJson === 'string' ? this.configJson : JSON.stringify(this.configJson, null, 2)
      // 打开对话框
      this.dialogFormVisible = true
      this.$nextTick(() => {
        this.$refs['dataForm'].clearValidate()
      })
    },
    resetTemp() {
      this.temp = {
        id: undefined,
        jobGroup: '',
        jobCron: '',
        jobDesc: '',
        executorRouteStrategy: '',
        executorBlockStrategy: '',
        childJobId: '',
        executorFailRetryCount: '',
        alarmEmail: '',
        executorTimeout: '',
        userId: 0,
        jobConfigId: '',
        executorHandler: 'executorJobHandler',
        glueType: 'BEAN',
        glueSource: '',
        jobJson: '',
        executorParam: '',
        replaceParam: '',
        replaceParamType: 'Timestamp',
        jvmParam: '',
        incStartTime: '',
        partitionInfo: '',
        incrementType: 0,
        incStartId: '',
        primaryKey: '',
        projectId: '',
        datasourceId: '',
        readerTable: ''
      }
      this.jobJson = ''
      this.timeOffset = 0
      this.timeFormatType = 'yyyy-MM-dd'
      this.partitionField = ''
    },
    handleClose(done) {
      this.$confirm('确认关闭？')
        .then(_ => {
          done()
        })
        .catch(_ => {})
    },
    getExecutor() {
      job.getExecutorList().then(response => {
        const { content } = response
        this.executorList = content
      })
    },
    getJobIdList() {
      job.getJobIdList().then(response => {
        const { content } = response
        this.jobIdList = content
      })
    },
    getJobProject() {
      jobProjectApi.getJobProjectList().then(response => {
        this.jobProjectList = response
      })
    },
    getDataSourceList() {
      datasourceApi.getDataSourceList().then(response => {
        this.dataSourceList = response
      })
    },
    createData() {
      if (this.temp.glueType === 'BEAN' && !isJSON(this.jobJson)) {
        this.$notify({
          title: 'Fail',
          message: 'json格式错误',
          type: 'error',
          duration: 2000
        })
        return
      }
      this.$refs['dataForm'].validate((valid) => {
        if (valid) {
          if (this.temp.childJobId) {
            const auth = []
            for (const i in this.temp.childJobId) {
              auth.push(this.temp.childJobId[i].id)
            }
            this.temp.childJobId = auth.toString()
          }
          this.temp.jobJson = this.jobJson
          this.temp.glueSource = ''
          this.temp.executorHandler = this.temp.glueType === 'BEAN' ? 'executorJobHandler' : ''
          if (this.partitionField) this.temp.partitionInfo = this.partitionField + ',' + this.timeOffset + ',' + this.timeFormatType
          job.createJob(this.temp).then(() => {
            this.dialogFormVisible = false
            this.$notify({
              title: 'Success',
              message: 'Created Successfully',
              type: 'success',
              duration: 2000
            })
          })
        }
      })
    },
    incStartTimeFormat(vData) {
    }
  }
}
</script>

<style lang="scss" scoped>

</style>

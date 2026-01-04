<template>
  <div class="app-container">
    <el-form label-position="right" label-width="120px" style="margin-bottom: 20px;">
      <el-form-item label="任务类型：">
        <el-radio-group v-model="taskType">
          <el-radio :label="0">全量同步</el-radio>
          <el-radio :label="1">增量同步</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <RDBMSReader v-show="dataSource!=='hive' && dataSource!=='hbase' && dataSource!=='mongodb'" ref="rdbmsreader" :task-type="taskType" :slot-name.sync="slotName" @selectDataSource="showDataSource" />
    <HiveReader v-show="dataSource==='hive'" ref="hivereader" @selectDataSource="showDataSource" />
    <HBaseReader v-show="dataSource==='hbase'" ref="hbasereader" @selectDataSource="showDataSource" />
    <MongoDBReader v-show="dataSource==='mongodb'" ref="mongodbreader" @selectDataSource="showDataSource" />
  </div>
</template>

<script>
import RDBMSReader from './reader/RDBMSReader'
import HiveReader from './reader/HiveReader'
import HBaseReader from './reader/HBaseReader'
import MongoDBReader from './reader/MongoDBReader'
export default {
  name: 'Reader',
  components: { RDBMSReader, HiveReader, HBaseReader, MongoDBReader },
  data() {
    return {
      dataSource: '',
      taskType: 0, // 默认全量同步
      slotName: '' // slotName 输入框值
    }
  },
  methods: {
    getData() {
      let data = {}
      if (this.dataSource === 'hive') {
        data = this.$refs.hivereader.getData()
      } else if (this.dataSource === 'hbase') {
        data = this.$refs.hbasereader.getData()
      } else if (this.dataSource === 'mongodb') {
        data = this.$refs.mongodbreader.getData()
      } else {
        data = this.$refs.rdbmsreader ? this.$refs.rdbmsreader.getData() : {}
      }
      // 添加任务类型
      if (data) {
        data.type = this.taskType
        // slotName 已经在 RDBMSReader 的 getData() 中返回了
        if (this.taskType === 1 && !data.slotName) {
          data.slotName = this.slotName
        }
      }
      return data
    },
    showDataSource(data) {
      this.dataSource = data
      this.getData()
    },
    getTaskType() {
      return this.taskType
    }
  }
}
</script>

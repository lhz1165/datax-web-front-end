<template>
  <div class="app-container">
    <el-page-header @back="goBack">
      <template slot="content">
        <span>创建视图</span>
        <el-tag size="small" style="margin-left: 10px;">{{ datasourceName }}</el-tag>
        <el-tag v-if="schema" size="small" type="info" style="margin-left: 5px;">{{ schema }}</el-tag>
      </template>
    </el-page-header>

    <el-card class="form-card">
      <div slot="header">
        <span>视图 SQL</span>
      </div>
      <el-form label-width="80px">
        <el-form-item label="SQL语句">
          <el-input
            v-model="viewSql"
            type="textarea"
            :rows="10"
            placeholder="请输入创建视图的 SQL，例如：CREATE VIEW view_name AS SELECT ... "
          />
        </el-form-item>
      </el-form>
    </el-card>

    <div class="footer-actions">
      <el-button @click="goBack">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleCreateView">创建视图</el-button>
    </div>
  </div>
</template>

<script>
import { createView } from '@/api/metadata-query'

export default {
  name: 'CreateView',
  data() {
    return {
      datasourceId: null,
      datasourceName: '',
      datasourceType: '',
      schema: '',
      viewSql: '',
      submitting: false
    }
  },
  created() {
    this.initFromRoute()
  },
  methods: {
    initFromRoute() {
      this.datasourceId = Number(this.$route.query.datasourceId)
      this.datasourceName = this.$route.query.datasourceName || '未知数据源'
      this.datasourceType = this.$route.query.datasourceType || ''
      this.schema = this.$route.query.schema || ''
    },
    goBack() {
      this.$router.back()
    },
    handleCreateView() {
      if (!this.viewSql || !this.viewSql.toLowerCase().includes('create view')) {
        this.$message.warning('请输入包含 CREATE VIEW 的视图 SQL')
        return
      }
      this.submitting = true
      createView({
        datasourceId: this.datasourceId,
        viewSql: this.viewSql
      }).then(() => {
        this.submitting = false
        this.$message.success('视图创建成功')
        this.$router.push({
          path: '/datax/integration/database',
          query: {
            datasourceId: this.datasourceId,
            datasourceName: this.datasourceName,
            datasourceType: this.datasourceType,
            schema: this.schema,
            refresh: Date.now()
          }
        })
      }).catch(err => {
        this.submitting = false
        this.$message.error('创建视图失败: ' + (err.message || '未知错误'))
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

.footer-actions {
  margin-top: 20px;
  text-align: center;
  padding: 20px 0;
}
</style>



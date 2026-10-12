<template>
  <div class="page-wrap">
    <div class="page-head">
      <div>
        <h2>门店管理</h2>
        <p>以门店 ID 为核心查看配送进度，并向配送中心发起催单。</p>
      </div>
      <el-button type="primary" icon="el-icon-plus" @click="openStore()">新增门店</el-button>
    </div>

    <div class="panel">
      <div class="toolbar">
        <el-input v-model="keyword" clearable placeholder="搜索门店 ID 或名称" style="width: 280px" @keyup.enter.native="load" />
        <el-button type="primary" @click="load">查询</el-button>
      </div>
      <el-table :data="rows" stripe v-loading="loading">
        <el-table-column prop="id" label="门店 ID" width="120" />
        <el-table-column prop="name" label="门店名称" min-width="180" />
        <el-table-column label="状态" width="90">
          <template slot-scope="scope"><el-tag :type="scope.row.status === 1 ? 'success' : 'info'">{{ scope.row.status === 1 ? '启用' : '停用' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="pendingCount" label="待接单" width="90" />
        <el-table-column prop="deliveringCount" label="配送中" width="90" />
        <el-table-column prop="deliveredCount" label="已送达" width="90" />
        <el-table-column label="催单" width="90">
          <template slot-scope="scope"><span :class="{ urgent: scope.row.urgedCount > 0 }">{{ scope.row.urgedCount || 0 }}</span></template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" @click="viewOrders(scope.row)">配送订单</el-button>
            <el-button type="text" @click="openStore(scope.row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination :total="total" :current-page="page" :page-size="pageSize" layout="total, prev, pager, next" @current-change="pageChange" />
    </div>

    <el-dialog :title="form.id ? '编辑门店' : '新增门店'" :visible.sync="dialogVisible" width="440px">
      <el-form :model="form" label-width="90px">
        <el-form-item v-if="form.id" label="门店 ID"><el-input :value="form.id" disabled /></el-form-item>
        <el-form-item label="门店名称"><el-input v-model="form.name" placeholder="例如：华东一号门店" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="enabled" active-text="启用" inactive-text="停用" /></el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></span>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'
import { addStore, getStores, updateStore } from '@/api/supplyChain'

@Component({ name: 'StoreManagement' })
export default class extends Vue {
  private rows: any[] = []
  private loading = false
  private keyword = ''
  private page = 1
  private pageSize = 10
  private total = 0
  private dialogVisible = false
  private form: any = { name: '', status: 1 }

  get enabled() { return this.form.status === 1 }
  set enabled(value: boolean) { this.form.status = value ? 1 : 0 }

  created() { this.load() }

  private async load() {
    this.loading = true
    try {
      const res: any = await getStores({ page: this.page, pageSize: this.pageSize, keyword: this.keyword || undefined })
      this.rows = res.data.data.records || []
      this.total = res.data.data.total || 0
    } finally { this.loading = false }
  }

  private pageChange(value: number) { this.page = value; this.load() }
  private openStore(row?: any) {
    this.form = row ? { id: row.id, name: row.name, status: row.status } : { name: '', status: 1 }
    this.dialogVisible = true
  }
  private async save() {
    if (!this.form.name.trim()) return this.$message.warning('请输入门店名称')
    const action = this.form.id ? updateStore : addStore
    const res: any = await action(this.form)
    if (res.data.code === 1) { this.$message.success('保存成功'); this.dialogVisible = false; this.load() }
  }
  private viewOrders(row: any) { this.$router.push({ path: '/delivery', query: { storeId: String(row.id) } }) }
}
</script>

<style lang="scss" scoped>
.page-wrap { padding: 28px; }
.page-head { display:flex; justify-content:space-between; align-items:center; margin-bottom:20px; h2{font-size:24px;color:#13233a;margin:0 0 8px} p{color:#7b8798;margin:0} }
.panel { background:#fff; border-radius:12px; padding:22px; box-shadow:0 4px 18px rgba(31,76,130,.06); }
.toolbar { display:flex; gap:10px; margin-bottom:18px; }
.el-pagination { margin-top:20px; text-align:right; }
.urgent { color:#f56c6c; font-weight:700; }
</style>

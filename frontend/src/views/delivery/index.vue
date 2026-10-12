<template>
  <div class="page-wrap">
    <div class="page-head">
      <div><h2>配送中心</h2><p>统一处理骑手接单、配送状态和门店催单。</p></div>
      <el-button type="primary" icon="el-icon-user" @click="courierDialog=true">配送员管理</el-button>
    </div>

    <div class="summary-grid">
      <div v-for="card in cards" :key="card.key" class="summary-card" :class="card.className">
        <span>{{ card.label }}</span><strong>{{ summary[card.key] || 0 }}</strong>
      </div>
    </div>

    <div class="panel">
      <div class="toolbar">
        <el-input v-model="filters.storeId" clearable placeholder="门店 ID" style="width:150px" />
        <el-input v-model="filters.deliveryNumber" clearable placeholder="配送单号" style="width:220px" />
        <el-select v-model="filters.status" clearable placeholder="配送状态" style="width:150px">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </div>
      <el-table :data="rows" stripe v-loading="loading" row-key="id">
        <el-table-column prop="deliveryNumber" label="配送单号" min-width="165" />
        <el-table-column prop="storeId" label="门店 ID" width="105" />
        <el-table-column prop="storeName" label="门店" min-width="140" />
        <el-table-column label="状态" width="100">
          <template slot-scope="scope"><el-tag :type="statusType(scope.row.status)">{{ statusLabel(scope.row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="配送员" min-width="130">
          <template slot-scope="scope">{{ scope.row.courierName || '待分配' }}</template>
        </el-table-column>
        <el-table-column prop="deliveryAddress" label="配送地址" min-width="210" show-overflow-tooltip />
        <el-table-column label="催单" width="90">
          <template slot-scope="scope"><span :class="{ urgent: scope.row.urgeCount > 0 }">{{ scope.row.urgeCount || 0 }} 次</span></template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="165" />
        <el-table-column label="操作" min-width="250" fixed="right">
          <template slot-scope="scope">
            <el-button v-if="scope.row.status===1" type="text" @click="openAssign(scope.row)">指派</el-button>
            <el-button v-if="scope.row.status===2" type="text" @click="changeStatus(scope.row,3)">开始配送</el-button>
            <el-button v-if="scope.row.status===3" type="text" @click="changeStatus(scope.row,4)">确认送达</el-button>
            <el-button v-if="scope.row.status<4" type="text" class="danger" :loading="urgingId===scope.row.id" :disabled="urgingId!==null" @click="urge(scope.row)">催单</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination :total="total" :current-page="page" :page-size="pageSize" layout="total, prev, pager, next" @current-change="pageChange" />
    </div>

    <el-dialog title="指派配送员" :visible.sync="assignDialog" width="420px">
      <el-select v-model="selectedCourier" placeholder="选择在线配送员" style="width:100%">
        <el-option v-for="item in onlineCouriers" :key="item.id" :label="`${item.name} · ${item.phone}`" :value="item.id" />
      </el-select>
      <span slot="footer"><el-button @click="assignDialog=false">取消</el-button><el-button type="primary" @click="confirmAssign">确认指派</el-button></span>
    </el-dialog>

    <el-dialog title="配送员管理" :visible.sync="courierDialog" width="720px">
      <div class="courier-head"><el-button type="primary" size="small" @click="openCourier()">新增配送员</el-button></div>
      <el-table :data="couriers" size="small">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column label="在线状态" width="100">
          <template slot-scope="scope"><el-switch :value="scope.row.onlineStatus===1" @change="toggleCourier(scope.row)" /></template>
        </el-table-column>
        <el-table-column label="操作" width="80"><template slot-scope="scope"><el-button type="text" @click="openCourier(scope.row)">编辑</el-button></template></el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog :title="courierForm.id ? '编辑配送员' : '新增配送员'" :visible.sync="courierFormDialog" width="420px" append-to-body>
      <el-form label-width="80px">
        <el-form-item label="姓名"><el-input v-model="courierForm.name" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="courierForm.phone" /></el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="courierFormDialog=false">取消</el-button><el-button type="primary" @click="saveCourier">保存</el-button></span>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'
import { addCourier, assignCourier, getCouriers, getDashboardSummary, getDeliveryOrders, setCourierStatus, setDeliveryStatus, updateCourier, urgeDelivery } from '@/api/supplyChain'

const reminderAudio = require('@/assets/reminder.mp3')

@Component({ name: 'DeliveryCenter' })
export default class extends Vue {
  private summary: any = {}
  private rows: any[] = []
  private couriers: any[] = []
  private loading = false
  private page = 1
  private pageSize = 10
  private total = 0
  private urgingId: number | null = null
  private filters: any = { storeId: '', deliveryNumber: '', status: undefined }
  private assignDialog = false
  private courierDialog = false
  private courierFormDialog = false
  private selectedOrder: any = null
  private selectedCourier: any = null
  private courierForm: any = { name: '', phone: '', onlineStatus: 1 }
  private statusOptions = [
    { value: 1, label: '待接单' }, { value: 2, label: '已接单' }, { value: 3, label: '配送中' },
    { value: 4, label: '已送达' }, { value: 5, label: '已取消' }
  ]
  private cards = [
    { key: 'pendingCount', label: '待接单', className: 'blue' },
    { key: 'acceptedCount', label: '已接单', className: 'cyan' },
    { key: 'deliveringCount', label: '配送中', className: 'purple' },
    { key: 'deliveredCount', label: '已送达', className: 'green' },
    { key: 'urgedCount', label: '催单任务', className: 'red' }
  ]

  get onlineCouriers() { return this.couriers.filter(item => item.onlineStatus === 1) }

  created() {
    if (this.$route.query.storeId) this.filters.storeId = this.$route.query.storeId
    this.refresh()
  }

  private async refresh() { await Promise.all([this.load(), this.loadSummary(), this.loadCouriers()]) }
  private async load() {
    this.loading = true
    try {
      const res: any = await getDeliveryOrders({ page: this.page, pageSize: this.pageSize, storeId: this.filters.storeId || undefined, deliveryNumber: this.filters.deliveryNumber || undefined, status: this.filters.status })
      this.rows = res.data.data.records || []; this.total = res.data.data.total || 0
    } finally { this.loading = false }
  }
  private async loadSummary() { const res: any = await getDashboardSummary(); this.summary = res.data.data || {} }
  private async loadCouriers() { const res: any = await getCouriers(); this.couriers = res.data.data || [] }
  private search() { this.page = 1; this.load() }
  private reset() { this.filters = { storeId: '', deliveryNumber: '', status: undefined }; this.search() }
  private pageChange(value: number) { this.page = value; this.load() }
  private statusLabel(status: number) { const item = this.statusOptions.find(option => option.value === status); return item ? item.label : '未知' }
  private statusType(status: number) { return ({ 1: 'warning', 2: '', 3: 'primary', 4: 'success', 5: 'info' } as any)[status] }
  private openAssign(row: any) { this.selectedOrder = row; this.selectedCourier = null; this.assignDialog = true }
  private async confirmAssign() {
    if (!this.selectedCourier) return this.$message.warning('请选择配送员')
    const res: any = await assignCourier(this.selectedOrder.id, this.selectedCourier)
    if (res.data.code === 1) { this.$message.success('指派成功'); this.assignDialog = false; this.refresh() }
  }
  private async changeStatus(row: any, status: number) {
    const res: any = await setDeliveryStatus(row.id, status)
    if (res.data.code === 1) { this.$message.success('配送状态已更新'); this.refresh() }
  }
  private async urge(row: any) {
    this.urgingId = row.id
    try {
      const res: any = await urgeDelivery(row.id)
      if (res.data.code === 1) {
        this.$message.success(res.data.data || '催单成功')
        this.playUrgeVoice(row)
        await this.refresh()
      } else {
        this.$message.error(res.data.msg || '催单失败，请稍后重试')
      }
    } catch (error) {
      if (this.$route.path !== '/login') this.$message.error('催单失败，请检查登录状态或后端服务')
    } finally {
      this.urgingId = null
    }
  }
  private playUrgeVoice(row: any) {
    const message = `配送单${row.deliveryNumber || row.id}催单成功，请配送中心及时处理`
    const audio = new Audio(reminderAudio)
    audio.volume = 0.75
    audio.play().catch(() => undefined)

    if ('speechSynthesis' in window && 'SpeechSynthesisUtterance' in window) {
      window.speechSynthesis.cancel()
      const speech = new SpeechSynthesisUtterance(message)
      speech.lang = 'zh-CN'
      speech.rate = 0.95
      speech.pitch = 1
      speech.volume = 1
      window.setTimeout(() => window.speechSynthesis.speak(speech), 450)
    }
  }
  private openCourier(row?: any) { this.courierForm = row ? { ...row } : { name: '', phone: '', onlineStatus: 1 }; this.courierFormDialog = true }
  private async saveCourier() {
    if (!this.courierForm.name || !this.courierForm.phone) return this.$message.warning('请填写姓名和手机号')
    const action = this.courierForm.id ? updateCourier : addCourier
    const res: any = await action(this.courierForm)
    if (res.data.code === 1) { this.$message.success('保存成功'); this.courierFormDialog = false; this.loadCouriers() }
  }
  private async toggleCourier(row: any) { await setCourierStatus(row.id, row.onlineStatus === 1 ? 0 : 1); this.loadCouriers() }
}
</script>

<style lang="scss" scoped>
.page-wrap{padding:28px}.page-head{display:flex;justify-content:space-between;align-items:center;margin-bottom:20px;h2{font-size:24px;color:#13233a;margin:0 0 8px}p{color:#7b8798;margin:0}}
.summary-grid{display:grid;grid-template-columns:repeat(5,1fr);gap:16px;margin-bottom:20px}.summary-card{background:#fff;border-radius:12px;padding:18px 20px;border-top:3px solid #1677ff;box-shadow:0 4px 18px rgba(31,76,130,.06);span{color:#718096;font-size:14px}strong{display:block;font-size:30px;margin-top:8px;color:#172b4d}.cyan{border-color:#13c2c2}.purple{border-color:#722ed1}.green{border-color:#52c41a}.red{border-color:#f5222d}}
.panel{background:#fff;border-radius:12px;padding:22px;box-shadow:0 4px 18px rgba(31,76,130,.06)}.toolbar{display:flex;gap:10px;margin-bottom:18px}.el-pagination{margin-top:20px;text-align:right}.urgent,.danger{color:#f5222d!important;font-weight:700}.courier-head{margin-bottom:14px;text-align:right}
</style>

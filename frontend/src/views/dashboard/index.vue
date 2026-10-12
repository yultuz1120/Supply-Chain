<template>
  <div class="dashboard">
    <section class="hero">
      <div><span class="eyebrow">AI SUPPLY CHAIN</span><h1>AI 智慧供应链</h1><p>连接集团人员、门店、配送中心与智能客服，让配送进度可见、异常可追踪、知识可问答。</p></div>
      <div class="hero-mark">SC</div>
    </section>
    <section class="metric-grid">
      <div v-for="item in metrics" :key="item.key" class="metric-card">
        <div class="metric-icon" :style="{ background: item.color }"><i :class="item.icon" /></div>
        <div><span>{{ item.label }}</span><strong>{{ summary[item.key] || 0 }}</strong></div>
      </div>
    </section>
    <section class="content-grid">
      <div class="panel process-panel">
        <div class="panel-title"><h3>配送业务闭环</h3><span>DEMO FLOW</span></div>
        <div class="flow">
          <div class="flow-item"><b>01</b><h4>门店发起</h4><p>通过门店 ID 查看配送任务</p></div><i class="el-icon-right" />
          <div class="flow-item"><b>02</b><h4>配送中心</h4><p>接单并指派在线配送员</p></div><i class="el-icon-right" />
          <div class="flow-item"><b>03</b><h4>门店催单</h4><p>异常任务实时提醒并留痕</p></div><i class="el-icon-right" />
          <div class="flow-item"><b>04</b><h4>AI 协同</h4><p>RAG 问答与 MCP 工具调用</p></div>
        </div>
      </div>
      <div class="panel quick-panel">
        <div class="panel-title"><h3>快捷入口</h3></div>
        <div class="quick" @click="$router.push('/stores')"><i class="el-icon-office-building" /><span>查看门店</span></div>
        <div class="quick" @click="$router.push('/delivery')"><i class="el-icon-truck" /><span>进入配送中心</span></div>
        <div class="quick" @click="$router.push('/ai-assistant')"><i class="el-icon-chat-dot-round" /><span>询问智能客服</span></div>
      </div>
    </section>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'
import { getDashboardSummary } from '@/api/supplyChain'

@Component({ name: 'Dashboard' })
export default class extends Vue {
  private summary: any = {}
  private metrics = [
    { key: 'staffCount', label: '集团人员', icon: 'el-icon-user', color: 'linear-gradient(135deg,#1677ff,#69a8ff)' },
    { key: 'storeCount', label: '门店数量', icon: 'el-icon-office-building', color: 'linear-gradient(135deg,#13c2c2,#5cdbd3)' },
    { key: 'courierCount', label: '配送员', icon: 'el-icon-bicycle', color: 'linear-gradient(135deg,#722ed1,#b37feb)' },
    { key: 'deliveringCount', label: '配送中', icon: 'el-icon-truck', color: 'linear-gradient(135deg,#fa8c16,#ffc069)' },
    { key: 'urgedCount', label: '催单任务', icon: 'el-icon-bell', color: 'linear-gradient(135deg,#f5222d,#ff7875)' }
  ]
  async created() {
    try { const res: any = await getDashboardSummary(); this.summary = res.data.data || {} } catch (e) { this.summary = {} }
  }
}
</script>

<style lang="scss" scoped>
.dashboard{padding:28px}.hero{min-height:180px;padding:34px 42px;border-radius:18px;color:#fff;background:radial-gradient(circle at 85% 20%,rgba(45,212,191,.35),transparent 28%),linear-gradient(120deg,#071b33,#0b4f8a 58%,#1677ff);display:flex;justify-content:space-between;align-items:center;box-shadow:0 14px 34px rgba(16,76,145,.18)}.eyebrow{font-size:12px;letter-spacing:3px;opacity:.72}.hero h1{font-size:34px;margin:10px 0}.hero p{max-width:700px;line-height:1.8;opacity:.85}.hero-mark{width:104px;height:104px;border:1px solid rgba(255,255,255,.4);border-radius:30px;display:flex;align-items:center;justify-content:center;font-size:36px;font-weight:700;background:rgba(255,255,255,.1);transform:rotate(8deg)}
.metric-grid{display:grid;grid-template-columns:repeat(5,1fr);gap:16px;margin:22px 0}.metric-card{background:#fff;border-radius:14px;padding:20px;display:flex;align-items:center;gap:16px;box-shadow:0 4px 18px rgba(31,76,130,.06)}.metric-icon{width:48px;height:48px;border-radius:14px;color:#fff;display:flex;align-items:center;justify-content:center;font-size:21px}.metric-card span{font-size:13px;color:#8490a3}.metric-card strong{display:block;font-size:27px;color:#13233a;margin-top:5px}
.content-grid{display:grid;grid-template-columns:1fr 280px;gap:20px}.panel{background:#fff;border-radius:14px;padding:24px;box-shadow:0 4px 18px rgba(31,76,130,.06)}.panel-title{display:flex;align-items:center;justify-content:space-between;margin-bottom:22px}.panel-title h3{margin:0;color:#172b4d}.panel-title span{font-size:11px;letter-spacing:2px;color:#9aa5b5}.flow{display:flex;align-items:center;justify-content:space-between}.flow>i{color:#b9c6d8;font-size:22px}.flow-item{width:20%;b{display:inline-flex;width:34px;height:34px;border-radius:10px;background:#eaf3ff;color:#1677ff;align-items:center;justify-content:center}h4{margin:12px 0 7px;color:#263b59}p{font-size:12px;color:#8a96a7;line-height:1.5}}
.quick{display:flex;align-items:center;gap:12px;padding:14px;border-radius:10px;cursor:pointer;color:#41536d}.quick:hover{background:#edf5ff;color:#1677ff}.quick i{font-size:20px}
</style>

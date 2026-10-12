<template>
  <div class="ai-page">
    <aside class="ai-side">
      <div class="ai-badge">AI</div><h2>供应链智能客服</h2><p>DeepSeek · RAG · MCP</p>
      <div class="status-card"><span :class="['dot', configured ? 'ok' : 'warn']" /><div><b>{{ configured ? 'DeepSeek 已配置' : '等待 API Key' }}</b><small>{{ model }}</small></div></div>
      <button v-for="item in suggestions" :key="item" @click="useSuggestion(item)">{{ item }}</button>
      <div class="tip"><i class="el-icon-info" /> 配置 DEEPSEEK_API_KEY 后即可对话；知识库入库和检索可独立使用。</div>
    </aside>
    <main class="chat-main">
      <header><div><h3>智慧供应链助手</h3><span>知识问答与实时业务工具</span></div><el-button size="small" @click="clearChat">新会话</el-button></header>
      <section ref="messages" class="messages">
        <div v-if="messages.length===0" class="welcome"><div class="spark">✦</div><h2>你好，我是供应链智能客服</h2><p>可以询问农产品储运知识，也可以查询门店配送情况或催单。</p></div>
        <div v-for="(message,index) in messages" :key="index" :class="['message',message.role]">
          <div class="avatar">{{ message.role==='user' ? '我' : 'AI' }}</div>
          <div class="bubble"><div v-if="message.tool" class="tool"><i class="el-icon-connection" /> 已调用业务工具</div><pre>{{ message.content }}</pre></div>
        </div>
      </section>
      <footer class="composer">
        <el-input v-model="input" type="textarea" :rows="3" resize="none" placeholder="例如：查询门店1001的配送订单，或叶菜应该如何储存？" @keydown.native="onKeydown" />
        <div class="composer-foot"><span>Enter 发送 · Shift+Enter 换行</span><el-button type="primary" :loading="sending" icon="el-icon-s-promotion" @click="send">发送</el-button></div>
      </footer>
    </main>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'
import { getAiStatus } from '@/api/ai'
import { UserModule } from '@/store/modules/user'

@Component({ name: 'AiAssistant' })
export default class extends Vue {
  private configured = false
  private model = 'deepseek-flash'
  private input = ''
  private sending = false
  private conversationId = this.newId()
  private messages: any[] = []
  private suggestions = ['配送中心现在有多少待接单？', '查询门店 1001 的配送订单', '叶菜类农产品应该如何储存？', '冷链运输温度异常怎么处理？']

  async created() {
    try { const res: any = await getAiStatus(); this.configured = !!res.data.data.configured; this.model = res.data.data.model } catch (e) { this.configured = false }
  }
  private newId() { return `chat-${Date.now()}-${Math.random().toString(36).slice(2, 8)}` }
  private useSuggestion(text: string) { this.input = text; this.send() }
  private clearChat() { this.messages = []; this.conversationId = this.newId() }
  private onKeydown(event: KeyboardEvent) { if (event.key === 'Enter' && !event.shiftKey) { event.preventDefault(); this.send() } }
  private scrollBottom() { this.$nextTick(() => { const el: any = this.$refs.messages; if (el) el.scrollTop = el.scrollHeight }) }
  private decodeEventData(value: string) {
    try {
      const bytes = Uint8Array.from(atob(value), char => char.charCodeAt(0))
      return new TextDecoder('utf-8').decode(bytes)
    } catch (e) { return value }
  }
  private async send() {
    const text = this.input.trim(); if (!text || this.sending) return
    this.messages.push({ role: 'user', content: text }); this.messages.push({ role: 'assistant', content: '', tool: false })
    const target = this.messages[this.messages.length - 1]; this.input = ''; this.sending = true; this.scrollBottom()
    try {
      const response = await fetch('/api/ai/chat/stream', { method: 'POST', headers: { 'Content-Type': 'application/json', token: UserModule.token || '' }, body: JSON.stringify({ conversationId: this.conversationId, message: text }) })
      if (!response.body) throw new Error('浏览器不支持流式响应')
      const reader = response.body.getReader(); const decoder = new TextDecoder('utf-8'); let buffer = ''
      while (true) {
        const part = await reader.read(); if (part.done) break
        buffer += decoder.decode(part.value, { stream: true })
        const events = buffer.split(/\r?\n\r?\n/); buffer = events.pop() || ''
        events.forEach(block => {
          const lines = block.split(/\r?\n/)
          const eventLine = lines.find(line => line.startsWith('event:'))
          const dataLines = lines.filter(line => line.startsWith('data:')).map(line => line.slice(5).replace(/^ /, ''))
          const eventName = eventLine ? eventLine.slice(6).trim() : 'message'
          const data = this.decodeEventData(dataLines.join(''))
          if (eventName === 'token') target.content += data
          if (eventName === 'tool') target.tool = true
          if (eventName === 'error') target.content += data
          this.scrollBottom()
        })
      }
      buffer += decoder.decode()
    } catch (error) { target.content += `请求失败：${(error as Error).message}` } finally { this.sending = false; this.scrollBottom() }
  }
}
</script>

<style lang="scss" scoped>
.ai-page{height:calc(100vh - 60px);display:grid;grid-template-columns:290px 1fr;background:#eef3f9}.ai-side{padding:30px 24px;background:linear-gradient(180deg,#0a203a,#0e345a);color:#fff}.ai-badge{width:52px;height:52px;border-radius:16px;background:linear-gradient(135deg,#22d3ee,#1677ff);display:flex;align-items:center;justify-content:center;font-weight:800;font-size:20px}.ai-side h2{margin:18px 0 6px}.ai-side>p{color:#8fb3d6;margin-bottom:25px}.status-card{display:flex;gap:12px;align-items:center;padding:14px;border:1px solid rgba(255,255,255,.12);border-radius:12px;background:rgba(255,255,255,.06);margin-bottom:20px}.status-card .dot{width:9px;height:9px;border-radius:50%}.ok{background:#3ddc97}.warn{background:#ffb547}.status-card b,.status-card small{display:block}.status-card small{color:#8fb3d6;margin-top:3px}.ai-side button{width:100%;text-align:left;padding:11px 12px;margin-bottom:8px;border:0;border-radius:9px;color:#c8def2;background:rgba(255,255,255,.06);cursor:pointer}.ai-side button:hover{background:rgba(22,119,255,.35);color:#fff}.tip{font-size:12px;line-height:1.65;color:#8fb3d6;margin-top:18px}.chat-main{margin:20px;background:#fff;border-radius:16px;display:grid;grid-template-rows:70px 1fr auto;overflow:hidden;box-shadow:0 8px 28px rgba(26,65,105,.08)}header{display:flex;justify-content:space-between;align-items:center;padding:0 24px;border-bottom:1px solid #edf1f6}header h3{margin:0 0 5px;color:#172b4d}header span{font-size:12px;color:#8a96a7}.messages{overflow-y:auto;padding:28px}.welcome{text-align:center;padding-top:12vh;color:#748197}.welcome .spark{font-size:42px;color:#1677ff}.welcome h2{color:#243b5a}.message{display:flex;gap:12px;margin-bottom:20px}.message.user{flex-direction:row-reverse}.avatar{width:36px;height:36px;flex:0 0 36px;border-radius:11px;background:#e8f2ff;color:#1677ff;display:flex;align-items:center;justify-content:center;font-weight:700}.user .avatar{background:#1677ff;color:#fff}.bubble{max-width:72%;background:#f3f6fa;border-radius:4px 14px 14px 14px;padding:13px 16px}.user .bubble{background:#1677ff;color:#fff;border-radius:14px 4px 14px 14px}.bubble pre{margin:0;white-space:pre-wrap;font:14px/1.75 inherit}.tool{font-size:11px;color:#1677ff;margin-bottom:7px}.composer{border-top:1px solid #edf1f6;padding:16px 22px}.composer-foot{display:flex;justify-content:space-between;align-items:center;margin-top:10px}.composer-foot span{font-size:12px;color:#9aa5b5}
</style>

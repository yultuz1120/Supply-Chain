<template>
  <div class="page-wrap">
    <div class="page-head"><div><h2>RAG 知识库</h2><p>内置农产品储运资料，使用本地中文 Embedding 模型写入 Milvus。</p></div><el-button type="primary" :loading="indexing" icon="el-icon-upload" @click="buildIndex">一键写入 Milvus</el-button></div>
    <div class="grid">
      <div class="panel docs"><div class="panel-title"><h3>内置演示资料</h3><el-tag>{{ documents.length }} 篇</el-tag></div><div v-for="name in documents" :key="name" class="doc"><i class="el-icon-document" /><span>{{ name }}</span><el-tag size="mini" type="success">Markdown</el-tag></div></div>
      <div class="panel search"><div class="panel-title"><h3>检索测试</h3><span>Milvus Similarity Search</span></div><div class="search-bar"><el-input v-model="query" placeholder="输入问题，例如：叶菜如何储存？" @keyup.enter.native="search" /><el-button type="primary" :loading="searching" @click="search">检索</el-button></div><div v-if="!results.length" class="empty">写入知识库后，可在这里测试向量召回效果</div><div v-for="(item,index) in results" :key="index" class="result"><div><el-tag size="mini">{{ item.document }}</el-tag><b>相似度 {{ score(item.score) }}</b></div><p>{{ item.text }}</p></div></div>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'
import { getKnowledgeDocuments, indexKnowledge, searchKnowledge } from '@/api/ai'

@Component({ name: 'KnowledgeBase' })
export default class extends Vue {
  private documents: string[] = []
  private results: any[] = []
  private query = ''
  private indexing = false
  private searching = false
  async created() { const res: any = await getKnowledgeDocuments(); this.documents = res.data.data || [] }
  private async buildIndex() {
    this.indexing = true
    try { const res: any = await indexKnowledge(); if (res.data.code === 1) this.$message.success(res.data.data.message) ; else this.$message.error(res.data.msg) } catch (e) { this.$message.error('Milvus 连接失败，请确认 Docker 服务和端口 19530') } finally { this.indexing = false }
  }
  private async search() {
    if (!this.query.trim()) return this.$message.warning('请输入检索问题')
    this.searching = true
    try { const res: any = await searchKnowledge(this.query); this.results = res.data.data || []; if (!this.results.length) this.$message.info('没有找到达到相似度阈值的内容') } catch (e) { this.$message.error('检索失败，请先写入知识库') } finally { this.searching = false }
  }
  private score(value: number) { return `${((value || 0) * 100).toFixed(1)}%` }
}
</script>

<style lang="scss" scoped>
.page-wrap{padding:28px}.page-head{display:flex;justify-content:space-between;align-items:center;margin-bottom:20px;h2{font-size:24px;color:#13233a;margin:0 0 8px}p{color:#7b8798;margin:0}}.grid{display:grid;grid-template-columns:420px 1fr;gap:20px}.panel{background:#fff;border-radius:14px;padding:22px;box-shadow:0 4px 18px rgba(31,76,130,.06)}.panel-title{display:flex;align-items:center;justify-content:space-between;margin-bottom:20px}.panel-title h3{margin:0;color:#243b5a}.panel-title span{font-size:11px;color:#9aa5b5}.doc{display:flex;align-items:center;gap:10px;padding:13px 8px;border-bottom:1px solid #eef2f7}.doc i{color:#1677ff;font-size:19px}.doc span{flex:1;color:#41536d}.search-bar{display:flex;gap:10px}.empty{text-align:center;color:#a0aaba;padding:100px 0}.result{padding:16px 0;border-bottom:1px solid #edf1f6}.result>div{display:flex;justify-content:space-between;align-items:center}.result b{font-size:12px;color:#36a473}.result p{font-size:13px;line-height:1.7;color:#56657a;white-space:pre-wrap}
</style>

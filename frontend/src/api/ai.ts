import request from '@/utils/request'

export const getAiStatus = () => request({ url: '/ai/status', method: 'get' })
export const getKnowledgeDocuments = () => request({ url: '/ai/knowledge/documents', method: 'get' })
export const indexKnowledge = () => request({ url: '/ai/knowledge/index', method: 'post' })
export const searchKnowledge = (query: string) =>
  request({ url: '/ai/knowledge/search', method: 'post', data: { query, maxResults: 5 } })


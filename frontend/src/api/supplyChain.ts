import request from '@/utils/request'

export const getDashboardSummary = () => request({ url: '/delivery/summary', method: 'get' })

export const getStores = (params: any) => request({ url: '/stores', method: 'get', params })
export const addStore = (data: any) => request({ url: '/stores', method: 'post', data })
export const updateStore = (data: any) => request({ url: '/stores', method: 'put', data })

export const getDeliveryOrders = (params: any) => request({ url: '/delivery/orders', method: 'get', params })
export const getCouriers = () => request({ url: '/delivery/couriers', method: 'get' })
export const addCourier = (data: any) => request({ url: '/delivery/couriers', method: 'post', data })
export const updateCourier = (data: any) => request({ url: '/delivery/couriers', method: 'put', data })
export const setCourierStatus = (id: number, status: number) =>
  request({ url: `/delivery/couriers/${id}/status/${status}`, method: 'post' })
export const assignCourier = (id: number, courierId: number) =>
  request({ url: `/delivery/orders/${id}/assign`, method: 'post', data: { courierId } })
export const setDeliveryStatus = (id: number, status: number) =>
  request({ url: `/delivery/orders/${id}/status/${status}`, method: 'post' })
export const urgeDelivery = (id: number, remark = '门店页面催单') =>
  request({ url: `/delivery/orders/${id}/urge`, method: 'post', data: { remark } })


import axios from 'axios'
import { ElMessage } from 'element-plus'
import { clearAuthSession } from '@/auth/session'

const apiBaseURL = import.meta.env.VITE_API_BASE_URL || '/api'

const request = axios.create({
  baseURL: apiBaseURL,
  timeout: 30000,
  withCredentials: true
})

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res
  },
  (error) => {
    const message = error?.response?.data?.message || error.message || '网络错误'
    if (error?.response?.status === 401) {
      clearAuthSession()
      if (window.location.pathname !== '/login') {
        const redirect = encodeURIComponent(window.location.pathname + window.location.search)
        window.location.href = `/login?redirect=${redirect}`
      }
    }
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

// ========== 登录认证 API ==========
export const loginAdmin = (data: { username: string; password: string }) => request.post('/auth/login', data)
export const getCurrentUser = () => request.get('/auth/me')
export const changePassword = (data: { currentPassword: string; newPassword: string }) =>
  request.post('/auth/change-password', data)
export const logoutAdmin = () => request.post('/auth/logout')

// ========== 配置管理 API ==========
export const getConfigList = () => request.get('/wechat-configs')
export const getActiveConfig = () => request.get('/wechat-configs/active')
export const createConfig = (data: any) => request.post('/wechat-configs', data)
export const updateConfig = (id: number, data: any) => request.put(`/wechat-configs/${id}`, data)
export const deleteConfig = (id: number) => request.delete(`/wechat-configs/${id}`)
export const activateConfig = (id: number) => request.post(`/wechat-configs/${id}/activate`)

// ========== 二维码 API ==========
export const generateQrcode = (data: any) => request.post('/qrcodes', data)
export const getQrcodeList = (params?: any) => request.get('/qrcodes', { params })
export const getQrcodeById = (id: number) => request.get(`/qrcodes/${id}`)
export const deleteQrcode = (id: number) => request.delete(`/qrcodes/${id}`)
export const batchDeleteQrcode = (ids: number[]) => request.post('/qrcodes/batch-delete', ids)

// ========== 扫码记录 API ==========
export const getScanList = (params?: any) => request.get('/scans', { params })
export const getScanStats = () => request.get('/scans/stats')

// ========== 活动管理 API ==========
export const getCampaignList = () => request.get('/campaigns')
export const getActiveCampaigns = () => request.get('/campaigns/active')
export const createCampaign = (data: any) => request.post('/campaigns', data)
export const updateCampaign = (id: number, data: any) => request.put(`/campaigns/${id}`, data)
export const deleteCampaign = (id: number) => request.delete(`/campaigns/${id}`)

// ========== 落地页 API ==========
export const getLandingData = (qid: number) => request.get('/public/landing', { params: { qid } })
export const getWechatJssdkSignature = (url: string) =>
  request.get('/public/wechat-jssdk/signature', { params: { url } })

export default request

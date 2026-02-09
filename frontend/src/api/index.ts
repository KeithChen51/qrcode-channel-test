import axios from 'axios'
import { ElMessage } from 'element-plus'

const apiBaseURL = import.meta.env.VITE_API_BASE_URL || '/api'

// 创建axios实例
const request = axios.create({
    baseURL: apiBaseURL,
    timeout: 30000
})

// 响应拦截器
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
        ElMessage.error(message)
        return Promise.reject(error)
    }
)

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

export default request

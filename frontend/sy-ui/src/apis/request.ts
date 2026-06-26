import axios from 'axios'
import type { ApiResponse } from '@/types/common'

const http = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' },
})

// Response interceptor
http.interceptors.response.use(
  (response) => {
    const data = response.data as ApiResponse
    if (data.code !== 20000) {
      return Promise.reject(new Error(data.message || '请求失败'))
    }
    return response
  },
  (error) => {
    if (error.code === 'ECONNABORTED') {
      return Promise.reject(new Error('请求超时，请重试'))
    }
    if (!error.response) {
      return Promise.reject(new Error('网络异常，请检查网络连接'))
    }
    const data = error.response.data as ApiResponse | undefined
    return Promise.reject(new Error(data?.message || `请求失败 (${error.response.status})`))
  },
)

export default http

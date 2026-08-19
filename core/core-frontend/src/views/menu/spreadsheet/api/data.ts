import request from '@/config/axios'
import type { PluginFieldValuesRequest } from '../types/plugin'

/**
 * 查询明细表 / 透视表数据。
 *
 * 返回整个响应体（含 code / msg / data），便于调用方判定业务错误：
 * - code === 0：成功，调用方取 res.data。
 * - code !== 0：判定为获取数据出错，抛出的 Error.message 即后端 msg，
 *   由渲染层作为异常原因展示在占位符的 tooltip 中。
 */
export const queryData = async (data): Promise<any> => {
  try {
    const res = await request.post({ url: '/spreadsheetData/queryData', data })
    if (res?.code !== undefined && res.code !== 0) {
      throw new Error(res.msg || 'Query data failed')
    }
    return res
  } catch (error) {
    if (error instanceof Error) {
      throw error
    }
    // 全局 axios 拦截器在 code !== 0 时会 reject msg 字符串，这里统一包装成 Error。
    throw new Error(typeof error === 'string' ? error : 'Query data failed')
  }
}

export const queryFieldValues = async (data: PluginFieldValuesRequest): Promise<string[]> => {
  const res = await request.post({ url: '/spreadsheetData/queryFieldValues', data })
  return Array.isArray(res?.data) ? res.data.filter((value): value is string => typeof value === 'string') : []
}

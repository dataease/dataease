import type { QueryRequest } from '@/config/axios/queryScope'
import request from '@/config/axios'
import type { DataEaseError } from '@/config/axios/error'
import { AxiosHeaders } from 'axios'
import { originNameHandleWithArr, originNameHandleBackWithArr } from '@/utils/CalculateFields'
import { cloneDeep } from 'lodash-es'
import { Base64 } from 'js-base64'
import { ElMessage } from 'element-plus-secondary'
export interface Field {
  id: number | string
  datasourceId: number | string
  datasetTableId: number | string
  datasetGroupId: number | string
  originName: string
  name: string
  dataeaseName: string
  groupType: string
  type: string
  deType: number
  deExtractType: number
  extField: number
  checked: boolean
  fieldShortName: string
  desensitized: boolean
  chartId?: number | string
  agg?: boolean
  params?: unknown[]
}

export interface ComponentInfo {
  id: string
  name: string
  deType: number
  type: string
  datasetId: string
  chartId?: number | string
}

export interface ChartCalcFieldValidationRequest {
  datasetId: string
  chartId: string
  originName: string
  params?: Array<{ id: string; name: string; value: number | string }>
}

export const validateCalcField = async (data: ChartCalcFieldValidationRequest): Promise<void> => {
  const copyData = cloneDeep(data)
  copyData.originName = Base64.encode(copyData.originName)
  const res = await request.post<Promise<IResponse<void>>>({
    url: '/chartData/validateCalcField',
    data: copyData
  })
  // 拦截器会放行图表错误码，公式校验仅将正常响应判定为成功。
  if (res.code !== 0) {
    ElMessage.error(res.msg)
    throw new Error(res.msg)
  }
}

export const getFieldByDQ = async (
  id,
  chartId,
  data
): Promise<{ dimensionList: Field[]; quotaList: Field[] }> => {
  return request.post({ url: `/chart/listByDQ/${id}/${chartId}`, data: data }).then(res => {
    originNameHandleBackWithArr(res?.data, ['dimensionList', 'quotaList'])
    return res?.data
  })
}

export const copyChartField = async (id, chartId): Promise<IResponse> => {
  return request.post({ url: `/chart/copyField/${id}/${chartId}`, data: {} }).then(res => {
    return res?.data
  })
}

export const deleteChartField = async (id): Promise<IResponse> => {
  return request.post({ url: `/chart/deleteField/${id}`, data: {} }).then(res => {
    return res?.data
  })
}

// 通过图表对象获取数据
export const getData = async (data, queryRequest?: QueryRequest): Promise<IResponse> => {
  delete data.data
  const copyData = cloneDeep(data)
  // K线图查询时先按横轴降序取最新数据，渲染阶段再恢复升序
  if (copyData.type === 'stock-line' && copyData.xAxis?.length) {
    copyData.xAxis[0].sort = 'desc'
  }
  const fields = [
    'xAxis',
    'xAxisExt',
    'yAxis',
    'yAxisExt',
    'extBubble',
    'extLabel',
    'extStack',
    'extTooltip',
    'extColor'
  ]
  const dataFields = ['fields', 'sourceFields']
  originNameHandleWithArr(copyData, fields)
  let chartError: DataEaseError | undefined
  return request
    .post({
      url: '/chartData/getData',
      data: copyData,
      queryRequest,
      handleErrorLocally: (error: DataEaseError) => {
        const headers = AxiosHeaders.from(error.response?.headers)
        const reason = headers.get('X-DE-Error-Reason') || error.category
        // 此处只处理 /chartData/getData 的错误响应，40001/50002 在图表内展示
        // 错误码不代表具体根因，认证、资源权限标记及其他错误保留全局处理
        if (
          !error.response ||
          (error.code !== 40001 && error.code !== 50002) ||
          error.httpStatus === 401 ||
          headers.has('DE-GATEWAY-FLAG') ||
          headers.has('DE-FORBIDDEN-FLAG') ||
          (reason && reason !== 'INTERNAL') ||
          error.messageKey === 'i18n_error_key_changed' ||
          error.message.includes('rsa info has been changed')
        ) {
          return false
        }
        chartError = error
        return true
      }
    })
    .catch(error => {
      if (!chartError || error !== chartError) throw error
      // 复用图表已有的非零业务码展示分支，兼容非 2xx 错误响应
      return { code: chartError.code, msg: chartError.message }
    })
    .then(res => {
      if (res.code === 0) {
        originNameHandleBackWithArr(res?.data, fields)
        // 动态计算字段在数据中，也需要转码
        originNameHandleWithArr(res?.data?.data, dataFields)
        originNameHandleBackWithArr(res?.data?.data, dataFields)
        originNameHandleBackWithArr(res?.data?.data?.left, ['fields'])
        originNameHandleBackWithArr(res?.data?.data?.right, ['fields'])
        return res?.data
      } else {
        originNameHandleBackWithArr(res, fields)
        originNameHandleBackWithArr(res?.data, dataFields)
        originNameHandleBackWithArr(res?.data?.left, ['fields'])
        originNameHandleBackWithArr(res?.data?.right, ['fields'])
        return res
      }
    })
}

export const innerExportDetails = async (data): Promise<IResponse> => {
  return request.post({
    url: '/chartData/innerExportDetails',
    method: 'post',
    data: data,
    loading: true,
    responseType: 'blob'
  })
}

export const innerExportDataSetDetails = async (data): Promise<IResponse> => {
  return request.post({
    url: '/chartData/innerExportDataSetDetails',
    method: 'post',
    data: data,
    loading: true,
    responseType: 'blob'
  })
}

// 通过图表id获取数据
export const getChart = async (id): Promise<IResponse> => {
  return request.post({ url: `/chart/getChart/${id}`, data: {} }).then(res => {
    return res?.data
  })
}

// 获取单个字段枚举值
export const getFieldData = async ({ fieldId, fieldType, data }): Promise<IResponse> => {
  delete data.data
  return request
    .post({ url: `/chartData/getFieldData/${fieldId}/${fieldType}`, data })
    .then(res => {
      return res
    })
}

// 获取下钻字段枚举值
export const getDrillFieldData = async ({ fieldId, data }): Promise<IResponse> => {
  delete data.data
  return request.post({ url: `/chartData/getDrillFieldData/${fieldId}`, data }).then(res => {
    return res
  })
}

export const checkSameDataSet = async (viewIdSource, viewIdTarget) =>
  request.get({ url: '/chart/checkSameDataSet/' + viewIdSource + '/' + viewIdTarget })

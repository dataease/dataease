import { service } from './service'
import { needsConfigurationEncryption, sealConfiguration } from './configurationTransport'

import { config } from './config'

const { default_headers } = config

const request = async (option: any): Promise<any> => {
  const {
    url,
    method,
    params,
    data,
    headersType,
    responseType,
    loading,
    signal,
    queryRequest,
    handleErrorLocally,
    onUploadProgress,
    headers = {}
  } = option
  let requestData = data
  if (needsConfigurationEncryption(url, method)) {
    const challenge = await request({
      url: '/configurationKey',
      method: 'get',
      params: { path: url }
    })
    requestData = await sealConfiguration(data, url, challenge.data)
  }
  const result = service({
    url: url,
    method,
    loading,
    signal,
    queryRequest,
    handleErrorLocally,
    onUploadProgress,
    params,
    data: requestData,
    responseType: responseType,
    headers: {
      'Content-Type': headersType || default_headers,
      Accept: 'application/json',
      'X-Requested-With': 'XMLHttpRequest',
      'Cache-Control': 'no-store',
      Pragma: 'no-cache',
      ...headers
    }
  })
  return queryRequest ? result.finally(queryRequest.finish) : result
}

export default {
  get: <T = any>(option: any) => {
    return request({ method: 'get', ...option }) as unknown as T
  },
  post: <T = any>(option: any) => {
    return request({ method: 'post', ...option }) as unknown as T
  },
  delete: <T = any>(option: any) => {
    return request({ method: 'delete', ...option }) as unknown as T
  },
  put: <T = any>(option: any) => {
    return request({ method: 'put', ...option }) as unknown as T
  }
}

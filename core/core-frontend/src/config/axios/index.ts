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

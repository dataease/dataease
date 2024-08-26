import request from '@/config/axios'

export interface Font {
  id: string
  name: string
  fileName: string
  isDefault: boolean
  isBuiltin?: boolean
}

export const list = (data = {}) => {
  return request.post({ url: '/typeface/listFont', data }).then(res => {
    return res?.data
  })
}

export const create = (data = {}) => {
  return request.post({ url: '/typeface/create', data }).then(res => {
    return res?.data
  })
}

export const edit = (data = {}) => {
  return request.post({ url: '/typeface/edit', data }).then(res => {
    return res?.data
  })
}

export const deleteById = id => {
  return request.post({ url: '/typeface/delete/' + id, data: {} }).then(res => {
    return res?.data
  })
}

export const defaultFont = () => {
  return request.get({ url: '/typeface/defaultFont' }).then(res => {
    return res?.data
  })
}

export const uploadFontFile = async (data): Promise<IResponse> => {
  return request
    .post({
      url: '/typeface/uploadFile',
      data,
      loading: true,
      headersType: 'multipart/form-data;'
    })
    .then(res => {
      return res
    })
}

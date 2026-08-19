import request from '@/config/axios'

export const bindApi = (origin: number, data) => {
  const prefixMapping =  {4: 'lark', 5: 'dingtalk', 6: 'wecom', 7: 'larksuite'}
  const url = `/${prefixMapping[origin]}/bind`
  return request.post({url, data})
}

export const unBindApi = (origin: number) => {
  return request.post({url: `/user/unBind/${origin}`})
}

export const bindStatusApi = () => {
  return request.get({url: '/user/bindStatus'})
}

export const queryCategoryStatus = () => {
  const url = `/setting/authentication/status`
  return request.get({ url })
}
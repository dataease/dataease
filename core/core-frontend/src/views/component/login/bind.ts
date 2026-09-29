import request from '@/config/axios'
import {
  BIND_NONCE_HEADER,
  bindingNonce,
  bindingReturnPath,
  saveBinding,
  type PendingBinding,
  type PreparedBinding
} from '@/utils/platformBinding'

export const prepareBindApi = async (
  origin: number,
  credentials: { pwd?: string; mfaCode?: string }
) => {
  const nonce = bindingNonce()
  const res = await request.post({
    url: '/platform/bind/prepare',
    data: { origin, returnPath: bindingReturnPath(), ...credentials },
    headers: { [BIND_NONCE_HEADER]: nonce }
  })
  const prepared = res.data as PreparedBinding
  saveBinding(prepared, nonce)
  return prepared
}

export const bindApi = (pending: PendingBinding, code: string) => {
  const origin = pending.origin
  const prefixMapping = { 4: 'lark', 5: 'dingtalk', 6: 'wecom', 7: 'larksuite' }
  const url = `/${prefixMapping[origin]}/bind`
  return request.post({
    url,
    data: { state: pending.state, code },
    headers: { [BIND_NONCE_HEADER]: pending.nonce }
  })
}

export const unBindApi = (origin: number) => {
  return request.post({ url: `/user/unBind/${origin}` })
}

export const bindStatusApi = () => {
  return request.get({ url: '/user/bindStatus' })
}

export const queryCategoryStatus = () => {
  const url = `/setting/authentication/status`
  return request.get({ url })
}

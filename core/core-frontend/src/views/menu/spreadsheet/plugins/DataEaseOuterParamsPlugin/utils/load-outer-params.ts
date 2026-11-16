import { Base64 } from 'js-base64'
import request from '@/config/axios'
import { useI18n } from '@/hooks/web/useI18n'
import { useEmbedded } from '@/store/modules/embedded'
import { findOuterParams } from '../../../api/outer-params'

const parseParams = (value: unknown): Record<string, unknown> => {
  if (!value || typeof value !== 'object' || Array.isArray(value)) {
    throw new Error(useI18n().t('visualization.outer_param_decode_error'))
  }
  return value as Record<string, unknown>
}

const mergeParams = (
  current: Record<string, unknown>,
  incoming: Record<string, unknown>
): Record<string, unknown> => {
  const version = incoming.outerParamsVersion ?? 'v1'
  if (version !== 'v1' && version !== 'v2') {
    throw new Error(useI18n().t('visualization.outer_param_decode_error'))
  }
  // 各来源先统一格式，避免 V1 令牌值覆盖 V2 参数后被误判为格式错误。
  const values = Object.fromEntries(
    Object.entries(incoming)
      .filter(([key]) => key !== 'outerParamsVersion')
      .map(([key, value]) => [key, version === 'v2' ? value : { value }])
  )
  return { ...current, ...values, outerParamsVersion: 'v2' }
}

export const loadSpreadsheetOuterParams = async (
  spreadsheetId: string | number,
  signal: AbortSignal
) => {
  const query = new URLSearchParams(window.location.hash.split('?')[1] || window.location.search)
  if (query.get('ignoreParams') === 'true') return undefined
  const config = await findOuterParams(String(spreadsheetId), signal)
  if (!config.checked) return undefined

  const embeddedStore = useEmbedded()
  let attachParams: Record<string, unknown> = {}
  try {
    const encodedParams = query.get('attachParams')
    if (encodedParams) {
      // URLSearchParams 已完成 URI 解码，剩余格式与仪表板一致：Base64(JSON)。
      attachParams = mergeParams(
        attachParams,
        parseParams(JSON.parse(Base64.decode(encodedParams)))
      )
    }
    if (embeddedStore.getOuterParams) {
      const embeddedParams = parseParams(JSON.parse(embeddedStore.getOuterParams))
      if (embeddedParams.attachParams !== undefined) {
        attachParams = mergeParams(attachParams, parseParams(embeddedParams.attachParams))
      }
    }
  } catch {
    throw new Error(useI18n().t('visualization.outer_param_decode_error'))
  }

  if (embeddedStore.getToken) {
    // 每次读取当前令牌的参数，避免同页重建嵌入实例时沿用旧令牌缓存。
    const response = await request.get({ url: '/embedded/getTokenArgs', signal })
    attachParams = mergeParams(attachParams, {
      ...(response.data || {}),
      outerParamsVersion: 'v1'
    })
  }
  return { config, attachParams }
}

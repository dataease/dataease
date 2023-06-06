import { PATH_URL } from '@/config/axios/service'

export function imgUrlTrans(url) {
  return url ? PATH_URL + url : null
}

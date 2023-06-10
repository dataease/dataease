import { deepCopy } from '@/utils/utils'

export function chartTransStr2Object(targetIn, copy) {
  const target = copy === 'Y' ? deepCopy(targetIn) : targetIn
  return target
}

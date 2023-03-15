import { nanoid } from 'nanoid'

// 主要用于 Vue 的 diff 算法，为每个元素创建一个独一无二的 ID
export const generateID = () => {
  return nanoid()
}

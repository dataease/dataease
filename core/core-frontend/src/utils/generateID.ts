import { guid } from '@/views/visualized/data/dataset/form/util.js'

// snowflake
export const generateID = (options?) => {
  return guid(options)
}
export default generateID

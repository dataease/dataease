import { guid } from '@/views/visualized/data/dataset/form/util.js'

// snowflake
export const generateID = () => {
  return guid()
}
export default generateID

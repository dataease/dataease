import { h } from 'vue'
import { ElIcon } from 'element-plus-secondary'
import Icon from './src/Icon.vue'
const hIcon = (name: string) => {
  return h(ElIcon, null, {
    default: () => h(name)
  })
}
export { Icon, hIcon }

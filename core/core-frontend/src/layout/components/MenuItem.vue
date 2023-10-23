<script lang="ts">
import { h } from 'vue'
import { Icon } from '@/components/icon-custom'
import { ElMenuItem, ElSubMenu, ElIcon } from 'element-plus-secondary'

const title = props => {
  const { title, icon } = props.menu?.meta || {}
  return [
    h(ElIcon, null, { default: () => h(Icon, { className: 'logo', name: icon }) }),
    h('span', null, { default: () => title })
  ]
}

const MenuItem = props => {
  const { children, hidden, path } = props.menu
  if (hidden) {
    return null
  }
  if (children?.length) {
    return h(
      ElSubMenu,
      { index: path },
      {
        title: () => title(props),
        default: () => children.map(ele => h(MenuItem, { menu: ele }))
      }
    )
  }
  return h(
    ElMenuItem,
    { index: path },
    {
      title: () => title(props)
    }
  )
}
export default MenuItem
</script>

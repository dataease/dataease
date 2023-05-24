<script lang="ts">
import { h } from 'vue'
import { ElMenuItem, ElSubMenu } from 'element-plus-secondary'

const title = props => {
  const { title } = props.menu?.meta || {}
  return [h('span', null, { default: () => title })]
}

const HeaderMenuItem = props => {
  const { children = [], hidden, path } = props.menu
  if (hidden) {
    return null
  }

  if (children?.length) {
    return h(
      ElSubMenu,
      { index: path },
      {
        title: () => title(props),
        default: () => children.map(ele => h(HeaderMenuItem, { menu: ele, index: path }))
      }
    )
  }

  return h(
    ElMenuItem,
    { index: props.index ? `${props.index}/${path}` : path },
    {
      title: () => title(props)
    }
  )
}
export default HeaderMenuItem
</script>

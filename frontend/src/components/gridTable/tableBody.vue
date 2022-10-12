<script>
export default {
  name: 'TableBody',
  functional: true,
  props: {
    columns: {
      type: Array,
      default: () => []
    }
  },
  render(h, context) {
    const nodes = []
    const { columns } = context.props
    const { children = [] } = context
    if (!columns?.length) return children
    children.forEach((ele) => {
      const { prop, type } = ele.componentOptions?.propsData || {}
      if (columns.includes(prop) || type === 'selection') {
        nodes.push(ele)
      }
    })
    return nodes
  }
}
</script>

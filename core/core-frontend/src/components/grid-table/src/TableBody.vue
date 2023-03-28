<script lang="ts">
const TableBody = (props, context) => {
  const nodes = []
  const { columns } = props
  const [slots] = context.slots.default()
  const { children = [] } = slots || {}
  if (!columns?.length) return children
  children.forEach(ele => {
    const { prop, key, type } = ele.props || {}
    if (columns.includes(prop) || key === '_operation' || type === 'selection') {
      nodes.push(ele)
    }
  })
  return nodes
}
export default TableBody
</script>

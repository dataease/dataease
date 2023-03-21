<script lang="ts">
const nodes = []
const TableBody = (props, context) => {
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

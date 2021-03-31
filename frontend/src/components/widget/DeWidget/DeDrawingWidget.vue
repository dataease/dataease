<script>
export default {
  functional: true,
  props: {
    item: {
      type: Object,
      default: null
    }
  },
  render(createElement, context) {
    const item = context.props.item
    return createElement(item.component, {
      props: {
        options: item.options,
        element: item
      }
    })
  }
}
</script>

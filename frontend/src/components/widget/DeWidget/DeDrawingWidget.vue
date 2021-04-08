<script>
export default {
  name: 'DeDrawingWidget',
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
      },
      on: {
        'value-change': value => {
          context.listeners['filter-value-change'] && context.listeners['filter-value-change'](value)
        }
      }
    },
    context.data,
    context.children
    )
  }
}
</script>

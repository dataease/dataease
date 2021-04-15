<script>
import { ApplicationContext } from '@/utils/ApplicationContext'
export default {
  name: 'DeDrawingWidget',
  functional: true,
  props: {
    serviceName: {
      type: String,
      default: null
    },
    panelId: {
      type: String,
      default: null
    }
  },

  render(createElement, context) {
    const widgetInfo = ApplicationContext.getService(context.props.serviceName)
    // const widgetInfo = context.props.widgetInfo
    const panelId = context.props.panelId
    const dialogInfo = widgetInfo.getDialogPanel && widgetInfo.getDialogPanel(panelId) || null
    if (!dialogInfo) {
      throw new Error('系统错误')
    }
    return createElement(dialogInfo.component, {
      props: {
        element: dialogInfo
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

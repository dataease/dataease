<script>
import { ApplicationContext } from '@/utils/ApplicationContext'
import store from '@/store'
export default {
  name: 'DeDrawingWidget',
  functional: true,
  props: {
    serviceName: {
      type: String,
      default: null
    }
  },

  render(createElement, context) {
    const widgetInfo = ApplicationContext.getService(context.props.serviceName)
    // const widgetInfo = context.props.widgetInfo
    const dialogInfo = widgetInfo.getDialogPanel && widgetInfo.getDialogPanel() || null
    const drawInfo = widgetInfo.getDrawPanel && widgetInfo.getDrawPanel() || null
    const curComponent = store.state.curComponent
    if (!dialogInfo) {
      throw new Error('系统错误')
    }
    return createElement(dialogInfo.component, {
      props: {
        element: curComponent || drawInfo || dialogInfo
      },
      style: context.data.style,
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

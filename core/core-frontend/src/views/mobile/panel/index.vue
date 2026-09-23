<script lang="ts" setup>
import { onBeforeMount, ref, onBeforeUnmount } from 'vue'
import { useEmitt } from '@/hooks/web/useEmitt'
import eventBus from '@/utils/eventBus'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { XpackComponent } from '@/components/plugin'
import DePreviewMobile from './MobileInPc.vue'
import { findComponentById, initTabMobileLayout, mobileViewStyleSwitch } from '@/utils/canvasUtils'
import { deepCopy } from '@/utils/utils'
const panelInit = ref(false)
const dvMainStore = dvMainStoreWithOut()

const checkItemPosition = component => {
  component.x = 1
  component.sizeX = 72
  component.y = dvMainStore.componentData.reduce((pre, next) => {
    return Math.max(pre, next.y + next.sizeY)
  }, 1)
  component.sizeY = 20
}

const apdataQuery = ele => {
  if (ele.component === 'VQuery') {
    ele.propValue?.forEach(queryItem => {
      queryItem['tempPlaceholder'] = queryItem.placeholder
      queryItem['tempQueryConditionWidth'] = queryItem.queryConditionWidth
      queryItem.placeholder = queryItem.mPlaceholder || queryItem.placeholder
      queryItem.queryConditionWidth =
        queryItem.mQueryConditionWidth || queryItem.queryConditionWidth
    })
  }
}

/**
 * 将主窗口传入的 PC 图表配置应用到移动端画布并触发原有刷新入口
 * @param component 与 viewInfo 对应的移动端组件，决定使用重绘还是重新取数
 * @param viewInfo 已清除移动端覆盖项的 PC 图表配置
 */
const applySyncedViewInfo = (component, viewInfo) => {
  mobileViewStyleSwitch(viewInfo)
  if (component?.component === 'VQuery') {
    useEmitt().emitter.emit('renderChart-' + component.id, viewInfo)
  } else if (component?.component === 'UserView') {
    useEmitt().emitter.emit('calcData-' + component.id, viewInfo)
  }
}

const hanedleMessage = event => {
  if (event.data.type === 'panelInit') {
    const { componentData, canvasStyleData, dvInfo, canvasViewInfo, isEmbedded } = event.data.value
    componentData.forEach(ele => {
      const { mx, my, mSizeX, mSizeY, mStyle, mCommonBackground, mEvents } = ele
      ele.x = mx
      ele.y = my
      ele.sizeX = mSizeX
      ele.sizeY = mSizeY
      ele.style = deepCopy(mStyle || ele.style)
      ele.commonBackground = deepCopy(mCommonBackground || ele.commonBackground)
      ele.events = deepCopy(mEvents || ele.events)
      apdataQuery(ele)

      if (ele.component === 'DeTabs') {
        // 初始化 Tab 子组件运行时 geometry，已保存的 m* 布局会优先恢复
        initTabMobileLayout(ele)
        ele.propValue?.forEach(tabItem => {
          tabItem.componentData?.forEach(tabComponent => {
            const {
              mStyle: tStyle,
              mCommonBackground: tCommonBackground,
              mEvents: tEvents
            } = tabComponent
            tabComponent.style = deepCopy(tStyle || tabComponent.style)
            tabComponent.commonBackground = deepCopy(
              tCommonBackground || tabComponent.commonBackground
            )
            tabComponent.events = deepCopy(tEvents || tabComponent.events)
            if (tabComponent.component === 'VQuery') {
              tabComponent.propValue = deepCopy(tabComponent.propValue)
            }
          })
        })
      }
    })
    dvMainStore.setComponentData(componentData)
    dvMainStore.setMobileInPc(true)
    dvMainStore.setCanvasStyle(canvasStyleData)
    dvMainStore.updateCurDvInfo(dvInfo)
    dvMainStore.setCanvasViewInfo(canvasViewInfo)
    eventBus.emit('doCanvasInit-canvas-main')
    if (isEmbedded) return
    panelInit.value = true
  }
  // 进行内部组件渲染 type render 渲染 calcData 计算  主组件渲染
  if (event.data.type === 'componentStyleChange') {
    const { type, component, otherComponent } = event.data.value
    if (type === 'renderChart') {
      mobileViewStyleSwitch(component)
      useEmitt().emitter.emit('renderChart-' + component.id, component)
    } else if (type === 'calcData') {
      mobileViewStyleSwitch(component)
      useEmitt().emitter.emit('calcData-' + component.id, component)
    } else if (type === 'updateTitle') {
      mobileViewStyleSwitch(component)
      useEmitt().emitter.emit('updateTitle-' + component.id)
    } else if (['style', 'commonBackground', 'events', 'propValue'].includes(type)) {
      const mobileComponent = findComponentById(component.id)
      mobileComponent[type] = component[type]
    } else if (['syncPcDesign'].includes(type)) {
      const mobileComponent = findComponentById(component.id)
      // key 为 Tab 子组件 ID，value 为同步前尚未保存的移动端位置和尺寸
      const tabMobileGeometry = new Map()
      if (mobileComponent.component === 'DeTabs') {
        mobileComponent.propValue?.forEach(tabItem => {
          tabItem.componentData?.forEach(tabComponent => {
            tabMobileGeometry.set(tabComponent.id, {
              // mx、my 对应移动端 Matrix 的列坐标和行坐标
              mx: tabComponent.x,
              my: tabComponent.y,
              // mSizeX、mSizeY 对应移动端 Matrix 的列宽和行高
              mSizeX: tabComponent.sizeX,
              mSizeY: tabComponent.sizeY
            })
          })
        })
      }
      mobileComponent['style'] = component['style']
      mobileComponent['commonBackground'] = component['commonBackground']
      mobileComponent['events'] = component['events']
      mobileComponent['propValue'] = deepCopy(component['propValue'])
      if (mobileComponent.component === 'DeTabs') {
        // 同步 PC 内容时保留用户尚未保存的 Tab 子组件移动布局
        mobileComponent.propValue?.forEach(tabItem => {
          tabItem.componentData?.forEach(tabComponent => {
            Object.assign(tabComponent, tabMobileGeometry.get(tabComponent.id) || {})
          })
        })
        initTabMobileLayout(mobileComponent)
      }
      if (mobileComponent.component === 'DeTabs' && Array.isArray(otherComponent)) {
        // Tab 内图表拥有各自的 viewInfo；逐个恢复 PC 配置并触发原有重算或重绘入口
        otherComponent.forEach(viewInfo => {
          applySyncedViewInfo(findComponentById(viewInfo.id), viewInfo)
        })
      } else {
        applySyncedViewInfo(mobileComponent, otherComponent)
      }
    }
  }

  if (event.data.type === 'addToMobile') {
    const component = event.data.value
    checkItemPosition(component)
    dvMainStore.componentData.push(component)
    eventBus.emit('doCanvasInit-canvas-main')
  }

  if (event.data.type === 'setCanvasStyle') {
    dvMainStore.setCanvasStyle(event.data.value)
  }

  if (['mobileSave', 'mobilePatch'].includes(event.data.type)) {
    window.parent.postMessage(
      {
        type: `${event.data.type}FromMobile`,
        value: dvMainStore.componentData.reduce((pre, next) => {
          const { x, y, sizeX, sizeY, id, component, propValue, style, events, commonBackground } =
            next
          pre[id] = {
            x,
            y,
            sizeX,
            sizeY,
            component,
            events: deepCopy(events),
            propValue: deepCopy(propValue),
            style: JSON.parse(JSON.stringify(style)),
            commonBackground: JSON.parse(JSON.stringify(commonBackground))
          }
          if (next.component === 'DeTabs') {
            pre[id].tab = {}
            next.propValue?.forEach(tabItem => {
              tabItem.componentData?.forEach(tabComponent => {
                const {
                  x: tx,
                  y: ty,
                  sizeX: tSizeX,
                  sizeY: tSizeY,
                  id: tId,
                  style: tStyle,
                  events: tEvents,
                  propValue: tPropValue,
                  commonBackground: tCommonBackground
                } = tabComponent
                pre[id].tab[tId] = {
                  x: tx,
                  y: ty,
                  sizeX: tSizeX,
                  sizeY: tSizeY,
                  style: JSON.parse(JSON.stringify(tStyle)),
                  events: deepCopy(tEvents),
                  propValue: deepCopy(tPropValue),
                  commonBackground: JSON.parse(JSON.stringify(tCommonBackground))
                }
              })
            })
          }
          return pre
        }, {})
      },
      '*'
    )
  }
}

const initIframe = () => {
  panelInit.value = false
  setTimeout(() => {
    panelInit.value = true
  })
}
const curComponentChangeHandle = (type, value) => {
  window.parent.postMessage({ type: type, value: value }, '*')
}
onBeforeMount(() => {
  window.parent.postMessage({ type: 'panelInit', value: true }, '*')
  window.addEventListener('message', hanedleMessage)
  useEmitt({
    name: 'onMobileStatusChange',
    callback: ({ type, value }) => {
      mobileStatusChange(type, value)
    }
  })
  useEmitt({
    name: 'curComponentChange',
    callback: ({ type, value }) => {
      curComponentChangeHandle(type, value)
    }
  })
})

const mobileStatusChange = (type, value) => {
  window.parent.postMessage({ type, value }, '*')
  if (type === 'delFromMobile') {
    eventBus.emit('removeMatrixItemById-canvas-main', value)
  }
}

onBeforeUnmount(() => {
  window.removeEventListener('message', hanedleMessage)
})
</script>

<template>
  <div class="panel-mobile">
    <de-preview-mobile v-if="panelInit"></de-preview-mobile>
  </div>
  <XpackComponent
    @initIframe="initIframe"
    jsname="L2NvbXBvbmVudC9lbWJlZGRlZC1pZnJhbWUvRW50cmFuY2Vz"
  />
</template>

<style lang="less" scoped>
.panel-mobile {
  width: 100vw;
  height: 100vh;
  overflow-y: auto;
}
</style>

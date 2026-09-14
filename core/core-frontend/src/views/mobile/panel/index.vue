<script lang="ts" setup>
import { onBeforeMount, ref, onBeforeUnmount, defineAsyncComponent, nextTick } from 'vue'
import { useEmitt } from '@/hooks/web/useEmitt'
import eventBus from '@/utils/eventBus'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import DePreviewMobile from './MobileInPc.vue'
import { findComponentById, initTabMobileLayout, mobileViewStyleSwitch } from '@/utils/canvasUtils'
import { deepCopy } from '@/utils/utils'
const panelInit = ref(false)
const dvMainStore = dvMainStoreWithOut()
import { useAppearanceStoreWithOut } from '@/store/modules/appearance'
import { useAppStoreWithOut } from '@/store/modules/app'
const Entrances = defineAsyncComponent(
  () => import('@/views/component/embedded-iframe/Entrances.vue')
)
const appearanceStore = useAppearanceStoreWithOut()
const appStore = useAppStoreWithOut()

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

const getTabChildren = component =>
  component.propValue?.flatMap(tabItem => tabItem.componentData || []) || []

const syncTabPcDesign = (mobileComponent, component, viewInfos) => {
  // 使用当前运行时坐标，保留用户尚未保存的拖拽、缩放结果
  const geometry = new Map(
    getTabChildren(mobileComponent).map(child => [
      child.id,
      { mx: child.x, my: child.y, mSizeX: child.sizeX, mSizeY: child.sizeY }
    ])
  )
  // 在挂载新子组件前注册完整的图表配置，避免只更新样式而丢失字段槽等配置
  const syncedViews = Array.isArray(viewInfos) ? viewInfos : []
  syncedViews.forEach(viewInfo => {
    dvMainStore.addCanvasViewInfo(viewInfo.id, deepCopy(viewInfo))
  })
  mobileComponent.style = deepCopy(component.style)
  mobileComponent.commonBackground = deepCopy(component.commonBackground)
  mobileComponent.events = deepCopy(component.events)
  // 内容跟随 PC 更新，再按稳定的组件 id 恢复移动布局；新增组件走初始排版
  mobileComponent.propValue = deepCopy(component.propValue)
  getTabChildren(mobileComponent).forEach(child => {
    Object.assign(child, geometry.get(child.id) || {})
  })
  initTabMobileLayout(mobileComponent)
  // 仅清理当前移动组件树中已不存在的图表，移到其他位置的组件仍保留缓存
  geometry.forEach((_, id) => {
    if (!findComponentById(id)) dvMainStore.removeCanvasViewInfo(id)
  })
  nextTick(() => {
    // 替换组件数组后同步 Matrix 的占位信息，只重建当前 Tab 的子画布
    mobileComponent.propValue?.forEach(tabItem => {
      eventBus.emit('doCanvasInit-' + mobileComponent.id + '--' + tabItem.name)
    })
    syncedViews.forEach(viewInfo => {
      // 新组件由挂载链路初始化；已有组件沿用原来的取数/重绘入口
      if (!geometry.has(viewInfo.id)) return
      const child = findComponentById(viewInfo.id)
      if (child?.component === 'VQuery') {
        useEmitt().emitter.emit('renderChart-' + child.id, dvMainStore.canvasViewInfo[child.id])
      } else if (child?.component === 'UserView') {
        // 不传入 PC 查询参数，让原取数入口重新计算当前过滤、联动和下钻条件
        useEmitt().emitter.emit('calcData-' + child.id)
      }
    })
  })
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
        // 重新进入移动编辑时恢复 m*；旧数据缺失时才生成默认子布局
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
      if (mobileComponent.component === 'DeTabs') {
        syncTabPcDesign(mobileComponent, component, otherComponent)
        return
      }
      mobileComponent['style'] = component['style']
      mobileComponent['commonBackground'] = component['commonBackground']
      mobileComponent['events'] = component['events']
      mobileComponent['propValue'] = component['propValue']
      mobileViewStyleSwitch(otherComponent)
      if (mobileComponent.component === 'VQuery') {
        useEmitt().emitter.emit('renderChart-' + component.id, otherComponent)
      } else if (mobileComponent.component === 'UserView') {
        useEmitt().emitter.emit('calcData-' + component.id, otherComponent)
      }
    }
  }

  if (event.data.type === 'addToMobile') {
    const component = event.data.value
    checkItemPosition(component)
    // 首次加入移动布局的 Tab 也需初始化子布局，避免直接沿用 PC 坐标
    initTabMobileLayout(component)
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
  appearanceStore.setFontList()
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
  <Entrances v-if="appStore.getXpackValid" @initIframe="initIframe" />
</template>

<style lang="less" scoped>
.panel-mobile {
  width: 100vw;
  height: 100vh;
  overflow-y: auto;
}
</style>

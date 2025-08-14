import { deepCopy } from './utils'
import { divide, multiply } from 'mathjs'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { groupItemStyleAdaptor, groupSizeStyleAdaptor } from '@/utils/style'
import { nextTick } from 'vue'

const dvMainStore = dvMainStoreWithOut()
const { componentData, curComponentIndex, canvasStyleData } = storeToRefs(dvMainStore)

const needToChangeAttrs = [
  'top',
  'left',
  'width',
  'height',
  'fontSize',
  'activeFontSize',
  'letterSpacing'
]
const needToChangeDirectionAttrs = {
  width: ['left', 'width', 'fontSize', 'activeFontSize', 'letterSpacing'],
  height: ['top', 'height']
}

export function changeSizeWithScaleAdaptor(scale) {
  return changeComponentsSizeWithScale(scale)
}

export function changeSizeWithScale(scale) {
  return changeComponentsSizeWithScale(scale)
}

function changeComponentsSizeWithScaleCircle(componentDataCopy, scale) {
  if (!componentDataCopy || !Array.isArray(componentDataCopy)) {
    return
  }
  componentDataCopy?.forEach(component => {
    Object.keys(component.style).forEach(key => {
      if (needToChangeDirectionAttrs.width.includes(key)) {
        // 根据原来的比例获取样式原来的尺寸
        // 再用原来的尺寸 * 现在的比例得出新的尺寸
        if (!!component.style[key]) {
          component.style[key] = format(
            getOriginStyle(component.style[key], canvasStyleData.value.scale),
            scale
          )
        }
      } else if (needToChangeDirectionAttrs.height.includes(key)) {
        // 根据原来的比例获取样式原来的尺寸
        // 再用原来的尺寸 * 现在的比例得出新的尺寸
        component.style[key] = format(
          getOriginStyle(component.style[key], canvasStyleData.value.scaleHeight),
          scale
        )
      }
    })

    if (['Group'].includes(component.component)) {
      groupSizeStyleAdaptor(component)
      const parentStyle = component.style
      component.propValue.forEach(componentInner => {
        if (['DeTabs'].includes(componentInner.component)) {
          componentInner.propValue.forEach(tabItem => {
            changeComponentsSizeWithScaleCircle(tabItem.componentData, scale)
          })
        } else {
          changeComponentsSizeWithScaleCircle(componentInner.propValue, scale)
          groupItemStyleAdaptor(componentInner, parentStyle)
        }
      })
    } else if (['DeTabs'].includes(component.component)) {
      component.propValue.forEach(tabItem => {
        changeComponentsSizeWithScaleCircle(tabItem.componentData, scale)
      })
    }
    // 如果是分组组件 则要进行分组内部组件groupStyle进行深度计算
    // 计算逻辑 Group 中样式 * groupComponent.groupStyle[sonKey].
    if (['Group', 'DeTabs'].includes(component.component)) {
      try {
        nextTick(() => groupSizeStyleAdaptor(component))
      } catch (e) {
        // 旧Group适配
        console.error('group adaptor error:' + e)
      }
    }
  })
}

function changeComponentsSizeWithScale(scale) {
  const componentDataCopy = deepCopy(componentData.value)
  changeComponentsSizeWithScaleCircle(componentDataCopy, scale)
  dvMainStore.setComponentData(componentDataCopy)
  // 更新画布数组后，需要重新设置当前组件，否则在改变比例后，直接拖动圆点改变组件大小不会生效
  dvMainStore.setCurComponent({
    component: componentData.value[curComponentIndex.value],
    index: curComponentIndex.value
  })

  // 分开保存初始化宽高比例
  dvMainStore.setCanvasStyle({
    ...canvasStyleData.value,
    scaleWidth: scale,
    scaleHeight: scale,
    scale
  })
}
export function changeRefComponentsSizeWithScale(componentDataRef, canvasStyleDataRef, scale) {
  componentDataRef.forEach(component => {
    Object.keys(component.style).forEach(key => {
      if (needToChangeAttrs.includes(key)) {
        if (key === 'fontSize' && component.style[key] === '') return
        // 根据原来的比例获取样式原来的尺寸
        // 再用原来的尺寸 * 现在的比例得出新的尺寸
        component.style[key] = format(
          getOriginStyle(component.style[key], canvasStyleDataRef.scale),
          scale
        )
      }
    })
  })
  canvasStyleDataRef.scale = scale
}

export function changeRefComponentsSizeWithScalePointCircle(
  componentDataRef,
  canvasStyleDataRef,
  scaleWidth,
  scaleHeight,
  outScale
) {
  componentDataRef.forEach(component => {
    Object.keys(component.style).forEach(key => {
      if (needToChangeDirectionAttrs.width.includes(key)) {
        // 根据原来的比例获取样式原来的尺寸
        // 再用原来的尺寸 * 现在的比例得出新的尺寸
        component.style[key] = format(
          getOriginStyle(component.style[key], canvasStyleDataRef.scale || outScale),
          scaleWidth
        )
      } else if (needToChangeDirectionAttrs.height.includes(key)) {
        // 根据原来的比例获取样式原来的尺寸
        // 再用原来的尺寸 * 现在的比例得出新的尺寸
        component.style[key] = format(
          getOriginStyle(component.style[key], canvasStyleDataRef.scaleHeight || outScale),
          scaleHeight
        )
      }
    })
    if (component.component === 'Group') {
      changeRefComponentsSizeWithScalePointCircle(
        component.propValue,
        canvasStyleDataRef,
        scaleWidth,
        scaleHeight,
        outScale
      )
    }
  })
}

export function changeRefComponentsSizeWithScalePoint(
  componentDataRef,
  canvasStyleDataRef,
  scaleWidth,
  scaleHeight,
  outScale
) {
  changeRefComponentsSizeWithScalePointCircle(
    componentDataRef,
    canvasStyleDataRef,
    scaleWidth,
    scaleHeight,
    outScale
  )
  canvasStyleDataRef.scale = scaleWidth
  canvasStyleDataRef.scaleWidth = scaleWidth
  canvasStyleDataRef.scaleHeight = scaleHeight
}

const needToChangeAttrs2 = ['width', 'height', 'fontSize']
export function changeComponentSizeWithScale(component, scale = canvasStyleData.value.scale) {
  Object.keys(component.style).forEach(key => {
    if (needToChangeAttrs2.includes(key)) {
      if (key === 'fontSize' && component.style[key] === '') return
      component.style[key] = format(component.style[key], scale)
    }
  })
}

function format(value, scale) {
  return multiply(value, divide(parseFloat(scale), 100))
}

function getOriginStyle(value = 0, scale) {
  if (!value) {
    value = 0
  }
  return divide(value, divide(parseFloat(scale), 100))
}

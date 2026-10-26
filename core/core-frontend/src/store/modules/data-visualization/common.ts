import { storeToRefs } from 'pinia'
import { dvMainStoreWithOut } from './dvMain'
const dvMainStore = dvMainStoreWithOut()
const { curComponent, componentData } = storeToRefs(dvMainStore)

export const getComponentById = (componentId?) => {
  const curInfo = getCurInfo(componentId)
  if (curInfo) {
    return curInfo.targetComponent
  } else return null
}

export const getCurInfo = (componentId?) => {
  if (componentId) {
    return getCurInfoById(componentId)
  } else if (curComponent.value) {
    return getCurInfoById(curComponent.value.id)
  }
}

const findComponentInfo = (components, componentId, currentTabIndex = 0) => {
  if (!Array.isArray(components)) {
    return null
  }
  for (let index = 0; index < components.length; index++) {
    const component = components[index]
    if (!component) {
      continue
    }
    if (component.id === componentId) {
      return {
        index,
        tabIndex: currentTabIndex,
        targetComponent: component,
        componentData: components
      }
    }
    if (component.component === 'Group' && Array.isArray(component.propValue)) {
      const info = findComponentInfo(component.propValue, componentId, currentTabIndex)
      if (info) {
        return info
      }
    }
    if (component.component === 'DeTabs' && Array.isArray(component.propValue)) {
      for (let tabIndex = 0; tabIndex < component.propValue.length; tabIndex++) {
        const tabItem = component.propValue[tabIndex]
        const info = findComponentInfo(tabItem?.componentData, componentId, tabIndex)
        if (info) {
          return info
        }
      }
    }
  }
  return null
}

export const getCurInfoById = curComponentId => {
  if (!curComponentId) {
    return null
  }
  return findComponentInfo(componentData.value, curComponentId)
}

export const componentArraySort = (sortArray, direction = 'down') => {
  sortArray.sort((a, b) => {
    // 在 componentData 中找到 a 和 b 的索引位置
    const indexA = componentData.value.findIndex(item => item.id === a.id)
    const indexB = componentData.value.findIndex(item => item.id === b.id)
    // 按照索引位置进行排序
    return direction === 'down' ? indexB - indexA : indexA - indexB
  })
}

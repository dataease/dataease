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

export const getCurInfoById = curComponentId => {
  if (curComponentId) {
    let curIndex = 0
    let curTabIndex = 0
    let curComponentData = componentData.value
    let targetComponent = null
    componentData.value.forEach((component, index) => {
      if (curComponentId === component.id) {
        curIndex = index
        targetComponent = component
      }
      if (component.component === 'Group') {
        component.propValue.forEach((subComponent, subIndex) => {
          if (curComponentId === subComponent.id) {
            curIndex = subIndex
            targetComponent = subComponent
            curComponentData = component.propValue
          }
        })
      }
      if (component.component === 'DeTabs') {
        component.propValue.forEach((tabItem, tabIndex) => {
          curTabIndex = tabIndex
          tabItem.componentData.forEach((tabComponent, subIndex) => {
            if (curComponentId === tabComponent.id) {
              curIndex = subIndex
              targetComponent = tabComponent
              curComponentData = tabItem.componentData
            }
          })
        })
      }
    })
    return {
      index: curIndex,
      tabIndex: curTabIndex,
      targetComponent: targetComponent,
      componentData: curComponentData
    }
  }
}

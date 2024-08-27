import { storeToRefs } from 'pinia'
import { dvMainStoreWithOut } from './dvMain'
const dvMainStore = dvMainStoreWithOut()
const { curComponent, componentData } = storeToRefs(dvMainStore)

export const getCurInfo = () => {
  if (curComponent.value) {
    const curComponentId = curComponent.value.id
    let curIndex = 0
    let curTabIndex = 0
    let curComponentData = componentData.value
    componentData.value.forEach((component, index) => {
      if (curComponentId === component.id) {
        curIndex = index
      }
      if (component.component === 'Group') {
        component.propValue.forEach((subComponent, subIndex) => {
          if (curComponentId === subComponent.id) {
            curIndex = subIndex
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
              curComponentData = tabItem.componentData
            }
          })
        })
      }
    })
    return {
      index: curIndex,
      tabIndex: curTabIndex,
      componentData: curComponentData
    }
  }
}

import { toPng } from 'html-to-image'
import store from "@/store";

export function toPngUrl(refContainer, callBack) {
  toPng(refContainer)
    .then(dataUrl => {
      callBack(dataUrl)
    })
    .catch(error => {
      console.error('oops, toPngUrl went wrong!', error)
    })
}


export function dataURLToBlobCheck(dataurl) { // ie 图片转格式
  const arr = dataurl.split(',')
  const mime = arr[0].match(/:(.*?);/)[1]
  const bstr = atob(arr[1])
  let n = bstr.length
  const u8arr = new Uint8Array(n)
  while (n--) {
    u8arr[n] = bstr.charCodeAt(n)
  }
  return new Blob([u8arr], { type: mime })
}

export function colorReverseCheck(OldColorValue) {
  OldColorValue = '0x' + OldColorValue.replace(/#/g, '')
  const str = '000000' + (0xFFFFFF - OldColorValue).toString(16)
  return '#' + str.substring(str.length - 6, str.length)
}

export function imgUrlTransCheck(url) {
  if (url && typeof url === 'string' && url.indexOf('static-resource') > -1) {
    return process.env.VUE_APP_BASE_API + url.replace('/static-resource', 'static-resource')
  } else {
    return url
  }
}

export function getNowCanvasComponentDataCheck(canvasId, showPosition) {
  if (showPosition && (showPosition.includes('email-task') || showPosition.includes('multiplexing'))) {
    return store.state.previewComponentData.filter(item => item.canvasId === canvasId)
  } else {
    return store.state.componentData.filter(item => item.canvasId === canvasId)
  }
}

export function findCurComponentIndexCheck(componentData, curComponent) {
  let curIndex = 0
  for (let index = 0; index < componentData.length; index++) {
    const element = componentData[index]
    if (element.id && element.id === curComponent.id) {
      curIndex = index
      break
    }
  }
  return curIndex
}

export function deleteTreeNodeCheck(nodeId, tree, nodeTarget) {
  if (!nodeId || !tree || !tree.length) {
    return
  }
  for (let i = 0, len = tree.length; i < len; i++) {
    if (tree[i].id === nodeId) {
      if (nodeTarget) {
        nodeTarget['target'] = tree[i]
      }
      tree.splice(i, 1)
      return
    } else if (tree[i].children && tree[i].children.length) {
      deleteTreeNode(nodeId, tree[i].children, nodeTarget)
    }
  }
}

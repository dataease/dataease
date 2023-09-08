const attrsMap = { brColor: 'borderColor', wordColor: 'color', innerBgColor: 'backgroundColor' }
const styleAttrs = ['innerBgColor', 'wordColor', 'brColor']
function timeDateRangeWidget(nodeCache, name, value) {
  const classList = ['.el-range-input', '.el-range-separator']
  classList.forEach(ele => {
    const nodeList = nodeCache.querySelectorAll(ele)
    if (!nodeList.length) return
    nodeList.forEach(ele => {
      ele.style[attrsMap[name]] = value
    })
  })
}
function textInputWidget(nodeCache, name, value) {
  const groupAppend = nodeCache.querySelector('.el-input-group__append')
  groupAppend.style[attrsMap[name]] = value
  if (name === 'brColor') {
    groupAppend.style.borderLeft = 'none'
  }
}

function textSelectGridWidget(nodeCache, name, value) {
  if (name === 'innerBgColor') {
    nodeCache.querySelector('.list').style.backgroundColor = value
  }
  if (name === 'wordColor') {
    const elRadio = nodeCache.querySelectorAll('.el-radio')
    const elCheckbox = nodeCache.querySelectorAll('.el-checkbox')
    if (elRadio.length) {
      elRadio.forEach(ele => {
        ele.style.color = value
      })
    }
    if (elCheckbox.length) {
      elCheckbox.forEach(ele => {
        ele.style.color = value
      })
    }
  }
}

function textSelectTreeWidget(nodeCache, style) {
  textSelectWidget(nodeCache, style)
}

function textSelectWidget(nodeCache, style) {
  const elTag = nodeCache.querySelectorAll('.el-tag.el-tag--info')
  if (elTag.length) {
    elTag.forEach(item => {
      item.style.flexWrap = 'wrap'
      item.style.padding = '0'
      const textNode = item.querySelector('.el-select__tags-text')
      const closeNode = item.querySelector('.el-tag__close')
      textNode.style.width = '100%'
      item.style.position = 'relative'
      textNode.style.padding = '0 20px 0 8px'
      textNode.style.borderRadius = '3px'
      if (closeNode) {
        closeNode.style.position = 'absolute'
        closeNode.style.top = '60%'
        closeNode.style.transform = 'translateY(-50%)'
        closeNode.style.right = '2px'
      }
      styleAttrs.forEach((ele) => {
        if (ele !== 'brColor' && closeNode) {
          closeNode.style[attrsMap[ele]] = style[ele]
        } else {
          item.style[attrsMap[ele]] = style[ele]
        }
        textNode.style[attrsMap[ele]] = style[ele]
      })
    })
  }
}

function handlerInputStyle(node, style) {
  if (!node) return
  styleAttrs.forEach(ele => {
    node.style[attrsMap[ele]] = style[ele]
  })
}

export {
  attrsMap,
  styleAttrs,
  timeDateRangeWidget,
  textInputWidget,
  textSelectGridWidget,
  textSelectTreeWidget,
  textSelectWidget,
  handlerInputStyle
}

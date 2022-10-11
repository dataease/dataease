import permission from '@/directive/Permission'
import { setLayout } from '@/utils/LayoutUtil'

export const left2RightDrag = {
  inserted(el, binding) {
    const value = binding.value
    el.onmousedown = function(e) {
      const init = e.clientX
      const parent = el.parentNode
      const initWidth = parent.offsetWidth
      document.onmousemove = function(e) {
        const end = e.clientX
        const newWidth = end - init + initWidth
        if (newWidth < document.body.clientWidth - 10 && newWidth > 10) {
          parent.style.width = newWidth + 'px'
        }
      }
      document.onmouseup = function() {
        value && setLayout(value, parent.style.width)

        document.onmousemove = document.onmouseup = null
      }
    }
  }
}

export const right2LeftDrag = {
  inserted(el, binding) {
    el.onmousedown = function(e) {
      const init = e.clientX
      const parent = el.parentNode
      const initWidth = parent.offsetWidth
      document.onmousemove = function(e) {
        const end = e.clientX
        const newWidth = initWidth - (end - init)
        if (newWidth < document.body.clientWidth - 10 && newWidth > 10) {
          parent.style.width = newWidth + 'px'
        }
      }
      document.onmouseup = function() {
        document.onmousemove = document.onmouseup = null
      }
    }
  }
}

export const bottom2TopDrag = {
  inserted(el, binding) {
    el.onmousedown = function(e) {
      const init = e.clientY
      const parent = el.parentNode
      const initHeight = parent.offsetHeight
      document.onmousemove = function(e) {
        const end = e.clientY
        const newHeight = initHeight - (end - init)
        if (newHeight < document.body.clientHeight - 10 && newHeight > 10) {
          parent.style.height = newHeight + 'px'
        }
      }
      document.onmouseup = function() {
        document.onmousemove = document.onmouseup = null
      }
    }
  }
}

const closePress = {
  inserted: function(el) {
    el.querySelector('.el-drawer__close-btn').onmousedown = function(e) {
      e.currentTarget.style.setProperty('background', '#d2d3d4', 'important')
    }
    el.querySelector('.el-drawer__close-btn').onmouseup = function(e) {
      e.currentTarget.style.background = 'none'
    }
  }
}

const btnPress = {
  update: function(el, binding) {
    el.onmousedown = function(e) {
      e.currentTarget.style.setProperty('background', binding.value || '#EFF0F1', 'important')
    }
    el.onmouseup = function(e) {
      e.currentTarget.style.background = 'none'
    }
  }
}

const customStyle = {
  inserted: function(el, binding) {
    const label = el.querySelector('.el-checkbox__label')
    if (label) {
      if (label.getAttribute('data-color') === binding.value.wordColor) {
        return
      }
      label.style.setProperty('color', binding.value.wordColor, 'important')
      label.setAttribute('data-color', binding.value.wordColor)
    }
  }
}

export default {
  install(Vue) {
    // Vue.directive('data-permission', dataPermission)
    Vue.directive('permission', permission)
    Vue.directive('left-to-right-drag', left2RightDrag)
    Vue.directive('right-to-left-drag', right2LeftDrag)
    Vue.directive('bottom-to-top-drag', bottom2TopDrag)
    Vue.directive('closePress', closePress)
    Vue.directive('btnPress', btnPress)
    Vue.directive('customStyle', customStyle)
  }
}


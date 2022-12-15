import { isServer } from './util'

export const on = (function() {
  if (!isServer) {
    if (document && document.addEventListener) {
      return function(element, event, handler) {
        if (element && event && handler) {
          element.addEventListener(event, handler, false)
        }
      }
    } else {
      return function(element, event, handler) {
        if (element && event && handler) {
          element.attachEvent('on' + event, handler)
        }
      }
    }
  }
})()
export const off = (function() {
  if (!isServer) {
    if (document && document.removeEventListener) {
      return function(element, event, handler) {
        if (element && event) {
          element.removeEventListener(event, handler, false)
        }
      }
    } else {
      return function(element, event, handler) {
        if (element && event) {
          element.detachEvent('on' + event, handler)
        }
      }
    }
  }
})()

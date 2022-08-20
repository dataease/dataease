import bus from '@/utils/bus'
import SockJS from 'sockjs-client'
import Stomp from 'stompjs'
import store from '@/store'

class DeWebsocket {
  constructor() {
    this.ws_url = '/websocket'
    this.client = null
    this.channels = [
      {
        topic: '/web-msg-topic',
        event: 'web-msg-topic-call'
      }
    ]
    this.timer = null
    this.initialize()
  }

  initialize() {
    this.connection()
    const _this = this
    this.timer = this.isLoginStatu() && setInterval(() => {
      this.isLoginStatu() || this.destroy()
      try {
        _this.client && _this.client.send('heart detection')
      } catch (error) {
        console.error('Disconnection reconnection...')
        _this.connection()
      }
    }, 5000)
  }

  destroy() {
    this.timer && clearInterval(this.timer)
    this.disconnect()
    return true
  }

  reconnect() {
    this.initialize()
  }

  isLoginStatu() {
    return store.state && store.state.user && store.state.user.user && store.state.user.user.userId
  }

  connection() {
    if (!this.isLoginStatu()) {
      return
    }
    const socket = new SockJS(this.ws_url + '?userId=' + store.state.user.user.userId)
    /* const socket = new SockJS('http://localhost:8081' + this.ws_url) */

    this.client = Stomp.over(socket)
    const heads = {
      /* Authorization: '', */
      userId: store.state.user.user.userId
    }

    this.client.connect(
      heads,
      res => {
        // 连接成功 订阅所有主题
        this.subscribe()
      },
      err => {
        // 连接失败 打印错误信息
        console.error(err)
      }
    ).bind(this)
  }
  subscribe() {
    this.channels.forEach(channel => {
      this.client.subscribe('/user/' + store.state.user.user.userId + channel.topic, res => {
        res && res.body && bus.$emit(channel.event, res.body)
      })
    })
  }
  disconnect() {
    this.client && this.client.disconnect()
  }
}

const result = new DeWebsocket()
export const getSocket = () => {
  return result
}
export default {
  install(Vue) {
    // 使用$$前缀，避免与Element UI的冲突
    Vue.prototype.$deWebsocket = result
  }
}


import SockJS from 'sockjs-client/dist/sockjs.min.js'
import Stomp from 'stompjs'
import { useCache } from '@/hooks/web/useCache'
import { useEmitt } from '@/hooks/web/useEmitt'
const { wsCache } = useCache()
let stompClient: Stomp.Client
let timeInterval: NodeJS.Timer | null = null
import dev from '../../config/dev'
const env = import.meta.env
const basePath = env.VITE_API_BASEPATH

export default {
  install() {
    const channels = [
      {
        topic: '/task-export-topic',
        event: 'task-export-topic-call'
      },
      {
        topic: '/report-notice',
        event: 'report-notice-call'
      }
    ]
    function isLoginStatus() {
      return wsCache.get('user.token') && wsCache.get('user.uid')
    }

    function connection() {
      if (!isLoginStatus()) {
        return
      }
      if (stompClient && stompClient.connected) {
        return
      }
      let prefix = '/'
      if (window.DataEaseBi?.baseUrl) {
        prefix = window.DataEaseBi.baseUrl
      } else {
        const href = window.location.href
        prefix = href.substring(0, href.indexOf('#'))
        if (env.MODE === 'dev') {
          prefix = dev.server.proxy[basePath].target + '/'
        }
      }
      const socket = new SockJS(prefix + 'websocket?userId=' + wsCache.get('user.uid'))
      stompClient = Stomp.over(socket)
      const heads = {
        userId: wsCache.get('user.uid')
      }
      stompClient.connect(
        heads,
        () => {
          channels.forEach(channel => {
            stompClient.subscribe('/user/' + wsCache.get('user.uid') + channel.topic, res => {
              res && res.body && useEmitt().emitter.emit(channel.event, res.body)
            })
          })
        },
        error => {
          console.log('连接失败: ' + error)
        }
      )
    }

    function disconnect() {
      if (stompClient && stompClient.connected) {
        stompClient.disconnect(
          function () {
            console.log('断开连接')
          },
          function (error) {
            console.log('断开连接失败: ' + error)
          }
        )
      }
    }

    function initialize() {
      connection()
      timeInterval = setInterval(() => {
        if (!isLoginStatus()) {
          disconnect()
          return
        }
        if (!stompClient || !stompClient.connected) {
          connection()
        }
      }, 5000)
    }
    initialize()
  }
}

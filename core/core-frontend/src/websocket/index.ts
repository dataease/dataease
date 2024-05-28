import SockJS from 'sockjs-client/dist/sockjs.min.js'
import Stomp from 'stompjs'
import eventBus from '@/utils/eventBus'
import { useCache } from '@/hooks/web/useCache'
import { ref } from 'vue'
const { wsCache } = useCache()
let stompClient: Stomp.Client
let timeInterval: NodeJS.Timer | null = null
const isDisconnect = ref(true)

export default {
  install() {
    const channels = [
      {
        topic: '/task-export-topic',
        event: 'task-export-topic-call'
      }
    ]
    let prefix = '/'
    if (window.DataEaseBi?.baseUrl) {
      prefix = window.DataEaseBi.baseUrl
    } else {
      const href = window.location.href
      prefix = href.substring(0, href.indexOf('#'))
    }

    function isLoginStatus() {
      return wsCache.get('user.token')
    }

    function connection() {
      if (!isLoginStatus()) {
        return
      }
      let prefix = '/'
      if (window.DataEaseBi?.baseUrl) {
        prefix = window.DataEaseBi.baseUrl
      } else {
        const href = window.location.href
        prefix = href.substring(0, href.indexOf('#'))
      }
      const socket = new SockJS(prefix + 'websocket?userId=' + wsCache.get('user.uid'))
      stompClient = Stomp.over(socket)
      const heads = {
        userId: wsCache.get('user.uid')
      }
      stompClient.connect(
        heads,
        res => {
          isDisconnect.value = false
          channels.forEach(channel => {
            stompClient.subscribe('/user/' + wsCache.get('user.uid') + channel.topic, res => {
              res && res.body && eventBus.emit(channel.event, res.body)
            })
          })
        },
        error => {
          console.log('连接失败: ' + error)
        }
      )
    }

    function disconnect() {
      if (!isDisconnect.value && stompClient != undefined) {
        stompClient.disconnect(
          function () {
            isDisconnect.value = true
          },
          function (error) {
            isDisconnect.value = false
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
        if (isDisconnect.value) {
          connection()
        }
        try {
          stompClient.send('heart detection')
        } catch (error) {
          connection()
        }
      }, 5000)
    }
    initialize()
  }
}

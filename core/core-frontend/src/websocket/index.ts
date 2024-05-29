import SockJS from 'sockjs-client/dist/sockjs.min.js'
import Stomp from 'stompjs'
import eventBus from '@/utils/eventBus'
import { useCache } from '@/hooks/web/useCache'
const { wsCache } = useCache()
let stompClient: Stomp.Client
let timeInterval: NodeJS.Timer | null = null
const basePath = import.meta.env.VITE_API_BASEPATH

export default {
  install() {
    const channels = [
      {
        topic: '/task-export-topic',
        event: 'task-export-topic-call'
      }
    ]
    function isLoginStatus() {
      return wsCache.get('user.token')
    }

    function connection() {
      if (!isLoginStatus()) {
        return
      }
      if (stompClient !== null && stompClient != undefined && stompClient.connected) {
        return
      }
      const socket = new SockJS(basePath + '/websocket?userId=' + wsCache.get('user.uid'))
      stompClient = Stomp.over(socket)
      const heads = {
        userId: wsCache.get('user.uid')
      }
      stompClient.connect(
        heads,
        res => {
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
      if (stompClient !== null && stompClient != undefined && !stompClient.connected) {
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
        if (stompClient !== null && stompClient != undefined && !stompClient.connected) {
          connection()
        }
      }, 5000)
    }
    initialize()
  }
}

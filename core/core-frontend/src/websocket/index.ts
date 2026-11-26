import SockJS from 'sockjs-client/dist/sockjs.min.js'
import Stomp from 'stompjs'
import { setConversationSocket } from './conversation'
import { useCache } from '@/hooks/web/useCache'
import { useEmitt } from '@/hooks/web/useEmitt'
const { wsCache } = useCache()
let stompClient: Stomp.Client | undefined
let connectionIdentity: string | undefined
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
      if (wsCache.get('app.desktop')) {
        return true
      }
      return wsCache.get('user.token') && wsCache.get('user.uid')
    }

    const identity = () =>
      wsCache.get('app.desktop')
        ? 'desktop'
        : wsCache.get('user.uid') + ':' + wsCache.get('user.token')

    function connection() {
      if (!isLoginStatus()) {
        return
      }
      if (stompClient) {
        return
      }
      let prefix = '/'
      if (window.DataEaseBi?.baseUrl) {
        prefix = window.DataEaseBi.baseUrl
      } else {
        prefix = location.origin + location.pathname
        if (env.MODE === 'dev') {
          prefix = dev.server.proxy[basePath].target + '/'
        }
      }
      if (!prefix.endsWith('/')) {
        prefix += '/'
      }
      const userId = wsCache.get('app.desktop') ? 1 : wsCache.get('user.uid')
      const query = wsCache.get('app.desktop')
        ? `userId=${userId}`
        : `token=${encodeURIComponent(wsCache.get('user.token') || '')}`
      const socket = new SockJS(prefix + 'websocket?' + query)
      const client = Stomp.over(socket)
      client.debug = () => {}
      stompClient = client
      connectionIdentity = identity()
      const heads = {
        userId: userId
      }
      client.connect(
        heads,
        () => {
          if (client !== stompClient) return
          setConversationSocket(client)
          channels.forEach(channel => {
            client.subscribe('/user/' + userId + channel.topic, res => {
              res && res.body && useEmitt().emitter.emit(channel.event, res.body)
            })
          })
        },
        () => {
          if (client === stompClient) disconnect()
        }
      )
    }

    function disconnect() {
      const client = stompClient
      stompClient = undefined
      connectionIdentity = undefined
      setConversationSocket()
      if (client?.connected) client.disconnect(() => {})
      else if (client && client.ws.readyState < WebSocket.CLOSING) client.ws.close()
    }

    function initialize() {
      connection()
      setInterval(() => {
        if (!isLoginStatus()) {
          disconnect()
          return
        }
        if (stompClient && connectionIdentity !== identity()) disconnect()
        connection()
      }, 5000)
    }
    initialize()
  }
}

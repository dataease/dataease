import SockJS from 'sockjs-client/dist/sockjs.min.js'
import Stomp from 'stompjs'
import eventBus from '@/utils/eventBus'
let stompClient: Stomp.Client

export default {
  install() {
    const channels = [
      {
        topic: '/task-export-topic',
        event: 'task-export-topic-call'
      }
    ]

    // const socket = new SockJS('http://localhost:8100' + '/websocket' + '?userId=1')
    // stompClient = Stomp.over(socket)
    // const heads = {
    //   userId: 1
    // }
    // stompClient.connect(
    //   heads,
    //   res => {
    //     console.log('连接成功: ' + res)
    //     channels.forEach(channel => {
    //       stompClient.subscribe('/user/' + 1 + channel.topic, res => {
    //         res && res.body && eventBus.emit(channel.event, res.body)
    //       })
    //     })
    //   },
    //   error => {
    //     console.log('连接失败: ' + error)
    //   }
    // )
  }
}

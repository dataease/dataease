import {
  bindConversationSubscription,
  getConversationEvents,
  type AiConversationEvents,
  type AiConversationSession
} from './aiConversation'
import { subscribeConversationSocket } from '@/websocket/conversation'

type Notice = { subscription: string } | { read: true; executionId: string } | { denied: true }
type Frame = Notice | AiConversationEvents

export interface ConversationDelivery {
  session: AiConversationSession
  executionId: string
  cursor: () => number
  update: (page: AiConversationEvents) => void
  signal: AbortSignal
}

// The callback is private to the STOMP session/subscription. HTTP binds it to one verified execution.
export function followConversationSocket(options: ConversationDelivery): Promise<boolean> {
  const { session, executionId, signal, update, cursor } = options
  return new Promise(resolve => {
    if (signal.aborted) return resolve(true)
    let closed = false
    let timer: number
    let unsubscribe: (() => void) | undefined
    const read = new AbortController()
    const finish = (done: boolean) => {
      if (closed) return
      closed = true
      read.abort()
      signal.removeEventListener('abort', abort)
      window.clearTimeout(timer)
      unsubscribe?.()
      resolve(done)
    }
    const abort = () => finish(true)
    const deadline = (ms: number) => {
      window.clearTimeout(timer)
      timer = window.setTimeout(() => finish(false), ms)
    }
    const catchUp = () =>
      getConversationEvents(session.sessionId, executionId, cursor(), read.signal)
    const receive = async (frame: Frame) => {
      if (closed) return
      if ('subscription' in frame) {
        await bindConversationSubscription(
          session,
          executionId,
          frame.subscription,
          cursor(),
          read.signal
        )
        return
      }
      let page: AiConversationEvents
      if ('denied' in frame) {
        // An authenticated read distinguishes loss of permission from loss of this socket route.
        page = await catchUp()
      } else {
        if (frame.executionId !== executionId) return
        if ('read' in frame) page = await catchUp()
        else {
          if (
            frame.sessionId !== session.sessionId ||
            frame.contextVersion !== session.contextVersion
          )
            return
          if (frame.cursor < cursor()) return
          const events = [...frame.events].sort((a, b) => a.sequence - b.sequence)
          let last = cursor()
          let gap = false
          for (const event of events) {
            if (event.sequence <= last) continue
            if (event.sequence !== ++last) {
              gap = true
              break
            }
          }
          page = gap || last < frame.cursor ? await catchUp() : { ...frame, events }
        }
      }
      if (closed || page.cursor < cursor()) return
      update(page)
      if (page.status !== 'RUNNING') finish(true)
      else if ('denied' in frame) finish(false)
    }
    // Serialize initial catch-up, push batches and gap reads so a late response cannot rewind a turn.
    let handling = Promise.resolve()
    unsubscribe = subscribeConversationSocket(
      body => {
        if (closed) return
        deadline(8500)
        handling = handling.then(() => receive(JSON.parse(body))).catch(() => finish(false))
      },
      () => finish(false)
    )
    if (!unsubscribe) return finish(false)
    signal.addEventListener('abort', abort, { once: true })
    deadline(2500)
  })
}

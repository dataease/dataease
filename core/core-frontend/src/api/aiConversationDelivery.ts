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
    let binding: string | undefined
    let lastReceipt = 0
    let timer: number
    const read = new AbortController()
    const finish = (done: boolean) => {
      if (closed) return
      closed = true
      read.abort()
      signal.removeEventListener('abort', abort)
      window.clearTimeout(timer)
      subscription?.close()
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
        binding = frame.subscription
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
      else if (binding && Date.now() - lastReceipt >= 3000) {
        lastReceipt = Date.now()
        subscription?.received(binding)
      }
    }
    // Serialize initial catch-up, push batches and gap reads so a late response cannot rewind a turn.
    let handling = Promise.resolve()
    const subscription = subscribeConversationSocket(
      body => {
        if (closed) return
        deadline(8500)
        handling = handling.then(() => receive(JSON.parse(body))).catch(() => finish(false))
      },
      () => finish(false)
    )
    if (!subscription) return finish(false)
    signal.addEventListener('abort', abort, { once: true })
    deadline(2500)
  })
}

// Only a successful authenticated read confirms the state of a disconnected execution.
interface ConversationRecovery extends ConversationDelivery {
  modelDeadline?: number
  readFirst: boolean
  recovering: (value: boolean) => void
  terminalReadError: (error: unknown) => boolean
}

export async function followConversation(options: ConversationRecovery): Promise<boolean> {
  const { modelDeadline, readFirst, recovering, terminalReadError } = options
  const { session, executionId, cursor, signal, update } = options
  const wait = (ms: number) => new Promise(resolve => window.setTimeout(resolve, ms))
  const socket = () =>
    followConversationSocket({
      ...options,
      update: page => {
        recovering(false)
        update(page)
      }
    })
  if (!readFirst && (await socket())) return true
  let disconnectedAt = Date.now()
  let nextSocket = disconnectedAt + 4000
  let delay = 500
  recovering(true)
  while (!signal.aborted) {
    const read = new AbortController()
    const abort = () => read.abort()
    signal.addEventListener('abort', abort, { once: true })
    const timer = window.setTimeout(abort, 2500)
    const after = cursor()
    let page: AiConversationEvents
    try {
      page = await getConversationEvents(session.sessionId, executionId, after, read.signal)
    } catch (error) {
      if (signal.aborted) return true
      if (terminalReadError(error)) throw error
      recovering(true)
      const remaining = Math.min(disconnectedAt + 30000, modelDeadline || Infinity) - Date.now()
      if (remaining <= 0) return false
      delay = Math.min(2000, delay * 2)
      await wait(Math.min(delay, remaining))
      continue
    } finally {
      window.clearTimeout(timer)
      signal.removeEventListener('abort', abort)
    }
    if (signal.aborted) return true
    recovering(false)
    update(page)
    if (page.status !== 'RUNNING') return true
    disconnectedAt = Date.now()
    delay = page.cursor > after ? 500 : Math.min(2000, delay * 2)
    if (Date.now() >= nextSocket) {
      if (await socket()) return true
      disconnectedAt = Date.now()
      nextSocket = disconnectedAt + 4000
      recovering(true)
      continue
    }
    await wait(delay)
  }
  return true
}

import type Stomp from 'stompjs'

let connection: Stomp.Client | undefined
const listeners = new Set<() => void>()

export function setConversationSocket(client?: Stomp.Client) {
  if (connection === client) return
  connection = client
  for (const listener of [...listeners]) listener()
}

export function subscribeConversationSocket(
  receive: (body: string) => void,
  disconnected: () => void
) {
  const client = connection
  if (!client?.connected) return
  let subscription: ReturnType<Stomp.Client['subscribe']>
  try {
    subscription = client.subscribe('/app/ai/conversation', message => receive(message.body))
  } catch {
    return
  }
  listeners.add(disconnected)
  return {
    received(binding: string) {
      if (client.connected)
        client.send(
          '/app/ai/conversation/received',
          { 'content-type': 'application/json' },
          JSON.stringify({ subscription: binding })
        )
    },
    close() {
      listeners.delete(disconnected)
      try {
        if (client.connected) subscription.unsubscribe()
      } catch {
        // The transport can close before STOMP has observed the disconnect.
      }
    }
  }
}

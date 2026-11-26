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
  const subscription = client.subscribe('/app/ai/conversation', message => receive(message.body))
  listeners.add(disconnected)
  return () => {
    listeners.delete(disconnected)
    if (client.connected) subscription.unsubscribe()
  }
}

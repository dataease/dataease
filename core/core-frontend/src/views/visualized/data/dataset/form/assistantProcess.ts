import type { AiConversationEvents, AiConversationTool } from '@/api/aiConversation'

export type ProcessEntry =
  | { sequence: number; text: string; tool?: never }
  | {
      sequence: number
      tool: AiConversationTool
      error?: string
      open?: boolean
    }

export interface AnswerProcess {
  entries: ProcessEntry[]
  open?: boolean
}

export function applyProcessEvent(
  turn: { process: AnswerProcess; answer: string },
  event: AiConversationEvents['events'][number]
) {
  if (event.type === 'TEXT') {
    turn.answer += event.text ?? ''
    return
  }
  const tool = event.tool
  if (event.type !== 'TOOL' || !tool) return
  const entries = turn.process.entries
  const entry = entries.find(item => item.tool?.callId === tool.callId)
  if (entry?.tool) {
    entry.tool = tool
    entry.error = event.error
    return
  }
  if (turn.answer) {
    entries.push({ sequence: event.sequence, text: turn.answer })
    turn.answer = ''
  }
  entries.push({ sequence: event.sequence, tool, error: event.error })
}

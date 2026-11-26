import type { AiConversationEvents, AiConversationTool } from '@/api/aiConversation'

export type ProcessEntry =
  | { kind: 'text'; sequence: number; text: string; tool?: never }
  | {
      kind: 'tool'
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
  if (entry?.kind === 'tool') {
    entry.tool = { ...tool, stepNumber: tool.stepNumber ?? entry.tool.stepNumber }
    entry.error = event.error
    return
  }
  if (turn.answer) {
    entries.push({ kind: 'text', sequence: event.sequence, text: turn.answer })
    turn.answer = ''
  }
  const stepNumber = tool.stepNumber ?? entries.filter(item => item.kind === 'tool').length + 1
  entries.push({
    kind: 'tool',
    sequence: event.sequence,
    tool: { ...tool, stepNumber },
    error: event.error
  })
}

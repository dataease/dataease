export interface AnswerFailure {
  step:
    | 'progress_step'
    | 'model_step'
    | 'tool_step'
    | 'execution_step'
    | 'validation_step'
    | 'submission_step'
    | 'stopped_step'
  reason: string
  requestId?: string
  tool?: string
  retryMessage?: string
  retryRequestId?: string
}

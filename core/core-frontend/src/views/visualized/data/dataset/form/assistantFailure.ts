export interface AnswerFailure {
  step:
    | 'progress_step'
    | 'model_step'
    | 'tool_step'
    | 'execution_step'
    | 'validation_step'
    | 'submission_step'
  reason: string
  tool?: string
  retryMessage?: string
}

import request from '@/config/axios'

export type MetadataAiKind = 'RELATION' | 'SEMANTIC'

export interface MetadataAiTask {
  id: string
  status: 'QUEUED' | 'RUNNING' | 'SUCCESS' | 'PARTIAL' | 'FAILED'
  written: number
  skipped: number
  failed: number
}

const path = (id: string, kind: MetadataAiKind) => `/datasource/metadata/${id}/ai/${kind}`

export const getMetadataAiTask = (id: string, kind: MetadataAiKind, taskId?: string) =>
  request.get<Promise<{ data: MetadataAiTask | null }>>({
    url: path(id, kind),
    params: { taskId }
  })

export const submitMetadataAiTask = (id: string, kind: MetadataAiKind, tables: string[]) =>
  request.post<Promise<{ data: MetadataAiTask }>>({ url: path(id, kind), data: tables })

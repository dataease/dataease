import { DataEaseError, errorBody } from '@/config/axios/error'

/** Retry only after explicit confirmation, against the version the user just confirmed. */
export const saveWithConflict = async <T, R>(
  data: T & { expectedContentId?: string | null },
  save: (data: T) => Promise<R>,
  confirmOverwrite: () => Promise<unknown>
): Promise<R> => {
  for (;;) {
    try {
      return await save(data)
    } catch (error) {
      if (!(error instanceof DataEaseError) || error.httpStatus !== 409 || error.code !== 50005)
        throw error
      const details = errorBody(error.response?.data).data
      if (!details || typeof details !== 'object' || !('currentContentId' in details)) throw error
      const current = details.currentContentId
      if (current !== null && typeof current !== 'string') throw error
      await confirmOverwrite()
      data.expectedContentId = current
    }
  }
}

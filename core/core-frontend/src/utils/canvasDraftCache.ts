import { useCache } from '@/hooks/web/useCache'

interface CanvasDraftSnapshot {
  dvInfo: {
    id: string | number
    contentId?: string | number | null
    materialBundle?: unknown
    [key: string]: any
  }
  componentData: any[]
  canvasStyleData: Record<string, any>
  canvasViewInfo: Record<string, any>
  cacheViewIdInfo: {
    snapshotCacheViewCalc: any[]
    snapshotCacheViewRender: any[]
  }
  [key: string]: any
}

interface DraftRecord {
  schemaVersion: number
  resourceId: string
  sourceContentId: string | null
  updatedAt: number
  writerId: string
  sequence: number
  snapshot?: CanvasDraftSnapshot
  deleted?: boolean
}

interface PendingDraft {
  record: DraftRecord
  generation: number
  firstQueuedAt: number
  timer?: ReturnType<typeof setTimeout>
}

const DATABASE_NAME = 'dataease-canvas-drafts'
const STORE_NAME = 'drafts'
const SCHEMA_VERSION = 1
const WRITE_DELAY = 500
const MAX_WRITE_DELAY = 2000
const LEGACY_PREFIX = 'DE-DV-CATCH-'

let database: IDBDatabase | undefined
let opening: Promise<IDBDatabase> | undefined
let legacyCache: ReturnType<typeof useCache>['wsCache'] | undefined
const writerId = Math.random().toString(36).slice(2)
let sequence = 0
let errorHandler: (() => void) | undefined
const pending = new Map<string, PendingDraft>()
const failed = new Map<string, PendingDraft>()
const generations = new Map<string, number>()
const operations = new Map<string, Promise<void>>()
const reportedErrors = new Set<string>()

const resourceKey = (id: string | number | null | undefined): string | undefined =>
  id === undefined || id === null || id === '' ? undefined : String(id)
const contentKey = (id: string | number | null | undefined): string | null =>
  id === undefined || id === null ? null : String(id)

const reportError = (resourceId: string) => {
  if (!reportedErrors.has(resourceId)) {
    reportedErrors.add(resourceId)
    errorHandler?.()
  }
}

export const setCanvasDraftErrorHandler = (handler: (() => void) | undefined): void => {
  errorHandler = handler
}

const openDatabase = (): Promise<IDBDatabase> => {
  if (database) return Promise.resolve(database)
  if (opening) return opening
  opening = new Promise<IDBDatabase>((resolve, reject) => {
    let settled = false
    const request = indexedDB.open(DATABASE_NAME, SCHEMA_VERSION)
    const fail = (error?: unknown) => {
      if (settled) return
      settled = true
      opening = undefined
      reject(error || new Error('Canvas draft database is unavailable'))
    }
    request.onupgradeneeded = () => {
      const db = request.result
      if (!db.objectStoreNames.contains(STORE_NAME)) {
        db.createObjectStore(STORE_NAME, { keyPath: 'resourceId' })
      }
    }
    request.onerror = () => fail(request.error)
    request.onblocked = () => fail()
    request.onsuccess = () => {
      if (settled) {
        request.result.close()
        return
      }
      settled = true
      database = request.result
      opening = undefined
      const db = database
      db.onversionchange = () => {
        db.close()
        if (database === db) database = undefined
      }
      db.onclose = () => {
        if (database === db) database = undefined
      }
      resolve(db)
    }
  }).catch(error => {
    opening = undefined
    throw error
  })
  return opening
}

// Once open, start the transaction in the caller's task, including pagehide flushes.
const withDatabase = <T>(action: (db: IDBDatabase) => Promise<T>): Promise<T> =>
  database ? action(database) : openDatabase().then(action)

const readRecord = (resourceId: string): Promise<DraftRecord | undefined> =>
  withDatabase(
    db =>
      new Promise((resolve, reject) => {
        const transaction = db.transaction(STORE_NAME, 'readonly')
        const request = transaction.objectStore(STORE_NAME).get(resourceId)
        transaction.oncomplete = () => resolve(request.result)
        transaction.onabort = () => reject(transaction.error)
        transaction.onerror = () => reject(transaction.error)
      })
  )

const writeRecord = (record: DraftRecord, generation?: number): Promise<boolean> =>
  withDatabase(db => {
    if (generation !== undefined && generations.get(record.resourceId) !== generation) {
      return Promise.resolve(false)
    }
    return new Promise((resolve, reject) => {
      const transaction = db.transaction(STORE_NAME, 'readwrite')
      const objectStore = transaction.objectStore(STORE_NAME)
      const request = objectStore.get(record.resourceId)
      request.onsuccess = () => {
        const existing = request.result as DraftRecord | undefined
        // Compare in the same transaction: an old queued write cannot replace a newer draft.
        const newer =
          !existing ||
          existing.updatedAt < record.updatedAt ||
          (existing.updatedAt === record.updatedAt &&
            (existing.writerId === record.writerId
              ? existing.sequence < record.sequence
              : record.deleted && !existing.deleted))
        if (newer) {
          try {
            objectStore.put(record)
          } catch (error) {
            transaction.abort()
            reject(error)
          }
        }
      }
      transaction.oncomplete = () => resolve(true)
      transaction.onabort = () => reject(transaction.error)
      transaction.onerror = () => reject(transaction.error)
    })
  })

const enqueueOperation = (resourceId: string, action: () => Promise<void>): Promise<void> => {
  const previous = operations.get(resourceId)
  let task: Promise<void>
  try {
    task = (previous ? previous.then(action) : action()).catch(() => reportError(resourceId))
  } catch {
    reportError(resourceId)
    task = Promise.resolve()
  }
  operations.set(resourceId, task)
  void task.then(() => {
    if (operations.get(resourceId) === task) operations.delete(resourceId)
  })
  return task
}

const withoutMaterialBundle = (snapshot: CanvasDraftSnapshot): CanvasDraftSnapshot => {
  const dvInfo = { ...snapshot.dvInfo }
  delete dvInfo.materialBundle
  return { ...snapshot, dvInfo }
}

const isSnapshot = (value: unknown, resourceId: string): value is CanvasDraftSnapshot => {
  if (!value || typeof value !== 'object') return false
  const snapshot = value as CanvasDraftSnapshot
  return (
    resourceKey(snapshot.dvInfo?.id) === resourceId &&
    Array.isArray(snapshot.componentData) &&
    !!snapshot.canvasStyleData &&
    typeof snapshot.canvasStyleData === 'object' &&
    !!snapshot.canvasViewInfo &&
    typeof snapshot.canvasViewInfo === 'object' &&
    !!snapshot.cacheViewIdInfo &&
    typeof snapshot.cacheViewIdInfo === 'object' &&
    Array.isArray(snapshot.cacheViewIdInfo.snapshotCacheViewCalc) &&
    Array.isArray(snapshot.cacheViewIdInfo.snapshotCacheViewRender)
  )
}

const deleteLegacy = (resourceId: string) => {
  try {
    window.localStorage.removeItem(LEGACY_PREFIX + resourceId)
  } catch {
    reportError(resourceId)
  }
}

const startPending = (resourceId: string) => {
  const entry = pending.get(resourceId) || failed.get(resourceId)
  if (!entry) return
  clearTimeout(entry.timer)
  pending.delete(resourceId)
  failed.delete(resourceId)
  void enqueueOperation(resourceId, async () => {
    if (generations.get(resourceId) !== entry.generation) return
    try {
      const stored = await writeRecord(entry.record, entry.generation)
      if (!stored) return
      reportedErrors.delete(resourceId)
      deleteLegacy(resourceId)
    } catch {
      if (generations.get(resourceId) === entry.generation) failed.set(resourceId, entry)
      reportError(resourceId)
    }
  })
}

export const queueCanvasDraft = (snapshot: CanvasDraftSnapshot): void => {
  const resourceId = resourceKey(snapshot?.dvInfo?.id)
  if (!resourceId || !isSnapshot(snapshot, resourceId)) return
  const previous = pending.get(resourceId)
  clearTimeout(previous?.timer)
  const generation = (generations.get(resourceId) || 0) + 1
  generations.set(resourceId, generation)
  failed.delete(resourceId)
  const entry: PendingDraft = {
    generation,
    firstQueuedAt: previous?.firstQueuedAt ?? Date.now(),
    record: {
      schemaVersion: SCHEMA_VERSION,
      resourceId,
      sourceContentId: contentKey(snapshot.dvInfo.contentId),
      updatedAt: Date.now(),
      writerId,
      sequence: ++sequence,
      snapshot: withoutMaterialBundle(snapshot)
    }
  }
  entry.timer = setTimeout(
    () => startPending(resourceId),
    Math.max(0, Math.min(WRITE_DELAY, entry.firstQueuedAt + MAX_WRITE_DELAY - Date.now()))
  )
  pending.set(resourceId, entry)
}

export const flushCanvasDraft = async (): Promise<void> => {
  for (const resourceId of new Set([...pending.keys(), ...failed.keys()])) {
    startPending(resourceId)
  }
  await Promise.all(operations.values())
}

export const hasPendingCanvasDraft = (): boolean =>
  pending.size > 0 || failed.size > 0 || operations.size > 0

export const discardCanvasDraft = (id: string | number): Promise<void> => {
  const resourceId = resourceKey(id)
  if (!resourceId) return Promise.resolve()
  const generation = (generations.get(resourceId) || 0) + 1
  generations.set(resourceId, generation)
  clearTimeout(pending.get(resourceId)?.timer)
  pending.delete(resourceId)
  failed.delete(resourceId)
  deleteLegacy(resourceId)
  const record: DraftRecord = {
    schemaVersion: SCHEMA_VERSION,
    resourceId,
    sourceContentId: null,
    updatedAt: Date.now(),
    writerId,
    sequence: ++sequence,
    deleted: true
  }
  // Keep a small deletion marker so an older write from another tab cannot revive the draft.
  const entry: PendingDraft = {
    record,
    generation,
    firstQueuedAt: Date.now()
  }
  return enqueueOperation(resourceId, async () => {
    try {
      await writeRecord(record)
      reportedErrors.delete(resourceId)
    } catch {
      if (generations.get(resourceId) === entry.generation) failed.set(resourceId, entry)
      reportError(resourceId)
    }
  })
}

export const getCanvasDraft = async (
  id: string | number
): Promise<CanvasDraftSnapshot | undefined> => {
  const resourceId = resourceKey(id)
  if (!resourceId) return undefined
  startPending(resourceId)
  await operations.get(resourceId)
  const failedRecord = failed.get(resourceId)?.record
  if (failedRecord) {
    try {
      return failedRecord.deleted ? undefined : structuredClone(failedRecord.snapshot)
    } catch {
      reportError(resourceId)
      return undefined
    }
  }
  try {
    const record = await readRecord(resourceId)
    if (record) {
      if (record.deleted) {
        reportedErrors.delete(resourceId)
        return undefined
      }
      if (
        record.schemaVersion === SCHEMA_VERSION &&
        record.resourceId === resourceId &&
        isSnapshot(record.snapshot, resourceId) &&
        record.sourceContentId === contentKey(record.snapshot.dvInfo.contentId)
      ) {
        reportedErrors.delete(resourceId)
        return withoutMaterialBundle(record.snapshot)
      }
      reportError(resourceId)
      return undefined
    }
  } catch {
    reportError(resourceId)
  }
  try {
    legacyCache ||= useCache().wsCache
    const legacy = legacyCache.get(LEGACY_PREFIX + resourceId) as CanvasDraftSnapshot
    if (legacy === undefined || legacy === null) return undefined
    if (!isSnapshot(legacy, resourceId)) {
      reportError(resourceId)
      return undefined
    }
    reportedErrors.delete(resourceId)
    return withoutMaterialBundle(legacy)
  } catch {
    reportError(resourceId)
    return undefined
  }
}

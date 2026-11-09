type ResourceId = string | number | null | undefined
interface DocumentIdentity {
  id?: ResourceId
  type?: string
}
interface EditorLocation {
  id?: ResourceId
  type: string
  opt?: string
  template?: unknown
  parent?: ResourceId
  createType?: unknown
}
const idOf = (id: ResourceId) => (id == null ? '' : String(id))
const sessions = new WeakMap<object, ReturnType<typeof createCanvasEditSession>>()

/** 与现有单文档 Store 对接；会话本身由编辑入口持有，不进入文档或撤销快照。 */
export const createCanvasEditSession = (
  owner: object,
  location: () => EditorLocation,
  document: () => DocumentIdentity
) => {
  let generation = 0
  let status: 'loading' | 'ready' | 'failed' | 'disposed' = 'loading'
  let source = location()
  let documentId = ''
  let disposed = false
  let savedTarget = false
  const current = () => {
    const value = location()
    return (
      sessions.get(owner) === session &&
      status !== 'disposed' &&
      value.type === source.type &&
      ((idOf(value.id) === idOf(source.id) &&
        value.opt === source.opt &&
        value.template === source.template &&
        idOf(value.parent) === idOf(source.parent) &&
        value.createType === source.createType) ||
        (status === 'ready' &&
          savedTarget &&
          !!documentId &&
          idOf(value.id) === documentId &&
          !value.opt))
    )
  }
  const session = {
    begin() {
      source = { ...location() }
      savedTarget = false
      generation++
      documentId = ''
      status = disposed ? 'disposed' : 'loading'
      if (!disposed) sessions.set(owner, session)
      const version = generation
      const isCurrent = () => version === generation && current()
      return {
        isCurrent,
        ready() {
          if (!isCurrent()) return false
          const value = document()
          if (
            value.type !== source.type ||
            (source.opt !== 'copy' && idOf(source.id) && idOf(value.id) !== idOf(source.id))
          ) {
            status = 'failed'
            return false
          }
          documentId = idOf(value.id)
          status = 'ready'
          return true
        },
        fail() {
          if (isCurrent()) status = 'failed'
        }
      }
    },
    capture() {
      if (!current() || status !== 'ready') return
      const version = generation
      let target = documentId
      const isCurrent = () =>
        version === generation &&
        current() &&
        status === 'ready' &&
        documentId === target &&
        idOf(document().id) === documentId &&
        document().type === source.type
      if (!isCurrent()) return
      return {
        isCurrent,
        saved(id?: ResourceId) {
          if (version === generation && current() && idOf(document().id) === idOf(id ?? target)) {
            documentId = idOf(id ?? target)
            target = documentId
            savedTarget = true
          }
        }
      }
    },
    fail() {
      generation++
      status = 'failed'
    },
    dispose() {
      generation++
      disposed = true
      status = 'disposed'
      if (sessions.get(owner) === session) sessions.delete(owner)
    }
  }
  sessions.set(owner, session)
  return session
}

export const captureCanvasSave = (owner: object, document: () => DocumentIdentity) => {
  const session = sessions.get(owner)
  if (session) return session.capture()
  // 未纳管的独立图表编辑入口保留既有保存行为，仍阻止跨文档回写。
  const initial = document()
  return {
    isCurrent: () => document() === initial,
    saved: (_id?: ResourceId) => undefined
  }
}

// 保存已经落库但重载失败时，保留文档身份并禁止用未刷新的字段再次保存。
export const failCanvasEditSession = (owner: object) => sessions.get(owner)?.fail()

export const isCanvasDraft = (value: unknown, id: ResourceId, type: string) => {
  if (!value || typeof value !== 'object') return false
  const draft = value as {
    dvInfo?: DocumentIdentity
    componentData?: unknown
    canvasStyleData?: unknown
    canvasViewInfo?: unknown
    cacheViewIdInfo?: { snapshotCacheViewCalc?: unknown; snapshotCacheViewRender?: unknown }
  }
  return (
    !!idOf(id) &&
    idOf(draft.dvInfo?.id) === idOf(id) &&
    draft.dvInfo?.type === type &&
    Array.isArray(draft.componentData) &&
    !!draft.canvasStyleData &&
    typeof draft.canvasStyleData === 'object' &&
    !!draft.canvasViewInfo &&
    typeof draft.canvasViewInfo === 'object' &&
    Array.isArray(draft.cacheViewIdInfo?.snapshotCacheViewCalc) &&
    Array.isArray(draft.cacheViewIdInfo?.snapshotCacheViewRender)
  )
}

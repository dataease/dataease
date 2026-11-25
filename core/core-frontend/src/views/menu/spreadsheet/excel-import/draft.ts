import type { ImportedSheet } from './protocol'

export interface ExcelImportDraft {
  name: string
  sheets: ImportedSheet[]
}

// 嵌入模式直接切换组件，工作表可以在内存中交接。
let embeddedDraft: ExcelImportDraft | undefined

export function setEmbeddedImportDraft(draft?: ExcelImportDraft) {
  embeddedDraft = draft
}

export function takeEmbeddedImportDraft() {
  const draft = embeddedDraft
  embeddedDraft = undefined
  return draft
}

// 独立编辑器可能在另一标签页打开。只在 URL 中传随机凭据，首次读取后删除工作表数据。
const storeName = 'imports'
const expiryMs = 10 * 60 * 1000

function withStore<T>(action: (store: IDBObjectStore) => IDBRequest<T>): Promise<T> {
  return new Promise((resolve, reject) => {
    const open = indexedDB.open('dataease-spreadsheet-import', 1)
    open.onupgradeneeded = () => {
      open.result.createObjectStore(storeName).createIndex('createdAt', 'createdAt')
    }
    open.onerror = () => reject(open.error)
    open.onsuccess = () => {
      const db = open.result
      const transaction = db.transaction(storeName, 'readwrite')
      transaction.onabort = () => {
        db.close()
        reject(transaction.error || new Error('Spreadsheet import aborted'))
      }
      try {
        const result = action(transaction.objectStore(storeName))
        transaction.oncomplete = () => {
          db.close()
          resolve(result.result)
        }
      } catch (error) {
        transaction.abort()
        reject(error)
      }
    }
  })
}

export async function stageImportDraft(draft: ExcelImportDraft) {
  const id = Array.from(crypto.getRandomValues(new Uint32Array(4)), n =>
    n.toString(16).padStart(8, '0')
  ).join('')
  const createdAt = Date.now()
  await withStore(store => {
    // 清理上次在编辑器打开前退出而遗留的数据，无需反序列化工作表。
    const expired = store
      .index('createdAt')
      .openKeyCursor(IDBKeyRange.upperBound(createdAt - expiryMs))
    expired.onsuccess = () => {
      const cursor = expired.result
      if (cursor) {
        store.delete(cursor.primaryKey)
        cursor.continue()
      }
    }
    return store.add({ ...draft, createdAt }, id)
  })
  return id
}

export function takeImportDraft(id: string): Promise<ExcelImportDraft | undefined> {
  return withStore(store => {
    const request = store.get(id)
    store.delete(id)
    return request
  })
}

export function discardImportDraft(id: string) {
  return withStore(store => store.delete(id))
}

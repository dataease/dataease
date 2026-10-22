import request from '@/config/axios'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'

export interface MaterialContext {
  resourceId?: string | number
  parentId?: string | number
  resourceType?: string
  resourceTable?: 'core' | 'snapshot'
  templateId?: string
  sourceTemplateId?: string
}
export interface MaterialItem {
  id: string
  name: string
  format: string
  size: number
  categoryId: string
  preset: boolean
  reference: string
}
export interface MaterialCategory {
  id: string | null
  name: string
  count: number
}
export interface MaterialQuery extends MaterialContext {
  keyword?: string
  format?: string
  categoryId?: string | null
  preset?: boolean
  allSources?: boolean
  management?: boolean
}
export const materialContext = (): MaterialContext => {
  const canvas = dvMainStoreWithOut()
  return {
    resourceId: canvas.dvInfo?.id,
    parentId: canvas.dvInfo?.pid,
    resourceType: canvas.dvInfo?.type,
    sourceTemplateId: canvas.dvInfo?.materialSourceTemplateId,
    resourceTable: canvas.editMode === 'edit' ? 'snapshot' : 'core'
  }
}
export interface MaterialCapabilities {
  manage: boolean
  available: boolean
}
export const materialCapabilities = (): Promise<{ data: MaterialCapabilities }> =>
  request.get({ url: '/material/capabilities' })
export const materialList = (query: MaterialQuery, page: number, size: number, manage: boolean) =>
  request.post({
    url: `/material/${manage ? 'pager' : 'select'}/${page}/${size}`,
    data: query
  })
export const materialCategories = (query: MaterialQuery) =>
  request.post({ url: '/material/categories', data: query })
export const saveMaterialCategory = (name: string, id?: string) =>
  request.post({ url: '/material/category/save', data: { name, id } })
export const deleteMaterialCategory = (id: string) =>
  request.post({ url: '/material/category/delete/' + id })
export const updateMaterial = (data: {
  id: string
  name?: string
  categoryId?: string
  moveCategory?: boolean
}) => request.post({ url: '/material/update', data })
export const deleteMaterial = (id: string) => request.post({ url: '/material/delete/' + id })
export const uploadMaterial = (
  file: File,
  categoryId?: string,
  progress?: (value: number) => void,
  options?: { name?: string; signal?: AbortSignal }
) => {
  const data = new FormData()
  data.append('file', file)
  return request.post({
    url: '/material/upload',
    onUploadProgress: (event: { loaded: number; total?: number }) => {
      if (event.total) progress?.(Math.min(99, Math.round((event.loaded * 100) / event.total)))
    },
    params: { categoryId, name: options?.name },
    signal: options?.signal,
    handleErrorLocally: true,
    headersType: 'multipart/form-data',
    data
  })
}
export const fetchMaterial = async (
  id: string,
  context: MaterialContext,
  signal?: AbortSignal
): Promise<Blob> => {
  const result = await request.post({
    url: '/material/content/' + id,
    data: context,
    responseType: 'blob',
    signal,
    handleErrorLocally: true
  })
  return result.data
}
export const downloadMaterial = async (id: string): Promise<Blob> => {
  const result = await request.get({
    url: '/material/download/' + id,
    responseType: 'blob'
  })
  return result.data
}

export interface MaterialBundle {
  version: number
  referenceMap?: Record<string, string>
  files: { reference: string; name: string; format: string; content: string }[]
}
export const MAX_MATERIAL_PACKAGE_BYTES = 300 * 1024 * 1024
export const exportMaterialBundle = (canvasStyleData: string, componentData: string) =>
  request.post({
    url: '/material/export',
    data: { ...materialContext(), canvasStyleData, componentData }
  })

export const remapMaterialDocument = (
  document: string,
  mapping?: Record<string, string>
): string => {
  if (!mapping || !Object.keys(mapping).length) return document
  const visit = (value: unknown, depth = 0): unknown => {
    if (depth > 100) throw new Error('Material document too complex')
    if (typeof value === 'string') return mapping[value] || value
    if (Array.isArray(value)) return value.map(item => visit(item, depth + 1))
    if (value && typeof value === 'object') {
      return Object.fromEntries(
        Object.entries(value).map(([key, item]) => [key, visit(item, depth + 1)])
      )
    }
    return value
  }
  return JSON.stringify(visit(JSON.parse(document)))
}

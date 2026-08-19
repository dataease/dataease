import type { DatasetMapping, ReplacementField } from '../types'

const sameId = (left: string | number | undefined, right: string | number | undefined): boolean =>
  left !== undefined && right !== undefined && String(left) === String(right)

export const getDatasetMapping = (
  mappings: DatasetMapping[],
  datasetId: string | number | undefined
): DatasetMapping | undefined =>
  mappings.find(mapping => sameId(mapping.source.dataset.id, datasetId))

export const getTargetField = (
  mappings: DatasetMapping[],
  datasetId: string | number | undefined,
  fieldId: string | number | undefined
): ReplacementField | undefined =>
  getDatasetMapping(mappings, datasetId)
    ?.fields.find(mapping => sameId(mapping.source.fieldId, fieldId))
    ?.target

export const getTargetDataset = (
  mappings: DatasetMapping[],
  datasetId: string | number | undefined
) => getDatasetMapping(mappings, datasetId)?.target

export const cloneReplacementValue = <T>(value: T): T => {
  return JSON.parse(JSON.stringify(value)) as T
}

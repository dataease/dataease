import { iconFieldMap } from '@/components/icon-group/field-list'
import {
  iconFieldCalculatedMap,
  iconFieldCalculatedQMap
} from '@/components/icon-group/field-calculated-list'
import { fieldType } from '@/utils/attr'
import type { FieldItemData } from '../types/plugin'

type FieldIconSource = Pick<FieldItemData, 'deType' | 'extField' | 'groupType'>

export const getSpreadsheetFieldIcon = (field: FieldIconSource) => {
  const deType = field.deType ?? 0

  // 与仪表板保持一致：维度和指标分别使用对应配色的计算字段图标。
  if (field.extField === 2) {
    const calculatedIconMap =
      field.groupType === 'd' ? iconFieldCalculatedQMap : iconFieldCalculatedMap
    return calculatedIconMap[deType] || iconFieldMap.text
  }

  const iconType = fieldType[deType] || 'text'
  return iconFieldMap[iconType] || iconFieldMap.text
}

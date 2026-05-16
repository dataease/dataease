type DrillField = {
  id?: string | number
}

export const hasNextDrillLevel = (drillFields?: unknown[], drillLevel = 0) => {
  const fieldSize = Array.isArray(drillFields) ? drillFields.length : 0
  return drillLevel >= 0 && fieldSize > 1 && drillLevel < fieldSize - 1
}

export const isCurrentDrillField = (
  drillFields: DrillField[] | undefined,
  drillLevel: number,
  fieldId: string | number
) => {
  if (!hasNextDrillLevel(drillFields, drillLevel)) {
    return false
  }
  return drillFields?.[drillLevel]?.id === fieldId
}

import type { IWorkbookData } from '@univerjs/core'
import type { MenuConfig } from '@univerjs/ui'
import {
  AddRangeProtectionCommand,
  AddRangeProtectionMutation,
  AddWorksheetProtectionCommand,
  AddWorksheetProtectionMutation,
  DeleteRangeProtectionCommand,
  DeleteRangeProtectionMutation,
  DeleteWorksheetProtectionCommand,
  DeleteWorksheetProtectionMutation,
  SetProtectionCommand,
  SetRangeProtectionMutation,
  SetWorksheetPermissionPointsCommand,
  SetWorksheetPermissionPointsMutation,
  SetWorksheetProtectionCommand,
  SetWorksheetProtectionMutation
} from '@univerjs/sheets'
import {
  AddRangeProtectionFromContextMenuCommand,
  AddRangeProtectionFromSheetBarCommand,
  AddRangeProtectionFromToolbarCommand,
  ChangeSheetProtectionFromSheetBarCommand,
  DeleteRangeProtectionFromContextMenuCommand,
  DeleteWorksheetProtectionFormSheetBarCommand,
  SetRangeProtectionFromContextMenuCommand,
  SheetPermissionOpenDialogOperation,
  SheetPermissionOpenPanelOperation,
  ViewSheetPermissionFromContextMenuCommand,
  ViewSheetPermissionFromSheetBarCommand
} from '@univerjs/sheets-ui'

// 该父菜单 ID 在 Univer 的权限菜单模块中公开，但当前版本没有从包根路径导出。
const SHEET_PERMISSION_CONTEXT_MENU_ID = 'sheet.contextMenu.permission'

export const DATAEASE_PROTECTION_RESOURCE_NAMES = new Set([
  'SHEET_WORKSHEET_PROTECTION_PLUGIN',
  'SHEET_WORKSHEET_PROTECTION_POINT_PLUGIN',
  'SHEET_RANGE_PROTECTION_PLUGIN'
])

export const DATAEASE_PROTECTION_MENU_CONFIG: MenuConfig = {
  [AddRangeProtectionFromToolbarCommand.id]: { hidden: true },
  [SHEET_PERMISSION_CONTEXT_MENU_ID]: { hidden: true },
  [AddRangeProtectionFromContextMenuCommand.id]: { hidden: true },
  [SetRangeProtectionFromContextMenuCommand.id]: { hidden: true },
  [DeleteRangeProtectionFromContextMenuCommand.id]: { hidden: true },
  [ViewSheetPermissionFromContextMenuCommand.id]: { hidden: true },
  [AddRangeProtectionFromSheetBarCommand.id]: { hidden: true },
  [DeleteWorksheetProtectionFormSheetBarCommand.id]: { hidden: true },
  [ChangeSheetProtectionFromSheetBarCommand.id]: { hidden: true },
  [ViewSheetPermissionFromSheetBarCommand.id]: { hidden: true }
}

/**
 * 显式列出所有公开的保护入口和写入命令，不通过命令名称模糊匹配，
 * 避免误伤 DataEase 自身需要保留的渲染区域编辑限制。
 */
export const DATAEASE_BLOCKED_PROTECTION_COMMAND_IDS = new Set([
  AddRangeProtectionFromToolbarCommand.id,
  AddRangeProtectionFromContextMenuCommand.id,
  SetRangeProtectionFromContextMenuCommand.id,
  DeleteRangeProtectionFromContextMenuCommand.id,
  ViewSheetPermissionFromContextMenuCommand.id,
  AddRangeProtectionFromSheetBarCommand.id,
  DeleteWorksheetProtectionFormSheetBarCommand.id,
  ChangeSheetProtectionFromSheetBarCommand.id,
  ViewSheetPermissionFromSheetBarCommand.id,
  SheetPermissionOpenDialogOperation.id,
  SheetPermissionOpenPanelOperation.id,
  AddRangeProtectionCommand.id,
  DeleteRangeProtectionCommand.id,
  SetProtectionCommand.id,
  AddWorksheetProtectionCommand.id,
  DeleteWorksheetProtectionCommand.id,
  SetWorksheetProtectionCommand.id,
  SetWorksheetPermissionPointsCommand.id,
  AddRangeProtectionMutation.id,
  DeleteRangeProtectionMutation.id,
  SetRangeProtectionMutation.id,
  AddWorksheetProtectionMutation.id,
  DeleteWorksheetProtectionMutation.id,
  SetWorksheetProtectionMutation.id,
  SetWorksheetPermissionPointsMutation.id
])

interface WorkbookWithResources extends Partial<IWorkbookData> {
  resources?: Array<{
    id?: string
    name: string
    data: string
  }>
}

/**
 * 在快照边界统一删除保护规则，确保旧规则在 Univer 创建工作簿之前失效，
 * 同时避免遗漏的运行时命令入口再次将保护规则持久化。
 */
export const clearUniverProtectionResources = (
  snapshot: Partial<IWorkbookData>
): Partial<IWorkbookData> => {
  const workbook = snapshot as WorkbookWithResources
  if (!workbook.resources?.length) {
    return snapshot
  }

  workbook.resources = workbook.resources.filter(
    resource => !DATAEASE_PROTECTION_RESOURCE_NAMES.has(resource.name)
  )
  return snapshot
}

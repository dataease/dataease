import { AddWorksheetMergeCommand } from '@univerjs/sheets'
import type { IMenuItem, IMenuManagerService, IMenuSchema } from '@univerjs/ui'
import {
  DATAEASE_HIDDEN_RIBBON_MENU_IDS,
  DATAEASE_INSERT_FUNCTION_MENU_ID_PREFIX,
  DATAEASE_RIBBON_GROUPS,
  DATAEASE_RIBBON_POSITION_ORDER,
  type DataEaseRibbonGroupConfig
} from '../config/ribbon-config'

export type DataEaseRibbonItem = {
  key: string
  item: IMenuItem
  order: number
}

export type DataEaseRibbonRow = DataEaseRibbonItem[]

export type DataEaseRibbonGroup = {
  key: string
  className?: string
  items: DataEaseRibbonItem[]
  rows: DataEaseRibbonRow[] | null
}

type DataEaseRibbonMenuItem = IMenuItem & {
  commandId?: string
  command?: { id?: string }
}

const getRibbonItemIds = (item: IMenuItem): string[] => {
  const menuItem = item as DataEaseRibbonMenuItem
  return [menuItem.id, menuItem.commandId, menuItem.command?.id]
    .filter((id): id is string => !!id)
}

const isHiddenRibbonItem = (item: IMenuItem): boolean =>
  getRibbonItemIds(item).some(id =>
    DATAEASE_HIDDEN_RIBBON_MENU_IDS.has(id) ||
    id.startsWith(`${DATAEASE_INSERT_FUNCTION_MENU_ID_PREFIX}.`)
  )

const normalizeRibbonItem = (item: IMenuItem): IMenuItem =>
  item.id === AddWorksheetMergeCommand.id
    ? { ...item, title: 'sheets-ui.toolbar.mergeCell.main' }
    : item

const getSchemaItems = (schema: IMenuSchema): DataEaseRibbonItem[] => {
  const groupItem = schema.item
    ? [{
        key: schema.key,
        item: schema.item,
        order: schema.order
      }]
    : []
  const childItems = schema.children
    ?.filter(child => !!child.item)
    .map(child => ({
      key: child.key,
      item: child.item as IMenuItem,
      order: child.order
    })) || []

  return [...groupItem, ...childItems]
}

const createMenuItemIndex = (
  menuManagerService: IMenuManagerService
): Map<string, DataEaseRibbonItem> => {
  const itemIndex = new Map<string, DataEaseRibbonItem>()

  DATAEASE_RIBBON_POSITION_ORDER
    .flatMap(position => menuManagerService.getMenuByPositionKey(position))
    .flatMap(getSchemaItems)
    .filter(({ item }) => !isHiddenRibbonItem(item))
    .forEach(menuItem => {
      const normalizedItem = {
        ...menuItem,
        item: normalizeRibbonItem(menuItem.item)
      }

      getRibbonItemIds(normalizedItem.item).forEach(id => {
        if (!itemIndex.has(id)) {
          itemIndex.set(id, normalizedItem)
        }
      })
    })

  return itemIndex
}

const resolveConfiguredItems = (
  ids: string[],
  itemIndex: Map<string, DataEaseRibbonItem>
): DataEaseRibbonItem[] =>
  ids
    .map(id => itemIndex.get(id))
    .filter((item): item is DataEaseRibbonItem => !!item)

const createRibbonGroup = (
  config: DataEaseRibbonGroupConfig,
  itemIndex: Map<string, DataEaseRibbonItem>
): DataEaseRibbonGroup | null => {
  const rows = config.rows
    ?.map(row => resolveConfiguredItems(row, itemIndex))
    .filter(row => row.length > 0) || null
  const items = rows?.flat() || resolveConfiguredItems(config.items || [], itemIndex)

  if (!items.length) {
    return null
  }

  return {
    key: config.key,
    className: config.className,
    items,
    rows
  }
}

export const collectToolbarGroups = (
  menuManagerService: IMenuManagerService
): DataEaseRibbonGroup[] => {
  const itemIndex = createMenuItemIndex(menuManagerService)

  return DATAEASE_RIBBON_GROUPS
    .map(config => createRibbonGroup(config, itemIndex))
    .filter((group): group is DataEaseRibbonGroup => !!group)
}

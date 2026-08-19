import type { IMenuItem } from '@univerjs/ui'
import { ToolbarItem } from '@univerjs/ui'
import type { ComponentManager, IMenuManagerService } from '@univerjs/ui'
import {
  DATAEASE_BUSINESS_MENU_IDS,
  DATAEASE_INSERT_DROPDOWN_MENU_ID,
  DATAEASE_QUERY_CONTROL_GROUP_KEY,
  DATAEASE_QUERY_CONTROL_MENU_ID
} from '../config/ribbon-config'
import {
  collectToolbarGroups,
  type DataEaseRibbonGroup,
  type DataEaseRibbonItem,
  type DataEaseRibbonRow
} from './dataease-ribbon-model'

const getGroupClassName = (group: DataEaseRibbonGroup): string => {
  const { key, className, items, rows } = group
  const classNames = ['dataease-spreadsheet-ribbon__group']

  if (items.some(({ item }) => DATAEASE_BUSINESS_MENU_IDS.has(item.id))) {
    classNames.push('dataease-spreadsheet-ribbon__group--business')
  }

  if (key === DATAEASE_QUERY_CONTROL_GROUP_KEY) {
    classNames.push('dataease-spreadsheet-ribbon__group--query-control')
  }

  if (className && rows) {
    classNames.push(className)
  } else if (items.length === 1) {
    classNames.push('dataease-spreadsheet-ribbon__group--single')
  } else if (items.length >= 4) {
    classNames.push('dataease-spreadsheet-ribbon__group--stacked')
  }

  return classNames.join(' ')
}

const getItemClassName = (item: IMenuItem): string => {
  const classNames = ['dataease-spreadsheet-ribbon__item']

  if (DATAEASE_BUSINESS_MENU_IDS.has(item.id)) {
    classNames.push('dataease-spreadsheet-ribbon__item--business')
  }

  if (item.id === DATAEASE_INSERT_DROPDOWN_MENU_ID) {
    classNames.push('dataease-spreadsheet-ribbon__item--insert')
  }

  if (item.id === DATAEASE_QUERY_CONTROL_MENU_ID) {
    classNames.push('dataease-spreadsheet-ribbon__item--query')
  }

  return classNames.join(' ')
}

const handleRibbonWheel = (ribbon: HTMLElement, event: WheelEvent): void => {
  const canScroll = ribbon.scrollWidth > ribbon.clientWidth
  if (!canScroll || Math.abs(event.deltaX) > Math.abs(event.deltaY)) {
    return
  }

  ribbon.scrollLeft += event.deltaY
  event.preventDefault()
}

export const createDataEaseRibbonComponent = (
  componentManager: ComponentManager,
  menuManagerService: IMenuManagerService
) => {
  const { createElement } = componentManager.reactUtils

  const renderItem = ({ key, item }: DataEaseRibbonItem) =>
    createElement(
      'div',
      {
        key,
        className: getItemClassName(item),
        'data-ribbon-item': item.id
      },
      createElement(ToolbarItem as any, item)
    )

  const renderRow = (row: DataEaseRibbonRow, rowIndex: number) =>
    createElement(
      'div',
      {
        key: `tool-row-${rowIndex}`,
        className: 'dataease-spreadsheet-ribbon__tool-row'
      },
      row.map(renderItem)
    )

  const renderGroup = (group: DataEaseRibbonGroup) => {
    const { items, rows } = group
    const children = rows
      ? rows.map(renderRow)
      : items.map(renderItem)

    return createElement(
      'div',
      {
        key: group.key,
        className: getGroupClassName(group),
        'data-ribbon-group': group.key
      },
      children
    )
  }

  return function DataEaseRibbon() {
    let removeRibbonWheelListener: (() => void) | undefined

    const setRibbonRef = (ribbon: HTMLDivElement | null): void => {
      removeRibbonWheelListener?.()
      removeRibbonWheelListener = undefined

      if (!ribbon) {
        return
      }

      const listener = (event: WheelEvent) => handleRibbonWheel(ribbon, event)
      const options: AddEventListenerOptions = { passive: false }

      ribbon.addEventListener('wheel', listener, options)
      removeRibbonWheelListener = () => ribbon.removeEventListener('wheel', listener, options)
    }

    return createElement(
      'div',
      {
        ref: setRibbonRef,
        className: 'dataease-spreadsheet-ribbon',
        role: 'toolbar',
        'aria-label': 'DataEase spreadsheet toolbar'
      },
      collectToolbarGroups(menuManagerService).map(renderGroup)
    )
  }
}

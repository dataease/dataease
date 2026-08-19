import { CommandType } from '@univerjs/core'
import type { ICommand, IAccessor } from '@univerjs/core'
import type { DetailTableConfig } from '../types'
import { TableFillService } from '../services/table-fill.service'
import { ElMessage } from 'element-plus-secondary'

export interface IApplyDetailTableOperationParams {
  univerApi: any
  config: DetailTableConfig
  startCell: string
}

export const ApplyDetailTableOperation: ICommand = {
  id: 'dataease.operation.apply-detail-table',
  type: CommandType.OPERATION,

  handler: async (accessor: IAccessor, params: IApplyDetailTableOperationParams) => {
    try {
      const { univerApi, config, startCell } = params
      const fillService = accessor.get(TableFillService)
      await fillService.fillTable(univerApi, config, startCell)

      return true
    } catch (error) {
      console.error('[DataPlugin] Failed to apply detail table:', error)
      ElMessage.error(`Update failed: ${error instanceof Error ? error.message : 'Unknown error'}`)
      return false
    }
  }
}

export interface IClearDetailTableOperationParams {
  univerApi: any
  startCell: string
  rowCount: number
  colCount: number
}

export const ClearDetailTableOperation: ICommand = {
  id: 'dataease.operation.clear-detail-table',
  type: CommandType.OPERATION,

  handler: async (accessor: IAccessor, params: IClearDetailTableOperationParams) => {
    try {
      const { univerApi, startCell, rowCount, colCount } = params
      const fillService = accessor.get(TableFillService)
      await fillService.clearTableData(univerApi, startCell, rowCount, colCount)
      return true
    } catch (error) {
      console.error('[DataPlugin] Failed to clear detail table:', error)
      return false
    }
  }
}

export interface IApplyDetailTableStyleOperationParams {
  univerApi: any
  config: DetailTableConfig
}

export const ApplyDetailTableStyleOperation: ICommand = {
  id: 'dataease.operation.apply-detail-table-style',
  type: CommandType.OPERATION,

  handler: async (accessor: IAccessor, params: IApplyDetailTableStyleOperationParams) => {
    try {
      const { univerApi, config } = params
      const fillService = accessor.get(TableFillService)
      await fillService.applyStyleOnly(univerApi, config)
      return true
    } catch (error) {
      console.error('[DataPlugin] Failed to apply detail table style:', error)
      ElMessage.error(`Update style failed: ${error instanceof Error ? error.message : 'Unknown error'}`)
      return false
    }
  }
}

import { CommandType, Injector } from '@univerjs/core'
import type { ICommand, IAccessor } from '@univerjs/core'
import { IDialogService } from '@univerjs/ui'
import { IRangeSelectDialogParams } from '../type'

export const OpenRangeSelectDialogOperation: ICommand = {
  id: 'dataease.operation.open-range-select-dialog',
  type: CommandType.OPERATION,
  handler: async (accessor: IAccessor, params?: IRangeSelectDialogParams) => {
    const dialogService: IDialogService = accessor.get(IDialogService)
    const injector = accessor.get(Injector)

    const dialogOptions = {
      id: 'RangeSelectDialog',
      children: {
        label: {
          name: 'RangeSelectDialog',
          props: {
            injector,
            params
          }
        }
      },
      title: {
        label: '选择单元格区域'
      },
      draggable: true,
      closable: true,
      mask: false,
      maskClosable: false,
      width: 400,
      onClose: () => {
        params?.onClose?.()
      }
    }
    // univer的bug，需要调用两次才能正确打开dialog
    dialogService.open(dialogOptions)
    dialogService.open(dialogOptions)

    return true
  }
}

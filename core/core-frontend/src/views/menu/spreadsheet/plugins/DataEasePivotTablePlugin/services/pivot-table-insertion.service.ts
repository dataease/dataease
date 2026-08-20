import { Inject } from '@univerjs/core'
import { TableInsertionService } from '../../DataEaseRuntimePlugin/services/table'

export class PivotTableInsertionService {
  constructor(
    @Inject(TableInsertionService)
    private readonly tableInsertionService: TableInsertionService
  ) {}

  start(): boolean {
    return this.tableInsertionService.start('pivot')
  }

  finish(): void {
    this.tableInsertionService.finish('pivot')
  }

  cancel(): void {
    this.tableInsertionService.cancel('pivot')
  }

  isInserting(): boolean {
    return this.tableInsertionService.isInserting()
  }
}

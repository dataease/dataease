import { Inject } from '@univerjs/core'
import { TableInsertionService } from '../../../services/table-insertion.service'

export class DetailTableInsertionService {
  constructor(
    @Inject(TableInsertionService)
    private readonly tableInsertionService: TableInsertionService
  ) {}

  start(): boolean {
    return this.tableInsertionService.start('detail')
  }

  finish(): void {
    this.tableInsertionService.finish('detail')
  }

  cancel(): void {
    this.tableInsertionService.cancel('detail')
  }

  isInserting(): boolean {
    return this.tableInsertionService.isInserting()
  }
}

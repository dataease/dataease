import { Disposable } from '@univerjs/core'
import { BehaviorSubject } from 'rxjs'

export type TableInsertionType = 'detail' | 'pivot'

export class TableInsertionService extends Disposable {
  private readonly insertingSubject = new BehaviorSubject(false)
  private insertionType?: TableInsertionType

  readonly inserting$ = this.insertingSubject.asObservable()

  start(type: TableInsertionType): boolean {
    if (this.insertionType) {
      return false
    }

    this.insertionType = type
    this.insertingSubject.next(true)
    return true
  }

  finish(type: TableInsertionType): void {
    this.end(type)
  }

  cancel(type: TableInsertionType): void {
    this.end(type)
  }

  isInserting(): boolean {
    return !!this.insertionType
  }

  override dispose(): void {
    this.insertionType = undefined
    this.insertingSubject.complete()
    super.dispose()
  }

  private end(type: TableInsertionType): void {
    if (this.insertionType !== type) {
      return
    }

    this.insertionType = undefined
    this.insertingSubject.next(false)
  }
}

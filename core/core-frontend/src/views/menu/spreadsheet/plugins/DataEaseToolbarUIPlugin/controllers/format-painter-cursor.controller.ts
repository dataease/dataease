import { Disposable, IUniverInstanceService } from '@univerjs/core'
import { IRenderManagerService } from '@univerjs/engine-render'
import { getSheetCommandTarget } from '@univerjs/sheets'
import { FormatPainterStatus, IFormatPainterService } from '@univerjs/sheets-ui'

export class FormatPainterCursorController extends Disposable {
  constructor(
    @IFormatPainterService formatPainterService: IFormatPainterService,
    @IUniverInstanceService instanceService: IUniverInstanceService,
    @IRenderManagerService renderManager: IRenderManagerService
  ) {
    super()
    let canvas: HTMLCanvasElement | undefined
    const clear = () => {
      canvas?.classList.remove('dataease-format-painter-cursor')
      canvas = undefined
    }
    const update = (status: FormatPainterStatus) => {
      clear()
      if (status === FormatPainterStatus.OFF) return
      const target = getSheetCommandTarget(instanceService)
      if (!target) return
      canvas = renderManager.getRenderById(target.unitId)?.engine.getCanvasElement()
      canvas?.classList.add('dataease-format-painter-cursor')
    }
    update(formatPainterService.getStatus())
    this.disposeWithMe(formatPainterService.status$.subscribe(update))
    this.disposeWithMe({ dispose: clear })
  }
}

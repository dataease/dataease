import { Inject } from '@univerjs/core'
import { CellPopupManagerService, SheetCanvasPopManagerService } from '@univerjs/sheets-ui'

export class PreviewCellPopupManagerService extends CellPopupManagerService {
  constructor(
    @Inject(SheetCanvasPopManagerService) canvasPopupManager: SheetCanvasPopManagerService
  ) {
    super(canvasPopupManager)
  }

  override showPopup(...[location, popup]: Parameters<CellPopupManagerService['showPopup']>) {
    // 使用工作台内的浮层容器，使其位于预览遮罩内及浏览器全屏元素的子树中。
    return super.showPopup(location, { ...popup, portal: true })
  }
}

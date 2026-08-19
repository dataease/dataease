import { Disposable } from '@univerjs/core'
import { IMenuManagerService, RibbonStartGroup } from '@univerjs/ui'

import {
  DATAEASE_INSERT_DROPDOWN_ID,
  InsertDropdownMainButtonFactory,
} from './menu'

export class DataEaseInsertMenuController extends Disposable {
  constructor(
    @IMenuManagerService private readonly _menuManagerService: IMenuManagerService,
  ) {
    super()
    this._initMenus()
  }

  private _initMenus(): void {
    // 将菜单合并到 Univer 的 Ribbon [开始] (Start) 面板的 OTHERS 组中
    this._menuManagerService.mergeMenu({
      [RibbonStartGroup.OTHERS]: {
        [DATAEASE_INSERT_DROPDOWN_ID]: {
          order: 10,
          menuItemFactory: InsertDropdownMainButtonFactory
        }
      }
    })
  }
}

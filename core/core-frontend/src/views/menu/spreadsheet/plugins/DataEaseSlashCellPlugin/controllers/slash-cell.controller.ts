import {
  Disposable,
  ICommandService,
  IResourceManagerService,
  CustomCommandExecutionError,
  InterceptorEffectEnum,
  Inject,
  IUniverInstanceService,
  UniverInstanceType
} from '@univerjs/core'
import { IRenderManagerService } from '@univerjs/engine-render'
import { IMenuManagerService, RibbonStartGroup } from '@univerjs/ui'
import { INTERCEPTOR_POINT, SheetInterceptorService } from '@univerjs/sheets'
import { SLASH_CELL_PLUGIN_RESOURCE_NAME } from '../../../utils/plugin-resource'
import {
  ApplyThreeSlashCellOperation,
  ApplyTwoSlashCellOperation,
  ClearSlashCellOperation
} from '../commands/operations'
import {
  ClearSlashCellMenuFactory,
  DATAEASE_SLASH_CELL_DROPDOWN_ID,
  SlashCellDropdownFactory,
  ThreeSlashCellMenuFactory,
  TwoSlashCellMenuFactory
} from './menu'
import { SlashCellRenderService } from '../services/slash-cell-render.service'
import { SlashCellStateService } from '../services/slash-cell-state.service'
import { SlashCellStyleHiderService } from '../services/slash-cell-style-hider.service'
import type { SlashCellItem } from '../types'

export class DataEaseSlashCellController extends Disposable {
  private readonly nativeSlashTokens = ['diagonal', 'tlbr', 'bltr', 'tlbc', 'tlmr', 'mltr', 'bctr']

  constructor(
    @ICommandService private readonly commandService: ICommandService,
    @IMenuManagerService private readonly menuManagerService: IMenuManagerService,
    @Inject(IResourceManagerService)
    private readonly resourceManagerService: IResourceManagerService,
    @Inject(IUniverInstanceService)
    private readonly univerInstanceService: IUniverInstanceService,
    @Inject(IRenderManagerService)
    private readonly renderManagerService: IRenderManagerService,
    @Inject(SheetInterceptorService)
    private readonly sheetInterceptorService: SheetInterceptorService,
    @Inject(SlashCellStateService)
    private readonly stateService: SlashCellStateService,
    @Inject(SlashCellRenderService)
    private readonly renderService: SlashCellRenderService,
    @Inject(SlashCellStyleHiderService)
    private readonly styleHiderService: SlashCellStyleHiderService
  ) {
    super()
    this.initCommands()
    this.initMenus()
    this.initResourceHook()
    this.initCellContentInterceptor()
    this.initNativeDiagonalGuard()
    this.styleHiderService.mount()
    this.disposeWithMe({ dispose: () => this.styleHiderService.dispose() })
  }

  private initCommands(): void {
    ;[ApplyTwoSlashCellOperation, ApplyThreeSlashCellOperation, ClearSlashCellOperation].forEach(command => {
      this.disposeWithMe(this.commandService.registerCommand(command))
    })
  }

  private initMenus(): void {
    this.menuManagerService.mergeMenu({
      [RibbonStartGroup.OTHERS]: {
        [DATAEASE_SLASH_CELL_DROPDOWN_ID]: {
          order: 30,
          menuItemFactory: SlashCellDropdownFactory,
          [ApplyTwoSlashCellOperation.id]: {
            order: 10,
            menuItemFactory: TwoSlashCellMenuFactory
          },
          [ApplyThreeSlashCellOperation.id]: {
            order: 20,
            menuItemFactory: ThreeSlashCellMenuFactory
          },
          [ClearSlashCellOperation.id]: {
            order: 30,
            menuItemFactory: ClearSlashCellMenuFactory
          }
        }
      }
    })
  }

  private initResourceHook(): void {
    this.disposeWithMe(
      this.resourceManagerService.registerPluginResource<SlashCellItem[]>({
        pluginName: SLASH_CELL_PLUGIN_RESOURCE_NAME,
        businesses: [UniverInstanceType.UNIVER_SHEET],
        onLoad: (unitId, resource) => {
          this.stateService.set(unitId, Array.isArray(resource) ? resource : [])
          this.refreshUnit(unitId)
        },
        onUnLoad: unitId => {
          this.stateService.deleteUnit(unitId)
        },
        toJson: unitId => JSON.stringify(this.stateService.list(unitId)),
        parseJson: data => JSON.parse(data) as SlashCellItem[]
      })
    )
  }

  private initCellContentInterceptor(): void {
    this.disposeWithMe(
      this.sheetInterceptorService.intercept(INTERCEPTOR_POINT.CELL_CONTENT, {
        effect: InterceptorEffectEnum.Style,
        priority: 20,
        handler: (rawCell, context, next) => {
          const cell = (rawCell || {}) as any
          const renderPosition = this.getRenderPosition(context)
          if (!renderPosition) {
            return next(rawCell)
          }

          const slashCell = this.stateService.get(
            context.unitId,
            context.subUnitId,
            renderPosition.row,
            renderPosition.col
          )
          if (!slashCell) {
            return next(rawCell)
          }

          const customRender = Array.isArray(cell.customRender)
            ? cell.customRender.filter((render: any) => render?.uKey !== this.renderService.customRenderKey)
            : []
          const cellValue = this.getRenderableCellValue(cell)

          return next({
            ...cell,
            customRender: [
              ...customRender,
              this.renderService.createCustomRender(slashCell.type, cellValue)
            ]
          })
        }
      })
    )
  }

  private getRenderableCellValue(cell: any): unknown {
    const dataStream = cell.p?.body?.dataStream
    if (typeof dataStream !== 'string') {
      return cell.v
    }

    // Univer 会把含换行的单元格保存为富文本，并在文档流末尾追加结束符。
    return dataStream.endsWith('\r\n') ? dataStream.slice(0, -2) : dataStream
  }

  private getRenderPosition(context: any): { row: number; col: number } | undefined {
    const mergedRange = context.worksheet?.getMergedCell?.(context.row, context.col)
    if (!mergedRange) {
      return { row: context.row, col: context.col }
    }

    const startRow = mergedRange.startRow ?? context.row
    const startColumn = mergedRange.startColumn ?? mergedRange.startCol ?? context.col
    if (context.row !== startRow || context.col !== startColumn) {
      return undefined
    }

    return { row: startRow, col: startColumn }
  }

  private initNativeDiagonalGuard(): void {
    this.disposeWithMe(
      this.commandService.beforeCommandExecuted(commandInfo => {
        const id = commandInfo.id?.toLowerCase?.() || ''
        if (
          this.nativeSlashTokens.some(token => id.includes(token)) ||
          this.hasNativeSlashToken(commandInfo.params)
        ) {
          throw new CustomCommandExecutionError('Native diagonal border is disabled by DataEase slash cell plugin')
        }
      })
    )
  }

  private hasNativeSlashToken(value: unknown, depth = 0, visited = new WeakSet<object>()): boolean {
    if (value == null || depth > 3) {
      return false
    }

    if (typeof value === 'string') {
      const text = value.toLowerCase()
      return this.nativeSlashTokens.some(token => text.includes(token))
    }

    if (typeof value !== 'object') {
      return false
    }

    if (visited.has(value)) {
      return false
    }
    visited.add(value)

    if (Array.isArray(value)) {
      return value.some(item => this.hasNativeSlashToken(item, depth + 1, visited))
    }

    return Object.entries(value as Record<string, unknown>).some(([key, item]) => {
      const keyText = key.toLowerCase()
      return (
        this.nativeSlashTokens.some(token => keyText.includes(token)) ||
        this.hasNativeSlashToken(item, depth + 1, visited)
      )
    })
  }

  private refreshUnit(unitId: string): void {
    const currentRender = this.renderManagerService.getRenderById(unitId) as any
    currentRender?.mainComponent?.makeDirty?.(true)
    currentRender?.scene?.makeDirty?.(true)
    const workbook = this.univerInstanceService.getUnit(unitId, UniverInstanceType.UNIVER_SHEET) as any
    workbook?.getActiveSheet?.()?.refreshCanvas?.()
  }
}

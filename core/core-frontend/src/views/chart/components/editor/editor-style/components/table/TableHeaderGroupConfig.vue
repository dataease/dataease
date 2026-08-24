<template>
  <div
    :id="containerId"
    class="table-container"
    ref="tableContainer"
    :class="{ dark: themes === 'dark' }"
  ></div>
  <div class="button-group">
    <el-button :effect="themes" @click="onCancelConfig">{{ t('chart.cancel') }}</el-button>
    <el-button type="primary" @click="onConfigChange">{{ t('chart.confirm') }}</el-button>
  </div>
  <div :id="menuGroupId" class="group-menu" ref="groupMenu"></div>
</template>

<script setup lang="ts">
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { formatterItem, valueFormatter } from '@/views/chart/components/js/formatter'
import {
  BaseTooltip,
  S2DataConfig,
  S2Event,
  S2Options,
  TableSheet,
  TooltipShowOptions,
  ColCell,
  Node,
  RowColumnClick,
  LayoutResult,
  TableDataCell,
  TableColCell,
  TextTheme
} from '@antv/s2'
import { ElMessageBox } from 'element-plus-secondary'
import { cloneDeep, debounce, isEqual, isNumber } from 'lodash-es'
import { computed, nextTick, onMounted, onUnmounted, PropType, ref } from 'vue'
import { uuid } from 'vue-uuid'
import { useI18n } from '@/hooks/web/useI18n'
import {
  getColumns,
  getCustomTheme,
  getLeafNodes,
  mapKeyToField,
  setupColumnTitle
} from '@/views/chart/components/js/panel/common/common_table'

const { t } = useI18n()
const dvMainStore = dvMainStoreWithOut()
const props = defineProps({
  chart: {
    type: Object as PropType<ChartObj>,
    required: true
  },
  themes: {
    type: String as PropType<EditorTheme>,
    default: 'dark'
  },
  propertyInner: {
    type: Array<string>
  }
})
const emits = defineEmits(['onConfigChange', 'onCancelConfig'])
const onCancelConfig = () => {
  emits('onCancelConfig')
}

const allAxis = computed(() => {
  const axis = [...props.chart.xAxis]
  if (props.chart.type === 'table-normal') {
    axis.push(...props.chart.yAxis)
  }
  return axis
})
const onConfigChange = () => {
  const { fields } = s2.dataCfg
  emits('onConfigChange', { columns: fields.columns, meta: [] })
}

const init = () => {
  const chart = cloneDeep(props.chart)
  const { headerGroupConfig } = chart.customAttr.tableHeader
  const showColumns = []
  allAxis.value?.forEach(axis => {
    axis.hide !== true &&
      showColumns.push({ field: axis.dataeaseName, title: axis.chartShowName ?? axis.name })
  })
  if (!showColumns.length) {
    return
  }
  if (headerGroupConfig?.columns?.length) {
    // 处理历史数据
    if (headerGroupConfig.columns[0].key) {
      mapKeyToField(headerGroupConfig.columns)
      const nameFieldMap = showColumns.reduce((pre, cur) => {
        pre[cur.field] = cur.title
        return pre
      }, {})
      if (headerGroupConfig.meta?.length) {
        headerGroupConfig.meta.forEach(item => {
          nameFieldMap[item.field] = item.name
        })
        delete headerGroupConfig.meta
      }
      setupColumnTitle(headerGroupConfig.columns, nameFieldMap)
    }
    const allKeys = showColumns.map(item => item.field)
    const leafNodes = getLeafNodes(headerGroupConfig.columns)
    const leafKeys = leafNodes.map(item => item.field)
    const { columns } = headerGroupConfig
    if (!isEqual(allKeys, leafKeys)) {
      columns.splice(0, columns.length, ...showColumns)
    } else {
      const nameMap = showColumns.reduce((pre, cur) => {
        pre[cur.field] = cur.title
        return pre
      }, {})
      setupColumnTitle(columns, nameMap)
    }
  } else {
    chart.customAttr.tableHeader.headerGroupConfig = {
      columns: [...showColumns]
    }
  }
  nextTick(() => {
    renderTable(chart)
  })
}
const menuGroupId = computed(() => {
  return 'menu-group-' + props.chart.id
})
const containerId = computed(() => {
  return 'table-container-' + props.chart.id
})
const groupMenu = ref<HTMLDivElement>()
const tableContainer = ref<HTMLDivElement>()
let s2: TableSheet
class CustomDataCell extends TableDataCell {
  protected getTextStyle(): TextTheme {
    const textStyle = super.getTextStyle()
    const dataCellAlignConfig = (this.theme as any)?.dataCellAlignConfig
    if (dataCellAlignConfig) {
      const align = dataCellAlignConfig[this.meta.valueField]
      if (align) {
        textStyle.textAlign = align
      }
    }
    if (textStyle.textAlign === 'custom') {
      textStyle.textAlign = 'left'
    }
    return textStyle
  }
}
class CustomColCell extends TableColCell {
  protected getTextStyle(): TextTheme {
    const textStyle = super.getTextStyle()
    const colCellAlignConfig = (this.theme as any)?.colCellAlignConfig
    if (colCellAlignConfig) {
      // 分组单元格居中
      if (this.meta.children?.length) {
        textStyle.textAlign = 'center'
        return textStyle
      }
      const align = colCellAlignConfig[this.meta.field]
      if (align) {
        textStyle.textAlign = align
      }
    }
    if (textStyle.textAlign === 'custom') {
      textStyle.textAlign = 'left'
    }
    return textStyle
  }
}
const renderTable = (chart: ChartObj) => {
  const data = dvMainStore.getPureCanvasViewDataInfo(chart.id)
  const containerDom = document.getElementById(containerId.value)
  let realData = []
  if (data?.tableRow?.length) {
    realData = data.tableRow.slice(0, 10)
  }
  const { headerGroupConfig } = chart.customAttr.tableHeader
  const meta = []
  const columns = headerGroupConfig.columns
  const axisMap = chart.xAxis.reduce((pre, cur) => {
    pre[cur.dataeaseName] = cur
    return pre
  }, {})
  if (data?.fields?.length) {
    data.fields.forEach(ele => {
      const f = axisMap[ele.dataeaseName]
      if (f?.hide === true) {
        return
      }
      meta.push({
        field: ele.dataeaseName,
        formatter: function (value) {
          if (!f) {
            return value
          }
          if (value === null || value === undefined) {
            return value
          }
          if (![2, 3].includes(f.deType) || !isNumber(value)) {
            return value
          }
          let formatCfg = f.formatterCfg
          if (!formatCfg) {
            formatCfg = formatterItem
          }
          return valueFormatter(value, formatCfg)
        }
      })
    })
  }
  // // data config
  const s2DataConfig: S2DataConfig = {
    fields: {
      columns
    },
    meta,
    data: realData
  }
  // options
  const s2Options: S2Options = {
    width: containerDom.offsetWidth,
    height: containerDom.offsetHeight,
    tooltip: {
      enable: false,
      getContainer: () => containerDom,
      render: sheet => new GroupMenu(sheet),
      autoAdjustBoundary: null,
      adjustPosition(positionInfo) {
        const {
          position: { x, y }
        } = positionInfo
        const scrollWidth = containerDom.scrollLeft
        const menuWidth = groupMenu.value?.offsetWidth || 120
        const containerWidth = containerDom.offsetWidth
        if (x - scrollWidth + menuWidth > containerWidth) {
          return { x: x - menuWidth, y: y + 10 }
        }
        return { x: x, y: y + 10 }
      },
      style: {
        position: 'absolute',
        borderRadius: '4px'
      }
    },
    interaction: {
      rangeSelection: false,
      resize: {
        colCellHorizontal: false,
        colCellVertical: false,
        rowCellVertical: false
      }
    },
    dataCell: (meta, sheet) => {
      return new CustomDataCell(meta, sheet)
    },
    colCell: (meta, sheet, config) => {
      return new CustomColCell(meta, sheet, config)
    }
  }
  s2 = new TableSheet(containerDom, s2DataConfig, s2Options)
  const { tableHeader, tableCell } = chart.customAttr
  const theme = getCustomTheme(chart)
  if (tableHeader.tableHeaderAlign === 'custom') {
    theme.colCellAlignConfig =
      tableHeader.alignConfig?.reduce((pre, cur) => {
        pre[cur.id] = cur.align
        return pre
      }, {}) || {}
  }
  if (tableCell.tableItemAlign === 'custom') {
    theme.dataCellAlignConfig =
      tableCell.alignConfig?.reduce((pre, cur) => {
        pre[cur.id] = cur.align
        return pre
      }, {}) || {}
  }
  s2.setTheme(theme)
  s2.on(S2Event.COL_CELL_CONTEXT_MENU, e => {
    const curColumns = s2.dataCfg.fields.columns as Array<ColumnNode>
    const activeCells = s2.interaction.getActiveColCells()
    const activeColFields = activeCells?.map(cell => cell.getMeta().field)
    const activeColumns = getColumns(activeColFields, curColumns)
    const curCell = s2.getCell(e.target)
    groupMenu.value.innerText = ''
    // 右键点击的目标单元格不在已选的单元格中，清空已选单元格，隐藏菜单
    if (activeColumns?.length) {
      const index = activeColumns.findIndex(cell => cell.field === curCell.getMeta().field)
      if (index === -1) {
        s2.interaction.clearState()
        s2.hideTooltip()
        return
      }
    }
    //只有一个cell，并且colIndex为-1，那就是组合的，显示取消分组按钮和重命名按钮
    if (activeColumns?.length === 1 && curCell.getMeta().colIndex === -1) {
      s2.interaction.clearState()
      s2.interaction.selectCell(curCell, { animate: false })
      const cancelBtn = document.createElement('span')
      groupMenu.value.appendChild(cancelBtn)
      cancelBtn.innerText = t('chart.cancel_group')
      cancelBtn.onclick = () => {
        s2.hideTooltip()
        const parent = curCell.getMeta().parent
        if (parent?.id === 'root') {
          const startIndex = curColumns.findIndex(cell => cell.field === curCell.getMeta().field)
          const [curCol] = getColumns([curCell.getMeta().field], curColumns)
          curColumns.splice(startIndex, 1, ...curCol.children)
          s2.setDataCfg({
            fields: {
              columns: curColumns
            }
          })
          s2.render(true)
        } else {
          const [parentColumn] = getColumns([parent.field], curColumns)
          if (parentColumn) {
            const startIndex = parentColumn.children?.findIndex(
              cell => cell.field === curCell.getMeta().field
            )
            const [curCol] = getColumns([curCell.getMeta().field], parentColumn.children)
            parentColumn.children?.splice(startIndex, 1, ...curCol.children)
            s2.setDataCfg({
              fields: {
                columns: curColumns
              }
            })
            s2.render(true)
          }
        }
        s2.interaction.clearState()
      }
      const cancelAllBtn = document.createElement('span')
      groupMenu.value.appendChild(cancelAllBtn)
      cancelAllBtn.innerText = t('chart.cancel_all_group')
      cancelAllBtn.onclick = () => {
        s2.hideTooltip()
        const parent = curCell.getMeta().parent
        if (parent?.id === 'root') {
          const [curCol] = getColumns([curCell.getMeta().field], curColumns)
          const leafNodes = getLeafNodes(curCol.children)
          const startIndex = curColumns.findIndex(cell => cell.field === curCell.getMeta().field)
          curColumns.splice(startIndex, 1, ...leafNodes)
          s2.setDataCfg({
            fields: {
              columns: curColumns
            }
          })
          s2.render(true)
        } else {
          const [parentColumn] = getColumns([parent.field], curColumns)
          if (parentColumn) {
            const [curCol] = getColumns([curCell.getMeta().field], parentColumn.children)
            const leafNodes = getLeafNodes(curCol.children)
            const startIndex = parentColumn.children?.findIndex(
              cell => cell.field === curCell.getMeta().field
            )
            parentColumn.children?.splice(startIndex, 1, ...leafNodes)
            s2.setDataCfg({
              fields: {
                columns: curColumns
              }
            })
            s2.render(true)
          }
        }
        s2.interaction.clearState()
      }
      const renameBtn = document.createElement('span')
      groupMenu.value.appendChild(renameBtn)
      renameBtn.innerText = t('chart.rename')
      renameBtn.onclick = () => {
        s2.hideTooltip()
        const [curColumn] = getColumns([curCell.getMeta().field], curColumns)
        ElMessageBox.prompt('', t('chart.group_name'), {
          confirmButtonText: t('chart.confirm'),
          cancelButtonText: t('chart.cancel'),
          showClose: false,
          showInput: true,
          inputPlaceholder: t('chart.group_name_edit_tip'),
          inputValue: curColumn.title,
          inputErrorMessage: t('chart.group_name_error_tip'),
          // 正则校验，长度 1-50
          inputValidator: val => {
            if (val?.length < 1 || val?.length > 50) {
              return t('chart.group_name_error_tip')
            }
            return true
          }
        })
          .then(res => {
            curColumn.title = res.value
            s2.setDataCfg({
              columns: curColumns
            })
            s2.render(true)
          })
          .catch(() => {
            // do nothing
          })
      }
      s2.showTooltip({
        position: {
          x: e.x,
          y: e.y
        },
        content: groupMenu.value
      })
      return
    }
    //如果有多个cell都在同一个层级，并且parent相同，那就是可以进行合并分组操作
    if (activeColumns?.length > 1) {
      const sameParent = activeCells.every(
        cell => cell.getMeta().parent.id === curCell.getMeta().parent.id
      )
      if (!sameParent) {
        return
      }
      let upDepth = -1
      let tmpCell = curCell
      while (tmpCell?.getMeta?.()?.parent || tmpCell?.parent) {
        upDepth++
        tmpCell = tmpCell?.getMeta?.()?.parent || tmpCell?.parent
      }
      let startIndex = -1
      let endIndex = -1
      const parent = curCell.getMeta().parent
      // 分组的节点
      if (parent.colIndex !== -1) {
        activeColumns.forEach(cell => {
          const index = parent.children.findIndex(item => item.getMeta().field === cell.field)
          if (index < startIndex || startIndex === -1) {
            startIndex = index
          }
          if (index > endIndex || endIndex === -1) {
            endIndex = index
          }
        })
      } else {
        activeColumns.forEach(cell => {
          const index = parent.children.findIndex(item => item.field === cell.field)
          if (index < startIndex || startIndex === -1) {
            startIndex = index
          }
          if (index > endIndex || endIndex === -1) {
            endIndex = index
          }
        })
      }
      const totalColumns = []
      if (parent?.id === 'root') {
        totalColumns.push(...curColumns.slice(startIndex, endIndex + 1))
      } else {
        const [parentColumn] = getColumns([parent.field], curColumns)
        totalColumns.push(...parentColumn.children?.slice(startIndex, endIndex + 1))
      }
      const chiildDepth = getTreesMaxDepth(totalColumns)
      // 最大分组为 3 级
      if (chiildDepth + upDepth > 1) {
        return
      }
      const mergeBtn = document.createElement('span')
      groupMenu.value.appendChild(mergeBtn)
      mergeBtn.innerText = t('chart.merge_group')
      mergeBtn.onclick = () => {
        s2.hideTooltip()
        ElMessageBox.prompt('', t('chart.group_name'), {
          confirmButtonText: t('chart.confirm'),
          cancelButtonText: t('chart.cancel'),
          showClose: false,
          showInput: true,
          inputPlaceholder: t('chart.group_name_edit_tip'),
          inputErrorMessage: t('chart.group_name_error_tip'),
          inputValue: t('chart.group'),
          // 正则校验，长度 1-50
          inputValidator: val => {
            if (val?.length < 1 || val?.length > 50) {
              return t('chart.group_name_error_tip')
            }
            return true
          }
        })
          .then(res => {
            if (parent?.id === 'root') {
              const newField = uuid.v4()
              curColumns?.splice(startIndex, endIndex - startIndex + 1, {
                field: newField,
                children: totalColumns,
                title: res.value
              })
              s2.setDataCfg({
                fields: {
                  columns: curColumns
                }
              })
              s2.render(true)
            } else {
              const [parentColumn] = getColumns([parent.field], curColumns)
              const newField = uuid.v4()
              parentColumn.children?.splice(startIndex, endIndex - startIndex + 1, {
                field: newField,
                children: totalColumns,
                title: res.value
              })
              s2.setDataCfg({
                fields: {
                  columns: curColumns
                }
              })
              s2.render(true)
            }
            s2.interaction.clearState()
          })
          .catch(() => {
            // do nothing
          })
      }
      s2.showTooltip({
        position: {
          x: e.x,
          y: e.y
        },
        content: groupMenu.value
      })
      return
    }
  })
  s2.on(S2Event.COL_CELL_CLICK, e => {
    const lastCell = s2.store.get('lastClickedCell') as ColCell
    const originEvent = e.originalEvent as unknown as MouseEvent
    if (!lastCell || !(originEvent?.ctrlKey || originEvent?.metaKey || originEvent?.shiftKey)) {
      const cell = s2.getCell(e.target)
      s2.store.set('lastClickedCell', cell)
      return
    }
    if (originEvent?.shiftKey) {
      if (!lastCell) {
        const cell = s2.getCell(e.target)
        s2.store.set('lastClickedCell', cell)
        return
      }
      const curCell = s2.getCell(e.target)
      const lastMeta = lastCell.getMeta()
      const curMeta = curCell.getMeta()
      if (
        lastMeta.field === curMeta.field ||
        lastMeta.level !== curMeta.level ||
        lastMeta.parent !== curMeta.parent
      ) {
        return
      }
      const parent = curMeta.parent as Node
      const lastIndex = parent.children.findIndex(item => item.field === lastMeta.field)
      const curIndex = parent.children.findIndex(item => item.field === curMeta.field)
      const startIndex = Math.min(lastIndex, curIndex)
      const endIndex = Math.max(lastIndex, curIndex)
      const activeCells = parent.children.slice(startIndex, endIndex + 1)
      s2.interaction.clearState()
      activeCells.forEach(cell => {
        s2.interaction.changeCell({ cell: cell.belongsCell, isMultiSelection: true })
      })
    }
  })
  s2.once(S2Event.LAYOUT_AFTER_HEADER_LAYOUT, (e: LayoutResult) => {
    const initialized = s2.store.get('initialized')
    if (initialized) {
      return
    }
    s2.store.set('initialized', true)
    s2.changeSheetSize(e.colsHierarchy.width)
    const length = s2.dataCfg.data?.length || 0
    const headerHeight = e.colsHierarchy.height
    const rowHeight = s2.options.style.dataCell.height
    const totalHeight = headerHeight + rowHeight * length
    if (containerDom.offsetHeight > totalHeight) {
      containerDom.style.height = totalHeight + 'px'
    }
    s2.render(false)
  })

  s2.render().then(() => {
    s2.getCanvasElement().addEventListener('contextmenu', e => {
      e.preventDefault()
    })
    // 处理右键不触发行列头的 click 事件，不然会清空选中状态
    const rowCelCLick = s2.interaction.interactions.get('rowColumnClick') as RowColumnClick
    const originClickHandler = rowCelCLick['handleRowColClick']
    const hookedClickHandler = e => {
      if (e.button === 2) {
        return
      }
      originClickHandler.call(rowCelCLick, e)
    }
    rowCelCLick['handleRowColClick'] = hookedClickHandler
  })
}

const getTreesMaxDepth = (nodes: Array<ColumnNode>): number => {
  if (!nodes?.length) {
    return 0
  }

  // 获取单个节点的最大子树深度
  const getNodeMaxDepth = (node: ColumnNode): number => {
    if (!node.children || node.children.length === 0) {
      return 0
    }
    const childrenDepths = node.children.map(child => getNodeMaxDepth(child as ColumnNode))
    return Math.max(...childrenDepths) + 1
  }

  // 计算所有根节点的最大深度
  const rootDepths = nodes.map(node => getNodeMaxDepth(node))
  return Math.max(...rootDepths)
}

const resize = debounce((width, height) => {
  if (!s2) {
    return
  }
  const tableHeight = s2.container.context.config.container.offsetHeight
  if (height > tableHeight) {
    const dom = document.getElementById(containerId.value)
    dom.style.height = tableHeight + 'px'
  }
  s2.changeSheetSize(undefined, height)
  s2.render(false)
}, 500)
const preSize = [0, 0]
const TOLERANCE = 1
let resizeObserver: ResizeObserver
onMounted(() => {
  init()
  resizeObserver = new ResizeObserver(([entry] = []) => {
    const [size] = entry.borderBoxSize || [] // 拖动的时候宽高重新计算，误差范围内不重绘，误差先设置为1
    if (!(preSize[0] || preSize[1])) {
      preSize[0] = size.inlineSize
      preSize[1] = size.blockSize
    }
    const heightOffset = Math.abs(size.blockSize - preSize[1])
    if (heightOffset < TOLERANCE) {
      return
    }
    preSize[0] = size.inlineSize
    preSize[1] = size.blockSize
    resize(Math.round(size.blockSize))
  })
  resizeObserver.observe(document.getElementById(containerId.value))
})
onUnmounted(() => {
  resizeObserver?.disconnect()
})
class GroupMenu extends BaseTooltip {
  show<T = string | Element>(showOptions: TooltipShowOptions<T>): void {
    super.show(showOptions)
    this.container.style.display = 'flex'
  }
  hide(): void {
    if (this.container) {
      this.container.style.display = 'none'
    }
  }
}
</script>

<style scoped lang="less">
.table-container {
  position: relative;
  width: 100%;
  height: 40vh;
  overflow-x: auto;
  overflow-y: hidden;
  &.dark {
    scrollbar-color: #3a3a3a #1a1a1a;
  }
}

.group-menu {
  display: flex;
  flex: 1;
  flex-direction: column;
  justify-content: space-between;
  color: black;
  font-size: 14px;
  :deep(span) {
    cursor: pointer;
    padding: 5px 10px;
    word-break: keep-all;
    &:hover {
      background-color: var(--ed-fill-color-light);
    }
  }
}
.button-group {
  display: flex;
  justify-content: end;
  margin-top: 4vh;
}
</style>

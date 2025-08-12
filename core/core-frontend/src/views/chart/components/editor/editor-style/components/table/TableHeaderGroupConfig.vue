<template>
  <div :id="containerId" class="table-container" :class="{ dark: themes === 'dark' }"></div>
  <div class="button-group">
    <el-button :effect="themes" @click="onCancelConfig">{{ t('chart.cancel') }}</el-button>
    <el-button type="primary" @click="onConfigChange">{{ t('chart.confirm') }}</el-button>
  </div>
  <div :id="menuGroupId" class="group-menu"></div>
</template>

<script setup lang="ts">
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { formatterItem, valueFormatter } from '@/views/chart/components/js/formatter'
import {
  BaseTooltip,
  ColumnNode,
  S2DataConfig,
  S2Event,
  S2Options,
  TableSheet,
  TooltipShowOptions,
  ColCell,
  Node,
  LayoutResult
} from '@antv/s2'
import { ElMessageBox } from 'element-plus-secondary'
import { cloneDeep, debounce, isEqual, isNumber } from 'lodash-es'
import { computed, nextTick, onMounted, onUnmounted, PropType } from 'vue'
import { uuid } from 'vue-uuid'
import { useI18n } from '@/hooks/web/useI18n'
import {
  getColumns,
  getCustomTheme,
  getLeafNodes
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
  const showAxis = allAxis.value
    ?.map(axis => axis.hide !== true && axis.dataeaseName)
    .filter(i => i)
  const { fields, meta } = s2.dataCfg
  const groupMeta = meta.filter(item => !showAxis.includes(item.field))
  emits('onConfigChange', { columns: fields.columns, meta: groupMeta })
}

const init = () => {
  const chart = cloneDeep(props.chart)
  const { headerGroupConfig } = chart.customAttr.tableHeader
  const showColumns = []
  allAxis.value?.forEach(axis => {
    axis.hide !== true && showColumns.push({ key: axis.dataeaseName })
  })
  if (!showColumns.length) {
    return
  }
  if (headerGroupConfig?.columns?.length) {
    const allAxis = showColumns.map(item => item.key)
    const leafNodes = getLeafNodes(headerGroupConfig.columns)
    const leafKeys = leafNodes.map(item => item.key)
    if (!isEqual(allAxis, leafKeys)) {
      const { columns, meta } = headerGroupConfig
      columns.splice(0, columns.length, ...showColumns)
      meta.splice(0, meta.length)
    }
  } else {
    chart.customAttr.tableHeader.headerGroupConfig = {
      columns: [...showColumns],
      meta: []
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
let s2: TableSheet
const renderTable = (chart: ChartObj) => {
  const data = dvMainStore.getViewDataDetails(chart.id)
  const containerDom = document.getElementById(containerId.value)
  let realData = []
  if (data?.tableRow?.length) {
    realData = data.tableRow.slice(0, 10)
  }
  const { headerGroupConfig } = chart.customAttr.tableHeader
  const meta = [...headerGroupConfig.meta]
  const columns = headerGroupConfig.columns
  const axisMap = allAxis.value.reduce((pre, cur) => {
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
        name: ele.chartShowName ?? ele.name,
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
  } else {
    allAxis.value?.forEach(axis => {
      if (axis.hide !== true) {
        meta.push({
          field: axis.dataeaseName,
          name: axis.chartShowName ?? axis.name
        })
      }
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
    width: containerDom.getBoundingClientRect().width,
    height: containerDom.offsetHeight,
    tooltip: {
      getContainer: () => containerDom,
      renderTooltip: sheet => new GroupMenu(sheet),
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
    }
  }
  s2 = new TableSheet(containerDom, s2DataConfig, s2Options)
  const theme = getCustomTheme(chart)
  s2.setTheme(theme)
  const groupMenuContainer = document.getElementById(menuGroupId.value)
  s2.on(S2Event.COL_CELL_CONTEXT_MENU, e => {
    e.preventDefault()
    const curColumns = s2.dataCfg.fields.columns as Array<ColumnNode>
    const curMeta = s2.dataCfg.meta
    const activeCells = s2.interaction.getActiveCells()
    const colKeys = activeCells?.map(cell => cell.getMeta().field)
    const activeColumns = getColumns(colKeys, curColumns)
    const curCell = s2.getCell(e.target)
    groupMenuContainer.innerText = ''
    // 右键点击的目标单元格不在已选的单元格中，清空已选单元格，隐藏菜单
    if (activeColumns?.length) {
      const index = activeColumns.findIndex(cell => cell.key === curCell.getMeta().field)
      if (index === -1) {
        s2.interaction.clearState()
        s2.hideTooltip()
        return
      }
    }
    //只有一个cell，并且colIndex为-1，那就是组合的，显示取消分组按钮和重命名按钮
    if (activeColumns?.length === 1 && curCell.getMeta().colIndex === -1) {
      s2.interaction.clearState()
      s2.interaction.selectHeaderCell({ cell: curCell })
      const cancelBtn = document.createElement('span')
      groupMenuContainer.appendChild(cancelBtn)
      cancelBtn.innerText = t('chart.cancel_group')
      cancelBtn.onclick = () => {
        s2.hideTooltip()
        const parent = curCell.getMeta().parent
        if (parent?.id === 'root') {
          const startIndex = curColumns.findIndex(cell => cell.key === curCell.getMeta().field)
          const [curCol] = getColumns([curCell.getMeta().field], curColumns)
          curColumns.splice(startIndex, 1, ...curCol.children)
          const index = curMeta.findIndex(meta => meta.field === curCell.getMeta().field)
          curMeta.splice(index, 1)
          s2.setDataCfg({
            fields: {
              columns: curColumns
            },
            meta: curMeta
          })
          s2.render(true)
        } else {
          const [parentColumn] = getColumns([parent.field], curColumns)
          if (parentColumn) {
            const startIndex = parentColumn.children?.findIndex(
              cell => cell.key === curCell.getMeta().field
            )
            const [curCol] = getColumns([curCell.getMeta().field], parentColumn.children)
            parentColumn.children?.splice(startIndex, 1, ...curCol.children)
            const index = curMeta.findIndex(meta => meta.field === curCell.getMeta().field)
            curMeta.splice(index, 1)
            s2.setDataCfg({
              fields: {
                columns: curColumns
              },
              meta: curMeta
            })
            s2.render(true)
          }
        }
        s2.interaction.clearState()
      }
      const cancelAllBtn = document.createElement('span')
      groupMenuContainer.appendChild(cancelAllBtn)
      cancelAllBtn.innerText = t('chart.cancel_all_group')
      cancelAllBtn.onclick = () => {
        s2.hideTooltip()
        const parent = curCell.getMeta().parent
        if (parent?.id === 'root') {
          const [curCol] = getColumns([curCell.getMeta().field], curColumns)
          const leafNodes = getLeafNodes(curCol.children)
          const startIndex = curColumns.findIndex(cell => cell.key === curCell.getMeta().field)
          curColumns.splice(startIndex, 1, ...leafNodes)
          const noneLeafNodes = getNonLeafNodes([curCol])
          const newMeta = curMeta.filter(meta => !noneLeafNodes.includes(meta.field))
          s2.setDataCfg({
            fields: {
              columns: curColumns
            },
            meta: newMeta
          })
          s2.render(true)
        } else {
          const [parentColumn] = getColumns([parent.field], curColumns)
          if (parentColumn) {
            const [curCol] = getColumns([curCell.getMeta().field], parentColumn.children)
            const leafNodes = getLeafNodes(curCol.children)
            const startIndex = parentColumn.children?.findIndex(
              cell => cell.key === curCell.getMeta().field
            )
            parentColumn.children?.splice(startIndex, 1, ...leafNodes)
            const noneLeafNodes = getNonLeafNodes([curCol])
            const newMeta = curMeta.filter(meta => !noneLeafNodes.includes(meta.field))
            s2.setDataCfg({
              fields: {
                columns: curColumns
              },
              meta: newMeta
            })
            s2.render(true)
          }
        }
        s2.interaction.clearState()
      }
      const renameBtn = document.createElement('span')
      groupMenuContainer.appendChild(renameBtn)
      renameBtn.innerText = t('chart.rename')
      renameBtn.onclick = () => {
        s2.hideTooltip()
        const cellMeta = curMeta.find(meta => meta.field === curCell.getMeta().field)
        ElMessageBox.prompt('', t('chart.group_name'), {
          confirmButtonText: t('chart.confirm'),
          cancelButtonText: t('chart.cancel'),
          showClose: false,
          showInput: true,
          inputPlaceholder: t('chart.group_name_edit_tip'),
          inputValue: cellMeta.name,
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
            cellMeta.name = res.value
            s2.setDataCfg({
              meta: curMeta
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
        content: groupMenuContainer
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
          const index = parent.children.findIndex(item => item.getMeta().field === cell.key)
          if (index < startIndex || startIndex === -1) {
            startIndex = index
          }
          if (index > endIndex || endIndex === -1) {
            endIndex = index
          }
        })
      } else {
        activeColumns.forEach(cell => {
          const index = parent.children.findIndex(item => item.key === cell.key)
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
      groupMenuContainer.appendChild(mergeBtn)
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
              const newKey = uuid.v4()
              curColumns?.splice(startIndex, endIndex - startIndex + 1, {
                key: newKey,
                children: totalColumns
              })
              curMeta.push({
                field: newKey,
                name: res.value
              })
              s2.setDataCfg({
                fields: {
                  columns: curColumns
                },
                meta: curMeta
              })
              s2.render(true)
            } else {
              const [parentColumn] = getColumns([parent.field], curColumns)
              const newKey = uuid.v4()
              parentColumn.children?.splice(startIndex, endIndex - startIndex + 1, {
                key: newKey,
                children: totalColumns
              })
              curMeta.push({
                field: newKey,
                name: res.value
              })
              s2.setDataCfg({
                fields: {
                  columns: curColumns
                },
                meta: curMeta
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
        content: groupMenuContainer
      })
      return
    }
  })
  s2.on(S2Event.COL_CELL_CLICK, e => {
    const lastCell = s2.store.get('lastClickedCell') as ColCell
    const originEvent = e.originalEvent as MouseEvent
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
        lastMeta.key === curMeta.key ||
        lastMeta.level !== curMeta.level ||
        lastMeta.parent !== curMeta.parent
      ) {
        return
      }
      const parent = curMeta.parent as Node
      const lastIndex = parent.children.findIndex(item => item.key === lastMeta.key)
      const curIndex = parent.children.findIndex(item => item.key === curMeta.key)
      const startIndex = Math.min(lastIndex, curIndex)
      const endIndex = Math.max(lastIndex, curIndex)
      const activeCells = parent.children.slice(startIndex, endIndex + 1)
      s2.interaction.clearState()
      activeCells.forEach(cell => {
        s2.interaction.selectHeaderCell({ cell: cell.belongsCell, isMultiSelection: true })
      })
    }
  })
  s2.once(S2Event.LAYOUT_AFTER_HEADER_LAYOUT, (e: LayoutResult) => {
    const initialized = s2.store.get('initialized')
    if (!initialized) {
      s2.store.set('initialized', true)
      s2.changeSheetSize(e.colsHierarchy.width)
      const length = s2.dataCfg.data?.length || 0
      const headerHeight = e.colsHierarchy.height
      const rowHeight = s2.options.style.cellCfg.height
      const totalHeight = headerHeight + rowHeight * length
      if (containerDom.offsetHeight > totalHeight) {
        containerDom.style.height = totalHeight + 'px'
      }
      s2.render(false)
    }
  })
  s2.render()
}

const getNonLeafNodes = (tree: Array<ColumnNode>): string[] => {
  const result: string[] = []

  const inorderTraversal = (node: ColumnNode) => {
    // 如果有子节点，则为非叶子节点
    if (node.children?.length > 0) {
      result.push(node.key)

      // 递归处理子节点
      for (let i = 0; i < node.children.length; i++) {
        inorderTraversal(node.children[i] as ColumnNode)
      }
    }
  }

  // 遍历树中所有节点
  tree.forEach(node => inorderTraversal(node))

  return result
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

const resize = debounce(height => {
  if (s2) {
    const tableHeight = s2.container.cfg.height
    if (height > tableHeight) {
      const dom = document.getElementById(containerId.value)
      dom.style.height = tableHeight + 'px'
    }
    s2.changeSheetSize(undefined, height)
    s2.render(false)
  }
}, 500)
const preSize = [0, 0]
const TOLERANCE = 1
let resizeObserver: ResizeObserver
onMounted(() => {
  init()
  resizeObserver = new ResizeObserver(([entry] = []) => {
    const [size] = entry.borderBoxSize || []
    // 拖动的时候宽高重新计算，误差范围内不重绘，误差先设置为1
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

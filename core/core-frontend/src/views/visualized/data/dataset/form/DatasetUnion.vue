<script lang="ts" setup>
import { reactive, computed, ref, nextTick, inject, type Ref, watch, unref } from 'vue'
import AddSql from './AddSql.vue'
import { useI18n } from '@/hooks/web/useI18n'
import zeroNodeImg from '@/assets/img/drag.png'
import { ElMessageBox, type Action } from 'element-plus-secondary'
import { guid } from './util'
import { HandleMore } from '@/components/handle-more'
import { propTypes } from '@/utils/propTypes'
import UnionFieldList from './UnionFieldList.vue'
import type { Node, Field } from './util'
import { getTableField } from '@/api/dataset'
import type { SqlNode } from './AddSql.vue'
import { cloneDeep } from 'lodash-es'
import type { Table } from '@/api/dataset'
import { useAppearanceStoreWithOut } from '@/store/modules/appearance'
const appearanceStore = useAppearanceStoreWithOut()
const state = reactive({
  nodeList: [],
  visualNode: null,
  visualNodeParent: null,
  visualPath: null
})
const props = defineProps({
  maskShow: propTypes.bool.def(false),
  offsetX: propTypes.number.def(0),
  offsetY: propTypes.number.def(0),
  dragHeight: propTypes.number.def(260),
  getDsName: propTypes.func
})

const primaryColor = computed(() => {
  return appearanceStore.themeColor === 'custom' ? appearanceStore.customColor : '#3370FF'
})

const iconName = {
  left: 'icon_left-association',
  right: 'icon_right-association',
  inner: 'icon_intersect',
  full: 'icon_full-association'
}
const { t } = useI18n()
const editSqlField = ref(false)

const nodeField = ref<Field[]>([])

const currentNode = ref<Node>()

const sqlNode = ref<SqlNode>()

const allfields = inject('allfields') as Ref

const getNodeField = ({ datasourceId, id, info, tableName, type, currentDsFields }) => {
  return getTableField({ datasourceId, id, info, tableName, type })
    .then(res => {
      const idOriginNameMap = allfields.value.reduce((pre, next) => {
        pre[`${next.datasetTableId}${next.originName}`] = next.id
        return pre
      }, {})
      nodeField.value = res as unknown as Field[]
      nodeField.value.forEach(ele => {
        ele.id = idOriginNameMap[`${id}${ele.originName}`]
        ele.checked = currentDsFields.map(ele => ele.originName).includes(ele.originName)
      })
    })
    .finally(() => {
      editUnion.value = true
    })
}

const nodeNameList = computed(() => {
  const arr = []
  dfsNodeNameList(state.nodeList, arr)
  return arr
})

const dragMask = ref()

const handleClickOutside = ev => {
  if (ev.target === dragMask.value) {
    activeNodeId.value = ''
  }
}

const activeNodeId = ref('')

let shadowWidth = 0
let shadowHeight = 0

const setLayOut = () => {
  svgWidth.value = `${shadowWidth * 100 + (shadowWidth + 1) * 200 + 48}px`
  svgHeight.value = `${shadowHeight * 24 + (shadowHeight + 1) * 32 + 48}px`
}
const dfsNodeList = computed(() => {
  let nodeListLocation = []
  shadowWidth = 0
  shadowHeight = 0
  dfsNode(state.nodeList, nodeListLocation)
  setLayOut()
  return nodeListLocation
})

const dfsNodeNameList = (list, arr) => {
  list.forEach(ele => {
    arr.push(`${ele.tableName}${ele.datasourceId}`)
    if (ele.children?.length) {
      dfsNodeNameList(ele.children, arr)
    }
  })
}

const dfsForDsId = (arr, datasourceId) => {
  return arr.every(ele => {
    if (ele.children?.length) {
      return dfsForDsId(ele.children, datasourceId)
    }
    return ele.datasourceId === datasourceId
  })
}

const crossDatasources = computed(() => {
  const { datasourceId, children = [] } = state.nodeList[0] || {}
  if (datasourceId && !!children.length) {
    return !dfsForDsId(children, datasourceId)
  }
  return false
})

let isUpdate = false

watch(
  () => state.nodeList,
  () => {
    if (isUpdate) {
      emits('changeUpdate')
    }
  },
  { deep: true }
)

const initState = nodeList => {
  Object.assign(state.nodeList, nodeList)
  nextTick(() => {
    isUpdate = true
    emits('addComplete')
  })
}

const editUnion = ref(false)

const svgWidth = ref('200px')
const svgHeight = ref('260px')

const delNode = (id, arr) => {
  arr.some((ele, index) => {
    if (id === ele.id) {
      arr.splice(index, 1)
      return true
    }
    if (ele.children?.length) {
      delNode(id, ele.children)
    }
    return false
  })
}
let fakeDelId = []

const collectId = arr => {
  arr.forEach(ele => {
    fakeDelId = [...fakeDelId, ...ele.currentDsFields.map(itx => itx.id)]
    if (ele.children?.length) {
      collectId(ele.children)
    }
  })
}

const delNodeFake = (id, arr) => {
  arr.forEach(ele => {
    if (id === ele.id) {
      fakeDelId = [...ele.currentDsFields.map(itx => itx.id)]
      if (ele.children?.length) {
        collectId(ele.children)
      }
    } else if (ele.children?.length) {
      delNodeFake(id, ele.children)
    }
  })
}

const changeSqlId = ref([])
const changedNodeId = ref([])
const saveSqlNode = (val: SqlNode, cb) => {
  const { tableName, id, sql, datasourceId, sqlVariableDetails = null, changeFlag = false } = val
  if (changeFlag) {
    changedNodeId.value = changedNodeId.value.filter(itx => itx.from !== id && id !== itx.to)
    !changeSqlId.value.includes(id) && changeSqlId.value.push(id)
  }
  if (state.visualNode) {
    Object.assign(state.visualNode, {
      info: JSON.stringify({ table: tableName, sql }),
      sqlVariableDetails,
      unionType: 'left',
      type: 'sql',
      id,
      datasourceId,
      unionFields: [],
      currentDsFields: []
    })
    state.visualNode.confirm = true
    if (!state.nodeList.length) {
      state.visualNode.tableName = tableName
      state.nodeList.push(state.visualNode)
      currentNode.value = state.nodeList[0]
      getTableField({
        datasourceId,
        id: id,
        info: state.visualNode.info,
        tableName,
        type: 'sql'
      }).then(res => {
        nodeField.value = res as unknown as Field[]
        nodeField.value.forEach(ele => {
          ele.checked = true
        })
        state.nodeList[0].currentDsFields = cloneDeep(res)
        cb?.()
        confirmEditUnion()
      })
      confirm()
    }
    return
  }
  const obj = { info: JSON.stringify({ table: tableName, sql }), id, tableName, sqlVariableDetails }
  dfsNodeBack([obj], [id], state.nodeList)
}

const setChangeStatus = (to, from) => {
  if (changedNodeId.value.some(ele => ele.from === from && ele.to === to)) return
  changedNodeId.value.push({ from, to })
}

const closeSqlNode = () => {
  if (
    state.nodeList.length === 1 &&
    !state.nodeList[0].children?.length &&
    changeSqlId.value.length === 1
  ) {
    currentNode.value = state.nodeList[0]
    const { datasourceId, id, info, tableName } = currentNode.value
    getTableField({
      datasourceId,
      id,
      info,
      tableName,
      type: 'sql'
    }).then(res => {
      const idOriginNameMap = allfields.value.reduce((pre, next) => {
        pre[`${next.datasetTableId}${next.originName}`] = next.id
        return pre
      }, {})
      nodeField.value = res as unknown as Field[]
      nodeField.value.forEach(ele => {
        ele.id = idOriginNameMap[`${id}${ele.originName}`]
        ele.checked = true
      })
      state.nodeList[0].currentDsFields = cloneDeep(res)
      editUnion.value = true
    })
    changeSqlId.value = []
  }
  if (state.visualNode?.confirm) {
    nextTick(() => {
      emits('joinEditor', [
        {
          ...state.visualNode,
          tableName: (JSON.parse(state.visualNode.info) as { table: string }).table
        },
        state.visualNodeParent
      ])
    })
    editSqlField.value = false
    return
  }
  editSqlField.value = false
  if (!state.visualNodeParent) {
    state.visualNode = null
    return
  }
  state.visualNodeParent.children = state.visualNodeParent.children.filter(ele => !ele.isShadow)
  confirm()
}

const changeNodeFields = val => {
  currentNode.value.currentDsFields = val
}

const closeEditUnion = () => {
  nodeField.value = []
  currentNode.value = null
  const [fir] = state.nodeList
  if (fir.isShadow) {
    delete fir.isShadow
    state.nodeList = []
    emits('addComplete')
  }
  editUnion.value = false
}
let num = +new Date()

const setGuid = (arr, id, datasourceId) => {
  arr.forEach(ele => {
    if (!ele.id) {
      ele.id = `${++num}`
      ele.datasetTableId = id
      ele.datasourceId = datasourceId
    }
  })
}

const delUpdateDsFields = (id, arr: Node[]) => {
  arr.some(ele => {
    if (id === ele.id) {
      setGuid(currentNode.value.currentDsFields, ele.id, ele.datasourceId)
      ele.currentDsFields = currentNode.value.currentDsFields
      return true
    }
    if (ele.children?.length) {
      delUpdateDsFields(id, ele.children)
    }
    return false
  })
}
const delUpdateDsFieldsFake = (id, arr: Node[]) => {
  arr.forEach(ele => {
    if (id === ele.id) {
      fakeDelId = [...ele.currentDsFields.map(itx => itx.id)]
    }
    if (ele.children?.length) {
      delUpdateDsFieldsFake(id, ele.children)
    }
  })
}

const confirmEditUnion = () => {
  delUpdateDsFieldsFake(currentNode.value.id, state.nodeList)
  const currentIds = currentNode.value.currentDsFields.map(ele => ele.id)
  let ids = fakeDelId.filter(ele => !currentIds.includes(ele))
  fakeDelId = []
  if (!!ids.length) {
    const idArr = allfields.value.reduce((pre, next) => {
      if (next.extField === 2) {
        const idMap = next.originName.match(/\[(.+?)\]/g)
        const result = idMap.map(itm => {
          return itm.slice(1, -1)
        })
        result.forEach(ele => {
          if (ids.includes(ele)) {
            pre.push(ele)
          }
        })
      }
      return pre
    }, [])

    if (!!idArr.length) {
      ElMessageBox.confirm(
        `字段${allfields.value
          .filter(ele => [...new Set(idArr)].includes(ele.id) && ele.extField !== 2)
          .map(ele => ele.name)
          .join(',')}未被选择，其相关的新建字段将被删除，是否继续？`,
        {
          confirmButtonText: t('dataset.confirm'),
          cancelButtonText: t('common.cancel'),
          showCancelButton: true,
          confirmButtonType: 'danger',
          type: 'warning',
          autofocus: false,
          showClose: false,
          callback: (action: Action) => {
            if (action === 'confirm') {
              delUpdateDsFields(currentNode.value.id, state.nodeList)
              const [fir] = state.nodeList
              if (fir.isShadow) {
                delete fir.isShadow
              }
              closeEditUnion()
              nextTick(() => {
                emits('updateAllfields')
              })
            }
          }
        }
      )
      return
    }
  }

  delUpdateDsFields(currentNode.value.id, state.nodeList)
  const [fir] = state.nodeList
  if (fir.isShadow) {
    delete fir.isShadow
  }
  closeEditUnion()
  nextTick(() => {
    emits('updateAllfields')
  })
}

const handleCommand = (ele, command) => {
  if (command === 'editerField') {
    getNodeField(ele)
    currentNode.value = cloneDeep(ele)
  }

  if (command === 'rename') {
    tableRename({ name: ele.tableName, id: ele.id })
  }

  if (command === 'editerSql') {
    const { tableName, datasourceId, info, id, sqlVariableDetails } = ele
    if (ele.type === 'sql') {
      sqlNode.value = {
        sql: ((JSON.parse(info) as { sql: string }) || {}).sql,
        tableName,
        id,
        variables: JSON.parse(sqlVariableDetails),
        datasourceId
      }
      editSqlField.value = true
      return
    }
  }

  if (command === 'del') {
    delNodeFake(ele.id, state.nodeList)
    if (!!fakeDelId.length) {
      const idArr = allfields.value.reduce((pre, next) => {
        if (next.extField === 2) {
          const idMap = next.originName.match(/\[(.+?)\]/g)
          const result = idMap.map(itm => {
            return itm.slice(1, -1)
          })
          result.forEach(ele => {
            if (fakeDelId.includes(ele)) {
              pre.push(ele)
            }
          })
        }
        return pre
      }, [])
      fakeDelId = []

      if (!!idArr.length) {
        ElMessageBox.confirm(
          `字段${allfields.value
            .filter(ele => [...new Set(idArr)].includes(ele.id) && ele.extField !== 2)
            .map(ele => ele.name)
            .join(',')}未被选择，其相关的新建字段将被删除，是否继续？`,
          {
            confirmButtonText: t('dataset.confirm'),
            cancelButtonText: t('common.cancel'),
            showCancelButton: true,
            confirmButtonType: 'danger',
            type: 'warning',
            autofocus: false,
            showClose: false,
            callback: (action: Action) => {
              if (action === 'confirm') {
                delNode(ele.id, state.nodeList)
                nextTick(() => {
                  emits('addComplete')
                  emits('updateAllfields')
                })
              }
            }
          }
        )
        return
      }
    }

    delNode(ele.id, state.nodeList)
    nextTick(() => {
      emits('addComplete')
      emits('updateAllfields')
    })
  }
}

const handlePathClick = ele => {
  const { from, to } = ele
  const arr = []
  const idArr = [from.id, to.id]
  dfsPath(arr, idArr, state.nodeList)
  emits('joinEditor', arr)
}

const dfsPath = (arr, idArr, list) => {
  list.forEach(ele => {
    if (idArr.includes(ele.id)) {
      arr.unshift(ele)
    }
    if (ele.children?.length) {
      dfsPath(arr, idArr, ele.children)
    }
  })
}

const dfsNodeBack = (arr, idArr, list) => {
  list.forEach(ele => {
    if (idArr.includes(ele.id)) {
      idArr.shift()
      const node = arr.shift()
      Object.assign(ele, node)
    }
    if (ele.children?.length) {
      dfsNodeBack(arr, idArr, ele.children)
    }
  })
}

const dfsNodeFieldBack = (list, { originName, datasetTableId }) => {
  list.forEach(ele => {
    if (datasetTableId === ele.id) {
      const currentDsFields = ele.currentDsFields.filter(ele => ele.originName !== originName)
      ele.currentDsFields = currentDsFields
    }
    if (ele.children?.length) {
      dfsNodeFieldBack(ele.children, { originName, datasetTableId })
    }
  })
}

const menuList = [
  {
    svgName: 'icon_text-box_outlined',
    label: '字段选择',
    command: 'editerField'
  },
  {
    svgName: 'icon_delete-trash_outlined',
    label: '删除',
    command: 'del'
  }
]

const sqlMenu = [
  {
    svgName: 'icon_edit_outlined',
    label: '编辑SQL',
    command: 'editerSql'
  },
  {
    svgName: 'icon_rename_outlined',
    label: t('datasource.field_rename'),
    command: 'rename'
  }
]

const dragOffsetX = ref(0)
const dragOffsetY = ref(0)

function elementInteractArea(pos1, pos2) {
  const pos1Width = pos1.right - pos1.left
  const pos1Height = pos1.bottom - pos1.top
  const pos2Width = pos2.right - pos2.left
  const pos2Height = pos2.bottom - pos2.top

  const axisOverlap =
    pos1Width + pos2Width - (Math.max(pos1.right, pos2.right) - Math.min(pos1.left, pos2.left))
  const crossOverlap =
    pos1Height + pos2Height - (Math.max(pos1.bottom, pos2.bottom) - Math.min(pos1.top, pos2.top))
  if (axisOverlap <= 0 || crossOverlap <= 0) {
    return 0
  }
  return axisOverlap * crossOverlap
}

const possibleNodeAreaList = computed(() => {
  let flatArr = []
  leafNode(dfsNodeList.value, flatArr)
  return flatArr.filter(ele => !ele.isShadow)
})

const leafNode = (arr, leafList) => {
  arr.forEach((ele, index) => {
    const fromX = ele.x * 300 + 24
    const fromY = ele.y * 56 + 24
    let toX = fromX + 200
    let toY = fromY + 56
    const next = arr[index + 1]
    if (next) {
      toY = next.y * 56 + 24
    }
    if (ele.children?.length) {
      leafNode(ele.children, leafList)
    }
    if (!!ele.x || (!!ele.y && !!ele.x)) {
      leafList.push({
        ...ele,
        isShadow: ele.isShadow,
        isLeaf: !ele.children?.length,
        fromX,
        fromY,
        toX,
        toY
      })
    }
  })
}

const flatNodeList = computed(() => {
  let flatArr = []
  flatNode(dfsNodeList.value, flatArr)
  return flatArr
})

const flatNode = (arr, flatNodeList) => {
  arr.forEach(ele => {
    flatNodeList.push(ele)
    if (ele.children?.length) {
      flatNode(ele.children, flatNodeList)
    }
  })
}

const flatPathList = computed(() => {
  let flatArr = []
  const [root = {}] = dfsNodeList.value
  flatLine(root, flatArr)
  return flatArr
})

const dfsNode = (arr = [], nodeListLocation, x = 0, y = 0) => {
  arr.map((ele, index) => {
    const pre = nodeListLocation[index - 1]
    if (!ele.children?.length) {
      let idxChild = index + y
      let maxY = 0
      if (pre) {
        const last = pre.children?.length
          ? pre.children[pre.children.length - 1]
          : { y: 0, maxY: 0 }
        idxChild = Math.max(last.y, pre.y) + 1
        maxY += idxChild
        idxChild = Math.max(idxChild, last.maxY)
      }
      nodeListLocation.push({
        ...ele,
        x,
        y: idxChild,
        maxY,
        isShadow: !!ele.isShadow
      })
      shadowHeight = Math.max(idxChild, shadowHeight)
      shadowWidth = Math.max(x, shadowWidth)
    } else {
      const children = []
      const pre = nodeListLocation[index - 1]
      let idx = y
      let maxY = 0
      if (pre) {
        const last = pre.children?.length
          ? pre.children[pre.children.length - 1]
          : { y: 0, maxY: 0 }
        idx = Math.max(last.y, pre.y) + 1
        maxY = last.maxY
      }
      dfsNode(ele.children, children, x + 1, idx ? Math.max(idx, maxY) : idx)
      maxY = Math.max(children[children.length - 1].maxY + 1, maxY)

      nodeListLocation.push({
        ...ele,
        x,
        y: children[0].y,
        maxY,
        isShadow: !!ele.isShadow,
        children
      })
      shadowHeight = Math.max(children[0].y, shadowHeight)
      shadowWidth = Math.max(x, shadowWidth)
    }
  })
}

const dfsNodeShadow = (arr, { tableName, id }, position, parent) => {
  return arr.some((ele, index) => {
    if (ele.tableName === tableName && id === ele.id) {
      const flag = tableName + '_&&' + position
      if (ele.isShadow && state.visualNode.flag === flag) return true
      state.visualNode = {
        tableName: '',
        isShadow: true,
        flag
      }

      if (position === 'b') {
        state.visualNodeParent = parent
        arr.splice(index + 1, 0, state.visualNode)
      } else {
        state.visualNodeParent = ele
        ele.children = [state.visualNode]
      }
      return true
    }
    if (ele.children?.length) {
      return dfsNodeShadow(ele.children, { tableName, id }, position, ele)
    }
    return false
  })
}

const flatLine = (item, flatNodeList) => {
  let sqlChangeFlag = changeSqlId.value.includes(item.id)
  if (item.children?.length) {
    sqlChangeFlag = item.children.some(itx => changeSqlId.value.includes(itx.id)) || sqlChangeFlag
  }
  const from = { ...item, d: '' }
  ;(item.children || []).forEach(ele => {
    let loaclSqlChangeFlag = true
    changedNodeId.value.some(element => {
      if (
        (element.from === item.id && ele.id === element.to) ||
        (element.from === ele.id && item.id === element.to)
      ) {
        loaclSqlChangeFlag = false
        return true
      }
      return false
    })
    flatNodeList.push({
      from,
      sqlChangeFlag: loaclSqlChangeFlag && sqlChangeFlag,
      isShadow: ele.isShadow || item.isShadow,
      to: {
        ...ele
      },
      d:
        ele.y === from.y
          ? `M ${item.x * 300 + 224} ${ele.y * 56 + 40} l 100 0`
          : `M ${item.x * 300 + 240} ${from.y * 56 + 40} l 0 ${(ele.y - from.y) * 56} l 84 0`
    })
    if (ele.children?.length) {
      flatLine(ele, flatNodeList)
    }
  })
}

const dragleave_handler = ev => {
  ev.preventDefault()
  notConfirm()
}

const dragover_handler = ev => {
  ev.preventDefault()

  dragOffsetX.value = ev.offsetX - props.offsetX
  dragOffsetY.value = ev.offsetY - props.offsetY

  const lg = state.nodeList.length
  const [fir] = state.nodeList

  if (!lg) return

  const [obj] = fir.children || []

  if (!fir.children?.length || obj?.isShadow) {
    if (obj?.isShadow) return
    state.visualNode = {
      tableName: '',
      isShadow: true,
      flag: '_&&'
    }

    state.nodeList[0].children = [state.visualNode]
    state.visualNodeParent = state.nodeList[0]
    return
  }

  let resultList = possibleNodeAreaList.value.map(ele => {
    const { fromX, fromY, toX, toY, isLeaf = false, tableName } = ele
    return [
      elementInteractArea(
        {
          left: dragOffsetX.value,
          right: dragOffsetX.value + 200,
          top: dragOffsetY.value,
          bottom: dragOffsetY.value + 32
        },
        {
          left: fromX,
          right: toX,
          top: fromY,
          bottom: toY
        }
      ),
      isLeaf || state.visualNode?.flag === tableName + '_&&r'
        ? elementInteractArea(
            {
              left: dragOffsetX.value,
              right: dragOffsetX.value + 200,
              top: dragOffsetY.value,
              bottom: dragOffsetY.value + 32
            },
            {
              left: fromX + 200,
              right: toX + 100,
              top: fromY,
              bottom: fromY + 32
            }
          )
        : 0
    ]
  })

  let maxIndex = 0

  resultList.reduce((pre, next, idx) => {
    const max = Math.max(...pre) > Math.max(...next)
    maxIndex = max ? maxIndex : idx
    return max ? pre : next
  })

  let maxArr = resultList[maxIndex]

  if (Array.isArray(state.visualNodeParent?.children)) {
    const shadowIndex = state.visualNodeParent.children.findIndex(ele => ele.isShadow)
    if (shadowIndex > -1) {
      state.visualNodeParent.children.splice(shadowIndex, 1)
    }
  }
  state.visualNode = null

  if (Math.max(...maxArr)) {
    const { tableName, isShadow = false, id } = possibleNodeAreaList.value[maxIndex]
    const [b, r] = maxArr
    if (!isShadow) {
      dfsNodeShadow(state.nodeList, { tableName, id }, b >= r ? 'b' : 'r', state.nodeList[0])
    }
  }
}

const dragenter_handler = ev => {
  ev.preventDefault()
}

const drop_handler = ev => {
  ev.preventDefault()
  let data = ev.dataTransfer.getData('text')
  const { tableName, type, datasourceId, name: noteName } = JSON.parse(data) as Table
  const extraData = {
    info: JSON.stringify({
      table: tableName,
      sql: ''
    }),
    noteName,
    unionType: 'left',
    unionFields: [],
    currentDsFields: [],
    sqlVariableDetails: null
  }

  if (!state.nodeList.length) {
    if (type === 'sql') {
      state.visualNode = {
        tableName,
        type,
        datasourceId,
        id: guid(),
        ...extraData
      }
      sqlNode.value = {
        sql: '',
        tableName,
        id: state.visualNode.id,
        datasourceId
      }
      editSqlField.value = true
      return
    }
    state.nodeList.push({
      tableName,
      type,
      isShadow: true,
      datasourceId,
      id: guid(),
      ...extraData
    })

    currentNode.value = state.nodeList[0]

    getTableField({
      datasourceId,
      id: currentNode.value.id,
      info: currentNode.value.info,
      tableName,
      type
    })
      .then(res => {
        nodeField.value = res as unknown as Field[]
        nodeField.value.forEach(ele => {
          ele.checked = true
        })
        state.nodeList[0].currentDsFields = cloneDeep(res)
      })
      .finally(() => {
        editUnion.value = true
      })
    nextTick(() => {
      emits('addComplete')
    })
    return
  }

  nextTick(() => {
    emits('addComplete')
  })

  if (!state.visualNode) return
  if (type === 'sql') {
    sqlNode.value = {
      sql: '',
      tableName,
      id: guid(),
      datasourceId
    }
    editSqlField.value = true
    return
  }

  nextTick(() => {
    Object.assign(state.visualNode, {
      tableName,
      type,
      datasourceId,
      id: guid(),
      ...extraData
    })
    emits('joinEditor', [
      {
        tableName,
        type,
        datasourceId,
        id: state.visualNode.id,
        ...extraData
      },
      state.visualNodeParent
    ])
  })
}

const setStateBack = (node, parent) => {
  delete parent.children
  delete node.children
  dfsNodeBack([parent, node], [parent.id, node.id], state.nodeList)
  if (state.visualNode) {
    confirm()
  }
}

const dialogRename = ref(false)
const renameForm = ref()
const defaultParam = {
  name: '',
  id: ''
}
const dfsNodeListRename = arr => {
  arr.some(ele => {
    if (ele.id === renameParam.id && ele.tableName !== renameParam.name) {
      ele.tableName = renameParam.name
      return true
    }

    if (!!ele.children?.length) {
      dfsNodeListRename(ele.children)
    }
  })
}
const renameParam = reactive(cloneDeep(defaultParam))
const confirmRename = () => {
  renameForm.value.validate(val => {
    if (val) {
      dfsNodeListRename(state.nodeList)
      renameParam.name = ''
      renameParam.id = ''
      dialogRename.value = false
    }
  })
}
const tableRename = ({ name, id }) => {
  renameParam.name = name
  renameParam.id = id
  dialogRename.value = true
}
const confirm = () => {
  state.visualNode.isShadow = false
  delete state.visualNode.flag
  state.visualNode = null
  state.visualNodeParent = null
}

const notConfirm = () => {
  if (!state.visualNodeParent) return
  state.visualNodeParent.children = state.visualNodeParent.children.filter(ele => !ele.isShadow)
  confirm()
}

const getNodeList = () => {
  return cloneDeep(unref(state.nodeList))
}

defineExpose({
  nodeNameList,
  getNodeList,
  setStateBack,
  notConfirm,
  dfsNodeFieldBack,
  initState,
  setChangeStatus,
  crossDatasources
})

const handleActiveNode = ele => {
  activeNodeId.value = ele.id
  handleCommand(ele, 'editerField')
}

const emits = defineEmits(['addComplete', 'joinEditor', 'updateAllfields', 'changeUpdate'])
</script>

<template>
  <div
    @drop="$event => drop_handler($event)"
    @dragenter="$event => dragenter_handler($event)"
    @dragover="$event => dragover_handler($event)"
    @dragleave="$event => dragleave_handler($event)"
    class="drag-mask_dataset"
    ref="dragMask"
    @click="handleClickOutside"
    :style="{ height: dragHeight + 'px' }"
  >
    <svg
      version="1.1"
      baseProfile="full"
      :width="svgWidth"
      :height="svgHeight"
      xmlns="http://www.w3.org/2000/svg"
    >
      <path
        :key="ele.d"
        class="path-point"
        v-for="ele in flatPathList"
        :d="ele.d"
        :stroke-dasharray="ele.isShadow ? '4,4' : '0'"
        :stroke="ele.isShadow ? primaryColor : '#BBBFC4'"
        stroke-width="1"
        fill="none"
      />
      <foreignObject
        :key="ele.tableName"
        v-for="ele in flatNodeList"
        :x="ele.x * 300 + 24"
        :y="ele.y * 56 + 24"
        width="200"
        height="32"
      >
        <div
          @click="handleActiveNode(ele)"
          class="node-union"
          ref="activeNode"
          :class="[
            {
              'shadow-node': ele.isShadow,
              'active-node': activeNodeId === ele.id
            }
          ]"
        >
          <el-icon>
            <Icon :name="ele.type !== 'sql' ? 'reference-table' : 'icon_sql_outlined'"></Icon>
          </el-icon>
          <span class="tableName">{{ ele.tableName }}</span>
          <span class="placeholder">拖拽表或自定义SQL至此处</span>
          <handle-more
            style="margin-left: auto"
            iconName="icon_more-vertical_outlined"
            :menuList="ele.type === 'sql' ? [...sqlMenu, ...menuList] : menuList"
            @handle-command="command => handleCommand(ele, command)"
          ></handle-more>
        </div>
      </foreignObject>

      <foreignObject
        :key="ele.d"
        v-for="ele in flatPathList"
        :x="ele.from.x * 300 + 272"
        :y="ele.to.y * 56 + 24"
        width="32"
        height="32"
      >
        <div
          v-if="!ele.isShadow"
          @click="handlePathClick(ele)"
          class="path-union"
          :style="{ borderColor: ele.sqlChangeFlag ? '#F54A45' : '' }"
        >
          <el-icon>
            <Icon :name="iconName[ele.to.unionType]"></Icon>
          </el-icon>
        </div>
      </foreignObject>
    </svg>
    <div
      class="mask-dataset"
      :class="[
        {
          'mask-dataset-none': !state.nodeList.length
        }
      ]"
      v-if="maskShow"
    ></div>
    <div class="zero" v-if="!state.nodeList.length">
      <img :src="zeroNodeImg" alt="" />
      <p>将左侧的数据表、自定义SQL</p>
      <p>拖拽到这里创建数据集</p>
    </div>
  </div>
  <el-dialog
    v-model="dialogRename"
    :close-on-press-escape="false"
    :close-on-click-modal="false"
    title="重命名表"
    width="420px"
  >
    <el-form
      ref="renameForm"
      require-asterisk-position="right"
      :model="renameParam"
      label-position="top"
    >
      <el-form-item
        prop="name"
        label="表名称"
        :rules="[
          {
            required: true,
            message: t('commons.cannot_be_null')
          }
        ]"
      >
        <el-input :placeholder="t('common.inputText')" v-model="renameParam.name"></el-input>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button secondary @click="dialogRename = false">
          {{ t('common.cancel') }}
        </el-button>
        <el-button type="primary" @click="confirmRename">
          {{ t('common.sure') }}
        </el-button>
      </span>
    </template>
  </el-dialog>
  <el-drawer
    :before-close="closeEditUnion"
    v-model="editUnion"
    custom-class="union-item-drawer"
    size="600px"
    direction="rtl"
  >
    <template #header v-if="currentNode">
      <div class="info-content">
        <div class="info">
          <span class="label">表名</span>
          <span :title="currentNode.tableName" class="name ellipsis">{{
            currentNode.tableName
          }}</span>
        </div>
        <div class="info">
          <span class="label">表备注</span>
          <span :title="currentNode.noteName" style="max-width: 240px" class="name ellipsis">{{
            currentNode.noteName || '-'
          }}</span>
        </div>
        <span
          :title="getDsName(currentNode.datasourceId)"
          style="max-width: 550px"
          class="ds ellipsis"
          >{{ t('auth.datasource') }}:{{ getDsName(currentNode.datasourceId) }}</span
        >
      </div>
    </template>
    <union-field-list
      :field-list="nodeField"
      :node="currentNode"
      v-if="nodeField.length"
      @checkedFields="changeNodeFields"
    />
    <template #footer>
      <el-button secondary @click="closeEditUnion">{{ t('dataset.cancel') }} </el-button>
      <el-button type="primary" @click="confirmEditUnion">{{ t('dataset.confirm') }} </el-button>
    </template>
  </el-drawer>
  <el-drawer
    direction="btt"
    :close-on-click-modal="false"
    size="calc(100% - 100px)"
    :with-header="false"
    :close-on-press-escape="false"
    modal-class="sql-drawer-fullscreen"
    v-model="editSqlField"
  >
    <add-sql @save="saveSqlNode" @close="closeSqlNode" :sqlNode="sqlNode"></add-sql>
  </el-drawer>
</template>

<style lang="less">
.path-point {
  cursor: pointer;
}

.sql-drawer-fullscreen {
  .ed-drawer.btt > .ed-drawer__body {
    padding: 0;
  }
}

.union-item-drawer {
  .ed-drawer__header {
    height: 82px;
    font-family: '阿里巴巴普惠体 3.0 55 Regular L3';

    .ed-drawer__close-btn {
      top: 26px;
    }

    .info-content {
      display: flex;
      flex-wrap: wrap;
    }

    .info {
      display: flex;
      flex-direction: column;
      width: 50%;
      .label {
        font-weight: 500;
        font-size: 16px;
        color: #1f2329;
        max-width: 500px;
      }
      .name {
        font-weight: 400;
        font-size: 14px;
      }
      .ds {
        font-weight: 400;
        font-size: 14px;
        max-width: 500px;
        color: #646a73;
      }
    }
  }
  .field-block-body {
    height: calc(100% - 70px) !important;
  }
}

.node-union {
  height: 100%;
  width: 100%;
  border: 1px solid #dee0e3;
  border-radius: 4px;
  font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
  font-size: 14px;
  font-weight: 400;
  color: #1f2329;
  padding-left: 9px;
  display: flex;
  align-items: center;
  background: #fff;
  position: relative;
  cursor: pointer;
  padding-right: 12px;

  &:hover {
    border-color: var(--ed-color-primary);
  }

  .placeholder {
    display: none;
  }

  & > .ed-icon {
    font-size: 13.3px;
    margin-right: 9.33px;
  }
  .tableName {
    max-width: 125px;
    text-overflow: ellipsis;
    overflow: hidden;
    white-space: nowrap;
  }

  &:not(.shadow-node)::before {
    content: '';
    position: absolute;
    width: 3px;
    height: 32px;
    left: -1px;
    top: -1px;
    background: var(--ed-color-primary);
    border-radius: 4px 0px 0px 4px;
  }
}

.path-union {
  width: 100%;
  height: 100%;
  border: 1px solid #dee0e3;
  border-radius: 50%;
  font-size: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  color: var(--ed-color-primary);
  cursor: pointer;
  &:hover {
    border-color: var(--ed-color-primary);
  }
}

.shadow-node {
  border: 1px dashed;
  border-color: var(--ed-color-primary);
  background-color: rgba(51, 112, 255, 0.08);
  .ed-icon,
  .tableName {
    display: none;
  }

  .placeholder {
    display: inline-block;
  }
}

.active-node {
  border-color: var(--ed-color-primary);
}

.drag-mask_dataset {
  background: #f5f6f7;
  overflow: auto;
  position: relative;
  width: 100%;
  border: none !important;
}

.mask-dataset {
  position: absolute;
  left: 16px;
  top: 16px;
  width: calc(100% - 32px);
  height: calc(100% - 32px);
  z-index: 5;
  user-select: none;
}

.mask-dataset-none {
  background-color: #e5ebf8;
  border: 1px dashed;
  border-color: var(--ed-color-primary);
}

.zero {
  position: absolute;
  left: 16px;
  top: 16px;
  width: calc(100% - 32px);
  height: calc(100% - 32px);
  z-index: 6;
  user-select: none;
  display: flex;
  align-items: center;
  flex-direction: column;
  padding-top: 42px;
  img {
    width: 125px;
    height: 125px;
    margin-bottom: 8px;
    -webkit-user-drag: none;
  }

  p {
    font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
    font-style: normal;
    font-weight: 400;
    font-size: 14px;
    line-height: 22px;
    text-align: center;
    color: #646a73;
  }
}
</style>

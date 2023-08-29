<script lang="ts" setup>
import { reactive, computed, ref, nextTick, onMounted } from 'vue'
// import { throttle } from 'lodash'
import AddSql from './AddSql.vue'
import { useI18n } from '@/hooks/web/useI18n'
import zeroNodeImg from '@/assets/img/drag.png'
import { guid } from './util'
import { HandleMore } from '@/components/handle-more'
import { propTypes } from '@/utils/propTypes'
import UnionFieldList from './UnionFieldList.vue'
import type { Node } from './UnionEdit.vue'
import { getTableField } from '@/api/dataset'
import type { Field } from './UnionFieldList.vue'
import type { SqlNode } from './AddSql.vue'
import { cloneDeep } from 'lodash-es'
import type { Table } from '@/api/dataset'
const state = reactive({
  nodeList: [],
  pathList: [],
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

const iconName = {
  left: 'icon_left-association',
  right: 'icon_right-association',
  inner: 'icon_intersect'
}
const { t } = useI18n()
const editSqlField = ref(false)

const nodeField = ref<Field[]>([])

const currentNode = ref<Node>()

const sqlNode = ref<SqlNode>()

const getNodeField = ({ datasourceId, id, info, tableName, type, currentDsFields }) => {
  return getTableField({ datasourceId, id, info, tableName, type })
    .then(res => {
      nodeField.value = res as unknown as Field[]
      nodeField.value.forEach(ele => {
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
  return list.forEach(ele => {
    arr.push(ele.tableName)
    if (ele.children?.length) {
      dfsNodeNameList(ele.children, arr)
    }
  })
}

const initState = nodeList => {
  Object.assign(state.nodeList, nodeList)
  nextTick(() => {
    emits('addComplete')
  })
}

const activeNode = ref('')

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

const saveSqlNode = (val: SqlNode, cb) => {
  const { tableName, id, sql, datasourceId, sqlVariableDetails = null } = val
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
        ;((res as unknown as Field[]) || []).forEach(ele => {
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

const closeSqlNode = () => {
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

const confirmEditUnion = () => {
  delUpdateDsFields(currentNode.value.id, state.nodeList)
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
    if (ele.x || ele.y) {
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

const dfsNode = (arr, nodeListLocation, x = 0, y = 0) => {
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

const dfsNodeShadow = (arr, tableName, position, parent) => {
  return arr.some((ele, index) => {
    if (ele.tableName === tableName) {
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
      return dfsNodeShadow(ele.children, tableName, position, ele)
    }
    return false
  })
}

const flatLine = (item, flatNodeList) => {
  const from = { ...item, d: '' }
  ;(item.children || []).forEach(ele => {
    flatNodeList.push({
      from,
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

  if (Math.max(...maxArr)) {
    const { tableName, isShadow = false } = possibleNodeAreaList.value[maxIndex]
    const [b, r] = maxArr

    if (!isShadow) {
      dfsNodeShadow(state.nodeList, tableName, b >= r ? 'b' : 'r', state.nodeList[0])
    }
  }
}

const dragenter_handler = ev => {
  // prevent Default event
  ev.preventDefault()
}

const drop_handler = ev => {
  ev.preventDefault()
  let data = ev.dataTransfer.getData('text')
  const { tableName, type, datasourceId } = JSON.parse(data) as Table
  const extraData = {
    info: JSON.stringify({
      table: tableName,
      sql: ''
    }),
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
    }).then(res => {
      ;((res as unknown as Field[]) || []).forEach(ele => {
        ele.checked = true
      })
      state.nodeList[0].currentDsFields = cloneDeep(res)
      confirmEditUnion()
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
    if (ele.id === renameParam.id) {
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

defineExpose({
  nodeNameList,
  nodeList: state.nodeList,
  setStateBack,
  notConfirm,
  dfsNodeFieldBack,
  initState
})

const emits = defineEmits(['addComplete', 'joinEditor', 'updateAllfields'])
</script>

<template>
  <div
    @drop="$event => drop_handler($event)"
    @dragenter="$event => dragenter_handler($event)"
    @dragover="$event => dragover_handler($event)"
    class="drag-mask"
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
        :stroke="ele.isShadow ? '#3370ff' : '#BBBFC4'"
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
          @click="activeNode = ele.tableName"
          class="node-union"
          :class="[
            {
              'shadow-node': ele.isShadow,
              'active-node': activeNode === ele.tableName
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
            v-if="activeNode === ele.tableName"
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
        <div v-if="!ele.isShadow" @click="handlePathClick(ele)" class="path-union">
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
  <el-drawer v-model="editUnion" custom-class="union-item-drawer" size="600px" direction="rtl">
    <template #header v-if="currentNode">
      <div class="info">
        <span class="name">{{ currentNode.tableName }}</span>
        <span class="ds">{{ getDsName(currentNode.datasourceId) }}</span>
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
    font-family: 'PingFang SC';

    .ed-drawer__close-btn {
      top: 26px;
    }

    .info {
      display: flex;
      flex-direction: column;
      .name {
        font-weight: 500;
        font-size: 16px;
        color: #1f2329;
      }
      .ds {
        font-weight: 400;
        font-size: 14px;
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
  font-family: PingFang SC;
  font-size: 14px;
  font-weight: 400;
  color: #1f2329;
  padding-left: 10.33px;
  display: flex;
  align-items: center;
  background: #fff;
  position: relative;
  cursor: pointer;
  padding-right: 8px;

  .placeholder {
    display: none;
  }

  .ed-icon {
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
    background: #3370ff;
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
  cursor: pointer;
}

.shadow-node {
  border: 1px dashed;
  border-color: #3370ff;
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
  border-color: #3370ff;
}

.drag-mask {
  background: #f5f6f7;
  overflow: auto;
  position: relative;
  width: 100%;
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
  border-color: #3370ff;
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
    font-family: 'PingFang SC';
    font-style: normal;
    font-weight: 400;
    font-size: 14px;
    line-height: 22px;
    text-align: center;
    color: #646a73;
  }
}
</style>

<script lang="ts" setup>
import { ref, reactive } from 'vue'
import UnionFieldList from './UnionFieldList.vue'
import UnionItemEdit from './UnionItemEdit.vue'
import type { Field } from './UnionFieldList.vue'
import { getTableField } from '@/api/dataset'
import { clone } from 'lodash-es'
interface UnionField {
  currentField: Field
  parentField: Field
}

type NodeType = 'db' | 'sql'
type UnionType = 'left' | 'right' | 'inner'

interface Node {
  tableName: string
  type: NodeType
  datasourceId: string
  id: string
  unionType: UnionType
  unionFields: UnionField[]
  info: string
  sqlVariableDetails: string
  currentDsFields: Field[]
}
const changeParentFields = val => {
  parent.currentDsFields = val
}
const changeNodeFields = val => {
  node.currentDsFields = val
}

const changeUnionFields = (index?: number) => {
  if (index !== undefined) {
    node.unionFields.splice(index, 1)
  } else {
    node.unionFields.push({
      parentField: null,
      currentField: null
    })
  }
}
const defaultNode = {
  info: '',
  tableName: '',
  type: 'db' as NodeType,
  datasourceId: '',
  id: '',
  unionType: 'left' as UnionType,
  unionFields: [],
  currentDsFields: [],
  sqlVariableDetails: null
}
const parentField = ref<Field[]>([])
const nodeField = ref<Field[]>([])
const node = reactive<Node>(clone(defaultNode))
const parent = reactive<Node>(clone(defaultNode))

const props = defineProps({
  editArr: {
    type: Array,
    default: () => []
  }
})

const clearState = () => {
  Object.assign(node, clone(defaultNode))
  Object.assign(parent, clone(defaultNode))
  parentField.value = []
  nodeField.value = []
}

const initState = () => {
  Object.assign(node, clone(props.editArr[0]))
  Object.assign(parent, clone(props.editArr[1]))
  getFields()
}
const getFields = async () => {
  const [n, p] = props.editArr as Node[]
  const [nr, pr] = await Promise.all([getTableField(n), getTableField(p)])
  parentField.value = pr as unknown as Field[]
  parentField.value.forEach(ele => {
    ele.checked = p.currentDsFields.map(ele => ele.originName).includes(ele.originName)
  })
  nodeField.value = nr as unknown as Field[]
  nodeField.value.forEach(ele => {
    ele.checked = n.currentDsFields.map(ele => ele.originName).includes(ele.originName)
  })
}

defineExpose({
  node,
  parent,
  clearState,
  initState
})
</script>

<template>
  <div style="height: calc(100% - 55px); overflow-y: auto">
    <div class="field-style">
      <div class="fields" v-loading="!parentField.length">
        <p :title="parent.tableName">
          {{ parent.tableName }}
        </p>
        <union-field-list
          :field-list="parentField"
          v-if="parentField.length"
          :node="parent"
          @checkedFields="changeParentFields"
        />
      </div>
      <div class="fields" v-loading="!nodeField.length">
        <p :title="node.tableName">
          {{ node.tableName }}
        </p>
        <union-field-list
          :field-list="nodeField"
          :node="node"
          v-if="nodeField.length"
          @checkedFields="changeNodeFields"
        />
      </div>
    </div>
    <union-item-edit
      :parent-field-list="parentField"
      :node-field-list="nodeField"
      :node="node"
      @change-union-type="val => (node.unionType = val)"
      v-if="node.tableName"
      @change-union-fields="changeUnionFields"
      :table-name="parent.tableName"
    />
  </div>
</template>

<style lang="less" scoped>
.field-style {
  height: 430px;
  border: 1px solid var(--deCardStrokeColor, #dee0e3);
  border-radius: 4px;
  width: 100%;
  box-sizing: border-box;
  display: flex;
  margin-bottom: 36px;
}
.fields {
  box-sizing: border-box;
  padding: 16px;
  width: 50%;

  & > p {
    font-family: PingFang SC;
    font-size: 14px;
    font-weight: 500;
    margin: 0;
    margin-bottom: 16px;
    text-overflow: ellipsis;
    overflow: hidden;
    white-space: nowrap;
    width: 100%;
    color: var(--deTextPrimary, #1f2329);
  }
  &:nth-child(1) {
    border-right: 1px solid var(--deCardStrokeColor, #dee0e3);
  }
}
</style>

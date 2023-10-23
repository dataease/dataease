<script lang="ts" setup>
import { ref } from 'vue'
import type Node from 'element-plus-secondary/es/components/tree/src/model/node'
import { propTypes } from '@/utils/propTypes'
interface Tree {
  deptId: number
  pid: number
  value?: number
  subCount: number
  name: string
  deptSort: number
  createBy?: string
  updateBy?: string
  createTime: number
  updateTime: number
  hasChildren: boolean
  leaf: boolean
  top: boolean
}
const tree = ref()
const currentSelect = ref()

defineProps({
  width: propTypes.string.def('200px')
})

const filterNodeMethod = (value, data) => data.name.includes(value)

const treeDefaultProps = {
  children: 'children',
  label: 'name',
  isLeaf: data => !data.hasChildren
}

const loadNode = (node: Node, resolve: (data: Tree[]) => void) => {
  if (node.level === 0) {
    return setTimeout(() => {
      const data: Tree[] = [
        {
          deptId: 2,
          pid: 0,
          subCount: 2,
          value: 2,
          name: 'wei的组织',
          deptSort: null,
          createBy: null,
          updateBy: null,
          createTime: 1667202467619,
          updateTime: 1667202467619,
          hasChildren: true,
          leaf: false,
          top: true
        },
        {
          deptId: 5,
          pid: 0,
          value: 5,
          subCount: 0,
          name: 'jinlong',
          deptSort: null,
          createBy: null,
          updateBy: null,
          createTime: 1669645057174,
          updateTime: 1669645057174,
          hasChildren: false,
          leaf: true,
          top: true
        },
        {
          deptId: 1,
          pid: 0,
          subCount: 0,
          name: '默认组织1121',
          value: 1,
          deptSort: 0,
          createBy: null,
          updateBy: null,
          createTime: 1622533297817,
          updateTime: 1679037885732,
          hasChildren: false,
          leaf: true,
          top: true
        }
      ]

      resolve(data)
    }, 500)
  }
  if (node.level > 1) return resolve([])

  setTimeout(() => {
    const data: Tree[] = [
      {
        deptId: 3,
        pid: 2,
        subCount: 1,
        value: 3,
        name: 'wei的二级组织',
        deptSort: null,
        createBy: null,
        updateBy: null,
        createTime: 1667202481457,
        updateTime: 1667202481457,
        hasChildren: true,
        leaf: false,
        top: false
      },
      {
        deptId: 4,
        pid: 2,
        subCount: 0,
        value: 4,
        name: 'yyp',
        deptSort: null,
        createBy: null,
        updateBy: null,
        createTime: 1667977447506,
        updateTime: 1667977447506,
        hasChildren: false,
        leaf: true,
        top: false
      }
    ]

    resolve(data)
  }, 500)
}
</script>

<template>
  <el-tree-select
    :load="loadNode"
    lazy
    v-model="currentSelect"
    filterable
    check-strictly
    :filter-node-method="filterNodeMethod"
    clearable
    ref="tree"
    :expand-on-click-node="false"
    check-on-click-node
    :props="treeDefaultProps"
  />
</template>

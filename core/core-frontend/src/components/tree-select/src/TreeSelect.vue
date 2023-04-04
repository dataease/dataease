<script lang="ts" setup>
import { ref } from 'vue'
import type Node from 'element-plus-secondary/es/components/tree/src/model/node'
import { propTypes } from '@/utils/propTypes'
interface Tree {
  deptId: number
  pid: number
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
const selectList = ref([])

defineProps({
  width: propTypes.string.def('200px')
})
const filterMethod = val => {
  tree.value.filter(val)
}
const filterNode = (value, data) => {
  if (!value) return true
  return data.name.indexOf(value) !== -1
}
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
  <el-popover placement="bottom" popper-class="tree-popper" :width="width" trigger="click">
    <template #reference>
      <el-select
        v-model="currentSelect"
        filterable
        :style="{ width: width }"
        :filter-method="filterMethod"
        clearable
        popper-class="tree-select"
      >
        <el-option
          v-for="item in selectList"
          :key="item.name"
          :label="item.name"
          :value="item.deptId"
        />
      </el-select>
    </template>
    <el-tree
      :load="loadNode"
      lazy
      ref="tree"
      :expand-on-click-node="false"
      check-on-click-node
      :filter-node-method="filterNode"
      :props="treeDefaultProps"
    ></el-tree>
  </el-popover>
</template>

<style lang="less">
.tree-popper {
  padding: 0;
  max-height: 300px;
  overflow: auto;
  .popper__arrow {
    display: none;
  }
}
.tree-select {
  display: none;
  .el-select-dropdown__empty,
  .popper__arrow {
    display: none;
  }
}
</style>

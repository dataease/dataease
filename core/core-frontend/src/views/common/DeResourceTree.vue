<script setup lang="ts">
import { ref } from 'vue'
const searchMap = {
  all: '全部',
  folder: '文件夹'
}

const searchPids = [] // 查询命中的pid
const filterText = ref(null)
const searchType = ref('all')
const expandedArray = ref([])
const resourceData = ref([
  {
    label: 'Level one 1',
    name: 'Level one 1',
    children: [
      {
        label: 'Level two 1-1',
        name: 'Level two 1-1',
        children: [
          {
            label: 'Level three 1-1-1'
          }
        ]
      }
    ]
  },
  {
    label: 'Level one 2',
    children: [
      {
        label: 'Level two 2-1',
        children: [
          {
            label: 'Level three 2-1-1'
          }
        ]
      },
      {
        label: 'Level two 2-2',
        children: [
          {
            label: 'Level three 2-2-1'
          }
        ]
      }
    ]
  }
])

const nodeExpand = data => {
  if (data.id) {
    expandedArray.value.push(data.id)
  }
}

const nodeCollapse = data => {
  if (data.id) {
    expandedArray.value.splice(expandedArray.value.indexOf(data.id), 1)
  }
}

const filterNode = (value, data) => {
  if (!value) return true
  const result = data.label.toLowerCase().indexOf(value.toLowerCase()) !== -1
  if (searchType.value === 'folder') {
    if (data.nodeType === 'folder' && result) {
      searchPids.push(data.id)
      return true
    }
    if (searchPids.indexOf(data.pid) !== -1) {
      if (data.nodeType === 'folder') {
        searchPids.push(data.id)
      }
      return true
    }
  } else {
    return result
  }
  return false
}

const nodeClick = (data, node) => {
  console.log('do something=' + JSON.stringify(data))
  //do something
}
</script>

<template>
  <div class="resource-tree">
    <el-input
      v-model="filterText"
      size="small"
      :placeholder="'搜索'"
      prefix-icon="el-icon-search"
      clearable
      class="search-bar"
    >
      <template #append>
        <el-select v-model="searchType" :placeholder="searchMap[searchType]">
          <el-option :label="searchMap['all']" value="all" />
          <el-option :label="searchMap['folder']" value="folder" />
        </el-select>
      </template>
    </el-input>
    <el-tree
      ref="panel_list_tree"
      :default-expanded-keys="expandedArray"
      :data="resourceData"
      node-key="id"
      :expand-on-click-node="true"
      :filter-node-method="filterNode"
      @node-expand="nodeExpand"
      @node-collapse="nodeCollapse"
      @node-click="nodeClick"
    >
      <template #default="{ data }">
        <span class="custom-tree-node-list">
          <span class="title-area-outer">
            <span class="title-area" :title="data.label">{{ data.label }}</span>
          </span>
        </span>
      </template>
    </el-tree>
  </div>
</template>
<style lang="less" scoped>
.resource-tree {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  .search-bar {
  }
}
.title-area {
  margin-left: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.title-area-outer {
  display: flex;
  flex: 1 1 0%;
  width: 0px;
}
.custom-tree-node-list {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding: 0 8px;
}
.father .child {
  /*display: none;*/
  visibility: hidden;
}

.father:hover .child {
  /*display: inline;*/
  visibility: visible;
}

:deep(.el-input__wrapper) {
  width: 80px;
}
</style>

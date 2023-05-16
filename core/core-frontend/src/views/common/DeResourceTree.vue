<script setup lang="ts">
import { EditPen } from '@element-plus/icons-vue'
import { onMounted, ref } from 'vue'
import { findById, findTree } from '@/api/dataVisualization'
const searchMap = {
  all: '全部',
  folder: '文件夹'
}

const searchPids = [] // 查询命中的pid
const filterText = ref(null)
const searchType = ref('all')
const expandedArray = ref([])
let resourceData = ref([])

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
  if (data.nodeType !== 'folder') {
    emit('nodeClick', data.id)
  }
}

const getTree = () => {
  // 从数据库中获取
  findTree().then(res => {
    resourceData.value = res.data
  })
}

const emit = defineEmits(['nodeClick'])

const clickMore = () => {
  //do something
}

const beforeClickEdit = (optType, data, node) => {
  return {
    data: data,
    node: node,
    optType: optType
  }
}

onMounted(() => {
  getTree()
})
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
        <span class="father">
          <span v-on:click.stop class="child">
            <el-button :icon="EditPen" type="primary" text></el-button>
          </span>
          <span style="margin-left: 12px" class="child" v-on:click.stop>
            <el-dropdown trigger="click" size="small" @command="clickMore">
              <span class="el-dropdown-link">
                <el-button icon="el-icon-more" type="text" size="small" />
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item>
                    {{ $t('panel.edit') }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
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

<template>
  <el-container class="geometry-container">
    <el-aside class="geonetry-aside">
      <div class="geo-title">
        <span>{{ t('online_map.geometry') }}</span>
        <span class="add-icon-span">
          <el-icon>
            <Icon name="icon_add_outlined"></Icon>
          </el-icon>
        </span>
      </div>
      <div class="geo-search">
        <el-input class="m16 w100" v-model="keyword" clearable :placeholder="t('commons.search')">
          <template #prefix>
            <el-icon>
              <Icon name="icon_search-outline_outlined"></Icon>
            </el-icon>
          </template>
        </el-input>
      </div>
      <div class="map-tree-container">
        <!-- <el-scrollbar> -->
        <el-tree :data="treeData" :props="defaultProps" @node-click="handleNodeClick">
          <template #default="{ node, data }">
            <span class="custom-tree-node">
              <span>{{ data.name || node.label }}</span>
            </span>
          </template>
        </el-tree>
        <!-- </el-scrollbar> -->
      </div>
    </el-aside>
    <el-main>地理信息内容区域</el-main>
  </el-container>
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { getWorldTree } from '@/api/map'
const { t } = useI18n()
const keyword = ref('')
const treeData = ref([])
interface Tree {
  label: string
  children?: Tree[]
}

const handleNodeClick = (data: Tree) => {
  console.log(data)
}

const loadTreeData = () => {
  getWorldTree()
    .then(res => {
      const root = res.data
      treeData.value = [root]
    })
    .catch(e => {
      console.error(e)
    })
}

const defaultProps = {
  children: 'children',
  label: 'name'
}

loadTreeData()
</script>

<style lang="less" scoped>
.geometry-container {
  height: 100%;
  .geonetry-aside {
    width: 280px !important;
    border-right: 1px solid #1f232926;
    padding: 16px;
    height: 100%;
    .geo-title {
      display: flex;
      justify-content: space-between;
      height: 24px;
      line-height: 24px;
      span:first-child {
        font-size: 16px;
        font-weight: 500;
        line-height: 24px;
      }
      .add-icon-span {
        color: #3370ff;
        height: 20px;
        width: 20px;
        i {
          left: 2px;
        }
        &:hover {
          background: #1f232926;
          cursor: pointer;
        }
        border-radius: 2px;
      }
      margin-bottom: 16px;
    }
    .geo-search {
      margin-bottom: 16px;
    }
    .map-tree-container {
      height: calc(100% - 96px);
      overflow-y: auto;
    }
  }
}
</style>

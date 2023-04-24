<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { ElIcon } from 'element-plus-secondary'
import { Icon } from '@/components/icon-custom'
import type { DsType } from './DsTypeList.vue'
import { useI18n } from '@/hooks/web/useI18n'
import DsTypeList from './DsTypeList.vue'
import EditorDetail from './EditorDetail.vue'
interface Node {
  name: string
  id: string
  type: DsType
}
const { t } = useI18n()

const dsType = ['OLTP', 'OLAP', 'dataWarehouseLake', 'OTHER']

const nameMap = {
  OLTP: 'OLTP',
  OLAP: 'OLAP',
  dataWarehouseLake: t('datasource.data_warehouse_lake'),
  OTHER: t('datasource.other'),
  LOCAL: '本地文件',
  API: 'API数据'
}

const state = reactive({
  addeddatasourceList: [],
  datasourceTree: [],
  datasourceTypeList: [],
  dsTableData: [],
  paginationConfig: {
    currentPage: 1,
    pageSize: 10,
    total: 0
  },
  filterTable: []
})
state.datasourceTree = dsType.map(ele => {
  return {
    name: nameMap[ele],
    type: ele
  }
})

const activeStep = ref(1)
const detail = ref()
const currentType = ref<DsType>('OLTP')
const nickName = ref('')
const currentDsType = ref('')
const selectDsType = (type: string) => {
  currentDsType.value = type
  detail.value.initForm(type)
}
const handleNodeClick = (data: Node) => {
  currentType.value = data.type
}
</script>

<template>
  <div class="datasource">
    <div class="ds-type-select">
      <div class="title">
        新建数据源
        <el-input class="m24 w100" v-model="nickName" clearable>
          <template #prefix>
            <el-icon>
              <Icon name="icon_search-outline_outlined"></Icon>
            </el-icon>
          </template>
        </el-input>
      </div>
      <div
        :key="ele.name"
        @click="handleNodeClick(ele)"
        v-for="ele in state.datasourceTree"
        class="list-item_primary"
      >
        <span :title="ele.name" class="label">{{ ele.name }}</span>
      </div>
    </div>
    <div class="ds-editor">
      <div class="editor-step flex-center">
        <el-steps :active="activeStep" align-center :space="144">
          <el-step title="选择数据源类型" />
          <el-step title="录入数据源信息" />
        </el-steps>
      </div>
      <div class="editor-content">
        <ds-type-list
          v-show="!currentDsType"
          @select-ds-type="selectDsType"
          :current-type="currentType"
        ></ds-type-list>
        <editor-detail ref="detail" v-show="currentDsType"></editor-detail>
      </div>
      <div class="editor-footer">
        <el-button secondary> 取消 </el-button>
        <el-button type="primary"> 上一步 </el-button>
        <el-button type="primary"> 下一步 </el-button>
        <el-button type="primary"> 确定 </el-button>
      </div>
    </div>
  </div>
</template>

<style lang="less" scoped>
.datasource {
  display: flex;
  width: 100%;
  height: 100%;
  background: #fff;
  .ds-type-select {
    width: 269px;
    padding: 24px;
    border-right: 1px solid #ccc;
    .title {
      display: flex;
      justify-content: space-between;
      font-family: PingFang SC;
      font-size: 20px;
      font-weight: 500;
      color: var(--TextPrimary, #1f2329);
      box-sizing: border-box;
      flex-wrap: wrap;
      position: sticky;
      top: 0;
      left: 24px;
      z-index: 5;
      background: white;
      &::before {
        content: '';
        width: 100%;
        height: 24px;
        top: -24px;
        position: absolute;
        z-index: 5;
        left: 0;
        background: white;
      }
    }

    .m24 {
      margin: 24px 0;
    }
    .w100 {
      width: 100%;
    }
  }
  .ds-editor {
    flex: 1;
    .editor-step {
      height: 100px;
      border-bottom: 1px solid #cccc;
      padding: 24px;
    }

    .editor-content {
      padding: 24px;
      height: calc(100vh - 180px);
      overflow-y: auto;
    }

    .editor-footer {
      height: 80px;
      padding-right: 24px;
      text-align: right;
    }
  }
}
</style>
<style lang="less">
.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-right: 8px;
  box-sizing: content-box;

  .label-tooltip {
    width: 60%;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }
}
</style>

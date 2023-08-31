<script lang="ts" setup>
import { useI18n } from '@/hooks/web/useI18n'
import { ref, reactive, onMounted } from 'vue'
import type { TabsPaneContext } from 'element-plus-secondary'
import GridTable from '@/components/grid-table/src/GridTable.vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { shortcutOption } from './ShortcutOption'
import { XpackComponent } from '@/components/plugin'
const { resolve } = useRouter()
const { t } = useI18n()

const panelKeyword = ref()
const activeName = ref('recent')
const userAddPopper = ref(false)
const activeCommand = ref('all_types')
const state = reactive({
  tableData: [],
  curTypeList: [],
  tableColumn: []
})

const handleVisibleChange = (val: boolean) => {
  userAddPopper.value = val
}

const handleCommand = (command: string) => {
  activeCommand.value = command
  loadTableData()
}
const handleClick = (ele: TabsPaneContext) => {
  if (ele.paneName === 'recent' || ele.paneName === 'store') {
    loading.value = true
    shortcutOption.setBusiFlag(ele.paneName)
    state.curTypeList = shortcutOption.getBusiList()
    state.tableColumn = shortcutOption.getColmunList()
    loadTableData()
  }
}
const triggerFilterPanel = () => {
  loadTableData()
}
const preview = id => {
  const routeUrl = resolve({
    path: '/preview',
    query: { dvId: id }
  })
  window.open(routeUrl.href, '_blank')
}
const formatterTime = (_, _column, cellValue) => {
  return dayjs(new Date(cellValue)).format('YYYY-MM-DD HH:mm:ss')
}

const loadTableData = () => {
  loading.value = true
  const queryType = activeCommand.value === 'all_types' ? '' : activeCommand.value
  shortcutOption
    .loadData({ type: queryType, keyword: panelKeyword.value, asc: !orderDesc.value })
    .then(res => {
      state.tableData = res.data
    })
    .finally(() => {
      loading.value = false
    })
}

const panelLoad = paneInfo => {
  tablePaneList.value.push({
    title: paneInfo.title,
    name: paneInfo.name
  })
}

const tablePaneList = ref([
  { title: '最近使用', name: 'recent' },
  { title: '我的收藏', name: 'store' }
])
onMounted(() => {
  handleClick({
    paneName: 'recent',
    uid: 0,
    slots: undefined,
    props: undefined,
    active: false,
    index: '',
    isClosable: false
  })
})
const orderDesc = ref(true)
const loading = ref(false)
const sortChange = param => {
  orderDesc.value = true
  const type = param.order.substring(0, param.order.indexOf('ending'))
  orderDesc.value = type === 'desc'
  loadTableData()
}
const setLoading = (val: boolean) => {
  loading.value = val
}
</script>

<template>
  <div class="dashboard-type" v-loading="loading">
    <el-tabs v-model="activeName" class="dashboard-type-tabs" @tab-click="handleClick">
      <el-tab-pane
        v-for="item in tablePaneList"
        :key="item.name"
        :label="item.title"
        :name="item.name"
      />
    </el-tabs>
    <XpackComponent jsname="c2hhcmUtcGFuZWw=" @loaded="panelLoad" />
    <XpackComponent :active-name="activeName" jsname="c2hhcmU=" @set-loading="setLoading" />
    <el-row v-if="activeName === 'recent' || activeName === 'store'">
      <el-col :span="12">
        <el-dropdown
          placement="bottom-start"
          @visible-change="handleVisibleChange"
          popper-class="menu-panel-select_popper"
          @command="handleCommand"
          trigger="click"
        >
          <el-button secondary>
            {{ t(`auth.${activeCommand}`) }}
            <el-icon style="margin-left: 4px">
              <arrow-up v-if="userAddPopper" />
              <arrow-down v-else />
            </el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item
                :class="activeCommand === ele && 'active'"
                v-for="ele in state.curTypeList"
                :command="ele"
                :key="ele"
              >
                {{ t(`auth.${ele}`) }}
                <el-icon v-if="activeCommand === ele">
                  <Icon name="icon_done_outlined"></Icon>
                </el-icon>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-col>
      <el-col class="search" :span="12">
        <el-input
          v-model="panelKeyword"
          clearable
          @change="triggerFilterPanel"
          placeholder="搜索关键词"
        >
          <template #prefix>
            <el-icon>
              <Icon name="icon_search-outline_outlined"></Icon>
            </el-icon>
          </template>
        </el-input>
      </el-col>
    </el-row>
    <div v-if="activeName === 'recent' || activeName === 'store'" class="panel-table">
      <GridTable :show-pagination="false" :table-data="state.tableData" @sort-change="sortChange">
        <el-table-column key="name" width="280" prop="name" :label="t('common.name')">
          <template v-slot:default="scope">
            <div class="name-content">
              <el-icon class="main-color"> <Icon name="icon_dashboard_outlined" /> </el-icon>
              <el-tooltip placement="top">
                <template #content>{{ scope.row.name }}</template>
                <span class="ellipsis" style="max-width: 250px">{{ scope.row.name }}</span>
              </el-tooltip>
              <el-icon v-if="activeName === 'store'" class="name-star">
                <Icon name="visual-star" />
              </el-icon>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          v-for="item in state.tableColumn"
          :key="item.label"
          prop="name"
          show-overflow-tooltip
          :sortable="item.type === 'time'"
          :label="item.label"
        >
          <template #default="scope">
            <span v-if="item.type && item.type === 'time'">{{
              formatterTime(null, null, scope.row[item.field])
            }}</span>
            <span v-else>{{ scope.row[item.field] }}</span>
          </template>
        </el-table-column>

        <el-table-column width="96" fixed="right" key="_operation" :label="$t('common.operate')">
          <template #default="scope">
            <template v-if="['dashboard', 'dataV'].includes(scope.row.type)">
              <el-tooltip effect="dark" content="新页面预览" placement="top">
                <el-icon
                  class="hover-icon hover-icon-in-table"
                  @click="preview(activeName === 'recent' ? scope.row.id : scope.row.resourceId)"
                >
                  <Icon name="icon_pc_outlined"></Icon>
                </el-icon>
              </el-tooltip>

              <XpackComponent
                :in-grid="true"
                jsname="c2hhcmUtaGFuZGxlcg=="
                :resource-id="activeName === 'recent' ? scope.row.id : scope.row.resourceId"
              />
            </template>
          </template>
        </el-table-column>
      </GridTable>
    </div>
  </div>
</template>

<style lang="less" scoped>
.dashboard-type {
  padding: 8px 24px 0 24px;
  background: #fff;
  border-radius: 4px;
  height: calc(100% - 280px);
  margin-top: 16px;

  .dashboard-type-tabs {
    margin-bottom: 16px;
  }

  .search {
    text-align: right;
    .ed-input {
      width: 240px;
    }
  }

  .panel-table {
    margin-top: 16px;
    height: calc(100% - 110px);

    .name-content {
      display: flex;
      align-items: center;
    }
    .main-color {
      font-size: 21.33px;
      padding: 5.33px;
      margin-right: 12px;
      border-radius: 4px;
      color: #fff;
      background: #3370ff;
    }
    .name-star {
      font-size: 15px;
      padding-left: 5px;
    }
  }
}
</style>

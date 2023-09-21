<script lang="ts" setup>
import { useI18n } from '@/hooks/web/useI18n'
import { ref, reactive, onMounted, computed } from 'vue'
import type { TabsPaneContext } from 'element-plus-secondary'
import GridTable from '@/components/grid-table/src/GridTable.vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { shortcutOption } from './ShortcutOption'
import { XpackComponent } from '@/components/plugin'
import { interactiveStoreWithOut } from '@/store/modules/interactive'
const { resolve } = useRouter()
const { t } = useI18n()
const interactiveStore = interactiveStoreWithOut()

defineProps({
  expand: {
    type: Boolean,
    default: false
  }
})
const panelKeyword = ref()
const activeName = ref('recent')
const userAddPopper = ref(false)
const activeCommand = ref('all_types')
const state = reactive({
  tableData: [],
  curTypeList: [],
  tableColumn: []
})
const busiDataMap = computed(() => interactiveStore.getData)

const handleClick = (ele: TabsPaneContext) => {
  if (ele.paneName === 'recent' || ele.paneName === 'store') {
    loading.value = true
    shortcutOption.setBusiFlag(ele.paneName)
    state.curTypeList = shortcutOption
      .getBusiList()
      .filter(busi => busi === 'all_types' || busiAuthList.includes(busi))
    state.tableColumn = shortcutOption.getColmunList()
    loadTableData()
  }
}
const getBusiListWithPermission = () => {
  const baseFlagList = ['panel', 'screen', 'dataset', 'datasource']
  const busiFlagList = []
  for (const key in busiDataMap.value) {
    if (busiDataMap.value[key].menuAuth) {
      busiFlagList.push(baseFlagList[parseInt(key)])
    }
  }
  tablePaneList.value[0].disabled = !busiFlagList?.length
  tablePaneList.value[1].disabled =
    !busiFlagList.includes('panel') && !busiFlagList.includes('screen')
  return busiFlagList
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
    name: paneInfo.name,
    disabled: tablePaneList.value[1].disabled
  })
}

const tablePaneList = ref([
  { title: '最近使用', name: 'recent', disabled: false },
  { title: '我的收藏', name: 'store', disabled: false }
])

const busiAuthList = getBusiListWithPermission()
onMounted(() => {
  !!busiAuthList.length &&
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
  <div
    class="dashboard-type"
    :class="expand && 'expand'"
    v-if="busiAuthList.length"
    v-loading="loading"
  >
    <el-tabs v-model="activeName" class="dashboard-type-tabs" @tab-click="handleClick">
      <el-tab-pane
        v-for="item in tablePaneList"
        :key="item.name"
        :disabled="item.disabled"
        :label="item.title"
        :name="item.name"
      >
        <template #label>
          <span class="custom-tabs-label">
            <el-tooltip placement="top" v-if="item.disabled" content="没有权限">
              <span>{{ item.title }}</span>
            </el-tooltip>
            <span v-else>{{ item.title }}</span>
          </span>
        </template>
      </el-tab-pane>
    </el-tabs>
    <XpackComponent jsname="c2hhcmUtcGFuZWw=" @loaded="panelLoad" />
    <XpackComponent :active-name="activeName" jsname="c2hhcmU=" @set-loading="setLoading" />
    <el-row v-if="activeName === 'recent' || activeName === 'store'">
      <el-col :span="12">
        <el-select
          popper-class="menu-panel-select_popper"
          class="select-type-list"
          v-model="activeCommand"
          @change="loadTableData"
        >
          <el-option
            v-for="item in state.curTypeList"
            :key="item"
            :label="t(`auth.${item}`)"
            :value="item"
          />
        </el-select>
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
                :weight="scope.row.weight"
                :resource-id="activeName === 'recent' ? scope.row.id : scope.row.resourceId"
              />
            </template>
          </template>
        </el-table-column>
      </GridTable>
    </div>
  </div>
  <el-empty class="dashboard-type" v-else description="没有任何业务菜单权限，请联系管理员授权" />
</template>

<style lang="less" scoped>
.dashboard-type {
  padding: 8px 24px 0 24px;
  background: #fff;
  border-radius: 4px;
  height: calc(100% - 280px);
  margin-top: 16px;

  .select-type-list {
    width: 104px;
  }

  &.expand {
    height: calc(100% - 89px);
  }

  .type-button {
    background-color: #fff;

    &:hover,
    &:active,
    &:focus {
      border-color: #3370ff;
      background-color: #fff;
    }
  }

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
<style lang="less">
.menu-panel-select_popper {
  .ed-select-dropdown__item {
    height: 32px;
    line-height: 32px;
    padding: 0 11px;
    position: relative;
    &.selected::after {
      position: absolute;
      content: '';
      width: 7px;
      height: 4px;
      top: 13px;
      right: 11px;
      border: 1px solid #3370ff;
      border-right: transparent;
      border-top: transparent;
      transform: rotate(314deg);
    }
  }
}
</style>

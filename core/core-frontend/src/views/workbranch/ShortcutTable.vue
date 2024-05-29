<script lang="ts" setup>
import { useI18n } from '@/hooks/web/useI18n'
import { ref, reactive, onMounted, computed } from 'vue'
import type { TabsPaneContext } from 'element-plus-secondary'
import GridTable from '@/components/grid-table/src/GridTable.vue'
import { useRoute, useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { shortcutOption } from './ShortcutOption'
/* import { XpackComponent } from '@/components/plugin' */
import { interactiveStoreWithOut } from '@/store/modules/interactive'
import { storeApi } from '@/api/visualization/dataVisualization'
import { useCache } from '@/hooks/web/useCache'
import { useUserStoreWithOut } from '@/store/modules/user'
import ShareGrid from '@/views/share/share/ShareGrid.vue'
import ShareHandler from '@/views/share/share/ShareHandler.vue'
import { useAppStoreWithOut } from '@/store/modules/app'
import { useEmbedded } from '@/store/modules/embedded'
const userStore = useUserStoreWithOut()
const { resolve } = useRouter()
const { t } = useI18n()
const interactiveStore = interactiveStoreWithOut()
const { wsCache } = useCache()
const appStore = useAppStoreWithOut()
const embeddedStore = useEmbedded()
const route = useRoute()
const { push } = useRouter()
defineProps({
  expand: {
    type: Boolean,
    default: false
  }
})
const desktop = wsCache.get('app.desktop')
const panelKeyword = ref()
const activeName = ref('recent')
const activeCommand = ref('all_types')
const state = reactive({
  tableData: [],
  curTypeList: [],
  tableColumn: []
})
const busiDataMap = computed(() => interactiveStore.getData)
const iconMap = {
  panel: 'icon_dashboard_outlined',
  panelMobile: 'dv-dashboard-spine-mobile',
  dashboard: 'icon_dashboard_outlined',
  dashboardMobile: 'dv-dashboard-spine-mobile',
  screen: 'icon_operation-analysis_outlined',
  dataV: 'icon_operation-analysis_outlined',
  dataset: 'icon_app_outlined',
  datasource: 'icon_database_outlined'
}

const jumpActiveCheck = row => {
  return row && ['dashboard', 'panel', 'dataV', 'screen'].includes(row.type)
}

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

const openDataset = id => {
  const routeUrl = resolve({
    path: '/dataset-form',
    query: { id: id }
  })
  window.open(routeUrl.href, '_blank')
}
const formatterTime = (_, _column, cellValue) => {
  return dayjs(new Date(cellValue)).format('YYYY-MM-DD HH:mm:ss')
}

const typeMap = {
  screen: '数据大屏',
  dataV: '数据大屏',
  dashboard: '仪表板',
  panel: '仪表板',
  dataset: '数据集',
  datasource: '数据源'
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
      imgType.value = getEmptyImg()
      emptyDesc.value = getEmptyDesc()
      loading.value = false
    })
}

/* const panelLoad = paneInfo => {
  tablePaneList.value.push({
    title: paneInfo.title,
    name: paneInfo.name,
    disabled: tablePaneList.value[1].disabled
  })
} */

const tablePaneList = ref([
  { title: '最近使用', name: 'recent', disabled: false },
  { title: '我的收藏', name: 'store', disabled: false },
  { title: t('visualization.share_out'), name: 'share', disabled: false }
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

const handleCellClick = row => {
  if (row) {
    const sourceId = activeName.value === 'recent' ? row.id : row.resourceId
    if (['dashboard', 'panel'].includes(row.type)) {
      window.open('#/panel/index?dvId=' + sourceId, '_self')
    } else if (['dataV', 'screen'].includes(row.type)) {
      window.open('#/screen/index?dvId=' + sourceId, '_self')
    } else if (['dataset'].includes(row.type)) {
      const routeName =
        embeddedStore.getToken && appStore.getIsIframe ? 'dataset-embedded' : 'dataset'
      push({
        name: routeName,
        params: {
          id: sourceId
        }
      })
    } else if (['datasource'].includes(row.type)) {
      push({
        name: 'datasource',
        params: {
          id: sourceId
        }
      })
    }
  }
}

const setLoading = (val: boolean) => {
  loading.value = val
}

const executeStore = rowInfo => {
  const param = {
    id: rowInfo.id,
    type: rowInfo.type
  }
  storeApi(param).then(() => {
    rowInfo.favorite = !rowInfo.favorite
  })
}

const executeCancelStore = rowInfo => {
  const param = {
    id: rowInfo.resourceId,
    type: rowInfo.type === 'dataV' ? 'screen' : 'panel'
  }
  storeApi(param).then(() => {
    loadTableData()
  })
}

const imgType = ref()
const emptyDesc = ref('')
const getEmptyImg = (): string => {
  if (panelKeyword.value) {
    return 'tree'
  }
  return 'noneWhite'
}

const getEmptyDesc = (): string => {
  if (panelKeyword.value) {
    return '没有找到相关内容'
  }
  if (activeName.value === 'recent') {
    return '暂无内容'
  }
  if (activeName.value === 'store') {
    return '暂无收藏'
  }
  return ''
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
    <!-- <XpackComponent jsname="c2hhcmUtcGFuZWw=" @loaded="panelLoad" /> -->
    <!-- <XpackComponent :active-name="activeName" jsname="c2hhcmU=" @set-loading="setLoading" /> -->
    <share-grid :active-name="activeName" @set-loading="setLoading" />
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
      <GridTable
        :show-pagination="false"
        :table-data="state.tableData"
        @sort-change="sortChange"
        @cell-click="handleCellClick"
        :empty-desc="emptyDesc"
        :empty-img="imgType"
        class="workbranch-grid"
      >
        <el-table-column key="name" width="280" prop="name" :label="t('common.name')">
          <template v-slot:default="scope">
            <div class="name-content" :class="{ 'jump-active': jumpActiveCheck(scope.row) }">
              <el-icon v-if="scope.row.extFlag" style="margin-right: 12px; font-size: 18px">
                <Icon :name="iconMap[scope.row.type + 'Mobile']" />
              </el-icon>
              <el-icon v-else :class="`main-color color-${scope.row.type}`">
                <Icon :name="iconMap[scope.row.type]" />
              </el-icon>
              <el-tooltip placement="top">
                <template #content>{{ scope.row.name }}</template>
                <span class="ellipsis" style="max-width: 250px">{{ scope.row.name }}</span>
              </el-tooltip>
              <el-icon
                v-if="activeName === 'recent' && ['screen', 'panel'].includes(scope.row.type)"
                class="custom-icon"
                @click.stop="executeStore(scope.row)"
                :style="{ color: scope.row.favorite ? '#FFC60A' : '#646A73' }"
              >
                <icon
                  :name="scope.row.favorite ? 'visual-star' : 'icon_collection_outlined'"
                ></icon>
              </el-icon>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          v-for="item in state.tableColumn"
          :key="item.label"
          :prop="item.field"
          show-overflow-tooltip
          :sortable="item.type === 'time'"
          :label="item.label"
        >
          <template #default="scope">
            <span :class="{ 'jump-active': jumpActiveCheck(scope.row) }">
              <span v-if="item.type && item.type === 'time'">{{
                formatterTime(null, null, scope.row[item.field])
              }}</span>
              <span v-else-if="item.field && item.field === 'type'">{{
                typeMap[scope.row[item.field]]
              }}</span>
              <span v-else-if="desktop && item.field && item.field.endsWith('tor')">{{
                userStore.getName
              }}</span>
              <span v-else>{{ scope.row[item.field] }}</span>
            </span>
          </template>
        </el-table-column>

        <el-table-column width="100" fixed="right" key="_operation" :label="$t('common.operate')">
          <template #default="scope">
            <template v-if="['dashboard', 'dataV', 'panel', 'screen'].includes(scope.row.type)">
              <el-tooltip effect="dark" content="新页面预览" placement="top">
                <el-icon
                  class="hover-icon hover-icon-in-table"
                  @click.stop="
                    preview(activeName === 'recent' ? scope.row.id : scope.row.resourceId)
                  "
                >
                  <Icon name="icon_pc_outlined"></Icon>
                </el-icon>
              </el-tooltip>
              <ShareHandler
                :in-grid="true"
                :weight="scope.row.weight"
                :resource-id="activeName === 'recent' ? scope.row.id : scope.row.resourceId"
                :resource-type="scope.row.type"
              />
              <!-- <XpackComponent
                :in-grid="true"
                jsname="c2hhcmUtaGFuZGxlcg=="
                :weight="scope.row.weight"
                :resource-id="activeName === 'recent' ? scope.row.id : scope.row.resourceId"
                :resource-type="scope.row.type"
              /> -->
              <el-tooltip
                v-if="activeName === 'store'"
                effect="dark"
                content="取消收藏"
                placement="top"
              >
                <el-icon
                  class="hover-icon hover-icon-in-table"
                  @click.stop="executeCancelStore(scope.row)"
                >
                  <Icon name="icon_cancel_store"></Icon>
                </el-icon>
              </el-tooltip>
            </template>

            <template v-if="['dataset'].includes(scope.row.type)">
              <el-tooltip effect="dark" content="打开数据集" placement="top">
                <el-icon
                  class="hover-icon hover-icon-in-table"
                  @click.stop="
                    openDataset(activeName === 'recent' ? scope.row.id : scope.row.resourceId)
                  "
                >
                  <Icon name="icon_pc_outlined"></Icon>
                </el-icon>
              </el-tooltip>
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
    :deep(.ed-input__wrapper) {
      padding-left: 11px;
      padding-right: 11px;
    }
  }

  &.expand {
    height: calc(100% - 89px);
  }

  .type-button {
    background-color: #fff;

    &:hover,
    &:active,
    &:focus {
      border-color: var(--ed-color-primary);
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

    :deep(.ed-table__row):hover {
      cursor: pointer;
    }

    .name-content {
      display: flex;
      align-items: center;
      .custom-icon {
        display: none;
      }
      &:hover .custom-icon {
        cursor: pointer;
        margin-left: 8px;
        display: inherit !important;
      }
    }
    .main-color {
      font-size: 18px;
      padding: 3px;
      margin-right: 12px;
      border-radius: 4px;
      color: #fff;
    }
    .name-star {
      font-size: 15px;
      padding-left: 5px;
    }
  }
}
.workbranch-grid :deep(.ed-empty) {
  padding: 80px 0 !important;
  .ed-empty__description {
    margin-top: 0px;
    line-height: 20px !important;
  }
}

.jump-active {
  cursor: pointer;
}
</style>
<style lang="less">
.menu-panel-select_popper {
  width: 140px;
  background: #fff;
}
</style>

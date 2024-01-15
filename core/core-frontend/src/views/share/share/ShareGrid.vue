<script lang="ts" setup>
import { useI18n } from '@/hooks/web/useI18n'
import { ref, reactive, watch, computed } from 'vue'
import GridTable from '@/components/grid-table/src/GridTable.vue'
import request from '@/config/axios'
import dayjs from 'dayjs'
import { propTypes } from '@/utils/propTypes'
import ShareHandler from './ShareHandler.vue'
import { interactiveStoreWithOut } from '@/store/modules/interactive'
const props = defineProps({
  activeName: propTypes.string.def('')
})

const { t } = useI18n()
const interactiveStore = interactiveStoreWithOut()

const busiDataMap = computed(() => interactiveStore.getData)
const panelKeyword = ref()
const userAddPopper = ref(false)
const activeCommand = ref('all_types')
const state = reactive({
  tableData: [],
  curTypeList: ['all_types', 'panel', 'screen'],
  tableColumn: [
    { field: 'creator', label: '分享人' },
    { field: 'time', label: '分享时间', type: 'time' },
    { field: 'exp', label: '有效期', type: 'time' }
  ]
})

const handleVisibleChange = (val: boolean) => {
  userAddPopper.value = val
}

const handleCommand = (command: string) => {
  activeCommand.value = command
  loadTableData()
}

const triggerFilterPanel = () => {
  loadTableData()
}
const preview = id => {
  const routeUrl = `/#/preview?dvId=${id}`
  window.open(routeUrl, '_blank')
}
const formatterTime = (_, _column, cellValue) => {
  if (!cellValue) {
    return '-'
  }
  return dayjs(new Date(cellValue)).format('YYYY-MM-DD HH:mm:ss')
}

const showLoading = () => {
  emits('setLoading', true)
}
const closeLoading = () => {
  emits('setLoading', false)
}
const emits = defineEmits(['setLoading'])
const loadTableData = () => {
  showLoading()
  const queryType = activeCommand.value === 'all_types' ? '' : activeCommand.value
  request
    .post({
      url: '/share/query',
      data: { type: queryType, keyword: panelKeyword.value, asc: !orderDesc.value }
    })
    .then(res => {
      state.tableData = res.data
    })
    .finally(() => {
      imgType.value = getEmptyImg()
      emptyDesc.value = getEmptyDesc()
      closeLoading()
    })
}
const orderDesc = ref(true)
const sortChange = param => {
  orderDesc.value = true
  const type = param.order.substring(0, param.order.indexOf('ending'))
  orderDesc.value = type === 'desc'
  loadTableData()
}
const getBusiListWithPermission = () => {
  const baseFlagList: string[] = ['panel', 'screen']
  const busiFlagList: string[] = []
  for (const key in busiDataMap.value) {
    if (busiDataMap.value[key].menuAuth) {
      busiFlagList.push(baseFlagList[parseInt(key)])
    }
  }
  return busiFlagList
}
const busiAuthList: string[] = getBusiListWithPermission()

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

  return ''
}
watch(
  () => props.activeName,
  () => {
    if (props.activeName === 'share') {
      loadTableData()
    }
  },
  {
    immediate: true
  }
)
</script>

<template>
  <el-row v-if="props.activeName === 'share'">
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
              v-for="ele in state.curTypeList.filter(
                busi => busi === 'all_types' || busiAuthList.includes(busi)
              )"
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
  <div v-if="props.activeName === 'share'" class="panel-table">
    <GridTable
      :show-pagination="false"
      :table-data="state.tableData"
      @sort-change="sortChange"
      :empty-desc="emptyDesc"
      :empty-img="imgType"
      class="workbranch-grid"
    >
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
        :label="item.label"
        :sortable="item.type === 'time' && item.field === 'time'"
      >
        <template #default="scope">
          <span v-if="item.type && item.type === 'time'">{{
            formatterTime(null, null, scope.row[item.field])
          }}</span>
          <span v-else>{{ scope.row[item.field] }}</span>
        </template>
      </el-table-column>

      <el-table-column width="96" fixed="right" key="_operation" :label="t('common.operate')">
        <template #default="scope">
          <el-tooltip effect="dark" content="新页面预览" placement="top">
            <el-icon class="hover-icon hover-icon-in-table" @click="preview(scope.row.resourceId)">
              <Icon name="icon_pc_outlined"></Icon>
            </el-icon>
          </el-tooltip>
          <ShareHandler
            :in-grid="true"
            :resource-id="scope.row.resourceId"
            :weight="scope.row.weight"
          />
        </template>
      </el-table-column>
    </GridTable>
  </div>
</template>

<style lang="less" scoped>
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
.workbranch-grid :deep(.ed-empty) {
  padding: 80px 0 !important;
  .ed-empty__description {
    margin-top: 0px;
    line-height: 20px !important;
  }
}
</style>

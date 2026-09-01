<script lang="ts" setup>
import docs from '@/assets/svg/docs.svg'
import icon_searchOutline_outlined from '@/assets/svg/icon_search-outline_outlined.svg'
import iconFilter from '@/assets/svg/icon-filter.svg'
import { ref, reactive, onMounted, computed, onBeforeUnmount, h } from 'vue'
import { RefreshLeft } from '@element-plus/icons-vue'
import { Icon } from '@/components/icon-custom'
import { FilterText, convertFilterText } from '@/components/filter-text'
import DrawerMain from '@/components/drawer-main/src/DrawerMain.vue'
import { useI18n } from '@/hooks/web/useI18n'
import { useEmitt } from '@/hooks/web/useEmitt'
import GridTable from '@/components/grid-table/src/GridTable.vue'
import { useUserStoreWithOut } from '@/store/modules/user'
import { filterOption } from '@/views/menu/system/log/options'
import {
  optionsApi,
  queryApi,
  mountedOrg,
  allUserApi,
  exportApi,
  queryUserApi,
  queryAdminOrgApi
} from '@/views/menu/system/log/logApi'
import { ElMessage, ElMessageBox, ElButton, Action } from 'element-plus-secondary'
const userStore = useUserStoreWithOut()
const { t } = useI18n()

const keyword = ref('')
const drawerMainRef = ref(null)
const optionLoad = ref(false)
const msgVisible = ref(false)
const msgContent = ref('')
const isOrgAdmin = ref(false)

const buildParam = () => {
  const param = {}
  if (state.conditions?.length) {
    state.conditions.forEach(condition => {
      if (condition['value']) {
        param[condition['field']] = condition['value']
      }
    })
  }
  if (keyword.value) {
    param['keyword'] = keyword.value
  }
  if (state.orders?.length) {
    state.orders.forEach(item => {
      param[item['field']] = item.type
    })
  }
  return param
}
const search = (pageValid?: boolean) => {
  if (!pageValid) {
    resetPage()
  }
  queryApi(state.paginationConfig.currentPage, state.paginationConfig.pageSize, buildParam()).then(
    res => {
      const records = res.data.records
      imgType.value = getEmptyImg()
      emptyDesc.value = getEmptyDesc()
      state.logList = records
      state.paginationConfig.total = res.data.total
    }
  )
}
const exportLog = () => {
  ElMessageBox.confirm(t('operate_log.confirm_export'), {
    confirmButtonType: 'primary',
    type: 'warning',
    confirmButtonText: t('common.sure'),
    cancelButtonText: t('common.cancel'),
    autofocus: false,
    showClose: false,
    callback: (action: Action) => {
      if (action === 'confirm') {
        const param = buildParam()
        exportApi(param).then(() => {
          openMessageLoading(() => {
            useEmitt().emitter.emit('data-export-center', { activeName: 'IN_PROGRESS' })
          })
        })
      }
    }
  })
}
const openMessageLoading = cb => {
  ElMessage({
    message: h('p', null, [
      t('data_set.can_go_to'),
      h(
        ElButton,
        {
          text: true,
          size: 'small',
          class: 'btn-text',
          onClick: () => {
            cb()
          }
        },
        t('data_export.export_center')
      ),
      t('data_set.progress_and_download')
    ]),
    iconClass: 'el-icon-loading',
    icon: h(RefreshLeft),
    showClose: true,
    customClass: 'de-message-loading de-message-export'
  })
}
const isProxyOrgMode = computed(() => {
  const proxyInfo = userStore.getProxyInfo
  return !!proxyInfo?.proxy && Number(proxyInfo.proxyOid) > 0
})
const isAdmin = computed(() => {
  return userStore.getUid === '1' && !isProxyOrgMode.value
})

const drawerMainOpen = async () => {
  let isOrgAdminValue = false
  if (!isAdmin.value && !isProxyOrgMode.value) {
    const res = await queryAdminOrgApi()
    const orgs = res.data || []
    isOrgAdminValue = orgs.length > 0
  }
  isOrgAdmin.value = isOrgAdminValue
  const hasOrgScope = isAdmin.value || isOrgAdminValue || isProxyOrgMode.value
  if (!hasOrgScope) {
    // 普通用户:只保留操作类型/时间/客户端过滤,移除组织过滤框
    if (state.filterOption[3]?.field === 'oid') {
      state.filterOption.splice(3, 1)
    }
  } else {
    addUserOption()
  }
  if (!optionLoad.value) {
    const res = await optionsApi()
    state.filterOption[1].option = res.data

    if (hasOrgScope) {
      if (isAdmin.value) {
        const userRes = await allUserApi()
        state.filterOption[4].option = userRes.data
      } else {
        const userRes = await queryUserApi([])
        state.filterOption[4].option = userRes.data
      }
      state.originUserList = state.filterOption[4].option
      const orgRes = await mountedOrg()
      const dataList = orgRes.data
      formatOrg(dataList)
      state.filterOption[3].option = dataList
    }

    optionLoad.value = true
  }
  drawerMainRef.value.init()
}
const drawerMainClose = () => {
  drawerMainRef.value.close()
}
const treeFilterChange = param => {
  const { value, field } = param
  if (!optionLoad.value || field !== 'oid') return
  if (!isAdmin.value && !isOrgAdmin.value && !isProxyOrgMode.value) return
  drawerMainRef.value.cleanrInnerValue(4)
  if (!value?.length) {
    // 清空组织过滤 → 恢复范围内全部用户
    state.filterOption[4].option = state.originUserList
    return
  }
  queryUserApi(value).then(res => {
    state.filterOption[4].option = res.data
  })
}
const addUserOption = () => {
  if (state.filterOption.length == 4) {
    state.filterOption.splice(4, 0, {
      type: 'select',
      option: [],
      field: 'uid',
      title: t('operate_log.user'),
      operate: 'in',
      property: {
        placeholder: t('system.user')
      }
    })
  }
}
const formatOrg = list => {
  const stack = [...list]
  while (stack.length) {
    const item = stack.pop()
    item.value = item.id
    item.label = item.name
    item.disabled = item.readOnly
    if (item.children?.length) {
      item.children.forEach(kid => stack.push(kid))
    }
  }
}
interface FieldSort {
  field: string
  type: boolean
}
const state = reactive({
  filterOption: [],
  originUserList: [],
  logList: [],
  filterTexts: [],
  paginationConfig: {
    currentPage: 1,
    pageSize: 10,
    total: 0
  },
  conditions: [],
  orders: [] as FieldSort[]
})
state.filterOption = state.filterOption.concat(filterOption)
state.filterTexts = []

const clearFilter = (index?: number) => {
  let isOrg = false
  if (isNaN(index)) {
    state.filterTexts = []
  } else {
    state.filterTexts.splice(index, 1)
    isOrg = state.conditions[index]?.field === 'oid'
  }
  drawerMainRef.value.clearFilter(index)
  if (isOrg) {
    clearUserFilterWithOrg()
  }
}

const clearUserFilterWithOrg = () => {
  for (let index = 0; index < state.conditions.length; index++) {
    const element = state.conditions[index]
    if (element.field === 'uid') {
      clearFilter(index)
      return
    }
  }
}

const searchCondition = conditions => {
  state.conditions = conditions
  search()
  fillFilterText()
  drawerMainClose()
}

const fillFilterText = () => {
  const textArray = state.conditions?.length
    ? convertFilterText(state.conditions, state.filterOption)
    : []
  state.filterTexts = [...textArray]
}

const pageChange = index => {
  if (typeof index !== 'number') {
    return
  }
  state.paginationConfig.currentPage = index
  search(true)
}
const sizeChange = size => {
  state.paginationConfig.currentPage = 1
  state.paginationConfig.pageSize = size
  search(true)
}

const resetPage = () => {
  state.paginationConfig.currentPage = 1
  state.paginationConfig.pageSize = 10
}
const sortChange = param => {
  state.orders = []
  if (param.order && param.prop === 'time') {
    const type = param.order.substring(0, param.order.indexOf('ending'))
    state.orders.push({
      field: 'timeDesc',
      type: type !== 'asc'
    })
    search()
  }
}

const imgType = ref()
const emptyDesc = ref('')
const getEmptyImg = (): string => {
  if (keyword.value) {
    return 'tree'
  }
  return 'noneWhite'
}

const getEmptyDesc = (): string => {
  if (keyword.value) {
    return t('operate_log.relevant_content_found')
  }

  return ''
}
const timestampFormatDate = value => {
  if (!value) {
    return '-'
  }
  return new Date(value)['format']()
}
const showErrorMsg = row => {
  msgVisible.value = true
  msgContent.value = row.msg
}
const handleClose = () => {
  msgVisible.value = false
  msgContent.value = ''
}

onMounted(() => {
  search()
})
onBeforeUnmount(() => {
  optionLoad.value = false
})
</script>

<template>
  <div class="de-log-container">
    <el-row class="log-table__filter log-top-operate">
      <el-col :span="12">
        <el-button @click="exportLog" type="primary">
          {{ t('auth.export') }}
        </el-button>
      </el-col>
      <el-col :span="12" class="log-right-filter">
        <el-input
          v-model="keyword"
          clearable
          :placeholder="t('operate_log.search_by_operate_info')"
          @change="search"
        >
          <template #prefix>
            <el-icon>
              <Icon name="icon_search-outline_outlined"
                ><icon_searchOutline_outlined class="svg-icon"
              /></Icon>
            </el-icon>
          </template>
        </el-input>
        <el-button
          @click="drawerMainOpen"
          :plain="!!state.conditions.length"
          :class="state.conditions.length ? 'filter-condition-button' : 'filter-button'"
        >
          <template #icon>
            <Icon name="icon-filter"><iconFilter class="svg-icon" /></Icon>
          </template>
          {{
            t('common.filter') + (state.conditions.length ? `(${state.conditions?.length})` : '')
          }}
        </el-button>
      </el-col>
    </el-row>
    <filter-text
      @clear-filter="clearFilter"
      :total="state.paginationConfig.total"
      :filter-texts="state.filterTexts"
    ></filter-text>
    <div :class="[state.filterTexts.length ? 'is-in-filter' : 'user-table__content']">
      <GridTable
        ref="multipleTableRef"
        :pagination="state.paginationConfig"
        :table-data="state.logList"
        :empty-desc="emptyDesc"
        :empty-img="imgType"
        class="popper-max-width"
        @current-change="pageChange"
        @size-change="sizeChange"
        @sort-change="sortChange"
      >
        <el-table-column
          key="opText"
          show-overflow-tooltip
          prop="opText"
          :label="t('operate_log.type')"
          width="150"
        />
        <el-table-column
          prop="opDetail"
          key="opDetail"
          :label="t('operate_log.detail')"
          min-width="200"
          show-overflow-tooltip
        />
        <el-table-column
          prop="success"
          key="success"
          :label="t('operate_log.status')"
          min-width="150"
        >
          <template v-slot:default="scope">
            <span
              :class="{ 'de-log-success': scope.row.success, 'de-log-error': !scope.row.success }"
              class="de-status"
            >
              <span>{{
                scope.row.success ? t('operate_log.success') : t('operate_log.failed')
              }}</span>
              <el-icon
                v-if="!scope.row.success"
                class="log-msg-icon"
                @click="showErrorMsg(scope.row)"
              >
                <icon name="docs"><docs class="svg-icon" /></icon>
              </el-icon>
            </span>
          </template>
        </el-table-column>
        <el-table-column
          prop="name"
          key="name"
          :label="t('operate_log.user')"
          width="150"
          show-overflow-tooltip
        />
        <el-table-column
          prop="ip"
          show-overflow-tooltip
          key="ip"
          :label="t('operate_log.ip')"
          width="170"
        />
        <el-table-column prop="time" :label="t('operate_log.time')" sortable="custom" width="175">
          <template v-slot:default="scope">
            <span>{{ timestampFormatDate(scope.row.time) }}</span>
          </template>
        </el-table-column>
      </GridTable>
    </div>
  </div>
  <drawer-main
    :filter-options="state.filterOption"
    @trigger-filter="searchCondition"
    @tree-filter-change="treeFilterChange"
    ref="drawerMainRef"
  />

  <el-dialog
    v-model="msgVisible"
    :label="t('operate_log.error_msg')"
    :show-close="false"
    width="30%"
    align-center
    :before-close="handleClose"
  >
    <div class="msg-content">
      {{ msgContent }}
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="msgVisible = false">{{ t('commons.close') }}</el-button>
      </span>
    </template>
  </el-dialog>
</template>
<style lang="less" scoped>
.de-status {
  position: relative;
  margin-left: 15px;
  &::before {
    content: '';
    position: absolute;
    top: 50%;
    left: -13px;
    transform: translateY(-50%);
    width: 5px;
    height: 5px;
    border-radius: 50%;
  }
  .log-msg-icon {
    cursor: pointer;
    top: 2px;
    left: 6px;
  }
}
.de-log-success {
  &::before {
    background: #34c724;
  }
}
.de-log-error {
  &::before {
    background: #f54a45;
  }
}
.de-log-container {
  padding: 24px;
  width: 100%;
  background: var(--ContentBG, #ffffff);
  height: calc(100% - 9px) !important;
  box-sizing: border-box;
  border-radius: 12px;
  margin-top: 8px;

  .user-table__content {
    height: calc(100vh - 196px);
  }

  .is-in-filter {
    height: calc(100vh - 246px);
  }
  .log-empty {
    padding-top: 136px !important;
  }
  .log-table__filter {
    height: 32px;
    margin-bottom: 16px;
    .log-right-filter {
      text-align: right;
      display: flex;
      align-items: center;
      justify-content: flex-end;
      .ed-input {
        width: 240px;
        margin-right: 12px;
      }
      .filter-button {
        &:hover {
          color: #bbbfc4;
          border-color: #bbbfc4;
          background-color: #f5f6f7;
          outline: 0;
        }
        &:focus {
          color: #bbbfc4;
          border-color: #bbbfc4;
          background-color: #eff0f1;
          outline: 0;
        }
      }
    }
  }
}
</style>

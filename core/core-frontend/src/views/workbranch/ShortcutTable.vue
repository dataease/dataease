<script lang="ts" setup>
import { useI18n } from '@/hooks/web/useI18n'
import { ref, reactive, onMounted } from 'vue'
import type { TabsPaneContext } from 'element-plus-secondary'
import GridTable from '@/components/grid-table/src/GridTable.vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { shotcutOption } from './ShortcutOption'
const { resolve } = useRouter()
const { t } = useI18n()

const panelKeyword = ref()
const activeName = ref('recent')
const userAddPopper = ref(false)
const activeCommand = ref('all_types')
const dialogVisible = ref(false)
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
}
const handleClick = (ele: TabsPaneContext) => {
  shotcutOption.setBusiFlag(ele.paneName)
  state.curTypeList = shotcutOption.getBusiList()
  state.tableColumn = shotcutOption.getColmunList()
  loadTableData()
}
const triggerFilterPanel = val => {
  console.log(val)
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

const share = data => {
  dialogVisible.value = true
  console.log(data)
}

const openShare = ref(true)
const overTime = ref(false)
const passwdProtect = ref(false)

const loadTableData = () => {
  shotcutOption.loadData({}).then(res => {
    state.tableData = res.data
  })
}

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
</script>

<template>
  <div class="dashboard-type">
    <el-tabs v-model="activeName" class="dashboard-type-tabs" @tab-click="handleClick">
      <el-tab-pane label="最近使用" name="recent"></el-tab-pane>
      <el-tab-pane label="我的收藏" name="store"></el-tab-pane>
      <el-tab-pane label="分享" name="share"></el-tab-pane>
    </el-tabs>
    <el-row>
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
    <div class="panel-table">
      <GridTable :show-pagination="false" :table-data="state.tableData">
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

              <el-tooltip effect="dark" :content="t('visualization.share')" placement="top">
                <el-icon class="hover-icon hover-icon-in-table" @click="share(scope.row)">
                  <Icon name="icon_share-label_outlined"></Icon>
                </el-icon>
              </el-tooltip>
            </template>
          </template>
        </el-table-column>
      </GridTable>
    </div>
  </div>
  <el-dialog
    class="copy-link_dialog"
    v-model="dialogVisible"
    title="公共链接分享"
    width="480px"
    :show-close="false"
  >
    <div class="copy-link">
      <div class="open-share flex-align-center">
        <el-switch size="small" v-model="openShare" />
        开启后，互联网用户可通过链接访问大屏
      </div>
      <div class="text">https://perier123.youdata.163.com/dash</div>
      <div>
        <el-checkbox v-model="overTime" :label="t('visualization.over_time')" />
      </div>
      <div>
        <el-checkbox v-model="passwdProtect" :label="t('visualization.passwd_protect')" />
      </div>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button type="primary" @click="dialogVisible = false">
          {{ t('visualization.copy_link') }}
        </el-button>
      </span>
    </template>
  </el-dialog>
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
.copy-link_dialog {
  .ed-dialog__footer {
    border-top: 1px solid rgba(31, 35, 41, 0.15);
    padding: 16px;
  }
  .copy-link {
    font-weight: 400;
    font-family: PingFang SC;

    .open-share {
      margin: -18px 0 8px 0;
      color: #646a73;
      font-size: 12px;
      font-style: normal;
      line-height: 20px;
      .ed-switch {
        margin-right: 8px;
      }
    }

    .text {
      border-radius: 4px;
      border: 1px solid #bbbfc4;
      background: #eff0f1;
      margin-bottom: 16px;
      height: 32px;
      padding: 5px 12px;
      color: #8f959e;
      font-size: 14px;
      font-style: normal;
      line-height: 22px;
    }
  }
}
</style>

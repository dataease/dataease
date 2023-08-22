<script lang="ts" setup>
import { useI18n } from '@/hooks/web/useI18n'
import { ref, shallowRef, reactive } from 'vue'
import type { TabsPaneContext } from 'element-plus-secondary'
import GridTable from '@/components/grid-table/src/GridTable.vue'
import imgtest from '@/assets/img/dataease-10000Star.jpg'
import { HandleMore } from '@/components/handle-more'
import { usePermissionStoreWithOut } from '@/store/modules/permission'
import { useRequestStoreWithOut } from '@/store/modules/request'
import { findRecent } from '@/api/visualization/dataVisualization'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
const { resolve } = useRouter()
const permissionStore = usePermissionStoreWithOut()
const requestStore = useRequestStoreWithOut()
const { t } = useI18n()
const panelKeyword = ref()
const state = reactive({
  panelList: [
    {
      name: '第一个参数是一个异步函数，在组件初始化时，会自动执行该异步函数'
    }
  ]
})
const quickCreationList = shallowRef([
  {
    icon: 'icon_dashboard_outlined',
    name: 'panel'
  },
  {
    icon: 'icon_operation-analysis_outlined',
    name: 'screen'
  },
  {
    icon: 'icon_app_outlined',
    name: 'dataset'
  },
  {
    icon: 'icon_database_outlined',
    name: 'datasource'
  }
])

const expandFold = ref('fold')
const handleExpandFold = () => {
  expandFold.value = expandFold.value === 'expand' ? 'fold' : 'expand'
}

const dialogVisible = ref(false)

const tabBtnList = ['推荐仪表板', t('auth.screen'), '应用模版']
const activeTabBtn = ref('推荐仪表板')
const typeList = quickCreationList.value.map(ele => ele.name)
typeList.unshift('all_types')

const handleClick = (ele: TabsPaneContext) => {
  console.log(ele)
}

const formatterTime = (_, _column, cellValue) => {
  return dayjs(new Date(cellValue)).format('YYYY-MM-DD HH:mm:ss')
}

const getTableData = () => {
  findRecent().then(res => {
    state.panelList = res
  })
}
getTableData()
const menuList = [
  {
    label: t('visualization.share'),
    command: 'share',
    svgName: 'icon_share-label_outlined'
  },
  {
    label: t('visualization.store'),
    command: 'store',
    svgName: 'icon_collection_outlined'
  }
]
const triggerFilterPanel = val => {
  console.log(val)
}
const activeName = ref('recentlyUsed')

const userAddPopper = ref(false)

const handleVisibleChange = (val: boolean) => {
  userAddPopper.value = val
}

const handleCommand = (command: string) => {
  activeCommand.value = command
}

const openShare = ref(true)
const overTime = ref(false)
const passwdProtect = ref(false)
const operation = (cmd: string, id) => {
  if (cmd === 'share') {
    dialogVisible.value = true
    console.log(cmd, id)
  }
}

const preview = ({ id }) => {
  const routeUrl = resolve({
    path: '/preview',
    query: { dvId: id }
  })
  window.open(routeUrl.href, '_blank')
}

const activeCommand = ref('all_types')
</script>

<template>
  <div class="workbranch" v-loading="requestStore.loadingMap[permissionStore.currentPath]">
    <div class="info-quick-creation">
      <div class="user-info">
        <el-icon class="main-color">
          <Icon name="user-img" />
        </el-icon>
        <div class="info">
          <div class="name-role flex-align-center">
            <span class="name">裴佩尔</span>
            <span class="role main-btn">{{ t('role.org_admin') }}</span>
          </div>
          <span class="id"> ID: 226204665738747993 </span>
        </div>
        <div class="item">
          <span class="name">
            {{ t('auth.dataset') }}
          </span>
          <span class="num"> 28 </span>
        </div>
        <div class="item">
          <span class="name">
            {{ t('auth.panel') }}
          </span>
          <span class="num"> 28 </span>
        </div>
        <div class="item">
          <span class="name">
            {{ t('auth.screen') }}
          </span>
          <span class="num"> 28 </span>
        </div>
      </div>

      <div class="quick-creation">
        <span class="label"> 快速创建 </span>
        <div class="item-creation">
          <div
            v-permission="[ele.name]"
            :key="ele.name"
            class="item"
            v-for="ele in quickCreationList"
          >
            <el-icon class="main-color">
              <Icon :name="ele.icon" />
            </el-icon>
            <span class="name">
              {{ t(`auth.${ele.name}`) }}
            </span>
          </div>
        </div>
      </div>
    </div>
    <div class="template-market-dashboard">
      <div class="template-market">
        <div class="label">
          模版市场
          <div class="expand-all">
            <button class="all flex-center">查看全部</button>
            <el-divider direction="vertical" />
            <button @click="handleExpandFold" class="expand flex-center">
              {{ t(`visualization.${expandFold}`) }}
            </button>
          </div>
        </div>
        <template v-if="expandFold === 'fold'">
          <div class="tab-btn">
            <div
              @click="activeTabBtn = ele"
              v-for="ele in tabBtnList"
              :key="ele"
              :class="activeTabBtn === ele && 'active'"
              class="main-btn"
            >
              {{ ele }}
            </div>
          </div>
          <div class="template-list">
            <div class="template">
              <div class="photo">
                <img :src="imgtest" alt="" />
              </div>
              <div class="apply">
                <span title="电子银行业务分析" class="name ellipsis"> 电子银行业务分析 </span>
                <el-button class="flex-center" secondary>{{ t('dataset.preview') }}</el-button>
                <el-button class="flex-center" type="primary">{{ t('commons.apply') }}</el-button>
              </div>
            </div>
            <div class="template">
              <div class="photo">
                <img :src="imgtest" alt="" />
              </div>
              <div class="apply">
                <span title="电子银行业务分析" class="name ellipsis"> 电子银行业务分析 </span>
                <el-button class="flex-center" secondary>{{ t('dataset.preview') }}</el-button>
                <el-button class="flex-center" type="primary">{{ t('commons.apply') }}</el-button>
              </div>
            </div>
          </div>
        </template>
      </div>
      <div class="dashboard-type">
        <el-tabs v-model="activeName" class="dashboard-type-tabs" @tab-click="handleClick">
          <el-tab-pane label="最近使用" name="recentlyUsed"></el-tab-pane>
          <el-tab-pane label="我的收藏" name="myCollection"></el-tab-pane>
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
                    v-for="ele in typeList"
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
          <GridTable :show-pagination="false" :table-data="state.panelList">
            <el-table-column key="name" width="280" prop="name" :label="t('common.name')">
              <template v-slot:default="scope">
                <div class="name-content">
                  <el-icon class="main-color"> <Icon name="icon_dashboard_outlined" /> </el-icon
                  ><el-tooltip placement="top">
                    <template #content>{{ scope.row.name }}</template>
                    <span class="ellipsis" style="max-width: 250px">{{ scope.row.name }}</span>
                  </el-tooltip>
                </div>
              </template>
            </el-table-column>
            <el-table-column
              key="type"
              show-overflow-tooltip
              prop="type"
              :label="t('datasource.type')"
            />
            <el-table-column
              key="create_by"
              show-overflow-tooltip
              prop="createBy"
              :label="t('visualization.create_by')"
            />

            <el-table-column
              prop="updateBy"
              show-overflow-tooltip
              key="updateBy"
              label="最近编辑人"
            />

            <el-table-column
              prop="updateTime"
              show-overflow-tooltip
              sortable
              key="updateTime"
              :formatter="formatterTime"
              label="最近编辑时间"
            />

            <el-table-column
              width="96"
              fixed="right"
              key="_operation"
              :label="$t('common.operate')"
            >
              <template #default="scope">
                <template v-if="['dashboard', 'dataV'].includes(scope.row.type)">
                  <el-tooltip effect="dark" content="新页面预览" placement="top">
                    <el-icon class="hover-icon hover-icon-in-table" @click="preview(scope.row)">
                      <Icon name="icon_pc_outlined"></Icon>
                    </el-icon>
                  </el-tooltip>
                </template>
                <handle-more
                  inTable
                  @handle-command="cmd => operation(cmd, scope.row.id)"
                  :menu-list="menuList"
                ></handle-more>
              </template>
            </el-table-column>
          </GridTable>
        </div>
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
  </div>
</template>

<style lang="less" scoped>
.workbranch {
  width: 100vw;
  height: calc(100vh - 56px);
  background: #f5f6f7;
  padding: 24px;
  display: flex;
  justify-content: space-between;

  .main-btn {
    display: inline-flex;
    height: 20px;
    padding: 0 6px;
    align-items: center;
  }

  .info-quick-creation {
    width: 360px;
    .main-color {
      background: #3370ff;
    }
    .user-info {
      padding: 24px 16px 16px 16px;
      background: #fff;
      border-radius: 4px;
      display: flex;
      flex-wrap: wrap;
      justify-content: space-between;
      .ed-icon {
        font-size: 48px;
        padding: 8px;
        border-radius: 50%;
      }

      .info {
        margin: 0 0 24px 12px;
        display: flex;
        flex-wrap: wrap;
        width: calc(100% - 60px);
        .name-role {
          margin-bottom: 4px;
          color: #1f2329;
          font-family: PingFang SC;
          font-style: normal;
          .name {
            font-size: 16px;
            font-weight: 500;
            line-height: 24px;
          }

          .role {
            display: inline-flex;
            margin-left: 4px;
            height: 20px;
            padding: 0 6px;
            align-items: center;
            font-size: 12px;
            color: #2b5fd9;
            border-radius: 2px;
            background: rgba(51, 112, 255, 0.2);
          }
        }
        .id {
          color: #646a73;
          font-size: 14px;
          font-weight: 400;
          line-height: 22px;
        }
      }

      .item {
        font-family: PingFang SC;
        font-style: normal;
        display: flex;
        flex-direction: column;
        cursor: pointer;
        width: 109px;
        height: 70px;
        padding: 8px;
        &:hover {
          border-radius: 4px;
          background: rgba(31, 35, 41, 0.1);
        }

        .name {
          color: #646a73;
          font-weight: 400;
          line-height: 22px;
        }
        .num {
          margin-top: 4px;
          color: #1f2329;
          font-size: 20px;
          font-weight: 500;
          line-height: 28px;
          letter-spacing: -0.2px;
        }
      }
    }

    .quick-creation {
      border-radius: 4px;
      background: #fff;
      margin-top: 16px;
      padding: 24px;

      .label {
        color: #1f2329;
        font-feature-settings: 'clig' off, 'liga' off;
        font-family: PingFang SC;
        font-size: 16px;
        font-style: normal;
        font-weight: 500;
        line-height: 24px;
      }

      .item-creation {
        display: flex;
        justify-content: space-between;
        flex-wrap: wrap;
        .item {
          padding: 16px;
          width: 148px;
          margin-top: 16px;
          border-radius: 4px;
          border: 1px solid #dee0e3;
          display: flex;
          align-items: center;
          cursor: pointer;
          &:hover {
            box-shadow: 0px 6px 24px 0px rgba(31, 35, 41, 0.08);
          }

          .main-color {
            font-size: 21.33px;
            padding: 5.33px;
            margin-right: 12px;
            border-radius: 4px;
            color: #fff;
          }

          .name {
            color: #1f2329;
            font-family: PingFang SC;
            font-size: 14px;
            font-style: normal;
            font-weight: 400;
            line-height: 22px;
          }
        }
      }
    }
  }

  .template-market-dashboard {
    width: calc(100% - 384px);
    height: 100%;

    .template-market {
      padding: 24px;
      background: #fff;
      border-radius: 4px;
      .label {
        color: #1f2329;
        font-feature-settings: 'clig' off, 'liga' off;
        font-family: PingFang SC;
        font-size: 16px;
        font-style: normal;
        font-weight: 500;
        line-height: 24px;
        display: flex;
        justify-content: space-between;
        .expand-all {
          display: flex;
          align-items: center;
          font-size: 14px;
          font-weight: 400;
          line-height: 22px;
          border-radius: 4px;

          .flex-center {
            padding: 0 4px;
            color: #646a73;
            border-radius: 4px;
            height: 26px;
            border: none;
            outline: none;
            background: none;
            &:hover {
              background: rgba(31, 35, 41, 0.1);
            }

            &:active {
              background: rgba(31, 35, 41, 0.2);
            }
          }
        }
      }

      .tab-btn {
        margin: 16px 0 8px 0;
        .main-btn {
          color: #1f2329;
          cursor: pointer;
          height: 24px;
          border-radius: 4px;
          background: rgba(31, 35, 41, 0.1);
          display: inline-flex;
          align-items: center;
          padding: 0 8px;
          font-family: PingFang SC;
          font-size: 12px;
          font-style: normal;
          font-weight: 400;
          line-height: 20px;
          & + .main-btn {
            margin-left: 8px;
          }

          &:hover {
            color: #2b5fd9;
          }

          &.active {
            color: #2b5fd9;
            background: rgba(51, 112, 255, 0.2);
          }
        }
      }

      .template-list {
        display: flex;
        margin-left: -16px;
        .template {
          border: 1px solid #d9d9d9;
          border-radius: 4px;
          display: flex;
          flex-wrap: wrap;
          width: 181px;
          height: 141px;
          margin-left: 16px;
          position: relative;

          .photo {
            padding: 4px;
            padding-bottom: 0;
            height: 101px;
            img {
              width: 100%;
              height: 100%;
              border-top-left-radius: 4px;
              border-top-right-radius: 4px;
            }
          }

          .apply {
            padding: 8px 12px;
            background: #fff;
            border-top: 1px solid #d9d9d9;
            position: absolute;
            width: 100%;
            left: 0;
            bottom: 0;
            height: 39px;
            display: flex;
            flex-wrap: wrap;
            border-bottom-left-radius: 4px;
            border-bottom-right-radius: 4px;
            justify-content: space-between;

            .ed-button {
              min-width: 73px;
              height: 28px;
              display: none;
              font-size: 12px;
              line-height: 20px;
              padding: 0;
              margin-top: 8px;
              & + .ed-button {
                margin-left: 8px;
              }
            }
            .name {
              color: #1f2329;
              font-family: PingFang SC;
              font-size: 14px;
              font-style: normal;
              font-weight: 500;
              line-height: 22px;
              width: 100%;
            }
          }

          &:hover {
            box-shadow: 0px 6px 24px 0px rgba(31, 35, 41, 0.08);
            .apply {
              height: 73px;
            }
            .ed-button {
              display: block;
            }
          }
        }
      }
    }

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
      }
    }
  }
}
</style>
<style lang="less">
.menu-panel-select_popper {
  margin-top: -10px !important;
  .ed-popper__arrow {
    display: none;
  }

  .ed-dropdown-menu__item {
    padding: 5px 11px;
    &.active {
      color: #3370ff;

      .ed-icon {
        font-size: 16px;
        margin-left: 20px;
        margin-right: 0;
      }
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

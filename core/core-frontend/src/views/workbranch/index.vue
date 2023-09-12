<script lang="ts" setup>
import { useI18n } from '@/hooks/web/useI18n'
import { ref, shallowRef, computed } from 'vue'

import imgtest from '@/assets/img/dataease-10000Star.jpg'
import { usePermissionStoreWithOut } from '@/store/modules/permission'
import { useRequestStoreWithOut } from '@/store/modules/request'
import { interactiveStoreWithOut } from '@/store/modules/interactive'
import ShortcutTable from './ShortcutTable.vue'
import { useUserStoreWithOut } from '@/store/modules/user'
import { useRouter } from 'vue-router'
const userStore = useUserStoreWithOut()
const interactiveStore = interactiveStoreWithOut()
const permissionStore = usePermissionStoreWithOut()
const requestStore = useRequestStoreWithOut()
const { t } = useI18n()
const busiDataMap = computed(() => interactiveStore.getData)
const busiCountCardList = ref([])

const router = useRouter()

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

const tabBtnList = ['推荐仪表板', t('auth.screen'), '应用模版']
const activeTabBtn = ref('推荐仪表板')
const typeList = quickCreationList.value.map(ele => ele.name)
typeList.unshift('all_types')

const fillCardInfo = () => {
  for (const key in busiDataMap.value) {
    if (key !== '3') {
      busiCountCardList.value.push(busiDataMap.value[key])
    }
    quickCreationList.value[key]['menuAuth'] = busiDataMap.value[key]['menuAuth']
    quickCreationList.value[key]['anyManage'] = busiDataMap.value[key]['anyManage']
  }
}
const quickCreate = (flag: number, hasAuth: boolean) => {
  if (!hasAuth) {
    return
  }
  switch (flag) {
    case 0:
      createPanel()
      break
    case 1:
      createScreen()
      break
    case 2:
      createDataset()
      break
    case 3:
      createDatasource()
      break
    default:
      break
  }
}
const createPanel = () => {
  console.error('please contact Jiahao Wang')
}
const createScreen = () => {
  console.error('please contact Jiahao Wang')
}
const createDataset = () => {
  let routeData = router.resolve({
    path: '/dataset-form'
  })
  window.open(routeData.href, '_blank')
}
const createDatasource = () => {
  console.error('please contact Jinlong Tao')
}
fillCardInfo()
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
            <span class="name">{{ userStore.getName }}</span>
            <span class="role main-btn" />
          </div>
          <span class="id"> {{ `ID: ${userStore.getUid}` }} </span>
        </div>
        <div
          class="item"
          :class="{ 'de-item-hidden': !item['menuAuth'] }"
          v-for="(item, index) in busiCountCardList"
          :key="index"
        >
          <span class="name">
            {{ t(`auth.${quickCreationList[index].name}`) }}
          </span>
          <span class="num"> {{ item['menuAuth'] ? item['leafNodeCount'] : '*' }} </span>
        </div>
      </div>

      <div class="quick-creation">
        <span class="label"> 快速创建 </span>
        <div class="item-creation">
          <div
            :key="ele.name"
            class="item"
            :class="{
              'quick-create-disabled': !ele['menuAuth'] || !ele['anyManage']
            }"
            v-for="(ele, index) in quickCreationList"
            @click="quickCreate(index, ele['menuAuth'] && ele['anyManage'])"
          >
            <el-tooltip
              v-if="!ele['menuAuth'] || !ele['anyManage']"
              class="box-item"
              effect="dark"
              content="缺少创建权限"
              placement="top"
            >
              <div class="empty-tooltip-container" />
            </el-tooltip>
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
      <shortcut-table />
    </div>
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
      width: 32px;
      height: 32px;
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
            width: 55px;
            display: inline-flex;
            margin-left: 4px;
            height: 20px;
            padding: 0 6px;
            align-items: center;
            font-size: 12px;
            color: #2b5fd9;
            border-radius: 2px;
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
        width: 109px;
        height: 70px;
        padding: 8px;

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

      .de-item-hidden {
        cursor: not-allowed;
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
        .quick-create-disabled {
          cursor: not-allowed;
          color: var(--ed-color-info-light-5);
          background-color: var(--ed-color-info-light-9);
          border-color: var(--ed-color-info-light-8);
          .name {
            color: var(--ed-color-info-light-5) !important;
          }
          .main-color {
            background-color: var(--ed-color-primary-light-8) !important;
            border-color: var(--ed-color-info-light-8) !important;
          }
          .empty-tooltip-container {
            width: 146px;
            position: absolute;
            height: 52px;
            margin-left: -16px;
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
</style>

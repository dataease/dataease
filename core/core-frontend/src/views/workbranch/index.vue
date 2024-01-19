<script lang="ts" setup>
import { useI18n } from '@/hooks/web/useI18n'
import { ref, shallowRef, computed, reactive, watch, nextTick } from 'vue'

import { usePermissionStoreWithOut } from '@/store/modules/permission'
import { useRequestStoreWithOut } from '@/store/modules/request'
import { interactiveStoreWithOut } from '@/store/modules/interactive'
import ShortcutTable from './ShortcutTable.vue'
import { useUserStoreWithOut } from '@/store/modules/user'
import { useRouter } from 'vue-router'
import { searchMarketRecommend } from '@/api/templateMarket'
import TemplateBranchItem from '@/views/workbranch/TemplateBranchItem.vue'
import { ElMessage } from 'element-plus-secondary'
import { decompression } from '@/api/visualization/dataVisualization'
import { useCache } from '@/hooks/web/useCache'
import DeResourceCreateOptV2 from '@/views/common/DeResourceCreateOptV2.vue'
const userStore = useUserStoreWithOut()
const interactiveStore = interactiveStoreWithOut()
const permissionStore = usePermissionStoreWithOut()
const requestStore = useRequestStoreWithOut()
const { t } = useI18n()
const busiDataMap = computed(() => interactiveStore.getData)
const busiCountCardList = ref([])
const { wsCache } = useCache()
const { push } = useRouter()
const router = useRouter()
const resourceCreateOpt = ref(null)

const quickCreationList = shallowRef([
  {
    icon: 'icon_dashboard_outlined',
    name: 'panel',
    color: '#3370ff'
  },
  {
    icon: 'icon_operation-analysis_outlined',
    name: 'screen',
    color: '#00d6b9'
  },
  {
    icon: 'icon_app_outlined',
    name: 'dataset',
    color: '#16c0ff'
  },
  {
    icon: 'icon_database_outlined',
    name: 'datasource',
    color: '#7f3bf6'
  }
])

const expandFold = ref('fold')
const handleExpandFold = () => {
  expandFold.value = expandFold.value === 'expand' ? 'fold' : 'expand'
}

const showTemplate = computed(() => {
  return state.networkStatus && state.hasResult
})

const createAuth = computed(() => {
  return {
    PANEL: havePanelAuth.value,
    SCREEN: haveScreenAuth.value
  }
})

const havePanelAuth = computed(() => {
  return quickCreationList.value[0]['menuAuth'] && quickCreationList.value[0]['anyManage']
})

const haveScreenAuth = computed(() => {
  return quickCreationList.value[1]['menuAuth'] && quickCreationList.value[1]['anyManage']
})

const activeTabChange = value => {
  activeTabBtn.value = value
}

const tabBtnList = [
  {
    name: '推荐仪表板',
    value: 'PANEL'
  },
  {
    name: t('auth.screen'),
    value: 'SCREEN'
  }
]
const activeTabBtn = ref('PANEL')
const typeList = quickCreationList.value.map(ele => ele.name)
typeList.unshift('all_types')

const state = reactive({
  templateType: 'PANEL',
  baseUrl: null,
  marketTemplatePreviewShowList: [],
  hasResult: false,
  networkStatus: true,
  loading: false,
  dvCreateForm: {
    resourceName: null,
    name: null,
    pid: null,
    nodeType: 'panel',
    templateUrl: null,
    newFrom: 'new_market_template',
    panelType: 'self',
    panelStyle: {},
    panelData: '[]'
  }
})

watch(
  () => activeTabBtn.value,
  () => {
    initTemplateShow()
  }
)

const initMarketTemplate = async () => {
  await searchMarketRecommend()
    .then(rsp => {
      state.baseUrl = rsp.data.baseUrl
      state.marketTemplatePreviewShowList = rsp.data.contents
      state.hasResult = true
      initTemplateShow()
    })
    .catch(() => {
      state.networkStatus = false
    })
}

const initTemplateShow = () => {
  state.hasResult = false
  state.marketTemplatePreviewShowList.forEach(template => {
    template.showFlag = templateShowCur(template)
    if (template.showFlag) {
      state.hasResult = true
    }
  })
}

const templateShowCur = templateItem => {
  let templateTypeMarch = false
  if (activeTabBtn.value === templateItem.templateType) {
    templateTypeMarch = true
  }
  return templateTypeMarch
}
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
  const baseUrl = '#/dashboard?opt=create'
  window.open(baseUrl, '_blank')
}

const createScreen = () => {
  const baseUrl = '#/dvCanvas?opt=create'
  window.open(baseUrl, '_blank')
}
const createDataset = () => {
  let routeData = router.resolve({
    path: '/dataset-form'
  })
  window.open(routeData.href, '_blank')
}
const createDatasource = () => {
  const baseUrl = '#/data/datasource?opt=create'
  window.open(baseUrl, '_blank')
}

const templatePreview = previewId => {
  wsCache.set(`template-preview-id`, previewId)
  toTemplateMarket()
}

const templateApply = template => {
  state.dvCreateForm.name = template.title
  state.dvCreateForm.templateUrl = template.metas.theme_repo
  state.dvCreateForm.resourceName = template.id
  apply()
}

const apply = () => {
  if (!state.dvCreateForm.templateUrl) {
    ElMessage.warning('未获取模板下载链接请联系模板市场官方')
    return false
  }
  state.loading = true
  decompression(state.dvCreateForm)
    .then(response => {
      state.loading = false
      const templateData = response.data
      // do create
      wsCache.set(`de-template-data`, JSON.stringify(templateData))
      const baseUrl =
        templateData.type === 'dataV'
          ? '#/dvCanvas?opt=create&createType=template'
          : '#/dashboard?opt=create&createType=template'
      window.open(baseUrl, '_blank')
    })
    .catch(() => {
      state.loading = false
    })
}

const toTemplateMarket = () => {
  push('/template-market/index')
}

const toTemplateMarketAdd = () => {
  if (havePanelAuth.value || haveScreenAuth.value) {
    const params = {
      curPosition: 'branchCreate',
      templateType: 'all'
    }
    resourceCreateOpt.value.optInit(params)
  }
}

fillCardInfo()
initMarketTemplate()
</script>

<template>
  <div class="workbranch" v-loading="requestStore.loadingMap[permissionStore.currentPath]">
    <div class="info-quick-creation">
      <div class="user-info">
        <el-icon class="main-color user-icon-container">
          <Icon name="user-img" />
        </el-icon>
        <div class="info">
          <div class="name-role flex-align-center">
            <span class="name">{{ userStore.getName }}</span>
            <span class="role main-btn" />
          </div>
          <span v-if="userStore.getUid" class="id"> {{ `ID: ${userStore.getUid}` }} </span>
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
            <el-icon class="main-color" :style="{ backgroundColor: ele.color }">
              <Icon :name="ele.icon" />
            </el-icon>
            <span class="name">
              {{ t(`auth.${ele.name}`) }}
            </span>
          </div>
          <div
            class="item item-quick"
            :class="{
              'quick-create-disabled': !(havePanelAuth || haveScreenAuth)
            }"
            @click="toTemplateMarketAdd"
          >
            <el-tooltip
              v-if="!(havePanelAuth || haveScreenAuth)"
              class="box-item"
              effect="dark"
              content="缺少创建权限"
              placement="top"
            >
              <div class="empty-tooltip-container-template" />
            </el-tooltip>
            <el-icon class="main-color-quick template-create">
              <Icon name="icon_template_colorful" />
            </el-icon>
            <span class="name">使用模板新建</span>
          </div>
        </div>
      </div>
    </div>
    <div class="template-market-dashboard">
      <div class="template-market">
        <div class="label">
          模板中心
          <div class="expand-all">
            <button class="all flex-center" @click="toTemplateMarket">查看全部</button>
            <el-divider direction="vertical" />
            <button @click="handleExpandFold" class="expand flex-center">
              {{ t(`visualization.${expandFold}`) }}
            </button>
          </div>
        </div>
        <template v-if="expandFold === 'fold'">
          <div class="tab-btn">
            <div
              v-for="ele in tabBtnList"
              :key="ele.value"
              :class="activeTabBtn === ele.value && 'active'"
              @click="activeTabChange(ele.value)"
              class="main-btn"
            >
              {{ ele.name }}
            </div>
          </div>
          <div class="template-list" v-show="state.networkStatus && state.hasResult">
            <template-branch-item
              v-for="(template, index) in state.marketTemplatePreviewShowList"
              v-show="template['showFlag']"
              :key="index"
              :template="template"
              :base-url="state.baseUrl"
              :create-auth="createAuth"
              @templateApply="templateApply"
              @templatePreview="templatePreview"
            >
            </template-branch-item>
          </div>
          <el-row v-show="state.networkStatus && !state.hasResult" class="template-empty">
            <div style="text-align: center">
              <Icon name="no_result" class="no-result"></Icon>
              <br />
              <span class="no-result-tips">没有找到相关模板</span>
            </div>
          </el-row>
          <el-row v-show="!state.networkStatus" class="template-empty">
            {{ t('visualization.market_network_tips') }}
          </el-row>
        </template>
      </div>
      <shortcut-table :expand="expandFold === 'expand'" />
    </div>
    <de-resource-create-opt-v2 ref="resourceCreateOpt"></de-resource-create-opt-v2>
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
      .user-icon-container {
        width: 48px !important;
        height: 48px !important;
      }
      .ed-icon {
        font-size: 48px;
        padding: 8px;
        border-radius: 50%;
      }

      .info {
        margin: 0 0 24px 12px;
        display: flex;
        align-items: center;
        flex-wrap: wrap;
        width: calc(100% - 60px);
        height: 50px;
        .name-role {
          margin-bottom: 4px;
          color: #1f2329;
          font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
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
          width: 200px;
        }
      }

      .item {
        font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
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
        font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
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
            font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
            font-size: 14px;
            font-style: normal;
            font-weight: 400;
            line-height: 22px;
          }
        }

        .item-quick {
          width: 100%;
          .main-color-quick {
            font-size: 32px;
            margin-right: 12px;
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
          .empty-tooltip-container-template {
            width: 300px;
            position: absolute;
            height: 52px;
            margin-left: -16px;
          }
          .template-create {
            opacity: 0.3;
          }
        }
      }
    }
  }

  .template-market-dashboard {
    width: calc(100% - 376px);
    height: 100%;

    .template-market {
      padding: 24px;
      background: #fff;
      border-radius: 4px;
      .label {
        color: #1f2329;
        font-feature-settings: 'clig' off, 'liga' off;
        font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
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
          font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
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
        overflow-x: auto;
      }
    }
  }
}

.template-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(239, 240, 241, 1);
  color: rgba(100, 106, 115, 1);
  min-height: 60px;
}
.no-result {
  width: 74px;
  height: 74px;
}
.no-result-tips {
  font-size: 14px;
  color: rgba(100, 106, 115, 1);
}
</style>

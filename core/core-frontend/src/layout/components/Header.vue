<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue'
import { usePermissionStore } from '@/store/modules/permission'
import { isExternal } from '@/utils/validate'
import { formatRoute } from '@/router/establish'
import HeaderMenuItem from './HeaderMenuItem.vue'
import { useEmitt } from '@/hooks/web/useEmitt'
import { Icon } from '@/components/icon-custom'
import { ElHeader, ElMenu } from 'element-plus-secondary'
import SystemCfg from './SystemCfg.vue'
import ToolboxCfg from './ToolboxCfg.vue'
import { useRouter, useRoute } from 'vue-router'
import TopDoc from '@/layout/components/TopDoc.vue'
import AccountOperator from '@/layout/components/AccountOperator.vue'
import { isDesktop } from '@/utils/ModelUtil'
import { XpackComponent } from '@/components/plugin'
import { useAppearanceStoreWithOut } from '@/store/modules/appearance'
import AiComponent from '@/layout/components/AiComponent.vue'
import { findBaseParams } from '@/api/aiComponent'
import ExportExcel from '@/views/visualized/data/dataset/ExportExcel.vue'
import AiTips from '@/layout/components/AiTips.vue'
import Copilot from '@/layout/components/Copilot.vue'

const appearanceStore = useAppearanceStoreWithOut()
const { push } = useRouter()
const route = useRoute()
import { useCache } from '@/hooks/web/useCache'
const { wsCache } = useCache('localStorage')
const aiBaseUrl = ref('https://maxkb.fit2cloud.com/ui/chat/2ddd8b594ce09dbb?mode=embed')
const handleIconClick = () => {
  if (route.path === '/workbranch/index') return
  push('/workbranch/index')
}

const handleAiClick = () => {
  useEmitt().emitter.emit('aiComponentChange')
}

const handleCopilotClick = () => {
  push('/copilot/index')
}

const desktop = isDesktop()
const activeIndex = computed(() => {
  if (route.path.includes('system')) {
    return '/system/user'
  }
  return route.path
})

const permissionStore = usePermissionStore()
const ExportExcelRef = ref()
const downloadClick = params => {
  ExportExcelRef.value.init(params)
}
const routers: any[] = formatRoute(permissionStore.getRoutersNotHidden as AppCustomRouteRecordRaw[])
const showSystem = ref(false)
const showToolbox = ref(false)
const showOverlay = ref(false)
const showOverlayCopilot = ref(true)
const handleSelect = (index: string) => {
  // 自定义事件
  if (isExternal(index)) {
    window.open(index, '_blank')
  } else {
    push(index)
  }
}
const initShowSystem = () => {
  showSystem.value = permissionStore.getRouters.some(route => route.path === '/system')
}
const initShowToolbox = () => {
  showToolbox.value = permissionStore.getRouters.some(route => route.path === '/toolbox')
}
const navigateBg = computed(() => appearanceStore.getNavigateBg)
const navigate = computed(() => appearanceStore.getNavigate)

const initAiBase = async () => {
  // const aiTipsCheck = wsCache.get('DE-AI-TIPS-CHECK')
  // if (aiTipsCheck === 'CHECKED') {
  //   showOverlay.value = false
  // } else {
  //   showOverlay.value = true
  // }
  await findBaseParams().then(rsp => {
    const params = rsp.data
    if (params && params['ai.baseUrl']) {
      aiBaseUrl.value = params['ai.baseUrl']
    } else {
      aiBaseUrl.value = null
    }
  })
}

const initCopilotBase = async () => {
  const aiCopilotCheck = wsCache.get('DE-COPILOT-TIPS-CHECK')
  if (aiCopilotCheck === 'CHECKED') {
    showOverlayCopilot.value = false
  } else {
    showOverlayCopilot.value = true
  }
}

const aiTipsConfirm = () => {
  wsCache.set('DE-AI-TIPS-CHECK', 'CHECKED')
  showOverlay.value = false
}

const copilotConfirm = () => {
  wsCache.set('DE-COPILOT-TIPS-CHECK', 'CHECKED')
  showOverlayCopilot.value = false
}

onMounted(() => {
  initShowSystem()
  initShowToolbox()
  initAiBase()
  initCopilotBase()
  useEmitt({
    name: 'data-export-center',
    callback: function (params) {
      ExportExcelRef.value.init(params)
    }
  })
})
</script>

<template>
  <el-header class="header-flex" :class="{ 'header-light': navigateBg && navigateBg === 'light' }">
    <img class="logo" v-if="navigate" :src="navigate" alt="" />
    <Icon
      style="cursor: pointer"
      v-else
      @click="handleIconClick"
      className="logo"
      name="logo"
    ></Icon>
    <el-menu
      :default-active="activeIndex"
      class="el-menu-demo"
      mode="horizontal"
      :ellipsis="false"
      @select="handleSelect"
    >
      <HeaderMenuItem v-for="menu in routers" :key="menu.path" :menu="menu"></HeaderMenuItem>
    </el-menu>
    <div class="operate-setting" v-if="!desktop">
      <XpackComponent jsname="c3dpdGNoZXI=" />
      <el-icon style="margin: 0 10px" class="ai-icon copilot-icon" v-if="!showOverlayCopilot">
        <Icon name="copilot" @click="handleCopilotClick" />
      </el-icon>
      <Copilot @confirm="copilotConfirm" v-if="showOverlayCopilot" class="copilot-icon-tips" />

      <el-icon
        style="margin: 0 10px"
        class="ai-icon"
        v-if="aiBaseUrl && !showOverlay && appearanceStore.getShowAi"
      >
        <Icon name="dv-ai" @click="handleAiClick" />
      </el-icon>
      <el-tooltip effect="dark" content="数据导出中心" placement="bottom">
        <el-icon
          class="preview-download_icon"
          :class="navigateBg === 'light' && 'is-light-setting'"
        >
          <Icon name="dv-preview-download" @click="downloadClick" />
        </el-icon>
      </el-tooltip>

      <ai-tips
        @confirm="aiTipsConfirm"
        v-if="showOverlay && appearanceStore.getShowAi"
        class="ai-icon-tips"
      />
      <ToolboxCfg v-if="showToolbox" />
      <TopDoc v-if="appearanceStore.getShowDoc" />
      <SystemCfg v-if="showSystem" />
      <AccountOperator />
      <ai-component
        v-if="aiBaseUrl && appearanceStore.getShowAi"
        :base-url="aiBaseUrl"
      ></ai-component>
      <div v-if="showOverlay && appearanceStore.getShowAi" class="overlay"></div>
      <div v-if="showOverlayCopilot" class="overlay"></div>
    </div>
  </el-header>
  <ExportExcel ref="ExportExcelRef"></ExportExcel>
</template>

<style lang="less" scoped>
.preview-download_icon {
  padding: 5px;
  height: 28px;
  width: 28px;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  &:hover {
    background-color: #1e2738;
  }
  &.is-light-setting {
    &:hover {
      background-color: var(--ed-menu-hover-bg-color) !important;
    }
  }
}

.overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5); /* 半透明黑色 */
  z-index: 10000;
}

.header-flex {
  margin-bottom: 0.5px;
  display: flex;
  align-items: center;
  height: 56px;
  background-color: #050e21;
  padding: 0 24px;
  .operate-setting {
    margin-left: auto;
    display: flex;
    align-items: center;
    &:focus {
      outline: none;
    }
  }

  .ed-menu {
    background-color: #050e21;
    height: 56px;
  }

  .ed-menu--horizontal {
    border: none;
    .ed-menu-item,
    :deep(.ed-sub-menu__title) {
      color: rgba(255, 255, 255, 0.8);
      line-height: 50px;
      border-bottom: none;

      &.is-active {
        border-bottom: none;
        color: #ffffff !important;
        background-color: var(--ed-color-primary);
      }
    }

    > .is-active {
      :deep(.ed-sub-menu__title) {
        color: #ffffff !important;
        background-color: var(--ed-color-primary);
      }
    }

    .ed-menu-item:not(.is-disabled):hover,
    :deep(.ed-sub-menu__title):not(.is-disabled):hover {
      color: #ffffffcc;
      background: #ffffff1a;
    }
  }
}

.header-light {
  background-color: #ffffff !important;
  box-shadow: 0px 0.5px 0px 0px #1f232926 !important;
  .ed-menu {
    background-color: #ffffff !important;
  }
  .ed-menu--horizontal {
    .ed-menu-item {
      color: var(--ed-color-black) !important;
    }
    :deep(.ed-sub-menu__title) {
      color: var(--ed-color-black) !important;
    }
    .ed-menu-item:not(.is-disabled):hover,
    :deep(.ed-sub-menu__title):not(.is-disabled):hover {
      color: #1f2329;
      background: #1f23291a;
    }
  }

  .logo {
    color: #3371ff !important;
  }
}

.logo {
  width: 134px;
  height: 34px;
  margin-right: 48px;
  color: #ffffff;
}
</style>

<style lang="less">
.header-flex {
  .operate-setting {
    .ed-icon {
      cursor: pointer;
      color: rgba(255, 255, 255, 0.8);
      font-size: 18px;
    }
  }
}
.header-light {
  .operate-setting {
    .ed-icon {
      color: var(--ed-color-black) !important;
    }
  }
}

.ai-icon {
  font-size: 24px !important;
}

.ai-icon-tips,
.copilot-icon-tips {
  font-size: 24px !important;
  z-index: 10001;
}
</style>

<script lang="ts" setup>
import logo from '@/assets/svg/logo.svg'
import copilot from '@/assets/svg/copilot.svg'
import msgNotice from '@/assets/svg/msg-notice.svg'
import dvAi from '@/assets/svg/dv-ai.svg'
import dvPreviewDownload from '@/assets/svg/dv-preview-download.svg'
import { computed, onMounted, ref } from 'vue'
import { usePermissionStore } from '@/store/modules/permission'
import { isExternal } from '@/utils/validate'
import { formatRoute } from '@/router/establish'
import HeaderMenuItem from './HeaderMenuItem.vue'
import { useEmitt } from '@/hooks/web/useEmitt'
import { Icon } from '@/components/icon-custom'
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
import AiTips from '@/layout/components/AiTips.vue'
import CopilotCom from '@/layout/components/Copilot.vue'
import DesktopSetting from './DesktopSetting.vue'

const appearanceStore = useAppearanceStoreWithOut()
const { push } = useRouter()
const route = useRoute()
import { useCache } from '@/hooks/web/useCache'
import { useI18n } from '@/hooks/web/useI18n'
import { msgCountApi } from '@/api/msg'
const { wsCache } = useCache('localStorage')
const aiBaseUrl = ref('https://maxkb.fit2cloud.com/ui/chat/2ddd8b594ce09dbb?mode=embed')
const handleIconClick = () => {
  if (route.path === '/workbranch/index') return
  push('/workbranch/index')
}

const handleAiClick = () => {
  useEmitt().emitter.emit('aiComponentChange')
}
const { t } = useI18n()
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
const downloadClick = params => {
  useEmitt().emitter.emit('data-export-center', params)
}
const routers: any[] = formatRoute(permissionStore.getRoutersNotHidden as AppCustomRouteRecordRaw[])
const showSystem = ref(false)
const showMsg = ref(false)
const showToolbox = ref(false)
const showOverlay = ref(false)
const showOverlayCopilot = ref(false)
const handleSelect = (index: string) => {
  // 自定义事件
  if (isExternal(index)) {
    const openType = wsCache.get('open-backend') === '1' ? '_self' : '_blank'
    window.open(index, openType)
  } else {
    push(index)
  }
}
const initShowSystem = () => {
  showSystem.value = permissionStore.getRouters.some(route => route.path === '/system')
}
const initShowMsg = () => {
  showMsg.value = permissionStore.getRouters.some(route => route.path === '/msg')
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

const aiTipsConfirm = () => {
  wsCache.set('DE-AI-TIPS-CHECK', 'CHECKED')
  showOverlay.value = false
}

const msgNoticePush = () => {
  push('/msg/msg-fill')
}

const copilotConfirm = () => {
  wsCache.set('DE-COPILOT-TIPS-CHECK', 'CHECKED')
  showOverlayCopilot.value = false
}
const badgeCount = ref('0')

onMounted(() => {
  initShowSystem()
  initShowToolbox()
  initShowMsg()
  initAiBase()

  msgCountApi().then(res => {
    badgeCount.value = (res?.data > 99 ? '99+' : res?.data) || '0'
  })
})
</script>

<template>
  <el-header class="header-flex" :class="{ 'header-light': navigateBg && navigateBg === 'light' }">
    <img class="logo" v-if="navigate" :src="navigate" alt="" />
    <Icon v-else @click="handleIconClick" className="logo" name="logo"
      ><logo class="svg-icon logo" style="cursor: pointer"
    /></Icon>
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
      <el-icon
        style="margin: 0 10px"
        class="ai-icon copilot-icon"
        v-if="!showOverlayCopilot && appearanceStore.getShowCopilot"
      >
        <Icon name="copilot"><copilot @click="handleCopilotClick" class="svg-icon" /></Icon>
      </el-icon>
      <CopilotCom
        @confirm="copilotConfirm"
        v-if="showOverlayCopilot && appearanceStore.getShowCopilot"
        class="copilot-icon-tips"
      />

      <el-icon
        style="margin: 0 10px"
        class="ai-icon"
        v-if="aiBaseUrl && !showOverlay && appearanceStore.getShowAi"
      >
        <Icon name="dv-ai"><dvAi @click="handleAiClick" class="svg-icon" /></Icon>
      </el-icon>
      <el-tooltip effect="dark" :content="t('data_export.export_center')" placement="bottom">
        <el-icon
          class="preview-download_icon"
          :class="navigateBg === 'light' && 'is-light-setting'"
        >
          <Icon name="dv-preview-download"
            ><dvPreviewDownload @click="downloadClick" class="svg-icon"
          /></Icon>
        </el-icon>
      </el-tooltip>

      <ai-tips
        @confirm="aiTipsConfirm"
        v-if="showOverlay && appearanceStore.getShowAi"
        class="ai-icon-tips"
      />
      <ToolboxCfg v-if="showToolbox" />
      <TopDoc v-if="appearanceStore.getShowDoc" />
      <el-tooltip
        v-if="showMsg"
        effect="dark"
        :content="$t('v_query.msg_center')"
        placement="bottom"
      >
        <el-badge
          style="margin-right: 10px"
          :hidden="[0, '0'].includes(badgeCount)"
          :value="badgeCount"
          class="ed-badge_custom"
        >
          <el-icon
            class="preview-download_icon"
            :class="navigateBg === 'light' && 'is-light-setting'"
          >
            <Icon name="dv-preview-download"
              ><msgNotice @click="msgNoticePush" class="svg-icon"
            /></Icon>
          </el-icon>
        </el-badge>
      </el-tooltip>

      <SystemCfg v-if="showSystem" />
      <AccountOperator />
      <ai-component
        v-if="aiBaseUrl && appearanceStore.getShowAi"
        :base-url="aiBaseUrl"
      ></ai-component>
      <div v-if="showOverlay && appearanceStore.getShowAi" class="overlay"></div>
      <div v-if="showOverlayCopilot && appearanceStore.getShowCopilot" class="overlay"></div>
    </div>
    <div v-else class="operate-setting">
      <desktop-setting />
    </div>
  </el-header>
</template>

<style lang="less" scoped>
:deep(.ed-badge_custom) {
  --ed-badge-size: 14px;
  .ed-badge__content {
    right: 0;
    padding: 3px;
    border: none;
    font-size: 8px;
    transform: translateX(20%) translateY(-30%);
  }
}
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
        background-color: var(--ed-color-primary) !important;
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

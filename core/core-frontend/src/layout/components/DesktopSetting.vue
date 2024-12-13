<script lang="ts" setup>
import iconSetting from '@/assets/svg/icon-setting.svg'
import copilot from '@/assets/svg/copilot.svg'
import LangSelector from '@/layout/components/LangSelector.vue'
import { useRouter } from 'vue-router'
import TopDesktopCard from './TopDesktopCard.vue'
import icon_right_outlined from '@/assets/svg/icon_right_outlined.svg'
import dvAi from '@/assets/svg/dv-ai.svg'
import AiComponent from '@/layout/components/AiComponent.vue'
import dvPreviewDownload from '@/assets/svg/dv-preview-download.svg'
import ToolboxCfg from './ToolboxCfg.vue'
import { findBaseParams } from '@/api/aiComponent'
import icon_more_outlined from '@/assets/svg/icon_more_outlined.svg'
import { usePermissionStore } from '@/store/modules/permission'
import { useAppearanceStoreWithOut } from '@/store/modules/appearance'
import { msgCountApi } from '@/api/msg'
import { computed, ref, onMounted } from 'vue'
import { useEmitt } from '@/hooks/web/useEmitt'

const aiBaseUrl = ref('https://maxkb.fit2cloud.com/ui/chat/2ddd8b594ce09dbb?mode=embed')
const showToolbox = ref(false)

const badgeCount = ref('0')
const permissionStore = usePermissionStore()
const appearanceStore = useAppearanceStoreWithOut()
const navigateBg = computed(() => appearanceStore.getNavigateBg)
const { push, resolve } = useRouter()
const redirectUser = () => {
  const sysMenu = resolve('/sys-setting')
  const kidPath = sysMenu.matched[0].children[0].path
  push(`${sysMenu.path}/${kidPath}`)
}
const initShowToolbox = () => {
  showToolbox.value = permissionStore.getRouters.some(route => route.path === '/toolbox')
}
const downloadClick = params => {
  useEmitt().emitter.emit('data-export-center', params)
}
const initAiBase = async () => {
  await findBaseParams().then(rsp => {
    const params = rsp.data
    if (params && params['ai.baseUrl']) {
      aiBaseUrl.value = params['ai.baseUrl']
    } else {
      aiBaseUrl.value = null
    }
  })
}

const handleCopilotClick = () => {
  push('/copilot/index')
}

const handleAiClick = () => {
  useEmitt().emitter.emit('aiComponentChange')
}
onMounted(() => {
  initShowToolbox()
  initAiBase()
  msgCountApi().then(res => {
    badgeCount.value = (res?.data > 99 ? '99+' : res?.data) || '0'
  })
})
</script>

<template>
  <ToolboxCfg v-if="showToolbox" />
  <el-tooltip effect="dark" :content="$t('data_export.export_center')" placement="bottom">
    <el-icon
      style="margin-left: 10px"
      class="preview-download_icon"
      :class="navigateBg === 'light' && 'is-light-setting'"
    >
      <Icon><dvPreviewDownload @click="downloadClick" class="svg-icon" /></Icon>
    </el-icon>
  </el-tooltip>
  <el-tooltip
    class="box-item"
    effect="dark"
    :content="$t('commons.system_setting')"
    placement="top"
  >
    <div
      class="sys-setting in-iframe-setting"
      :class="{
        'is-light-setting': navigateBg && navigateBg === 'light'
      }"
    >
      <el-icon @click="redirectUser">
        <Icon><iconSetting class="svg-icon icon-setting" /></Icon>
      </el-icon>
    </div>
  </el-tooltip>

  <el-popover
    popper-class="popper-class_ai-copilot"
    placement="bottom-end"
    :width="224"
    trigger="hover"
  >
    <template #default>
      <div>
        <div class="card-content_desk">
          <TopDesktopCard
            v-if="aiBaseUrl && appearanceStore.getShowAi"
            @openBlank="handleAiClick"
            :cardInfo="{
              icon: dvAi,
              name: $t('commons.assistant')
            }"
          ></TopDesktopCard>
          <TopDesktopCard
            v-if="appearanceStore.getShowCopilot"
            @openBlank="handleCopilotClick"
            :cardInfo="{
              icon: copilot,
              name: 'Copilot'
            }"
          ></TopDesktopCard>
        </div>
        <div class="border-top">
          <el-popover
            :teleported="false"
            popper-class="popper-class_ai-copilot"
            placement="left-start"
            :width="224"
            trigger="click"
            ><template #default>
              <div style="padding: 8px 0">
                <LangSelector />
              </div>
            </template>
            <template #reference>
              <div class="item-select_info">
                {{ $t('commons.language') }}
                <el-icon style="font-size: 16px">
                  <Icon><icon_right_outlined></icon_right_outlined></Icon>
                </el-icon>
              </div> </template
          ></el-popover>
        </div>
      </div>
    </template>
    <template #reference>
      <el-icon class="preview-download_icon" :class="navigateBg === 'light' && 'is-light-setting'">
        <Icon><icon_more_outlined class="svg-icon" /></Icon>
      </el-icon>
    </template>
  </el-popover>
  <ai-component v-if="aiBaseUrl && appearanceStore.getShowAi" :base-url="aiBaseUrl"></ai-component>
</template>

<style lang="less" scoped>
.sys-setting {
  margin: 0 10px 0 0;
  padding: 5px;
  height: 28px;
  width: 28px;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  &:hover {
    background-color: #1e2738;
  }
}
.in-iframe-setting {
  margin-left: 10px !important;
}
.is-light-setting {
  &:hover {
    background-color: #1f23291a !important;
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
</style>
<style lang="less">
.popper-class_ai-copilot {
  padding: 0 !important;

  .card-content_desk {
    display: flex;
    justify-content: space-between;
    padding: 12px 12px 8px;
  }
  .border-top {
    border-top: 1px solid #1f232926;
    padding-top: 4px;
    padding-bottom: 8px;
  }
  .item-select_info {
    cursor: pointer;
    height: 40px;
    padding: 9px 11px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    color: #1f2329;
    .ed-icon {
      color: #8f959e;
    }

    &:hover {
      background: #1f23291a;
    }
  }
}
</style>

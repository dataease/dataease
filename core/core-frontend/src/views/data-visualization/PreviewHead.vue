<script setup lang="ts">
import icon_collection_outlined from '@/assets/svg/icon_collection_outlined.svg'
import visualStar from '@/assets/svg/visual-star.svg'
import dvInfoSvg from '@/assets/svg/dv-info.svg'
import dvHeadMore from '@/assets/svg/dv-head-more.svg'
import icon_pc_fullscreen from '@/assets/svg/icon_pc_fullscreen.svg'
import icon_pc_outlined from '@/assets/svg/icon_pc_outlined.svg'
import icon_download_outlined from '@/assets/svg/icon_download_outlined.svg'
import icon_replace_outlined from '@/assets/svg/icon_replace_outlined.svg'
import icon_edit_outlined from '@/assets/svg/icon_edit_outlined.svg'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { useI18n } from '@/hooks/web/useI18n'
import { useAppStoreWithOut } from '@/store/modules/app'
import DvDetailInfo from '@/views/common/DvDetailInfo.vue'
import { useEmbedded } from '@/store/modules/embedded'
import { storeApi, storeStatusApi } from '@/api/visualization/dataVisualization'
import { ref, watch, computed } from 'vue'
import ShareVisualHead from '@/views/share/share/ShareVisualHead.vue'
import { XpackComponent } from '@/components/plugin'
import { useEmitt } from '@/hooks/web/useEmitt'
import { useShareStoreWithOut } from '@/store/modules/share'
import { exportPermission } from '@/utils/utils'
import { useCache } from '@/hooks/web/useCache'
import { isDesktop } from '@/utils/ModelUtil'

const shareStore = useShareStoreWithOut()
const { wsCache } = useCache('localStorage')
const dvMainStore = dvMainStoreWithOut()
const appStore = useAppStoreWithOut()
const { dvInfo } = storeToRefs(dvMainStore)
const emit = defineEmits(['reload', 'download', 'downloadAsAppTemplate'])
const { t } = useI18n()
const embeddedStore = useEmbedded()
const openType = wsCache.get('open-backend') === '1' ? '_self' : '_blank'
const favorited = ref(false)
const preview = () => {
  const baseUrl = isDataEaseBi.value ? embeddedStore.baseUrl : ''
  const url = baseUrl + '#/preview?dvId=' + dvInfo.value.id + '&ignoreParams=true'
  const newWindow = window.open(url, '_blank')
  initOpenHandler(newWindow)
}
const isDataEaseBi = computed(() => appStore.getIsDataEaseBi)
const isIframe = computed(() => appStore.getIsIframe)
const shareDisable = computed(() => shareStore.getShareDisable || isDesktop())
const exportPermissions = computed(() =>
  exportPermission(dvInfo.value['weight'], dvInfo.value['ext'])
)
const reload = () => {
  emit('reload', dvInfo.value.id)
}

const download = type => {
  emit('download', type)
}
const downloadAsAppTemplate = downloadType => {
  emit('downloadAsAppTemplate', downloadType)
}

const dvEdit = () => {
  if (isDataEaseBi.value || isIframe.value) {
    embeddedStore.clearState()
    if (dvInfo.value.type === 'dataV') {
      embeddedStore.setDvId(dvInfo.value.id)
    } else {
      embeddedStore.setResourceId(dvInfo.value.id)
    }
    useEmitt().emitter.emit(
      'changeCurrentComponent',
      dvInfo.value.type === 'dataV' ? 'VisualizationEditor' : 'DashboardEditor'
    )
    return
  }
  const baseUrl = dvInfo.value.type === 'dataV' ? '#/dvCanvas?dvId=' : '#/dashboard?resourceId='
  const newWindow = window.open(baseUrl + dvInfo.value.id, openType)
  initOpenHandler(newWindow)
}

const executeStore = () => {
  const param = {
    id: dvInfo.value.id,
    type: dvInfo.value.type === 'dashboard' ? 'panel' : 'screen'
  }
  storeApi(param).then(() => {
    storeQuery()
  })
}
const storeQuery = () => {
  if (!dvInfo?.value?.id) return
  storeStatusApi(dvInfo.value.id).then(res => {
    favorited.value = res.data
  })
}
storeQuery()
watch(
  () => dvInfo.value.id,
  () => {
    storeQuery()
  }
)

const openHandler = ref(null)
const initOpenHandler = newWindow => {
  if (openHandler?.value) {
    const pm = {
      methodName: 'initOpenHandler',
      args: newWindow
    }
    openHandler.value.invokeMethod(pm)
  }
}
</script>

<template>
  <div class="preview-head flex-align-center">
    <div :title="dvInfo.name" class="canvas-name ellipsis">{{ dvInfo.name }}</div>
    <div v-show="dvInfo.status === 2" class="canvas-have-update">
      {{ t('visualization.publish_update_tips') }}
    </div>
    <el-tooltip
      effect="dark"
      :content="favorited ? t('visualization.cancel_store') : t('visualization.store')"
      placement="top"
    >
      <el-icon
        v-if="dvInfo.status !== 0"
        class="custom-icon hover-icon"
        @click="executeStore"
        :style="{ color: favorited ? '#FFC60A' : '#646A73' }"
      >
        <icon
          ><component
            class="svg-icon"
            :is="favorited ? visualStar : icon_collection_outlined"
          ></component
        ></icon>
      </el-icon>
    </el-tooltip>
    <el-divider style="margin: 0 16px 0 7px" direction="vertical" />
    <div class="create-area flex-align-center">
      <span style="line-height: 22px"
        >{{ t('visualization.creator') }}:{{ dvInfo.creatorName }}</span
      >
      <el-popover show-arrow :offset="8" placement="bottom" width="400" trigger="hover">
        <template #reference>
          <el-icon class="info-tips"
            ><Icon name="dv-info"><dvInfoSvg class="svg-icon" /></Icon
          ></el-icon>
        </template>
        <dv-detail-info></dv-detail-info>
      </el-popover>
    </div>
    <div class="canvas-opt-button">
      <el-button
        v-if="!isIframe"
        :disabled="dvInfo.status === 0"
        secondary
        @click="() => useEmitt().emitter.emit('canvasFullscreen')"
      >
        <template #icon>
          <icon name="icon_pc_fullscreen"><icon_pc_fullscreen class="svg-icon" /></icon>
        </template>
        {{ t('visualization.fullscreen') }}</el-button
      >
      <el-button secondary @click="preview()" :disabled="dvInfo.status === 0">
        <template #icon>
          <icon name="icon_pc_outlined"><icon_pc_outlined class="svg-icon" /></icon>
        </template>
        {{ t('template_manage.preview') }}
      </el-button>
      <ShareVisualHead
        v-if="!shareDisable"
        :disabled="dvInfo.status === 0"
        :resource-id="dvInfo.id"
        :weight="dvInfo.weight"
        :resource-type="dvInfo.type"
      />
      <el-button class="custom-button" v-if="dvInfo.weight > 6" type="primary" @click="dvEdit()">
        <template #icon>
          <icon name="icon_edit_outlined"><icon_edit_outlined class="svg-icon" /></icon>
        </template>
        {{ t('visualization.edit') }}</el-button
      >
      <el-dropdown :disabled="dvInfo.status === 0" popper-class="pad12" trigger="click">
        <el-icon class="head-more-icon">
          <Icon name="dv-head-more"><dvHeadMore class="svg-icon" /></Icon>
        </el-icon>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="reload()"
              ><el-icon color="#646A73" size="16"><icon_replace_outlined /></el-icon
              >{{ t('visualization.refresh_data') }}
            </el-dropdown-item>
            <el-dropdown
              style="width: 100%; overflow: hidden"
              trigger="hover"
              popper-class="pad12"
              placement="left-start"
              v-if="exportPermissions[0]"
            >
              <div class="ed-dropdown-menu__item flex-align-center icon">
                <el-icon color="#646A73" size="16"><icon_download_outlined /></el-icon>
                {{ t('visualization.export_as') }}
                <el-icon color="#646A73" size="16" class="arrow-right_icon"><ArrowRight /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="download('pdf')">PDF</el-dropdown-item>
                  <el-dropdown-item @click="downloadAsAppTemplate('template')">{{
                    t('visualization.style_template')
                  }}</el-dropdown-item>
                  <el-dropdown-item @click="downloadAsAppTemplate('app')">{{
                    t('visualization.apply_template')
                  }}</el-dropdown-item>
                  <el-dropdown-item @click="download('img')">{{
                    t('chart.image')
                  }}</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
  <XpackComponent ref="openHandler" jsname="L2NvbXBvbmVudC9lbWJlZGRlZC1pZnJhbWUvT3BlbkhhbmRsZXI=" />
</template>

<style lang="less">
.pad12 {
  .ed-dropdown-menu__item {
    padding: 5px 36px 5px 12px !important;

    .ed-icon {
      margin-right: 8px;
    }
    .arrow-right_icon {
      position: absolute;
      right: 12px;
      margin-right: 0;
    }

    &:has(.arrow-right_icon) {
      width: 100%;
    }
  }
}
.preview-head {
  width: 100%;
  min-width: 300px;
  height: 56px;
  padding: 16px 24px;
  border-bottom: 1px solid rgba(31, 35, 41, 0.15);
  .canvas-name {
    max-width: 200px;
    font-size: 16px;
    font-weight: 500;
  }
  .canvas-have-update {
    background-color: rgba(52, 199, 36, 0.2);
    color: rgba(44, 169, 31, 1);
    font-weight: 400;
    font-size: 12px;
    line-height: 20px;
    vertical-align: middle;
    padding: 0 4px;
    margin-left: 8px;
  }
  .custom-icon {
    cursor: pointer;
    margin-left: 8px;
  }
  .create-area {
    color: #646a73;
    font-weight: 400;
    font-size: 14px;
  }
  .canvas-opt-button {
    display: flex;
    justify-content: right;
    align-items: center;
    flex: 1;
    .head-more-icon {
      color: #1f2329;
      margin-left: 12px;
      cursor: pointer;
      font-size: 20px;
      border-radius: 4px;
      position: relative;
      &:hover {
        &::after {
          content: '';
          position: absolute;
          top: -4px;
          left: -4px;
          border-radius: 4px;
          height: 28px;
          width: 28px;
          background: #1f23291a;
        }
      }
    }
  }
}
.info-tips {
  margin-left: 4px;
  font-size: 16px;
  color: #646a73;
}

.custom-button {
  margin-left: 12px;
}
</style>

<script setup lang="ts">
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
import DeFullscreen from '@/components/visualization/common/DeFullscreen.vue'
const dvMainStore = dvMainStoreWithOut()
const appStore = useAppStoreWithOut()
const { dvInfo } = storeToRefs(dvMainStore)
const emit = defineEmits(['reload', 'download', 'downloadAsAppTemplate'])
const { t } = useI18n()
const embeddedStore = useEmbedded()

const favorited = ref(false)
const fullScreeRef = ref(null)
const preview = () => {
  const baseUrl = isDataEaseBi.value ? embeddedStore.baseUrl : ''
  const url = baseUrl + '#/preview?dvId=' + dvInfo.value.id
  const newWindow = window.open(url, '_blank')
  initOpenHandler(newWindow)
}
const isDataEaseBi = computed(() => appStore.getIsDataEaseBi)
const isIframe = computed(() => appStore.getIsIframe)

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
  const newWindow = window.open(baseUrl + dvInfo.value.id, '_blank')
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

    <el-tooltip
      effect="dark"
      :content="favorited ? '取消收藏' : t('visualization.store')"
      placement="top"
    >
      <el-icon
        class="custom-icon hover-icon"
        @click="executeStore"
        :style="{ color: favorited ? '#FFC60A' : '#646A73' }"
      >
        <icon :name="favorited ? 'visual-star' : 'icon_collection_outlined'"></icon>
      </el-icon>
    </el-tooltip>
    <el-divider style="margin: 0 16px 0 7px" direction="vertical" />
    <div class="create-area flex-align-center">
      <span style="line-height: 22px">创建人:{{ dvInfo.creatorName }}</span>
      <el-popover show-arrow :offset="8" placement="bottom" width="400" trigger="hover">
        <template #reference>
          <el-icon class="info-tips"><Icon name="dv-info"></Icon></el-icon>
        </template>
        <dv-detail-info></dv-detail-info>
      </el-popover>
    </div>
    <div class="canvas-opt-button">
      <de-fullscreen ref="fullScreeRef"></de-fullscreen>
      <el-button v-if="!isIframe" secondary @click="() => fullScreeRef.toggleFullscreen()">
        <template #icon>
          <icon name="icon_pc_fullscreen"></icon>
        </template>
        全屏</el-button
      >
      <el-button secondary @click="preview()">
        <template #icon>
          <icon name="icon_pc_outlined"></icon>
        </template>
        预览</el-button
      >
      <ShareVisualHead
        :resource-id="dvInfo.id"
        :weight="dvInfo.weight"
        :resource-type="dvInfo.type"
      />
      <el-button class="custom-button" v-if="dvInfo.weight > 6" type="primary" @click="dvEdit()">
        <template #icon>
          <icon name="icon_edit_outlined"></icon>
        </template>
        编辑</el-button
      >
      <el-dropdown trigger="click">
        <el-icon class="head-more-icon">
          <Icon name="dv-head-more"></Icon>
        </el-icon>
        <template #dropdown>
          <el-dropdown-menu style="width: 130px">
            <el-dropdown-item icon="Refresh" @click="reload()">刷新数据 </el-dropdown-item>
            <el-dropdown
              style="width: 100%"
              trigger="hover"
              placement="left-start"
              v-if="dvInfo.weight > 3"
            >
              <div class="ed-dropdown-menu__item flex-align-center icon">
                <el-icon><Download /></el-icon>
                导出为&nbsp;&nbsp;&nbsp;&nbsp;
                <el-icon><ArrowRight /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item style="width: 118px" @click="download('pdf')"
                    >PDF</el-dropdown-item
                  >
                  <el-dropdown-item style="width: 118px" @click="downloadAsAppTemplate('template')"
                    >模板</el-dropdown-item
                  >
                  <el-dropdown-item style="width: 118px" @click="downloadAsAppTemplate('app')"
                    >应用</el-dropdown-item
                  >
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

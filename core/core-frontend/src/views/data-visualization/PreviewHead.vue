<script setup lang="ts">
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { useI18n } from '@/hooks/web/useI18n'
import DvDetailInfo from '@/views/common/DvDetailInfo.vue'
import { storeApi, storeStatusApi } from '@/api/visualization/dataVisualization'
import { ref, watch } from 'vue'
import { XpackComponent } from '@/components/plugin'
const dvMainStore = dvMainStoreWithOut()
const { dvInfo } = storeToRefs(dvMainStore)
const emit = defineEmits(['reload', 'download'])
const { t } = useI18n()

const favorited = ref(false)
const preview = () => {
  const url = '#/preview?dvId=' + dvInfo.value.id
  window.open(url, '_blank')
}

const reload = () => {
  emit('reload', dvInfo.value.id)
}

const download = () => {
  emit('download')
}

const dvEdit = () => {
  const baseUrl = dvInfo.value.type === 'dataV' ? '#/dvCanvas?dvId=' : '#/dashboard?resourceId='
  window.open(baseUrl + dvInfo.value.id, '_blank')
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
</script>

<template>
  <div class="preview-head">
    <div class="canvas-name">{{ dvInfo.name }}</div>
    <div class="canvas-opt-icon">
      <el-icon class="custom-icon" @click="executeStore">
        <Star v-if="!favorited" />
        <Icon v-else name="visual-star" />
      </el-icon>
    </div>
    <el-divider style="margin-top: 15px" direction="vertical" />
    <div class="create-area">
      <span>创建人：{{ dvInfo.creatorName }}</span>
      <el-popover placement="right-start" width="400" trigger="click">
        <template #reference>
          <div class="info-tips">
            <el-icon><Icon name="dv-info"></Icon></el-icon>
          </div>
        </template>
        <dv-detail-info></dv-detail-info>
      </el-popover>
    </div>
    <div class="canvas-opt-button">
      <!--      <el-button type="primary" @click="download()">导出</el-button>-->
      <el-button icon="DataAnalysis" @click="preview()">预览</el-button>
      <XpackComponent
        jsname="c2hhcmUtaGFuZGxlcg=="
        :resource-id="dvInfo.id"
        :weight="dvInfo.weight"
        :is-button="true"
      />
      <el-button v-if="dvInfo.weight > 6" type="primary" icon="EditPen" @click="dvEdit()"
        >编辑</el-button
      >
      <el-dropdown trigger="click">
        <el-icon style="margin-left: 8px" class="hover-icon">
          <Icon name="dv-head-more"></Icon>
        </el-icon>
        <template #dropdown>
          <el-dropdown-menu style="width: 100px">
            <el-dropdown-item icon="Refresh" @click="reload()">刷新数据</el-dropdown-item>
            <el-dropdown
              style="width: 100%"
              trigger="hover"
              placement="right-start"
              v-if="dvInfo.weight > 3"
            >
              <div style="margin-left: 15px">
                <el-icon><Download /></el-icon>
                导出为
                <el-icon><ArrowRight /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item disabled icon="CopyDocument">{{
                    t('visualization.export_to_panel')
                  }}</el-dropdown-item>
                  <el-dropdown-item disabled icon="Notebook">{{
                    t('visualization.export_to_pdf')
                  }}</el-dropdown-item>
                  <el-dropdown-item icon="Picture" @click="download()">{{
                    t('visualization.export_to_img')
                  }}</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<style lang="less">
.preview-head {
  width: 100%;
  min-width: 300px;
  height: 45px;
  line-height: 45px;
  display: flex;
  padding: 0 24px 0 24px;
  border-bottom: 1px solid #d7d7d7;
  .canvas-name {
    min-width: 100px;
    max-width: 200px;
    font-size: 16px;
    font-weight: 500;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  .canvas-opt-icon {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-left: 8px;
    .custom-icon {
      color: #3a8ee6;
      &:hover {
        background-color: #ecf5ff;
        cursor: pointer;
      }
    }
    .with-light {
      color: red;
    }
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
  }
}
.info-tips {
  float: right;
  margin: 2px 0 0 4px;
}
</style>

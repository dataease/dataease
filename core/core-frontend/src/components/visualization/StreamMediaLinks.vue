<template>
  <el-popover ref="popover" width="400" trigger="click">
    <el-row>
      <el-form ref="form" size="mini" label-width="70px">
        <el-form-item :label="t('visualization.video_type')">
          <el-radio-group v-model="state.streamMediaInfoTemp.videoType">
            <el-radio :label="'flv'">FLV</el-radio>
          </el-radio-group>
          <span style="color: #909399; font-size: 8px; margin-left: 3px">
            Tips:{{ t('visualization.live_tips') }}
          </span>
        </el-form-item>
        <el-row v-if="state.streamMediaInfoTemp.videoType === 'flv'">
          <el-form-item :label="t('visualization.is_live')">
            <el-radio-group
              v-model="state.streamMediaInfoTemp[state.streamMediaInfoTemp.videoType].isLive"
            >
              <el-radio :label="true">{{ t('visualization.yes') }}</el-radio>
              <el-radio :label="false">{{ t('visualization.no') }}</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item
            v-if="!state.streamMediaInfoTemp[state.streamMediaInfoTemp.videoType].isLive"
            :label="t('visualization.play_frequency')"
          >
            <el-radio-group
              v-model="state.streamMediaInfoTemp[state.streamMediaInfoTemp.videoType].loop"
            >
              <el-radio :label="false">{{ t('visualization.play_once') }}</el-radio>
              <el-radio :label="true">{{ t('visualization.play_circle') }}</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item :label="t('visualization.video_links')">
            <el-input
              v-model="state.streamMediaInfoTemp[state.streamMediaInfoTemp.videoType].url"
            />
          </el-form-item>
        </el-row>
        <el-form-item>
          <el-button type="primary" @click="onSubmit">{{ t('visualization.confirm') }}</el-button>
          <el-button @click="onClose">{{ t('visualization.cancel') }}</el-button>
        </el-form-item>
      </el-form>
    </el-row>
    <template #reference>
      <span> ICON<i class="icon iconfont icon-chaolianjie" /> </span>
    </template>
  </el-popover>
</template>

<script setup lang="ts">
import { reactive, ref, toRefs, watch } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { checkAddHttp, deepCopy } from '@/utils/utils'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import eventBus from '@/utils/eventBus'
import { useI18n } from '@/hooks/web/useI18n'
const dvMainStore = dvMainStoreWithOut()
const { curComponent, curActiveTabInner } = storeToRefs(dvMainStore)
const snapshotStore = snapshotStoreWithOut()
const emits = defineEmits(['close'])
const popover = ref(null)
const { t } = useI18n()

const props = defineProps({
  linkInfo: {
    type: Object,
    required: true
  },
  // 属性所属组件位置
  attrPosition: {
    type: String,
    required: false,
    default: 'panel'
  }
})

const { linkInfo, attrPosition } = toRefs(props)

const state = reactive({
  streamMediaInfoTemp: null,
  componentType: null,
  linkageActiveStatus: false,
  editFilter: ['view', 'custom']
})

const init = () => {
  state.streamMediaInfoTemp = deepCopy(linkInfo.value)
}

const onSubmit = () => {
  state.streamMediaInfoTemp[state.streamMediaInfoTemp.videoType].url = checkAddHttp(
    state.streamMediaInfoTemp[state.streamMediaInfoTemp.videoType].url
  )
  if (attrPosition.value === 'panel') {
    curComponent.value.streamMediaLinks = state.streamMediaInfoTemp
  } else {
    curActiveTabInner.value.streamMediaLinks = state.streamMediaInfoTemp
  }
  snapshotStore.recordSnapshotCache()
  eventBus.emit('streamMediaLinksChange-' + curComponent.value.id)
  popoverClose()
}
const onClose = () => {
  emits('close')
  popoverClose()
}
const popoverClose = () => {
  popover.value.showPopper = false
}
init()
watch(
  linkInfo,
  () => {
    init()
  },
  { deep: true }
)
</script>

<style lang="less" scoped>
.slot-class {
  color: white;
}

.bottom {
  margin-top: 20px;
  text-align: center;
}

.ellipsis-area {
  margin-left: 10px;
  margin-right: 10px;
  overflow: hidden; /*超出部分隐藏*/
  white-space: nowrap; /*不换行*/
  text-overflow: ellipsis; /*超出部分文字以...显示*/
  background-color: #f7f8fa;
  color: #3d4d66;
  font-size: 12px;
  line-height: 24px;
  height: 24px;
  border-radius: 3px;
}

.select-filed {
  margin-left: 10px;
  margin-right: 10px;
  overflow: hidden; /*超出部分隐藏*/
  white-space: nowrap; /*不换行*/
  text-overflow: ellipsis; /*超出部分文字以...显示*/
  color: #3d4d66;
  font-size: 12px;
  line-height: 35px;
  height: 35px;
  border-radius: 3px;
}

:deep(.el-popover) {
  height: 200px;
  overflow: auto;
}
</style>

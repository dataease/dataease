<template>
  <el-popover ref="popover" width="400" trigger="click">
    <el-row>
      <el-form ref="form" size="mini" label-width="70px">
        <el-form-item :label="'Tips:'">
          <span style="color: #909399; font-size: 8px; margin-left: 3px">
            {{ t('visualization.web_set_tips') }}
          </span>
        </el-form-item>
        <el-form-item :label="t('visualization.web_url')">
          <el-input v-model="state.linkInfoTemp.src" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSubmit">{{ t('visualization.confirm') }}</el-button>
          <el-button @click="onClose">{{ t('visualization.cancel') }}</el-button>
        </el-form-item>
      </el-form>
    </el-row>
    <template #reference>
      <span>
        ICON
        <i class="icon iconfont icon-chaolianjie" />
      </span>
    </template>
  </el-popover>
</template>

<script setup lang="ts">
import { defineEmits, reactive, ref, toRefs, watch } from 'vue'
import { dvMainStoreWithOut } from '../../store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia/dist/pinia'
import { checkAddHttp, deepCopy } from '../../utils/utils'
import { snapshotStoreWithOut } from '../../store/modules/data-visualization/snapshot'
import eventBus from '../../utils/eventBus'
import { useI18n } from '../../hooks/web/useI18n'
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
  linkInfoTemp: null,
  componentType: null,
  linkageActiveStatus: false,
  editFilter: ['view', 'custom']
})

const init = () => {
  state.linkInfoTemp = deepCopy(linkInfo.value)
}
const onSubmit = () => {
  state.linkInfoTemp.src = checkAddHttp(state.linkInfoTemp.src)
  if (attrPosition.value === 'panel') {
    curComponent.value.frameLinks = state.linkInfoTemp
  } else {
    curActiveTabInner.value.frameLinks = state.linkInfoTemp
  }
  snapshotStore.recordSnapshotCache()
  eventBus.emit('frameLinksChange-' + curComponent.value.id)
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
  /*width: 100%;*/
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
  /*width: 100%;*/
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

:deep(.ed-popover) {
  height: 200px;
  overflow: auto;
}
</style>

<template>
  <el-row>
    <el-form ref="form" size="mini" label-width="70px">
      <el-form-item :label="t('visualization.enable_carousel')">
        <el-switch v-model="state.carouselEnable" size="mini" />
      </el-form-item>
      <el-form-item :label="t('visualization.switch_time')">
        <el-input
          v-model="state.switchTime"
          :disabled="!state.carouselEnable"
          type="number"
          size="mini"
          :min="2"
          :max="3600"
          class="hide-icon-number number-padding"
          @change="switchTimeChange"
        >
          <template v-slot:append>S</template>
        </el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSubmit">{{ t('visualization.confirm') }}</el-button>
        <el-button @click="onClose">{{ t('visualization.cancel') }}</el-button>
      </el-form-item>
    </el-form>
  </el-row>
</template>

<script lang="ts" setup>
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { reactive } from 'vue'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { useI18n } from '@/hooks/web/useI18n'
const { t } = useI18n()
const dvMainStore = dvMainStoreWithOut()
const { curComponent } = storeToRefs(dvMainStore)
const snapshotStore = snapshotStoreWithOut()
const state = reactive({
  carouselEnable: false,
  switchTime: 50
})

const emits = defineEmits(['onClose'])
state.carouselEnable = curComponent.value.style.carouselEnable
state.switchTime = curComponent.value.style.switchTime

const switchTimeChange = () => {
  if (!state.switchTime || state.switchTime < 2) {
    state.switchTime = 2
  } else if (state.switchTime && state.switchTime > 3600) {
    state.switchTime = 3600
  }
}

const onSubmit = () => {
  curComponent.value.style.carouselEnable = state.carouselEnable
  curComponent.value.style.switchTime = state.switchTime
  snapshotStore.recordSnapshotCache()
  onClose()
}

const onClose = () => {
  emits('onClose')
}
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
:deep(.el-popover) {
  height: 200px;
  overflow: auto;
}
.icon-font {
  color: white;
}
</style>

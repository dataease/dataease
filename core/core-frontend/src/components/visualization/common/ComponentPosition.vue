<template>
  <el-form label-position="left" :label-width="14">
    <el-row :gutter="8" v-for="(x, i) in positionKeysGroup" :key="i">
      <el-col :span="12" v-for="({ key, label, min, max, step }, j) in x" :key="j">
        <el-form-item class="form-item" :class="'form-item-' + themes" :label="label">
          <el-input-number
            :effect="themes"
            size="middle"
            :disabled="curComponent['isLock']"
            :min="min"
            :max="max"
            :step="step"
            v-model="curComponent.style[key]"
            @change="onPositionChange"
            controls-position="right"
          />
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { positionData } from '@/utils/attr'
import { storeToRefs } from 'pinia'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import _ from 'lodash'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
const snapshotStore = snapshotStoreWithOut()

const dvMainStore = dvMainStoreWithOut()
const { curComponent } = storeToRefs(dvMainStore)

const props = withDefaults(
  defineProps<{
    themes: EditorTheme
  }>(),
  { themes: 'dark' }
)

const positionKeys = computed(() => {
  if (curComponent.value) {
    const curComponentStyleKeys = Object.keys(curComponent.value.style)
    return positionData.filter(item => curComponentStyleKeys.includes(item.key))
  } else {
    return []
  }
})

const positionKeysGroup = computed(() => {
  const _list = []
  _.forEach(positionKeys.value, (x, i) => {
    const index = i % 2
    if (_list[index] === undefined) {
      _list[index] = []
    }
    _list[index].push(x)
  })
  return _list
})

const onPositionChange = () => {
  snapshotStore.recordSnapshotCache()
}
</script>

<style lang="less" scoped>
:deep(.ed-form-item) {
  display: flex !important;
}
</style>

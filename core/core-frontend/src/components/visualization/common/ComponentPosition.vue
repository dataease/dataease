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
            v-model="positionMounted[key]"
            @change="onPositionChange(key)"
            controls-position="right"
          />
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { positionData } from '@/utils/attr'
import { storeToRefs } from 'pinia'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import _ from 'lodash'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { groupSizeStyleAdaptor } from '@/utils/style'
const snapshotStore = snapshotStoreWithOut()

const dvMainStore = dvMainStoreWithOut()
const { curComponent, canvasStyleData } = storeToRefs(dvMainStore)
const positionMounted = ref({
  width: 0,
  height: 0,
  top: 0,
  left: 0
})

withDefaults(
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

const onPositionChange = key => {
  if (!positionMounted.value[key]) {
    positionMounted.value[key] = 0
  }
  curComponent.value.style[key] = Math.round(
    (positionMounted.value[key] * canvasStyleData.value.scale) / 100
  )

  if (curComponent.value.component === 'Group') {
    //如果当前组件是Group分组 则要进行内部组件深度计算
    const parentNode = document.querySelector('#editor-canvas-main')
    groupSizeStyleAdaptor(curComponent.value)
  }

  snapshotStore.recordSnapshotCache()
}

const positionInit = () => {
  if (curComponent.value) {
    Object.keys(positionMounted.value).forEach(key => {
      positionMounted.value[key] = Math.round(
        (curComponent.value.style[key] * 100) / canvasStyleData.value.scale
      )
    })
  }
}

watch(
  () => curComponent.value,
  () => {
    positionInit()
  },
  {
    deep: true,
    immediate: true
  }
)
</script>

<style lang="less" scoped>
:deep(.ed-form-item) {
  display: flex !important;
  .ed-form-item__label {
    line-height: 24px;
    margin-bottom: 0;
  }
}
</style>

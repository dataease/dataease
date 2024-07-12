<script setup lang="ts">
import { computed, toRefs } from 'vue'
import { ElFormItem } from 'element-plus-secondary'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'

const snapshotStore = snapshotStoreWithOut()

const props = withDefaults(
  defineProps<{
    themes?: EditorTheme
    element: any
  }>(),
  {
    themes: 'dark'
  }
)
const { themes, element } = toRefs(props)

const eventsInfo = computed(() => {
  return element.value.events
})

const onEventChange = () => {
  snapshotStore.recordSnapshotCache('renderChart')
}
</script>

<template>
  <el-row class="custom-row">
    <el-form label-position="top">
      <el-form-item class="form-item" :class="'form-item-' + themes">
        <el-checkbox
          :effect="themes"
          size="small"
          v-model="eventsInfo.checked"
          @change="onEventChange"
          >开启事件绑定</el-checkbox
        >
      </el-form-item>

      <el-form-item class="form-item" :class="'form-item-' + themes" style="margin-bottom: 8px">
        <el-radio-group
          :effect="themes"
          v-model="eventsInfo.type"
          class="radio-span"
          @change="onEventChange"
        >
          <el-radio label="displayChange" :effect="themes"> 开启弹框区 </el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
  </el-row>
</template>

<style scoped lang="less"></style>

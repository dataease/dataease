<script setup lang="ts">
import { computed, toRefs } from 'vue'
import { ElFormItem, ElIcon, ElInputNumber } from 'element-plus-secondary'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import CollapseSwitchItem from '../../components/collapse-switch-item/src/CollapseSwitchItem.vue'
import Icon from '../../components/icon-custom/src/Icon.vue'

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

const carouselInfo = computed(() => {
  return element.value.carousel
})

const onSettingChange = () => {
  snapshotStore.recordSnapshotCache('renderChart')
}
</script>

<template>
  <collapse-switch-item
    :themes="themes"
    v-model="carouselInfo.enable"
    name="carouselInfo"
    title="轮播"
  >
    <el-row class="custom-row">
      <el-form label-position="top">
        <el-form-item
          class="form-item"
          :class="'form-item-' + themes"
          style="width: 50%; margin-bottom: 8px"
        >
          <span style="font-size: 12px">轮播时间</span>
          <el-tooltip class="item" :effect="themes" placement="top">
            <template #content>
              <div>Tab轮播退出编辑模式才开生效</div>
            </template>
            <el-icon class="hint-icon" :class="{ 'hint-icon--dark': themes === 'dark' }">
              <Icon name="icon_info_outlined" />
            </el-icon>
          </el-tooltip>

          <el-input
            v-model="carouselInfo.time"
            :effect="themes"
            type="number"
            :min="1"
            :max="3600"
            :disabled="!carouselInfo.enable"
            @change="onSettingChange"
          >
            <template #append> 秒 </template>
          </el-input>
        </el-form-item>
      </el-form>
    </el-row>
  </collapse-switch-item>
</template>

<style scoped lang="less"></style>

<script setup lang="ts">
import icon_info_outlined from '@/assets/svg/icon_info_outlined.svg'
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
  // 如果输入值小于1，强制设置为1
  if (!carouselInfo.value.time || carouselInfo.value.time < 1) {
    carouselInfo.value.time = 1
  }
  snapshotStore.recordSnapshotCache('renderChart')
}

const handleInput = value => {
  // 使用正则表达式过滤掉小数点和非数字字符，只保留正整数
  const integerValue = String(value).replace(/[^0-9]/g, '')

  carouselInfo.value.time = integerValue ? parseInt(integerValue, 10) : null

  // 如果输入值小于1，强制设置为1
  if (carouselInfo.value.time < 1) {
    carouselInfo.value.time = 1
  }
}
</script>

<template>
  <collapse-switch-item
    :themes="themes"
    v-model="carouselInfo.enable"
    name="carouselInfo"
    @modelChange="onSettingChange"
    title="轮播"
  >
    <el-row class="custom-row">
      <el-form label-position="top" @submit.prevent>
        <el-form-item
          class="form-item"
          :class="'form-item-' + themes"
          style="width: 50%; margin-bottom: 8px"
        >
          <span style="font-size: 12px">轮播时间（秒）</span>
          <el-tooltip class="item" :effect="themes" placement="top">
            <template #content>
              <div>轮播退出编辑模式才开生效</div>
              <div v-if="element.innerType === 'picture-group'">启用条件样式后，轮播失效</div>
            </template>
            <el-icon class="hint-icon" :class="{ 'hint-icon--dark': themes === 'dark' }">
              <Icon name="icon_info_outlined"><icon_info_outlined class="svg-icon" /></Icon>
            </el-icon>
          </el-tooltip>

          <el-input-number
            v-model="carouselInfo.time"
            :effect="themes"
            :min="1"
            :max="3600"
            :step="1"
            :disabled="!carouselInfo.enable"
            controls-position="right"
            @input="handleInput"
            @change="onSettingChange"
          >
          </el-input-number>
        </el-form-item>
      </el-form>
    </el-row>
  </collapse-switch-item>
</template>

<style scoped lang="less"></style>

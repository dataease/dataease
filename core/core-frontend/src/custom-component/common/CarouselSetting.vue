<script setup lang="ts">
import icon_info_outlined from '@/assets/svg/icon_info_outlined.svg'
import { computed, nextTick, toRefs } from 'vue'
import { ElFormItem, ElIcon, ElInputNumber } from 'element-plus-secondary'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import CollapseSwitchItem from '../../components/collapse-switch-item/src/CollapseSwitchItem.vue'
import Icon from '../../components/icon-custom/src/Icon.vue'
import { useEmitt } from '@/hooks/web/useEmitt'
import { useI18n } from '@/hooks/web/useI18n'
const { t } = useI18n()

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
  if (carouselInfo.value.enable) {
    useEmitt().emitter.emit('carouselValueChange')
  }

  nextTick(() => {
    useEmitt().emitter.emit('calcData-' + element.value.id)
  })
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
    :title="t('visualization.carousel')"
  >
    <el-row class="custom-row" style="margin-top: -8px">
      <el-form label-position="top" @submit.prevent>
        <el-form-item
          class="form-item"
          :class="'form-item-' + themes"
          style="width: 50%; margin-bottom: 0"
        >
          <span style="font-size: 12px">{{ t('visualization.carousel_time') }}</span>
          <el-tooltip class="item" :effect="themes" placement="top">
            <template #content>
              <div v-if="element.innerType === 'picture-group'">
                {{ t('visualization.carousel_tips2') }}
              </div>
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
            @blur="onSettingChange"
            @change="onSettingChange"
          >
          </el-input-number>
        </el-form-item>
      </el-form>
    </el-row>
  </collapse-switch-item>
</template>

<style scoped lang="less"></style>

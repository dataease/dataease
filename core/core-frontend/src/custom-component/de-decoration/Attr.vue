<template>
  <div class="attr-list de-collapse-style">
    <CommonAttr :element="curComponent">
      <el-collapse-item
        effect="dark"
        :title="t('visualization.style')"
        name="decoration_style"
        v-if="curComponent && !mobileInPc"
      >
        <div style="display: flex">
          <el-tooltip effect="dark" placement="bottom">
            <template #content> {{ t('visualization.color_setting', [1]) }} </template>
            <el-form-item
              effect="dark"
              style="margin-left: 12px"
              class="form-item no-margin-bottom"
              :class="'form-item-dark'"
            >
              <el-color-picker
                :title="t('visualization.color_setting', [1])"
                v-model="state.style.color0"
                class="color-picker-style"
                :prefix-icon="dvStyleColor"
                :triggerWidth="80"
                is-custom
                show-alpha
                :predefine="state.predefineColors"
                @change="onStyleChange('color0')"
              >
              </el-color-picker>
            </el-form-item>
          </el-tooltip>

          <el-tooltip effect="dark" placement="bottom">
            <template #content> {{ t('visualization.color_setting', [2]) }} </template>
            <el-form-item
              effect="dark"
              style="margin-left: 12px"
              class="form-item no-margin-bottom"
              :class="'form-item-dark'"
            >
              <el-color-picker
                :title="t('visualization.color_setting', [2])"
                v-model="curComponent.style.color1"
                class="color-picker-style"
                :prefix-icon="dvStyleColor"
                :triggerWidth="80"
                is-custom
                show-alpha
                :predefine="state.predefineColors"
                @change="onStyleChange('color0')"
              >
              </el-color-picker>
            </el-form-item>
          </el-tooltip>
        </div>
      </el-collapse-item>
    </CommonAttr>
  </div>
</template>

<script setup lang="ts">
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import CommonAttr from '@/custom-component/common/CommonAttr.vue'
import { storeToRefs } from 'pinia'
import { useI18n } from '@/hooks/web/useI18n'
import { onMounted, reactive, watch } from 'vue'
import { COLOR_PANEL } from '@/views/chart/components/editor/util/chart'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
const { t } = useI18n()
const dvMainStore = dvMainStoreWithOut()
const { curComponent, mobileInPc } = storeToRefs(dvMainStore)
import dvStyleColor from '@/assets/svg/dv-style-color.svg'
const state = reactive({
  style: {
    color0: null,
    color1: null,
    color2: null,
    dur: 6,
    reverse: false
  },
  predefineColors: COLOR_PANEL
})

const snapshotStore = snapshotStoreWithOut()

const onStyleChange = key => {
  curComponent.value.style[key] = state.style[key]
  snapshotStore.recordSnapshotCache('decoration')
}

watch(
  [() => curComponent.value],
  () => {
    init()
  },
  {
    deep: true
  }
)

onMounted(() => {
  init()
})

const init = () => {
  setTimeout(() => {
    state.style.color0 = curComponent.value.style.color0
    state.style.color1 = curComponent.value.style.color1
    state.style.color2 = curComponent.value.style.color2
  })
}
</script>

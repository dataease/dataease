<script setup lang="ts">
import { computed, reactive, ref, toRefs, watch } from 'vue'
import { ElFormItem, ElInputNumber } from 'element-plus-secondary'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { COLOR_PANEL } from '@/views/chart/components/editor/util/chart'
const dvMainStore = dvMainStoreWithOut()

const snapshotStore = snapshotStoreWithOut()
const { canvasStyleData } = storeToRefs(dvMainStore)

const props = withDefaults(
  defineProps<{
    themes?: EditorTheme
    element: any
  }>(),
  {
    themes: 'dark'
  }
)
const emits = defineEmits(['onStyleAttrChange'])
const { themes, element } = toRefs(props)
const state = reactive({
  fontSize: [],
  isSetting: false,
  predefineColors: COLOR_PANEL
})
const styleMounted = ref({
  borderWidth: 0,
  borderRadius: 5
})

const borderStyleList = [
  { name: '实线', value: 'solid' },
  { name: '虚线', value: 'dashed' },
  { name: '点线', value: 'dotted' }
]

const styleInit = () => {
  if (element.value) {
    Object.keys(styleMounted.value).forEach(key => {
      styleMounted.value[key] = Math.round(
        (element.value.style[key] * 100) / canvasStyleData.value.scale
      )
    })
  }
}

const styleForm = computed<any>(() => element.value.style)

const changeStyle = params => {
  snapshotStore.recordSnapshotCache()
  emits('onStyleAttrChange', params)
}

const changeStylePre = key => {
  changeStyle({ key: key, value: element.value.style[key] })
}

const sizeChange = key => {
  element.value.style[key] = Math.round(
    (styleMounted.value[key] * canvasStyleData.value.scale) / 100
  )
  changeStyle({ key: key, value: element.value.style[key] })
}

watch(
  () => element.value,
  () => {
    styleInit()
  },
  {
    deep: true,
    immediate: true
  }
)
</script>

<template>
  <el-row class="custom-row">
    <el-form label-position="top">
      <el-form-item label="颜色" class="form-item" :class="'form-item-' + themes">
        <el-color-picker
          title="颜色"
          v-model="styleForm.borderColor"
          class="color-picker-style"
          :triggerWidth="50"
          is-custom
          :predefine="state.predefineColors"
          @change="changeStylePre('borderColor')"
        >
        </el-color-picker>
      </el-form-item>
      <el-form-item label="圆角" class="form-item" :class="'form-item-' + themes">
        <el-input-number
          title="圆角"
          v-model="styleForm.borderRadio"
          class="color-picker-style"
          @change="sizeChange('borderWidth')"
        >
        </el-input-number>
      </el-form-item>
      <el-form-item label="样式" class="form-item" :class="'form-item-' + themes">
        <el-select
          :effect="themes"
          v-model="styleForm.borderStyle"
          size="small"
          @change="changeStylePre('borderStyle')"
        >
          <el-option
            class="custom-style-option"
            v-for="option in borderStyleList"
            :key="option.value"
            :label="option.name"
            :value="option.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="线宽" class="form-item" :class="'form-item-' + themes">
        <el-input-number
          title="线宽"
          v-model="styleForm.borderWidth"
          class="color-picker-style"
          @change="sizeChange('borderWidth')"
        >
        </el-input-number>
      </el-form-item>
    </el-form>
  </el-row>
</template>

<style scoped lang="less"></style>

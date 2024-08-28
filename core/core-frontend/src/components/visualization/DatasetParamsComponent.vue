<script lang="ts" setup>
import { computed, reactive, ref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { deepCopy } from '@/utils/utils'
import { useEmitt } from '@/hooks/web/useEmitt'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { ElMessage } from 'element-plus-secondary'
const { t } = useI18n()
const loading = ref(false)
const subject = ref()
const subjectDialogShow = ref(false)
const dvMainStore = dvMainStoreWithOut()
const { canvasViewInfo } = storeToRefs(dvMainStore)

const state = reactive({
  viewId: null
})
const curDataSetParamsInfo = ref([])

const resetForm = () => {
  subject.value.clearValidate()
  subjectDialogShow.value = false
}
const subjectForm = ref(null)
const rules = ref({})

const optInit = item => {
  if (item) {
    state.viewId = item.id
    const chartInfo = canvasViewInfo.value[state.viewId]
    curDataSetParamsInfo.value = deepCopy(chartInfo.calParams)
    subjectDialogShow.value = true
  }
}

const saveSubject = () => {
  if (disabledCheck.value) {
    ElMessage.error('请输入正确参数')
    return
  }
  canvasViewInfo.value[state.viewId]['calParams'] = curDataSetParamsInfo.value
  useEmitt().emitter.emit('calcData-' + state.viewId, canvasViewInfo.value[state.viewId])
  resetForm()
}

const disabledCheck = computed(() => {
  return (
    !curDataSetParamsInfo.value ||
    (curDataSetParamsInfo.value &&
      curDataSetParamsInfo.value.filter(item => item.value === null || item.value === undefined)
        .length > 0)
  )
})

const statesCheck = () => {
  return subjectDialogShow.value
}

const handleDialogClick = e => {
  e.preventDefault()
  e.stopPropagation()
}

defineExpose({
  optInit,
  statesCheck,
  resetForm
})

const emits = defineEmits(['finish'])
</script>

<template>
  <el-dialog
    v-loading="loading"
    title="计算参数输入"
    v-model="subjectDialogShow"
    width="400px"
    :before-close="resetForm"
    @click="handleDialogClick"
  >
    <el-form
      v-if="subjectDialogShow"
      label-position="top"
      ref="subject"
      :model="subjectForm"
      :rules="rules"
      @submit.prevent
    >
      <el-form-item
        v-for="paramsItem in curDataSetParamsInfo"
        :key="paramsItem"
        class="form-item"
        :prop="'value_' + paramsItem.name"
      >
        <template #label>
          <label class="m-label">
            计算字段[{{ paramsItem.name }}] <span style="color: red">*</span>
          </label>
        </template>
        <el-input-number
          style="width: 100%"
          v-model="paramsItem.value"
          placeholder="请输入一个数字"
          controls-position="right"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button secondary @click="resetForm()">取消</el-button>
      <el-button type="primary" @click="saveSubject()">确认</el-button>
    </template>
  </el-dialog>
</template>

<style lang="less" scoped>
:deep(.ed-dialog__header) {
  text-align: left;
}
.m-label {
  color: #1f2329;
  font-size: 14px;
  font-style: normal;
  font-weight: 400;
  line-height: 14px;
  display: inline-block;
}
.form-item {
  margin-bottom: 16px;
  :deep(.ed-form-item__label) {
    line-height: 14px !important;
  }

  :deep(.ed-input__inner) {
    font-size: 14px !important;
  }

  :deep(.ed-form-item__error) {
    top: 88%;
  }
  .ed-input {
    --ed-input-height: 32px !important;
  }

  &:last-child {
    margin-bottom: 0;
  }

  :deep(.avatar-uploader-container) {
    margin-bottom: 0px;
  }
}
</style>

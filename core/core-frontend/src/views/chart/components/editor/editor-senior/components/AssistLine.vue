<script lang="tsx" setup>
import { reactive, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { ElMessage } from 'element-plus-secondary'
import AssistLineEdit from '@/views/chart/components/editor/editor-senior/components/dialog/AssistLineEdit.vue'

const { t } = useI18n()

const emit = defineEmits(['onAssistLineChange'])

const props = defineProps({
  chart: {
    type: Object,
    required: true
  },
  quotaData: {
    type: Array,
    required: true
  },
  themes: {
    type: String,
    default: 'dark'
  }
})

const state = reactive({
  assistLine: [],
  editLineDialog: false,
  lineArr: [],
  quotaFields: []
})

watch(
  () => props.chart,
  () => {
    init()
  }
)

const changeAssistLine = () => {
  emit('onAssistLineChange', state.assistLine)
}
const lineChange = val => {
  state.lineArr = val
}

const editLine = () => {
  state.editLineDialog = true
}
const closeEditLine = () => {
  state.editLineDialog = false
}

const changeLine = () => {
  // check line config
  for (let i = 0; i < state.lineArr.length; i++) {
    const ele = state.lineArr[i]
    if (!ele.name || ele.name === '') {
      ElMessage.error(t('chart.name_can_not_empty'))
      return
    }
    if (ele.field === '0') {
      if (!ele.value) {
        ElMessage.error(t('chart.value_can_not_empty'))
        return
      }
      if (parseFloat(ele.value).toString() === 'NaN') {
        ElMessage.error(t('chart.value_error'))
        return
      }
    } else {
      if (!ele.fieldId || ele.fieldId === '') {
        ElMessage.error(t('chart.field_not_empty'))
        return
      }
      if (!ele.summary || ele.summary === '') {
        ElMessage.error(t('chart.summary_not_empty'))
        return
      }
    }
  }
  state.assistLine = JSON.parse(JSON.stringify(state.lineArr))
  changeAssistLine()
  closeEditLine()
}

const init = () => {
  const chart = JSON.parse(JSON.stringify(props.chart))
  if (chart.senior) {
    let senior = null
    if (Object.prototype.toString.call(chart.senior) === '[object Object]') {
      senior = JSON.parse(JSON.stringify(chart.senior))
    } else {
      senior = JSON.parse(chart.senior)
    }
    if (senior.assistLine) {
      for (let i = 0; i < senior.assistLine.length; i++) {
        if (!senior.assistLine[i].fontSize) {
          senior.assistLine[i].fontSize = '10'
        }
      }
      state.assistLine = senior.assistLine
    } else {
      state.assistLine = []
    }
    state.lineArr = JSON.parse(JSON.stringify(state.assistLine))
  }
}

init()
</script>

<template>
  <div style="width: 100%">
    <el-col>
      <el-button
        class="circle-button"
        :title="t('chart.edit')"
        type="text"
        size="small"
        :style="{ width: '24px', marginLeft: '4px' }"
        @click="editLine"
      >
        <template #icon>
          <Icon name="icon_edit_outlined"></Icon>
        </template>
      </el-button>
      <el-col>
        <el-row v-for="(item, index) in state.assistLine" :key="index" class="line-style">
          <el-col :span="8">
            <span :title="item.name">{{ item.name }}</span>
          </el-col>
          <el-col :span="8">
            <span v-if="item.field === '0'" :title="t('chart.field_fixed')">{{
              t('chart.field_fixed')
            }}</span>
            <span v-if="item.field === '1'" :title="t('chart.field_dynamic')">{{
              t('chart.field_dynamic')
            }}</span>
          </el-col>
          <el-col v-if="item.field === '0'" :span="8">
            <span :title="item.value">{{ item.value }}</span>
          </el-col>
          <el-col v-if="item.field === '1'" :span="8">
            <span :title="item.curField.name + '(' + t('chart.' + item.summary) + ')'">{{
              item.curField.name + '(' + t('chart.' + item.summary) + ')'
            }}</span>
          </el-col>
        </el-row>
      </el-col>
    </el-col>

    <!--编辑辅助线-->
    <el-dialog
      v-if="state.editLineDialog"
      v-model="state.editLineDialog"
      :title="t('chart.assist_line')"
      :visible="state.editLineDialog"
      :show-close="false"
      width="1000px"
      class="dialog-css"
    >
      <assist-line-edit
        :line="state.assistLine"
        :quota-fields="props.quotaData"
        @onAssistLineChange="lineChange"
      />
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeEditLine">{{ t('chart.cancel') }}</el-button>
          <el-button type="primary" @click="changeLine">{{ t('chart.confirm') }}</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="less" scoped>
.shape-item {
  padding: 6px;
  border: none;
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.form-item-slider :deep(.ed-form-item__label) {
  font-size: 12px;
  line-height: 38px;
}
.form-item :deep(.ed-form-item__label) {
  font-size: 12px;
}
.ed-select-dropdown__item {
  padding: 0 20px;
}
span {
  font-size: 12px;
}
.ed-form-item {
  margin-bottom: 6px;
}

.switch-style {
  position: absolute;
  right: 10px;
  margin-top: -4px;
}
.color-picker-style {
  cursor: pointer;
  z-index: 1003;
}

.line-style {
}
.line-style :deep(span) {
  display: inline-block;
  width: 100%;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
  padding: 0 10px;
}

.dialog-css :deep(.ed-dialog__title) {
  font-size: 14px;
}

.dialog-css :deep(.ed-dialog__header) {
  padding: 20px 20px 0;
}

.dialog-css :deep(.ed-dialog__body) {
  padding: 10px 20px 20px;
}
</style>

<script lang="tsx" setup>
import { onMounted, reactive, watch, computed, PropType } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { ElIcon, ElMessage } from 'element-plus-secondary'
import AssistLineEdit from '@/views/chart/components/editor/editor-senior/components/dialog/AssistLineEdit.vue'
import { defaultsDeep, find } from 'lodash-es'
import { DEFAULT_ASSIST_LINE_CFG } from '../../util/chart'

const { t } = useI18n()

const emit = defineEmits(['onAssistLineChange'])

const props = defineProps({
  chart: {
    type: Object as PropType<ChartObj>,
    required: true
  },
  quotaData: {
    type: Array,
    required: true
  },
  quotaExtData: {
    type: Array,
    required: true
  },
  themes: {
    type: String as PropType<EditorTheme>,
    default: 'dark'
  },
  propertyInner: {
    type: Array<string>
  }
})

const quotaFields = computed<Array<any>>(() => {
  return props.quotaData.filter(ele => ele.summary !== '' && ele.id !== '-1')
})

const quotaExtFields = computed<Array<any>>(() => {
  return props.quotaExtData.filter(ele => ele.summary !== '' && ele.id !== '-1')
})

const useQuotaExt = computed<boolean>(() => {
  return props.chart.type.includes('chart-mix')
})

const state = reactive({
  assistLineCfg: {
    enable: false,
    assistLine: []
  },
  editLineDialog: false,
  lineArr: [],
  quotaFields: []
})

watch(
  () => props.chart.senior.assistLineCfg,
  () => {
    init()
  },
  { deep: true }
)

const changeAssistLine = () => {
  const flag = state.assistLineCfg.assistLine.findIndex(ele => ele.field === '1') !== -1
  emit('onAssistLineChange', { data: state.assistLineCfg, requestData: flag })
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
      if (ele.value === null || ele.value === undefined || ele.value === '') {
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
  state.assistLineCfg.assistLine = JSON.parse(JSON.stringify(state.lineArr))
  changeAssistLine()
  closeEditLine()
}

function existField(line) {
  if (useQuotaExt.value) {
    return (
      !!find(quotaFields.value, d => d.id === line.id) ||
      !!find(quotaExtFields.value, d => d.id === line.id)
    )
  } else {
    return !!find(quotaFields.value, d => d.id === line.id)
  }
}

const init = () => {
  const chart = JSON.parse(JSON.stringify(props.chart))
  if (chart.senior) {
    const senior = chart.senior
    if (senior.assistLineCfg?.assistLine) {
      const assistLine = senior.assistLineCfg.assistLine
      for (let i = 0; i < assistLine.length; i++) {
        if (!assistLine[i].fontSize) {
          assistLine[i].fontSize = 10
        }
      }
      state.assistLineCfg = defaultsDeep(senior.assistLineCfg, DEFAULT_ASSIST_LINE_CFG)
    }
    state.lineArr = JSON.parse(JSON.stringify(state.assistLineCfg.assistLine))
  }
}

onMounted(() => {
  init()
})
</script>

<template>
  <div @keydown.stop @keyup.stop class="assist-line-container">
    <div class="inner-container">
      <span class="label" :class="'label-' + props.themes">辅助线设置</span>
      <span class="right-btns">
        <span
          class="set-text-info"
          :class="{ 'set-text-info-dark': themes === 'dark' }"
          v-if="state.assistLineCfg.assistLine.length > 0"
        >
          已设置
        </span>
        <el-button
          :class="'label-' + props.themes"
          :style="{ width: '24px', marginLeft: '6px' }"
          :disabled="!state.assistLineCfg.enable"
          class="circle-button font14"
          text
          size="small"
          @click="editLine"
        >
          <template #icon>
            <el-icon size="14px">
              <Icon name="icon_edit_outlined" />
            </el-icon>
          </template>
        </el-button>
      </span>
    </div>

    <el-row v-for="(item, index) in state.assistLineCfg.assistLine" :key="index" class="line-style">
      <el-col :span="8">
        <span :title="item.name">{{ item.name }}</span>
      </el-col>
      <el-col :span="6">
        <span v-if="item.field === '0'" :title="t('chart.field_fixed')">{{
          t('chart.field_fixed')
        }}</span>
        <span v-if="item.field === '1'" :title="t('chart.field_dynamic')">{{
          t('chart.field_dynamic')
        }}</span>
      </el-col>
      <el-col v-if="item.field === '0'" :span="10">
        <span :title="item.value">{{ item.value }}</span>
      </el-col>
      <el-col v-else-if="item.field === '1'" :span="10">
        <template v-if="existField(item.curField)">
          <span :title="item.curField.name + '(' + t('chart.' + item.summary) + ')'">
            {{ item.curField.name + '(' + t('chart.' + item.summary) + ')' }}
          </span>
        </template>
        <template v-else>
          <span style="color: red">无效字段</span>
        </template>
      </el-col>
    </el-row>

    <!--编辑辅助线-->
    <el-dialog
      v-if="state.editLineDialog"
      v-model="state.editLineDialog"
      :visible="state.editLineDialog"
      width="1000px"
      class="dialog-css"
    >
      <assist-line-edit
        :line="state.assistLineCfg.assistLine"
        :quota-fields="quotaFields"
        :quota-ext-fields="quotaExtFields"
        :use-quota-ext="useQuotaExt"
        @onAssistLineChange="lineChange"
      />
      <template #header>
        <div class="assist-line-cfg-header">
          <span class="ed-dialog__title">{{ t('chart.assist_line') }}</span>
          <el-tooltip class="item" effect="dark" placement="top">
            <template #content>
              <span> {{ t('chart.assist_line_tip') }}</span>
            </template>
            <el-icon class="hint-icon" :class="{ 'hint-icon--dark': themes === 'dark' }">
              <Icon name="icon_info_outlined" />
            </el-icon>
          </el-tooltip>
        </div>
      </template>
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
.assist-line-container {
  width: 100%;
  margin-bottom: 16px;

  .inner-container {
    display: flex;
    align-items: center;
    flex-direction: row;
    justify-content: space-between;

    .label {
      cursor: default;
      color: #646a73;
      font-size: 12px;
      font-style: normal;
      font-weight: 400;
      line-height: 20px;
    }

    .right-btns {
      display: flex;
      align-items: center;
      flex-direction: row;
    }

    .set-text-info {
      cursor: default;
      padding: 1.5px 4px;
      border-radius: 2px;
      background: rgba(31, 35, 41, 0.1);

      color: #646a73;

      font-size: 10px;
      font-style: normal;
      font-weight: 500;
      line-height: 13px;

      &.set-text-info-dark {
        color: #a6a6a6;
        background: rgba(235, 235, 235, 0.1);
      }
    }
  }
  .assist-line-cfg-header {
    display: flex;
    flex-direction: row;
    justify-content: flex-start;
    align-items: center;
    .ed-dialog__title {
      margin-right: 4px;
      font-size: 16px;
    }
  }
}

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
  width: 100%;
  font-weight: 400;
  padding: 4px 8px;
  display: flex;
  flex-direction: row;
  align-items: center;
  flex-wrap: nowrap;

  &:deep(span) {
    white-space: nowrap;
    text-overflow: ellipsis;
    overflow: hidden;
    cursor: default;
  }
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

.label-dark {
  font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
  font-style: normal;
  font-weight: 400;
  line-height: 20px;
  color: #a6a6a6 !important;
  &.ed-button {
    color: var(--ed-color-primary) !important;
  }
  &.is-disabled {
    color: #5f5f5f !important;
  }
}

.font14 {
  :deep(.ed-icon) {
    font-size: 14px;
  }
}
</style>

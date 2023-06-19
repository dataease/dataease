<script lang="tsx" setup>
import { reactive, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { ElMessage } from 'element-plus-secondary'
import { DEFAULT_THRESHOLD } from '@/views/chart/components/editor/util/chart'
import TableThresholdEdit from '@/views/chart/components/editor/editor-senior/components/dialog/TableThresholdEdit.vue'
import TextLabelThresholdEdit from '@/views/chart/components/editor/editor-senior/components/dialog/TextLabelThresholdEdit.vue'
import TextThresholdEdit from '@/views/chart/components/editor/editor-senior/components/dialog/TextThresholdEdit.vue'

const { t } = useI18n()

const fieldType = (deType: number) => {
  return ['text', 'time', 'value', 'value', 'location'][deType]
}

const props = defineProps({
  chart: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['onThresholdChange'])

watch(
  () => props.chart,
  () => {
    init()
  }
)

const state = reactive({
  thresholdForm: JSON.parse(JSON.stringify(DEFAULT_THRESHOLD)),
  editTextLabelThresholdDialog: false,
  textThresholdArr: [],
  editLabelThresholdDialog: false,
  thresholdArr: [],
  editTableThresholdDialog: false,
  tableThresholdArr: []
})

const init = () => {
  const chart = JSON.parse(JSON.stringify(props.chart))
  if (chart.senior) {
    let senior = null
    if (Object.prototype.toString.call(chart.senior) === '[object Object]') {
      senior = JSON.parse(JSON.stringify(chart.senior))
    } else {
      senior = JSON.parse(chart.senior)
    }
    if (senior.threshold) {
      state.thresholdForm = senior.threshold
    }
    state.textThresholdArr = JSON.parse(JSON.stringify(state.thresholdForm.textLabelThreshold))
    state.thresholdArr = JSON.parse(JSON.stringify(state.thresholdForm.labelThreshold))
    state.tableThresholdArr = JSON.parse(JSON.stringify(state.thresholdForm.tableThreshold))
  }
}
const changeThreshold = () => {
  emit('onThresholdChange', state.thresholdForm)
}
const gaugeThresholdChange = () => {
  // check input
  if (state.thresholdForm.gaugeThreshold) {
    const arr = state.thresholdForm.gaugeThreshold.split(',')
    for (let i = 0; i < arr.length; i++) {
      const ele = arr[i]
      if (parseFloat(ele).toString() === 'NaN' || parseFloat(ele) <= 0 || parseFloat(ele) >= 100) {
        ElMessage.error(t('chart.gauge_threshold_format_error'))
        return
      }
      if (i > 0) {
        if (parseFloat(ele) <= parseFloat(arr[i - 1])) {
          ElMessage.error(t('chart.gauge_threshold_compare_error'))
          return
        }
      }
    }
  }
  changeThreshold()
}
const editLabelThreshold = () => {
  state.editLabelThresholdDialog = true
}
const closeLabelThreshold = () => {
  state.editLabelThresholdDialog = false
}
const changeLabelThreshold = () => {
  // check line config
  for (let i = 0; i < state.thresholdArr.length; i++) {
    const ele = state.thresholdArr[i]
    if (!ele.term || ele.term === '') {
      ElMessage.error(t('chart.exp_can_not_empty'))
      return
    }
    if (ele.term === 'between') {
      if (!ele.min || !ele.max) {
        ElMessage.error(t('chart.value_can_not_empty'))
        return
      }
      if (parseFloat(ele.min).toString() === 'NaN' || parseFloat(ele.max).toString() === 'NaN') {
        ElMessage.error(t('chart.value_error'))
        return
      }
    } else {
      if (!ele.value) {
        ElMessage.error(t('chart.value_can_not_empty'))
        return
      }
      if (parseFloat(ele.value).toString() === 'NaN') {
        ElMessage.error(t('chart.value_error'))
        return
      }
    }
  }
  state.thresholdForm.labelThreshold = JSON.parse(JSON.stringify(state.thresholdArr))
  changeThreshold()
  closeLabelThreshold()
}
const thresholdChange = val => {
  state.thresholdArr = val
}

const editTextLabelThreshold = () => {
  state.editTextLabelThresholdDialog = true
}
const closeTextLabelThreshold = () => {
  state.editTextLabelThresholdDialog = false
}
const changeTextLabelThreshold = () => {
  // check line config
  for (let i = 0; i < state.textThresholdArr.length; i++) {
    const ele = state.textThresholdArr[i]
    if (!ele.term || ele.term === '') {
      ElMessage.error(t('chart.exp_can_not_empty'))
      return
    }
    if (!ele.value) {
      ElMessage.error(t('chart.value_can_not_empty'))
      return
    }
  }
  state.thresholdForm.textLabelThreshold = JSON.parse(JSON.stringify(state.textThresholdArr))
  changeThreshold()
  closeTextLabelThreshold()
}
const thresholdTextChange = val => {
  state.textThresholdArr = val
}

const tableThresholdChange = val => {
  state.tableThresholdArr = val
}
const editTableThreshold = () => {
  state.editTableThresholdDialog = true
}
const closeTableThreshold = () => {
  state.editTableThresholdDialog = false
}
const changeTableThreshold = () => {
  // check line config
  for (let i = 0; i < state.tableThresholdArr.length; i++) {
    const field = state.tableThresholdArr[i]
    if (!field.fieldId) {
      ElMessage.error(t('chart.field_can_not_empty'))
      return
    }
    if (!field.conditions || field.conditions.length === 0) {
      ElMessage.error(t('chart.conditions_can_not_empty'))
      return
    }
    for (let j = 0; j < field.conditions.length; j++) {
      const ele = field.conditions[j]
      if (!ele.term || ele.term === '') {
        ElMessage.error(t('chart.exp_can_not_empty'))
        return
      }
      if (ele.term === 'between') {
        if (!ele.term.includes('null') && !ele.term.includes('empty') && (!ele.min || !ele.max)) {
          ElMessage.error(t('chart.value_can_not_empty'))
          return
        }
        if (
          (field.field.deType === 2 || field.field.deType === 3 || field.field.deType === 4) &&
          (parseFloat(ele.min).toString() === 'NaN' || parseFloat(ele.max).toString() === 'NaN')
        ) {
          ElMessage.error(t('chart.value_error'))
          return
        }
      } else {
        if (!ele.term.includes('null') && !ele.term.includes('empty') && !ele.value) {
          ElMessage.error(t('chart.value_can_not_empty'))
          return
        }
        if (
          (field.field.deType === 2 || field.field.deType === 3 || field.field.deType === 4) &&
          parseFloat(ele.value).toString() === 'NaN'
        ) {
          ElMessage.error(t('chart.value_error'))
          return
        }
      }
    }
  }
  state.thresholdForm.tableThreshold = JSON.parse(JSON.stringify(state.tableThresholdArr))
  changeThreshold()
  closeTableThreshold()
}

init()
</script>

<template>
  <div style="width: 100%">
    <!--仪表盘-->
    <el-col v-if="props.chart.type && props.chart.type === 'gauge'">
      <el-form ref="thresholdForm" :model="state.thresholdForm" label-width="80px">
        <el-form-item :label="t('chart.threshold_range') + '(%)'" class="form-item">
          <span>0,</span>
          <el-input
            effect="dark"
            v-model="state.thresholdForm.gaugeThreshold"
            style="width: 100px; margin: 0 10px"
            :placeholder="t('chart.threshold_range')"
            size="small"
            clearable
            @change="gaugeThresholdChange"
          />
          <span>,100</span>
          <!--          <el-tooltip class="item" effect="dark" placement="bottom">-->
          <!--            <div slot="content">-->
          <!--              阈值设置，决定仪表盘区间颜色，为空则不开启阈值，范围(0-100)，逐级递增-->
          <!--              <br />-->
          <!--              例如：输入 30,70；表示：分为3段，分别为[0,30],(30,70],(70,100]-->
          <!--            </div>-->
          <!--            <i class="el-icon-info" style="cursor: pointer; margin-left: 10px; font-size: 12px" />-->
          <!--          </el-tooltip>-->
        </el-form-item>
      </el-form>
    </el-col>

    <!--文本卡-->
    <el-col v-if="props.chart.type && props.chart.type === 'label'">
      <el-col>
        <el-button
          :title="t('chart.edit')"
          class="circle-button"
          type="text"
          size="small"
          style="width: 24px; margin-left: 4px"
          @click="editTextLabelThreshold"
        >
          <template #icon>
            <Icon name="icon_edit_outlined"></Icon>
          </template>
        </el-button>
        <el-col style="padding: 0 18px">
          <el-row
            v-for="(item, index) in state.thresholdForm.textLabelThreshold"
            :key="index"
            class="line-style"
          >
            <el-col :span="6">
              <span v-if="item.term === 'eq'" :title="t('chart.filter_eq')">{{
                t('chart.filter_eq')
              }}</span>
              <span v-else-if="item.term === 'not_eq'" :title="t('chart.filter_not_eq')">{{
                t('chart.filter_not_eq')
              }}</span>
              <span v-else-if="item.term === 'like'" :title="t('chart.filter_like')">{{
                t('chart.filter_like')
              }}</span>
              <span v-else-if="item.term === 'not like'" :title="t('chart.filter_not_like')">{{
                t('chart.filter_not_like')
              }}</span>
              <span v-else-if="item.term === 'null'" :title="t('chart.filter_null')">{{
                t('chart.filter_null')
              }}</span>
              <span v-else-if="item.term === 'not_null'" :title="t('chart.filter_not_null')">{{
                t('chart.filter_not_null')
              }}</span>
            </el-col>
            <el-col :span="12">
              <span v-if="!item.term.includes('null')" :title="item.value">{{ item.value }}</span>
              <span v-else>&nbsp;</span>
            </el-col>
            <el-col :span="6">
              <span
                :style="{
                  width: '14px',
                  height: '14px',
                  backgroundColor: item.color,
                  border: 'solid 1px #e1e4e8'
                }"
              />
            </el-col>
          </el-row>
        </el-col>
      </el-col>
    </el-col>

    <!--指标卡-->
    <el-col v-if="props.chart.type && props.chart.type === 'text'">
      <el-col>
        <el-button
          :title="t('chart.edit')"
          class="circle-button"
          type="text"
          size="small"
          style="width: 24px; margin-left: 4px"
          @click="editLabelThreshold"
        >
          <template #icon>
            <Icon name="icon_edit_outlined"></Icon>
          </template>
        </el-button>
        <el-col style="padding: 0 18px">
          <el-row
            v-for="(item, index) in state.thresholdForm.labelThreshold"
            :key="index"
            class="line-style"
          >
            <el-col :span="6">
              <span v-if="item.term === 'eq'" :title="t('chart.filter_eq')">{{
                t('chart.filter_eq')
              }}</span>
              <span v-else-if="item.term === 'not_eq'" :title="t('chart.filter_not_eq')">{{
                t('chart.filter_not_eq')
              }}</span>
              <span v-else-if="item.term === 'lt'" :title="t('chart.filter_lt')">{{
                t('chart.filter_lt')
              }}</span>
              <span v-else-if="item.term === 'gt'" :title="t('chart.filter_gt')">{{
                t('chart.filter_gt')
              }}</span>
              <span v-else-if="item.term === 'le'" :title="t('chart.filter_le')">{{
                t('chart.filter_le')
              }}</span>
              <span v-else-if="item.term === 'ge'" :title="t('chart.filter_ge')">{{
                t('chart.filter_ge')
              }}</span>
              <span v-else-if="item.term === 'between'" :title="t('chart.filter_between')">{{
                t('chart.filter_between')
              }}</span>
            </el-col>
            <el-col :span="12">
              <span v-if="item.term !== 'between'" :title="item.value">{{ item.value }}</span>
              <span v-if="item.term === 'between'">
                {{ item.min }}&nbsp;≤{{ t('chart.drag_block_label_value') }}≤&nbsp;{{ item.max }}
              </span>
            </el-col>
            <el-col :span="6">
              <span
                :style="{
                  width: '14px',
                  height: '14px',
                  backgroundColor: item.color,
                  border: 'solid 1px #e1e4e8'
                }"
              />
            </el-col>
          </el-row>
        </el-col>
      </el-col>
    </el-col>

    <!--表格-->
    <el-col v-if="props.chart.type && props.chart.type.includes('table')">
      <el-col>
        <el-button
          :title="t('chart.edit')"
          class="circle-button"
          type="text"
          size="small"
          style="width: 24px; margin-left: 4px"
          @click="editTableThreshold"
        >
          <template #icon>
            <Icon name="icon_edit_outlined"></Icon>
          </template>
        </el-button>
        <el-col :style="{ padding: '0 18px', maxHeight: '500px', overflowY: 'auto' }">
          <el-row
            v-for="(fieldItem, fieldIndex) in state.thresholdForm.tableThreshold"
            :key="fieldIndex"
          >
            <el-row class="field-style">
              <span>
                <el-icon>
                  <Icon
                    :className="`field-icon-${fieldType(fieldItem.field.deType.deType)}`"
                    :name="`field_${fieldType(fieldItem.field.deType.deType)}`"
                  ></Icon>
                </el-icon>
              </span>
              <span :title="fieldItem.field.name" class="field-text">{{
                fieldItem.field.name
              }}</span>
            </el-row>
            <el-row v-for="(item, index) in fieldItem.conditions" :key="index" class="line-style">
              <el-col :span="6">
                <span v-if="item.term === 'eq'" :title="t('chart.filter_eq')">{{
                  t('chart.filter_eq')
                }}</span>
                <span v-else-if="item.term === 'not_eq'" :title="t('chart.filter_not_eq')">{{
                  t('chart.filter_not_eq')
                }}</span>
                <span v-else-if="item.term === 'lt'" :title="t('chart.filter_lt')">{{
                  t('chart.filter_lt')
                }}</span>
                <span v-else-if="item.term === 'gt'" :title="t('chart.filter_gt')">{{
                  t('chart.filter_gt')
                }}</span>
                <span v-else-if="item.term === 'le'" :title="t('chart.filter_le')">{{
                  t('chart.filter_le')
                }}</span>
                <span v-else-if="item.term === 'ge'" :title="t('chart.filter_ge')">{{
                  t('chart.filter_ge')
                }}</span>
                <span v-else-if="item.term === 'between'" :title="t('chart.filter_between')">{{
                  t('chart.filter_between')
                }}</span>
                <span v-else-if="item.term === 'like'" :title="t('chart.filter_like')">{{
                  t('chart.filter_like')
                }}</span>
                <span v-else-if="item.term === 'not like'" :title="t('chart.filter_not_like')">{{
                  t('chart.filter_not_like')
                }}</span>
                <span v-else-if="item.term === 'null'" :title="t('chart.filter_null')">{{
                  t('chart.filter_null')
                }}</span>
                <span v-else-if="item.term === 'not_null'" :title="t('chart.filter_not_null')">{{
                  t('chart.filter_not_null')
                }}</span>
                <span v-else-if="item.term === 'empty'" :title="t('chart.filter_empty')">{{
                  t('chart.filter_empty')
                }}</span>
                <span v-else-if="item.term === 'not_empty'" :title="t('chart.filter_not_empty')">{{
                  t('chart.filter_not_empty')
                }}</span>
              </el-col>
              <el-col :span="10">
                <span
                  v-if="
                    !item.term.includes('null') &&
                    !item.term.includes('empty') &&
                    item.term !== 'between'
                  "
                  :title="item.value"
                  >{{ item.value }}</span
                >
                <span
                  v-else-if="
                    !item.term.includes('null') &&
                    !item.term.includes('empty') &&
                    item.term === 'between'
                  "
                >
                  {{ item.min }}&nbsp;≤{{ t('chart.drag_block_label_value') }}≤&nbsp;{{ item.max }}
                </span>
                <span v-else>&nbsp;</span>
              </el-col>
              <el-col :span="4">
                <span
                  :title="t('chart.textColor')"
                  :style="{
                    width: '14px',
                    height: '14px',
                    backgroundColor: item.color,
                    border: 'solid 1px #e1e4e8'
                  }"
                />
              </el-col>
              <el-col :span="4">
                <span
                  :title="t('chart.backgroundColor')"
                  :style="{
                    width: '14px',
                    height: '14px',
                    backgroundColor: item.backgroundColor,
                    border: 'solid 1px #e1e4e8'
                  }"
                />
              </el-col>
            </el-row>
          </el-row>
        </el-col>
      </el-col>
    </el-col>

    <!--编辑文本卡阈值-->
    <el-dialog
      v-if="state.editTextLabelThresholdDialog"
      v-model="state.editTextLabelThresholdDialog"
      v-dialogDrag
      :title="t('chart.threshold')"
      :visible="state.editTextLabelThresholdDialog"
      :show-close="false"
      width="800px"
      class="dialog-css"
      append-to-body
    >
      <text-label-threshold-edit
        :threshold="state.thresholdForm.textLabelThreshold"
        @onTextLabelThresholdChange="thresholdTextChange"
      />
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeTextLabelThreshold">{{ t('chart.cancel') }}</el-button>
          <el-button type="primary" @click="changeTextLabelThreshold">{{
            t('chart.confirm')
          }}</el-button>
        </div>
      </template>
    </el-dialog>

    <!--编辑指标卡阈值-->
    <el-dialog
      v-if="state.editLabelThresholdDialog"
      v-model="state.editLabelThresholdDialog"
      v-dialogDrag
      :title="t('chart.threshold')"
      :visible="state.editLabelThresholdDialog"
      :show-close="false"
      width="800px"
      class="dialog-css"
      append-to-body
    >
      <text-threshold-edit
        :threshold="state.thresholdForm.labelThreshold"
        @onLabelThresholdChange="thresholdChange"
      />
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeLabelThreshold">{{ t('chart.cancel') }}</el-button>
          <el-button type="primary" @click="changeLabelThreshold">{{
            t('chart.confirm')
          }}</el-button>
        </div>
      </template>
    </el-dialog>

    <!--编辑表格阈值-->
    <el-dialog
      v-if="state.editTableThresholdDialog"
      v-model="state.editTableThresholdDialog"
      v-dialogDrag
      :title="t('chart.threshold')"
      :visible="state.editTableThresholdDialog"
      :show-close="false"
      width="800px"
      class="dialog-css"
      append-to-body
    >
      <table-threshold-edit
        :threshold="state.thresholdForm.tableThreshold"
        :chart="chart"
        @onTableThresholdChange="tableThresholdChange"
      />
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeTableThreshold">{{ t('chart.cancel') }}</el-button>
          <el-button type="primary" @click="changeTableThreshold">{{
            t('chart.confirm')
          }}</el-button>
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
.form-item-slider :deep(.el-form-item__label) {
  font-size: 12px;
  line-height: 38px;
}
.form-item :deep(.el-form-item__label) {
  font-size: 12px;
}
.el-select-dropdown__item {
  padding: 0 20px;
}
span {
  font-size: 12px;
}
.el-form-item {
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

.line-style :deep(span) {
  display: inline-block;
  width: 100%;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
  margin: 0 10px;
}

.dialog-css :deep(.el-dialog__title) {
  font-size: 14px;
}

.dialog-css :deep(.el-dialog__header) {
  padding: 20px 20px 0;
}

.dialog-css :deep(.el-dialog__body) {
  padding: 10px 20px 20px;
}

.field-style {
  display: flex;
  align-items: center;
  justify-content: flex-start;
}

.field-text {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #8492a6;
  font-size: 12px;
}
</style>

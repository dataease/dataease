<script lang="ts" setup>
import icon_info_outlined from '@/assets/svg/icon_info_outlined.svg'
import icon_searchOutline_outlined from '@/assets/svg/icon_search-outline_outlined.svg'
import icon_adjustment_outlined from '@/assets/svg/icon_adjustment_outlined.svg'
import icon_edit_outlined from '@/assets/svg/icon_edit_outlined.svg'
import icon_deleteTrash_outlined from '@/assets/svg/icon_delete-trash_outlined.svg'
import { ref, reactive, onMounted, onBeforeUnmount, computed } from 'vue'
import { ElMessage } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import type { AiCalcFieldSuggestion } from '@/api/aiConversation'
import CodeMirror from './CodeMirror.vue'
import { getFunction } from '@/api/dataset'
import { fieldType } from '@/utils/attr'
import { cloneDeep } from 'lodash-es'
import { guid } from './util'
import { iconFieldMap } from '@/components/icon-group/field-list'
export interface CalcFieldType {
  id?: string
  datasourceId?: string // 数据源id
  datasetTableId?: string // union node id
  datasetGroupId?: string // 有就传，没有null
  originName: string // 物理字段名
  name: string // 字段显示名
  dataeaseName?: string // 字段别名
  groupType: 'd' | 'q' // d=维度，q=指标
  type: string
  params?: Array<{ id: string; name: string; value: number }>
  checked: boolean
  deType: number // 字段类型
  deExtractType?: number // 字段原始类型
  extField?: number // 0=原始字段，2=复制或计算字段
  fieldShortName?: string // 字段别名
}
const { t } = useI18n()

const myCm = ref()
const searchField = ref('')
const searchFunction = ref('')

const mirror = ref()

const props = defineProps({
  datasetReferences: Boolean,
  crossDs: {
    type: Boolean,
    default: () => false
  }
})

const fields = [
  { label: t('dataset.text'), value: 0 },
  { label: t('dataset.time'), value: 1 },
  { label: t('dataset.value'), value: 2 },
  {
    label: t('dataset.value') + '(' + t('dataset.float') + ')',
    value: 3
  },
  { label: t('dataset.location'), value: 5 },
  { label: 'URL', value: 7 }
]

const defaultForm = {
  originName: '', // 物理字段名
  name: '', // 字段显示名
  groupType: 'd', // d=维度，q=指标
  type: 'VARCHAR',
  deType: 0, // 字段类型
  extField: 2,
  id: '',
  params: [],
  checked: true
}

const fieldForm = reactive<CalcFieldType>({ ...(defaultForm as CalcFieldType) })
const dimensionDataList = ref([])
const quotaFields = ref([])
const quotaDataList = computed(() => [...quotaFields.value, ...(fieldForm.params || [])])
const functions = ref([])
const matchesField = field =>
  field.name.toLocaleLowerCase().includes(searchField.value.toLocaleLowerCase())
const dimensionData = computed(() => dimensionDataList.value.filter(matchesField))
const quotaData = computed(() => quotaDataList.value.filter(matchesField))
const functionData = computed(() =>
  functions.value.filter(item =>
    item.func.toLocaleLowerCase().includes(searchFunction.value.toLocaleLowerCase())
  )
)

const formQuotaRef = ref()
const formQuota = reactive({
  id: null,
  name: '',
  value: null
})
const dialogFormVisible = ref(false)
const formQuotaRules = {
  name: [
    { required: true, message: t('data_set.enter_parameter_name'), trigger: 'blur' },
    { min: 1, max: 50, message: t('data_set.enter_1_50_characters'), trigger: 'blur' }
  ],
  value: [{ required: true, message: t('data_set.parameter_default_value'), trigger: 'blur' }]
}

const formQuotaClose = () => {
  formQuotaRef.value.resetFields()
  dialogFormVisible.value = false
}

const formQuotaConfirm = () => {
  formQuotaRef.value.validate(val => {
    if (val) {
      if (!formQuota.id) {
        formQuota.id = `params_${guid()}`
      }
      if (!setFieldForm()) return
      fieldForm.params = [cloneDeep(formQuota)]
      showFormula()
      formQuotaClose()
    }
  })
}

const setFieldForm = () => {
  const formula = readFormula()
  if (props.datasetReferences) {
    for (const [, name] of formula.matchAll(/\[([^\]]+)\]/g)) {
      if (references.value.counts.has(name) && !references.value.byName.has(name)) {
        ElMessage.error(t('ai_conversation.ambiguous_field'))
        return false
      }
    }
  }
  fieldForm.originName = setNameIdTrans('name', formula)
  return true
}

const references = computed(() => {
  const items = [...dimensionDataList.value, ...quotaDataList.value]
  const counts = new Map<string, number>()
  for (const item of items) counts.set(item.name, (counts.get(item.name) || 0) + 1)
  const byId = new Map<string, string>()
  const byName = new Map<string, string>()
  for (const item of items) {
    const id = String(item.id)
    const name = item.name
    let label = name
    if (props.datasetReferences) {
      // The same qualified token is advertised by the assistant for ambiguous names.
      let qualified = name.replaceAll('[', '［').replaceAll(']', '］') + ' #' + id
      while (counts.has(qualified)) qualified += ' #' + id
      byName.set(qualified, id)
      if (
        (counts.get(name) || 0) > 1 ||
        name.includes('[') ||
        name.includes(']') ||
        / #(?:params_)?-?\d+$/.test(name)
      )
        label = qualified
    }
    byId.set(id, label)
    byName.set(label, id)
  }
  return { byId, byName, counts }
})
const referenceName = item => references.value.byId.get(String(item.id)) || item.name
const setNameIdTrans = (from: 'id' | 'name', formula: string) => {
  const mapping = from === 'id' ? references.value.byId : references.value.byName
  return formula.replace(/\[([^\]]+)\]/g, (token, key) =>
    mapping.has(key) ? `[${mapping.get(key)}]` : token
  )
}

const showFormula = () => {
  mirror.value.dispatch({
    changes: {
      from: 0,
      to: mirror.value.state.doc.length,
      insert: setNameIdTrans('id', fieldForm.originName)
    }
  })
}

const editingExisting = ref(false)
const formulaRevision = ref(0)
const touched = reactive({ name: false, groupType: false, deType: false })
const readFormula = (): string => mirror.value.state.doc.toString()
const draftField = () => ({
  ...cloneDeep(fieldForm),
  originName: setNameIdTrans('name', readFormula())
})
const applyFormula = (suggestion: AiCalcFieldSuggestion) => {
  const { boundExpression, name, groupType, deType } = suggestion
  const expression = boundExpression ? setNameIdTrans('id', boundExpression) : suggestion.expression
  if (!editingExisting.value) {
    if (!touched.name && name) fieldForm.name = name
    if (!touched.groupType) fieldForm.groupType = groupType
    if (!touched.deType) fieldForm.deType = deType
  }
  mirror.value.dispatch({
    changes: { from: 0, to: mirror.value.state.doc.length, insert: expression }
  })
}
const selectGroup = (group: 'd' | 'q') => {
  touched.groupType = true
  fieldForm.groupType = group
}
const initEdit = (obj, dimensions, quotas) => {
  editingExisting.value = obj.extField === 2
  Object.assign(touched, { name: false, groupType: false, deType: false })
  Object.assign(fieldForm, cloneDeep({ ...defaultForm, ...obj }))
  dimensionDataList.value = dimensions
  quotaFields.value = quotas
  searchField.value = ''
  searchFunction.value = ''
  formField.value.clearValidate()
  showFormula()
}

const insertFieldToCodeMirror = (value: string) => {
  const { from, to } = mirror.value.state.selection.main
  mirror.value.dispatch({
    changes: { from, to, insert: value },
    selection: { anchor: from + value.length }
  })
}

onMounted(() => {
  mirror.value = myCm.value.codeComInit()
})

onBeforeUnmount(() => {
  mirror.value.destroy?.()
})

const formField = ref()

defineExpose({
  initEdit,
  setFieldForm,
  readFormula,
  draftField,
  applyFormula,
  formulaRevision,
  preserveType: computed(() => editingExisting.value || touched.deType),
  fieldForm,
  formField
})
const paramsTitle = ref('')
const addParamsToQuota = () => {
  if (disableCaParams.value) return
  Object.assign(formQuota, { id: null, name: '', value: null })
  paramsTitle.value = t('data_set.add_calculation_parameters')
  if (!fieldForm.params) {
    fieldForm.params = []
  }
  dialogFormVisible.value = true
}

const updateParamsToQuota = () => {
  const [o] = fieldForm.params
  paramsTitle.value = t('data_set.edit_calculation_parameters')
  Object.assign(formQuota, o || {})
  dialogFormVisible.value = true
}

const disableCaParams = computed(() => {
  return !!fieldForm.params?.length
})

const delParamsToQuota = () => {
  const [param] = fieldForm.params
  if (!setFieldForm()) return
  fieldForm.originName = fieldForm.originName.replaceAll(`[${param.id}]`, '')
  fieldForm.params = []
  showFormula()
}

getFunction().then(res => {
  functions.value = res
})
</script>

<template>
  <div @keydown.stop @keyup.stop class="calcu-field">
    <div class="calcu-cont">
      <div style="flex: 1">
        <div class="formula-editor">
          <el-form
            require-asterisk-position="right"
            ref="formField"
            @keydown.stop.prevent.enter
            :model="fieldForm"
            label-position="top"
          >
            <el-form-item
              prop="name"
              :rules="[
                {
                  required: true,
                  message: t('dataset.input_edit_name')
                },
                {
                  max: 50,
                  message: t('commons.char_can_not_more_50')
                }
              ]"
              :label="t('dataset.field_edit_name')"
            >
              <el-input
                v-model="fieldForm.name"
                @input="touched.name = true"
                :placeholder="t('dataset.input_edit_name')"
              />
            </el-form-item>
          </el-form>
          <div>
            <el-form label-position="top" ref="form" inline :model="fieldForm">
              <el-form-item class="mr12" :label="t('dataset.data_type')">
                <div class="btn-select">
                  <el-button
                    @click="selectGroup('d')"
                    :class="[fieldForm.groupType === 'd' && 'is-active']"
                    text
                  >
                    {{ t('chart.dimension_abb') }}
                  </el-button>
                  <el-button
                    @click="selectGroup('q')"
                    :class="[fieldForm.groupType === 'q' && 'is-active']"
                    text
                  >
                    {{ t('chart.quota_abb') }}
                  </el-button>
                </div>
              </el-form-item>
              <el-form-item class="mr0" :label="t('dataset.field_type')">
                <el-select
                  v-model="fieldForm.deType"
                  @change="touched.deType = true"
                  style="width: min(376px, 100%)"
                >
                  <template #prefix>
                    <el-icon>
                      <Icon
                        ><component
                          class="svg-icon"
                          :class="`field-icon-${fieldType[fieldForm.deType]}`"
                          :is="iconFieldMap[fieldType[fieldForm.deType]]"
                        ></component
                      ></Icon>
                    </el-icon>
                  </template>
                  <el-option
                    v-for="item in fields"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  >
                    <span style="display: flex; align-items: center">
                      <el-icon>
                        <Icon
                          ><component
                            class="svg-icon"
                            :class="`field-icon-${fieldType[item.value]}`"
                            :is="iconFieldMap[fieldType[item.value]]"
                          ></component
                        ></Icon>
                      </el-icon>
                    </span>
                    <span style="margin-left: 5px; font-size: 12px; color: #8492a6">{{
                      item.label
                    }}</span>
                  </el-option>
                </el-select>
              </el-form-item>
            </el-form>
          </div>
          <div class="mb8 field-exp">
            <span>{{ t('dataset.field_exp') }}</span>
            <span>*</span>
            <el-tooltip class="item" effect="dark" placement="top">
              <template #content>
                <div v-if="props.crossDs">{{ t('dataset.calc_tips.tip1') }}</div>
                <div v-else>{{ t('dataset.calc_tips.tip1_1') }}</div>
                <div>{{ t('dataset.calc_tips.tip2') }}</div>
              </template>
              <el-icon size="16px">
                <Icon name="icon_info_outlined"><icon_info_outlined class="svg-icon" /></Icon>
              </el-icon>
            </el-tooltip>
          </div>
          <code-mirror
            :quotaMap="quotaDataList.map(ele => ele.name)"
            :dimensionMap="dimensionDataList.map(ele => ele.name)"
            @change="formulaRevision++"
            ref="myCm"
            height="318px"
            dom-id="calcField"
          ></code-mirror>
        </div>
      </div>
      <div class="padding-lr field-ref">
        <span class="mb8">
          {{ t('dataset.click_ref_field') }}
          <el-tooltip class="item" effect="dark" placement="bottom">
            <template #content>
              {{ t('dataset.calc_tips.tip3') }}
              <br />
              {{ t('dataset.calc_tips.tip4') }}
              <br />
              {{ t('dataset.calc_tips.tip5') }}
            </template>
            <el-icon size="16px">
              <Icon name="icon_info_outlined"><icon_info_outlined class="svg-icon" /></Icon>
            </el-icon>
          </el-tooltip>
        </span>
        <div class="padding-lr-content">
          <el-input v-model="searchField" :placeholder="t('dataset.edit_search')" clearable>
            <template #prefix>
              <el-icon>
                <Icon name="icon_search-outline_outlined"
                  ><icon_searchOutline_outlined class="svg-icon"
                /></Icon>
              </el-icon>
            </template>
          </el-input>
          <div class="field-height">
            <span>{{ t('chart.dimension') }}</span>
            <div v-if="dimensionData.length" class="field-list">
              <span
                v-for="item in dimensionData"
                :key="item.id"
                class="item-dimension flex-align-center"
                :title="item.name"
                @click="insertFieldToCodeMirror('[' + referenceName(item) + ']')"
              >
                <el-icon>
                  <Icon
                    ><component
                      class="svg-icon"
                      :class="`field-icon-${fieldType[item.deType]}`"
                      :is="iconFieldMap[fieldType[item.deType]]"
                    ></component
                  ></Icon>
                </el-icon>
                <span class="ellipsis" :title="referenceName(item)">{{ referenceName(item) }}</span>
              </span>
            </div>

            <div v-else class="class-na">{{ t('dataset.na') }}</div>
          </div>
          <div class="quota-btn_de">
            <span>{{ t('chart.quota') }}</span>
            <el-tooltip
              effect="dark"
              :content="
                disableCaParams
                  ? t('data_set.parameter_is_supported')
                  : t('data_set.add_calculation_parameters')
              "
              placement="top"
            >
              <el-icon class="hover-icon_quota" @click="addParamsToQuota">
                <Icon
                  :class="[`field-icon-${fieldType[0]}`, disableCaParams && 'not-allow']"
                  style="color: #646a73"
                  name="icon_adjustment_outlined"
                  ><icon_adjustment_outlined class="svg-icon"
                /></Icon>
              </el-icon>
            </el-tooltip>
          </div>
          <div class="field-height">
            <div v-if="quotaData.length" class="field-list">
              <span
                v-for="item in quotaData"
                :key="item.id"
                class="item-quota flex-align-center"
                @click="insertFieldToCodeMirror('[' + referenceName(item) + ']')"
              >
                <el-icon v-if="!item.groupType">
                  <Icon name="icon_adjustment_outlined"
                    ><icon_adjustment_outlined class="svg-icon"
                  /></Icon>
                </el-icon>
                <el-icon v-else>
                  <Icon
                    ><component
                      class="svg-icon"
                      :class="`field-icon-${fieldType[item.deType]}`"
                      :is="iconFieldMap[fieldType[item.deType]]"
                    ></component
                  ></Icon>
                </el-icon>
                <span class="ellipsis" :title="referenceName(item)">{{ referenceName(item) }}</span>
                <div v-if="!item.groupType" class="icon-right">
                  <el-icon @click.stop="updateParamsToQuota" class="hover-icon">
                    <Icon name="icon_edit_outlined"><icon_edit_outlined class="svg-icon" /></Icon>
                  </el-icon>
                  <el-icon @click.stop="delParamsToQuota" class="hover-icon">
                    <Icon name="icon_delete-trash_outlined"
                      ><icon_deleteTrash_outlined class="svg-icon"
                    /></Icon>
                  </el-icon>
                </div>
              </span>
            </div>
            <div v-else class="class-na">{{ t('dataset.na') }}</div>
          </div>
        </div>
      </div>
      <div class="padding-lr">
        <span class="mb8">
          {{ t('dataset.click_ref_function') }}
          <el-tooltip class="item" effect="dark" placement="bottom">
            <template #content>
              <div v-if="props.crossDs">
                {{ t('dataset.calc_tips.tip6') }}
                <br />
                {{ t('dataset.calc_tips.tip8') }}
                <br />
                https://calcite.apache.org/docs/reference.html
              </div>
              <div v-else>{{ t('dataset.calc_tips.tip7') }}</div>
            </template>
            <el-icon size="16px">
              <Icon name="icon_info_outlined"><icon_info_outlined class="svg-icon" /></Icon>
            </el-icon>
          </el-tooltip>
        </span>
        <div class="padding-lr-content">
          <el-input
            v-model="searchFunction"
            style="margin-bottom: 8px"
            :placeholder="t('dataset.edit_search')"
            clearable
          >
            <template #prefix>
              <el-icon>
                <Icon name="icon_search-outline_outlined"
                  ><icon_searchOutline_outlined class="svg-icon"
                /></Icon>
              </el-icon>
            </template>
          </el-input>
          <el-row class="function-height">
            <div v-if="functionData.length" style="width: 100%">
              <el-popover
                v-for="(item, index) in functionData"
                :key="index"
                class="function-pop"
                placement="right"
                width="200"
                trigger="hover"
                :open-delay="500"
              >
                <template #reference>
                  <span
                    class="function-style flex-align-center"
                    @click="insertFieldToCodeMirror(item.func)"
                    >{{ item.func }}</span
                  >
                </template>
                <p class="pop-title">{{ item.name }}</p>
                <p class="pop-info">{{ item.func }}</p>
                <p class="pop-info">{{ item.desc }}</p>
              </el-popover>
            </div>
            <div v-else class="class-na">{{ t('chart.no_function') }}</div>
          </el-row>
        </div>
      </div>
    </div>
    <el-dialog
      :before-close="formQuotaClose"
      v-model="dialogFormVisible"
      append-to-body
      class="create-dialog"
      :title="paramsTitle"
      width="500"
    >
      <el-form
        @keydown.stop.prevent.enter
        label-position="top"
        ref="formQuotaRef"
        :model="formQuota"
        :rules="formQuotaRules"
      >
        <el-form-item :label="t('data_set.parameter_name')" prop="name">
          <el-input
            style="width: 100%"
            v-model="formQuota.name"
            :placeholder="t('data_set.enter_1_50_characters')"
          />
        </el-form-item>
        <el-form-item :label="t('data_set.parameter_default_value_de')" prop="value">
          <el-input-number
            style="width: 100%"
            v-model="formQuota.value"
            :placeholder="t('data_set.enter_a_number')"
            controls-position="right"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="formQuotaClose">{{ t('chart.cancel') }}</el-button>
          <el-button type="primary" @click="formQuotaConfirm"> {{ t('chart.confirm') }} </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="less" scoped>
.calcu-field {
  .calcu-cont {
    color: #606266;
    font-size: 14px;
    box-sizing: border-box;
    display: flex;
    justify-content: space-between;
  }

  .formula-editor {
    max-width: var(--calc-form-width, 488px);
  }

  .mr12 {
    margin-right: 12px;
  }

  .mr0 {
    margin-right: 0;

    :deep(.ed-select__prefix::after) {
      display: none;
    }
  }

  .btn-select {
    width: 100px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #ffffff;
    border: 1px solid #d9dcdf;
    border-radius: 6px;

    .is-active {
      background: var(--ed-color-primary-1a, rgba(51, 112, 255, 0.1));
    }

    .ed-button:not(.is-active) {
      color: #1f2329;
    }
    .ed-button.is-text {
      height: 24px;
      width: 44px;
      line-height: 24px;
    }
    .ed-button + .ed-button {
      margin-left: 4px;
    }
  }

  .mb8 {
    margin-bottom: 8px;
    display: inline-flex;
    align-items: center;
    color: #1f2329;

    &.field-exp {
      & > :nth-child(2) {
        margin: 0 -0.67px 0 2px;
        color: #f54a45;
        font-family: var(--de-custom_font, 'PingFang');
        font-size: 14px;
        font-style: normal;
        font-weight: 400;
        line-height: 22px;
      }
    }

    .ed-icon {
      color: #646a73;
      margin-left: 4.67px;
    }
  }
}
.padding-lr {
  margin-left: 12px;
  width: 214px;
  overflow-y: hidden;
  .padding-lr-content {
    padding: 11px;
    border: 1px solid var(--deCardStrokeColor, #dee0e3);
    box-sizing: border-box;
    height: 500px;
    border-radius: 6px;
  }

  &.field-ref .padding-lr-content {
    display: flex;
    flex-direction: column;
    padding-bottom: 2px;

    > .ed-input {
      flex: none;
    }
  }
}
.hover-icon_quota {
  cursor: pointer;
  border-radius: 6px;
  font-size: 16px;
  position: relative;

  &[aria-expanded='true'] {
    &::after {
      content: '';
      position: absolute;
      width: 24px;
      height: 24px;
      background: rgba(31, 35, 41, 0.1);
      border-radius: 6px;
      transform: translate(-50%, -50%);
      top: 50%;
      left: 50%;
    }
  }

  &:hover {
    &::after {
      content: '';
      position: absolute;
      width: 24px;
      height: 24px;
      background: rgba(31, 35, 41, 0.1);
      border-radius: 6px;
      transform: translate(-50%, -50%);
      top: 50%;
      left: 50%;
    }
  }

  &:active {
    &::after {
      content: '';
      position: absolute;
      width: 24px;
      height: 24px;
      background: rgba(31, 35, 41, 0.2);
      border-radius: 6px;
      transform: translate(-50%, -50%);
      top: 50%;
      left: 50%;
    }
  }
}

.quota-btn_de {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16px;
  margin-bottom: 3px;
  line-height: 22px;
  color: #1f2329;
}
.field-height {
  flex: none;
  height: 209px;
  margin-top: 16px;
  margin-right: -9px;
  padding-right: 1px;
  overflow-y: scroll;
  scrollbar-gutter: stable;

  &:last-child {
    flex: 1;
    height: auto;
    min-height: 0;
    margin-top: 3px;
  }

  &::-webkit-scrollbar {
    width: 8px;
    height: 8px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    border-radius: 6px;
    background: #1f23294d;
  }

  & > :nth-child(1) {
    color: #1f2329;
  }

  .not-allow {
    cursor: not-allowed;
    color: #bbbfc4 !important;
  }
}
.item-dimension,
.item-quota {
  padding: 1px 8px;
  border: solid 1px #dee0e3;
  background-color: white;
  color: #1f2329;

  .ed-icon {
    font-size: 16px;
    margin-right: 4px;
  }
  height: 28px;
  margin-top: 4px;
  word-break: break-all;
  border-radius: 6px;

  .icon-right {
    display: none;
    margin-left: auto;
    align-items: center;
    .ed-icon {
      margin: 0 0 0 6px;
    }
  }
}

.item-dimension:hover {
  border-color: var(--ed-color-primary, #3370ff);
  background: var(--ed-color-primary-1a, rgba(51, 112, 255, 0.1));
  cursor: pointer;
}

.item-quota {
  .ed-icon {
    color: #04b49c;
  }
}

.item-quota:hover {
  background: rgba(4, 180, 156, 0.1);
  border-color: #04b49c;
  cursor: pointer;
  .icon-right {
    display: flex;
  }
}

.function-style {
  min-height: 28px;
  padding: 0px 8px;
  margin-bottom: 4px;
  border-radius: 6px;
  color: #1f2329;
  &:hover {
    background: rgba(31, 35, 41, 0.1);
  }
}

.function-style:hover {
  border-color: var(--ed-color-primary, #3370ff);
  cursor: pointer;
}
.function-height {
  height: calc(100% - 29px);
  overflow: auto;
  width: calc(100% + 16px);
  margin-left: -8px;
}
.function-pop :deep(.ed-popover) {
  padding: 6px !important;
}
.pop-title {
  margin: 6px 0 0 0;
  font-size: 14px;
  font-weight: 500;
}
.pop-info {
  margin: 6px 0 0 0;
  font-size: 12px;
}

.class-na {
  margin-top: 8px;
  text-align: center;
  font-size: 14px;
  color: var(--deTextDisable);
}
</style>

<style lang="less">
.calcu-field {
  .cm-scroller {
    height: 320px;
    border: 1px solid #d9dcdf;
    border-radius: 6px;
    overflow-y: auto;
    background: #fff;
  }

  .cm-focused {
    outline: none;
  }
}
</style>

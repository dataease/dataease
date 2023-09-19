<script lang="ts" setup>
import { ref, reactive, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import CodeMirror from './CodeMirror.vue'
import { getFunction } from '@/api/dataset'
import { fieldType } from '@/utils/attr'
import { cloneDeep } from 'lodash-es'
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

const fields = [
  { label: t('dataset.text'), value: 0 },
  { label: t('dataset.time'), value: 1 },
  { label: t('dataset.value'), value: 2 },
  {
    label: t('dataset.value') + '(' + t('dataset.float') + ')',
    value: 3
  },
  { label: t('dataset.location'), value: 5 }
]

const defaultForm = {
  originName: '', // 物理字段名
  name: '', // 字段显示名
  groupType: 'd', // d=维度，q=指标
  type: 'VARCHAR',
  deType: 0, // 字段类型
  extField: 2,
  id: '',
  checked: true
}

const state = reactive({
  functionData: [],
  dimensionData: [],
  dimensionList: [],
  quotaList: [],
  quotaData: []
})

const fieldForm = reactive<CalcFieldType>({ ...(defaultForm as CalcFieldType) })

const setFieldForm = () => {
  const str = mirror.value.state.doc.toString()
  const name2Auto = []
  fieldForm.originName = setNameIdTrans('name', 'id', str, name2Auto)
}

const setNameIdTrans = (from, to, originName, name2Auto?: string[]) => {
  let name2Id = originName
  const nameIdMap = [...state.dimensionData, ...state.quotaData].reduce((pre, next) => {
    pre[next[from]] = next[to]
    return pre
  }, {})
  const on = originName.match(/\[(.+?)\]/g)
  if (on) {
    on.forEach(itm => {
      const ele = itm.slice(1, -1)
      if (name2Auto) {
        name2Auto.push(nameIdMap[ele])
      }
      name2Id = name2Id.replace(`[${ele}]`, `[${nameIdMap[ele]}]`)
    })
  }
  return name2Id
}

let quotaDataList = []
let dimensionDataList = []
const initEdit = (obj, dimensionData, quotaData) => {
  Object.assign(fieldForm, { ...defaultForm, ...obj })
  state.dimensionData = dimensionData
  state.quotaData = quotaData
  quotaDataList = cloneDeep(quotaData)
  dimensionDataList = cloneDeep(dimensionData)
  setTimeout(() => {
    formField.value.clearValidate()
  }, 100)
  if (!obj.originName) {
    mirror.value.dispatch({
      changes: {
        from: 0,
        to: mirror.value.viewState.state.doc.length,
        insert: ''
      }
    })
    return
  }
  mirror.value.dispatch({
    changes: {
      from: 0,
      to: mirror.value.viewState.state.doc.length,
      insert: setNameIdTrans('id', 'name', obj.originName)
    }
  })
}

const insertFieldToCodeMirror = (value: string) => {
  mirror.value.dispatch({
    changes: { from: mirror.value.viewState.state.selection.ranges[0].from, insert: value },
    selection: { anchor: mirror.value.viewState.state.selection.ranges[0].from }
  })
}

onMounted(() => {
  mirror.value = myCm.value.codeComInit()
})

onBeforeUnmount(() => {
  mirror.value.destroy?.()
})

const insertParamToCodeMirror = (value: string) => {
  mirror.value.dispatch({
    changes: { from: 0, to: mirror.value.state.doc.toString().length, insert: '' }
  })
  mirror.value.dispatch({
    changes: { from: mirror.value.viewState.state.selection.ranges[0].from, insert: value },
    selection: { anchor: mirror.value.viewState.state.selection.ranges[0].from }
  })
}
let functions = []
const initFunction = () => {
  getFunction().then(res => {
    functions = cloneDeep(res)
    state.functionData = cloneDeep(res)
  })
}
watch(
  () => searchField.value,
  val => {
    if (val && val !== '') {
      state.dimensionData = JSON.parse(
        JSON.stringify(
          dimensionDataList.filter(
            ele =>
              ele.name.toLocaleLowerCase().includes(val.toLocaleLowerCase()) && ele.extField === 0
          )
        )
      )
      state.quotaData = JSON.parse(
        JSON.stringify(
          quotaDataList.filter(
            ele =>
              ele.name.toLocaleLowerCase().includes(val.toLocaleLowerCase()) && ele.extField === 0
          )
        )
      )
    } else {
      state.dimensionData = JSON.parse(JSON.stringify(dimensionDataList)).filter(
        ele => ele.extField === 0
      )
      state.quotaData = JSON.parse(JSON.stringify(quotaDataList)).filter(ele => ele.extField === 0)
    }
  }
)

watch(
  () => searchFunction.value,
  val => {
    if (val && val !== '') {
      state.functionData = JSON.parse(
        JSON.stringify(
          functions.filter(ele => {
            return ele.func.toLocaleLowerCase().includes(val.toLocaleLowerCase())
          })
        )
      )
    } else {
      state.functionData = cloneDeep(functions)
    }
  }
)

const formField = ref()

defineExpose({
  initEdit,
  setFieldForm,
  fieldForm,
  formField
})

initFunction()
</script>

<template>
  <div @keydown.stop @keyup.stop class="calcu-field">
    <div class="calcu-cont">
      <div style="flex: 1">
        <div style="max-width: 488px">
          <el-form
            require-asterisk-position="right"
            ref="formField"
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
              <el-input v-model="fieldForm.name" :placeholder="t('dataset.input_edit_name')" />
            </el-form-item>
          </el-form>
          <div>
            <el-form label-position="top" ref="form" inline :model="fieldForm">
              <el-form-item class="mr12" :label="t('dataset.data_type')">
                <div class="btn-select">
                  <el-button
                    @click="fieldForm.groupType = 'd'"
                    :class="[fieldForm.groupType === 'd' && 'is-active']"
                    text
                  >
                    {{ t('chart.dimension') }}
                  </el-button>
                  <el-button
                    @click="fieldForm.groupType = 'q'"
                    :class="[fieldForm.groupType === 'q' && 'is-active']"
                    text
                  >
                    {{ t('chart.quota') }}
                  </el-button>
                </div>
              </el-form-item>
              <el-form-item class="mr0" :label="t('dataset.field_type')">
                <el-select v-model="fieldForm.deType" style="width: 376px">
                  <template #prefix>
                    <el-icon>
                      <Icon
                        :name="`field_${fieldType[fieldForm.deType]}`"
                        :className="`field-icon-${fieldType[fieldForm.deType]}`"
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
                          :name="`field_${fieldType[item.value]}`"
                          :className="`field-icon-${fieldType[item.value]}`"
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
                {{ t('dataset.calc_tips.tip1') }}
                <br />
                {{ t('dataset.calc_tips.tip2') }}
              </template>
              <el-icon>
                <Icon name="icon_info_outlined"></Icon>
              </el-icon>
            </el-tooltip>
          </div>
          <code-mirror
            :quotaMap="state.quotaData.map(ele => ele.name)"
            :dimensionMap="state.dimensionData.map(ele => ele.name)"
            ref="myCm"
            height="318px"
            dom-id="calcField"
          ></code-mirror>
        </div>
      </div>
      <div class="padding-lr">
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
            <el-icon>
              <Icon name="icon_info_outlined"></Icon>
            </el-icon>
          </el-tooltip>
        </span>
        <div class="padding-lr-content">
          <el-input v-model="searchField" :placeholder="t('dataset.edit_search')" clearable>
            <template #prefix>
              <el-icon>
                <Icon name="icon_search-outline_outlined"></Icon>
              </el-icon>
            </template>
          </el-input>
          <div class="field-height">
            <span>{{ t('chart.dimension') }}</span>
            <div v-if="state.dimensionData.length" class="field-list">
              <span
                v-for="item in state.dimensionData"
                :key="item.id"
                class="item-dimension flex-align-center ellipsis"
                :title="item.name"
                @click="insertFieldToCodeMirror('[' + item.name + ']')"
              >
                <el-icon>
                  <Icon
                    :name="`field_${fieldType[item.deType]}`"
                    :className="`field-icon-${fieldType[item.deType]}`"
                  ></Icon>
                </el-icon>
                {{ item.name }}
              </span>
            </div>
            <div v-else class="class-na">{{ t('dataset.na') }}</div>
          </div>
          <div class="field-height">
            <span>{{ t('chart.quota') }}</span>
            <div v-if="state.quotaData.length" class="field-list">
              <span
                v-for="item in state.quotaData"
                :key="item.id"
                class="item-quota flex-align-center ellipsis"
                :title="item.name"
                @click="insertFieldToCodeMirror('[' + item.name + ']')"
              >
                <el-icon>
                  <Icon
                    :name="`field_${fieldType[item.deType]}`"
                    :className="`field-icon-${fieldType[item.deType]}`"
                  ></Icon>
                </el-icon>
                {{ item.name }}
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
              {{ t('dataset.calc_tips.tip6') }}
              <br />
              {{ t('dataset.calc_tips.tip7') }}
              <br />
              {{ t('dataset.calc_tips.tip8') }}
              https://doris.apache.org/zh-CN/
            </template>
            <el-icon>
              <Icon name="icon_info_outlined"></Icon>
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
                <Icon name="icon_search-outline_outlined"></Icon>
              </el-icon>
            </template>
          </el-input>
          <el-row class="function-height">
            <div v-if="state.functionData.length" style="width: 100%">
              <el-popover
                v-for="(item, index) in state.functionData"
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
                    @click="insertParamToCodeMirror(item.func)"
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

  .mr12 {
    margin-right: 12px;
  }

  .mr0 {
    margin-right: 0;

    :deep(.ed-select__prefix--light) {
      padding: 0;
      border: none;
      margin: 0;
    }
  }

  .btn-select {
    width: 100px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #ffffff;
    border: 1px solid #bbbfc4;
    border-radius: 4px;

    .is-active {
      background: rgba(51, 112, 255, 0.1);
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
        font-family: PingFang SC;
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
    padding: 12px;
    border: 1px solid var(--deCardStrokeColor, #dee0e3);
    box-sizing: border-box;
    height: 500px;
    border-radius: 4px;
  }
}
.field-height {
  height: calc(50% - 41px);
  margin-top: 12px;
  overflow-y: auto;
  & > :nth-child(1) {
    color: #1f2329;
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
  border-radius: 4px;
}

.item-dimension:hover {
  border-color: var(--primary, #3370ff);
  background: rgba(51, 112, 255, 0.1);
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
}

.function-style {
  min-height: 28px;
  padding: 0px 8px;
  margin-bottom: 4px;
  border-radius: 4px;
  color: #1f2329;
  &:hover {
    background: rgba(31, 35, 41, 0.1);
  }
}

.function-style:hover {
  border-color: var(--primary, #3370ff);
  cursor: pointer;
}
.function-height {
  height: calc(100% - 81px);
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
  font-size: 10px;
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
    border: 1px solid #bbbfc4;
    border-radius: 4px;
    overflow-y: auto;
    background: #fff;
  }

  .cm-focused {
    outline: none;
  }
}
</style>

<script lang="ts" setup>
import { ref, reactive, onMounted, onBeforeUnmount, watch } from 'vue'
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
  formField.value.clearValidate()
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
    <div class="calcu-cont" style="height: 544px">
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
          <span class="mb8">
            {{ t('dataset.field_exp') }}
            <el-tooltip class="item" effect="dark" placement="bottom">
              <template #content>
                {{ t('dataset.calc_tips.tip1') }}
                <br />
                {{ t('dataset.calc_tips.tip2') }}
              </template>
              <el-icon>
                <Icon name="icon_info_outlined"></Icon>
              </el-icon>
            </el-tooltip>
          </span>
          <code-mirror
            :quotaMap="state.quotaData.map(ele => ele.name)"
            :dimensionMap="state.dimensionData.map(ele => ele.name)"
            ref="myCm"
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
        <el-input
          v-model="searchField"
          :placeholder="t('dataset.edit_search')"
          style="margin-bottom: 12px"
          clearable
        >
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
              class="item-dimension"
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
              class="item-quota"
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
        <el-input
          v-model="searchFunction"
          style="margin-bottom: 12px"
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
                <span class="function-style" @click="insertParamToCodeMirror(item.func)">{{
                  item.func
                }}</span>
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
</template>

<style lang="less" scoped>
.calcu-field {
  .calcu-cont {
    color: #606266;
    font-size: 14px;
    box-sizing: border-box;
    display: flex;
    justify-content: space-between;
    height: 544px;
  }

  .mr12 {
    margin-right: 12px;
  }

  .mr0 {
    margin-right: 0;
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

    i {
      margin-left: 4.67px;
    }
  }
}
.padding-lr {
  height: 500px;
  border: 1px solid var(--deCardStrokeColor, #dee0e3);
  border-radius: 4px;
  padding: 12px;
  box-sizing: border-box;
  margin-left: 12px;
  width: 214px;
  overflow-y: hidden;
}
.field-height {
  height: calc(50% - 41px);
  margin-top: 4px;
  overflow-y: auto;
}
.item-dimension {
  padding: 3px 8px;
  margin: 2px 2px 0 2px;
  border: solid 1px #eee;
  text-align: left;
  color: #606266;
  /*background-color: rgba(35,46,64,.05);*/
  background-color: white;
  display: flex;
  align-items: center;
  .ed-icon {
    font-size: 14px;
    margin-right: 5.25px;
  }
  word-break: break-all;
  border-radius: 2px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  i {
    color: #04b49c;
  }
}

.blackTheme .item-dimension {
  border: solid 1px;
  border-color: #495865;
  color: #f2f6fc;
  background-color: var(--MainBG);
}

.item-dimension + .item-dimension {
  margin-top: 2px;
}

.item-dimension:hover {
  border-color: var(--primary, #3370ff);
  background: rgba(51, 112, 255, 0.1);
  cursor: pointer;
}

.blackTheme .item-dimension:hover {
  /* color: var(--Main); */
  background: var(--ContentBG);
  /* cursor: pointer; */
}

.item-quota {
  padding: 3px 8px;
  margin: 2px 2px 0 2px;
  border: solid 1px #eee;
  text-align: left;
  color: #606266;
  /*background-color: rgba(35,46,64,.05);*/
  background-color: white;
  display: flex;
  align-items: center;
  .ed-icon {
    font-size: 14px;
    margin-right: 5.25px;
  }
  border-radius: 2px;
  word-break: break-all;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;

  i {
    color: #04b49c;
  }
}

.blackTheme .item-quota {
  border: solid 1px;
  border-color: #495865;
  color: #f2f6fc;
  background-color: var(--MainBG);
}

.item-quota + .item-quota {
  margin-top: 2px;
}

.item-quota:hover {
  background: rgba(4, 180, 156, 0.1);
  border-color: #04b49c;
  cursor: pointer;
}

.blackTheme .item-quota:hover {
  background: var(--ContentBG);
}
.function-style {
  color: var(--deTextPrimary, #1f2329);
  display: block;
  padding: 3px 8px;
  cursor: pointer;
  margin: 4px 0;
  word-break: break-word;
  border-radius: 2px;
  border: solid 1px #eee;
}

.blackTheme .function-style {
  border: solid 1px;
  border-color: #495865;
  color: #f2f6fc;
  background-color: var(--MainBG);
}
.function-style:hover {
  border-color: var(--primary, #3370ff);
  cursor: pointer;
}
.blackTheme .function-style:hover {
  background: var(--ContentBG);
}
.function-height {
  height: calc(100% - 81px);
  overflow: auto;
  margin-top: 4px;
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
  }

  .cm-focused {
    outline: none;
  }
}
</style>

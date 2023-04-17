<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { propTypes } from '@/utils/propTypes'
import CodeMirror from './CodeMirror.vue'

const { t } = useI18n()

const props = defineProps({
  param: propTypes.objectOf(
    propTypes.shape({
      id: Number
    })
  )
})

const myCm = ref()
const searchField = ref('')
const searchFunction = ref('')

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

const state = reactive({
  fieldForm: {
    id: null,
    name: '',
    groupType: 'd',
    deType: 0,
    originName: 'Dear [[name]],\nYour [[item]] is on its way. Please see [[order]] for details.\n',
    tableId: props.param.id,
    checked: 1,
    columnIndex: 0,
    size: 0,
    extField: 2
  },
  functionData: [],
  dimensionData: [],
  dimensionList: [],
  quotaList: [],
  quotaData: [
    {
      id: 'b5e574d6-e86a-42da-9d1e-54338e83fb50',
      tableId: '525bd11c-1b9d-4a41-ac3a-32a85b5d44a6',
      originName: 'result_count',
      name: '展示结果',
      dataeaseName: 'C_49546866e6fc51cc9412b4b12b7f8574',
      groupType: 'q',
      type: 'INT',
      size: 10,
      deType: 2,
      deTypeFormat: null,
      deExtractType: 2,
      extField: 0,
      checked: true,
      columnIndex: 7,
      lastSyncTime: 1680166956365,
      accuracy: 0,
      dateFormat: null,
      dateFormatType: null,
      jsonPath: null,
      deTypeCascader: [2]
    },
    {
      id: '8af54084-dd81-47e2-9379-42faa791cbc0',
      tableId: '525bd11c-1b9d-4a41-ac3a-32a85b5d44a6',
      originName: 'create_time',
      name: '创建时间',
      dataeaseName: 'C_b16a626598674ad426e4b885017906d7',
      groupType: 'q',
      type: 'BIGINT',
      size: 19,
      deType: 2,
      deTypeFormat: null,
      deExtractType: 2,
      extField: 0,
      checked: true,
      columnIndex: 21,
      lastSyncTime: 1680166956365,
      accuracy: 0,
      dateFormat: null,
      dateFormatType: null,
      jsonPath: null,
      deTypeCascader: [2]
    },
    {
      id: '94aca56d-c7c3-4509-95dd-804bf521d550',
      tableId: '525bd11c-1b9d-4a41-ac3a-32a85b5d44a6',
      originName: 'update_time',
      name: '更新时间',
      dataeaseName: 'C_e0df5a999da22bc6fa9e26b115eb4ae4',
      groupType: 'q',
      type: 'BIGINT',
      size: 19,
      deType: 2,
      deTypeFormat: null,
      deExtractType: 2,
      extField: 0,
      checked: true,
      columnIndex: 22,
      lastSyncTime: 1680166956365,
      accuracy: 0,
      dateFormat: null,
      dateFormatType: null,
      jsonPath: null,
      deTypeCascader: [2]
    },
    {
      id: '715c196a-b583-4e1a-adc3-1f76bb49312a',
      tableId: '525bd11c-1b9d-4a41-ac3a-32a85b5d44a6',
      originName: 'is_plugin',
      name: '是否插件',
      dataeaseName: 'C_79b7dae9e0be6b32227abc3e713704d2',
      groupType: 'q',
      type: 'BIT',
      size: 1,
      deType: 2,
      deTypeFormat: null,
      deExtractType: 4,
      extField: 0,
      checked: true,
      columnIndex: 26,
      lastSyncTime: 1680166956365,
      accuracy: 0,
      dateFormat: null,
      dateFormatType: null,
      jsonPath: null,
      deTypeCascader: [2]
    },
    {
      id: 'ccf2348d-2cca-43cb-a681-38829dfe6e24',
      tableId: '525bd11c-1b9d-4a41-ac3a-32a85b5d44a6',
      originName: 'refresh_view_enable',
      name: '是否开启刷新',
      dataeaseName: 'C_89602a9d5037f7fa71cdbab25408fda4',
      groupType: 'q',
      type: 'BIT',
      size: 1,
      deType: 2,
      deTypeFormat: null,
      deExtractType: 4,
      extField: 0,
      checked: true,
      columnIndex: 29,
      lastSyncTime: 1680166956365,
      accuracy: 0,
      dateFormat: null,
      dateFormatType: null,
      jsonPath: null,
      deTypeCascader: [2]
    },
    {
      id: '9ba74f05-4e82-44f2-bca4-1ec75e0d5125',
      tableId: '525bd11c-1b9d-4a41-ac3a-32a85b5d44a6',
      originName: 'refresh_time',
      name: '刷新时间',
      dataeaseName: 'C_0a3af0dc007b4aea737aea55e34b1f93',
      groupType: 'q',
      type: 'INT',
      size: 10,
      deType: 2,
      deTypeFormat: null,
      deExtractType: 2,
      extField: 0,
      checked: true,
      columnIndex: 31,
      lastSyncTime: 1680166956365,
      accuracy: 0,
      dateFormat: null,
      dateFormatType: null,
      jsonPath: null,
      deTypeCascader: [2]
    }
  ]
})

const fieldType = (deType: number) => {
  return ['text', 'time', 'value', 'value', 'location'][deType]
}

const insertFieldToCodeMirror = (value: string) => {
  console.log('myCm.value.myCm', myCm.value.myCm)

  const mirror = myCm.value.myCm
  mirror.dispatch({
    changes: { from: mirror.viewState.state.selection.ranges[0].from, insert: value },
    selection: { anchor: mirror.viewState.state.selection.ranges[0].from }
  })
}

const insertParamToCodeMirror = (value: string) => {
  const mirror = myCm.value.myCm
  mirror.dispatch({
    changes: { from: mirror.viewState.state.selection.ranges[0].from, insert: value },
    selection: { anchor: mirror.viewState.state.selection.ranges[0].from }
  })
}
</script>

<template>
  <div class="calcu-field">
    <el-form ref="form" :model="state.fieldForm" label-position="top">
      <el-form-item :label="t('dataset.field_edit_name')">
        <el-input v-model="state.fieldForm.name" :placeholder="t('dataset.input_edit_name')" />
      </el-form-item>
    </el-form>
    <div class="calcu-cont" style="height: 544px">
      <div style="flex: 1">
        <div style="max-width: 480px">
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
          <code-mirror ref="myCm"></code-mirror>
        </div>
        <div style="margin-top: 28px">
          <el-form label-position="top" ref="form" :model="state.fieldForm" class="de-form-item">
            <el-form-item :label="t('dataset.data_type')">
              <el-radio v-model="state.fieldForm.groupType" label="d">{{
                t('chart.dimension')
              }}</el-radio>
              <el-radio v-model="state.fieldForm.groupType" label="q">{{
                t('chart.quota')
              }}</el-radio>
            </el-form-item>
            <el-form-item label-position="top" :label="t('dataset.field_type')">
              <el-radio
                v-for="item in fields"
                :key="item.value"
                v-model="state.fieldForm.deType"
                :label="item.value"
                >{{ item.label }}</el-radio
              >
            </el-form-item>
          </el-form>
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
                  :name="`field_${fieldType(item.deType)}`"
                  :className="`field-icon-${fieldType(item.deType)}`"
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
                  :name="`field_${fieldType(item.deType)}`"
                  :className="`field-icon-${fieldType(item.deType)}`"
                ></Icon>
              </el-icon>
              <span>{{ item.name }}</span>
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
          <div v-if="state.functionData.length">
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
  height: calc(100% - 80px);
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
  display: block;
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
.function-pop :deep(.el-popover) {
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
    height: 250px;
    border: 1px solid #bbbfc4;
    border-radius: 4px;
    overflow-y: auto;
  }

  .cm-focused {
    outline: none;
  }
}
</style>

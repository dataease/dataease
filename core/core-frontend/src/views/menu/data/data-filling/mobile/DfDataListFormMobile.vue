<script setup lang="ts">
import {filter, find, findIndex, forEach, map} from 'lodash-es'
import {useI18n} from "@/hooks/web/useI18n";
import {computed, onMounted, onUnmounted, ref} from "vue";
import {ColumnItem, formatDate, getDataFillingTemplateSettings, searchTable,} from "../data-filling";
import RowDataFormMobile from "./RowDataFormMobile.vue";
import {loadUserTaskDataList} from "../fill/fill_api";
import {isMobile} from "@/utils/utils";
import VanList from "vant/es/list";
import {ElDrawer} from "element-plus-secondary";
import VanTab from "vant/es/tab";
import VanTabs from "vant/es/tabs";
import DfItemCell from "./DfItemCell.vue";

const {t} = useI18n()

const emit = defineEmits(['update:modelValue', 'finish', 'close'])

const props = defineProps<{
  modelValue: boolean,
  subTaskId: string,
  formId: string,
  edit: boolean
}>()

const title = ref('')
const edit = ref(false);
const showDialog = ref(false)

const inMobile = computed(() => {
  return isMobile()
})

const tableData = ref([])
const subInstances = ref([])
const forms = ref([])
const formExtSettings = ref([])
const imgType = ref()
const emptyDesc = ref('')
const loading = ref(false)

const subTaskId = ref('')

const key = ref()

const getEmptyImg = (): string => {
  return 'noneWhite'
}

const getEmptyDesc = (): string => {
  return t('data_fill.data.data_not_exists')
}

const subOpenManual = ref(false)

function openDataForm(_data) {
  const dataList = map(subInstances.value, instance => {
    return {
      taskItemId: instance.id,
      rowDataId: instance.dataId
    }
  })
  const _id = _data[key.value]
  const _index = findIndex(subInstances.value, instance => instance.dataId === _id)

  subOpenManual.value = true
  window.history.pushState(null, null, window.location.href);
  rowDataFormRef.value?.init(formId.value, append.value, edit.value, dataList, _index, true)
}


const computedTableData = computed(() => {
  const ids = map(filter(subInstances.value, s => {
    if (activeCommand.value === 'all') {
      return true
    } else if (activeCommand.value === 'todo') {
      return s.status === 0
    } else {
      return s.status === 1
    }
  }), x => x.dataId)
  return filter(tableData.value, td => ids.includes(td[key.value]))
})

const append = ref(false)
const appendBaseTaskInstance = ref<string | undefined>(undefined)

function getStatus(item) {
  return find(subInstances.value, instance => instance.dataId === item[key.value])?.status
}

const loadTableData = () => {
  loading.value = true

  key.value = undefined
  loadUserTaskDataList(subTaskId.value)
      .then(res => {
        if (res?.data?.subInstances) {
          subInstances.value = res.data.subInstances
          //看subInstance 有没有dataId
          title.value = res.data.formTitle
          append.value = res.data.fillType !== 1
          if (append.value) {
            appendBaseTaskInstance.value = subTaskId.value
          } else {
            appendBaseTaskInstance.value = undefined
          }
          if (edit.value || res.data?.dataIds?.length > 1 || res.data?.subInstances?.length > 1) {
            if (!append.value) {
              getDataFillingTemplateSettings(res.data.subInstances[0].id).then(d => {
                if (d) {
                  templateSettings.value = JSON.parse(d)
                }
              }).catch(e => {
              })
            }

            showDialog.value = true
            forms.value = JSON.parse(res.data.form)
            formExtSettings.value = res.data.formExtSetting ? JSON.parse(res.data.formExtSetting) : []
            //查询数据
            if (res.data.dataIds && res.data.dataIds.length > 0) {
              loading.value = true
              searchTable(res.data.formId, {
                withoutLogs: true,
                primaryKeyValueList: res.data.dataIds
              }).then(res2 => {
                if (res2.data) {
                  tableData.value = res2.data.data
                  key.value = res2.data.key
                }
              }).finally(() => {
                loading.value = false
              })
            } else {
              tableData.value = []
              loading.value = false
            }
          } else {
            //只有一条直接转表单
            const dataList = map(res.data.subInstances, instance => {
              return {
                taskItemId: instance.id,
                rowDataId: instance.dataId
              }
            })
            rowDataFormRef.value?.init(formId.value, append.value, edit.value, dataList, undefined, true)
            loading.value = false
          }
        } else {
          loading.value = false
        }
      })
      .catch(err => {
        tableData.value = []
        subInstances.value = []
        loading.value = false
      })
      .finally(() => {
        imgType.value = getEmptyImg()
        emptyDesc.value = getEmptyDesc()
      })
}

const addRowData = () => {
  rowDataFormRef.value?.init(
      formId.value,
      append.value,
      true,
      undefined,
      undefined,
      true,
      appendBaseTaskInstance.value
  );
};

const formId = ref()

function init(_subTaskId: string, _formId: string, _edit?: boolean) {
  checkedColumns.value = []
  key.value = undefined
  formId.value = _formId
  subTaskId.value = _subTaskId
  edit.value = !!_edit
  templateSettings.value = []
  append.value = false
  loadTableData()
}

const rowDataFormRef = ref()

const edited = ref(false)

const templateSettings = ref([])

const columns = computed<Array<ColumnItem>>(() => {
  const _list: Array<ColumnItem> = []
  //根据task设置过滤展示
  forEach(filter(forms.value, f => !f.removed), f => {
    const _t = find(templateSettings.value, s => s.id === f.id)
    if (f.type === 'dateRange') {
      _list.push({
        props: f.settings?.mapping?.columnName1,
        label: f.settings?.name,
        date: true,
        dateType: f.settings?.dateType,
        type: f.type,
        multiple: !!f.settings.multiple,
        rangeIndex: 0,
        disabled: !!_t?.disable
      } as ColumnItem)
      _list.push({
        props: f.settings?.mapping?.columnName2,
        label: f.settings?.name,
        date: true,
        dateType: f.settings?.dateType,
        type: f.type,
        multiple: !!f.settings.multiple,
        rangeIndex: 1,
        disabled: !!_t?.disable
      } as ColumnItem)
    } else {
      _list.push({
        props: f.settings?.mapping?.columnName,
        label: f.settings?.name,
        date: f.type === 'date',
        dateType: f.settings?.dateType,
        type: f.type,
        multiple: !!f.settings.multiple,
        disabled: !!_t?.disable
      } as ColumnItem)
    }
  })
  return _list
})

const reset = (trigger) => {
  if (edited.value) {
    emit('finish')
  }
  edited.value = false
  title.value = ''
  formId.value = undefined
  subTaskId.value = ''
  tableData.value = []
  emit('close')
  showDialog.value = false
  emit('update:modelValue', false)
  if (trigger == undefined || typeof trigger !== "boolean") {
    //window.history.go(-1)
  }
}

function closeDialog(trigger) {
  reset(trigger)
}

function onCloseData() {
  subOpenManual.value = false
  if (!showDialog.value) {
    closeDialog()
  }
}

function onCloseToRefresh() {
  if (showDialog.value) {
    edited.value = true
    loadTableData()
  } else {
    edited.value = true
    closeDialog()
  }
}

const activeCommand = ref('all')

const curTypeList = ref([
  {key: "all", name: t("data_fill.all")},
  {key: "todo", name: t("data_fill.todo")},
  {key: "finished", name: t("data_fill.finished")},
])

const checkedColumns = ref([])

function onColumnChange(list) {
  checkedColumns.value = list
}

const computedColumns = computed(() => {
  if (checkedColumns.value.length === 0) {
    return columns.value
  }
  return filter(columns.value, c => checkedColumns.value.includes(c.props))
})

defineExpose({init, closeDialog})

const keyFunction = (event: any) => {
  event.preventDefault()
  if (subOpenManual.value) {
    rowDataFormRef.value?.closeDialog()
  } else {
    closeDialog(true)
  }
}

onMounted(() => {
  init(props.subTaskId, props.formId, props.edit)
  window.history.pushState(null, null, window.location.href);//手机端返回的时候这里无法拦截返回事件，为防止直接退到其他页面，增加一个当前路由
  window.addEventListener("popstate", keyFunction);
})

onUnmounted(() => {
  window.removeEventListener("popstate", keyFunction);
})

</script>

<template>
  <div>
    <el-drawer
        size="100%"
        direction="rtl"
        :before-close="reset"
        v-model="showDialog"
        :close-on-click-modal="false"
        :z-index="11"
        append-to-body
        destroy-on-close
        :show-close="false"
        modal-class="df-list-mobile"
    >
      <template #header>
        <div style="flex: 1; display: flex; flex-direction: row; align-items: center">
          <el-button class="back-btn" icon="ArrowLeftBold" link @click="closeDialog"/>
          <div class="title">{{ title }}</div>
        </div>
        <el-button v-if="edit && append" class="add-btn" type="primary" icon="Plus" link @click="addRowData"/>
      </template>
      <el-main class="df-main" v-loading="loading">

        <div
            style="width: 100%; height: calc(100vh - 100px); display: flex;  align-items: center; justify-content: center;"
            v-if="!loading && tableData.length === 0">
          <template v-if="!(edit && append)">
            {{ getEmptyDesc() }}
          </template>
          <template v-else>
            <el-button v-if="edit && append" type="primary" icon="Plus" link @click="addRowData">
              {{ t("data_fill.data.add_data") }}
            </el-button>
          </template>
        </div>
        <template v-else>
          <van-tabs @click-tab="onActiveCommandChange" v-model:active="activeCommand">
            <van-tab
                v-for="item in curTypeList"
                :key="item.key"
                :name="item.key"
                :title="item.name"
            />
          </van-tabs>

          <el-main class="workbranch-grid">
            <van-list>
              <DfItemCell v-for="(item, _index) in computedTableData" :key="_index"
                          :index="_index + 1"
                          :columns="computedColumns"
                          :data="item"
                          @click="openDataForm(item)"
              >
                <table style="table-layout: fixed; text-overflow: ellipsis;">
                  <template v-for="(c, i) in computedColumns">
                    <tr v-if="i < 4" :key="c.props">
                      <td style="font-weight: 400">
                        {{ c.label }}:&nbsp;
                      </td>
                      <td
                          v-if="c.date && item[c.props]"
                          style="
                          text-overflow: ellipsis;
                          word-break: break-all;
                          white-space: nowrap;"
                      >
                        {{ formatDate(item[c.props], c.dateType) }}
                      </td>
                      <td v-else style="
                          text-overflow: ellipsis;
                          word-break: break-all;
                          white-space: nowrap;">
                        {{ item[c.props] }}
                      </td>
                    </tr>
                  </template>
                </table>
                <template #icon>
                  <div v-if="getStatus(item) === 1" class="icon-1">
                    {{ t("data_fill.finished") }}
                  </div>
                  <div v-else class="icon-0">
                    {{ t("data_fill.todo") }}
                  </div>
                </template>
              </DfItemCell>
            </van-list>
          </el-main>

        </template>
      </el-main>

    </el-drawer>

    <RowDataFormMobile ref="rowDataFormRef" @finish="onCloseToRefresh" @close="onCloseData"/>
  </div>
</template>

<style scoped lang="less">
.workbranch-grid {
  height: calc(100% - 40px);
  padding: 0;
}

.disabled-text {
  color: #909399;
}


</style>
<style lang="less">
.df-list-mobile {
  --van-tab-active-text-color: var(--ed-color-primary);
  --van-tabs-line-height: 40px;
  --van-border-width: 0;
  --van-tab-text-color: #646a73;
  --van-tabbar-item-text-color: #8f959e;


  .ed-drawer__header {
    height: 44px;
    padding: 0;
    display: flex;
    align-items: center;
    border-bottom: unset;

    & > :first-child {
      flex: unset;
    }

    .title {
      max-width: 60%;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      color: var(--van-nav-bar-title-text-color);
      font-weight: var(--van-font-bold);
      font-size: var(--van-nav-bar-title-font-size);
    }

    .back-btn {
      height: 44px;
      min-width: 44px;
      font-size: 16px;
      margin-right: 8px;
    }

    .add-btn {
      height: 44px;
      min-width: 44px;
      font-size: 16px;
    }
  }

  .ed-drawer__body {
    padding: 0;
    background: #f5f6f7;

    .van-tabs__line {
      display: none;
    }

    .df-main {
      margin-top: 8px;
      padding: 0;
      height: calc(100% - 8px);
      background: var(--ed-bg-color);

      .icon-0 {
        padding: 2px 4px;
        font-size: 12px;
        background: #e10000;
        color: white;
        border-radius: 4px;
      }

      .icon-1 {
        padding: 2px 4px;
        font-size: 12px;
        background: #38e000;
        color: white;
        border-radius: 4px;
      }

    }

  }

}
</style>

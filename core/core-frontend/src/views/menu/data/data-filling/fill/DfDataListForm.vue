<script setup lang="ts">
import icon_describe_outlined from '@/assets/svg/icon_describe_outlined.svg'
import icon_edit_outlined from '@/assets/svg/icon_edit_outlined.svg'
import {filter, find, findIndex, forEach, map, slice} from 'lodash-es'
import {useI18n} from "@/hooks/web/useI18n";
import {computed, ref, unref} from "vue";
import {ColumnItem, formatDate, getDataFillingTemplateSettings, searchTable,} from "../data-filling";
import RowDataForm from "../manage/form/RowDataForm.vue";
import GridTable from "@/components/grid-table/src/GridTable.vue";
import {loadUserTaskDataList, saveTaskRowData, userTaskDeleteRowData} from "./fill_api";
import {Icon} from "@/components/icon-custom";
import dayjs from "dayjs";
import icon_add_outlined from "@/assets/svg/icon_add_outlined.svg";
import icon_deleteTrash_outlined from "@/assets/svg/icon_delete-trash_outlined.svg";
import {ElDrawer, ElMessage, ElMessageBox} from "element-plus-secondary";
import ExcelBatchUpload from "../manage/ExcelBatchUpload.vue";
import icon_upload_outlined from "@/assets/svg/icon_upload_outlined.svg";

const {t} = useI18n()

const emit = defineEmits(['update:show', 'finish'])

const title = ref('')
const edit = ref(false);
const showDialog = ref(false)


const readonly = computed<boolean>(() => {
  return !edit.value
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
  return '没有数据'
}

function openDataForm(_data) {
  const dataList = map(subInstances.value, instance => {
    return {
      taskItemId: instance.id,
      rowDataId: instance.dataId
    }
  })
  const _id = _data[key.value]
  const _index = findIndex(subInstances.value, instance => instance.dataId === _id)

  rowDataFormRef.value?.init(formId.value, append.value, edit.value, dataList, _index, true)

}

function deleteRow(_data) {
  ElMessageBox.confirm(t("data_fill.confirm_delete_data"), {
    confirmButtonType: "danger",
    type: "warning",
    confirmButtonText: t("common.delete"),
    cancelButtonText: t("dataset.cancel"),
    autofocus: false,
    showClose: false,
  }).then(() => {
    loading.value = true;
    const _id = _data[key.value]
    userTaskDeleteRowData(subTaskId.value, _id)
        .then((res) => {
          ElMessage.success(t("common.delete_success"));
          loadTableData()
          emit('finish')
        })
        .catch(() => {
          loading.value = false;
        });
  });
}

function quickConfirm(_data) {
  //开始校验数据
  const data = {}
  data[key.value] = _data[key.value]
  for (let i = 0; i < columns.value.length; i++) {
    const c = columns.value[i]
    if (c.type === "date" || c.type === "dateRange") {
      if (_data[c.props]) {
        data[c.props] = dayjs(_data[c.props]).toDate().getTime()
      }
    } else {
      data[c.props] = _data[c.props]
    }
  }
  loading.value = true
  saveTaskRowData(find(subInstances.value, si => si.dataId === data[key.value])?.id, data).then(res => {
    onCloseToRefresh()
  }).finally(() => {
    closeQuickConfirmForm(_data)
    loading.value = false
  })

}

function closeQuickConfirmForm(row) {
  if (row._popoverRef) {
    row._popoverRef?.hide();
  }
}

function setPopoverRef(el, row) {
  row._popoverRef = el
}

function setButtonRef(el, row) {
  row._buttonRef = el
}

const onClickOutside = (row) => {
  if (row._popoverRef) {
    unref(row._popoverRef)._popoverRef?.delayHide?.();
  }
};

function getInstanceStatus(id) {
  return find(subInstances.value, s => s.dataId === id)?.status
}

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const computedTableData = computed(() => {
  return slice(tableData.value, (currentPage.value - 1) * pageSize.value, currentPage.value * pageSize.value)
})

const append = ref(false)
const appendBaseTaskInstance = ref<string | undefined>(undefined)

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

                  total.value = res2.data.data.length
                }
              }).finally(() => {
                loading.value = false
              })
            } else {
              tableData.value = []
              total.value = 0
              currentPage.value = 1
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

const reset = () => {
  if (edited.value) {
    emit('finish')
  }
  showDialog.value = false
  edited.value = false
  title.value = ''
  formId.value = undefined
  subTaskId.value = ''
  tableData.value = []
}

function closeDialog() {
  reset()
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


const keyFunction = (e: any) => {
  if (e?.keyCode === 13) {
  }
}
const removeKeyDown = () => {
  window.removeEventListener("keydown", keyFunction);
}
const addKeyDown = () => {
  window.addEventListener("keydown", keyFunction);
}

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

function getSelectOptions(data) {
  try {
    return JSON.parse(data)
  } catch (e) {
    console.error(e)
  }
  return []
}

const showDownloadDrawer = ref(false);

function openUploadData() {
  showDownloadDrawer.value = true;
}

function closeUpload() {
  showDownloadDrawer.value = false;
}

function finishUpload() {
  closeUpload();
  loadTableData()
  emit('finish')
}

defineExpose({init, closeDialog})

</script>

<template>
  <div>
    <el-drawer
        size="calc(100% - 64px)"
        direction="btt"
        :before-close="reset"
        v-model="showDialog"
        :title="title"
        :close-on-click-modal="false"
        :z-index="11"
        append-to-body
        destroy-on-close
        @open="addKeyDown"
        @close="removeKeyDown"
    >
      <el-main class="df-main" v-loading="loading">
        <div style="margin-bottom: 8px; display: flex; justify-content: space-between;">
          <div>
            <template v-if="edit && append">
              <el-button @click="addRowData">
                <template #icon>
                  <Icon name="icon_add_outlined">
                    <icon_add_outlined class="svg-icon"/>
                  </Icon>
                </template>
                {{ t("data_fill.data.add_data") }}
              </el-button>
              <el-button @click="openUploadData">
                <template #icon>
                  <Icon name="icon_upload_outlined">
                    <icon_upload_outlined/>
                  </Icon>
                </template>
                {{ t("data_fill.data.batch_upload") }}
              </el-button>
            </template>
          </div>
          <column-list :columnNames="columns" @columnChange="onColumnChange"/>
        </div>
        <GridTable
            ref="multipleTableRef"
            :show-pagination="false"
            :table-data="computedTableData"
            :empty-desc="emptyDesc"
            :empty-img="imgType"
            border
            class="workbranch-grid"
        >
          <el-table-column
              min-width="150"
              v-for="c in computedColumns"
              :key="c.props"
              :prop="c.props"
          >
            <template #header>
              {{ c.label }}
              <span v-if="c.rangeIndex === 0">({{ t('data_fill.data.start') }})</span>
              <span v-if="c.rangeIndex === 1">({{ t('data_fill.data.end') }})</span>
            </template>
            <template #default="scope">
              <div :class="{'disabled-text': c.disabled}">
                <span
                    v-if="c.date && scope.row[c.props]"
                    style="white-space:nowrap; width: fit-content"
                    :title="formatDate(scope.row[c.props], c.dateType)"
                >
                  {{ formatDate(scope.row[c.props], c.dateType) }}
                </span>
                <template
                    v-else-if="(c.type === 'select' && c.multiple || c.type === 'checkbox') && scope.row[c.props]">
                  <div
                      v-for="(x, $index) in getSelectOptions(scope.row[c.props])"
                      :key="$index"
                      style="white-space:nowrap; width: fit-content"
                      :title="x"
                  >
                    {{ x }}
                  </div>
                </template>
                <span
                    v-else
                    style="white-space:nowrap; width: fit-content"
                    :title="scope.row[c.props]"
                >
                  {{ scope.row[c.props] }}
                </span>
              </div>
            </template>
          </el-table-column>
          <el-table-column
              :label="t('data_fill.form.status')"
              width="90"
              fixed="right"
          >
            <template #default="scope">
              <div v-if="getInstanceStatus(scope.row[key]) === 1" style="color: #34C724">
                {{ t('data_fill.form.status_1') }}
              </div>
              <div v-else-if="!append">
                {{ t('data_fill.form.status_0') }}
              </div>
            </template>
          </el-table-column>

          <el-table-column
              :label="t('data_fill.form.operation')"
              width="90"
              fixed="right"
          >
            <template #default="scope">
              <el-button
                  text
                  @click="openDataForm(scope.row)"
              >
                <template #icon>
                  <Icon name="icon_describe_outlined" v-if="!edit">
                    <icon_describe_outlined class="svg-icon"/>
                  </Icon>
                  <Icon name="icon_edit_outlined" v-else>
                    <icon_edit_outlined class="svg-icon"/>
                  </Icon>
                </template>
              </el-button>
              <el-button
                  v-if="edit && append"
                  text
                  @click="deleteRow(scope.row)"
              >
                <template #icon>
                  <Icon name="icon_delete-trash_outlined">
                    <icon_deleteTrash_outlined/>
                  </Icon>
                </template>
              </el-button>
              <template v-if="!append">
                <el-popover placement="top"
                            :width="160"
                            show-arrow
                            trigger="click"
                            :persistent="false"
                            :virtual-ref="scope.row._buttonRef"
                            :ref="
                            (el) => {
                              setPopoverRef(el, scope.row);
                            }
                          "
                >
                  <div>
                    <div style="margin-bottom: 8px">{{ t('data_fill.form.confirm_to_mark_as_complete') }}</div>
                    <div style="text-align: right; margin: 0">
                      <el-button size="small" text @click="closeQuickConfirmForm(scope.row)">
                        {{ t("common.cancel") }}
                      </el-button>
                      <el-button size="small" type="primary" @click="quickConfirm(scope.row)">
                        {{ t('commons.confirm') }}
                      </el-button>
                    </div>
                  </div>
                  <template #reference>
                    <el-button
                        text
                        v-if="edit"
                        :disabled="getInstanceStatus(scope.row[key]) === 1"
                        :ref="
                        (el) => {
                          setButtonRef(el, scope.row);
                        }
                      "
                        v-click-outside="onClickOutside"
                    >
                      <template #icon>
                        <Icon name="icon_describe_outlined">
                          <Select/>
                        </Icon>
                      </template>
                    </el-button>
                  </template>
                </el-popover>
              </template>
            </template>
          </el-table-column>


        </GridTable>

        <div style="
            display: flex;
            flex-direction: row;
            align-items: center;
            justify-content: flex-end;
            height: 45px;
        ">
          <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[5, 10, 20, 30, 40, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="total"
          />
        </div>

      </el-main>

      <template #footer>
        <div
            class="de-footer"
        >
          <el-button @click="closeDialog">{{ t("commons.close") }}</el-button>
        </div>
      </template>

      <el-drawer
          v-if="append"
          :title="t('data_fill.data.batch_upload')"
          :close-on-click-modal="false"
          size="calc(100% - 100px)"
          v-model="showDownloadDrawer"
          direction="btt"
          append-to-body
          modal-class="df-upload-drawer"
      >
        <ExcelBatchUpload
            v-if="showDownloadDrawer"
            :formId="formId"
            :form-name="title"
            :taskInstanceId="appendBaseTaskInstance"
            :columns="columns"
            @close="closeUpload"
            @finish="finishUpload"
        />
      </el-drawer>

    </el-drawer>

    <RowDataForm ref="rowDataFormRef" @finish="onCloseToRefresh"/>
  </div>
</template>

<style scoped lang="less">
.df-main {
  height: 100%;
}

.workbranch-grid {
  height: calc(100% - 45px - 40px);
}

.disabled-text {
  color: #909399;
}

</style>
<style lang="less">
.df-upload-drawer {
  .ed-drawer__body {
    padding: 0;
  }
}
</style>

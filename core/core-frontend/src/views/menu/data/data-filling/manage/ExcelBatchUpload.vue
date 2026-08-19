<script setup lang="tsx">
import icon_upload_outlined from "@/assets/svg/icon_upload_outlined.svg";
import icon_close_outlined from "@/assets/svg/icon_close_outlined.svg";
import icon_edit_outlined from "@/assets/svg/icon_edit_outlined.svg";
import ExcelInfo from "@/views/visualized/data/datasource/ExcelInfo.vue";
import type {ColumnItem} from "../data-filling";
import {Icon} from "@/components/icon-custom";
import {useI18n} from "@/hooks/web/useI18n";
import {computed, nextTick, onBeforeUnmount, onMounted, ref} from "vue";
import {
  ElButton,
  ElCheckbox,
  ElFormItem,
  ElMessage,
} from "element-plus-secondary";
import {debounce, every, filter, join, map} from "lodash-es";
import {
  confirmUpload,
  DfFormItem,
  downloadExcelTemplate,
  formatDate,
  uploadDfExcel,
} from "../data-filling";
import GridTable from "@/components/grid-table/src/GridTable.vue";
import {appendConfirmUpload} from "../fill/fill_api";

const props = withDefaults(
    defineProps<{
      formId: string;
      formName: string;
      columns: Array<ColumnItem>;
      taskInstanceId?: string;
    }>(),
    {
      columns: () => [],
    }
);

const {t} = useI18n();

const upload = ref();
const uploadAgain = ref();

const excelName = ref(undefined);
const dataList = ref<Array<any>>([]);
const excelId = ref(undefined);
const excelPath = ref(undefined);

const status = ref(false);

const sheetFile = computed(() => {
  return {
    name: excelName.value,
    size: excelName.value,
  };
});

const generatedColumns = computed(() => {
  return props.columns.map((ele) => ({
    key: ele.props,
    dataKey: ele.props,
    title: ele.label,
    width: 150,
    headerCellRenderer: ({column, columnIndex}) => {
      const c = props.columns[columnIndex];
      return (
          <div class="flex-align-center icon">
          <span
              class="ellipsis"
              title={column.title}
              style={{width: "100px"}}
          >
            {column.title}{" "}
            {c.rangeIndex === 0 ? (
                <span>{t("data_fill.data.start")}</span>
            ) : c.rangeIndex === 1 ? (
                <span>{t("data_fill.data.end")}</span>
            ) : (
                <span/>
            )}
          </span>
          </div>
      );
    },
    cellRenderer: ({cellData, columnIndex}) => {
      const c = props.columns[columnIndex];
      if (c.date) {
        const _str = formatDate(cellData, c.dateType);
        return (
            <div
                style="color: black; overflow: hidden; text-overflow:ellipsis; white-space: nowrap;"
                title={cellData}
            >
              {_str}
            </div>
        );
      } else if (
          ((c.type === "select" && c.multiple) || c.type === "checkbox") &&
          cellData
      ) {
        const _list: Array<any> = JSON.parse(cellData);
        const _str = join(_list, "; ");
        return (
            <div
                style="color: black; overflow: hidden; text-overflow:ellipsis; white-space: nowrap;"
                title={cellData}
            >
              {_str}
            </div>
        );
      }
      return (
          <div
              style="color: black; overflow: hidden; text-overflow:ellipsis; white-space: nowrap;"
              title={cellData}
          >
            {cellData}
          </div>
      );
    },
  }));
});

const fileList = ref();
const loading = ref(false);

const onChange = (file) => {
  fileList.value = file;
};
const uploadFail = (response) => {
  let myError = response.toString();
  myError.replace("Error: ", "");
};

const handleExcelDel = () => {
  dataList.value = [];
  excelName.value = undefined;
  excelId.value = undefined;
  excelPath.value = undefined;
};

const componentList = ref({
  input: {
    type: "input",
    typeName: t("common.component.input"),
    icon: "icon_single_line_outlined",
  },
  textarea: {
    type: "textarea",
    typeName: t("common.component.textarea"),
    icon: "icon_multi_line_outlined",
  },
  select: {
    type: "select",
    typeName: t("common.component.select"),
    icon: "icon_down_outlined",
  },
  radio: {
    type: "radio",
    typeName: t("common.component.radio"),
    icon: "icon_radio_outlined",
  },
  checkbox: {
    type: "checkbox",
    typeName: t("common.component.checkbox"),
    icon: "icon_todo_outlined",
  },
  date: {
    type: "date",
    typeName: t("common.component.date"),
    icon: "icon_calendar_outlined",
  },
  dateRange: {
    type: "dateRange",
    typeName: t("common.component.dateRange"),
    icon: "icon_calendar_outlined",
  },
});

const uploadType = ref<0, 1>(0);

const ruleVisible = ref(false);

const ruleSetting = ref([]);
const ruleSettingTemp = ref([]);

const ruleSettingNameStr = computed(() => {
  return join(
      map(
          filter(ruleSetting.value, (r) => r.checked),
          (s) => s.name
      ),
      ", "
  );
});

function toSetUploadRule() {
  if (ruleSetting.value.length === 0) {
    props.columns.forEach((c) => {
      if (!c.date) {
        ruleSetting.value.push({
          checked: false,
          name: c.label,
          props: c.props,
          type: c.type,
          icon: componentList.value[c.type]?.icon,
          component: componentList.value[c.type]?.typeName,
        });
      }
    });
  }
  ruleSettingTemp.value = JSON.parse(JSON.stringify(ruleSetting.value));
  ruleVisible.value = true;
}

function closeRuleDialog() {
  ruleVisible.value = false;
}

function saveRuleDialog() {
  if (every(ruleSettingTemp.value, (r) => r.checked)) {
    ElMessage.warning(t("data_fill.data.cannot_select_all"));
    return;
  }
  ruleSetting.value = JSON.parse(JSON.stringify(ruleSettingTemp.value));
  closeRuleDialog();
}

function downloadTemplate() {
  loading.value = true;
  downloadExcelTemplate(props.formId)
      .then((res) => {
        const blobData = res.data;
        const temp = res.headers["content-disposition"]
            ?.split(";")[1]
            ?.split("filename*=utf-8''")[1];
        const fileName = temp
            ? decodeURIComponent(temp)
            : `${props.formName}.xlsx`;
        const blob = new Blob([blobData], {
          type:
              res.headers["content-type"] ??
              "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8",
        });
        const link = document.createElement("a");
        link.style.display = "none";
        link.href = URL.createObjectURL(blob);
        link.download = fileName; // 下载的文件名
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
      })
      .finally(() => {
        loading.value = false;
      });
}

const beforeUpload = () => {
  loading.value = true;
};

const uploadExcel = () => {
  const formData = new FormData();
  formData.append("file", fileList.value.raw);
  loading.value = true;
  return uploadDfExcel(props.formId, formData)
      .then((res) => {
        upload.value?.clearFiles();
        uploadAgain.value?.clearFiles();
        uploadSuccess(res);
        loading.value = false;
      })
      .catch((error) => {
        if (error.code === "ECONNABORTED") {
          ElMessage({
            type: "error",
            message: error.message,
            showClose: true,
          });
        }
        loading.value = false;
      });
};

function uploadSuccess(res) {
  if (res?.code !== 0) {
    ElMessage.warning(res.msg);
    return;
  }
  loading.value = false;

  excelName.value = res.data.excelName;
  excelId.value = res.data.id;
  excelPath.value = res.data.path;

  const _list: Array<any> = map(res.data.dataList, (d) => d.data);

  dataList.value = _list;
}

const emit = defineEmits(["close", "finish"]);

function close() {
  emit("close");
}

function save() {
  loading.value = true;
  const param = {
    excelId: excelId.value,
    excelName: excelName.value,
    excelPath: excelPath.value,
  };

  if (props.taskInstanceId) {
    appendConfirmUpload(props.taskInstanceId, props.formId, param)
        .then((res) => {
          if (res.code === 0) {
            emit("finish");
          }
        })
        .finally(() => {
          loading.value = false;
        });
  } else {
    confirmUpload(props.formId, param)
        .then((res) => {
          if (res.code === 0) {
            emit("finish");
          }
        })
        .finally(() => {
          loading.value = false;
        });
  }
}

const isResize = ref(true);

const handleResize = debounce(() => {
  isResize.value = false;
  nextTick(() => {
    isResize.value = true;
  });
}, 500);
onMounted(() => {
  window.addEventListener("resize", handleResize);
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", handleResize);
});
</script>

<template>
  <div class="df-excel-detail" v-loading="loading">
    <el-main class="main-area">
      <div class="detail-inner">
        <el-form
            ref="excelForm"
            require-asterisk-position="right"
            label-position="top"
            v-loading="loading"
        >
          <el-form-item
              v-if="sheetFile.name"
              prop="id"
              label="文件"
              key="sheetFile"
              :rules="[
              {
                required: true,
              },
            ]"
          >
            <ExcelInfo
                @del="handleExcelDel"
                show-del
                :name="sheetFile.name"
                :size="sheetFile.size"
            />
            <el-upload
                action=""
                :multiple="false"
                ref="uploadAgain"
                :show-file-list="false"
                accept=".xls,.xlsx"
                :before-upload="beforeUpload"
                :on-change="onChange"
                :http-request="uploadExcel"
                :on-error="uploadFail"
                name="file"
            >
              <template #trigger>
                <el-button text>重新上传</el-button>
              </template>
            </el-upload>
          </el-form-item>
          <el-form-item
              v-else
              prop="id"
              key="sheetId"
              label="文件"
              :rules="[
              {
                required: true,
              },
            ]"
          >
            <el-upload
                :multiple="false"
                action=""
                ref="upload"
                :show-file-list="false"
                accept=".xls,.xlsx"
                :before-upload="beforeUpload"
                :on-change="onChange"
                :http-request="uploadExcel"
                :on-error="uploadFail"
                name="file"
            >
              <template #trigger>
                <el-button secondary>
                  <template #icon>
                    <Icon name="icon_upload_outlined">
                      <icon_upload_outlined class="svg-icon"/>
                    </Icon>
                  </template>
                  {{ t("dataset.upload_file") }}
                </el-button>
              </template>
            </el-upload>
            <p class="upload-tip" style="width: 100%">
              仅支持xls,xlsx格式的文件
            </p>
            <p class="upload-tip" style="width: 100%">
              上传数据需要与表单表头保持一致，如有需要，请点击<a
                @click="downloadTemplate"
                class="download-link"
            >下载模版</a
            >
            </p>
            <div class="ed-form-item__error" v-if="status">请上传文件</div>
          </el-form-item>

          <!--          <el-form-item
              label="提交方式"
              :rules="[
            {
              required: true
            }]">
            <el-radio-group size="small" v-model="uploadType">
              <el-radio :label="0">数据追加</el-radio>
              <el-radio :label="1">数据更新</el-radio>
            </el-radio-group>
          </el-form-item>-->

          <template v-if="uploadType === 1">
            <div style="font-size: 12px">
              更新规则设置
              <el-button text @click="toSetUploadRule">
                <template #icon>
                  <Icon name="icon_edit_outlined">
                    <icon_edit_outlined class="svg-icon"/>
                  </Icon>
                </template>
              </el-button>
            </div>
            <div style="font-size: 12px">
              设置字段组件: {{ ruleSettingNameStr }}
            </div>
          </template>
        </el-form>
        <template v-if="dataList.length > 0">
          <div class="title-form_primary">
            {{ t("chart.data_preview") }}
          </div>

          <div class="info-table" v-if="isResize">
            <el-auto-resizer>
              <template #default="{ height, width }">
                <el-table-v2
                    :columns="generatedColumns"
                    header-class="excel-header-cell"
                    :data="dataList"
                    :width="width"
                    :height="height"
                    fixed
                />
              </template>
            </el-auto-resizer>
          </div>
        </template>
      </div>
    </el-main>
    <el-footer>
      <div class="upload-footer">
        <el-button @click="close()">{{ t("commons.cancel") }}</el-button>
        <el-button type="primary" :disabled="!excelId" @click="save()">
          {{ t("commons.confirm") }}
        </el-button>
      </div>
    </el-footer>

    <el-dialog
        v-model="ruleVisible"
        width="840"
        destroy-on-close
        class="reci-dialog"
        align-center
        :close-on-click-modal="false"
    >
      <div class="reci-dialog-container">
        <div class="reci-dialog-header">
          <span>{{ t("data_fill.task.template_setting") }}</span>
          <el-icon @click="closeRuleDialog" class="dialog-close">
            <Icon name="icon_close_outlined">
              <icon_close_outlined class="svg-icon"/>
            </Icon>
          </el-icon>
        </div>
        <div class="reci-dialog-main template-setting">
          <div
              style="
              padding: 12px;
              margin-bottom: 12px;
              background: #d0defd;
              height: 87px;
            "
          >
            <div>
              提交表单数据时，将选择的组件作为主键，对表单中的已有数据进行匹配更新，更新规则如下：
            </div>
            <div>
              1、当新提交的数据，与表单中已有数据主键相匹配时，则更新表单已有数据；
            </div>
            <div>
              2、当新提交的数据，与表单中已有数据主键匹配不上时，则将新提交的数据插入表单。
            </div>
          </div>

          <div style="height: calc(100% - 99px)">
            <GridTable
                ref="multipleTableRef"
                :table-data="ruleSettingTemp"
                style="width: 100%; height: 100%"
                :show-pagination="false"
            >
              <el-table-column>
                <template #default="scope">
                  <el-checkbox v-model="scope.row.checked"/>
                </template>
              </el-table-column>
              <el-table-column :label="t('data_fill.task.component')">
                <template #default="scope">
                  {{ scope.row.name }}
                </template>
              </el-table-column>
              <el-table-column :label="t('data_fill.form.component')">
                <template #default="scope">
                  {{ scope.row.component }}
                </template>
              </el-table-column>
            </GridTable>
          </div>
        </div>
        <div class="reci-dialog-footer">
          <el-button secondary @click="closeRuleDialog">{{
              t("commons.cancel")
            }}
          </el-button>
          <el-button type="primary" @click="saveRuleDialog">{{
              t("commons.confirm")
            }}
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped lang="less">
.df-excel-detail {
  display: flex;
  justify-content: center;
  flex-direction: column;
  height: 100%;

  .download-link {
    color: var(--ed-color-primary);
    cursor: pointer;
  }

  .main-area {
    display: flex;
    justify-content: center;
  }

  .ed-form-item {
    margin-bottom: 16px;
  }

  .detail-operate {
    height: 56px;
    padding: 16px 24px;
    font-size: 16px;
    font-weight: 500;
    width: 100%;
    border-bottom: 1px solid rgba(31, 35, 41, 0.15);
  }

  .detail-inner {
    width: 800px;
    padding-top: 16px;
    height: calc(100vh - 280px);

    .dropdown-icon {
      .down-outlined {
        transform: rotate(180deg);
      }

      &[aria-expanded="true"] {
        .down-outlined {
          transform: rotate(0);
        }
      }

      cursor: pointer;
    }

    .error-status {
      margin-top: 32px;
    }

    .upload-tip {
      color: #8f959e;
      font-family: var(--de-custom_font, "PingFang");
      font-size: 14px;
      font-style: normal;
      font-weight: 400;
      line-height: 22px;
    }

    .title-form_primary {
      margin: 16px 0;
      margin-top: 32px;
    }

    .info-table {
      width: 100%;
      height: calc(100%);
    }
  }

  .upload-footer {
    display: flex;
    flex-direction: row;
    justify-content: flex-end;
  }

  :deep(.reci-dialog) {
    height: 580px !important;

    .ed-dialog__header {
      display: none !important;
    }

    .ed-dialog__body {
      height: 100% !important;

      .reci-dialog-container {
        height: 100% !important;

        .reci-dialog-header {
          display: flex;
          justify-content: space-between;
          align-items: center;

          span {
            height: 24px;
            line-height: 24px;
            font-family: var(--de-custom_font, "PingFang");
            font-size: 16px;
            font-weight: 500;
            line-height: 24px;
            text-align: left;
            color: #1f2329;
          }

          i {
            width: 20px;
            height: 20px;
            color: #646a73;
            padding: 3px;
            cursor: pointer;
          }
        }

        .template-setting {
          border: unset !important;
          display: flex;
          flex-direction: column;
        }

        .reci-dialog-main {
          display: flex;
          height: 428px;
          margin: 24px 0;
          border-radius: 4px;
          border: 1px solid #dee0e3;

          .reci-left {
            height: 100%;
            width: 50%;
            border-right: 1px solid #dee0e3;

            .search-head {
              height: 56px;
              padding: 16px 16px 8px;
            }

            .entity-parent-container {
              height: 76px;
              width: 100%;

              .parent-item {
                width: 100%;
                display: flex;
                height: 38px;
                line-height: 38px;
                padding: 0 16px;
                align-items: center;
                cursor: pointer;

                &:hover {
                  background-color: #1f23291a;
                }

                i {
                  width: 16px;
                  height: 16px;
                }

                .role-icon {
                  color: #f78704;
                }

                .people-icon {
                  color: var(--ed-color-primary, #3370ff);
                }

                span {
                  margin-left: 8px;
                  height: 22px;
                  line-height: 22px;
                  width: calc(100% - 40px);
                }
              }
            }

            .entity-kid-container {
              margin-top: 4px;
              height: calc(100% - 60px);

              .kid-title {
                height: 22px;
                line-height: 22px;
                margin-bottom: 4px;
                padding: 0 16px;
                display: flex;
                color: #646a73;
                align-items: center;

                .kid-title-first {
                  height: 22px;
                  line-height: 22px;
                  width: 50px;
                  border-radius: 2px;
                  padding: 0 4px;
                  cursor: pointer;

                  &:hover {
                    background-color: var(--ed-color-primary-1a, #3370ff1a);
                    color: var(--ed-color-primary, #3370ff);
                  }
                }

                .kid-title-i {
                  width: 12px;
                  height: 12px;
                }

                .kid-title-span {
                  padding: 0 4px;
                  color: #1f2329 !important;
                }
              }

              .select-all {
                padding-left: 16px;
                height: 38px;
                display: flex;
                align-items: center;
                cursor: pointer;

                &:hover {
                  background-color: #1f23291a;
                }
              }

              .kid-content {
                height: calc(100% - 64px);
              }
            }
          }

          .reci-right {
            height: 100%;
            width: 50%;

            .reci-selected-head {
              height: 56px;
              padding: 22px 16px 12px;
              display: flex;
              align-items: center;
              justify-content: space-between;

              :nth-child(1) {
                height: 22px;
                line-height: 22px;
                font-size: 14px;
                font-family: var(--de-custom_font, "PingFang");
                color: #1f2329;
              }

              :nth-child(2) {
                height: 26px;
                line-height: 26px;
                border-radius: 4px;
                padding: 0 4px;
                color: var(--ed-color-primary, #3370ff);

                &:hover {
                  background-color: var(--ed-color-primary-1a, #3370ff1a);
                }
              }
            }

            .reci-selected-content {
              height: calc(100% - 56px);
            }
          }
        }

        .reci-dialog-footer {
          height: 32px;
          float: right;
        }
      }
    }
  }
}
</style>

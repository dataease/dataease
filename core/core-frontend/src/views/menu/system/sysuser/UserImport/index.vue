<script lang="ts" setup>
import icon_warning_filled from "@/assets/svg/icon_warning_filled.svg";
import icon_upload_outlined from "@/assets/svg/icon_upload_outlined.svg";
import icon_fileExcel_colorful from "@/assets/svg/icon_file-excel_colorful.svg";
import { ref, reactive, h } from "vue";
import {
  ElMessage,
  ElMessageBox,
  ElLoading,
  UploadRequestOptions,
  UploadProps,
  ElButton,
} from "element-plus-secondary";
import { useI18n } from "@/hooks/web/useI18n";
import {
  downExcelTemplateApi,
  importUserApi,
  downErrorRecordApi,
  clearErrorApi,
} from "@/api/user";
const { t } = useI18n();
const defaultTip = t("userimport.defaultTip");
const loadingInstance = ref(null);
const dialogShow = ref(false);
const form = ref({});
const file = ref(null);
const fileName = ref("");
const errorFileKey = ref(null);
const emits = defineEmits(["refresh-grid"]);
const state = reactive({
  errList: [],
  filesTmp: [],
});

const showLoading = () => {
  loadingInstance.value = ElLoading.service({ target: ".user-import-class" });
};
const closeLoading = () => {
  loadingInstance.value?.close();
};
const showDialog = () => {
  file.value = null;
  fileName.value = null;
  errorFileKey.value = null;
  dialogShow.value = true;
};
const closeDialog = () => {
  dialogShow.value = false;
};
const handleExceed: UploadProps["onExceed"] = () => {
  ElMessage.warning(t("userimport.exceedMsg"));
};
const handleError = () => {
  ElMessage.warning(t("user.contact_the_administrator"));
};
const uploadValidate = (file) => {
  const suffix = file.name.substring(file.name.lastIndexOf(".") + 1);
  if (suffix !== "xlsx" && suffix !== "xls") {
    ElMessage.warning(t("userimport.suffixMsg"));
    return false;
  }

  if (file.size / 1024 / 1024 > 10) {
    ElMessage.warning(t("userimport.limitMsg"));
    return false;
  }
  state.errList = [];
  return true;
};
const setFile = (options: UploadRequestOptions) => {
  file.value = options.file;
  fileName.value = options.file.name;
};

const buildFormData = (file, files, param) => {
  const formData = new FormData();
  if (file) {
    formData.append("file", file);
  }
  if (files) {
    files.forEach((f) => {
      formData.append("files", f);
    });
  }
  if (param) {
    formData.append(
      "request",
      new Blob([JSON.stringify(param)], { type: "application/json" })
    );
  }

  return formData;
};
const downExcel = () => {
  showLoading();
  downExcelTemplateApi()
    .then((res) => {
      const blobData = res.data;
      const blob = new Blob([blobData], { type: "application/vnd.ms-excel" });
      const link = document.createElement("a");
      link.style.display = "none";
      link.href = URL.createObjectURL(blob);
      link.download = "user.xlsx"; // 下载的文件名
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      closeLoading();
    })
    .catch(() => {
      closeLoading();
    });
};

const toGrid = () => {
  file.value = null;
  fileName.value = "";
  dialogShow.value = false;
  emits("refresh-grid");
};
const sure = () => {
  const param = buildFormData(file.value, null, null);
  showLoading();
  importUserApi(param)
    .then((res) => {
      closeLoading();
      const data = res.data;
      errorFileKey.value = data.dataKey;
      closeDialog();
      showTips(data.successCount, data.errorCount);
    })
    .catch(() => {
      closeLoading();
    });
};
const downErrorExcel = () => {
  if (errorFileKey.value) {
    showLoading();
    downErrorRecordApi(errorFileKey.value)
      .then((res) => {
        const blobData = res.data;
        const blob = new Blob([blobData], { type: "application/vnd.ms-excel" });
        const link = document.createElement("a");
        link.style.display = "none";
        link.href = URL.createObjectURL(blob);
        link.download = "error.xlsx"; // 下载的文件名
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        closeLoading();
        // closeDialog()
      })
      .catch(() => {
        closeLoading();
      });
  }
};
const showTips = (successCount, errorCount) => {
  let title = !errorCount
    ? t("user.data_import_successful")
    : successCount
    ? t("user.data_import_failed")
    : t("user.data_import_failed_de");
  const childrenDomList = [
    h("strong", null, title),
    h("br", null, null),
    h("span", null, t("user.imported_1_data", { msg: successCount })),
  ];
  if (errorCount) {
    const errorCountDom = h(
      "span",
      null,
      t("user.import_1_data", { msg: errorCount })
    );
    const errorDom = h("div", { class: "error-record-tip flex-align-center" }, [
      h("span", null, t("user.can")),
      h(
        ElButton,
        {
          onClick: downErrorExcel,
          type: "primary",
          text: true,
          class: "down-button",
        },
        t("user.download_error_report")
      ),
      h("span", null, t("user.modify_and_re_import")),
    ]);

    childrenDomList.push(errorCountDom);
    childrenDomList.push(errorDom);
  }
  ElMessageBox.confirm("", {
    confirmButtonType: "primary",
    type: !errorCount ? "success" : successCount ? "warning" : "error",
    autofocus: false,
    dangerouslyUseHTMLString: true,
    message: h("div", { class: "import-tip-box" }, childrenDomList),
    showClose: false,
    cancelButtonText: t("user.return_to_view"),
    confirmButtonText: t("user.continue_importing"),
  })
    .then(() => {
      clearErrorRecord();
      showDialog();
      emits("refresh-grid");
    })
    .catch(() => {
      clearErrorRecord();
      toGrid();
    });
};
const clearErrorRecord = () => {
  if (errorFileKey.value) {
    clearErrorApi(errorFileKey.value);
  }
};
</script>
<template>
  <div style="display: inline-block; margin-left: 15px">
    <el-button @click="showDialog">{{ t("userimport.buttonText") }}</el-button>
  </div>
  <el-dialog
    v-model="dialogShow"
    :title="t('userimport.buttonText')"
    width="600px"
    class="user-import-class"
    v-if="dialogShow"
    @close="closeDialog"
  >
    <div class="down-template">
      <span class="icon-span">
        <el-icon>
          <Icon name="icon_warning_filled"
            ><icon_warning_filled class="svg-icon"
          /></Icon>
        </el-icon>
      </span>
      <div class="down-template-content">
        <span>{{ t("userimport.first_please") }}</span>
        <el-button type="primary" text class="down-button" @click="downExcel">{{
          t("userimport.downTip")
        }}</el-button>
        <span>{{ t("userimport.fill_and_upload") }}</span>
      </div>
    </div>
    <el-form ref="form" class="import-form" :model="form" label-width="0px">
      <el-form-item label="" style="margin-bottom: 0px">
        <el-upload
          class="upload-user"
          action=""
          accept=".xlsx,.xls"
          :on-exceed="handleExceed"
          :before-upload="uploadValidate"
          :on-error="handleError"
          :show-file-list="false"
          :file-list="state.filesTmp"
          :http-request="setFile"
        >
          <el-input
            :placeholder="t('userimport.placeholder')"
            readonly
            v-model="fileName"
          >
            <template #suffix>
              <el-icon>
                <Icon name="icon_upload_outlined"
                  ><icon_upload_outlined class="svg-icon"
                /></Icon>
              </el-icon>
            </template>
            <template #prefix>
              <el-icon v-if="!!fileName">
                <Icon name="icon_file-excel_colorful"
                  ><icon_fileExcel_colorful class="svg-icon"
                /></Icon>
              </el-icon>
            </template>
          </el-input>
        </el-upload>

        <span style="float: left">
          <span>
            <el-link class="font12" type="info" disabled>
              {{ defaultTip }}
            </el-link>
          </span>
        </span>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="closeDialog">{{ t("common.cancel") }}</el-button>
        <el-button
          :type="file && fileName ? 'primary' : 'info'"
          @click="sure"
          :disabled="!file || !fileName"
          >{{ t("userimport.import") }}</el-button
        >
      </span>
    </template>
  </el-dialog>
</template>

<style lang="less">
.upload-user {
  width: 100%;
  height: 32px;
  .ed-upload {
    width: 100% !important;
  }
}
.color-danger {
  :deep(.el-link--inner) {
    color: var(--deDanger, #f54a45) !important;
  }
}
.font12 {
  color: #8f959e !important;
  font-family: var(--de-custom_font, "PingFang");
  font-size: 14px;
  font-style: normal;
  font-weight: 400;
  line-height: 22px;
}
.down-template {
  display: flex;
  flex-direction: row;
  width: 100%;
  height: 40px;
  line-height: 40px;
  background: #d6e2ff;
  border-radius: 4px;
  padding-left: 10px;
  .icon-span {
    color: var(--ed-color-primary);
    font-size: 18px;
    i {
      top: 3px;
    }
  }
  .down-template-content {
    font-size: 14px;
    display: flex;
    flex-direction: row;
    margin-left: 10px;
    .down-button {
      height: 40px;
    }
  }
}
.import-form {
  margin-top: 16px;
}
.import-tip-box {
  strong {
    font-size: 16px;
  }
  span {
    font-size: 13px;
  }
  .error-record-tip {
    font-size: 13px;
    flex-flow: wrap;
  }
}
</style>

<template>
  <el-upload
    class="upload-plugin"
    ref="handler"
    action=""
    accept=".jar"
    :on-exceed="handleExceed"
    :before-upload="uploadValidate"
    :on-error="handleError"
    :show-file-list="false"
    :http-request="setFile"
    :on-success="onSuccess"
  >
    <slot />
  </el-upload>
</template>

<script lang="ts" setup>
import { ref } from "vue";
import { ElMessage } from "element-plus-secondary";
import { useI18n } from "@/hooks/web/useI18n";
import type { UploadRequestOptions, UploadProps } from "element-plus-secondary";
import request from "@/config/axios";
import { propTypes } from "@/utils/propTypes";
const props = defineProps({
  isEdit: propTypes.bool.def(false),
  id: propTypes.string,
});
const { t } = useI18n();

const handler = ref();
const file = ref();

const handleExceed: UploadProps["onExceed"] = () => {
  ElMessage.warning(t("userimport.exceedMsg"));
};
const handleError = () => {
  ElMessage.warning(t("system.contact_the_administrator"));
};
const setFile = (options: UploadRequestOptions) => {
  file.value = options.file;
  return executeHandler();
};

const executeHandler = () => {
  let param = null;
  let url = "/plugin/install";
  if (props.isEdit) {
    param = {
      id: props.id,
    };
    url = "/plugin/update";
  }
  const data = buildFormData(file.value, param);
  return request.post({
    url,
    headersType: "multipart/form-data;",
    data,
  });
};
const uploadValidate = (file) => {
  const suffix = file.name.substring(file.name.lastIndexOf(".") + 1);
  if (suffix !== "jar") {
    ElMessage.warning(t("system.can_be_uploaded"));
    return false;
  }

  if (file.size / 1024 / 1024 > 200) {
    ElMessage.warning(t("system.maximum_upload_200m"));
    return false;
  }
  return true;
};
const buildFormData = (file, param) => {
  const formData = new FormData();
  if (file) {
    formData.append("file", file);
  }
  if (param) {
    formData.append(
      "request",
      new Blob([JSON.stringify(param)], { type: "application/json" })
    );
  }
  return formData;
};

const emits = defineEmits(["onSuccess"]);
const onSuccess = () => {
  emits("onSuccess");
};
</script>

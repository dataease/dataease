<template>
  <div>
    <el-row type="flex" justify="center">
      <el-col>
        <el-table class="basic-config" :data="tableData">
          <el-table-column
            prop="name"
            :label="$t('load_test.file_name')">
          </el-table-column>
          <el-table-column
            prop="size"
            :label="$t('load_test.file_size')">
          </el-table-column>
          <el-table-column
            prop="type"
            :label="$t('load_test.file_type')">
          </el-table-column>
          <el-table-column
            :label="$t('test_track.case.upload_time')">
            <template v-slot:default="scope">
              <i class="el-icon-time"/>
              <span class="last-modified">{{ scope.row.updateTime | timestampFormatDate }}</span>
            </template>
          </el-table-column>
          <el-table-column
            :label="$t('commons.operating')">
            <template v-slot:default="scope">
              <el-button @click="preview(scope.row)" :disabled="!scope.row.id || readOnly" type="primary"
                         v-if="isPreview(scope.row)"
                         icon="el-icon-view"
                         size="mini" circle/>
              <el-button @click="handleDownload(scope.row)" :disabled="!scope.row.id || readOnly" type="primary"
                         icon="el-icon-download"
                         size="mini" circle/>
              <el-button :disabled="readOnly || !isDelete" @click="handleDelete(scope.row, scope.$index)" type="danger"
                         icon="el-icon-delete" size="mini"
                         circle/>
            </template>
          </el-table-column>
        </el-table>
      </el-col>
    </el-row>

    <test-case-file ref="testCaseFile"/>
  </div>
</template>

<script>
import TestCaseFile from "@/business/components/track/case/components/TestCaseFile";
import {Message} from "element-ui";

export default {
  name: "TestCaseAttachment",
  components: {TestCaseFile},
  props: {
    tableData: Array,
    readOnly: {
      type: Boolean,
      default: true
    },
    isDelete: {
      type: Boolean,
      default: false
    },
  },
  data() {
    return {

    }
  },
  methods: {
    preview(row) {
      this.$refs.testCaseFile.open(row);
    },
    isPreview(row) {
      const fileType = row.type;
      return fileType === 'JPG' || fileType === 'JPEG' || fileType === 'PDF' || fileType === 'PNG';
    },
    handleDownload(file) {
      let data = {
        name: file.name,
        id: file.id,
      };
      let config = {
        url: '/test/case/file/download',
        method: 'post',
        data: data,
        responseType: 'blob'
      };
      this.result = this.$request(config).then(response => {
        const content = response.data;
        const blob = new Blob([content]);
        if ("download" in document.createElement("a")) {
          // 非IE下载
          //  chrome/firefox
          let aTag = document.createElement('a');
          aTag.download = file.name;
          aTag.href = URL.createObjectURL(blob);
          aTag.click();
          URL.revokeObjectURL(aTag.href)
        } else {
          // IE10+下载
          navigator.msSaveBlob(blob, this.filename)
        }
      }).catch(e => {
        Message.error({message: e.message, showClose: true});
      });
    },
    handleDelete(file, index) {
      this.$emit("handleDelete", file, index);
    },
  }
}
</script>

<style scoped>

</style>

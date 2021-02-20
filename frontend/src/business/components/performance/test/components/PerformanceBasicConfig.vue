<template>
  <div v-loading="result.loading">
    <el-upload
      accept=".jmx,.csv,.jar"
      drag
      action=""
      :limit="fileNumLimit"
      multiple
      :show-file-list="false"
      :before-upload="beforeUpload"
      :http-request="handleUpload"
      :on-exceed="handleExceed"
      :disabled="isReadOnly"
      :file-list="fileList">
      <i class="el-icon-upload"/>
      <div class="el-upload__text" v-html="$t('load_test.upload_tips')"></div>
      <template v-slot:tip>
        <div class="el-upload__tip">{{ $t('load_test.upload_type') }}</div>
      </template>
    </el-upload>

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
        :label="$t('load_test.last_modify_time')">
        <template v-slot:default="scope">
          <i class="el-icon-time"/>
          <span class="last-modified">{{ scope.row.updateTime | timestampFormatDate }}</span>
        </template>
      </el-table-column>
      <el-table-column
        :label="$t('commons.operating')">
        <template v-slot:default="scope">
          <el-button @click="handleDownload(scope.row)" :disabled="!scope.row.id || isReadOnly" type="primary"
                     icon="el-icon-download"
                     size="mini" circle/>
          <el-button :disabled="isReadOnly" @click="handleDelete(scope.row, scope.$index)" type="danger"
                     icon="el-icon-delete" size="mini"
                     circle/>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import {Message} from "element-ui";
import {findThreadGroup} from "@/business/components/performance/test/model/ThreadGroup";

export default {
  name: "PerformanceBasicConfig",
  props: {
    test: {
      type: Object
    },
    isReadOnly: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      result: {},
      getFileMetadataPath: "/performance/file/metadata",
      jmxDownloadPath: '/performance/file/download',
      jmxDeletePath: '/performance/file/delete',
      fileList: [],
      tableData: [],
      uploadList: [],
      fileNumLimit: 10,
    };
  },
  created() {
    if (this.test.id) {
      this.getFileMetadata(this.test)
    }
  },
  watch: {
    test() {
      if (this.test.id) {
        this.getFileMetadata(this.test)
      }
    },
    uploadList() {
      let self = this;
      let fileList = self.uploadList.filter(f => f.name.endsWith(".jmx"));
      if (fileList.length > 0) {
        let file = fileList[0];
        let jmxReader = new FileReader();
        jmxReader.onload = function (event) {
          let threadGroups = findThreadGroup(event.target.result);
          self.$emit('fileChange', threadGroups);
        };
        jmxReader.readAsText(file);
      }
    }
  },
  methods: {
    getFileMetadata(test) {
      this.fileList = [];
      this.tableData = [];
      this.uploadList = [];
      this.result = this.$get(this.getFileMetadataPath + "/" + test.id, response => {
        let files = response.data;

        if (!files) {
          Message.error({message: this.$t('load_test.related_file_not_found'), showClose: true});
          return;
        }
        // deep copy
        this.fileList = JSON.parse(JSON.stringify(files));
        this.tableData = JSON.parse(JSON.stringify(files));
        this.tableData.map(f => {
          f.size = (f.size / 1024).toFixed(2) + ' KB';
        });
      })
    },
    beforeUpload(file) {
      if (!this.fileValidator(file)) {
        /// todo: 显示错误信息
        return false;
      }

      if (this.tableData.filter(f => f.name === file.name).length > 0) {
        this.$error(this.$t('load_test.delete_file'));
        return false;
      }

      let type = file.name.substring(file.name.lastIndexOf(".") + 1);

      this.tableData.push({
        name: file.name,
        size: file.size + ' Bytes', /// todo: 按照大小显示Byte、KB、MB等
        type: type.toUpperCase(),
        updateTime: file.lastModified,
      });

      return true;
    },
    handleUpload(uploadResources) {
      this.uploadList.push(uploadResources.file);
    },
    handleDownload(file) {
      let data = {
        name: file.name,
        id: file.id,
      };
      let config = {
        url: this.jmxDownloadPath,
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
      this.$alert(this.$t('load_test.delete_file_confirm') + file.name + "？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            this._handleDelete(file, index);
          }
        }
      });
    },
    _handleDelete(file, index) {
      this.fileList.splice(index, 1);
      this.tableData.splice(index, 1);
      //
      let i = this.uploadList.findIndex(upLoadFile => upLoadFile.name === file.name);
      if (i > -1) {
        this.uploadList.splice(i, 1);
      }
    },
    handleExceed() {
      this.$error(this.$t('load_test.file_size_limit'));
    },
    fileValidator(file) {
      /// todo: 是否需要对文件内容和大小做限制
      return file.size > 0;
    },
    updatedFileList() {
      return this.fileList;// 表示修改了已经上传的文件列表
    },
    validConfig() {
      let newJmxNum = 0, oldJmxNum = 0, newCsvNum = 0, oldCsvNum = 0, newJarNum = 0, oldJarNum = 0;
      if (this.uploadList.length > 0) {
        this.uploadList.forEach(f => {
          if (f.name.toLowerCase().endsWith(".jmx")) {
            newJmxNum++;
          }
          if (f.name.toLowerCase().endsWith(".csv")) {
            newCsvNum++;
          }
          if (f.name.toLowerCase().endsWith(".jar")) {
            newJarNum++;
          }
        });
      }
      if (this.fileList.length > 0) {
        this.fileList.forEach(f => {
          if (f.name.toLowerCase().endsWith(".jmx")) {
            oldJmxNum++;
          }
          if (f.name.toLowerCase().endsWith(".csv")) {
            oldCsvNum++;
          }
          if (f.name.toLowerCase().endsWith(".jar")) {
            oldJarNum++;
          }
        });
      }
      if (newCsvNum + oldCsvNum + newJarNum + oldJarNum > this.fileNumLimit - 1) {
        this.handleExceed();
        return false;
      }
      if (newJmxNum + oldJmxNum !== 1) {
        this.$error(this.$t('load_test.jmx_is_null'));
        return false;
      }
      return true;
    }
  },
}
</script>

<style scoped>
.basic-config {
  width: 100%
}

.last-modified {
  margin-left: 5px;
}
</style>

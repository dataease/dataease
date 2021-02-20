<template>
  <el-dialog class="testcase-import" :title="$t('test_track.case.import.case_import')" :visible.sync="dialogVisible"
             @close="close">

    <el-tabs v-model="activeName" simple>
      <el-tab-pane :label="$t('test_track.case.import.excel_title')" name="excelImport">

        <el-row>
          <el-link type="primary" class="download-template"
                   @click="downloadTemplate"
          >{{$t('test_track.case.import.download_template')}}
          </el-link>
        </el-row>
        <el-row>
          <el-upload
            v-loading="result.loading"
            :element-loading-text="$t('test_track.case.import.importing')"
            element-loading-spinner="el-icon-loading"
            class="upload-demo"
            multiple
            :limit="1"
            action=""
            :on-exceed="handleExceed"
            :beforeUpload="uploadValidate"
            :on-error="handleError"
            :show-file-list="false"
            :http-request="upload"
            :file-list="fileList">
            <template v-slot:trigger>
              <el-button size="mini" type="success" plain>{{$t('test_track.case.import.click_upload')}}</el-button>
            </template>
            <template v-slot:tip>
              <div class="el-upload__tip">{{$t('test_track.case.import.upload_limit')}}</div>
            </template>
          </el-upload>
        </el-row>


        <el-row>
          <ul>
            <li v-for="errFile in errList" :key="errFile.rowNum">
              {{errFile.errMsg}}
            </li>
          </ul>
        </el-row>
      </el-tab-pane>
      <!-- Xmind 导入 -->
      <el-tab-pane :label="$t('test_track.case.import.xmind_title')" name="xmindImport" style="border: 0px">
        <el-row class="import-row">
          <div class="el-step__icon is-text" style="background-color: #C9E6F8;border-color: #C9E6F8;margin-right: 10px">
            <div class="el-step__icon-inner">1</div>
          </div>
          <label class="ms-license-label">{{$t('test_track.case.import.import_desc')}}</label>
        </el-row>
        <el-row class="import-row">
          <el-card :body-style="{ padding: '0px' }">
            <img src="../../../../../assets/xmind.jpg"
                 class="testcase-import-img">
          </el-card>

        </el-row>
        <el-row class="import-row">
          <div class="el-step__icon is-text"
               style="background-color: #C9E6F8;border-color: #C9E6F8;margin-right: 10px ">
            <div class="el-step__icon-inner">2</div>
          </div>
          <label class="ms-license-label">{{$t('test_track.case.import.import_file')}}</label>
        </el-row>
        <el-row class="import-row">
          <el-link type="primary" class="download-template"
                   @click="downloadXmindTemplate"
          >{{$t('test_track.case.import.download_template')}}
          </el-link>
        </el-row>
        <el-row class="import-row">
          <el-upload
            v-loading="result.loading"
            :element-loading-text="$t('test_track.case.import.importing')"
            element-loading-spinner="el-icon-loading"
            class="upload-demo"
            multiple
            :limit="1"
            action=""
            :on-exceed="handleExceed"
            :beforeUpload="uploadValidateXmind"
            :on-error="handleError"
            :show-file-list="false"
            :http-request="uploadXmind"
            :file-list="fileList">
            <template v-slot:trigger>
              <el-button size="mini" type="success" plain>{{$t('test_track.case.import.click_upload')}}</el-button>
            </template>
            <template v-slot:tip>
              <div class="el-upload__tip">{{$t('test_track.case.import.upload_xmind')}}</div>
            </template>
          </el-upload>
        </el-row>
        <el-row>
          <ul>
            <li v-for="errFile in xmindErrList" :key="errFile.rowNum">
              {{errFile.errMsg}}
            </li>
          </ul>
        </el-row>
      </el-tab-pane>

    </el-tabs>
  </el-dialog>
</template>

<script>
  import ElUploadList from "element-ui/packages/upload/src/upload-list";
  import MsTableButton from '../../../../components/common/components/MsTableButton';
  import {getCurrentProjectID, listenGoBack, removeGoBackListener} from "../../../../../common/js/utils";
  import {TokenKey} from '../../../../../common/js/constants';
  import axios from "axios";

  export default {
    name: "TestCaseImport",
    components: {ElUploadList, MsTableButton},
    data() {
      return {
        result: {},
        activeName: 'excelImport',
        dialogVisible: false,
        fileList: [],
        errList: [],
        xmindErrList: [],
        isLoading: false
      }
    },
    methods: {
      handleExceed(files, fileList) {
        this.$warning(this.$t('test_track.case.import.upload_limit_count'));
      },
      uploadValidate(file) {
        let suffix = file.name.substring(file.name.lastIndexOf('.') + 1);
        if (suffix != 'xls' && suffix != 'xlsx') {
          this.$warning(this.$t('test_track.case.import.upload_limit_format'));
          return false;
        }

        if (file.size / 1024 / 1024 > 20) {
          this.$warning(this.$t('test_track.case.import.upload_limit_size'));
          return false;
        }
        this.isLoading = true;
        this.errList = [];
        this.xmindErrList = [];
        return true;
      },
      uploadValidateXmind(file) {
        let suffix = file.name.substring(file.name.lastIndexOf('.') + 1);
        if (suffix != 'xmind') {
          this.$warning(this.$t('test_track.case.import.upload_xmind_format'));
          return false;
        }

        if (file.size / 1024 / 1024 > 20) {
          this.$warning(this.$t('test_track.case.import.upload_limit_size'));
          return false;
        }
        this.isLoading = true;
        this.errList = [];
        this.xmindErrList = [];
        return true;
      },
      handleError(err, file, fileList) {
        this.isLoading = false;
        this.$error(err.message);
      },
      open() {
        listenGoBack(this.close);
        this.projectId = getCurrentProjectID();
        this.dialogVisible = true;
      },
      close() {
        removeGoBackListener(this.close);
        this.dialogVisible = false;
        this.fileList = [];
        this.errList = [];
        this.xmindErrList = [];
      },
      downloadTemplate() {
        this.$fileDownload('/test/case/export/template');
      },
      downloadXmindTemplate() {
        axios.get('/test/case/export/xmindTemplate', {responseType: 'blob'})
          .then(response => {
            let fileName = window.decodeURI(response.headers['content-disposition'].split('=')[1]);
            let link = document.createElement("a");
            link.href = window.URL.createObjectURL(new Blob([response.data]));
            link.download = fileName;
            link.click();
          });
      },
      upload(file) {
        this.isLoading = false;
        this.fileList.push(file.file);
        let user = JSON.parse(localStorage.getItem(TokenKey));

        this.result = this.$fileUpload('/test/case/import/' + this.projectId + '/' + user.id, file.file, null, {}, response => {
          let res = response.data;
          if (res.success) {
            this.$success(this.$t('test_track.case.import.success'));
            this.dialogVisible = false;
            this.$emit("refreshAll");
          } else {
            this.errList = res.errList;
          }
          this.fileList = [];
        }, erro => {
          this.fileList = [];
        });
      },
      uploadXmind(file) {
        this.isLoading = false;
        this.fileList.push(file.file);
        let user = JSON.parse(localStorage.getItem(TokenKey));

        this.result = this.$fileUpload('/test/case/import/' + this.projectId + '/' + user.id, file.file, null, {}, response => {
          let res = response.data;
          if (res.success) {
            this.$success(this.$t('test_track.case.import.success'));
            this.dialogVisible = false;
            this.$emit("refreshAll");
          } else {
            this.xmindErrList = res.errList;
          }
          this.fileList = [];
        }, erro => {
          this.fileList = [];
        });
      }
    }
  }
</script>

<style>
</style>

<style scoped>

  .download-template {
    padding-top: 0px;
    padding-bottom: 10px;
  }

  .import-row {
    padding-top: 20px;
  }

  .testcase-import >>> .el-dialog {
    width: 650px;
  }

  .testcase-import-img {
    width: 614px;
    height: 312px;
    size: 200px;
  }


</style>

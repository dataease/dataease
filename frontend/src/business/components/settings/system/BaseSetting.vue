<template>
  <div v-loading="result.loading">
    {{systemParams}}
    <el-form ref="systemParams" class="demo-form-inline"
             :disabled="show" v-loading="loading" size="small">
      <el-row>
        <el-col v-for="(param,index) in systemParams" :key="index">
          <!--logo upload-->
          <el-form-item :label="$t('system_config.base.logo')"
                        v-if="param.paramKey==='base.logo'">
              <el-upload style="float: left"
                         v-loading="result.loading"
                         class="upload-demo"
                         action=""
                         accept=".jpeg,.jpg,.png,.gif"
                         :on-exceed="handleExceed"
                         :beforeUpload="uploadValidate"
                         :on-error="handleError"
                         :show-file-list="false"
                         :file-list="filesTmp"
                         :http-request="uploadLogo">
                <el-button style="display: inline-block" size="mini" type="success" plain>
                  {{ $t('commons.upload') }}
                </el-button>
              </el-upload>
              <el-button style="float:left;margin-left: 10px;margin-top: 3px" size="mini" type="danger" plain
                         @click="removeValue('base.logo')">
                {{ $t('commons.upload') }}
              </el-button>
              <el-input :disabled="true" v-model="param.fileName"
                        :placeholder="$t('system_config.base.logo_size')+'：135px * 30px'"/>
          </el-form-item>
          <!--title-->
          <el-form-item :label="$t('system_config.base.title')" v-if="param.paramKey==='base.title'">
            <el-input v-model="param.paramValue" placeholder="eg：DateEase"/>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <div>
      <el-button @click="edit" v-if="showEdit" size="small">{{ $t('commons.edit') }}</el-button>
      <el-button type="success" @click="save('systemParams')" v-if="showSave" :disabled="disabledSave" size="small">
        {{ $t('commons.save') }}
      </el-button>
      <el-button @click="cancel" type="info" v-if="showCancel" size="small">{{ $t('commons.cancel') }}</el-button>
    </div>
  </div>
</template>

<script>
  import ElUploadList from "element-ui/packages/upload/src/upload-list";

  export default {
    name: "BaseSetting",
    data() {
      return {
        filesTmp: [],
        suffixes: new Set(['png', 'jpg', 'gif', 'jpeg']),
        files: [],
        systemParams: [],
        systemParamsOld: [],
        input: '',
        visible: true,
        result: {},
        showEdit: true,
        showSave: false,
        showCancel: false,
        show: true,
        disabledConnection: false,
        disabledSave: false,
        loading: false,
        rules: {
          url: [
            {
              required: true,
              message: 'Not Null',
              trigger: ['change', 'blur']
            },
          ],
        }
      }
    },
    created() {
      this.query()
    },
    methods: {
      query() {
        this.result = this.$get("/system/base/info", response => {
          this.systemParams = response.data;
        })
      },
      edit() {
        this.showEdit = false;
        this.showSave = true;
        this.showCancel = true;
        this.show = false;
      },
      save() {
        this.showEdit = true;
        this.showCancel = false;
        this.showSave = false;
        this.show = true;
        this.systemParams.forEach((param) => {
          if (param.file !== null) {
            let file = param.file;
            let name = file.name + "," + param.paramKey;
            let newfile = new File([file], name, {type: file.type});
            this.files.push(newfile);
            param.file = null;
          }
        });

        this.result = this.$fileUpload("/system/save/base", null, this.files, {"systemParams": this.systemParams}, response => {
          if (response.success) {
            this.query();//刷新数据
            this.$success(this.$t('commons.save_success'));
          } else {
            this.$message.error(this.$t('commons.save_failed'));
          }
        });
      },
      cancel() {
        this.showEdit = true;
        this.showCancel = false;
        this.showSave = false;
        this.show = true;
        this.query();
      },
      handleExceed(files, fileList) {
        this.$warning(this.$t('test_track.case.import.upload_limit_count'));
      },
      handleError() {
        this.$warning(this.$t('test_track.case.import.upload_limit_count'));
      },
      uploadValidate(file) {
        let suffix = file.name.substring(file.name.lastIndexOf('.') + 1);
        if (!this.suffixes.has(suffix)) {
          this.$warning(this.$t('test_track.case.import.upload_limit_format'));
          return false;
        }

        if (file.size / 1024 / 1024 > 5) {
          this.$warning(this.$t('test_track.case.import.upload_limit_size'));
          return false;
        }
        this.errList = [];
        return true;
      },
      uploadLogo(file) {
        this.systemParams.forEach((param) => {
          if (param.paramKey === "base.logo") {
            param.fileName = file.file.name;
            param.file = file.file;
          }
        })
      },
      removeValue(paramKey) {
        this.systemParams.forEach((param) => {
          if (param.paramKey === paramKey) {
            param.fileName = null;
            param.file = null;
          }
        });
      }
    }
  }
</script>

<style scoped>

  .el-form {
    min-height: 300px;
  }

  .inline-block {
    display: inline-block;
    position: absolute;
    left: 105px;
  }

</style>

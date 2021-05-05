<template>
  <div v-loading="$store.getters.loadingMap[$store.getters.currentPath]">
    <el-form
      ref="systemParams"
      v-loading="$store.getters.loadingMap[$store.getters.currentPath]"
      class="demo-form-inline"
      :disabled="show"
      size="small"
    >
      <el-row>
        <el-col v-for="(param,index) in systemParams" :key="index">
          <!--logo upload-->
          <el-form-item
            v-if="param.paramKey==='ui.logo'"
            :label="$t('display.logo')"
          >
            <el-upload
              v-loading="$store.getters.loadingMap[$store.getters.currentPath]"
              style="float: right;margin-left: 10px"
              class="upload-demo"
              action=""
              accept=".jpeg,.jpg,.png,.gif"
              :on-exceed="handleExceed"
              :before-upload="uploadValidate"
              :on-error="handleError"
              :show-file-list="false"
              :file-list="filesTmp"
              :http-request="uploadLogo"
            >
              <el-button style="display: inline-block" size="mini" type="success" plain>
                {{ $t('commons.upload') }}
              </el-button>
            </el-upload>
            <el-button
              style="float:right;margin-top: 3px"
              size="mini"
              type="danger"
              plain
              @click="removeValue('ui.logo')"
            >
              {{ $t('commons.clear') }}
            </el-button>
            <el-input
              v-model="param.fileName"
              :disabled="true"
              :placeholder="$t('display.advice_size')+'：135px * 30px'"
            />
          </el-form-item>
          <!--logo upload-->
          <el-form-item
            v-if="param.paramKey==='ui.loginImage'"
            :label="$t('display.loginImage')"
          >
            <el-upload
              v-loading="$store.getters.loadingMap[$store.getters.currentPath]"
              style="float: right;margin-left: 10px"
              class="upload-demo"
              action=""
              accept=".jpeg,.jpg,.png,.gif"
              :on-exceed="handleExceed"
              :before-upload="uploadValidate"
              :on-error="handleError"
              :show-file-list="false"
              :file-list="filesTmp"
              :http-request="uploadLoginImage"
            >
              <el-button style="display: inline-block" size="mini" type="success" plain>
                {{ $t('commons.upload') }}
              </el-button>
            </el-upload>
            <el-button
              style="float:right;margin-top: 3px"
              size="mini"
              type="danger"
              plain
              @click="removeValue('ui.loginImage')"
            >
              {{ $t('commons.clear') }}
            </el-button>
            <el-input
              v-model="param.fileName"
              :disabled="true"
              :placeholder="$t('display.advice_size')+'：500px * 450px'"
            />
          </el-form-item>
          <!--favicon upload-->
          <el-form-item
            v-if="param.paramKey==='ui.loginLogo'"
            :label="$t('display.loginLogo')"
          >
            <el-upload
              v-loading="$store.getters.loadingMap[$store.getters.currentPath]"
              style="float: right;margin-left: 10px"
              class="upload-demo"
              action=""
              accept=".jpeg,.jpg,.png,.gif"
              :on-exceed="handleExceed"
              :before-upload="uploadValidate"
              :on-error="handleError"
              :show-file-list="false"
              :file-list="filesTmp"
              :http-request="uploadLoginLogo"
            >
              <el-button style="display: inline-block" size="mini" type="success" plain>
                {{ $t('commons.upload') }}
              </el-button>
            </el-upload>
            <el-button
              style="float:right;margin-top: 3px"
              size="mini"
              type="danger"
              plain
              @click="removeValue('ui.loginLogo')"
            >
              {{ $t('commons.clear') }}
            </el-button>
            <el-input
              v-model="param.fileName"
              :disabled="true"
              :placeholder="$t('display.advice_size')+'：135px * 30px'"
            />
          </el-form-item>
          <!--favicon upload-->
          <el-form-item
            v-if="param.paramKey==='ui.favicon'"
            :label="$t('display.favicon')"
          >
            <el-upload
              v-loading="$store.getters.loadingMap[$store.getters.currentPath]"
              style="float: right;margin-left: 10px"
              class="upload-demo"
              action=""
              accept=".jpeg,.jpg,.png,.gif"
              :on-exceed="handleExceed"
              :before-upload="uploadValidate"
              :on-error="handleError"
              :show-file-list="false"
              :file-list="filesTmp"
              :http-request="uploadFavicon"
            >
              <el-button style="display: inline-block" size="mini" type="success" plain>
                {{ $t('commons.upload') }}
              </el-button>
            </el-upload>
            <el-button
              style="float:right;margin-top: 3px"
              size="mini"
              type="danger"
              plain
              @click="removeValue('ui.favicon')"
            >
              {{ $t('commons.clear') }}
            </el-button>
            <el-input
              v-model="param.fileName"
              :disabled="true"
              :placeholder="$t('display.advice_size')+'：16px * 16px'"
            />
          </el-form-item>
          <!--ui.loginTitle-->
          <el-form-item v-if="param.paramKey==='ui.loginTitle'" :label="$t('display.loginTitle')">
            <el-input v-model="param.paramValue" placeholder="eg：DateEase" />
          </el-form-item>
          <!--ui.title-->
          <el-form-item v-if="param.paramKey==='ui.title'" :label="$t('display.title')">
            <el-input v-model="param.paramValue" placeholder="eg：DateEase" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <div>
      <el-button v-if="showEdit" size="small" @click="edit">{{ $t('commons.edit') }}</el-button>
      <el-button v-if="showSave" type="success" :disabled="disabledSave" size="small" @click="save">
        {{ $t('commons.save') }}
      </el-button>
      <el-button v-if="showCancel" type="info" size="small" @click="cancel">{{ $t('commons.cancel') }}</el-button>
    </div>
  </div>
</template>

<script>
import { get, fileUpload } from '@/api/commonAjax'

export default {
  name: 'UiSetting',
  components: {

  },
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
          }
        ]
      }
    }
  },
  created() {
    this.query()
  },
  methods: {
    query() {
      get('/system/ui/info').then(res => {
        this.systemParams = res.data
      })
    },
    edit() {
      this.showEdit = false
      this.showSave = true
      this.showCancel = true
      this.show = false
    },
    save() {
      this.showEdit = true
      this.showCancel = false
      this.showSave = false
      this.show = true
      this.systemParams.forEach((systemParam) => {
        if (systemParam.file !== null) {
          const file = systemParam.file
          const name = file.name + ',' + systemParam.paramKey
          const newfile = new File([file], name, { type: file.type })
          this.files.push(newfile)
          systemParam.file = null
        }
      })

      fileUpload('/system/save/ui', null, this.files, { 'systemParams': this.systemParams }).then(response => {
        if (response.success) {
          this.query()// 刷新数据
          this.$success(this.$t('commons.save_success'))
        } else {
          this.$message.error(this.$t('commons.save_failed'))
        }
      })
    },
    cancel() {
      this.showEdit = true
      this.showCancel = false
      this.showSave = false
      this.show = true
      this.query()
    },
    handleExceed(files, fileList) {
      this.$warning(this.$t('test_track.case.import.upload_limit_count'))
    },
    handleError() {
      this.$warning(this.$t('test_track.case.import.upload_limit_count'))
    },
    uploadValidate(file) {
      const suffix = file.name.substring(file.name.lastIndexOf('.') + 1)
      if (!this.suffixes.has(suffix)) {
        this.$warning(this.$t('test_track.case.import.upload_limit_format'))
        return false
      }

      if (file.size / 1024 / 1024 > 5) {
        this.$warning(this.$t('test_track.case.import.upload_limit_size'))
        return false
      }
      this.errList = []
      return true
    },
    uploadLogo(file) {
      this.upload(file, 'ui.logo')
    },
    uploadFavicon(file) {
      this.upload(file, 'ui.favicon')
    },
    uploadLoginImage(file) {
      this.upload(file, 'ui.loginImage')
    },
    uploadLoginLogo(file) {
      this.upload(file, 'ui.loginLogo')
    },
    upload(file, paramKey) {
      this.systemParams.forEach((systemParam) => {
        if (systemParam.paramKey === paramKey) {
          systemParam.fileName = file.file.name
          systemParam.file = file.file
        }
      })
    },
    removeValue(paramKey) {
      this.systemParams.forEach((systemParam) => {
        if (systemParam.paramKey === paramKey) {
          systemParam.fileName = null
          systemParam.file = null
        }
      })
    }
  }
}
</script>

<style scoped>
  .el-form {
    min-height: 300px;
  }

</style>

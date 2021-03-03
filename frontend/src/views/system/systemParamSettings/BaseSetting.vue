<template>
  <div v-loading="result.loading">
    {{ systemParams }}
    <el-form
      ref="systemParams"
      v-loading="loading"
      class="demo-form-inline"
      :disabled="show"
      size="small"
    >
      <el-row>
        <el-col v-for="(param,index) in systemParams" :key="index">
          <!--logo upload-->
          <el-form-item
            v-if="param.paramKey==='base.logo'"
            :label="$t('system_config.base.logo')"
          >
            <el-upload
              v-loading="result.loading"
              style="float: left"
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
              style="float:left;margin-left: 10px;margin-top: 3px"
              size="mini"
              type="danger"
              plain
              @click="removeValue('base.logo')"
            >
              {{ $t('commons.upload') }}
            </el-button>
            <el-input
              v-model="param.fileName"
              :disabled="true"
              :placeholder="$t('system_config.base.logo_size')+'：135px * 30px'"
            />
          </el-form-item>
          <!--title-->
          <el-form-item v-if="param.paramKey==='base.title'" :label="$t('system_config.base.title')">
            <el-input v-model="param.paramValue" placeholder="eg：DateEase" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <div>
      <el-button v-if="showEdit" size="small" @click="edit">{{ $t('commons.edit') }}</el-button>
      <el-button v-if="showSave" type="success" :disabled="disabledSave" size="small" @click="save('systemParams')">
        {{ $t('commons.save') }}
      </el-button>
      <el-button v-if="showCancel" type="info" size="small" @click="cancel">{{ $t('commons.cancel') }}</el-button>
    </div>
  </div>
</template>

<script>
import ElUploadList from 'element-ui/packages/upload/src/upload-list'

export default {
  name: 'BaseSetting',
  components: {
    ElUploadList
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
      this.result = this.$get('/system/base/info', response => {
        this.systemParams = response.data
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
      this.systemParams.forEach((param) => {
        if (param.file !== null) {
          const file = param.file
          const name = file.name + ',' + param.paramKey
          const newfile = new File([file], name, { type: file.type })
          this.files.push(newfile)
          param.file = null
        }
      })

      this.result = this.$fileUpload('/system/save/base', null, this.files, { 'systemParams': this.systemParams }, response => {
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
      this.systemParams.forEach((param) => {
        if (param.paramKey === 'base.logo') {
          param.fileName = file.file.name
          param.file = file.file
        }
      })
    },
    removeValue(paramKey) {
      this.systemParams.forEach((param) => {
        if (param.paramKey === paramKey) {
          param.fileName = null
          param.file = null
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

  .inline-block {
    display: inline-block;
    position: absolute;
    left: 105px;
  }

</style>

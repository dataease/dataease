<template>
  <layout-content>
    <template v-slot:header>
      <el-icon name="back" class="back-button" @click.native="backToList" />
      {{
        params && params.id && params.showModel && params.showModel === 'show' && !canEdit ? $t('driver.show_info') : $t('driver.modify')
      }}
      <el-button v-if="canEdit" size="mini" style="float: right;"type="primary" @click="save">{{ $t('commons.save') }}
      </el-button>
      <el-button v-else size="mini" style="float: right;" type="primary" @click="changeEdit">{{ $t('commons.edit') }}
      </el-button>

    </template>
    <div>

      <el-form
        ref="driverForm"
        :model="driverForm"
        :rules="rule"
        size="small"
        :disabled="params && params.id && params.showModel && params.showModel === 'show' && !canEdit"
        label-width="180px"
        label-position="right"
      >
        <el-form-item :label="$t('commons.name')" prop="name">
          <el-input v-model="driverForm.name" autocomplete="off" />
        </el-form-item>
        <el-form-item :label="$t('commons.description')">
          <el-input v-model="driverForm.desc" autocomplete="off" />
        </el-form-item>
        <el-form-item :label="$t('datasource.type')" prop="type">
          <el-select
            v-model="driverForm.type"
            :placeholder="$t('datasource.please_choose_type')"
            class="select-width"
            style="width: 100%"
            disabled
            filterable
          >
            <el-option
              v-for="item in dsTypes"
              :key="item.type"
              :label="item.name"
              :value="item.type"
            />
          </el-select>
        </el-form-item>

        <!--        <el-form-item :label="$t('driver.driver')" >-->
        <!--          <el-select-->
        <!--            v-model="driverForm.driverClass"-->
        <!--            :placeholder="$t('driver.please_choose_driver')"-->
        <!--            class="select-width"-->
        <!--            filterable-->
        <!--          >-->
        <!--            <el-option-->
        <!--              v-for="item in driverClassList"-->
        <!--              :key="item"-->
        <!--              :label="item"-->
        <!--              :value="item"-->
        <!--            />-->
        <!--          </el-select>-->
        <!--        </el-form-item>-->

        <el-form-item :label="$t('driver.driver')" prop="driverClass">
          <el-input v-model="driverForm.driverClass" autocomplete="off" />
        </el-form-item>

      </el-form>

      <el-upload
        :action="baseUrl+'driver/file/upload'"
        :multiple="true"
        :show-file-list="false"
        :file-list="fileList"
        :data="params"
        accept=".jar"
        :before-upload="beforeUpload"
        :on-success="uploadSuccess"
        :on-error="uploadFail"
        name="file"
        :headers="headers"
        style="float: right;"
      >
        <el-button size="mini" type="primary" style="float: right;" :disabled="uploading">
          <span v-if="!uploading" style="font-size: 12px;">{{ $t('dataset.upload_file') }}</span>
          <span v-if="uploading" style="font-size: 12px;"><i class="el-icon-loading" /> {{ $t('dataset.uploading') }}</span>
        </el-button>
      </el-upload>
      <fu-table :data="driverFiles">
        <el-table-column prop="fileName" :label="$t('driver.file_name')" />
        <!--      <el-table-column prop="version" :label="$t('driver.version')"/>-->
        <fu-table-operations :buttons="buttons" :label="$t('commons.operating')" fix />
      </fu-table>

    </div>
  </layout-content>
</template>

<script>
import LayoutContent from '@/components/business/LayoutContent'
import i18n from '@/lang/index'
import ApiHttpRequestForm from '@/views/system/datasource/ApiHttpRequestForm'
import DsConfiguration from '@/views/system/datasource/DsConfiguration'
import PluginCom from '@/views/system/plugin/PluginCom'
import { deleteDriverFile, listDriverDetails, updateDriver } from '@/api/system/datasource'
import { delUser } from '@/api/system/user'
import { $alert } from '@/utils/message'
import store from '@/store'
import { getToken } from '@/utils/auth'

const token = getToken()

export default {
  name: 'DriverForm',
  components: {
    DsConfiguration,
    LayoutContent,
    ApiHttpRequestForm,
    PluginCom
  },
  props: {
    params: {
      type: Object,
      default: null
    },
    tData: {
      type: Array,
      default: null
    },
    dsTypes: {
      type: Array,
      default: null
    }
  },
  data() {
    return {
      disabled: false,
      driverForm: {
        id: '',
        name: '',
        desc: '',
        type: '',
        driverClass: ''
      },
      datasourceType: {},
      driverClassList: [],
      rule: {
        name: [{ required: true, message: i18n.t('datasource.input_name'), trigger: 'blur' },
          { min: 2, max: 50, message: i18n.t('datasource.input_limit_2_25', [2, 25]), trigger: 'blur' }],
        desc: [{ required: true, message: i18n.t('datasource.input_name'), trigger: 'blur' },
          { min: 2, max: 200, message: i18n.t('datasource.input_limit_2_25', [2, 25]), trigger: 'blur' }],
        type: [{ required: true, message: i18n.t('datasource.please_choose_type'), trigger: 'blur' }],
        driverClass: [{ required: true, message: i18n.t('driver.please_set_driverClass'), trigger: 'blur' }]
      },
      canEdit: false,
      driverFiles: [],
      btnDisabled: false,
      buttons: [
        {
          label: this.$t('commons.delete'), icon: 'el-icon-delete', type: 'danger', click: this.deleteDriverFile,
          disabled: this.btnDisabled
        }
      ],
      uploading: false,
      baseUrl: process.env.VUE_APP_BASE_API,
      headers: { Authorization: token, 'Accept-Language': i18n.locale.replace('_', '-') },
      fileList: []
    }
  },

  created() {
    const row = this.params
    this.driverForm = JSON.parse(JSON.stringify(row))
    this.disabled = this.params && this.params.id && this.params.showModel && this.params.showModel === 'show' && !this.canEdit
    this.listDriverDetails()
  },
  mounted() {
  },

  methods: {
    beforeUpload(file) {
      this.uploading = true
    },
    uploadSuccess(response, file, fileList) {
      this.uploading = false
      this.listDriverDetails()
    },
    uploadFail(response, file, fileList) {
      let myError = response.toString()
      myError = myError.replace('Error: ', '')
      if (myError.indexOf('AuthenticationException') >= 0) {
        const message = i18n.t('login.tokenError')
        $alert(message, () => {
          store.dispatch('user/logout').then(() => {
            location.reload()
          })
        }, {
          confirmButtonText: i18n.t('login.re_login'),
          showClose: false
        })
        return
      }
      const errorMessage = JSON.parse(myError).message
      this.uploading = false
      this.$message({
        type: 'error',
        message: errorMessage,
        showClose: true
      })
    },
    deleteDriverFile(row) {
      deleteDriverFile(row).then(res => {
        this.$success(this.$t('commons.delete_success'))
        this.listDriverDetails()
      })
    },
    listDriverDetails() {
      listDriverDetails(this.driverForm.id).then(res => {
        this.driverFiles = res.data
        this.driverClassList = []
        this.driverFiles.forEach(driverFile => {
          this.driverClassList = this.driverClassList.concat(driverFile.driverClass.split(','))
        })
      })
    },
    changeEdit() {
      this.canEdit = true
      this.formType = 'modify'
      this.disabled = this.params && this.params.id && this.params.showModel && this.params.showModel === 'show' && !this.canEdit
    },
    save() {
      this.$refs.driverForm.validate(valid => {
        if (!valid) {
          return false
        }
        updateDriver(this.driverForm).then(res => {
          this.$success(i18n.t('commons.success'))
          this.canEdit = false
        })
      })
    },
    reset() {
      this.$refs.dsForm.resetFields()
    },
    backToList() {
      this.$emit('switch-component', {})
    },
    refreshType(form) {
      this.$emit('refresh-type', DsForm)
    },
    handleClick(tab, event) {}
  }
}
</script>
<style scoped>
/* .el-input {
  width: 300px;
}

.el-select {
  width: 300px;
} */

.ms-http-input {
  width: 500px;
  margin-top: 5px;
}

.tip {
  padding: 3px 5px;
  font-size: 16px;
  border-radius: 0;
  border-left: 4px solid #409EFF;
  margin: 5px 5px 10px 5px;
}

.el-select ::v-deep input {
  padding-right: 10px;
}

.el-select ::v-deep .el-input__suffix {
  right: 0;
}

.dialog-css ::v-deep .el-dialog__header {
  padding: 10px 20px 0px;
}

.dialog-css ::v-deep .el-dialog__body {
  padding: 10px 20px 10px;
}

.dialog-footer ::v-deep .el-dialog__footer {
  padding: 10px 10px 10px;
}

</style>

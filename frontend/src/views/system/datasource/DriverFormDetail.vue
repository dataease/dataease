<template>
  <div class="driver-detail">
    <p class="name">
      {{ driverForm.name }}
    </p>
    <p class="descript">
      {{ driverForm.desc }}
    </p>
    <div class="de-row-rules">
      <span>{{ $t('datasource.driver_file') }}</span>
    </div>

    <el-form
      ref="driverForm"
      :model="driverForm"
      :rules="rule"
      size="small"
      class="de-form-item"
      label-width="180px"
      label-position="right"
    >
      <el-form-item :label="$t('driver.driver')" prop="driverClass">
        <el-input
          style="width: 600px"
          v-model="driverForm.driverClass"
          autocomplete="off"
        />
      </el-form-item>
    </el-form>
    <el-upload
      :action="baseUrl + 'driver/file/upload'"
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
    >
      <deBtn
        icon="el-icon-upload2"
        secondary
        :loading="uploading"
        :disabled="uploading"
      >
        {{ uploading ? $t('dataset.uploading') : $t('dataset.upload_file') }}
      </deBtn>
    </el-upload>
    <p class="tips">
      {{ $t('datasource.can_be_uploaded') }}
    </p>
    <div class="jar-cont">
      <div v-for="jar in driverFiles" :key="jar.id" class="jar-info">
        <img :src="imgUrl" alt="" />
        <p class="name-descript">
          <span class="jar-name">
            {{ jar.fileName }}
          </span>
          <span class="jar-descript">
            {{ jar.fileName }}
          </span>
        </p>
        <el-tooltip
          class="item"
          effect="dark"
          :content="$t('commons.delete')"
          placement="top"
        >
          <i @click="deleteDriverFile(jar)" class="el-icon-delete"></i>
        </el-tooltip>
      </div>
    </div>
    <div class="de-foot">
      <deBtn type="primary" @click="save">{{ $t('commons.save') }}</deBtn>
    </div>
  </div>
</template>
<script>
import i18n from '@/lang/index'
import ApiHttpRequestForm from '@/views/system/datasource/ApiHttpRequestForm'
import DsConfiguration from '@/views/system/datasource/DsConfiguration'
import {
  deleteDriverFile,
  listDriverDetails,
  updateDriver
} from '@/api/system/datasource'
import { $alert } from '@/utils/message'
import store from '@/store'
import { getToken } from '@/utils/auth'

const token = getToken()

export default {
  name: 'DriverForm',
  components: {
    DsConfiguration,
    ApiHttpRequestForm
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
      imgUrl: require('@/assets/FileType.png'),
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
        name: [
          {
            required: true,
            message: i18n.t('datasource.input_name'),
            trigger: 'blur'
          },
          {
            min: 2,
            max: 50,
            message: i18n.t('datasource.input_limit_2_25', [2, 25]),
            trigger: 'blur'
          }
        ],
        desc: [
          {
            required: true,
            message: i18n.t('datasource.input_name'),
            trigger: 'blur'
          },
          {
            min: 2,
            max: 200,
            message: i18n.t('datasource.input_limit_2_25', [2, 25]),
            trigger: 'blur'
          }
        ],
        type: [
          {
            required: true,
            message: i18n.t('datasource.please_choose_type'),
            trigger: 'blur'
          }
        ],
        driverClass: [
          {
            required: true,
            message: i18n.t('driver.please_set_driverClass'),
            trigger: 'blur'
          }
        ]
      },
      canEdit: false,
      driverFiles: [],
      uploading: false,
      baseUrl: process.env.VUE_APP_BASE_API,
      headers: {
        Authorization: token,
        'Accept-Language': i18n.locale.replace('_', '-')
      },
      fileList: []
    }
  },
  created() {
    const row = this.params
    this.driverForm = JSON.parse(JSON.stringify(row))
    this.disabled =
      this.params &&
      this.params.id &&
      this.params.showModel &&
      this.params.showModel === 'show' &&
      !this.canEdit
    this.listDriverDetails()
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
        $alert(
          message,
          () => {
            store.dispatch('user/logout').then(() => {
              location.reload()
            })
          },
          {
            confirmButtonText: i18n.t('login.re_login'),
            showClose: false
          }
        )
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
      deleteDriverFile(row).then((res) => {
        this.$success(this.$t('commons.delete_success'))
        this.listDriverDetails()
      })
    },
    listDriverDetails() {
      listDriverDetails(this.driverForm.id).then((res) => {
        this.driverFiles = res.data
        this.driverClassList = []
        this.driverFiles.forEach((driverFile) => {
          this.driverClassList = this.driverClassList.concat(
            driverFile.driverClass.split(',')
          )
        })
      })
    },
    changeEdit() {
      this.canEdit = true
      this.formType = 'modify'
      this.disabled =
        this.params &&
        this.params.id &&
        this.params.showModel &&
        this.params.showModel === 'show' &&
        !this.canEdit
    },
    save() {
      this.$refs.driverForm.validate((valid) => {
        if (!valid) {
          return false
        }
        updateDriver(this.driverForm).then((res) => {
          this.$success(i18n.t('commons.success'))
          this.$emit('DataUpdate', res.data)
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
<style lang="scss" scoped>
.driver-detail {
  font-family: PingFang SC;
  width: 100%;
  height: 100%;

  .name {
    font-size: 16px;
    font-weight: 500;
    margin: 0;
    color: var(--deTextPrimary, #1f2329);
  }

  .descript {
    font-size: 14px;
    font-weight: 400;
    margin: 0;
    margin-top: 4px;
    color: var(--deTextSecondary, #646a73);
  }

  .el-upload {
    margin-top: -4px;
  }

  .tips {
    margin: 2px 0 8px 0;
    color: var(----deTextPlaceholder, #8f959e);
  }

  .jar-cont {
    display: flex;
    flex-wrap: wrap;
    width: 100%;

    .jar-info {
      border: 1px solid var(--deCardStrokeColor, #dee0e3);
      border-radius: 4px;
      padding: 8px 12px;
      margin: 0 12px 12px 0;
      position: relative;
      display: flex;
      align-items: center;
      width: 340px;
      box-sizing: border-box;
      &:hover {
        border: 1px solid var(--primary, #3370ff);
      }

      img {
        height: 42px;
        width: 40px;
      }
      .name-descript {
        margin: 0;
        margin-left: 10px;
        font-family: PingFang SC;
        font-weight: 400;
        display: flex;
        flex-direction: column;

        .jar-descript {
          font-size: 14px;
          color: var(--deTextPlaceholder, #8f959e);
        }

        .jar-name {
          font-size: 12px;
          color: var(--deTextPrimary, #1f2329);
        }
      }

      .el-icon-delete {
        cursor: pointer;
        width: 24px;
        height: 24px;
        line-height: 24px;
        text-align: center;
        cursor: pointer;
        font-size: 13.3px;
        color: var(--deTextPlaceholder, #8f959e);
        position: absolute;
        top: 18px;
        right: 17px;

        &:hover {
          background: rgba(31, 35, 41, 0.1);
          border-radius: 4px;
        }
      }
    }
  }
}
</style>

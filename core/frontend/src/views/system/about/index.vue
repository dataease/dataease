<template>
  <div style="width: 100%;display: flex;justify-content: center;">
    <el-card
      class="box-card about-card"
      :class="dynamicCardClass"
    >
      <div
        slot="header"
        class="clearfix license-header"
      >
        <img
          src="@/assets/DataEase-white.png"
          alt=""
          width="300"
        >
      </div>
      <div class="license-content">
        <div v-if="license.status === 'Fail'">{{ $t('about.invalid_license') }}</div>
        <div v-if="license.status !== 'Fail'">
          <table>
            <tr>
              <th>{{ $t('about.auth_to') }}</th>
              <td>{{ license.corporation }}</td>
            </tr>
            <tr>
              <th>{{ $t('about.expiration_time') }}</th>
              <td>
                <label
                  v-if="license.status === 'expired'"
                  style="color: red"
                >{{ license.expired }} {{ $t('about.expirationed') }}</label>
                <label v-if="license.status === 'valid'">{{ license.expired }}</label>
              </td>
            </tr>
            <tr>
              <th>{{ $t('about.auth_num') }}</th>
              <td>{{ license.count }}</td>
            </tr>
            <tr>
              <th>{{ $t('about.version') }}</th>
              <td>
                <span v-if="license.edition">
                  <span v-if="license.edition === 'Standard'">{{ $t('about.standard') }}</span>
                  <span v-if="license.edition === 'Enterprise'">{{ $t('about.enterprise') }}</span>
                </span>
              </td>
            </tr>
            <tr>
              <th>{{ $t('about.version_num') }}</th>
              <td>
                <span>{{ build }}</span>
              </td>
            </tr>
            <tr v-if="license.serialNo">
              <th>{{ $t('about.serial_no') }}</th>
              <td>
                <span>{{ license.serialNo }}</span>
              </td>
            </tr>
            <tr v-if="license.remark">
              <th>{{ $t('about.remark') }}</th>
              <td>
                <span>{{ license.remark }}</span>
              </td>
            </tr>
          </table>
        </div>

        <div class="md-padding" />
        <div
          v-if="user.isAdmin"
          layout="row"
          layout-align="space-between center"
          class="lic_rooter"
        >
          <el-upload
            action=""
            :multiple="false"
            :show-file-list="false"
            :file-list="fileList"
            accept=".key"
            name="file"
            :before-upload="beforeUpload"
          >
            <a class="md-primary pointer">{{ $t('about.update_license') }}</a>

          </el-upload>

          <a
            class="md-primary pointer"
            @click="support"
          >{{ $t('about.support') }}</a>

        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { validate, buildVersion, updateInfo } from '@/api/system/about'
import { getToken } from '@/utils/auth'
import { mapGetters } from 'vuex'
export default {
  data() {
    return {
      license: {},
      version: null,
      build: null,
      licenseKey: '',
      fileList: [],
      headers: { Authorization: getToken() }
    }
  },
  computed: {
    dynamicCardClass() {
      if (this.license?.serialNo && this.license?.remark) {
        return 'about-card-max'
      }
      if (!this.license?.serialNo && !this.license?.remark) {
        return ''
      }
      return 'about-card-medium'
    },
    ...mapGetters([
      'user'
    ])
  },
  mounted() {
    this.$nextTick(() => {
      this.$store.dispatch('app/toggleSideBarHide', true)
    })
  },
  created() {
    this.$store.dispatch('app/toggleSideBarHide', true)
    this.initVersion()
    this.getLicenseInfo()
  },
  methods: {

    initVersion() {
      buildVersion().then(res => {
        this.build = res.data
      })
    },
    getLicenseInfo() {
    //   validate({}).then(res => {
    //     this.license = this.getLicense(res.data)
    //   })
      this.validateHandler({}, res => {
        this.license = this.getLicense(res.data)
      })
    },
    validateHandler(param, success) {
      validate(param).then(success)
    },
    getLicense(result) {
      return {
        status: result.status,
        corporation: result.license ? result.license.corporation : '',
        expired: result.license ? result.license.expired : '',
        count: result.license ? result.license.count : '',
        version: result.license ? result.license.version : '',
        edition: result.license ? result.license.edition : '',
        serialNo: result.license ? result.license.serialNo : '',
        remark: result.license ? result.license.remark : ''
      }
    },
    importLic(file) {
      const reader = new FileReader()
      reader.onload = function(e) {
        this.licenseKey = e.target.result
        this.validateHandler({ license: this.licenseKey }, response => {
          this.updateLicense = this.getLicense(response.data)
          this.update()
        })
      }.bind(this)
      reader.readAsText(file)
    },
    update() {
      const param = { license: this.licenseKey }
      updateInfo(param).then(response => {
        if (response.data.status === 'valid') {
          this.$success(this.$t('about.update_success'))
          this.license = this.getLicense(response.data)
        } else {
          this.$warning(response.data.message)
        }
      })
    },
    support() {
      const url = 'https://support.fit2cloud.com/'
      window.open(url, '_blank')
    },
    beforeUpload(file) {
      this.importLic(file)
      return false
    }

  }
}
</script>

<style lang="scss" scoped>
    .about-card {
        background: inherit;
        margin-top: 5%;
        flex-direction: row;
        width: 640px;
        min-width: 640px;
        height: 400px;
        position: relative;
        ::v-deep div.el-card__header {
            padding: 0;
        }
    }
    .about-card-medium {
      height: 415px !important;
    }
    .about-card-max {
      height: 430px !important;
    }
    .license-header {
        height: 100px;
        background-image: url('../../../assets/license_header.png');
        text-align: center;
        padding: 20px 0;
        background-size: 100% 100%;
    }

    .license-content {
        font-size: 16px;
        padding: 50px;
        ::v-deep table {
            width: 100%;
        }
        ::v-deep th {
            text-align: left;
            width: 45%;
        }
        ::v-deep td {
            display: table-cell;
            vertical-align: inherit;
        }
    }
    .md-padding {
        padding: 10px;
    }
    .lic_rooter {
        flex-direction: row;
        box-sizing: border-box;
        display: flex;
        align-items: center;
        align-content: center;
        max-width: 100%;
        justify-content: space-between;

        ::v-deep a{
            color: rgb(10,123,224);
            cursor: pointer;
        }
    }
</style>

<template>
  <layout-content>

    <div>
      <el-card class="box-card about-card">
        <div slot="header" class="clearfix license-header">
          <img src="@/assets/DataEase-white.png" alt="" style="height: 80px;width: 400px;padding-top: 15px;">
        </div>
        <div class="license-content">
          <div v-if="license.status === 'Fail'">License 无效</div>
          <div v-if="license.status !== 'Fail'">
            <table>
              <tr>
                <th>授权给</th>
                <td>{{ license.corporation }}</td>
              </tr>
              <tr>
                <th>过期时间</th>
                <td>
                  <label v-if="license.status === 'expired'" style="color: red">{{ license.expired }} (已过期)</label>
                  <label v-if="license.status === 'valid'">{{ license.expired }}</label>
                </td>
              </tr>
              <tr>
                <th>授权数量</th>
                <td>{{ license.count }}</td>
              </tr>
              <tr>
                <th>版本</th>
                <td>
                  <span v-if="license.edition">
                    <span v-if="license.edition === 'Standard'">标准版</span>
                    <span v-if="license.edition === 'Enterprise'">企业版</span>
                  </span>
                </td>
              </tr>
              <tr>
                <th>版本号</th>
                <td>
                  <span>{{ build }}</span>
                </td>
              </tr>
            </table>
          </div>

          <div class="md-padding" />
          <div v-if="user.isAdmin" layout="row" layout-align="space-between center" class="lic_rooter">
            <el-upload
              action=""
              :multiple="false"
              :show-file-list="false"
              :file-list="fileList"
              accept=".key"
              name="file"
              :before-upload="beforeUpload"
            >
              <a class="md-primary pointer">更新 License</a>

            </el-upload>

            <a class="md-primary pointer" @click="support">获取技术支持</a>

          </div>
        </div>
      </el-card>
    </div>
  </layout-content>
</template>

<script>
import LayoutContent from '@/components/business/LayoutContent'
import { validate, buildVersion, updateInfo } from '@/api/system/about'
import { getToken } from '@/utils/auth'
import { mapGetters } from 'vuex'
export default {

  components: { LayoutContent },
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
    ...mapGetters([
      'user'
    ])
  },
  created() {
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
    //     console.log(this.license)
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
        edition: result.license ? result.license.edition : ''
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
          this.$success(this.$t('i18n_mc_update_success'))
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
      // console.log(file)
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
        margin-left: 20%;
        margin-right: 20%;
        width: 640px;
        height: 400px;
        position: relative;
        >>>div.el-card__header {
            padding: 0;
        }
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
        >>>table {
            width: 100%;
        }
        >>>th {
            text-align: left;
            width: 45%;
        }
        >>>td {
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

        >>>a{
            color: rgb(10,123,224);
            cursor: pointer;
        }
    }
</style>

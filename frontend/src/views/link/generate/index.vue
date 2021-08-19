<template>
  <div>
    <el-form ref="createOrganization" inline :model="form" size="small" label-width="80px">

      <el-form-item :label="$t('panel.link_share')">
        <el-switch
          v-model="valid"
          style="width: 370px;"
          :active-value="true"
          :inactive-value="false"
          @change="onChange"
        />
      </el-form-item>
      <el-form-item label=" ">
        <el-link class="de-link" style="width: 370px;" disabled>{{ $t('panel.link_share_desc') }}</el-link>
      </el-form-item>
      <el-form-item v-if="valid" :label="$t('panel.link')">
        <el-input
          v-model.number="form.uri"
          disabled
          style="width: 370px;"
        />
      </el-form-item>

      <el-form-item v-if="valid" label=" ">
        <el-checkbox v-model="form.enablePwd" @change="resetEnablePwd">{{ $t('panel.passwd_protect') }}  </el-checkbox>

        <span v-if="form.enablePwd" class="de-span">{{ form.pwd }}</span>
        <span v-if="form.enablePwd" class="de-span" @click="resetPwd"><el-link :underline="false" type="primary">{{ $t('commons.reset') }}</el-link></span>
      </el-form-item>

      <div v-if="valid" class="auth-root-class">
        <span slot="footer">

          <el-button v-if="newUrl && !form.enablePwd" v-clipboard:copy="newUrl" v-clipboard:success="onCopy" v-clipboard:error="onError" size="mini" type="primary">{{ $t('panel.copy_short_link') }}</el-button>
          <el-button v-if="newUrl && form.enablePwd" v-clipboard:copy="newUrl + ' Password: '+ form.pwd" v-clipboard:success="onCopy" v-clipboard:error="onError" size="mini" type="primary">{{ $t('panel.copy_short_link_passwd') }}</el-button>
          <el-button v-if="!form.enablePwd" v-clipboard:copy="form.uri" v-clipboard:success="onCopy" v-clipboard:error="onError" size="mini" type="primary">{{ $t('panel.copy_link') }}</el-button>
          <el-button v-if="form.enablePwd" v-clipboard:copy="form.uri + ' Password: '+ form.pwd" v-clipboard:success="onCopy" v-clipboard:error="onError" size="mini" type="primary">{{ $t('panel.copy_link_passwd') }}</el-button>

        </span>
      </div>

    </el-form>
  </div>
</template>
<script>

import { loadGenerate, setPwd, switchValid, switchEnablePwd, shortUrl } from '@/api/link'
import { encrypt, decrypt } from '@/utils/rsaEncrypt'
export default {

  name: 'LinkGenerate',
  components: {

  },
  props: {
    resourceId: {
      type: String,
      default: null
    }
  },
  data() {
    return {
      pwdNums: 4,
      valid: false,
      form: {},
      newUrl: null,
      defaultForm: { enablePwd: false, pwd: null, uri: null }
    }
  },
  computed: {
    origin() {
      return window.location.origin
    }
  },
  created() {
    this.form = this.defaultForm
    this.currentGenerate()
  },
  methods: {
    currentGenerate() {
      loadGenerate(this.resourceId).then(res => {
        const { valid, enablePwd, pwd, uri } = res.data
        this.valid = valid
        this.form.enablePwd = enablePwd
        this.form.uri = uri ? (this.origin + uri) : uri
        // 返回的密码是共钥加密后的 所以展示需要私钥解密一波
        pwd && (this.form.pwd = decrypt(pwd))
        this.requestShort()
      })
    },

    createPwd() {
      const randomNum = () => {
        return Math.floor(Math.random() * 10) + ''
      }
      let result = ''
      for (let index = 0; index < this.pwdNums; index++) {
        result += randomNum()
      }
      return result
    },

    resetPwd() {
      // 密码采用RSA共钥加密
      const newPwd = this.createPwd()
      const param = {
        resourceId: this.resourceId,
        password: encrypt(newPwd)
      }
      setPwd(param).then(res => {
        this.form.pwd = newPwd
      })
    },
    resetEnablePwd(value) {
      const param = {
        resourceId: this.resourceId,
        enablePwd: value
      }
      switchEnablePwd(param).then(res => {
        // 当切换到启用密码保护 如果没有密码 要生成密码
        value && !this.form.pwd && this.resetPwd()
      })
    },

    onCopy(e) {
    //   alert('You just copied: ' + e.text)
      this.$success(this.$t('commons.copy_success'))
    },
    onError(e) {
    //   alert('Failed to copy texts')
    },
    onChange(value) {
      const param = {
        resourceId: this.resourceId,
        valid: value
      }
      switchValid(param).then(res => {

      })
    },
    requestShort() {
      const url = this.form.uri
      if (!url) return
      //   if (this.origin.includes('localhost') || this.origin.includes('127.0.0.1')) {
      //     console.log('本地无法生成短链接')
      //     this.$warning('本地无法生成短链接')
      //     return
      //   }
      shortUrl({ url }).then(res => {
        if (res.success) {
          this.newUrl = res.data
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
    .de-link{
        justify-content: left !important;
    }
    .de-span {
        margin: 0 15px;
    }
    .auth-root-class {
        margin: 15px 0px 5px;
        text-align: right;
    }
</style>

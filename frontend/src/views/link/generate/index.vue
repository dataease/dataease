<template>
  <div>
    <el-form ref="createOrganization" inline :model="form" size="small" label-width="80px">

      <el-form-item label="链接分享">
        <el-switch
          v-model="valid"
          style="width: 370px;"
          :active-value="true"
          :inactive-value="false"
          @change="onChange"
        />
      </el-form-item>
      <el-form-item label=" ">
        <el-link class="de-link" style="width: 370px;" disabled>开启链接后，任何人可通过此链接访问仪表板。</el-link>
      </el-form-item>
      <el-form-item v-if="valid" label="链接">
        <el-input
          v-model.number="form.uri"
          disabled
          style="width: 370px;"
        />
      </el-form-item>

      <el-form-item v-if="valid" label=" ">
        <el-checkbox v-model="form.enablePwd" @change="resetEnablePwd">密码保护</el-checkbox>

        <span v-if="form.enablePwd" class="de-span">{{ form.pwd }}</span>
        <span v-if="form.enablePwd" class="de-span" @click="resetPwd"><el-link :underline="false" type="primary">重置</el-link></span>
      </el-form-item>

      <div v-if="valid" class="auth-root-class">
        <span slot="footer">

          <el-button v-if="!form.enablePwd" v-clipboard:copy="form.uri" v-clipboard:success="onCopy" v-clipboard:error="onError" size="mini" type="primary">复制链接</el-button>
          <el-button v-if="form.enablePwd" v-clipboard:copy="form.uri + ' 密码: '+ form.pwd" v-clipboard:success="onCopy" v-clipboard:error="onError" size="mini" type="primary">复制链接及密码</el-button>

        </span>
      </div>

    </el-form>
  </div>
</template>
<script>

import { loadGenerate, setPwd, switchValid, switchEnablePwd } from '@/api/link'
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
      defaultForm: { enablePwd: false, pwd: null, uri: null }
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
        this.form.uri = uri
        // 返回的密码是共钥加密后的 所以展示需要私钥解密一波
        pwd && (this.form.pwd = decrypt(pwd))
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

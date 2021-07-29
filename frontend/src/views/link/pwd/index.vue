<template>
  <div class="pwd-body">
    <div class="pwd-wrapper">
      <div class="pwd-content">

        <div class="span-header">
          <div class="bi-text">
            <span style="text-align: center;">{{ $t('pblink.key_pwd') }}
            </span>
          </div>
        </div>

        <div class="input-layout">
          <div class="input-main">
            <div class="div-input">
              <el-form ref="pwdForm" :model="form" :rules="rule" size="small" @submit.native.prevent>
                <el-form-item prop="password">
                  <el-input v-model="form.password" maxlength="4" show-password class="real-input" :placeholder="$t('pblink.input_placeholder')" />
                </el-form-item>
              </el-form>
            </div>
          </div>
          <div class="abs-input">
            <div class="input-text">{{ msg }}</div>
          </div>
        </div>

        <div class="auth-root-class">
          <span slot="footer">
            <el-button size="mini" type="primary" @click="refresh">{{ $t('pblink.sure_bt') }}</el-button>
          </span>
        </div>
      </div>

    </div>
  </div>
</template>

<script>
import { validatePwd } from '@/api/link'
import { encrypt } from '@/utils/rsaEncrypt'

export default {
  name: 'LinkPwd',
  props: {
    resourceId: {
      type: String,
      default: null
    }
  },
  data() {
    return {
      msg: null,
      form: { password: null },
      rule: {
        password: [
          { required: true, message: this.$t('pblink.key_pwd'), trigger: 'blur' },
          {
            required: true,
            pattern: /^\d{4}$/,
            message: this.$t('pblink.pwd_format_error'),
            trigger: 'blur'
          }
        ]
      }
    }
  },
  mounted() {
    this.bindKey()
  },
  destroyed() {
    this.unBindKey()
  },
  methods: {
    entryKey(event) {
      const keyCode = event.keyCode
      if (keyCode === 13) {
        this.refresh()
      }
    },
    bindKey() {
      document.addEventListener('keypress', this.entryKey)
    },
    unBindKey() {
      document.removeEventListener('keypress', this.entryKey)
    },
    // 验证密码是否正确 如果正确 设置请求头部带LINK-PWD-TOKEN=entrypt(pwd)再刷新页面
    refresh() {
      this.$refs.pwdForm.validate(valid => {
        if (!valid) return false
        const param = {
          password: encrypt(this.form.password),
          resourceId: this.resourceId
        }
        validatePwd(param).then(res => {
          if (!res.data) {
            this.msg = this.$t('pblink.pwd_error')
          } else {
            window.location.reload()
          }
        })
      })
    }
  }
}
</script>

<style lang="scss" scoped>
    .pwd-body {
        position: absolute;
        width: 100%;
        margin: 0;
        padding: 0;
        top: 0;
        left: 0;
        background-repeat: repeat;
        -webkit-user-select: none;
        -moz-user-select: none;
        -ms-user-select: none;
        -o-user-select: none;
        user-select: none;
        color: #3d4d66;
        font: normal 12px Helvetica Neue,Arial,PingFang SC,Hiragino Sans GB,Microsoft YaHei,微软雅黑,Heiti,黑体,sans-serif;
        -webkit-font-smoothing: antialiased;
        -moz-osx-font-smoothing: grayscale;
        text-decoration: none;
        -kthml-user-focus: normal;
        -moz-user-focus: normal;
        -moz-outline: 0 none;
        outline: 0 none;
        height: 100%;
        display: block;
    }
    .pwd-wrapper {
        background-color: #F7F8FA;
        height: 100%;
        justify-content: center !important;
        align-items: center !important;
        min-height: 25px;
        display: flex;
        -moz-flex-direction: row;
        -o-flex-direction: row;
        flex-direction: row;
        -moz-justify-content: flex-start;
        -ms-justify-content: flex-start;
        -o-justify-content: flex-start;
        justify-content: flex-start;
        -moz-align-items: flex-start;
        -ms-align-items: flex-start;
        -o-align-items: flex-start;
        align-items: flex-start;
        -o-flex-wrap: nowrap;
        flex-wrap: nowrap;
    }
    .pwd-content {
        width: 450px;
        height: 250px;
        position: relative;
        flex-shrink: 0;
        background-color: #FFFFFF;
        display: block;
    }
    .span-header {
        position: relative;
        margin: 57px auto 0px;
        justify-content: center !important;
        align-items: center !important;
    }
    .bi-text {
        max-width: 100%;
        text-align: center;
        white-space: pre;
        text-overflow: ellipsis;
        position: relative;
        flex-shrink: 0;
        box-sizing: border-box;
        overflow: hidden;
        overflow-x: hidden;
        overflow-y: hidden;
        word-break: break-all;
        display: block;
    }
    .input-layout{
        width: 200px;
        position: relative;
        margin: 0px auto;
        padding: 0;
        display: block;
    }
    .input-main {
        width: 192px;
        height: 35px;
        position: relative;
        margin-top: 30px;
        border: 1px solid #e8eaed;
        display: block;
    }
    // .div-input {
    //     inset: 2px 4px;
    // position: absolute;
    // display: block;
    // }
    .abs-input {
        height: 20px;
    position: relative;
    margin-top: 5px;
    display: block;
    }
    .input-text {
        height: 20px;
    line-height: 20px;
    text-align: center;
    white-space: pre;
    text-overflow: ellipsis;
    left: 0px;
    top: 0px;
    bottom: 0px;
    position: absolute;
    color: #E65251;
    box-sizing: border-box;
    }
    // .real-input {
    //     width: 100%;
    //     height: 100%;
    //     border: none;
    //     outline: none;
    //     padding: 0px;
    //     margin: 0px;
    //     inset: 0px;
    //     position: absolute;
    //     display: block;

    // }
    .auth-root-class {
        margin: 15px 0px 5px;
        text-align: center;
    }

</style>

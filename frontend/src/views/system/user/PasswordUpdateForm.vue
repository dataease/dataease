<template>
  <el-form
    ref="createUserForm"
    :model="form"
    :rules="rule"
    size="small"
    label-width="auto"
    label-position="right"
  >
    <el-form-item v-if="!oldPwd" :label="$t('user.origin_passwd')" prop="oldPwd">
      <dePwd v-model="form.oldPwd" />
    </el-form-item>
    <el-form-item :label="$t('user.new_passwd')" prop="newPwd">
      <dePwd v-model="form.newPwd" />
    </el-form-item>
    <el-form-item :label="$t('user.confirm_passwd')" prop="repeatPwd">
      <dePwd v-model="form.repeatPwd" />
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="save">{{
        $t('commons.confirm')
      }}</el-button>
    </el-form-item>
  </el-form>
</template>

<script>
import { updatePersonPwd } from '@/api/system/user'
import dePwd from '@/components/deCustomCm/DePwd.vue'
import { Base64 } from 'js-base64'
export default {
  name: 'PasswordUpdateForm',
  components: { dePwd },
  props: {
    oldPwd: {
      type: String,
      default: ""
    }
  },
  data() {
    return {
      form: {},
      rule: {
        oldPwd: [
          {
            required: true,
            message: this.$t('user.input_password'),
            trigger: 'blur'
          }
        ],
        newPwd: [
          {
            required: true,
            message: this.$t('user.input_password'),
            trigger: 'blur'
          },
          {
            required: true,
            pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[^]{8,30}$/,
            message: this.$t('member.password_format_is_incorrect'),
            trigger: 'blur'
          }
        ],
        repeatPwd: [
          {
            required: true,
            message: this.$t('user.input_password'),
            trigger: 'blur'
          },
          { required: true, trigger: 'blur', validator: this.repeatValidator }
        ]
      }
    }
  },
  created() {
    if (this.oldPwd) {
      this.form.oldPwd = this.oldPwd
    }
  },
  methods: {
    repeatValidator(rule, value, callback) {
      if (value !== this.form.newPwd) {
        callback(new Error(this.$t('member.inconsistent_passwords')))
      } else {
        callback()
      }
    },

    save() {
      this.$refs.createUserForm.validate((valid) => {
        if (valid) {
          const param = {
            password: Base64.encode(this.form.oldPwd),
            newPassword: Base64.encode(this.form.newPwd)
          }
          updatePersonPwd(param).then((res) => {
            this.$success(this.$t('commons.save_success'))
            this.$store.commit('user/SET_PASSWORD_MODIFIED', true)
            this.logout()
          })
        } else {
          return false
        }
      })
    },
    async logout() {
      await this.$store.dispatch('user/logout')
      this.$router.push('/')
    }
  }
}
</script>

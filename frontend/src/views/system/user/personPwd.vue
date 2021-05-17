<template>
  <layout-content header="修改密码">

    <el-form ref="createUserForm" :model="form" :rules="rule" size="small" label-width="auto" label-position="right">
      <el-form-item label="原始密码" prop="oldPwd">
        <el-input v-model="form.oldPwd" type="password" />
      </el-form-item>
      <el-form-item label="新密码" prop="newPwd">
        <el-input v-model="form.newPwd" type="password" />
      </el-form-item>
      <el-form-item label="确认密码" prop="repeatPwd">
        <el-input v-model="form.repeatPwd" type="password" />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="save">保存</el-button>
      </el-form-item>
    </el-form>

  </layout-content>
</template>

<script>
import LayoutContent from '@/components/business/LayoutContent'
import { updatePersonPwd } from '@/api/system/user'
export default {

  components: { LayoutContent },
  data() {
    return {
      form: {

      },
      rule: {

        oldPwd: [
          { required: true, message: this.$t('user.input_password'), trigger: 'blur' }
        //   {
        //     required: true,
        //     pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[^]{6,30}$/,
        //     message: this.$t('member.password_format_is_incorrect'),
        //     trigger: 'blur'
        //   }
        ],
        newPwd: [
          { required: true, message: this.$t('user.input_password'), trigger: 'blur' }
        ],
        repeatPwd: [
          { required: true, message: this.$t('user.input_password'), trigger: 'blur' },
          { required: true, trigger: 'blur', validator: this.repeatValidator }
        ]

      }

    }
  },

  created() {

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
      this.$refs.createUserForm.validate(valid => {
        if (valid) {
          const param = {
            password: this.form.oldPwd,
            newPassword: this.form.newPwd
          }
          updatePersonPwd(param).then(res => {
            this.$success(this.$t('commons.save_success'))
            this.$router.push('/panel/index')
          })
        } else {
          return false
        }
      })
    }
  }
}
</script>

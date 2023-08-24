<template>
        <el-form ref="pluginForm" :model="form" :rules="rule" size="small" label-width="auto" label-position="right">
        <el-form-item label="系统名称" prop="name">
            <el-input v-model="form.name" />
        </el-form-item>

        <el-form-item label="系统logo" prop="logo">
            <el-input v-model="form.logo"  />
        </el-form-item>

        <el-form-item>
            <el-button type="primary" @click="save">保存</el-button>
            <el-button @click="reset">重置</el-button>
        </el-form-item>
        </el-form>
</template>

<script>
export default {
  data () {
    return {
      form: {},
      rule: {
        name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
        logo: [{ required: true, message: '请输入logo', trigger: 'blur' }]
      }
    }
  },
  components: {
  },
  methods: {
    reset () {
      this.$refs.pluginForm.resetFields()
    },
    save () {
      this.$refs.pluginForm.validate(valid => {
        if (valid) {
          const param = {
            url: '/system/ui/info',
            type: 'get',
            loading: true
          }
          this.$emit('execute-axios', param)
        } else {
          return false
        }
      })
    }
  }
}
</script>

<style  scoped>

</style>

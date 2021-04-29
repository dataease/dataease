<template>
  <layout-content :header="formType=='add' ? $t('role.add') : $t('role.modify')" back-name="角色管理">
    <el-form ref="roleForm" :model="form" :rules="rule" size="small" label-width="auto" label-position="right">
      <el-form-item label="角色名称" prop="name">
        <el-input v-model="form.name" />
      </el-form-item>

      <el-form-item label="角色代码" prop="code">
        <el-input v-model="form.code" :disabled="formType !== 'add'" />
      </el-form-item>

      <el-form-item label="描述信息" prop="description">
        <el-input v-model="form.description" type="textarea" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="save">保存</el-button>
        <el-button @click="reset">重置</el-button>
      </el-form-item>
    </el-form>

  </layout-content>
</template>

<script>
import LayoutContent from '@/components/business/LayoutContent'
import { addRole, editRole } from '@/api/system/role'
export default {

  components: { LayoutContent },
  data() {
    return {
      form: {},
      rule: {
        name: [
          { required: true, message: '请输入名称', trigger: 'blur' }
        ],
        code: [{ required: true, message: '请输入代码', trigger: 'blur' }]
      }
    }
  },

  created() {
    if (this.$router.currentRoute.params && this.$router.currentRoute.params.roleId) {
      const row = this.$router.currentRoute.params
      this.edit(row)
    } else {
      this.create()
    }
  },
  methods: {
    create() {
      this.formType = 'add'
    },
    edit(row) {
      this.formType = 'modify'
      this.form = Object.assign({}, row)
    },

    reset() {
      this.$refs.roleForm.resetFields()
    },
    save() {
      this.$refs.roleForm.validate(valid => {
        if (valid) {
          const method = this.formType === 'add' ? addRole : editRole
          method(this.form).then(res => {
            this.$success(this.$t('commons.save_success'))
            this.backToList()
          })
        } else {
          return false
        }
      })
    },
    backToList() {
      this.$router.push({ name: '角色管理' })
    }
  }
}
</script>

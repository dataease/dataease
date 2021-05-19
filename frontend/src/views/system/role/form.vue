<template>
  <layout-content :header="formType=='add' ? $t('role.add') : $t('role.modify')" back-name="system-role">
    <el-form ref="roleForm" :model="form" :rules="rule" size="small" label-width="auto" label-position="right">
      <el-form-item :label="$t('commons.name')" prop="name">
        <el-input v-model="form.name" />
      </el-form-item>
      <el-form-item :label="$t('commons.description')" prop="description">
        <el-input v-model="form.description" type="textarea" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="save">{{ $t('commons.save') }}</el-button>
        <el-button @click="reset">{{ $t('commons.reset') }}</el-button>
      </el-form-item>
    </el-form>

  </layout-content>
</template>

<script>
import LayoutContent from '@/components/business/LayoutContent'
import { addRole, editRole, allRoles } from '@/api/system/role'
export default {

  components: { LayoutContent },
  data() {
    return {
      formType: 'add',
      form: {},
      rule: {
        name: [
          { required: true, trigger: 'blur', validator: this.roleValidator }
        ],
        code: [{ required: true, message: '请输入代码', trigger: 'blur' }]
      },
      roles: [],
      originName: null
    }
  },

  created() {
    if (this.$router.currentRoute.params && this.$router.currentRoute.params.roleId) {
      const row = this.$router.currentRoute.params
      this.edit(row)
    } else {
      this.create()
    }
    this.queryAllRoles()
  },
  methods: {
    create() {
      this.formType = 'add'
    },
    edit(row) {
      this.formType = 'modify'
      this.form = Object.assign({}, row)
      this.originName = row.name
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
    queryAllRoles() {
      allRoles().then(res => {
        this.roles = res.data
      })
    },
    nameRepeat(value) {
      if (!this.roles || this.roles.length === 0) {
        return false
      }
      // 编辑场景 不能 因为名称重复而报错
      if (this.formType === 'modify' && this.originName === value) {
        return false
      }
      return this.roles.some(role => role.name === value)
    },
    roleValidator(rule, value, callback) {
      if (!value || value.length === 0) {
        callback(new Error('请输入名称'))
      } else if (this.nameRepeat(value)) {
        callback(new Error('角色名称已存在'))
      } else {
        callback()
      }
    },
    backToList() {
      this.$router.push({ name: 'system-role' })
    }
  }
}
</script>

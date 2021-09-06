<template>
  <layout-content :header="$t('user.import_ldap') " back-name="system-user">
    <el-form ref="importUserForm" :model="form" :rules="rule" size="small" label-width="auto" label-position="right">

      <el-form-item :label="$t('commons.organization')" prop="deptId">
        <treeselect
          ref="deptTreeSelect"
          v-model="form.deptId"
          :options="depts"
          :load-options="loadDepts"
          :auto-load-root-options="false"
          :placeholder="$t('user.choose_org')"
          @open="filterData"
        />
      </el-form-item>
      <el-form-item :label="$t('commons.role')" prop="roleIds">
        <el-select
          ref="roleSelect"
          v-model="form.roleIds"
          filterable
          style="width: 100%"
          multiple
          :placeholder="$t('commons.please_select')"
          @remove-tag="deleteTag"
          @change="changeRole"
        >
          <el-option
            v-for="item in roles"
            :key="item.name"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="save">{{ $t('commons.confirm') }}</el-button>
        <el-button @click="cancel">{{ $t('commons.cancel') }}</el-button>
      </el-form-item>
    </el-form>

  </layout-content>
</template>

<script>
import LayoutContent from '@/components/business/LayoutContent'
import { getDeptTree, treeByDeptId } from '@/api/system/dept'
import { addUser, allRoles } from '@/api/system/user'
export default {

  components: { LayoutContent },
  data() {
    return {
      form: {
        roles: [{
          id: ''
        }]
      },
      rule: {

        roleIds: [{ required: true, message: this.$t('user.input_roles'), trigger: 'change' }],
        deptId: [],
        enable: []

      },
      defaultForm: { enabled: 1, deptId: null, roleIds: [2] },
      depts: null,
      roles: [],
      roleDatas: [],
      userRoles: []
    }
  },

  created() {
    this.initRoles()
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
        this.save()
      }
    },
    bindKey() {
      document.addEventListener('keypress', this.entryKey)
    },
    unBindKey() {
      document.removeEventListener('keypress', this.entryKey)
    },

    create() {
      this.depts = null
      this.form = Object.assign({}, this.defaultForm)
    },

    initRoles() {
      allRoles().then(res => {
        this.roles = res.data
      })
    },

    // 获取弹窗内部门数据
    loadDepts({ action, parentNode, callback }) {
      if (action === 'LOAD_ROOT_OPTIONS' && !this.form.deptId) {
        const _self = this
        treeByDeptId(0).then(res => {
          const results = res.data.map(node => {
            if (node.hasChildren && !node.children) {
              node.children = null
            }
            return node
          })
          _self.depts = results
          callback()
        })
      }

      if (action === 'LOAD_CHILDREN_OPTIONS') {
        const _self = this
        getDeptTree(parentNode.id).then(res => {
          parentNode.children = res.data.map(function(obj) {
            return _self.normalizer(obj)
          })
          callback()
        })
      }
    },
    normalizer(node) {
      if (node.hasChildren) {
        node.children = null
      }
      return {
        id: node.deptId,
        label: node.name,
        children: node.children
      }
    },
    deleteTag(value) {
      this.userRoles.forEach(function(data, index) {
        if (data.id === value) {
          this.userRoles.splice(index, value)
        }
      }.bind(this))
    },
    changeRole(value) {
      this.userRoles = []
      value.forEach(function(data, index) {
        const role = { id: data }
        this.userRoles.push(role)
      }.bind(this))
    },
    cancel() {
      this.$refs.importUserForm.resetFields()
    },
    save() {
      this.$refs.importUserForm.validate(valid => {
        if (valid) {
          const method = addUser
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
      this.$router.push({ name: 'system-user' })
    },
    filterData(instanceId) {
      this.$refs.roleSelect && this.$refs.roleSelect.blur && this.$refs.roleSelect.blur()
      if (!this.depts) {
        return
      }
      const results = this.depts.map(node => {
        if (node.hasChildren) {
          node.children = null
        }
        return node
      })
      this.depts = results
    }
  }
}
</script>

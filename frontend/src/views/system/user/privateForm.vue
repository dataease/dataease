<template>
  <layout-content>
    <div style="width: 100%;display: flex;justify-content: center;">
      <el-card class="box-card about-card">
        <div class="form-header">
          <span>{{ $t('commons.personal_info') }}</span>
        </div>
        <el-form ref="createUserForm" :disabled="formType !== 'modify'" :model="form" :rules="rule" size="small" label-width="auto" label-position="right">
          <el-form-item label="ID" prop="username">
            <el-input v-model="form.username" disabled />
          </el-form-item>
          <el-form-item :label="$t('commons.phone')" prop="phone">
            <el-input v-model="form.phone" />
          </el-form-item>
          <el-form-item :label="$t('commons.nick_name')" prop="nickName">
            <el-input v-model="form.nickName" />
          </el-form-item>
          <el-form-item :label="$t('commons.email')" prop="email">
            <el-input v-model="form.email" />
          </el-form-item>

          <el-form-item :label="$t('commons.status')">
            <el-radio-group v-model="form.enabled" disabled style="width: 140px">
              <el-radio :label="1">{{ $t('commons.enable') }}</el-radio>
              <el-radio :label="0">{{ $t('commons.disable') }}</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item disabled :label="$t('commons.organization')" prop="dept">
            <treeselect
              v-model="form.deptId"
              disabled
              :options="depts"
              :load-options="loadDepts"
              :auto-load-root-options="false"
              :placeholder="$t('user.choose_org')"
            />
          </el-form-item>
          <el-form-item :label="$t('commons.role')" prop="roleIds">
            <el-select
              v-model="form.roleIds"
              disabled
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
          <!-- <el-form-item>
        <el-button v-if="formType==='modify'" type="primary" @click="save">保存</el-button>
        <el-button v-if="formType==='modify'" @click="reset">重置</el-button>
      </el-form-item> -->
        </el-form>

        <div slot="footer" style="margin-left: 30px;" class="dialog-footer">
          <el-button v-if="formType==='modify'" type="text" @click="reset">{{ $t('commons.cancel') }}</el-button>
          <el-button v-if="formType==='modify'" type="primary" @click="save">{{ $t('commons.confirm') }}</el-button>
          <el-button v-if="formType!=='modify'" type="primary" @click="edit">{{ $t('commons.edit') }}</el-button>
        </div>
      </el-card>
    </div>

  </layout-content>
</template>

<script>
import LayoutContent from '@/components/business/LayoutContent'
import Treeselect from '@riophae/vue-treeselect'
import '@riophae/vue-treeselect/dist/vue-treeselect.css'
import { PHONE_REGEX } from '@/utils/validate'
import { LOAD_CHILDREN_OPTIONS, LOAD_ROOT_OPTIONS } from '@riophae/vue-treeselect'
import { getDeptTree, treeByDeptId } from '@/api/system/dept'
import { allRoles } from '@/api/system/user'
import { updatePerson, persionInfo } from '@/api/system/user'
export default {

  components: { LayoutContent, Treeselect },
  data() {
    return {
      form: {
        roles: [{
          id: ''
        }]
      },
      rule: {
        username: [
          { required: true, message: this.$t('user.input_id'), trigger: 'blur' },
          { min: 1, max: 50, message: this.$t('commons.input_limit', [1, 50]), trigger: 'blur' },
          {
            required: true,
            pattern: '^[^\u4e00-\u9fa5]+$',
            message: this.$t('user.special_characters_are_not_supported'),
            trigger: 'blur'
          }
        ],
        nickName: [
          { required: true, message: this.$t('user.input_name'), trigger: 'blur' },
          { min: 2, max: 50, message: this.$t('commons.input_limit', [2, 50]), trigger: 'blur' },
          {
            required: true,
            message: this.$t('user.special_characters_are_not_supported'),
            trigger: 'blur'
          }
        ],
        phone: [
          {
            pattern: PHONE_REGEX,
            message: this.$t('user.mobile_number_format_is_incorrect'),
            trigger: 'blur'
          }
        ],
        email: [
          { required: true, message: this.$t('user.input_email'), trigger: 'blur' },
          {
            required: true,
            pattern: /^[a-zA-Z0-9_._-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/,
            message: this.$t('user.email_format_is_incorrect'),
            trigger: 'blur'
          }
        ],
        password: [
          { required: true, message: this.$t('user.input_password'), trigger: 'blur' },
          {
            required: true,
            pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[^]{8,30}$/,
            message: this.$t('member.password_format_is_incorrect'),
            trigger: 'blur'
          }
        ],
        newPassword: [
          { required: true, message: this.$t('user.input_password'), trigger: 'blur' },
          {
            required: true,
            pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[^]{8,30}$/,
            message: this.$t('member.password_format_is_incorrect'),
            trigger: 'blur'
          }
        ],
        roleIds: [{ required: true, message: this.$t('user.input_roles'), trigger: 'change' }]

      },
      defaultForm: { id: null, username: null, nickName: null, gender: '男', email: null, enabled: 1, deptId: null, phone: null, roleIds: [] },
      depts: null,
      roles: [],
      roleDatas: [],
      userRoles: [],
      formType: 'add'
    }
  },
  mounted() {
    this.$nextTick(() => {
      this.$store.dispatch('app/toggleSideBarHide', true)
    })
  },
  created() {
    this.$store.dispatch('app/toggleSideBarHide', true)
    this.queryPerson()
    this.initRoles()
  },
  methods: {

    queryPerson() {
      persionInfo().then(res => {
        const info = res.data
        this.form = info
        const roles = info.roles
        this.form['roleIds'] = roles.map(role => role.id)
        if (this.form.deptId === 0) {
          this.form.deptId = null
        }
        this.initDeptTree()
      })
    },

    edit() {
      this.formType = 'modify'
    },
    initRoles() {
      allRoles().then(res => {
        this.roles = res.data
      })
    },
    initDeptTree() {
      treeByDeptId(this.form.deptId || 0).then(res => {
        const results = res.data.map(node => {
          if (node.hasChildren && !node.children) {
            node.children = null
            // delete node.children
          }
          return node
        })
        this.depts = results
      })
    },
    // 获取弹窗内部门数据
    loadDepts({ action, parentNode, callback }) {
      if (action === LOAD_ROOT_OPTIONS && !this.form.deptId) {
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

      if (action === LOAD_CHILDREN_OPTIONS) {
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
    reset() {
      this.formType = 'add'
      this.queryPerson()
    },
    save() {
      this.$refs.createUserForm.validate(valid => {
        if (valid) {
          updatePerson(this.form).then(res => {
            this.$success(this.$t('commons.save_success'))
            this.reset()
          })
        } else {
          return false
        }
      })
    }
  }
}
</script>
<style lang="scss" scoped>
.about-card {
  background: inherit;
  margin-top: 5%;
  flex-direction: row;
  width: 640px;
  min-width: 640px;
  height: auto;
  position: relative;
  >>>div.el-card__header {
    padding: 0;
  }
}
.form-header {
    line-height: 60px;
    font-size: 18px;
}
</style>

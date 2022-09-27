<template>
  <!-- <layout-content :header="formType=='add' ? $t('user.create') : $t('user.modify')" back-name="system-user">
  </layout-content> -->
  <el-dialog
    :title="formType == 'add' ? $t('user.create') : $t('user.modify')"
    :visible.sync="dialogVisible"
    class="user-editer-form"
    width="840px"
    :before-close="reset"
  >
    <div v-if="formType === 'add'" class="editer-form-title">
      <i class="el-icon-info" />
      <span class="pwd" type="text">{{
        $t("commons.default_pwd") + "：" + defaultPWD
      }}</span>
      <el-button
        v-clipboard:copy="defaultPWD"
        v-clipboard:success="onCopy"
        v-clipboard:error="onError"
        class="btn-text"
        type="text"
      >
        {{ $t("commons.copy") }}
      </el-button>
    </div>

    <el-form
      ref="createUserForm"
      :model="form"
      :rules="rule"
      size="small"
      label-width="80px"
      label-position="right"
    >
      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item :label="$t('commons.nick_name')" prop="nickName">
            <el-input
              v-model="form.nickName"
              :placeholder="$t('user.input_name')"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="ID" prop="username">
            <el-input
              v-model="form.username"
              :placeholder="$t('user.input_id')"
              :disabled="formType !== 'add'"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item :label="$t('commons.email')" prop="email">
            <el-input
              v-model="form.email"
              :placeholder="$t('user.input_email')"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item :label="$t('commons.mobile_phone_number')" prop="phone">
            <el-input
              v-model="form.phone"
              :placeholder="$t('commons.mobile_phone')"
              class="input-with-select"
            >
              <el-select
                slot="prepend"
                v-model="form.phonePrefix"
                :placeholder="$t('fu.search_bar.please_select')"
              >
                <el-option label="+86" value="+86" />
              </el-select>
            </el-input>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item :label="$t('commons.gender')" prop="gender">
            <el-select
              v-model="form.gender"
              class="form-gender-select"
              :placeholder="$t('user.select_gender')"
            >
              <el-option :label="$t('commons.man')" value="男" />
              <el-option :label="$t('commons.woman')" value="女" />
              <el-option :label="$t('commons.keep_secret')" value="保密" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item
            v-show="isPluginLoaded"
            :label="$t('commons.organization')"
            prop="deptId"
          >
            <el-popover
              placement="bottom"
              popper-class="user-popper dept"
              width="384"
              trigger="click"
            >
              <el-tree
                v-if="dialogVisible"
                :load="loadNode"
                :lazy="true"
                :expand-on-click-node="false"
                :data="depts"
                :props="defaultProps"
                @node-click="handleNodeClick"
              />

              <el-select
                ref="roleSelect"
                slot="reference"
                v-model="form.deptId"
                clearable
                class="form-gender-select"
                popper-class="tree-select"
                :placeholder="$t('commons.please_select')"
              >
                <el-option
                  v-for="item in selectDepts"
                  :key="item.label"
                  :label="item.label"
                  :value="item.id"
                />
              </el-select>
            </el-popover>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- <el-form-item v-if="formType !== 'modify'" :label="$t('commons.password')" prop="password">
        <el-input v-model="form.password" autocomplete="off" show-password />
      </el-form-item>
      <el-form-item v-if="formType !== 'modify'" :label="$t('commons.confirmPassword')" prop="confirmPassword">
        <el-input v-model="form.confirmPassword" autocomplete="off" show-password />
      </el-form-item> -->
      <el-form-item
        v-show="isPluginLoaded"
        :label="$t('commons.role')"
        prop="roleIds"
      >
        <el-select
          ref="roleSelect"
          v-model="form.roleIds"
          style="width: 100%"
          :disabled="formType !== 'add' && form.isAdmin"
          multiple
          :placeholder="$t('user.input_roles')"
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

      <el-form-item :label="$t('commons.status')" prop="enabled">
        <el-switch
          v-model="form.enabled"
          :disabled="formType !== 'add' && form.isAdmin"
          :active-value="1"
          :inactive-value="0"
        />
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button class="btn normal" @click="reset">{{
        $t("commons.cancel")
      }}</el-button>
      <el-button class="btn" type="primary" @click="save">{{
        $t("commons.confirm")
      }}</el-button>
    </span>
  </el-dialog>
</template>

<script>
import { PHONE_REGEX } from '@/utils/validate'
import { getDeptTree, treeByDeptId } from '@/api/system/dept'
import { addUser, editUser, allRoles, queryAssist } from '@/api/system/user'
import { pluginLoaded, defaultPwd, wecomStatus, dingtalkStatus, larkStatus } from '@/api/user'
export default {
  data() {
    return {
      defaultProps: {
        children: 'children',
        label: 'label',
        isLeaf: 'leaf'
      },
      selectDepts: [],
      form: {
        roles: [
          {
            id: ''
          }
        ],
        sysUserAssist: {
          wecomId: null,
          dingtalkId: null,
          larkId: null
        }
      },
      rule: {
        username: [
          {
            required: true,
            message: this.$t('user.id_mandatory'),
            trigger: 'blur'
          },
          {
            min: 1,
            max: 50,
            message: this.$t('commons.input_limit', [1, 50]),
            trigger: 'blur'
          },
          {
            required: true,
            pattern: '^[^\u4e00-\u9fa5]+$',
            message: this.$t('user.special_characters_are_not_supported'),
            trigger: 'blur'
          }
        ],
        nickName: [
          {
            required: true,
            message: this.$t('user.name_mandatory'),
            trigger: 'blur'
          },
          {
            min: 2,
            max: 50,
            message: this.$t('commons.input_limit', [2, 50]),
            trigger: 'blur'
          },
          {
            required: true,
            message: this.$t('user.special_characters_are_not_supported'),
            trigger: 'blur'
          }
        ],
        phone: [
          {
            pattern: PHONE_REGEX,
            message: this.$t('user.phone_format'),
            trigger: 'blur'
          }
        ],
        email: [
          {
            required: true,
            message: this.$t('user.email_mandatory'),
            trigger: 'blur'
          },
          {
            required: true,
            pattern: /^[a-zA-Z0-9_._-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/,
            message: this.$t('user.email_format_is_incorrect'),
            trigger: 'blur'
          }
        ],
        password: [
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
        confirmPassword: [
          {
            required: true,
            message: this.$t('user.input_password'),
            trigger: 'blur'
          },
          { required: true, validator: this.repeatValidator, trigger: 'blur' }
        ],
        newPassword: [
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
        deptId: [],
        gender: [],
        enabled: [{ required: true, trigger: 'change' }]
      },
      defaultForm: {
        id: null,
        username: null,
        nickName: null,
        gender: '男',
        email: null,
        enabled: 1,
        deptId: null,
        phone: null,
        phonePrefix: '+86',
        roleIds: [2],
        sysUserAssist: {
          wecomId: null,
          dingtalkId: null,
          larkId: null
        }
      },
      depts: [],
      roles: [],
      roleDatas: [],
      userRoles: [],
      formType: 'add',
      isPluginLoaded: false,
      defaultPWD: 'DataEase123..',
      dialogVisible: false,
      wecomOpen: false,
      dingTalkOpen: false,
      larkOpen: false,
      assistInfo: {}
    }
  },
  beforeCreate() {
    pluginLoaded().then((res) => {
      this.isPluginLoaded = res.success && res.data
    })
    wecomStatus().then(res => {
      if (res.success && res.data) {
        this.wecomOpen = true
      }
    })

    dingtalkStatus().then(res => {
      if (res.success && res.data) {
        this.dingTalkOpen = true
      }
    })

    larkStatus().then(res => {
      if (res.success && res.data) {
        this.larkOpen = true
      }
    })
    defaultPwd().then((res) => {
      if (res && res.data) {
        this.defaultPWD = res.data
      }
    })
  },
  methods: {
    repeatValidator(rule, value, callback) {
      if (value !== this.form.password) {
        callback(new Error(this.$t('member.inconsistent_passwords')))
      } else {
        callback()
      }
    },
    create() {
      this.formType = 'add'
      this.form = Object.assign({}, JSON.parse(JSON.stringify(this.defaultForm)))
    },
    init(row) {
      this.initRoles()

      this.dialogVisible = true
      if (!row) {
        this.create()
        return
      }
      this.initAssistInfo(row.userId).then(res => {
        this.assistInfo = res.data
        const { deptId: id, deptName: label } = (row.dept || {})
        this.selectDepts = [{ id, label }]
        this.formType = 'modify'
        this.dialogVisible = true
        row.sysUserAssist = JSON.parse(JSON.stringify(this.defaultForm.sysUserAssist))
        this.form = Object.assign({}, row)
        this.form.password = ''
        if (this.form.deptId === 0) {
          this.form.deptId = null
        }

        if (!this.form.phonePrefix) {
          this.form.phonePrefix = '+86'
        }

        if (this.assistInfo) {
          const info = JSON.parse(JSON.stringify(this.assistInfo))
          delete info.needFirstNoti
          delete info.userId
          const assist = Object.assign(JSON.parse(JSON.stringify(this.defaultForm.sysUserAssist)), info)
          this.form.sysUserAssist = assist
        }
      })
    },
    initRoles() {
      allRoles().then((res) => {
        this.roles = res.data
      })
    },
    initAssistInfo(userId) {
      return queryAssist(userId)
    },
    handleNodeClick({ id, label }) {
      const [dept] = this.selectDepts
      if (!dept || dept.id !== id) {
        this.selectDepts = [{ id, label }]
        this.form.deptId = id
        return
      }

      if (dept.id === id) {
        this.selectDepts = []
        this.form.deptId = null
      }
    },
    // 获取弹窗内部门数据
    treeByDeptId() {
      treeByDeptId(0).then((res) => {
        this.depts = (res.data || []).map(ele => {
          return {
            ...ele,
            leaf: !ele.hasChildren
          }
        })
      })
    },
    loadNode(node, resolve) {
      if (!this.depts.length) {
        this.treeByDeptId()
        return
      }
      getDeptTree(node.data.id).then((res) => {
        resolve(
          res.data.map((dept) => {
            return this.normalizer(dept)
          })
        )
      })
    },
    normalizer(node) {
      return {
        id: node.deptId,
        label: node.name,
        leaf: !node.hasChildren
      }
    },
    deleteTag(value) {
      this.userRoles.forEach(
        function(data, index) {
          if (data.id === value) {
            this.userRoles.splice(index, value)
          }
        }.bind(this)
      )
    },
    changeRole(value) {
      this.userRoles = []
      value.forEach(
        function(data, index) {
          const role = { id: data }
          this.userRoles.push(role)
        }.bind(this)
      )
    },
    reset() {
      this.depts = []
      this.form.sysUserAssist = JSON.parse(JSON.stringify(this.defaultForm.sysUserAssist))
      this.$refs.createUserForm.resetFields()
      this.dialogVisible = false
    },
    save() {
      this.$refs.createUserForm.validate((valid) => {
        if (valid) {
          // !this.form.deptId && (this.form.deptId = 0)
          const method = this.formType === 'add' ? addUser : editUser
          method(this.form).then((res) => {
            this.$success(this.$t('commons.save_success'))
            this.reset()
            this.$emit('saved')
          })
        } else {
          return false
        }
      })
    },
    onCopy(e) {
      this.$success(this.$t('commons.copy_success'))
    },
    onError(e) {}
  }
}
</script>

<style lang="scss" scoped>
.user-editer-form {
  ::v-deep .el-dialog__body {
    padding: 0 24px 24px 24px;
  }

  ::v-deep .el-dialog__header {
    padding: 24px 24px 16px 24px;
  }

  ::v-deep .el-dialog__footer {
    padding-top: 0;
  }

  .editer-form-title {
    width: 100%;
    border-radius: 4px;
    background: #e1eaff;
    padding: 9px 16px;
    margin-bottom: 16px;

    i {
      color: #3370ff;
      font-size: 14.666666030883789px;
    }

    .pwd,
    .btn-text {
      font-family: PingFang SC;
      font-size: 14px;
      font-weight: 400;
      line-height: 22px;
      text-align: left;
    }

    .pwd {
      margin: 0 8px;
      color: #1f2329;
    }

    .btn-text {
      padding: 0;
      border: none;
    }
  }

  ::v-deep .el-form-item__label {
    width: 100% !important;
    text-align: left;
  }

  ::v-deep
    .el-form-item.is-required:not(.is-no-asterisk)
    > .el-form-item__label:before {
    display: none;
  }

  ::v-deep
    .el-form-item.is-required:not(.is-no-asterisk)
    > .el-form-item__label::after {
    content: "*";
    color: #f54a45;
    margin-left: 2px;
  }

  ::v-deep .el-form-item__content {
    margin-left: 0 !important;
  }

  .input-with-select {
    ::v-deep .el-input-group__prepend {
      background-color: #fff;
    }
    .el-select {
      ::v-deep .el-input__inner {
        width: 72px;
      }
    }
  }

  .btn {
    border-radius: 4px;
    padding: 5px 26px 5px 26px;
    font-family: PingFang SC;
    font-size: 14px;
    font-weight: 400;
    line-height: 20px;
    letter-spacing: 0px;
    text-align: center;
    border: none;
    box-sizing: border-box;
  }

  .normal {
    color: #1f2329;
    border: 1px solid #bbbfc4;
    margin-left: 12px;
  }

  .form-gender-select {
    width: 100%;
  }
}

</style>
<style lang="scss">
.tree-select {
  display: none !important;
}
.user-popper {
  background: #fff;
  padding: 0;
  .popper__arrow {
    display: none;
  }
}
.user-popper.dept {
  height: 300px;
  overflow: auto;
}
</style>

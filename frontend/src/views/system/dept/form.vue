<template>
  <layout-content :header="formType=='add' ? $t('organization.create') : $t('organization.modify')" back-name="system-dept" style="height: 100%;">
    <el-form ref="deptForm" :model="form" :rules="rule" size="small" label-width="auto" label-position="right">
      <el-form-item :label="$t('organization.name')" prop="name">
        <el-input v-model="form.name" />
      </el-form-item>
      <el-form-item :label="$t('organization.sort')" prop="deptSort">
        <el-input-number

          v-model.number="form.deptSort"
          :min="0"
          :max="999"
          controls-position="right"
        />
      </el-form-item>

      <el-form-item :label="$t('organization.top_org')" prop="top">
        <el-radio-group v-model="form.top" @change="topChange">
          <el-radio :label="true">{{ $t('commons.yes') }}</el-radio>
          <el-radio :label="false">{{ $t('commons.no') }}</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item v-if="!form.top" :label="$t('organization.parent_org')" prop="pid">
        <treeselect
          v-model="form.pid"
          :auto-load-root-options="false"
          :load-options="loadDepts"
          :options="depts"
          :placeholder="$t('organization.select_parent_org')"
        />
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
import Treeselect from '@riophae/vue-treeselect'
import '@riophae/vue-treeselect/dist/vue-treeselect.css'
import { LOAD_CHILDREN_OPTIONS, LOAD_ROOT_OPTIONS } from '@riophae/vue-treeselect'
import { getDeptTree, treeByDeptId, addDept, editDept } from '@/api/system/dept'
export default {

  components: { LayoutContent, Treeselect },
  data() {
    return {
      defaultForm: { deptId: null, top: true, pid: null },
      maps: new Map(),
      form: {},
      rule: {
        name: [
          { required: true, message: this.$t('organization.input_name'), trigger: 'blur' },
          { min: 2, max: 25, message: this.$t('commons.input_limit', [2, 25]), trigger: 'blur' }
        ],
        description: [
          { max: 50, message: this.$t('commons.input_limit', [0, 50]), trigger: 'blur' }
        ]
      },
      depts: null,

      formType: 'add'
    }
  },

  created() {
    if (this.$router.currentRoute.params && this.$router.currentRoute.params.deptId) {
      const row = this.$router.currentRoute.params
      this.edit(row)
    } else {
      this.create()
    }
  },
  methods: {
    create() {
      this.formType = 'add'
      this.form = Object.assign({}, this.defaultForm)
    },
    edit(row) {
      this.formType = 'modify'
      this.form = Object.assign({}, row)
      this.initDeptTree()
    },

    initDeptTree() {
      treeByDeptId(this.form.pid || 0).then(res => {
        const results = res.data.map(node => {
          if (node.hasChildren && !node.children) {
            node.children = null
          }
          return node
        })
        this.depts = results
      })
    },
    // 获取弹窗内部门数据
    loadDepts({ action, parentNode, callback }) {
      if (action === LOAD_ROOT_OPTIONS && !this.form.pid) {
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
    topChange(value) {
      if (!value) {
        this.form.pid = null
        this.depts = null
      }
    },
    reset() {
      this.$refs.deptForm.resetFields()
    },
    save() {
      this.$refs.deptForm.validate(valid => {
        if (valid) {
          const method = this.formType === 'add' ? addDept : editDept
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
      this.$router.push({ name: 'system-dept' })
    }
  }
}
</script>

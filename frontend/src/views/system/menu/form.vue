<template>
  <layout-content :header="formType=='add' ? $t('menu.create') : $t('menu.modify')" back-name="system-menu">
    <el-form ref="menuForm" :model="form" :rules="rule" size="small" label-width="auto" label-position="right">
      <el-form-item :label="$t('menu.menu_type')" prop="type">
        <el-radio-group v-model="form.type" size="mini" :disabled="formType!=='add'">
          <el-radio-button label="0">{{ $t('commons.catalogue') }}</el-radio-button>
          <el-radio-button label="1">{{ $t('commons.menu') }}</el-radio-button>
          <el-radio-button label="2">{{ $t('commons.button') }}</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="form.type=== 1 && form.icon" :label="$t('commons.icon')" prop="icon">
        <el-popover
          placement="bottom-start"
          width="425"
          trigger="click"
          @show="$refs['iconSelect'].reset()"
        >
          <IconSelect ref="iconSelect" @selected="selected" />
          <el-input slot="reference" v-model="form.icon" :placeholder="$t('menu.select_icon')" readonly>
            <svg-icon v-if="form.icon" slot="prefix" :icon-class="form.icon" class="el-input__icon" style="height: 32px;width: 16px;" />
            <i v-else slot="prefix" class="el-icon-search el-input__icon" />
          </el-input>
        </el-popover>
      </el-form-item>

      <el-form-item v-if="form.type !== 2" :label="$t('menu.tile')" prop="title">
        <el-input v-model="form.title" :placeholder="$t('menu.tile')" />
      </el-form-item>
      <el-form-item v-if="form.type === 2" :label="$t('menu.button_name')" prop="title">
        <el-input v-model="form.title" :placeholder="$t('menu.button_name')" />
      </el-form-item>
      <el-form-item v-if="form.type !== 2" :label="$t('menu.menu_sort')" prop="menuSort">
        <el-input-number v-model.number="form.menuSort" :min="0" :max="999" controls-position="right" />
      </el-form-item>
      <el-form-item :label="$t('menu.parent_category')" prop="pid">
        <treeselect
          v-model="form.pid"
          :disabled="formType!=='add'"
          :options="menus"
          :load-options="loadMenus"
          :placeholder="$t('menu.parent_category')"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="save">{{ $t('commons.reset') }}</el-button>
        <el-button @click="reset">{{ $t('commons.confirm') }}</el-button>
      </el-form-item>
    </el-form>

  </layout-content>
</template>

<script>
import LayoutContent from '@/components/business/LayoutContent'
import Treeselect from '@riophae/vue-treeselect'
import IconSelect from '@/components/IconSelect'
import '@riophae/vue-treeselect/dist/vue-treeselect.css'
import { LOAD_CHILDREN_OPTIONS, LOAD_ROOT_OPTIONS } from '@riophae/vue-treeselect'
import { addMenu, editMenu, getMenusTree, treeByMenuId } from '@/api/system/menu'
export default {

  components: { LayoutContent, Treeselect, IconSelect },
  data() {
    return {
      topMunu: { id: 0, label: '顶级目录', children: null },
      defaultForm: { menuId: null, title: null, menuSort: 999, path: null, component: null, componentName: null, iframe: false, pid: 0, icon: null, cache: false, hidden: false, type: 0, permission: null },
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
      menus: null,
      maps: new Map(),
      formType: 'add'
    }
  },

  created() {
    if (this.$router.currentRoute.params && this.$router.currentRoute.params.menuId) {
      const row = this.$router.currentRoute.params
      this.edit(row)
    } else {
      this.create()
    }
    this.initData()
  },
  methods: {
    create() {
      this.formType = 'add'
      this.form = Object.assign({}, this.defaultForm)
    },
    edit(row) {
      this.formType = 'modify'
      this.form = Object.assign({}, row)
      this.initMenuTree()
    },
    initMenuTree() {
      treeByMenuId(this.form.pid || 0).then(res => {
        const results = res.data.map(node => {
          if (node.hasChildren && !node.children) {
            node.children = null
          }
          return node
        })
        this.menus = results
      })
    },

    // 获取弹窗内部门数据
    loadMenus({ action, parentNode, callback }) {
      if (action === LOAD_ROOT_OPTIONS) {
        const _self = this
        !this.menus && getMenusTree('0').then(res => {
          _self.menus = res.data.map(node => _self.normalizer(node))
          callback()
        })
      }

      if (action === LOAD_CHILDREN_OPTIONS) {
        const _self = this
        getMenusTree(parentNode.id).then(res => {
          parentNode.children = res.data.map(function(obj) {
            return _self.normalizer(obj)
          })
          callback()
        })
      }
    },
    initData() {
      this.menus = []
      this.menus.push(this.topMunu)
    },
    normalizer(node) {
      if (node.hasChildren) {
        node.children = null
      }
      return {
        id: node.menuId,
        label: node.title,
        children: node.children
      }
    },

    selected(name) {
      this.form.icon = name
    },

    reset() {
      this.$refs.menuForm.resetFields()
    },
    save() {
      this.$refs.menuForm.validate(valid => {
        if (valid) {
          const method = this.formType === 'add' ? addMenu : editMenu
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
      this.$router.push({ name: 'system-menu' })
    }
  }
}
</script>

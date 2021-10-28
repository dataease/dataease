<template>
  <de-container>
    <de-aside-container>
      <el-tabs v-model="currentTemplateType" @tab-click="handleClick">
        <el-tab-pane name="system">
          <span slot="label"><i class="el-icon-document tablepanel-i" /> {{ $t('panel.sys_template') }}</span>
          <template-list
            v-if="currentTemplateType==='system'"
            :template-type="currentTemplateType"
            :template-list="templateList"
            @templateDelete="templateDelete"
            @templateEdit="templateEdit"
            @showCurrentTemplate="showCurrentTemplate"
            @templateImport="templateImport"
            @showTemplateEditDialog="showTemplateEditDialog"
          />
        </el-tab-pane>
        <el-tab-pane name="self">
          <span slot="label"><i class="el-icon-star-off tablepanel-i" />{{ $t('panel.user_template') }}</span>
          <!--v-if 重新渲染 强制刷新首行高亮属性-->
          <template-list
            v-if="currentTemplateType==='self'"
            :template-type="currentTemplateType"
            :template-list="templateList"
            @templateDelete="templateDelete"
            @templateEdit="templateEdit"
            @showCurrentTemplate="showCurrentTemplate"
            @templateImport="templateImport"
            @showTemplateEditDialog="showTemplateEditDialog"
          />
        </el-tab-pane>
      </el-tabs>
    </de-aside-container>
    <de-main-container>
      <template-item
        v-for="item in currentTemplateShowList"
        :key="item.id"
        :template="item"
        @templateDelete="templateDelete"
        @templateEdit="templateEdit"
      />
    </de-main-container>
    <el-dialog :title="dialogTitle" :visible="editTemplate" :show-close="false" width="30%">
      <el-form ref="templateEditForm" :model="templateEditForm" :rules="templateEditFormRules">
        <el-form-item :label="$t('commons.name')" prop="name">
          <el-input v-model="templateEditForm.name" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="close()">{{ $t('commons.cancel') }}</el-button>
        <el-button type="primary" size="mini" @click="saveTemplateEdit(templateEditForm)">{{ $t('commons.confirm') }}
        </el-button>
      </div>
    </el-dialog>

    <!--导入templatedialog-->
    <el-dialog :title="templateDialog.title" :visible.sync="templateDialog.visible" :show-close="true" width="600px">
      <template-import
        v-if="templateDialog.visible"
        :pid="templateDialog.pid"
        @closeEditTemplateDialog="closeEditTemplateDialog"
      />
    </el-dialog>
  </de-container>
</template>

<script>
import DeMainContainer from '@/components/dataease/DeMainContainer'
import DeContainer from '@/components/dataease/DeContainer'
import DeAsideContainer from '@/components/dataease/DeAsideContainer'
import TemplateList from './component/TemplateList'
import TemplateItem from './component/TemplateItem'
import TemplateImport from './component/TemplateImport'
import { save, templateDelete, find } from '@/api/system/template'

export default {
  name: 'PanelMain',
  components: { DeMainContainer, DeContainer, DeAsideContainer, TemplateList, TemplateItem, TemplateImport },
  data() {
    return {
      showShare: false,
      currentTemplateShowList: [],
      currentPid: '',
      currentTemplateType: 'system',
      templateEditFormRules: {
        name: [
          { required: true, message: this.$t('commons.input_content'), trigger: 'change' },
          { max: 50, message: this.$t('commons.char_can_not_more_50'), trigger: 'change' }
        ]
      },
      templateEditForm: {},
      editTemplate: false,
      dialogTitle: '',
      templateList: [],
      templateDialog: {
        title: this.$t('panel.import_template'),
        visible: false,
        pid: ''
      }
    }
  },
  mounted() {
    this.getTree()
  },
  methods: {
    handleClick(tab, event) {
      this.getTree()
    },
    showCurrentTemplate(pid) {
      this.currentTemplateId = pid
      if (this.currentTemplateId) {
        find({ pid: this.currentTemplateId }).then(response => {
          this.currentTemplateShowList = response.data
        })
      }
    },
    templateDelete(id) {
      if (id) {
        templateDelete(id).then(response => {
          this.$message({
            message: this.$t('commons.delete_success'),
            type: 'success',
            showClose: true
          })
          this.getTree()
        })
      }
    },
    showTemplateEditDialog(type, templateInfo) {
      if (type === 'edit') {
        this.dialogTitle = this.$t('commons.edit')
        this.templateEditForm = JSON.parse(JSON.stringify(templateInfo))
      } else {
        this.dialogTitle = this.$t('commons.create')
        this.templateEditForm = { name: '', nodeType: 'folder', templateType: this.currentTemplateType, level: 0 }
      }
      this.editTemplate = true
    },
    templateEdit(templateInfo) {
      this.showTemplateEditDialog('edit', templateInfo)
    },
    saveTemplateEdit(templateEditForm) {
      this.$refs['templateEditForm'].validate((valid) => {
        if (valid) {
          save(templateEditForm).then(response => {
            this.close()
            this.$message({
              message: this.$t('commons.save_success'),
              type: 'success',
              showClose: true
            })
            this.getTree()
          })
        } else {
          return false
        }
      })
    },
    close() {
      this.$refs['templateEditForm'].resetFields()
      this.editTemplate = false
    },
    getTree() {
      const request = {
        templateType: this.currentTemplateType,
        level: '0'
      }
      find(request).then(res => {
        this.templateList = res.data
        this.showFirst()
      })
    },
    showFirst() {
      // 判断是否默认点击第一条
      if (this.templateList && this.templateList.length > 0) {
        let showFirst = true
        this.templateList.forEach(template => {
          if (template.id === this.currentTemplateId) {
            showFirst = false
          }
        })
        if (showFirst) {
          this.$nextTick().then(() => {
            const firstNode = document.querySelector('.el-tree-node')
            firstNode.click()
          })
        } else {
          this.showCurrentTemplate(this.currentTemplateId)
        }
      } else {
        this.currentTemplateShowList = []
      }
    },
    closeEditTemplateDialog() {
      this.templateDialog.visible = false
      this.showCurrentTemplate(this.templateDialog.pid)
    },
    templateImport(pid) {
      this.templateDialog.visible = true
      this.templateDialog.pid = pid
    }
  }
}
</script>

<style scoped>
  .el-card-template {
    min-width: 260px;
    min-width: 460px;
    width: 100%;
    height: 100%;
  }

</style>

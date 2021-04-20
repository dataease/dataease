<template>
  <de-container>
    <de-aside-container>
      <el-tabs v-model="currentTemplateType" @tab-click="handleClick">
        <el-tab-pane name="system">
          <span slot="label"><i class="el-icon-document" />系统模板</span>
          <system-template-list :system-template-list="systemTemplateList" @showTemplateEditDialog="showTemplateEditDialog" />
        </el-tab-pane>
        <el-tab-pane name="self">
          <span slot="label"><i class="el-icon-star-off" />用户模板</span>
          开发中...
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
        <el-form-item label="名称" prop="name">
          <el-input v-model="templateEditForm.name" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="close()">{{ $t('panel.cancel') }}</el-button>
        <el-button type="primary" size="mini" @click="saveTemplateEdit(templateEditForm)">{{ $t('panel.confirm') }}
        </el-button>
      </div>
    </el-dialog>
  </de-container>
</template>

<script>
import DeMainContainer from '@/components/dataease/DeMainContainer'
import DeContainer from '@/components/dataease/DeContainer'
import DeAsideContainer from '@/components/dataease/DeAsideContainer'
import SystemTemplateList from './component/SystemTemplateList'
import TemplateItem from './component/TemplateItem'
import { get, post } from '@/api/panel/panel'
import { deepCopy } from '../../../components/canvas/utils/utils'

export default {
  name: 'PanelMain',
  components: { DeMainContainer, DeContainer, DeAsideContainer, SystemTemplateList, TemplateItem },
  data() {
    return {
      showShare: false,
      currentTemplateShowList: [],
      currentPid: '',
      currentTemplateType: 'system',
      templateEditFormRules: {
        name: [
          { required: true, message: this.$t('commons.input_content'), trigger: 'change' }
        ]
      },
      templateEditForm: {},
      editTemplate: false,
      dialogTitle: '',
      systemTemplateList: []
    }
  },
  mounted() {
    this.getTree()
  },
  methods: {
    handleClick(tab, event) {

    },
    showCurrentTemplate(pid) {
      this.currentPid = pid
      if (this.currentPid) {
        post('/template/templateList', { pid: this.currentPid }).then(response => {
          this.currentTemplateShowList = response.data
        })
      }
    },
    templateDelete(id) {
      if (id) {
        post('/template/delete/' + id, null).then(response => {
          this.$message({
            message: '删除成功',
            type: 'success',
            showClose: true
          })
          this.showCurrentTemplate(this.currentPid)
        })
      }
    },
    saveTemplate(templateEditForm) {
      if (templateEditForm) {
        post('/template/save', templateEditForm).then(response => {
          this.$message({
            message: '删除成功',
            type: 'success',
            showClose: true
          })
          this.showCurrentTemplate(this.currentPid)
        })
      }
    },
    showTemplateEditDialog(type, templateInfo) {
      if (type === 'edit') {
        this.dialogTitle = '编辑'
        this.templateEditForm = deepCopy(templateInfo)
      } else {
        this.dialogTitle = '新建'
        this.templateEditForm = { name: '', templateType: this.currentTemplateType, level: 0 }
      }
      this.editTemplate = true
    },
    templateEdit(templateInfo) {
      this.showTemplateEditDialog('edit', templateInfo)
    },
    saveTemplateEdit(templateEditForm) {
      debugger
      post('/template/save', templateEditForm).then(response => {
        this.$message({
          message: '保存成功',
          type: 'success',
          showClose: true
        })
        this.editTemplate = false
        this.getTree()
      })
    },
    close() {
      this.editTemplate = false
    },
    getTree() {
      const request = {
        templateType: this.currentTemplateType,
        level: '0'
      }
      post('/template/templateList', request).then(res => {
        this.systemTemplateList = res.data
        if (this.systemTemplateList && this.systemTemplateList.length > 0) {
          this.showCurrentTemplate(this.systemTemplateList[0].id)
        }
      })
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

<template>
  <de-container>
    <de-aside-container>
      <el-tabs v-model="currentTemplateType" @tab-click="handleClick">
        <el-tab-pane name="system">
          <span slot="label"><i class="el-icon-document" /> {{ $t('panel.sys_template') }}</span>
          <template-list v-if="currentTemplateType==='system'" :template-type="currentTemplateType" :template-list="templateList"
                         @templateDelete="templateDelete" @templateEdit="templateEdit" @showCurrentTemplate="showCurrentTemplate"
                         @showTemplateEditDialog="showTemplateEditDialog" />
        </el-tab-pane>
        <el-tab-pane name="self">
          <span slot="label"><i class="el-icon-star-off" />{{ $t('panel.user_template') }}</span>
          <!--v-if 重新渲染 强制刷新首行高亮属性-->
          <template-list v-if="currentTemplateType==='self'" :template-type="currentTemplateType" :template-list="templateList"
                         @templateDelete="templateDelete" @templateEdit="templateEdit" @showCurrentTemplate="showCurrentTemplate"
                         @showTemplateEditDialog="showTemplateEditDialog" />
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
  </de-container>
</template>

<script>
import DeMainContainer from '@/components/dataease/DeMainContainer'
import DeContainer from '@/components/dataease/DeContainer'
import DeAsideContainer from '@/components/dataease/DeAsideContainer'
import TemplateList from './component/TemplateList'
import TemplateItem from './component/TemplateItem'
import { get, post } from '@/api/panel/panel'

export default {
  name: 'PanelMain',
  components: { DeMainContainer, DeContainer, DeAsideContainer, TemplateList, TemplateItem },
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
      templateList: []
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
        post('/template/templateList', { pid: this.currentTemplateId }).then(response => {
          this.currentTemplateShowList = response.data
        })
      }
    },
    templateDelete(id) {
      if (id) {
        post('/template/delete/' + id, null).then(response => {
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
      post('/template/save', templateEditForm).then(response => {
        this.$message({
          message: this.$t('commons.save_success'),
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

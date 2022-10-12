<template>
  <de-layout-content>
    <div class="de-template">
      <el-tabs
        v-model="currentTemplateType"
        class="de-tabs"
        @tab-click="handleClick"
      >
        <el-tab-pane name="self">
          <span slot="label">{{ $t("panel.user_template") }}</span>
        </el-tab-pane>
        <el-tab-pane
          v-permission="['sys-template:read']"
          name="system"
        >
          <span
            slot="label"
            v-permission="['sys-template:read']"
          >
            {{ $t("panel.sys_template") }}</span>
        </el-tab-pane>
      </el-tabs>
      <div
        v-loading="$store.getters.loadingMap[$store.getters.currentPath]"
        class="tabs-container flex-tabs"
      >
        <div class="de-tabs-left">
          <template-list
            ref="templateList"
            :template-type="currentTemplateType"
            :template-list="templateList"
            @templateDelete="templateFolderDelete"
            @templateEdit="templateEdit"
            @showCurrentTemplate="showCurrentTemplate"
            @templateImport="templateImport"
            @showTemplateEditDialog="showTemplateEditDialog"
          />
        </div>
        <div class="de-tabs-right">
          <div
            v-if="currentTemplateLabel"
            class="active-template"
          >
            {{ currentTemplateLabel }}&nbsp;&nbsp;({{
              currentTemplateShowList.length
            }})
            <deBtn
              type="primary"
              icon="el-icon-upload2"
              @click="templateImport(currentTemplateId)"
            >
              {{ $t("panel.import") }}
            </deBtn>
          </div>
          <el-empty
            v-if="!currentTemplateShowList.length"
            :image="noneImg"
            :description="$t('components.no_template')"
          />
          <div
            v-show="currentTemplateId !== ''"
            id="template-box"
            class="template-box"
          >
            <template-item
              v-for="item in currentTemplateShowList"
              :key="item.id"
              :width="templateCurWidth"
              :model="item"
              @command="(key) => handleCommand(key, item)"
            />
          </div>
        </div>
      </div>
    </div>
    <el-dialog
      :title="dialogTitle"
      :visible.sync="editTemplate"
      append-to-body
      class="de-dialog-form"
      width="600px"
    >
      <el-form
        ref="templateEditForm"
        class="de-form-item"
        :model="templateEditForm"
        :rules="templateEditFormRules"
      >
        <el-form-item
          :label="dialogTitleLabel"
          prop="name"
        >
          <el-input v-model="templateEditForm.name" />
        </el-form-item>
      </el-form>
      <div
        slot="footer"
        class="dialog-footer"
      >
        <deBtn
          secondary
          @click="close()"
        >{{ $t("commons.cancel") }}</deBtn>
        <deBtn
          type="primary"
          @click="saveTemplateEdit(templateEditForm)"
        >{{ $t("commons.confirm") }}
        </deBtn>
      </div>
    </el-dialog>

    <!--导入templatedialog-->
    <el-dialog
      :title="templateDialog.title"
      :visible.sync="templateDialog.visible"
      :show-close="true"
      class="de-dialog-form"
      width="600px"
    >
      <template-import
        v-if="templateDialog.visible"
        :pid="templateDialog.pid"
        @refresh="showCurrentTemplate(currentTemplateId,
                                      currentTemplateLabel)"
        @closeEditTemplateDialog="closeEditTemplateDialog"
      />
    </el-dialog>
  </de-layout-content>
</template>

<script>
import DeLayoutContent from '@/components/business/DeLayoutContent'
import TemplateList from './component/TemplateList'
import TemplateItem from './component/TemplateItem'
import TemplateImport from './component/TemplateImport'
import { save, templateDelete, find } from '@/api/system/template'
import elementResizeDetectorMaker from 'element-resize-detector'

import msgCfm from '@/components/msgCfm/index'
export default {
  name: 'PanelMain',
  components: { DeLayoutContent, TemplateList, TemplateItem, TemplateImport },
  mixins: [msgCfm],
  data() {
    return {
      showShare: false,
      currentTemplateShowList: [],
      noneImg: require('@/assets/None.png'),
      currentPid: '',
      currentTemplateType: 'self',
      templateEditFormRules: {
        name: [
          { required: true, trigger: 'blur', validator: this.roleValidator },
          {
            required: true,
            message: this.$t('commons.input_content'),
            trigger: 'blur'
          },
          {
            max: 50,
            message: this.$t('commons.char_can_not_more_50'),
            trigger: 'change'
          }
        ]
      },
      templateEditForm: {},
      editTemplate: false,
      dialogTitle: '',
      dialogTitleLabel: '',
      currentTemplateLabel: '',
      currentTemplateId: '',
      templateList: [],
      templateMiniWidth: 286,
      templateCurWidth: 286,
      formType: '',
      originName: '',
      templateDialog: {
        title: this.$t('panel.import_template'),
        visible: false,
        pid: ''
      }
    }
  },
  computed: {
    nameList() {
      const { nodeType } = this.templateEditForm || {}
      if (nodeType === 'template') {
        return this.currentTemplateShowList.map((ele) => ele.label)
      }

      if (nodeType === 'folder') {
        return this.templateList.map((ele) => ele.label)
      }
      return []
    }
  },
  mounted() {
    this.getTree()
    const _this = this
    const erd = elementResizeDetectorMaker()
    const templateMainDom = document.getElementById('template-box')
    // 监听div变动事件
    erd.listenTo(templateMainDom, (element) => {
      _this.$nextTick(() => {
        const curSeparator = Math.trunc(
          templateMainDom.offsetWidth / _this.templateMiniWidth
        )
        _this.templateCurWidth =
          Math.trunc(templateMainDom.offsetWidth / curSeparator) - 24 - curSeparator
      })
    })
  },
  methods: {
    roleValidator(rule, value, callback) {
      if (this.nameRepeat(value)) {
        const { nodeType } = this.templateEditForm || {}
        callback(
          new Error(
            this.$t(
              `system_parameter_setting.${
                nodeType === 'folder'
                  ? 'name_already_exists_type'
                  : 'the_same_category'
              }`
            )
          )
        )
      } else {
        callback()
      }
    },
    nameRepeat(value) {
      if (!this.nameList || this.nameList.length === 0) {
        return false
      }
      // 编辑场景 不能 因为名称重复而报错
      if (this.formType === 'edit' && this.originName === value) {
        return false
      }
      return this.nameList.some((name) => name === value)
    },
    handleCommand(key, data) {
      switch (key) {
        case 'rename':
          this.templateEdit(data)
          break
        case 'delete':
          this.templateDeleteConfirm(data)
          break
        default:
          break
      }
    },
    templateDeleteConfirm(template) {
      const options = {
        title: 'system_parameter_setting.delete_this_template',
        type: 'primary',
        cb: () => this.templateDelete(template.id)
      }
      this.handlerConfirm(options)
    },
    handleClick(tab, event) {
      this.getTree()
    },
    showCurrentTemplate(pid, label) {
      this.currentTemplateId = pid
      this.currentTemplateLabel = label
      if (this.currentTemplateId) {
        find({ pid: this.currentTemplateId }).then((response) => {
          this.currentTemplateShowList = response.data
        })
      }
    },
    templateFolderDelete(id) {
      if (id) {
        templateDelete(id).then((response) => {
          this.openMessageSuccess('commons.delete_success')
          this.getTree()
        })
      }
    },
    templateDelete(id) {
      if (id) {
        templateDelete(id).then((response) => {
          this.openMessageSuccess('commons.delete_success')
          this.showCurrentTemplate(this.currentTemplateId, this.currentTemplateLabel)
        })
      }
    },
    showTemplateEditDialog(type, templateInfo) {
      this.templateEditForm = null
      this.formType = type
      if (type === 'edit') {
        this.templateEditForm = JSON.parse(JSON.stringify(templateInfo))
        this.dialogTitle = this.$t(
          `system_parameter_setting.${
            this.templateEditForm.nodeType === 'folder'
              ? 'edit_classification'
              : 'edit_template'
          }`
        )
        this.originName = this.templateEditForm.label
      } else {
        this.dialogTitle = this.$t('panel.add_category')
        this.templateEditForm = {
          name: '',
          nodeType: 'folder',
          templateType: this.currentTemplateType,
          level: 0
        }
      }
      this.dialogTitleLabel = this.$t(
        `system_parameter_setting.${
          this.templateEditForm.nodeType === 'folder'
            ? 'classification_name'
            : 'template_name'
        }`
      )
      this.editTemplate = true
    },
    templateEdit(templateInfo) {
      this.showTemplateEditDialog('edit', templateInfo)
    },
    saveTemplateEdit(templateEditForm) {
      this.$refs['templateEditForm'].validate((valid) => {
        if (valid) {
          save(templateEditForm).then((response) => {
            this.close()
            this.openMessageSuccess(
              `system_parameter_setting.${
                this.templateEditForm.id
                  ? 'rename_succeeded'
                  : 'added_successfully'
              }`
            )
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
      find(request).then((res) => {
        this.templateList = res.data
        this.showFirst()
      })
    },
    showFirst() {
      // 判断是否默认点击第一条
      if (this.templateList && this.templateList.length > 0) {
        let showFirst = true
        this.templateList.forEach((template) => {
          if (template.id === this.currentTemplateId) {
            showFirst = false
          }
        })
        if (showFirst) {
          this.$nextTick().then(() => {
            const [obj = {}] = this.templateList
            this.$refs.templateList.nodeClick(obj)
          })
        } else {
          this.showCurrentTemplate(this.currentTemplateId, this.currentTemplateLabel)
        }
      } else {
        this.currentTemplateShowList = []
      }
    },
    closeEditTemplateDialog() {
      this.templateDialog.visible = false
    },
    templateImport(pid) {
      this.templateDialog.visible = true
      this.templateDialog.pid = pid
    }
  }
}
</script>

<style lang="scss" scoped>
.de-template {
  height: 100%;
  background-color: var(--MainBG, #f5f6f7);

  .tabs-container {
    height: calc(100% - 48px);
    background: var(--ContentBG, #ffffff);
    overflow-x: auto;
  }

  .flex-tabs {
    display: flex;
    background: #f5f6f7;
  }

  .de-tabs-left {
    background: #fff;
    width: 269px;
    border-right: 1px solid rgba(31, 35, 41, 0.15);
    padding: 24px;
  }

  .de-tabs-right {
    flex: 1;
    background: #fff;
    padding: 24px 0 24px 24px;
    overflow: hidden;

    .template-box {
      display: flex;
      flex-wrap: wrap;
      overflow-y: auto;
      box-sizing: border-box;
      align-content: flex-start;
      height: calc(100% - 10px);
      width: 100%;
      padding-bottom: 24px;
    }

    .active-template {
      margin: 4px 0 20px 0;
      padding-right: 24px;
      font-family: "PingFang SC";
      font-style: normal;
      font-weight: 500;
      font-size: 16px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      color: var(--deTextPrimary, #1f2329);
    }
  }
}
</style>

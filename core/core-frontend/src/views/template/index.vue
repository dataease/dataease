<template>
  <div>
    <div class="de-template">
      <el-tabs v-model="state.currentTemplateType" class="de-tabs" @tab-click="handleClick">
        <el-tab-pane name="self">
          <template #label>
            <span>{{ t('panel.user_template') }}</span>
          </template>
        </el-tab-pane>
        <el-tab-pane name="system">
          <template #label>
            <span>{{ t('panel.sys_template') }}</span>
          </template>
        </el-tab-pane>
      </el-tabs>
      <div class="tabs-container flex-tabs">
        <div class="de-tabs-left">
          <template-list
            ref="templateListRef"
            :template-type="state.currentTemplateType"
            :template-list="state.templateList"
            @templateDelete="templateFolderDelete"
            @templateEdit="templateEdit"
            @showCurrentTemplate="showCurrentTemplate"
            @templateImport="templateImport"
            @showTemplateEditDialog="showTemplateEditDialog"
          />
        </div>
        <div class="de-tabs-right">
          <div v-if="state.currentTemplateLabel" class="active-template">
            {{ state.currentTemplateLabel }}&nbsp;&nbsp;({{ state.currentTemplateShowList.length }})
            <el-button
              type="primary"
              icon="el-icon-upload2"
              @click="templateImport(state.currentTemplateId)"
            >
              {{ t('panel.import') }}
            </el-button>
          </div>
          <el-empty
            v-if="!state.currentTemplateShowList.length"
            :image="state.noneImg"
            :description="t('components.no_template')"
          />
          <div v-show="state.currentTemplateId !== ''" id="template-box" class="template-box">
            <template-item
              v-for="item in state.currentTemplateShowList"
              :key="item.id"
              :width="state.templateCurWidth"
              :model="item"
              @command="key => handleCommand(key, item)"
            />
          </div>
        </div>
      </div>
    </div>
    <el-dialog
      :title="state.dialogTitle"
      v-model:visible="state.editTemplate"
      append-to-body
      class="de-dialog-form"
      width="600px"
    >
      <el-form
        ref="templateEditFormRef"
        class="de-form-item"
        :model="state.templateEditForm"
        :rules="state.templateEditFormRules"
      >
        <el-form-item :label="state.dialogTitleLabel" prop="name">
          <el-input v-model="state.templateEditForm.name" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button secondary @click="close()">{{ t('commons.cancel') }}</el-button>
          <el-button type="primary" @click="saveTemplateEdit(state.templateEditForm)"
            >{{ t('commons.confirm') }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!--导入templatedialog-->
    <el-dialog
      :title="state.templateDialog.title"
      v-model:visible="state.templateDialog.visible"
      :show-close="true"
      class="de-dialog-form"
      width="600px"
    >
      <template-import
        v-if="state.templateDialog.visible"
        :pid="state.templateDialog.pid"
        @refresh="showCurrentTemplate(state.currentTemplateId, state.currentTemplateLabel)"
        @closeEditTemplateDialog="closeEditTemplateDialog"
      />
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { save, templateDelete, find } from '@/api/template'
import elementResizeDetectorMaker from 'element-resize-detector'
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { ElMessage } from 'element-plus-secondary'
import TemplateList from '@/views/template/component/TemplateList.vue'
const { t } = useI18n()
const templateEditFormRef = ref(null)
const templateListRef = ref(null)

const roleValidator = (rule, value, callback) => {
  if (nameRepeat(value)) {
    const { nodeType } = state.templateEditForm || {}
    callback(
      new Error(
        t(
          `system_parameter_setting.${
            nodeType === 'folder' ? 'name_already_exists_type' : 'the_same_category'
          }`
        )
      )
    )
  } else {
    callback()
  }
}

const state = reactive({
  showShare: false,
  currentTemplateShowList: [],
  noneImg: '@/assets/None.png',
  currentPid: '',
  currentTemplateType: 'self',
  templateEditFormRules: {
    name: [
      { required: true, trigger: 'blur', validator: roleValidator },
      {
        required: true,
        message: t('commons.input_content'),
        trigger: 'blur'
      },
      {
        max: 50,
        message: t('commons.char_can_not_more_50'),
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
    title: t('panel.import_template'),
    visible: false,
    pid: ''
  }
})

const nameList = computed(() => {
  const { nodeType } = state.templateEditForm || {}
  if (nodeType === 'template') {
    return state.currentTemplateShowList.map(ele => ele.label)
  }

  if (nodeType === 'folder') {
    return state.templateList.map(ele => ele.label)
  }
  return []
})

const nameRepeat = value => {
  if (!nameList.value) {
    return false
  }
  // 编辑场景 不能 因为名称重复而报错
  if (state.formType === 'edit' && state.originName === value) {
    return false
  }
  return nameList.value.some(name => name === value)
}

const handleCommand = (key, data) => {
  switch (key) {
    case 'rename':
      templateEdit(data)
      break
    case 'delete':
      templateDeleteConfirm(data)
      break
    default:
      break
  }
}

const handlerConfirm = option => {
  //do handlerConfirm
}

const templateDeleteConfirm = template => {
  const options = {
    title: 'system_parameter_setting.delete_this_template',
    type: 'primary',
    cb: () => templateDelete(template.id)
  }
  handlerConfirm(options)
}

const handleClick = (tab, event) => {
  getTree()
}

const showCurrentTemplate = (pid, label) => {
  state.currentTemplateId = pid
  state.currentTemplateLabel = label
  if (state.currentTemplateId) {
    find({ pid: state.currentTemplateId }).then(response => {
      state.currentTemplateShowList = response.data
    })
  }
}

const templateFolderDelete = id => {
  if (id) {
    templateDelete(id).then(response => {
      ElMessage.info(t('commons.delete_success'))
      getTree()
    })
  }
}
const templateDelete = id => {
  if (id) {
    templateDelete(id).then(response => {
      ElMessage.info(t('commons.delete_success'))
      showCurrentTemplate(state.currentTemplateId, state.currentTemplateLabel)
    })
  }
}

const showTemplateEditDialog = (type, templateInfo) => {
  state.templateEditForm = null
  state.formType = type
  if (type === 'edit') {
    state.templateEditForm = JSON.parse(JSON.stringify(templateInfo))
    state.dialogTitle = t(
      `system_parameter_setting.${
        state.templateEditForm['nodeType'] === 'folder' ? 'edit_classification' : 'edit_template'
      }`
    )
    state.originName = state.templateEditForm['label']
  } else {
    state.dialogTitle = t('panel.add_category')
    state.templateEditForm = {
      name: '',
      nodeType: 'folder',
      templateType: state.currentTemplateType,
      level: 0
    }
  }
  state.dialogTitleLabel = t(
    `system_parameter_setting.${
      state.templateEditForm['nodeType'] === 'folder' ? 'classification_name' : 'template_name'
    }`
  )
  state.editTemplate = true
}

const templateEdit = templateInfo => {
  showTemplateEditDialog('edit', templateInfo)
}

const saveTemplateEdit = templateEditForm => {
  templateEditFormRef.value.validate(valid => {
    if (valid) {
      save(templateEditForm).then(response => {
        close()
        // openMessageSuccess(
        //   `system_parameter_setting.${
        //     this.templateEditForm.id ? 'rename_succeeded' : 'added_successfully'
        //   }`
        // )
        getTree()
      })
    } else {
      return false
    }
  })
}
const close = () => {
  templateEditFormRef.value.resetFields()
  state.editTemplate = false
}
const getTree = () => {
  const request = {
    templateType: state.currentTemplateType,
    level: '0'
  }
  find(request).then(res => {
    state.templateList = res.data
    showFirst()
  })
}
const showFirst = () => {
  // 判断是否默认点击第一条
  if (state.templateList && state.templateList.length > 0) {
    let showFirst = true
    state.templateList.forEach(template => {
      if (template.id === state.currentTemplateId) {
        showFirst = false
      }
    })
    if (showFirst) {
      nextTick().then(() => {
        const [obj = {}] = state.templateList
        templateListRef.value.nodeClick(obj)
      })
    } else {
      showCurrentTemplate(state.currentTemplateId, state.currentTemplateLabel)
    }
  } else {
    state.currentTemplateShowList = []
  }
}

const closeEditTemplateDialog = () => {
  state.templateDialog.visible = false
}

const templateImport = pid => {
  state.templateDialog.visible = true
  state.templateDialog.pid = pid
}

onMounted(() => {
  getTree()
  const erd = elementResizeDetectorMaker()
  const templateMainDom = document.getElementById('template-box')
  // 监听div变动事件
  erd.listenTo(templateMainDom, element => {
    nextTick(() => {
      const curSeparator = Math.trunc(templateMainDom.offsetWidth / state.templateMiniWidth)
      state.templateCurWidth =
        Math.trunc(templateMainDom.offsetWidth / curSeparator) - 24 - curSeparator
    })
  })
})
</script>

<style lang="less" scoped>
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
      font-family: 'PingFang SC';
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

<template>
  <div class="template-head">
    <p class="router-title">模板管理</p>
    <el-button style="float: right" type="primary" @click="templateImport(state.currentTemplateId)">
      {{ t('visualization.import') }}
    </el-button>
    <el-input
      v-model="state.templateFilterText"
      :placeholder="'搜索关键字'"
      class="template-search-class"
      clearable
    >
      <template #prefix>
        <el-icon>
          <Icon name="de-search" />
        </el-icon>
      </template>
    </el-input>
  </div>
  <div class="sys-setting-p">
    <div class="container-sys-param">
      <div style="width: 100%; height: 100%">
        <div class="de-template">
          <div class="tabs-container flex-tabs">
            <div class="de-tabs-left">
              <de-template-list
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
                <span v-show="!state.templateFilterText"
                  >{{ state.currentTemplateLabel }}&nbsp;&nbsp;({{
                    state.currentTemplateShowList.length
                  }})</span
                >
                <span v-show="state.templateFilterText">
                  <span style="color: #3370ff">{{ state.currentTemplateLabel }}&nbsp;&nbsp;</span>
                  <span>的搜索结果&nbsp;{{ currentTemplateShowListComputed.length }}&nbsp;个</span>
                </span>
              </div>
              <el-empty
                v-if="!state.currentTemplateShowList.length"
                :image="NoneImage"
                :description="'暂无模板'"
              />
              <div v-show="state.currentTemplateId !== ''" id="template-box" class="template-box">
                <de-template-item
                  v-for="item in currentTemplateShowListComputed"
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
          v-model="state.editTemplate"
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
        <!--导入templateDialog-->
        <el-dialog
          :title="state.templateDialog.title"
          v-model="state.templateDialog.visible"
          :show-close="true"
          class="de-dialog-form"
          width="600px"
        >
          <de-template-import
            v-if="state.templateDialog.visible"
            :pid="state.templateDialog.pid"
            @refresh="showCurrentTemplate(state.currentTemplateId, state.currentTemplateLabel)"
            @closeEditTemplateDialog="closeEditTemplateDialog"
          />
        </el-dialog>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { save, templateDelete, find } from '@/api/template'
import elementResizeDetectorMaker from 'element-resize-detector'
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { ElMessage } from 'element-plus-secondary'
import DeTemplateList from '@/views/template/component/DeTemplateList.vue'
const { t } = useI18n()
const templateEditFormRef = ref(null)
const templateListRef = ref(null)
import NoneImage from '@/assets/none.png'
import DeTemplateImport from '@/views/template/component/DeTemplateImport.vue'
import DeTemplateItem from '@/views/template/component/DeTemplateItem.vue'

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
  templateFilterText: '',
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
  templateMiniWidth: 256,
  templateCurWidth: 256,
  formType: '',
  originName: '',
  templateDialog: {
    title: t('visualization.import_template'),
    visible: false,
    pid: ''
  }
})

const currentTemplateShowListComputed = computed(() => {
  if (!state.templateFilterText) return [...state.currentTemplateShowList]
  return state.currentTemplateShowList.filter(ele =>
    ele['name']?.includes(state.templateFilterText)
  )
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

const initStyle = () => {
  nextTick(() => {
    const tree = document.querySelector('.ed-tree')
    // 创建横线元素
    const line = document.createElement('hr')
    line.classList.add('custom-line')

    // 将横线元素插入到第一个选项后面
    tree.firstElementChild.appendChild(line)
  })
}

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
  templateDeleteInfo(template.id)
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
      ElMessage({
        message: t('commons.delete_success'),
        type: 'success',
        showClose: true
      })
      getTree()
    })
  }
}
const templateDeleteInfo = id => {
  if (id) {
    templateDelete(id).then(response => {
      ElMessage({
        message: t('commons.delete_success'),
        type: 'success',
        showClose: true
      })
      showCurrentTemplate(state.currentTemplateId, state.currentTemplateLabel)
    })
  }
}

const showTemplateEditDialog = (type, templateInfo) => {
  state.templateEditForm = null
  state.formType = type
  if (type === 'edit') {
    state.templateEditForm = JSON.parse(JSON.stringify(templateInfo))
    state.dialogTitle = state.templateEditForm['nodeType'] === 'folder' ? '编辑分类' : '编辑模板'
    state.originName = state.templateEditForm['label']
  } else {
    state.dialogTitle = t('visualization.add_category')
    state.templateEditForm = {
      name: '',
      nodeType: 'folder',
      templateType: state.currentTemplateType,
      level: 0
    }
  }
  state.dialogTitleLabel = state.templateEditForm['nodeType'] === 'folder' ? '分类名称' : '模板名称'
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
  nextTick(() => {
    const request = {
      templateType: state.currentTemplateType,
      level: '0'
    }
    find(request).then(res => {
      state.templateList = res.data
      showFirst()
    })
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
      const offsetWidth = templateMainDom.offsetWidth - 24
      const curSeparator = Math.trunc(offsetWidth / state.templateMiniWidth)
      state.templateCurWidth = Math.trunc(offsetWidth / curSeparator) - 24 - curSeparator
    })
  })
  initStyle()
})
</script>

<style lang="less" scoped>
.de-template {
  height: 100%;
  .tabs-container {
    height: 100%;
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
    overflow: hidden;
    background: rgba(239, 240, 241, 1);

    .template-box {
      display: flex;
      flex-wrap: wrap;
      overflow-y: auto;
      box-sizing: border-box;
      align-content: flex-start;
      height: calc(100% - 10px);
      width: 100%;
      padding: 16px 0px 16px 24px;
    }

    .active-template {
      height: 56px;
      padding: 0px 24px;
      font-family: 'PingFang SC';
      font-style: normal;
      font-weight: 500;
      font-size: 16px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      color: rgba(31, 35, 41, 1);
      background: #fff;
      border-bottom: 1px solid rgba(31, 35, 41, 0.15);
    }
  }
}

.router-title {
  color: #1f2329;
  font-feature-settings: 'clig' off, 'liga' off;
  font-family: PingFang SC;
  font-size: 20px;
  font-style: normal;
  font-weight: 500;
  line-height: 28px;
  float: left;
}
.sys-setting-p {
  width: 100%;
  background: #ffffff;
  height: calc(100vh - 136px);
  box-sizing: border-box;
  margin-top: 12px;
}
.setting-auto-h {
  height: auto !important;
}

.container-sys-param {
  height: 100%;
  overflow-y: auto;
}

.template-head {
  height: 32px;
}
.template-search-class {
  float: right;
  width: 320px;
  margin-right: 12px;
}
</style>

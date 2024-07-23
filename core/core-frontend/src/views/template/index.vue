<template>
  <div class="template-head">
    <p class="router-title">模板管理</p>
    <el-button style="float: right" type="primary" @click="templateImport(state.currentTemplateId)">
      {{ t('visualization.import') }}
    </el-button>
    <el-button
      style="float: right; margin-right: 12px"
      @click="showTemplateEditDialog('new', null)"
    >
      添加分类
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
    <div class="container-sys-param" v-show="state.templateCategories.length">
      <div style="width: 100%; height: 100%">
        <div class="de-template">
          <div class="tabs-container flex-tabs">
            <div class="de-tabs-left">
              <de-template-list
                ref="templateListRef"
                :template-type="state.currentTemplateType"
                :template-list="state.templateCategories"
                @categoryDelete="categoryDelete"
                @categoryEdit="categoryEdit"
                @showCurrentTemplate="showCurrentTemplate"
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
                  <span style="color: #3370ff">{{ state.templateFilterText }}&nbsp;&nbsp;</span>
                  <span>的搜索结果&nbsp;{{ currentTemplateShowListComputed.length }}&nbsp;个</span>
                </span>
              </div>
              <el-row
                style="height: 100%"
                v-if="!state.currentTemplateShowList.length && !state.templateFilterText"
                class="custom-position"
              >
                <Icon style="width: 125px; height: 125px" name="dv-empty" />
                <span style="margin-top: 8px; font-size: 14px"> 暂无模板 </span>
              </el-row>

              <el-row
                style="height: 100%"
                v-if="!currentTemplateShowListComputed.length && state.templateFilterText"
                class="custom-position"
              >
                <Icon style="width: 125px; height: 125px" name="dv-nothing" />
                <span style="margin-top: 8px; font-size: 14px"> 没有找到相关模板 </span>
              </el-row>

              <div v-show="state.currentTemplateId !== ''" id="template-box" class="template-box">
                <de-template-item
                  v-for="item in currentTemplateShowListComputed"
                  :key="item.id"
                  :width="state.templateCurWidth"
                  :batch-state="batchState > 0"
                  :model="item"
                  @command="key => handleCommand(key, item)"
                />
              </div>
              <div v-show="batchState" class="batch-opt-area">
                <el-button @click="batchUpdate" type="danger" plain style="margin-left: 24px"
                  >修改分类</el-button
                >
                <el-button @click="batchPreDelete" type="danger" plain>批量删除</el-button>
                <span style="margin-left: 24px; font-size: 14px">已选 {{ batchState }} 项</span>
                <el-button @click="batchFullSelect" style="margin-left: 16px" text
                  >全选 {{ currentTemplateShowListComputed.length }} 项</el-button
                >
                <el-button @click="batchClear" text>清空</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="container-sys-param" v-show="!state.templateCategories.length">
      <el-row style="height: 100%" class="custom-position">
        <Icon style="width: 125px; height: 125px" name="dv-empty" />
        <span style="margin-top: 8px; font-size: 14px">
          <el-button
            style="float: right"
            type="primary"
            @click="templateImport(state.currentTemplateId)"
          >
            {{ t('visualization.import') }}
          </el-button>
          <el-button
            style="float: right; margin-right: 12px"
            @click="showTemplateEditDialog('new', null)"
          >
            添加分类
          </el-button>
        </span>
      </el-row>
    </div>
    <el-dialog
      :title="state.dialogTitle"
      v-model="state.editTemplate"
      append-to-body
      class="de-dialog-form create-dialog"
      width="420px"
    >
      <el-form
        ref="templateEditFormRef"
        label-position="top"
        class="de-form-item"
        :model="state.templateEditForm"
        :rules="state.templateEditFormRules"
      >
        <el-form-item :label="state.dialogTitleLabel" prop="name">
          <el-input :placeholder="'请输入分类名称'" v-model="state.templateEditForm.name" />
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
      :destroy-on-close="true"
      class="de-dialog-form create-dialog"
      width="600px"
    >
      <de-template-import
        v-if="state.templateDialog.visible"
        :pid="state.templateDialog.pid"
        :template-id="state.templateDialog.templateId"
        :opt-type="state.templateDialog.optType"
        :template-categories="state.templateCategories"
        @refresh="importRefresh"
        @closeEditTemplateDialog="closeEditTemplateDialog"
      />
    </el-dialog>

    <!--导入templateDialog-->
    <el-dialog
      :title="'修改分类'"
      v-model="state.batchOptDialogShow"
      :show-close="true"
      :destroy-on-close="true"
      class="de-dialog-form"
      width="600px"
    >
      <de-category-change
        v-if="state.batchOptDialogShow"
        :template-ids="batchTemplateIds"
        :template-categories="state.templateCategories"
        @refresh="showCurrentTemplate(state.currentTemplateId, state.currentTemplateLabel)"
        @closeBatchEditTemplateDialog="closeBatchOptDialog"
      ></de-category-change>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import {
  save,
  templateDelete,
  find,
  findCategories,
  deleteCategory,
  batchDelete
} from '@/api/template'
import elementResizeDetectorMaker from 'element-resize-detector'
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { ElMessage, ElMessageBox } from 'element-plus-secondary'
import DeTemplateList from '@/views/template/component/DeTemplateList.vue'
const { t } = useI18n()
const templateEditFormRef = ref(null)
const templateListRef = ref(null)
import DeTemplateImport from '@/views/template/component/DeTemplateImport.vue'
import DeTemplateItem from '@/views/template/component/DeTemplateItem.vue'
import DeCategoryChange from '@/views/template/component/DeCategoryChange.vue'

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
  batchOptDialogShow: false,
  batchOptList: [],
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
  templateCategories: [],
  templateMiniWidth: 256,
  templateCurWidth: 256,
  formType: '',
  originName: '',
  templateDialog: {
    title: t('visualization.import_template'),
    visible: false,
    templateId: null,
    optType: 'insert',
    pid: '',
    categories: []
  }
})

const batchUpdate = () => {
  state.batchOptDialogShow = true
}

const batchPreDelete = () => {
  ElMessageBox.confirm(`确定删除${batchState.value}个模板吗？`, {
    confirmButtonType: 'danger',
    type: 'warning',
    autofocus: false,
    showClose: false
  }).then(() => {
    const params = {
      templateIds: batchTemplateIds.value,
      categories: [state.currentTemplateId]
    }
    batchDelete(params).then(() => {
      ElMessage({
        message: t('commons.delete_success'),
        type: 'success',
        showClose: true
      })
      showCurrentTemplate(state.currentTemplateId, state.currentTemplateLabel)
    })
  })
}

const batchFullSelect = () => {
  currentTemplateShowListComputed.value.forEach(item => {
    item.checked = true
  })
}

const batchClear = () => {
  currentTemplateShowListComputed.value.forEach(item => {
    item.checked = false
  })
}

const batchState = computed(() => {
  return currentTemplateShowListComputed.value.filter(ele => ele.checked).length
})

const batchTemplateIds = computed(() => {
  return currentTemplateShowListComputed.value.filter(ele => ele.checked).map(item => item.id)
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
    return state.templateCategories.map(ele => ele.label)
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
    case 'templateEdit':
      templateEdit(data)
      break
    case 'delete':
      templateDeleteConfirm(data)
      break
    default:
      break
  }
}

const templateDeleteConfirm = template => {
  templateDeleteInfo(template.id)
}

const importRefresh = params => {
  if (params.optType === 'refresh') {
    templateListRef.value.nodeClick({ id: params.refreshPid, name: params.refreshPName })
  } else {
    showTemplateEditDialog('new', null)
  }
}

const showCurrentTemplate = (pid, label) => {
  state.currentTemplateId = pid
  state.currentTemplateLabel = label
  if (state.currentTemplateId) {
    find({ categoryId: state.currentTemplateId }).then(response => {
      state.currentTemplateShowList = response.data
    })
  }
}

const categoryDelete = id => {
  if (id) {
    deleteCategory(id).then(response => {
      if (response.data === 'success') {
        ElMessage({
          message: t('commons.delete_success'),
          type: 'success',
          showClose: true
        })
        getTree()
      } else {
        ElMessageBox.confirm('无法删除分类', {
          tip: '请先移除该分类下所有模板再进行删除分类操作',
          confirmButtonText: '知道了',
          confirmButtonType: 'default',
          showCancelButton: false,
          type: 'warning',
          autofocus: false,
          showClose: false
        })
      }
    })
  }
}
const templateDeleteInfo = id => {
  if (id) {
    ElMessageBox.confirm('确定删除该模板吗？', {
      tip: '',
      confirmButtonType: 'danger',
      type: 'warning',
      confirmButtonText: t('common.delete'),
      autofocus: false,
      showClose: false
    }).then(() => {
      templateDelete(id, state.currentTemplateId).then(() => {
        ElMessage({
          message: t('commons.delete_success'),
          type: 'success',
          showClose: true
        })
        showCurrentTemplate(state.currentTemplateId, state.currentTemplateLabel)
      })
    })
  }
}

const showTemplateEditDialog = (type, templateInfo) => {
  state.templateEditForm = null
  state.formType = type
  if (type === 'edit') {
    state.templateEditForm = JSON.parse(JSON.stringify(templateInfo))
    state.dialogTitle = state.templateEditForm['nodeType'] === 'folder' ? '重命名' : '编辑模板'
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

const categoryEdit = templateInfo => {
  showTemplateEditDialog('edit', templateInfo)
}

const templateEdit = templateInfo => {
  state.templateDialog.visible = true
  state.templateDialog.title = '编辑模板'
  state.templateDialog.optType = 'update'
  state.templateDialog.templateId = templateInfo.id
}

const saveTemplateEdit = templateEditForm => {
  if (templateEditForm.name === '最近使用') {
    ElMessage({
      message: '不合法命名，请更换！',
      type: 'error',
      showClose: true
    })
    return
  }
  templateEditFormRef.value.validate(valid => {
    if (valid) {
      save({ ...templateEditForm }).then(() => {
        ElMessage({
          message: '添加成功',
          type: 'success',
          showClose: true
        })
        state.currentTemplateId = null
        getTree()
        close()
      })
    } else {
      return false
    }
  })
}
const close = () => {
  state.editTemplate = false
}
const getTree = () => {
  nextTick(() => {
    const request = {
      templateType: state.currentTemplateType,
      level: '0'
    }
    findCategories(request).then(res => {
      state.templateCategories = res.data
      showFirst()
    })
  })
}
const showFirst = () => {
  // 判断是否默认点击第一条
  if (state.templateCategories && state.templateCategories.length > 0) {
    let showFirst = true
    state.templateCategories.forEach(template => {
      if (template.id === state.currentTemplateId) {
        showFirst = false
      }
    })
    if (showFirst) {
      nextTick().then(() => {
        const [obj = {}] = state.templateCategories
        templateListRef.value.nodeClick(obj)
      })
    } else {
      showCurrentTemplate(state.currentTemplateId, state.currentTemplateLabel)
    }
  } else {
    state.currentTemplateShowList = []
  }
}

const closeBatchOptDialog = () => {
  state.batchOptDialogShow = false
}

const closeEditTemplateDialog = () => {
  state.templateDialog.visible = false
}

const templateImport = pid => {
  state.templateDialog.visible = true
  state.templateDialog.title = '导入模板'
  state.templateDialog.templateId = null
  state.templateDialog.optType = 'insert'
  state.templateDialog.pid = pid
}

onMounted(() => {
  getTree()
  const erd = elementResizeDetectorMaker()
  const templateMainDom = document.getElementById('template-box')
  // 监听div变动事件
  erd.listenTo(templateMainDom, () => {
    nextTick(() => {
      const offsetWidth = templateMainDom.offsetWidth - 24
      const curSeparator = Math.trunc(offsetWidth / state.templateMiniWidth)
      state.templateCurWidth = Math.trunc(offsetWidth / curSeparator) - 24 - curSeparator
    })
  })
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
    padding: 12px 8px;
  }

  .de-tabs-right {
    flex: 1;
    overflow: hidden;
    background: rgba(239, 240, 241, 1);
    position: relative;
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
      font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
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
    .batch-opt-area {
      background-color: #ffffff;
      position: absolute;
      display: flex;
      align-items: center;
      height: 48px;
      width: 100%;
      bottom: 0;
    }
  }
}

.router-title {
  color: #1f2329;
  font-feature-settings: 'clig' off, 'liga' off;
  font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
  font-size: 20px;
  font-style: normal;
  font-weight: 500;
  line-height: 28px;
  float: left;
}
.sys-setting-p {
  width: 100%;
  background: #ffffff;
  height: calc(100vh - 140px);
  box-sizing: border-box;
  margin-top: 16px;
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
}

.custom-position {
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  flex-flow: row nowrap;
  color: #9ea6b2;
  flex-direction: column;
  span {
    line-height: 22px;
    color: #646a73;
  }
}

.ed-empty__image img {
  width: 126px;
}
</style>

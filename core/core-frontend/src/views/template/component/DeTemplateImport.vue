<template>
  <div class="template-import">
    <el-form
      ref="templateImportForm"
      class="de-form-item"
      :model="state.templateInfo"
      :rules="state.templateInfoRules"
      label-position="top"
    >
      <el-form-item :label="'模板名称'" prop="name">
        <div class="flex-template">
          <el-input v-model="state.templateInfo.name" placeholder="请输入模板名称" clearable />
          <el-button style="margin-left: 10px" icon="Upload" secondary @click="goFile"
            >导入模板</el-button
          >
          <input
            id="input"
            ref="filesRef"
            type="file"
            accept=".DET2,.DET2APP"
            hidden
            @change="handleFileChange"
          />
        </div>
      </el-form-item>
      <el-row v-show="!!state.templateInfo.snapshot" class="preview" :style="classBackground" />
      <el-form-item :label="'选择分类'" prop="categories" style="margin-top: 16px">
        <el-select
          v-model="state.templateInfo.categories"
          multiple
          placeholder="可多选"
          style="width: 100%"
          :popper-class="
            templateCategories.length
              ? 'custom-category-padding'
              : 'custom-category-empty custom-category-padding'
          "
        >
          <el-option
            v-for="option in templateCategories"
            :key="option.id"
            :label="option.name"
            :value="option.id"
          />
          <div class="custom-dropdown__empty">
            <span>暂无可选分类</span>
          </div>
          <div class="custom-option-line">
            <div @click="doAddCategory" class="flex-align-center">
              <el-icon>
                <Plus></Plus>
              </el-icon>
              添加分类
            </div>
          </div>
        </el-select>
      </el-form-item>
    </el-form>
    <el-row> </el-row>
    <el-row class="de-root-class">
      <el-button secondary @click="cancel()">{{ t('commons.cancel') }}</el-button>
      <el-button type="primary" @click="saveTemplate()">{{ t('commons.confirm') }}</el-button>
    </el-row>
  </div>
</template>

<script lang="ts" setup>
import { save, nameCheck, findOne, categoryTemplateNameCheck } from '@/api/template'
import { computed, reactive, ref } from 'vue'
import { imgUrlTrans } from '@/utils/imgUtils'
import { ElMessage, ElMessageBox } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
const emits = defineEmits(['closeEditTemplateDialog', 'refresh', 'addCategoryInfo'])
const { t } = useI18n()
const filesRef = ref(null)
const props = defineProps({
  pid: {
    type: String,
    required: true
  },
  templateCategories: {
    type: Array,
    required: true
  },
  optType: {
    type: String,
    required: true,
    default: 'insert'
  },
  templateId: {
    type: String,
    required: false
  }
})

const state = reactive({
  categories: [],
  nameList: [],
  importTemplateInfo: {
    snapshot: ''
  },
  templateInfoRules: {
    name: [
      {
        required: true,
        message: t('commons.input_content'),
        trigger: 'change'
      },
      {
        max: 50,
        message: t('commons.char_can_not_more_50'),
        trigger: 'change'
      }
    ],
    categories: [
      {
        required: true,
        message: t('commons.input_content'),
        trigger: 'change'
      }
    ]
  },
  recover: false,
  templateInfo: {
    id: null,
    level: '1',
    pid: props.pid,
    categories: [],
    dvType: 'dashboard',
    nodeType: 'template',
    name: '',
    templateStyle: null,
    templateData: null,
    dynamicData: null,
    appData: null,
    staticResource: null,
    snapshot: '',
    version: null
  }
})

const classBackground = computed(() => {
  if (state.templateInfo.snapshot) {
    return {
      background: `url(${imgUrlTrans(state.templateInfo.snapshot)}) no-repeat`
    }
  } else {
    return {}
  }
})

const cancel = () => {
  emits('closeEditTemplateDialog')
}

const saveTemplate = () => {
  if (!state.templateInfo.name) {
    ElMessage.warning(t('chart.name_can_not_empty'))
    return false
  }
  if (state.templateInfo.name.length > 50) {
    ElMessage.warning(t('commons.char_can_not_more_50'))
    return false
  }
  if (!state.templateInfo.templateData) {
    ElMessage.warning(t('chart.template_can_not_empty'))
    return false
  }

  if (!state.templateInfo.categories.length) {
    ElMessage.warning('请选择分类')
    return false
  }

  if (props.optType === 'insert') {
    importTemplate()
  } else {
    editTemplate()
  }
}

const editTemplate = () => {
  const nameCheckRequest = {
    pid: state.templateInfo.pid,
    id: state.templateInfo.id,
    name: state.templateInfo.name,
    categories: state.templateInfo.categories,
    optType: props.optType
  }
  // 全局名称校验
  nameCheck(nameCheckRequest).then(response => {
    if (response.data.indexOf('exist') > -1) {
      ElMessage.warning('当前名称已在模版管理中存在，请修改')
    } else {
      save(state.templateInfo).then(response => {
        ElMessage.success(t('编辑成功'))
        emits('refresh', getRefreshPInfo())
        emits('closeEditTemplateDialog')
      })
    }
  })
}

const getRefreshPInfo = () => {
  const refreshPid = state.templateInfo.categories[0]
  let refreshPName = ''
  props.templateCategories.forEach(category => {
    if (category.id === refreshPid) {
      refreshPName = category.name
    }
  })
  return {
    optType: 'refresh',
    refreshPid: refreshPid,
    refreshPName: refreshPName
  }
}

const importTemplate = () => {
  const nameCheckRequest = {
    pid: state.templateInfo.pid,
    name: state.templateInfo.name,
    categories: state.templateInfo.categories,
    optType: props.optType
  }
  categoryTemplateNameCheck(nameCheckRequest).then(response => {
    if (response.data.indexOf('exist') > -1) {
      ElMessageBox.confirm('提示', {
        tip: '当前分类存在相同模板名称，是否覆盖？',
        confirmButtonType: 'danger',
        type: 'warning',
        autofocus: false,
        showClose: false
      }).then(() => {
        save(state.templateInfo).then(() => {
          ElMessage.success(t('覆盖成功'))
          emits('refresh', getRefreshPInfo())
          emits('closeEditTemplateDialog')
        })
      })
    } else {
      // 全局名称校验
      nameCheck(nameCheckRequest).then(response => {
        if (response.data.indexOf('exist') > -1) {
          ElMessage.warning('当前名称已在模版管理中存在，请修改')
        } else {
          save(state.templateInfo).then(response => {
            ElMessage.success(t('导入成功'))
            emits('refresh', getRefreshPInfo())
            emits('closeEditTemplateDialog')
          })
        }
      })
    }
  })
}

const handleFileChange = e => {
  const file = e.target.files[0]
  const reader = new FileReader()
  reader.onload = res => {
    const result = res.target.result as string
    state.importTemplateInfo = JSON.parse(result)
    state.templateInfo.name = state.importTemplateInfo['name']
    state.templateInfo.dvType = state.importTemplateInfo['dvType']
    state.templateInfo.templateStyle = state.importTemplateInfo['canvasStyleData']
    state.templateInfo.templateData = state.importTemplateInfo['componentData']
    state.templateInfo.snapshot = state.importTemplateInfo.snapshot
    state.templateInfo.dynamicData = state.importTemplateInfo['dynamicData']
    state.templateInfo.appData = state.importTemplateInfo['appData']
    state.templateInfo.staticResource = state.importTemplateInfo['staticResource']
    state.templateInfo['nodeType'] = state.importTemplateInfo['nodeType'] || 'template'
    state.templateInfo['version'] = state.importTemplateInfo['version']
  }
  reader.readAsText(file)
}
const goFile = () => {
  filesRef.value.click()
}

const doAddCategory = () => {
  emits('refresh', { optType: 'addCategory' })
}

if (props.templateId) {
  findOne(props.templateId).then(rsp => {
    state.templateInfo = rsp.data
  })
}
</script>

<style scoped lang="less">
.my_table :deep(.el-table__row > td) {
  /* 去除表格线 */
  border: none;
  padding: 0 0;
}
.my_table :deep(.el-table th.is-leaf) {
  /* 去除上边框 */
  border: none;
}
.my_table :deep(.el-table::before) {
  /* 去除下边框 */
  height: 0;
}

.de-root-class {
  margin-top: 24px;
  justify-content: flex-end;
}
.preview {
  margin-top: -8px;
  border: 1px solid #e6e6e6;
  height: 300px !important;
  overflow: auto;
  background-size: 100% 100% !important;
  border-radius: 4px;
}
.preview-show {
  border-left: 1px solid #e6e6e6;
  height: 300px;
  background-size: 100% 100% !important;
}
</style>

<style lang="less">
.flex-template {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  .el-input {
    margin-right: 2px;
    flex: 1;
  }
}

.custom-option-line {
  width: 100%;
  position: absolute;
  bottom: 0;
  height: 42px;
  display: flex;
  align-items: center;
  background: #fff;

  &::before {
    content: '';
    position: absolute;
    height: 1px;
    width: calc(100% - 22px);
    left: 11px;
    top: 0;
    background-color: #1f232926;
  }

  .flex-align-center {
    width: 100%;
    height: 32px;
    color: var(--el-input-icon-color);
    cursor: pointer;
    padding-left: 11px;
    font-size: 14px;
    font-weight: 400;
    line-height: 22px;

    &:hover {
      background-color: var(--ed-fill-color-light);
    }
    .ed-icon {
      font-size: 16px;
      margin: 0 8px 0 0;
    }
  }
}

.custom-category-empty {
  .ed-scrollbar {
    display: inherit !important;
  }

  .custom-dropdown__empty {
    display: inherit !important;
  }

  .ed-select-dropdown__empty {
    display: none !important;
  }
}

.custom-category-padding {
  .ed-select-dropdown__list {
    padding-bottom: 42px !important;
  }
}

.custom-dropdown__empty {
  display: none;
  margin-left: 12px;
  font-size: 14px;
  height: 32px;
  line-height: 32px;
  color: rgba(143, 149, 158, 1);
}
</style>

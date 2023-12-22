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
          <el-input v-model="state.templateInfo.name" clearable />
          <el-button style="margin-left: 10px" class="el-icon-upload2" secondary @click="goFile">{{
            t('visualization.upload_template')
          }}</el-button>
          <input
            id="input"
            ref="filesRef"
            type="file"
            accept=".DET2"
            hidden
            @change="handleFileChange"
          />
        </div>
      </el-form-item>
      <el-row v-show="!!state.templateInfo.snapshot" class="preview" :style="classBackground" />
      <el-form-item :label="'选择分类'" prop="categories" style="margin-top: 16px">
        <el-select v-model="state.templateInfo.categories" multiple style="width: 100%">
          <el-option
            v-for="option in templateCategories"
            :key="option.id"
            :label="option.name"
            :value="option.id"
          />
          <div class="custom-option-line"></div>
          <div>
            <el-button
              @click="doAddCategory"
              icon="Plus"
              text
              style="width: 100%; justify-content: flex-start"
              >添加分类</el-button
            >
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
import { save, nameCheck, find, findOne, categoryTemplateNameCheck } from '@/api/template'
import { computed, onMounted, reactive, ref } from 'vue'
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
    level: '1',
    pid: props.pid,
    categories: [],
    dvType: 'dashboard',
    name: '',
    templateStyle: null,
    templateData: null,
    dynamicData: null,
    staticResource: null,
    snapshot: ''
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

const showCurrentTemplate = pid => {
  find({ pid }).then(response => {
    state.nameList = response.data
  })
}

const cancel = () => {
  emits('closeEditTemplateDialog')
}

const saveTemplate = () => {
  if (!state.templateInfo.name) {
    ElMessage.warning(t('chart.name_can_not_empty'))
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
    name: state.templateInfo.name,
    categories: state.templateInfo.categories,
    optType: props.optType
  }
  // 全局名称校验
  nameCheck(nameCheckRequest).then(response => {
    save(state.templateInfo).then(response => {
      ElMessage.success(t('编辑成功'))
      emits('refresh')
      emits('closeEditTemplateDialog')
    })
  })
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
      ElMessageBox.confirm('提示？', {
        tip: '当前分类存在相同模版名称，是否覆盖？',
        confirmButtonType: 'danger',
        type: 'warning',
        autofocus: false,
        showClose: false
      }).then(() => {
        save(state.templateInfo).then(response => {
          ElMessage.success(t('覆盖成功'))
          emits('refresh')
          emits('closeEditTemplateDialog')
        })
      })
    } else {
      // 全局名称校验
      nameCheck(nameCheckRequest).then(response => {
        save(state.templateInfo).then(response => {
          ElMessage.success(t('导入成功'))
          emits('refresh')
          emits('closeEditTemplateDialog')
        })
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
    state.templateInfo.staticResource = state.importTemplateInfo['staticResource']
    state.templateInfo['nodeType'] = 'template'
  }
  reader.readAsText(file)
}
const goFile = () => {
  filesRef.value.click()
}

const doAddCategory = () => {
  emits('addCategoryInfo')
}

onMounted(() => {
  // showCurrentTemplate(props.pid)
})

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
  margin-top: -12px;
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
  width: calc(100% -8px);
  margin: 4px;
  height: 1px;
  background-color: rgba(31, 35, 41, 0.15);
}
</style>

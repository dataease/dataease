<template>
  <el-dialog
    class="create-dialog"
    title="从模板新建"
    v-model="state.dialogShow"
    width="700"
    :before-close="close"
    @submit.prevent
  >
    <el-row class="create-main" v-loading="state.loading">
      <el-row>
        <el-col :span="18" style="height: 40px">
          <el-radio v-model="state.inputType" label="new_outer_template"
            >{{ t('visualization.import_template') }}
          </el-radio>
          <el-radio v-model="state.inputType" label="new_inner_template" @click="getTree"
            >{{ t('visualization.copy_template') }}
          </el-radio>
        </el-col>
        <el-col v-if="state.inputType === 'new_outer_template'" :span="6" class="button-main">
          <el-button class="el-icon-upload" size="small" type="primary" @click="goFile"
            >{{ t('visualization.upload_template') }}
          </el-button>
          <input
            id="input"
            ref="files"
            type="file"
            accept=".DET2"
            hidden
            @change="handleFileChange"
          />
        </el-col>
      </el-row>
      <el-row style="margin-top: 5px">
        <el-col :span="4" class="name-area">名称</el-col>
        <el-col :span="20">
          <el-input v-model="state.dvCreateInfo.name" clearable size="small" />
        </el-col>
      </el-row>
      <el-row v-if="state.inputType === 'new_inner_template'" class="preview">
        <el-col :span="8" style="height: 100%; overflow-y: auto">
          <de-template-preview-list
            :template-list="state.templateList"
            @showCurrentTemplateInfo="showCurrentTemplateInfo"
          />
        </el-col>
        <el-col :span="16" :style="classBackground" class="preview-show" />
      </el-row>
      <el-row
        v-if="state.inputType === 'new_outer_template'"
        class="preview"
        :style="classBackground"
      />
      <el-row class="root-class">
        <el-button size="small" @click="cancel()">{{ t('commons.cancel') }} </el-button>
        <el-button type="primary" size="small" :disabled="!saveStatus" @click="save()"
          >{{ t('commons.confirm') }}
        </el-button>
      </el-row>
    </el-row>
  </el-dialog>
</template>

<script setup lang="ts">
import { showTemplateList } from '@/api/template'
import { useI18n } from '@/hooks/web/useI18n'
import { computed, reactive, ref, watch } from 'vue'
import { imgUrlTrans } from '@/utils/imgUtils'
import { ElMessage } from 'element-plus-secondary'
import { decompression } from '@/api/visualization/dataVisualization'
import DeTemplatePreviewList from '@/views/common/DeTemplatePreviewList.vue'
const { t } = useI18n()
const emits = defineEmits(['finish'])
const files = ref(null)
const props = defineProps({
  curCanvasType: {
    type: String,
    required: true
  }
})

const state = reactive({
  dialogShow: false,
  loading: false,
  inputType: 'new_outer_template',
  fieldName: 'name',
  tableRadio: null,
  keyWordSearch: '',
  columnLabel: t('visualization.belong_to_category'),
  templateList: [],
  importTemplateInfo: {
    snapshot: ''
  },
  dvCreateInfo: {
    pid: -1,
    name: null,
    canvasStyleData: null,
    componentData: null,
    templateId: null,
    dynamicData: null,
    staticResource: null
  },
  templateSelected: false
})

const saveStatus = computed(() => {
  return state.dvCreateInfo.name && state.templateSelected
})

const classBackground = computed(() => {
  if (state.importTemplateInfo.snapshot) {
    return {
      background: `url(${imgUrlTrans(state.importTemplateInfo.snapshot)}) no-repeat`
    }
  } else {
    return {}
  }
})

watch(
  () => state.inputType,
  () => {
    createInit()
  }
)

const createInit = () => {
  state.templateSelected = false
  state.dvCreateInfo.name = null
  state.dvCreateInfo.canvasStyleData = null
  state.dvCreateInfo.componentData = null
  state.importTemplateInfo.snapshot = null
  state.dvCreateInfo.templateId = null
}

const showCurrentTemplateInfo = data => {
  state.dvCreateInfo.templateId = data.id
  if (data.nodeType === 'folder') {
    state.dvCreateInfo.name = null
    state.importTemplateInfo.snapshot = null
    state.templateSelected = false
  } else {
    state.dvCreateInfo.name = data.name
    state.importTemplateInfo.snapshot = data.snapshot
    state.templateSelected = true
  }
}

const getTree = () => {
  const request = {
    level: '0',
    leafDvType: props.curCanvasType,
    withChildren: true
  }
  state.loading = true
  showTemplateList(request).then(res => {
    state.templateList = res.data
    state.loading = false
  })
}

const cancel = () => {
  emits('finish')
}

const save = () => {
  if (!state.dvCreateInfo.name) {
    ElMessage.warning(t('common.save_success'))
    return false
  }

  if (state.dvCreateInfo.name.length > 50) {
    ElMessage.warning(t('common.char_can_not_more_50'))
    return false
  }

  if (!state.dvCreateInfo.templateId && state.inputType === 'new_inner_template') {
    ElMessage.warning('chart.template_can_not_empty')
    return false
  }
  state.dvCreateInfo['newFrom'] = state.inputType
  state.loading = true
  decompression(state.dvCreateInfo)
    .then(response => {
      state.loading = false
      emits('finish', response.data)
    })
    .catch(() => {
      state.loading = false
    })
}
const handleFileChange = e => {
  const file = e.target.files[0]
  const reader = new FileReader()
  reader.onload = res => {
    state.templateSelected = true
    const result = res.target.result
    state.importTemplateInfo = JSON.parse(result)
    state.dvCreateInfo.name = state.importTemplateInfo['name'].name
    state.dvCreateInfo.canvasStyleData = state.importTemplateInfo['canvasStyleData']
    state.dvCreateInfo.componentData = state.importTemplateInfo['componentData']
    state.dvCreateInfo.dynamicData = state.importTemplateInfo['dynamicData']
    state.dvCreateInfo.staticResource = state.importTemplateInfo['staticResource']
  }
  reader.readAsText(file)
}
const goFile = () => {
  files.value.click()
}

const close = () => {
  state.dialogShow = false
}
const optInit = () => {
  state.dialogShow = true
  createInit()
}

defineExpose({
  optInit
})
</script>

<style scoped lang="less">
.create-main {
  display: inherit;
}

.name-area {
  display: flex;
  align-items: center;
  justify-content: left;
}

.button-main {
  display: flex;
  align-items: center;
  justify-content: right;
}
.root-class {
  display: flex;
  align-items: center;
  justify-content: right;
  margin: 15px 0px 5px;
}

.preview {
  margin-top: 5px;
  border: 1px solid #e6e6e6;
  height: 310px !important;
  overflow: hidden;
  background-size: 100% 100% !important;
}

.preview-show {
  border-left: 1px solid #e6e6e6;
  height: 310px;
  background-size: 100% 100% !important;
}
</style>

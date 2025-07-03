<template>
  <el-drawer
    :title="t('visualization.app_export')"
    v-model="state.applyDownloadDrawer"
    modal-class="de-user-drawer"
    size="600px"
    direction="rtl"
  >
    <div class="app-export">
      <el-form
        ref="applyDownloadForm"
        :model="state.form"
        :rules="state.rule"
        class="de-form-item"
        label-width="180px"
        label-position="top"
      >
        <el-form-item :label="t('visualization.app_name')" prop="appName">
          <el-input
            v-model="state.form.appName"
            autocomplete="off"
            :placeholder="t('common.input_name')"
          />
        </el-form-item>
        <el-form-item :label="t('visualization.app_version')" prop="version">
          <el-input v-model="state.form.version" autocomplete="off" />
        </el-form-item>
        <el-form-item :label="t('visualization.app_export')" prop="required">
          <el-input v-model="state.form.required" autocomplete="off" />
        </el-form-item>
        <el-form-item :label="t('visualization.creator')" prop="creator">
          <el-input v-model="state.form.creator" autocomplete="off" />
        </el-form-item>
        <el-form-item :label="t('visualization.description')" prop="description">
          <el-input
            :placeholder="t('commons.input_content')"
            show-word-limit
            v-model="state.form.description"
            type="textarea"
          />
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <div class="apply" style="width: 100%">
        <el-button secondary @click="close">{{ t('commons.cancel') }} </el-button>
        <el-button type="primary" @click="downloadApp">{{ t('chart.export') }}</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script lang="ts" setup>
import { ElButton, ElDrawer, ElForm, ElFormItem, ElInput } from 'element-plus-secondary'
import { reactive, ref, toRefs } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { export2AppCheck } from '@/api/visualization/dataVisualization'
const { t } = useI18n()
const emits = defineEmits(['closeDraw', 'downLoadApp'])
const applyDownloadForm = ref(null)

const props = defineProps({
  componentData: {
    type: Object,
    required: true
  },
  canvasViewInfo: {
    type: Object,
    required: true
  },
  dvInfo: {
    type: Object,
    required: true
  }
})

const { componentData, canvasViewInfo, dvInfo } = toRefs(props)

const state = reactive({
  applyDownloadDrawer: false,
  form: {
    appName: null,
    icon: null,
    version: null,
    creator: null,
    required: '2.8.0',
    description: null
  },
  rule: {
    appName: [
      {
        required: true,
        min: 2,
        max: 25,
        message: t('datasource.input_limit_2_25', [2, 25]),
        trigger: 'blur'
      }
    ],
    creator: [
      {
        required: true,
        min: 2,
        max: 25,
        message: t('datasource.input_limit_2_25', [2, 25]),
        trigger: 'blur'
      }
    ],
    required: [
      {
        required: true,
        min: 2,
        max: 25,
        message: t('datasource.input_limit_2_25', [2, 25]),
        trigger: 'blur'
      }
    ],
    version: [
      {
        required: true,
        min: 2,
        max: 25,
        message: t('datasource.input_limit_2_25', [2, 25]),
        trigger: 'blur'
      }
    ]
  }
})

const init = params => {
  state.applyDownloadDrawer = true
  state.form = params
}

const close = () => {
  emits('closeDraw')
  state.applyDownloadDrawer = false
}

const gatherAppInfo = (viewIds, dsIds, componentDataCheck) => {
  componentDataCheck.forEach(item => {
    if (item.component === 'VQuery' && item.propValue?.length) {
      item.propValue.forEach(filterItem => {
        if (filterItem.dataset?.id) {
          dsIds.push(filterItem.dataset.id)
        }
      })
    } else if (item.component === 'UserView' && canvasViewInfo.value[item.id]) {
      const viewDetails = canvasViewInfo.value[item.id]
      const { id, tableId } = viewDetails
      viewIds.push(id)
      dsIds.push(tableId)
    } else if (item.component === 'Group') {
      gatherAppInfo(viewIds, dsIds, item.propValue)
    } else if (item.component === 'DeTabs') {
      item.propValue.forEach(tabItem => {
        gatherAppInfo(viewIds, dsIds, tabItem.componentData)
      })
    }
  })
}
const downloadApp = () => {
  applyDownloadForm.value?.validate(valid => {
    if (valid) {
      const viewIds = []
      const dsIds = []
      gatherAppInfo(viewIds, dsIds, componentData.value)
      export2AppCheck({ dvId: dvInfo.value.id, viewIds, dsIds }).then(rsp => {
        const params = {
          ...rsp.data,
          ...state.form,
          visualizationInfo: JSON.stringify(dvInfo.value)
        }
        emits('downLoadApp', params)
        state.applyDownloadDrawer = false
      })
    } else {
      return false
    }
  })
}

defineExpose({
  init
})
</script>
<style lang="less" scoped>
.app-export {
  width: 100%;
  height: calc(100% - 56px);
}

.app-export-bottom {
  width: 100%;
  height: 56px;
  text-align: right;
}

:deep(.ed-drawer__body) {
  padding-bottom: 0 !important;
}
</style>

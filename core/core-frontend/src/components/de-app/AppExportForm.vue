<template>
  <el-drawer
    :title="'应用导出'"
    v-model="state.applyDownloadDrawer"
    custom-class="de-user-drawer"
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
        <el-form-item :label="'应用名称'" prop="appName">
          <el-input v-model="state.form.appName" autocomplete="off" :placeholder="'请输入名称'" />
        </el-form-item>
        <el-form-item :label="'应用版本号'" prop="version">
          <el-input v-model="state.form.version" autocomplete="off" />
        </el-form-item>
        <el-form-item :label="'DataEase最低版本号'" prop="required">
          <el-input v-model="state.form.required" autocomplete="off" />
        </el-form-item>
        <el-form-item :label="'作者'" prop="creator">
          <el-input v-model="state.form.creator" autocomplete="off" />
        </el-form-item>
        <el-form-item :label="'描述'" prop="description">
          <el-input
            :placeholder="'请输入内容'"
            show-word-limit
            v-model="state.form.description"
            type="textarea"
          />
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <div class="apply" style="width: 100%">
        <el-button secondary @click="close">{{ $t('commons.cancel') }} </el-button>
        <el-button type="primary" @click="downloadApp">导出</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script lang="ts" setup>
import { ElButton, ElDrawer, ElForm, ElFormItem, ElInput } from 'element-plus-secondary'
import { reactive, ref, toRefs } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { export2AppCheck } from '@/api/visualization/dataVisualization'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
const { t } = useI18n()
const emits = defineEmits(['closeDraw', 'downLoadApp'])
const applyDownloadForm = ref(null)
const dvMainStore = dvMainStoreWithOut()

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

const gatherAppInfo = (viewIds, dsIds) => {
  componentData.value.forEach(item => {
    if (item.component === 'UserView' && canvasViewInfo.value[item.id]) {
      const viewDetails = canvasViewInfo.value[item.id]
      const { id, tableId } = viewDetails
      viewIds.push(id)
      dsIds.push(tableId)
    } else if (item.component === 'Group') {
      item.propValue.forEach(groupItem => {
        if (groupItem.component === 'UserView') {
          const viewDetails = canvasViewInfo.value[groupItem.id]
          const { id, tableId } = viewDetails
          viewIds.push(id)
          dsIds.push(tableId)
        }
      })
    } else if (item.component === 'DeTabs') {
      item.propValue.forEach(tabItem => {
        tabItem.componentData.forEach(tabComponent => {
          if (tabComponent.component === 'UserView') {
            const viewDetails = canvasViewInfo.value[tabComponent.id]
            const { id, tableId } = viewDetails
            viewIds.push(id)
            dsIds.push(tableId)
          }
        })
      })
    }
  })
}

const downloadApp = () => {
  applyDownloadForm.value?.validate(valid => {
    if (valid) {
      const viewIds = []
      const dsIds = []
      gatherAppInfo(viewIds, dsIds)
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

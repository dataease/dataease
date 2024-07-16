<template>
  <el-drawer
    :title="'保存应用'"
    v-model="state.appApplyDrawer"
    custom-class="de-user-drawer"
    size="600px"
    direction="rtl"
  >
    <div class="app-export">
      <el-form
        ref="appSaveForm"
        :model="state.form"
        :rules="state.rule"
        class="de-form-item"
        label-width="180px"
        label-position="top"
      >
        <div class="de-row-rules" style="margin: 0 0 16px">
          <span>基本信息</span>
        </div>
        <el-form-item :label="dvPreName + '名称'" prop="appName">
          <el-input v-model="state.form.name" autocomplete="off" :placeholder="'请输入名称'" />
        </el-form-item>
        <el-form-item :label="dvPreName + '所在位置'" prop="version">
          <el-tree-select
            style="width: 100%"
            @keydown.stop
            @keyup.stop
            v-model="state.form.pid"
            :data="state.dvTree"
            :props="state.propsTree"
            @node-click="dvTreeSelect"
            :filter-method="dvTreeFilterMethod"
            :render-after-expand="false"
            filterable
          >
            <template #default="{ data: { name } }">
              <span class="custom-tree-node">
                <el-icon>
                  <Icon name="dv-folder"></Icon>
                </el-icon>
                <span :title="name">{{ name }}</span>
              </span>
            </template>
          </el-tree-select>
        </el-form-item>
        <el-form-item :label="'数据集分组名称'" prop="appName">
          <el-input
            v-model="state.form.datasetFolderName"
            autocomplete="off"
            :placeholder="'请输入名称'"
          />
        </el-form-item>
        <el-form-item label="数据集分组位置" prop="version">
          <el-tree-select
            style="width: 100%"
            @keydown.stop
            @keyup.stop
            v-model="state.form.datasetFolderPid"
            :data="state.dvTree"
            :props="state.propsTree"
            @node-click="dvTreeSelect"
            :filter-method="dvTreeFilterMethod"
            :render-after-expand="false"
            filterable
          >
            <template #default="{ data: { name } }">
              <span class="custom-tree-node">
                <el-icon>
                  <Icon name="dv-folder"></Icon>
                </el-icon>
                <span :title="name">{{ name }}</span>
              </span>
            </template>
          </el-tree-select>
        </el-form-item>
        <div class="de-row-rules" style="margin: 0 0 16px">
          <span>数据源信息</span>
        </div>
        <div>数据源信息配置</div>
      </el-form>
    </div>
    <template #footer>
      <div class="apply" style="width: 100%">
        <el-button secondary @click="close">{{ $t('commons.cancel') }} </el-button>
        <el-button type="primary" @click="downloadApp">保存</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script lang="ts" setup>
import {
  ElButton,
  ElDrawer,
  ElForm,
  ElFormItem,
  ElInput,
  ElTreeSelect
} from 'element-plus-secondary'
import { computed, reactive, ref, toRefs } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
const { t } = useI18n()
const emits = defineEmits(['closeDraw', 'downLoadApp'])
const appSaveForm = ref(null)

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
  },
  dvType: {
    type: String,
    default: 'dashboard'
  }
})

const { componentData, canvasViewInfo, dvInfo, dvType } = toRefs(props)

const dvPreName = computed(() => (dvType.value === 'dashboard' ? '仪表板' : '数据大屏'))

const state = reactive({
  appApplyDrawer: false,
  dvTree: [],
  propsTree: {
    label: 'name',
    children: 'children',
    isLeaf: node => !node.children?.length
  },
  form: {
    pid: '',
    name: '新建',
    datasetFolderPid: null,
    datasetFolderName: null,
    datasourceMap: {} // 数据源ID映射
  },
  rule: {
    name: [
      {
        required: true,
        min: 2,
        max: 25,
        message: t('datasource.input_limit_2_25', [2, 25]),
        trigger: 'blur'
      }
    ],
    pid: [
      {
        required: true,
        min: 2,
        max: 25,
        message: '请选择所属文件夹',
        trigger: 'blur'
      }
    ],
    datasetFolderName: [
      {
        required: true,
        min: 2,
        max: 25,
        message: t('datasource.input_limit_2_25', [2, 25]),
        trigger: 'blur'
      }
    ],
    datasetFolderPid: [
      {
        required: true,
        min: 2,
        max: 25,
        message: '请选择数据集分组所属文件夹',
        trigger: 'blur'
      }
    ]
  }
})

const init = params => {
  console.log('init==')
  state.appApplyDrawer = true
  state.form = params
}

const dvTreeFilterMethod = value => {
  state.dvTree = [...state.dvTree].filter(item => item.name.includes(value))
}

const dvTreeSelect = element => {
  state.form.pid = element.id
}

const close = () => {
  emits('closeDraw')
  state.appApplyDrawer = false
}

const saveApp = () => {
  appSaveForm.value?.validate(valid => {
    if (valid) {
      const viewIds = []
      const dsIds = []
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

.de-row-rules {
  display: flex;
  align-items: center;
  position: relative;
  font-size: 14px;
  font-weight: 500;
  line-height: 22px;
  padding-left: 10px;
  margin: 24px 0 16px 0;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    height: 14px;
    width: 2px;
    background: #3370ff;
  }
}
</style>

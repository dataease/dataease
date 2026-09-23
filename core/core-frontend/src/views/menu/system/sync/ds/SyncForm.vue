<script lang="ts" setup>
import dvFolder from '@/assets/svg/dv-folder.svg'
import {reactive, ref, watch} from 'vue'
import type { FormInstance } from 'element-plus-secondary'
import {BusiTreeRequest} from "@/models/tree/TreeNode";
import {checkRepeat, getDsTree, save} from "@/api/datasource";
import {ElMessage, ElMessageBox, ElMessageBoxOptions} from "element-plus-secondary";
import {useI18n} from '@/hooks/web/useI18n'
import {useCache} from "@/hooks/web/useCache";
import {Base64} from "js-base64";

const { t } = useI18n()
const { wsCache } = useCache()
export interface Tree {
  name: string
  value?: string | number
  id: string | number
  nodeType: string
  createBy?: string
  level: number
  leaf?: boolean
  pid: string | number
  type?: string
  createTime: number
  children?: Tree[]
  request: any
}
const activeAll = ref(false)
const formLoading = ref<boolean>(false)
const loadingDirectories = ref(false)
const syncForm = ref<FormInstance>()
const syncTargetToDatasourceFormVisible = ref<boolean>(false)
let request = ref<any>()
const form = reactive({
  pid: '' as string | number,
  name: ''
})
const init = (dsObj:any) => {
  form.name = dsObj.name
  request.value = dsObj
}
const props = {
  label: 'name',
  children: 'children',
  isLeaf: node => !node.children?.length
}
const state = reactive({
  tData: [] as Tree[],
})
const dfs = (arr: Tree[]) => {
  arr.forEach(ele => {
    ele.value = ele.id
    if (ele.children?.length) {
      dfs(ele.children)
    }
  })
}
const nodeClick = (data: Tree) => {
  activeAll.value = false
  form.pid = data.id as string
}
let directoryRequest = 0
const getDsDirList = async () => {
  const currentRequest = ++directoryRequest
  form.pid = ''
  state.tData = []
  syncForm.value?.clearValidate()
  loadingDirectories.value = true
  const params = { leaf: false, id: 0, weight: 7 } as BusiTreeRequest
  try {
    const res = await getDsTree(params)
    if (currentRequest !== directoryRequest) return
    state.tData = (res as unknown as Tree[]) || []
    state.tData.forEach(node => {
      if (String(node.id) === '0' && node.name === 'root') {
        node.name = t('sync_datasource.datasource')
      }
    })
    dfs(state.tData)
    form.pid = state.tData[0]?.id ?? ''
  } catch {
    // 请求失败时不使用上一次打开弹窗的目录。
  } finally {
    if (currentRequest === directoryRequest) loadingDirectories.value = false
  }
}
watch(syncTargetToDatasourceFormVisible, visible => {
  if (visible) {
    request.value = undefined
    getDsDirList()
  } else {
    directoryRequest++
  }
})
const closeDialog = ()=> {
  syncTargetToDatasourceFormVisible.value = false
}
const onSync = async () => {
  if (loadingDirectories.value || formLoading.value || !request.value) return
  const containsSelection = (nodes: Tree[]): boolean => nodes.some(node =>
    String(node.id) === String(form.pid) || containsSelection(node.children || [])
  )
  if (!containsSelection(state.tData)) form.pid = ''
  if (!(await syncForm.value?.validate().catch(() => false))) return
  if (request.value && form.name != "") {
    let options = {
      confirmButtonType: 'danger',
      type: 'warning',
      autofocus: false,
      showClose: false,
      tip: ''
    }
    const params = {
      name: request.value.name,
      // 系统数据源使用插件声明的兼容类型
      type: request.value.systemDatasourceType || request.value.type,
      description: request.value.desc,
      configuration: Base64.encode(JSON.stringify(request.value.configuration)),
      apiConfiguration:[]
    }
    formLoading.value = true
    checkRepeat(params).then(res => {
      if (res) {
        ElMessageBox.confirm(t('datasource.has_same_ds'), options as ElMessageBoxOptions).then(
            () => {
              save({ ...params, name: form.name, pid: form.pid })
                  .then(res => {
                    if (res !== undefined) {
                      wsCache.set('ds-new-success', true)
                      ElMessage.success(t('common.save_success'))
                      closeDialog()
                    }
                  })
                  .finally(() => {
                    formLoading.value = false
                  })
            },
            () => {
              formLoading.value = false
            }
        )
      } else {
        save({ ...params, name: form.name, pid: form.pid })
            .then(res => {
              if (res !== undefined) {
                wsCache.set('ds-new-success', true)
                ElMessage.success(t('common.save_success'))
                closeDialog()
              }
            })
            .finally(() => {
              formLoading.value = false
            })
      }
    })
    return
  }
}
const filterMethod = (value, data) => ()=> {
  return data.name.includes(value)
}
const rules = reactive({
  name: [{required: true, message: t('commons.name') + t('commons.cannot_be_null'), trigger: 'blur'}],
  pid: [{required: true, message: t('sync_datasource.select_folder'), trigger: 'change'}],
})
defineExpose({
  init,
  syncTargetToDatasourceFormVisible
})

</script>
<template>
  <el-dialog v-model="syncTargetToDatasourceFormVisible" width="420px" :z-index="2000">
   <template #title>
     <div class="title">
       <div class="main-title">
         {{ t('sync_datasource.sync_ds') }}
       </div>
       <div class="subtitle">
         <p> {{ t('sync_datasource.sync_to_datasource') }}</p>
       </div>
     </div>
   </template>
    <template #default>
      <div class="dialog-content">
        <el-form ref="syncForm" :model="form" label-position="top" v-loading="formLoading || loadingDirectories" :rules="rules">
          <el-form-item :label="t('sync_datasource.datasource') + t('sync_datasource.name')" prop="name" style="margin-bottom: 24px">
            <el-input v-model="form.name" :placeholder="t('sync_datasource.input_ds_name')"></el-input>
          </el-form-item>
          <el-form-item :label="t('sync_datasource.folder')" prop="pid" style="margin-bottom: 0">
            <el-tree-select
                v-model="form.pid"
                :data="state.tData"
                popper-class="dataset-tree-select"
                style="width: 100%"
                :render-after-expand="false"
                :props="props"
                :placeholder="t('sync_datasource.select_folder')"
                check-strictly
                @node-click="nodeClick"
                :filter-method="filterMethod"
                filterable
            >
              <template #default="{ data: { name } }">
                <el-icon>
                  <Icon name="dv-folder"><dvFolder class="svg-icon" /></Icon>
                </el-icon>
                <span :title="name">{{ name }}</span>
              </template>
            </el-tree-select>
          </el-form-item>
        </el-form>
      </div>
    </template>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="closeDialog">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :disabled="loadingDirectories || !state.tData.length || !request" :loading="formLoading" @click="onSync">{{ t('common.sure') }}</el-button>
      </span>
    </template>
  </el-dialog>
</template>
<style lang="less" scoped>
.title{
  .main-title{
    color: var(--ed-text-color-regular);
    font-family: var(--de-custom_font, 'PingFang');
    font-style: normal;
    font-weight: 500;
    font-size: 16px;
    line-height: 24px;
  }
  .subtitle{
    margin: 8px 0 0 0;
    > p{
      font-family: var(--de-custom_font, 'PingFang');
      font-size: 14px;
      font-weight: 400;
      line-height: 22px;
      letter-spacing: 0;
      text-align: left;
      color: #646A73;
    }
  }
}
.dialog-content{
  .ed-select {
    width: 100%;
  }
  :deep(.ed-select .ed-input__wrapper){
      padding-right: 12px;
      padding-left: 12px;
  }
}
</style>
<style lang="less">
.dataset-tree-select {
  .ed-select-dropdown__item {
    display: flex;
    align-items: center;
    .ed-icon {
      margin-right: 5px;
    }
  }
}
</style>

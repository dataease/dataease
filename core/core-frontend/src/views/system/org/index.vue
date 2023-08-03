<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox, ElIcon, Action } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import { Icon } from '@/components/icon-custom'
import DeptEditer from './DeptEditer.vue'
import OrgResources from './OrgResources.vue'
import { searchApi, resourceExistApi, deleteApi } from '@/api/org'
import { useUserStoreWithOut } from '@/store/modules/user'
import { useEmitt } from '@/hooks/web/useEmitt'
import { setColorName } from '@/utils/utils'
import { HandleMore } from '@/components/handle-more'
const { t } = useI18n()
const userStore = useUserStoreWithOut()
const activeName = ref('manage')
const keyword = ref(null)
const deptEditor = ref(null)
const defaultExpandAll = ref(false)
const timestampFormatDate = value => {
  if (!value) {
    return '-'
  }
  return new Date(value)['format']()
}
const expandRowKeys = ref<string[]>([])

const table = ref(null)

const tableData = ref<Org[]>([])

const allTableData = ref<Org[]>([])
const loading = ref(false)
interface Org {
  id: string
  name: number
  children?: []
  readOnly: boolean
  subCount: number
  createTime: number
}

tableData.value = []
allTableData.value = []

const moreList = [
  {
    label: t('org.org_move'),
    divided: false,
    svgName: 'de-move',
    command: 'move'
  },
  {
    label: t('common.delete'),
    divided: true,
    svgName: 'icon_delete-trash_outlined',
    command: 'delete'
  }
]

const handleClick = () => {
  console.log('handleClick')
}

const addOrg = row => {
  const pid = row?.id || (userStore.getUid === '1' ? null : userStore.getOid)
  deptEditor?.value?.createOrg && deptEditor.value.createOrg(pid)
}

const edit = row => {
  const { id, name } = row
  deptEditor?.value?.editOrg && deptEditor.value.editOrg({ id, name })
}
// 加载表格数据
const search = () => {
  loading.value = true
  tableData.value = []
  searchApi(null).then(res => {
    tableData.value = res.data
    if (!keyword.value) {
      allTableData.value = res.data
    }
    loading.value = false
  })
}

const deptIsEmpty = organization => {
  if (organization.children) {
    canNotDelete()
    return
  }

  resourceExistApi(organization.id).then(res => {
    if (res.data) {
      resourceExistMsg()
    } else {
      _handleDeleteZero(organization)
    }
  })
}
const canNotDelete = () => {
  ElMessageBox.confirm(t('org.cannot_delete'), {
    confirmButtonText: t('common.roger_that'),
    showCancelButton: false,
    tip: t('org.delete_children_first'),
    confirmButtonType: 'primary',
    type: 'warning',
    autofocus: false,
    showClose: false
  })
}

const resourceExistMsg = () => {
  ElMessageBox.confirm(t('org.confirm_delete'), {
    showPreButton: true,
    confirmButtonText: t('org.move_resource_first'),
    cancelButtonText: t('common.cancel'),
    showCancelButton: true,
    preButtonText: t('org.give_up_resource'),
    preButtonType: 'danger',
    tip: t('org.confirm_content'),
    confirmButtonType: 'primary',
    type: 'warning',
    autofocus: false,
    showClose: false,
    callback: (action: Action) => {
      ElMessage({
        message: `action: ${action}`,
        type: 'success'
      })
    }
  })
}

const _handleDeleteZero = organization => {
  ElMessageBox.confirm(t('org.confirm_delete'), {
    confirmButtonType: 'danger',
    type: 'warning',
    autofocus: false,
    showClose: false
  }).then(() => {
    loading.value = true
    deleteApi(organization.id).then(() => {
      ElMessage({
        message: t('common.delete_success'),
        type: 'success'
      })
      search()
      useEmitt().emitter.emit('refresh-org-options')
    })
  })
}
const dynamicResourceClass = param => {
  const row = param.row
  return row.hidden ? 'dynamic-resource-hidden' : ''
}
const matchFilter = (row, val): boolean => {
  let match = !val || row.name.toLocaleLowerCase().includes(val.toLocaleLowerCase())
  setColorName(row, val)
  if (row.children?.length) {
    for (let index = 0; index < row.children.length; index++) {
      const kid = row.children[index]
      const kidMatch = matchFilter(kid, val)
      if (kidMatch && !match) {
        match = kidMatch
      }
    }
  }
  row.hidden = !match

  if (match) {
    expandRowKeys.value.push(row.id)
  }
  return match
}
const filterGrid = val => {
  clearExpandKeys()
  tableData.value.forEach(item => {
    matchFilter(item, val)
  })
}
const clearExpandKeys = () => {
  let len = expandRowKeys.value.length
  while (len--) {
    expandRowKeys.value.splice(len, 1)
  }
}

const moreHandler = (cmd: string, row: Org) => {
  if (cmd === 'delete') {
    deptIsEmpty(row)
    return
  }
  console.log('This module is under development')
}
onMounted(() => {
  search()
})
const saveCallBack = () => {
  search()
  useEmitt().emitter.emit('refresh-org-options')
}
</script>

<template>
  <el-tabs v-model="activeName" @tab-click="handleClick">
    <el-tab-pane :label="t('org.org_title')" name="manage"></el-tab-pane>
    <el-tab-pane :label="t('org.org_move')" name="resources"></el-tab-pane>
  </el-tabs>
  <div class="org-table__content de-search-table" v-if="activeName === 'manage'">
    <el-row class="top-operate">
      <el-col :span="12">
        <el-button @click="addOrg" type="primary">
          <template #icon>
            <Icon name="icon_add_outlined"></Icon>
          </template>
          {{ t('org.add') }}
        </el-button>
      </el-col>
      <el-col :span="12" class="right-filter">
        <el-input
          :placeholder="t('org.search_placeholder')"
          v-model="keyword"
          clearable
          @change="filterGrid"
        >
          <template #prefix>
            <el-icon>
              <Icon name="icon_search-outline_outlined"></Icon>
            </el-icon>
          </template>
        </el-input>
      </el-col>
    </el-row>
    <div class="table-container-org">
      <el-table
        ref="table"
        :data="tableData"
        header-cell-class-name="header-cell"
        :indent="30"
        style="width: 100%"
        row-key="id"
        :default-expand-all="defaultExpandAll"
        :expand-row-keys="expandRowKeys"
        :row-class-name="dynamicResourceClass"
      >
        <el-table-column :label="t('org.name')" prop="name">
          <template v-slot:default="scope">
            <span v-if="scope.row.colorName" v-html="scope.row.colorName" />
            <span v-else>{{ scope.row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="t('org.sub_count')" prop="subCount">
          <template v-slot:default="scope">
            <span>{{
              scope.row.readOnly ? '' : (scope.row.children && scope.row.children.length) || 0
            }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="t('common.create_time')">
          <template v-slot:default="scope">
            <span>{{ scope.row.readOnly ? '' : timestampFormatDate(scope.row.createTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column :label="t('common.operate')" fixed="right" width="186">
          <!-- <template #default="scope">
            <span v-if="scope.row.readOnly"></span>
            <div v-else class="operate-icon-container">
              <div><Icon name="add" @click="addOrg(scope.row)"></Icon></div>
              <div><Icon name="edit" @click="edit(scope.row)"></Icon></div>
              <div :class="scope.row.id === '1' ? 'icon-disabled' : ''">
                <Icon name="delete" @click="deptIsEmpty(scope.row)"></Icon>
              </div>
            </div>
          </template> -->

          <template #default="scope">
            <span v-if="scope.row.readOnly"></span>
            <div v-else>
              <el-button @click="addOrg(scope.row)" text>
                <template #icon>
                  <Icon name="icon_add_outlined"></Icon>
                </template>
              </el-button>
              <el-button @click="edit(scope.row)" text>
                <template #icon>
                  <Icon name="icon_edit_outlined  "></Icon>
                </template>
              </el-button>

              <div class="icon-more" v-if="scope.row.id !== '1'">
                <handle-more
                  @handle-command="cmd => moreHandler(cmd, scope.row)"
                  :menu-list="moreList"
                />
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <dept-editer ref="deptEditor" @saved="saveCallBack" :tree-data="allTableData"></dept-editer>
  </div>
  <div v-else class="org-table__content de-search-table">
    <org-resources></org-resources>
  </div>
</template>

<style lang="less">
.org-table__content {
  padding: 24px;
  width: 100%;
  background: var(--ContentBG, #ffffff);
  height: calc(100% - 60px);
  box-sizing: border-box;
  margin-top: 12px;
}
.table-container-org {
  height: calc(100vh - 260px);
  :deep(.ed-icon-arrow-right::before) {
    content: '\E791' !important;
  }
}
.dynamic-resource-hidden {
  display: none !important;
}
.btn-outer {
  width: 36px;
  display: inline-block;
  margin-left: 12px;
  margin-right: 6px;
}
.icon-more {
  color: var(--ed-color-primary) !important;
  border: 0 solid transparent;
  background-color: transparent;
  font-family: PingFang SC;
  font-size: 14px;
  font-weight: 400;
  line-height: 26px;
  height: 26px;
  letter-spacing: 0;
  text-align: center;
  padding: 2px 4px;
  display: inline-flex;
  justify-content: center;
  align-items: center;
  height: 32px;
  white-space: nowrap;
  cursor: pointer;
  box-sizing: border-box;
  outline: 0;
  transition: 0.1s;

  user-select: none;
  vertical-align: middle;

  border-radius: var(--ed-border-radius-base);
  .ed-dropdown {
    color: var(--ed-color-primary) !important;
    &:hover {
      i {
        background-color: rgba(51, 112, 255, 0.1);
      }
    }
  }
}
/* .operate-icon-container {
  font-size: 16px;
  display: flex;
  div {
    width: 24px;
    height: 20px;
    padding: 0 3px;
    svg {
      width: 16px;
      height: 16px;
      color: var(--ed-text-color-regular);
      background-color: var(--ed-color-white);
    }
  }

  div:hover:not(.icon-disabled) {
    cursor: pointer;
    svg {
      color: var(--ed-color-primary) !important;
      background: var(--ed-color-primary-light-7) !important;
    }
  }
  .icon-disabled {
    color: var(--ed-button-disabled-text-color);
    cursor: not-allowed;
    background-image: none;
    background-color: var(--ed-button-disabled-bg-color);
    border-color: var(--ed-button-disabled-border-color);
  }
} */
</style>

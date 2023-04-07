<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox, ElIcon } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import { Icon } from '@/components/icon-custom'
import DeptEditer from './DeptEditer.vue'
import OrgResources from './OrgResources.vue'
import { searchApi, resourceExistApi, deleteApi } from '@/api/org'
import { useUserStoreWithOut } from '@/store/modules/user'
const { t } = useI18n()
const userStore = useUserStoreWithOut()
const activeName = ref('manage')
const keyword = ref(null)
const deptEditor = ref(null)

const timestampFormatDate = value => {
  if (!value) {
    return '-'
  }
  return new Date(value).format()
}

const table = ref(null)

const tableData = ref<Org[]>([])

const allTableData = ref<Org[]>([])

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

const handleClick = () => {
  console.log('handleClick')
}

const addOrg = row => {
  const pid = row?.id || userStore.getOid
  deptEditor?.value?.createOrg && deptEditor.value.createOrg(pid)
}

const edit = row => {
  const { id, name } = row
  deptEditor?.value?.editOrg && deptEditor.value.editOrg({ id, name })
}
// 加载表格数据
const search = () => {
  tableData.value = []
  const param = keyword.value
  searchApi(param).then(res => {
    tableData.value = res.data
    if (!keyword.value) {
      allTableData.value = res.data
    }
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
    confirmButtonText: t('org.move_resource_first'),
    cancelButtonText: t('common.cancel'),
    showCancelButton: true,
    // dangerouslyUseHTMLString: true,
    // message: '<span>' + t('org.confirm_content') + '</span>',
    tip: t('org.confirm_content'),
    confirmButtonType: 'primary',
    type: 'warning',
    autofocus: false,
    showClose: false
  })
}

const _handleDeleteZero = organization => {
  ElMessageBox.confirm(t('org.confirm_delete'), {
    confirmButtonType: 'danger',
    type: 'warning',
    autofocus: false,
    showClose: false
  }).then(() => {
    deleteApi(organization.id).then(() => {
      ElMessage({
        message: t('common.delete_success'),
        type: 'success'
      })
      search()
    })
  })
}

onMounted(() => {
  search()
})
const saveCallBack = () => {
  search()
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
          @blur="search"
          @clear="search"
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
      >
        <el-table-column :label="t('org.name')" prop="name"> </el-table-column>
        <el-table-column :label="t('org.sub_count')" prop="subCount">
          <template v-slot:default="scope">
            <span>{{ (scope.row.children && scope.row.children.length) || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="t('common.create_time')">
          <template v-slot:default="scope">
            <span>{{ timestampFormatDate(scope.row.createTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column :label="t('common.operate')" fixed="right" width="186">
          <template #default="scope">
            <el-button @click="edit(scope.row)" type="text">{{ t('common.edit') }}</el-button>

            <template v-if="scope.row.id === '1'">
              <el-tooltip
                class="item"
                effect="dark"
                :content="t('org.default_cannot_move')"
                placement="left"
              >
                <div class="btn-outer">
                  <el-button disabled type="text">{{ t('common.delete') }}</el-button>
                </div>
              </el-tooltip>
            </template>
            <el-button v-else @click="deptIsEmpty(scope.row)" type="text">{{
              t('common.delete')
            }}</el-button>
            <el-button @click="addOrg(scope.row)" type="text">{{ t('org.add_sub') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <dept-editer ref="deptEditor" @saved="saveCallBack" :treeData="allTableData"></dept-editer>
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

  :deep(.el-icon-arrow-right::before) {
    content: '\E791' !important;
  }
}
.btn-outer {
  width: 36px;
  display: inline-block;
  margin-left: 12px;
  margin-right: 6px;
}
</style>

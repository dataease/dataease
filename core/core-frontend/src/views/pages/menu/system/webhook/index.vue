<template>
  <p class="router-title">{{ t('webhook.title') }}</p>
  <div class="org-table__content border-radius-12" v-loading="loading">
    <el-row class="top-operate">
      <el-col :span="12">
        <el-button @click="addHandler" type="primary">
          {{ t('webhook.add') }}
        </el-button>
      </el-col>

      <el-col :span="12" class="webhook-right-filter">
        <el-input
          v-model="keyword"
          clearable
          :placeholder="t('webhook.search_placeholder')"
          @change="search"
        >
          <template #prefix>
            <el-icon>
              <Icon name="icon_search-outline_outlined"
                ><icon_searchOutline_outlined class="svg-icon"
              /></Icon>
            </el-icon>
          </template>
        </el-input>
      </el-col>
    </el-row>
    <div
      class="auth-card-container"
      :class="{ 'show-bottom-container': state.multipleSelection.length }"
    >
      <GridTable
        ref="multipleTableRef"
        :table-data="state.instanceList"
        :pagination="state.paginationConfig"
        class="popper-max-width"
        :empty-desc="emptyDesc"
        :empty-img="imgType"
        :show-empty-img="!loading"
        @current-change="pageChange"
        @size-change="sizeChange"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="40" />
        <el-table-column
          key="name"
          show-overflow-tooltip
          prop="name"
          :label="t('common.name')"
          width="220"
        >
          <template v-slot:default="scope">
            <span>{{ scope.row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="url" key="url" label="URL" show-overflow-tooltip />

        <el-table-column
          prop="contentType"
          key="contentType"
          :label="t('webhook.content_type')"
          show-overflow-tooltip
          width="257"
        />

        <el-table-column
          prop="secret"
          show-overflow-tooltip
          key="secret"
          label="Secret"
          width="120"
        />

        <el-table-column
          prop="ssl"
          show-overflow-tooltip
          key="ssl"
          :label="`SSL ${t('commons.verification')}`"
          width="132"
        >
          <template #default="scope">
            <el-switch
              size="small"
              v-model="scope.row.ssl"
              @change="sslChange(scope.row)"
            ></el-switch>
          </template>
        </el-table-column>

        <el-table-column width="80" fixed="right" key="_operation" :label="t('common.operate')">
          <template #default="scope">
            <div class="operate-icon-container">
              <el-tooltip effect="dark" :content="t('common.edit')" placement="top">
                <el-button text @click="editHandler(scope.row)">
                  <template #icon>
                    <Icon name="icon_edit_outlined"><icon_edit_outlined class="svg-icon" /></Icon>
                  </template>
                </el-button>
              </el-tooltip>

              <el-tooltip effect="dark" :content="t('common.delete')" placement="top">
                <el-button text @click="delHandler(scope.row)">
                  <template #icon>
                    <Icon name="icon_delete-trash_outlined"
                      ><icon_deleteTrash_outlined class="svg-icon"
                    /></Icon>
                  </template>
                </el-button>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
      </GridTable>
    </div>
  </div>
  <div v-if="state.multipleSelection.length" class="bottom-bar flex-align-center">
    <el-button type="danger" class="batch-delete-button" plain @click="batchDelHandler">
      {{ t('sync_task.batch_del') }}
    </el-button>
    <span class="bottom-info">{{
      t('sync_task.selection_info', [state.multipleSelection.length])
    }}</span>
    <el-button text @click="allSelection">{{
      `${t('dataset.check_all')} ${Math.min(
        state.paginationConfig.pageSize,
        state.paginationConfig.total
      )} 项`
    }}</el-button>
    <el-button text @click="clearSelection">{{ t('sync_task.clear_button') }}</el-button>
  </div>
  <editor ref="formEditor" @saved="search" />
</template>

<script lang="ts" setup>
import icon_searchOutline_outlined from '@/assets/svg/icon_search-outline_outlined.svg'
import icon_edit_outlined from '@/assets/svg/icon_edit_outlined.svg'
import icon_deleteTrash_outlined from '@/assets/svg/icon_delete-trash_outlined.svg'
import { ref, reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import request from '@/config/axios'
import GridTable from '@/components/grid-table/src/GridTable.vue'
import { ElMessage, ElMessageBox } from 'element-plus-secondary'
import editor from '@/views/menu/system/webhook/editor.vue'
const { t } = useI18n()
const loading = ref(false)
const formEditor = ref()
const keyword = ref('')
const imgType = ref()
const emptyDesc = ref('')
const multipleTableRef = ref()
const state = reactive({
  instanceList: [],
  paginationConfig: {
    currentPage: 1,
    pageSize: 10,
    total: 0
  },
  multipleSelection: []
})

const handleSelectionChange = val => {
  state.multipleSelection = val
}
const pageChange = index => {
  if (typeof index !== 'number') {
    return
  }
  state.paginationConfig.currentPage = index
  search()
}
const sizeChange = size => {
  state.paginationConfig.pageSize = size
  state.paginationConfig.currentPage = 1
  search()
}

const allSelection = () => {
  multipleTableRef.value?.toggleAllSelection()
}
const clearSelection = () => {
  multipleTableRef.value?.clearSelection()
}
const batchDelHandler = () => {
  const len = state.multipleSelection.length
  const confirmMsg = t('webhook.batch_del_confirm', [len])
  const boxOption = {
    confirmButtonType: 'danger',
    type: 'warning',
    confirmButtonText: t('common.delete'),
    cancelButtonText: t('dataset.cancel'),
    autofocus: false,
    showClose: false
  }
  ElMessageBox.confirm(confirmMsg, boxOption)
    .then(() => {
      const url = '/webhook/delete'
      const ids = state.multipleSelection.map(item => item['id'])
      if (ids?.length) {
        request.post({ url, data: ids }).then(() => {
          ElMessage.success(t('common.delete_success'))
          search()
        })
      }
    })
    .finally(err => {
      ElMessage.error(err)
    })
}

const search = () => {
  const url = `/webhook/pager/${state.paginationConfig.currentPage}/${state.paginationConfig.pageSize}`
  loading.value = true
  const param = { keyword: keyword.value }
  request
    .post({ url, data: param })
    .then(res => {
      const data = res.data.records
      if (data?.length) {
        state.instanceList = data
      } else {
        state.instanceList = []
      }
      state.paginationConfig.total = res.data.total
      imgType.value = getEmptyImg()
      emptyDesc.value = getEmptyDesc()
    })
    .finally(() => {
      loading.value = false
    })
}
const addHandler = () => {
  formEditor?.value.edit()
}

const editHandler = row => {
  formEditor?.value.edit(row.id)
}

const delHandler = row => {
  ElMessageBox.confirm(t('webhook.del_confirm'), {
    confirmButtonType: 'danger',
    type: 'warning',
    confirmButtonText: t('common.delete'),
    cancelButtonText: t('dataset.cancel'),
    autofocus: false,
    showClose: false
  })
    .then(() => {
      loading.value = true
      const id = row.id
      const url = 'webhook/delete'
      request.post({ url, data: [id] }).then(() => {
        ElMessage.success(t('common.delete_success'))
        search()
      })
    })
    .finally(() => {
      loading.value = false
    })
}
const sslChange = row => {
  const url = 'webhook/switchSsl'
  const param = {
    id: row.id,
    ssl: row.ssl
  }
  request.post({ url, data: param }).then(() => {
    ElMessage.success(t('user.switch_success'))
    search()
  })
}
const getEmptyImg = (): string => {
  if (keyword.value) {
    return 'tree'
  }
  return 'noneWhite'
}

const getEmptyDesc = (): string => {
  if (keyword.value) {
    return t('work_branch.relevant_content_found')
  }

  return ''
}
search()
</script>

<style lang="less" scoped>
.router-title {
  color: #1f2329;
  font-feature-settings: 'clig' off, 'liga' off;
  font-family: var(--de-custom_font, 'PingFang');
  font-size: 20px;
  font-style: normal;
  font-weight: 500;
  line-height: 28px;
}
.org-table__content {
  padding: 24px 24px 0 24px;
  border-radius: 6px;
  width: 100%;
  background: var(--ContentBG, #ffffff);
  height: calc(100vh - 140px) !important;
  box-sizing: border-box;
  margin-top: 16px;
}
.auth-card-container {
  margin-top: 20px;
  height: calc(100% - 74px);
  .authentication-card {
    min-width: 204px;
    width: 24%;
    height: 58px;
    border: 1px solid #dee0e3;
    border-radius: 12px;
    margin: 10px 10px 10px 0;
    float: left;
    padding-left: 16px;
    .inner-card {
      height: 100%;
      display: flex;
      align-items: center;
      .card-span {
        width: calc(100% - 50px);
      }
    }
  }
}
.show-bottom-container {
  height: calc(100% - 118px) !important;
}
.reset-secret-confirm {
  padding: 5px 15px;
  .confirm-header {
    width: 100%;
    height: 40px;
    line-height: 40px;
    display: flex;
    flex-direction: row;
    .icon-span {
      color: var(--ed-color-warning);
      font-size: 22px;
      i {
        top: 3px;
      }
    }
    .header-span {
      font-size: 16px;
      font-weight: bold;
      margin-left: 10px;
    }
  }
  .confirm-foot {
    padding: 0;
    display: flex;
    flex-wrap: wrap;
    justify-content: flex-end;
    align-items: center;
    margin-top: 15px;
    .ed-button {
      min-width: 48px;
      height: 28px;
      line-height: 28px;
      font-size: 12px;
    }
  }
  .confirm-warning {
    font-size: 12px;
    color: var(--ed-color-danger);
    margin-left: 33px;
  }
  .confirm-content {
    margin-left: 33px;
    display: flex;
    align-items: center;
  }
}

.webhook-right-filter {
  text-align: right;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  .ed-input {
    width: 300px;
  }
}
.bottom-bar {
  position: absolute;
  bottom: 0;
  height: 64px;
  width: calc(100% - 327px);
  padding-left: 24px;
  background: var(--neutral-00, #fff);
  box-shadow: 0px -2px 4px 0px rgba(31, 35, 41, 0.08);

  .bottom-info {
    color: #646a73;
    margin: 0 16px 0 24px;
  }

  .batch-delete-button {
    color: var(--ed-button-text-color);
    border-color: var(--ed-button-border-color);

    &:hover {
      color: var(--ed-button-hover-text-color);
      border-color: var(--ed-button-hover-border-color);
      background-color: var(--ed-button-hover-bg-color);
      outline: none;
    }
  }
}
.operate-icon-container {
  .ed-button + .ed-button {
    margin-left: 4px;
  }
}
</style>

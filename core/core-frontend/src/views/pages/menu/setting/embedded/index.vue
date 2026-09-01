<template>
  <div class="org-table__content" v-loading="loading" v-if="state.instanceList?.length || keyword">
    <el-row class="top-operate">
      <el-col :span="12">
        <el-tooltip
          v-if="limitCount && state.instanceList?.length >= limitCount"
          effect="dark"
          :content="t('system.to_5_applications', [limitCount])"
          placement="top"
        >
          <el-button disabled @click="addHandler" type="info">
            {{ t('system.create_embedded_application') }}
          </el-button>
        </el-tooltip>
        <el-button v-else @click="addHandler" type="primary">
          {{ t('system.create_embedded_application') }}
        </el-button>
      </el-col>

      <el-col :span="12" class="embedded-right-filter">
        <el-input
          v-model="keyword"
          clearable
          :placeholder="t('system.embedded_search_placeholder')"
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
        @current-change="pageChange"
        @size-change="sizeChange"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="40" />
        <el-table-column key="name" show-overflow-tooltip prop="name">
          <template #header>
            <span style="white-space: nowrap" :title="t('system.application_name')">{{
              t('system.application_name')
            }}</span>
          </template>
          <template v-slot:default="scope">
            <span>{{ scope.row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="appId" key="appId" label="APP ID" show-overflow-tooltip width="200">
          <template v-slot:default="scope">
            <div class="embedded-line-item">
              <span v-html="scope.row.appId" />
              <el-tooltip effect="dark" :content="t('common.copy')" placement="top">
                <el-button text @click="copyAppId(scope.row)">
                  <template #icon>
                    <Icon name="de-copy"><deCopy class="svg-icon" /></Icon>
                  </template>
                </el-button>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>

        <el-table-column
          prop="appSecret"
          key="appSecret"
          label="APP Secret"
          show-overflow-tooltip
          width="260"
        >
          <template v-slot:default="scope">
            <div class="embedded-line-item">
              <el-tooltip
                effect="dark"
                v-if="scope.row.show"
                :content="scope.row.appSecret"
                placement="top"
              >
                <span class="ellipsis" style="display: inline-block; width: 110px">{{
                  scope.row.appSecret
                }}</span>
              </el-tooltip>
              <span v-else v-html="scope.row.show ? scope.row.appSecret : '**********'" />
              <el-tooltip effect="dark" :content="t('common.copy')" placement="top">
                <el-button text @click="copyAppSecret(scope.row)">
                  <template #icon>
                    <Icon name="de-copy"><deCopy class="svg-icon" /></Icon>
                  </template>
                </el-button>
              </el-tooltip>

              <el-tooltip
                effect="dark"
                :content="scope.row.show ? t('system.click_to_hide') : t('system.click_to_show')"
                placement="top"
              >
                <el-button text @click="viewSecret(scope.row)">
                  <template #icon>
                    <Icon>
                      <component :is="scope.row.show ? eyeOpen : eye"></component>
                    </Icon>
                  </template>
                </el-button>
              </el-tooltip>

              <div @click="scope.row.resetInfoShow = true">
                <el-tooltip effect="dark" :content="t('dataset.update')" placement="top">
                  <el-button
                    text
                    :ref="
                      el => {
                        setButtonRef(el, scope.row)
                      }
                    "
                    v-click-outside="onClickOutside(scope.row)"
                  >
                    <template #icon>
                      <Icon name="icon_refresh_outlined"
                        ><icon_refresh_outlined class="svg-icon"
                      /></Icon>
                    </template>
                  </el-button>
                </el-tooltip>
                <el-popover
                  placement="bottom"
                  :width="280"
                  popper-class="reset-secret-popover"
                  :virtual-ref="scope.row.buttonRef"
                  trigger="click"
                  :ref="
                    el => {
                      setPopoverRef(el, scope.row)
                    }
                  "
                  :show-arrow="true"
                >
                  <div class="reset-secret-confirm">
                    <div class="confirm-header">
                      <span class="icon-span">
                        <el-icon>
                          <Icon name="icon_warning_filled"
                            ><icon_warning_filled class="svg-icon"
                          /></Icon>
                        </el-icon>
                      </span>
                      <span class="header-span">{{ t('system.update_app_secret') }}</span>
                    </div>
                    <div class="confirm-content">
                      <span>{{ t('system.operate_with_caution') }}</span>
                    </div>
                    <div class="confirm-foot">
                      <el-button secondary @click="closeResetInfo(scope.row)">{{
                        t('common.cancel')
                      }}</el-button>
                      <el-button type="primary" @click="resetSecret(scope.row)">
                        {{ t('common.sure') }}
                      </el-button>
                    </div>
                  </div>
                </el-popover>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column
          prop="domain"
          show-overflow-tooltip
          key="domain"
          :label="t('system.cross_domain_settings')"
          width="200"
        />

        <el-table-column width="124" fixed="right" key="_operation" :label="t('common.operate')">
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
  <div v-else class="org-table__content">
    <el-empty class="embedded-empty" :image="nothingNone" :description="t('system.no_application')">
      <el-button type="primary" @click="addHandler">{{
        t('system.create_embedded_application')
      }}</el-button>
    </el-empty>
  </div>
  <div v-if="state.multipleSelection.length" class="bottom-bar flex-align-center">
    <el-button type="danger" class="batch-delete-button" plain @click="batchDelHandler">
      {{ t('sync_task.batch_del') }}
    </el-button>
    <span class="bottom-info">{{
      t('sync_task.selection_info', [state.multipleSelection.length])
    }}</span>
    <el-button text @click="allSelection">{{
      `${t('dataset.check_all')} ${state.paginationConfig.pageSize} 项`
    }}</el-button>
    <el-button text @click="clearSelection">{{ t('sync_task.clear_button') }}</el-button>
  </div>
  <editor ref="formEditor" @saved="search" />
</template>

<script lang="ts" setup>
import eyeOpen from '@/assets/svg/eye-open.svg'
import icon_searchOutline_outlined from '@/assets/svg/icon_search-outline_outlined.svg'
import eye from '@/assets/svg/eye.svg'
import deCopy from '@/assets/svg/de-copy.svg'
import icon_refresh_outlined from '@/assets/svg/icon_refresh_outlined.svg'
import icon_warning_filled from '@/assets/svg/icon_warning_filled.svg'
import icon_edit_outlined from '@/assets/svg/icon_edit_outlined.svg'
import icon_deleteTrash_outlined from '@/assets/svg/icon_delete-trash_outlined.svg'
import { ref, reactive, unref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import nothingNone from '@/assets/img/nothing-none.png'
import request from '@/config/axios'
import GridTable from '@/components/grid-table/src/GridTable.vue'
import { ElMessage, ElMessageBox } from 'element-plus-secondary'
import useClipboard from 'vue-clipboard3'
import editor from '@/views/menu/setting/embedded/editor.vue'
const { t } = useI18n()
const loading = ref(false)
const formEditor = ref()
const limitCount = ref(0)
const keyword = ref('')
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
const { toClipboard } = useClipboard()

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
  state.paginationConfig.currentPage = 1
  state.paginationConfig.pageSize = size
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
  const confirmMsg = t('system.embedded_del_confirm', [len])
  const boxOption = {
    confirmButtonType: 'danger',
    type: 'warning',
    confirmButtonText: t('common.delete'),
    cancelButtonText: t('dataset.cancel'),
    autofocus: false,
    showClose: false
  }
  ElMessageBox.confirm(confirmMsg, boxOption).then(() => {
    const url = '/embedded/batchDelete'
    const ids = state.multipleSelection.map(item => item['id'])
    if (ids?.length) {
      request.post({ url, data: ids }).then(() => {
        ElMessage.success(t('common.delete_success'))
        pageChange(1)
      })
    }
  })
}

const getLimitCount = () => {
  const url = '/embedded/limitCount'
  request.get({ url }).then(res => {
    limitCount.value = res.data
  })
}
const imgType = ref()
const emptyDesc = ref('')
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
const search = () => {
  const url = `/embedded/pager/${state.paginationConfig.currentPage}/${state.paginationConfig.pageSize}`
  loading.value = true
  const param = { keyword: keyword.value }
  request
    .post({ url, data: param })
    .then(res => {
      const data = res.data.records
      if (data?.length) {
        const list = data.map(item => {
          item['show'] = false
          return item
        })
        state.instanceList = list
      } else {
        state.instanceList = []
      }
      imgType.value = getEmptyImg()
      emptyDesc.value = getEmptyDesc()
      state.paginationConfig.total = res.data.total
    })
    .finally(() => {
      loading.value = false
    })
}
const createLimit = (count?: number) => {
  const realCount = count ? count : state.instanceList.length || 0
  if (limitCount.value > 0 && realCount >= limitCount.value) {
    ElMessageBox.confirm(t('dataset.tips'), {
      confirmButtonType: 'primary',
      type: 'warning',
      confirmButtonText: t('common.roger_that'),
      cancelButtonText: t('dataset.cancel'),
      autofocus: false,
      showClose: false,
      showCancelButton: false,
      tip: t('system.to_5_applications', [limitCount.value])
    })
    return false
  }
  return true
}
const addHandler = () => {
  if (createLimit()) {
    formEditor?.value.edit()
  }
}

const editHandler = row => {
  formEditor?.value.edit(row)
}

const delHandler = row => {
  ElMessageBox.confirm(t('system.delete_this_application'), {
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
      const url = `embedded/delete/${id}`
      request.post({ url }).then(() => {
        ElMessage.success(t('common.delete_success'))
        pageChange(1)
      })
    })
    .finally(() => {
      loading.value = false
    })
}

const copyAppId = async row => {
  try {
    await toClipboard(row.appId)
    ElMessage.success(t('common.copy_success'))
  } catch (e) {
    ElMessage.warning(t('common.copy_unsupported'), e)
  }
}
const copyAppSecret = async row => {
  try {
    await toClipboard(row.appSecret)
    ElMessage.success(t('common.copy_success'))
  } catch (e) {
    ElMessage.warning(t('common.copy_unsupported'), e)
  }
}
const viewSecret = row => {
  const show = !!row.show
  row['show'] = !show
}
const resetSecret = row => {
  const url = '/embedded/reset'
  const data = { id: row.id, appSecret: row.appSecret }
  loading.value = true
  request
    .post({ url, data })
    .then(() => {
      ElMessage.success(t('user.reset_success'))
      pageChange(1)
    })
    .finally(() => {
      loading.value = false
    })
}

const setPopoverRef = (el, row) => {
  row.popoverRef = el
}
const setButtonRef = (el, row) => {
  row.buttonRef = el
}
const onClickOutside = row => {
  if (row.popoverRef) {
    unref(row.popoverRef).popperRef?.delayHide?.()
  }
}
const closeResetInfo = row => {
  row.popoverRef?.hide()
  row.resetInfoShow = false
}

search()
getLimitCount()
</script>

<style lang="less" scoped>
.org-table__content {
  padding: 24px 24px 0 24px;
  width: 100%;
  background: var(--ContentBG, #ffffff);
  height: calc(100vh - 107px) !important;
  box-sizing: border-box;
  margin-top: 8px;
  border-radius: 12px;
  .embedded-empty {
    padding-top: 136px !important;
    height: 155px;
    margin: 0;
    padding: 0;
    :deep(.ed-empty__image) {
      height: 125px;
      width: 125px;
      margin: auto;
    }
    :deep(.ed-empty__description) {
      line-height: 22px;
      margin-top: 8px !important;
      p {
        font-size: 14px;
      }
    }
  }
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
.embedded-line-item {
  display: flex;
  align-items: center;
  button {
    margin-left: 6px;
  }
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

.embedded-right-filter {
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
</style>

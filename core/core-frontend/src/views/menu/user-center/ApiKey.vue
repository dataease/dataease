<script lang="ts" setup>
import icon_visible_outlined from '@/assets/svg/icon_visible_outlined.svg'
import icon_invisible_outlined from '@/assets/svg/icon_invisible_outlined.svg'
import icon_info_colorful from '@/assets/svg/icon_info_colorful.svg'
import icon_copy_outlined from '@/assets/svg/icon_copy_outlined.svg'
import icon_deleteTrash_outlined from '@/assets/svg/icon_delete-trash_outlined.svg'
import { ref, reactive } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import useClipboard from 'vue-clipboard3'
import request from '@/config/axios'
const { t } = useI18n()
const { toClipboard } = useClipboard()

const state = reactive({
  tableData: []
})

const copyInfo = async val => {
  try {
    await toClipboard(val)
    ElMessage.success(t('common.copy_success'))
  } catch (e) {
    ElMessage.warning(t('common.copy_unsupported'))
  }
}

const timestampFormatDate = (_, __, cellValue) => {
  if (!cellValue) {
    return '-'
  }
  return new Date(cellValue)['format']()
}

const handleDelete = row => {
  ElMessageBox.confirm(t('userCenter.delete_api_key'), {
    confirmButtonType: 'danger',
    type: 'warning',
    autofocus: false,
    confirmButtonText: t('common.delete'),
    cancelButtonText: t('dataset.cancel'),
    showClose: false
  }).then(() => {
    const url = `/apikey/delete/${row.id}`
    request.post({ url }).then(() => {
      ElMessage.success(t('common.delete_success'))
      search()
    })
  })
}

const createApiKey = () => {
  if (createLimit()) {
    const url = '/apikey/generate'
    request.post({ url }).then(() => {
      ElMessage.success(t('data_source.successfully_created'))
      search()
    })
  }
}

const switchApiEnable = row => {
  const url = '/apikey/switch'
  const data = {
    id: row.id,
    enable: row.enable
  }
  request.post({ url, data }).then(() => {
    ElMessage.success(
      row.enable ? t('userCenter.enable_success') : t('userCenter.disabled_success')
    )
  })
}

const switchShow = row => {
  row.show = !row.show
}

const createLimit = (count?: number) => {
  const realCount = count ? count : state.tableData?.length || 0
  if (realCount > 4) {
    ElMessageBox.confirm(t('userCenter.tips'), {
      confirmButtonType: 'primary',
      type: 'warning',
      confirmButtonText: t('common.roger_that'),
      cancelButtonText: t('dataset.cancel'),
      autofocus: false,
      showClose: false,
      showCancelButton: false,
      tip: t('userCenter.api_limit_5')
    })
    return false
  }
  return true
}
const viewApi = () => {
  window.open(`${getPathname()}/#/apidoc`)
}
const getPathname = () => {
  let pathname = window.location.pathname
  if (pathname) {
    if (pathname.includes('oidcbi/') || pathname.includes('casbi/')) {
      pathname = pathname.replace('oidcbi/', '')
      pathname = pathname.replace('casbi/', '')
    }
    pathname = pathname.substring(0, pathname.length - 1)
  }
  return pathname
}
const search = () => {
  const url = '/apikey/query'
  request.get({ url }).then(res => {
    const list = res.data
    if (list?.length) {
      list.forEach(item => {
        item.show = false
      })
    }
    state.tableData = list
  })
}
search()
const bindLoading = ref(false)
</script>

<template>
  <div class="api-key" v-loading="bindLoading">
    <div class="editor-form-title">
      <el-icon>
        <Icon name="icon_info_colorful"><icon_info_colorful class="svg-icon" /></Icon>
      </el-icon>
      <span class="pwd"> {{ t('userCenter.api_key_desc') }} </span>
    </div>
    <el-button type="primary" @click="createApiKey"> {{ t('userCenter.create') }} </el-button>
    <el-button secondary @click="viewApi"> {{ t('userCenter.view_api') }} </el-button>
    <div class="api-key-table">
      <el-table style="width: 100%" header-cell-class-name="header-cell" :data="state.tableData">
        <el-table-column width="240" label="Access Key">
          <template #default="scope">
            <div class="access-key ellipsis">
              {{ scope.row.accessKey }}
            </div>
            <el-tooltip effect="dark" :content="t('common.copy')" placement="top">
              <el-icon
                @click="copyInfo(scope.row.accessKey)"
                class="hover-icon hover-icon-in-table"
              >
                <Icon name="icon_copy_outlined"><icon_copy_outlined class="svg-icon" /></Icon>
              </el-icon>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column width="220" label="Secret Key">
          <template #default="scope">
            <div class="secret-key ellipsis">
              {{ scope.row.show ? scope.row.accessSecret : '********' }}
            </div>
            <el-tooltip effect="dark" :content="t('common.copy')" placement="top">
              <el-icon
                @click="copyInfo(scope.row.accessSecret)"
                class="hover-icon hover-icon-in-table"
              >
                <Icon name="icon_copy_outlined"><icon_copy_outlined class="svg-icon" /></Icon>
              </el-icon>
            </el-tooltip>
            <el-tooltip
              effect="dark"
              :content="
                scope.row.show ? t('userCenter.click_to_hind') : t('userCenter.click_to_show')
              "
              placement="top"
            >
              <el-icon @click="switchShow(scope.row)" class="hover-icon hover-icon-in-table">
                <Icon
                  ><component
                    :is="scope.row.show ? icon_visible_outlined : icon_invisible_outlined"
                  ></component
                ></Icon>
              </el-icon>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column width="80" :label="t('commons.status')">
          <template #default="scope">
            <el-switch
              size="small"
              v-model="scope.row.enable"
              @change="switchApiEnable(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column
          width="180"
          prop="createTime"
          :formatter="timestampFormatDate"
          :label="t('common.create_time')"
        >
        </el-table-column>
        <el-table-column :label="t('common.operate')">
          <template #default="scope">
            <el-button text @click="handleDelete(scope.row)">
              <template #icon>
                <Icon name="icon_delete-trash_outlined"><icon_deleteTrash_outlined /></Icon>
              </template>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<style lang="less" scoped>
.api-key {
  .editor-form-title {
    border-radius: 4px;
    background: var(--ed-color-primary-1a, rgba(51, 112, 255, 0.1));
    padding: 9px 16px;
    display: flex;
    margin: 16px 0;
    .ed-icon {
      color: var(--ed-color-primary);
      font-size: 16px;
      margin-top: 3px;
      float: left;
    }

    .pwd {
      margin-left: 8px;
      font-family: var(--de-custom_font, 'PingFang');
      font-size: 14px;
      font-style: normal;
      font-weight: 400;
      line-height: 22px;
      white-space: pre-wrap;
    }
  }

  :deep(.cell) {
    display: flex;
    align-items: center;
  }

  .access-key {
    max-width: 190px;
  }

  .secret-key {
    max-width: 166px;
  }

  .api-key-table {
    margin-top: 16px;
  }
}
</style>

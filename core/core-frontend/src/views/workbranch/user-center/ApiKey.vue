<script lang="ts" setup>
import { ref } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import useClipboard from 'vue-clipboard3'

const { t } = useI18n()
const { toClipboard } = useClipboard()

const createApiKey = () => {
  console.log('createApiKey')
}
const viewApi = () => {
  console.log('viewApi')
}

const copyInfo = async val => {
  try {
    await toClipboard(val)
    ElMessage.success(t('common.copy_success'))
  } catch (e) {
    ElMessage.warning(t('common.copy_unsupported'), e)
  }
}

const keyData = ref([
  {
    accessKey: 'OA5GPTdcQV4NjnTZT',
    secretKey: 'OA5GPTdcQV4NjnTZT',
    createTime: new Date(),
    statu: false
  }
])

const timestampFormatDate = (_, __, cellValue) => {
  if (!cellValue) {
    return '-'
  }
  return new Date(cellValue)['format']()
}

const handleDelete = row => {
  ElMessageBox.confirm('确定删除该 API key 吗？', {
    confirmButtonType: 'danger',
    type: 'warning',
    autofocus: false,
    confirmButtonText: t('common.delete'),
    cancelButtonText: t('dataset.cancel'),
    showClose: false
  }).then(() => {
    ElMessage.success('删除成功')
    ElMessage.success('创建成功')
    ElMessage.success('启用成功')
    ElMessage.success('禁用成功')
  })
}
</script>

<template>
  <div class="api-key">
    <div class="editer-form-title">
      <el-icon>
        <Icon name="icon_info_colorful"></Icon>
      </el-icon>
      <span class="pwd"
        >API Key 是您访问 DataEase API
        的密钥，具有账户的完全权限，请您务必妥善保管！不要以任何方式公开 API Key
        到外部渠道，避免被他人利用造成 安全威胁。</span
      >
    </div>
    <el-button type="primary" @click="createApiKey"> 创建 </el-button>
    <el-button secondary @click="viewApi"> 查看API </el-button>
    <div class="api-key-table">
      <el-table style="width: 100%" header-cell-class-name="header-cell" :data="keyData">
        <el-table-column width="240" label="Access Key">
          <template #default="scope">
            <div class="access-key ellipsis">
              {{ scope.row.accessKey }}
            </div>
            <el-icon @click="copyInfo(scope.row.accessKey)" class="hover-icon hover-icon-in-table">
              <Icon name="icon_copy_outlined"></Icon>
            </el-icon>
          </template>
        </el-table-column>
        <el-table-column width="240" label="Secret Key">
          <template #default="scope">
            <div class="secret-key ellipsis">
              {{ scope.row.secretKey }}
            </div>
            <el-icon @click="copyInfo(scope.row.secretKey)" class="hover-icon hover-icon-in-table">
              <Icon name="icon_copy_outlined"></Icon>
            </el-icon>
            <el-icon class="hover-icon hover-icon-in-table">
              <Icon name="icon_visible_outlined"></Icon>
            </el-icon>
            <el-icon class="hover-icon hover-icon-in-table">
              <Icon name="icon_invisible_outlined"></Icon>
            </el-icon>
          </template>
        </el-table-column>
        <el-table-column width="60" :label="t('user.state')">
          <template #default="scope">
            <el-switch size="small" v-model="scope.row.state" />
          </template>
        </el-table-column>
        <el-table-column
          width="180"
          sortable
          prop="createTime"
          :formatter="timestampFormatDate"
          :label="t('common.create_time')"
        >
        </el-table-column>
        <el-table-column :label="t('common.operate')">
          <template #default="scope">
            <el-button text @click="handleDelete(scope.row)">
              <template #icon>
                <Icon name="icon_delete-trash_outlined"></Icon>
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
  .editer-form-title {
    border-radius: 4px;
    background: #e1eaff;
    padding: 9px 16px;
    display: flex;
    margin-bottom: 16px;
    .ed-icon {
      color: #3370ff;
      font-size: 16px;
      margin-top: 3px;
      float: left;
    }

    .pwd {
      margin-left: 8px;
      font-family: PingFang SC;
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

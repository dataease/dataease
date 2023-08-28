<script lang="ts" setup>
import { ref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { ElMessageBox, ElMessage } from 'element-plus-secondary'
import UserForm from './UserForm.vue'
import UpdatePwd from './UpdatePwd.vue'
import ApiKey from './ApiKey.vue'
const { t } = useI18n()
const tabs = [
  {
    label: t('commons.personal_info'),
    value: 'info'
  },
  {
    label: t('user.change_password'),
    value: 'updatePwd'
  },
  {
    label: 'API Key',
    value: 'apiKey'
  }
]

const activeTab = ref('info')
const handleTabClick = tab => {
  activeTab.value = tab
  switch (tab) {
    case 'info':
      console.log('activeTab.value', activeTab.value)
      break
    case 'updatePwd':
      console.log('activeTab.value', activeTab.value)
      break
    case 'apiKey':
      console.log('activeTab.value', activeTab.value)
      break
    default:
      break
  }
}

const bindList = ref([
  {
    icon: 'logo_wechat-work',
    name: '企业微信',
    tip: '绑定企业微信后，您可在网页端扫码登录， 在企业微信应用内和小程序免登录， 并能实时接收小程序通知，沟通和协作将更加便捷',
    bind: false
  },
  {
    icon: 'logo_dingtalk',
    name: '钉钉',
    tip: '绑定钉钉后，',
    bind: false
  },
  {
    icon: 'logo_lark',
    name: '飞书',
    tip: '绑定飞书后，可导入飞书成员以及组织架构，成员可扫码登录',
    bind: false
  },
  {
    icon: 'logo_lark',
    name: '国际飞书',
    tip: '绑定飞书后，可导入飞书成员以及组织架构，成员可扫码登录',
    bind: true
  }
])

const userForm = ref()

const edit = () => {
  userForm.value.init()
}

const unbind = val => {
  if (val) {
    bindDialogVisible.value = true
    return
  }
  ElMessageBox.confirm('确定解除钉钉绑定吗？', {
    confirmButtonType: 'danger',
    type: 'warning',
    autofocus: false,
    confirmButtonText: t('commons.unbind'),
    cancelButtonText: t('dataset.cancel'),
    showClose: false
  }).then(() => {
    console.log(t('dataset.cancel'))
  })
}

const deleteApiKey = () => {
  ElMessageBox.confirm('确定删除该 API key 吗？', {
    confirmButtonType: 'danger',
    type: 'warning',
    autofocus: false,
    confirmButtonText: t('commons.delete'),
    cancelButtonText: t('dataset.cancel'),
    showClose: false
  }).then(() => {
    console.log(t('dataset.cancel'))
  })
}

const confirmPersonalInfo = () => {
  console.log('save')
}

const bindDialogTitle = ref('绑定钉钉')
const bindDialogVisible = ref(false)
const loginTip = ref('请使用钉钉扫描二维码登录')
</script>

<template>
  <div class="user-center flex-align-center">
    <div class="user-center-container">
      <div class="user-tabs">
        <div class="tabs-title flex-align-center">用户中心</div>
        <el-divider />
        <div
          @click="handleTabClick(tab.value)"
          :key="tab.value"
          v-for="tab in tabs"
          class="list-item_primary"
          :class="[activeTab === tab.value && 'active']"
        >
          {{ tab.label }}
        </div>
      </div>
      <div class="user-info">
        <template v-if="activeTab === 'info'">
          <div class="base-info">
            <div class="info-title flex-align-center">
              <span class="title">
                {{ t('datasource.basic_info') }}
              </span>
              <el-button @click="edit" type="primary">{{ t('common.edit') }}</el-button>
            </div>
            <el-row>
              <el-col :span="12">
                <div class="base-info-item mt12">
                  <p class="label">{{ t('user.name') }}</p>
                  <p class="value">11</p>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="base-info-item">
                  <p class="label">ID</p>
                  <p class="value">11</p>
                </div>
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="12">
                <div class="base-info-item">
                  <p class="label">{{ t('auth.user_email') }}</p>
                  <p class="value">11</p>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="base-info-item">
                  <p class="label">{{ t('commons.phone') }}</p>
                  <p class="value">11</p>
                </div>
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="12">
                <div class="base-info-item">
                  <p class="label">{{ t('commons.organization') }}</p>
                  <p class="value">11</p>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="base-info-item">
                  <p class="label">{{ t('commons.role') }}</p>
                  <p class="value">
                    <span class="role">组织管理员</span>
                    <span class="role">组织管理员</span>
                  </p>
                </div>
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="12">
                <div class="base-info-item">
                  <p class="label">{{ t('commons.status') }}</p>
                  <p class="value">11</p>
                </div>
              </el-col>
            </el-row>
          </div>

          <div class="base-info">
            <div class="info-title flex-align-center">
              <span class="title"> 绑定设置 </span>
            </div>
            <div class="bind-info" v-for="ele in bindList" :key="ele.name">
              <el-icon class="bind">
                <Icon :name="ele.icon"></Icon>
              </el-icon>
              <div class="info">
                <p class="name">{{ ele.name || '-' }}</p>
                <p class="tip">{{ ele.tip || '-' }}</p>
              </div>
              <el-button @click="unbind(ele.bind)" v-if="ele.bind" type="primary" class="delete">
                {{ t('commons.bind') }}
              </el-button>
              <el-button @click="unbind(ele.bind)" v-else class="delete" secondary>{{
                t('commons.unbind')
              }}</el-button>
            </div>
          </div>
        </template>
        <div v-else-if="activeTab === 'updatePwd'" class="base-info">
          <div class="info-title flex-align-center">
            <span class="title">
              {{ t('user.change_password') }}
            </span>
          </div>
          <UpdatePwd></UpdatePwd>
        </div>
        <div v-else-if="activeTab === 'apiKey'" class="base-info">
          <div class="info-title flex-align-center">
            <span class="title"> API Key </span>
          </div>
          <ApiKey></ApiKey>
        </div>
      </div>
    </div>
  </div>
  <el-dialog
    :title="bindDialogTitle"
    v-model="bindDialogVisible"
    width="420px"
    class="qr-code-dialog"
  >
    <div class="qr-code-img">
      <img
        src="https://img.xianjichina.com/editer/20210903/image/8978254ff0d1129c6fb0186a9e550bda.jpg"
        alt=""
      />
    </div>
    <div class="refresh-login flex-align-center">
      {{ loginTip }}
      <el-button text>
        <template #icon>
          <icon name="icon_replace_outlined"></icon>
        </template>
        {{ t('commons.refresh') }}
      </el-button>
    </div>
  </el-dialog>
  <UserForm ref="userForm"></UserForm>
</template>

<style lang="less" scoped>
.user-center {
  width: 100%;
  flex-direction: column;
  padding-top: 24px;
  .user-center-container {
    display: flex;
    font-family: PingFang SC;
    font-style: normal;
  }

  .user-tabs {
    width: 200px;
    height: 201px;
    border-radius: 4px;
    background: #fff;
    padding: 16px;
    .list-item_primary {
      padding: 9px 8px;
    }

    .ed-divider {
      margin: 4px 0;
      border-color: rgba(31, 35, 41, 0.15);
    }

    .tabs-title {
      padding-left: 8px;
      color: #8d9199;
      font-family: PingFang SC;
      font-size: 14px;
      font-style: normal;
      font-weight: 500;
      line-height: 22px;
      height: 40px;
    }
  }
  .user-info {
    margin-left: 16px;
    width: 864px;
    height: 326px;

    .base-info {
      & + .base-info {
        margin-top: 12px;
        .bind-info {
          margin-top: 16px;
          display: flex;
          align-items: center;
          width: 100%;
          padding: 16px 24px 16px 24px;
          border-radius: 4px;
          border: 1px solid #dee0e3;
          .bind {
            font-size: 48px;
            margin-right: 12px;
          }

          .info {
            font-family: PingFang SC;
            font-style: normal;
            font-weight: 400;
            width: 80%;

            .name {
              color: #1f2329;
              font-size: 16px;
              line-height: 24px;
              font-weight: 500;
              width: 100%;
            }

            .tip {
              color: #646a73;
              font-size: 14px;
              line-height: 22px;
              max-width: 600px;
              white-space: pre-wrap;
            }
          }

          .delete {
            margin-left: auto;
          }
        }
      }
      padding: 20px 24px 24px 24px;
      border-radius: 4px;
      background: #fff;
      .role {
        & + .role {
          margin-left: 4px;
        }
        display: inline-flex;
        height: 20px;
        padding: 0 6px;
        align-items: center;
        font-size: 12px;
        color: #2b5fd9;
        border-radius: 2px;
        background: rgba(51, 112, 255, 0.2);
      }
      .info-title {
        .ed-button {
          margin-left: auto;
        }

        .title {
          color: #1f2329;
          font-size: 16px;
          font-weight: 500;
          line-height: 24px;
        }
      }
    }
  }
  .base-info-item {
    margin-top: 16px;
    font-size: 14px;
    font-weight: 400;
    line-height: 22px;
    width: 100%;
    .label {
      color: #646a73;
    }
    .value {
      margin-top: 4px;
      color: #1f2329;
    }
  }
  .mr12 {
    margin-top: 12px;
  }
}
</style>
<style lang="less">
.qr-code-dialog {
  font-family: PingFang SC;
  font-style: normal;
  .ed-dialog__body {
    display: flex;
    flex-direction: column;
    align-items: center;
  }
  .qr-code-img {
    margin-top: 16px;
    display: inline-block;
    padding: 8px 12px;
    img {
      width: 184px;
      height: 184px;
    }
    border-radius: 8px;
    border: 1px solid #bbbfc4;
  }

  .refresh-login {
    margin: 0 auto;
    margin-top: 9px;
    color: #646a73;
    font-size: 14px;
    font-weight: 400;
    line-height: 22px;

    .ed-icon {
      margin-left: 4px;
    }
  }
}
</style>

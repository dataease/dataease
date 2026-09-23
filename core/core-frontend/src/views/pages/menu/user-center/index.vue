<script lang="ts" setup>
import logo_wechatWork from '@/assets/svg/logo_wechat-work.svg'
import logo_dingtalk from '@/assets/svg/logo_dingtalk.svg'
import logo_lark from '@/assets/svg/logo_lark.svg'
import icon_replace_outlined from '@/assets/svg/icon_replace_outlined.svg'
import { ref, reactive, watch, nextTick, computed } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { ElMessageBox, ElMessage } from 'element-plus-secondary'
import UserForm from '@/views/menu/user-center/UserForm.vue'
import UpdatePwd from '@/views/menu/user-center/UpdatePwd.vue'
import ApiKey from '@/views/menu/user-center/ApiKey.vue'
import router from '@/router'
import { personInfoApi, roleOptionForUserApi } from '@/api/user'
import WecomQr from '@/views/component/login/WecomQr.vue'
import DingtalkQr from '@/views/component/login/DingtalkQr.vue'
import LarkQr from '@/views/component/login/LarkQr.vue'
import LarksuiteQr from '@/views/component/login/LarksuiteQr.vue'
import { useUserStoreWithOut } from '@/store/modules/user'
import { unBindApi, bindStatusApi, queryCategoryStatus } from '@/views/component/login/bind'
import { useCache } from '@/hooks/web/useCache'
import request from '@/config/axios'
const userStore = useUserStoreWithOut()

const isAdmin = computed(() => userStore.getUid === '1')
const { t } = useI18n()

const { wsCache } = useCache()
const inPlatformClient = computed(() => !!wsCache.get('de-platform-client'))
const globalMfaStatus = ref(0)
const userMfaBound = ref(false)
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
if (inPlatformClient.value) {
  tabs.splice(1, 1)
}

const activeTab = ref('info')
const handleTabClick = tab => {
  activeTab.value = tab
  switch (tab) {
    case 'info':
      break
    case 'updatePwd':
      break
    case 'apiKey':
      break
    default:
      break
  }
}

const bindList = ref([
  {
    icon: logo_wechatWork,
    name: t('userCenter.wechat'),
    tip: t('userCenter.wechat_desc'),
    bind: false,
    enable: false
  },
  {
    icon: logo_dingtalk,
    name: t('userCenter.dingtalk'),
    tip: t('userCenter.dingtalk_desc'),
    bind: false,
    enable: false
  },
  {
    icon: logo_lark,
    name: t('userCenter.lark'),
    tip: t('userCenter.lark_desc'),
    bind: false,
    enable: false
  },
  {
    icon: logo_lark,
    name: t('userCenter.international_lark'),
    tip: t('userCenter.international_lark_desc'),
    bind: false,
    enable: false
  }
])

const userForm = ref()

const edit = () => {
  userForm.value.edit()
}

const staticForm = ref({
  id: null,
  account: null,
  name: null,
  phone: null,
  email: null,
  roleIds: [],
  enable: false,
  roleNames: [],
  mfaEnable: false,
  origin: 0
})

const roleList = ref([])
const queryForm = () => {
  personInfoApi().then(res => {
    const data = res.data
    const map = {}
    if (roleList.value?.length) {
      roleList.value.forEach(role => {
        map[role['id']] = role
      })
    }
    const roleNames = data.roleIds?.length
      ? data.roleIds.map(roleId => map[roleId]?.name).filter(item => !!item)
      : []
    staticForm.value = reactive({ ...res.data, ...{ roleNames: roleNames } })
  })
}
const optIndex = ref(0)
const bindHandler = index => {
  optIndex.value = index
  const item = bindList.value[index]
  loginTip.value = t('userCenter.pls_use') + `${item.name}` + t('userCenter.bind_use_qr')
  bindDialogTitle.value = t('commons.bind') + `${item.name}`
  bindDialogVisible.value = true
  return
}
const unbindHandler = index => {
  const mapping = [6, 5, 4, 7]
  optIndex.value = index
  const item = bindList.value[index]
  if (staticForm.value.origin === mapping[index]) {
    ElMessage.warning(t('userCenter.unbind_error', [item.name]))
    return
  }
  ElMessageBox.confirm(t('userCenter.confirm_unbind_dingtalk', [item.name]), {
    confirmButtonType: 'danger',
    type: 'warning',
    autofocus: false,
    confirmButtonText: t('commons.unbind'),
    cancelButtonText: t('dataset.cancel'),
    showClose: false,
    callback: action => {
      if (action === 'confirm') {
        unBindApi(mapping[index]).then(res => {
          if (!res.msg) {
            queryBindStatus()
            ElMessage.success(t('userCenter.unbind_success'))
          }
        })
      }
    }
  })
}
const refreshQr = () => {
  const temp = optIndex.value
  optIndex.value = -1
  nextTick(() => {
    optIndex.value = temp
  })
}
const bindDialogTitle = ref(t('commons.bind') + t('userCenter.dingtalk'))
const bindDialogVisible = ref(false)
const loginTip = ref(t('userCenter.pls_use_dingtalk'))

const queryRole = (resolve, reject) => {
  const param = {}
  if (isAdmin.value) {
    resolve(null)
  }
  roleOptionForUserApi(param)
    .then(res => {
      const roles = res.data
      roleList.value = roles
      resolve && resolve(res)
    })
    .catch(e => {
      reject && reject(e)
    })
}
const p = new Promise((resolve, reject) => {
  queryRole(resolve, reject)
})
p.then(() => {
  queryForm()
})

const saved = () => {
  queryForm()
}

const switchTab = (index: number) => {
  if (index) {
    const tabNames = ['info', 'updatePwd', 'apiKey']
    if (inPlatformClient.value && index !== 1) {
      activeTab.value = 'apiKey'
    } else {
      activeTab.value = tabNames[index - 1]
    }
  }
}

const queryBindStatus = () => {
  bindList.value.forEach(item => {
    item.bind = false
  })
  bindStatusApi().then(res => {
    const list = res.data
    if (list?.length) {
      const mappingArray = { 6: 0, 5: 1, 4: 2, 7: 3 }
      list.forEach(item => {
        if (bindList.value[mappingArray[item]]) {
          bindList.value[mappingArray[item]].bind = true
        }
      })
    }
  })
}
const queryGlobalMfaStatus = () => {
  const url = '/perSetting/mfaStatus'
  request.get({ url }).then(res => {
    globalMfaStatus.value = res.data
  })
}
const queryPlatformStatus = () => {
  queryCategoryStatus().then(res => {
    if (res['data']) {
      const list: any[] = res['data'] as any[]
      const platformMapping = { wecom: 0, dingtalk: 1, lark: 2, larksuite: 3 }
      const keys = Object.keys(platformMapping)
      let anyEnable = false
      list.forEach(item => {
        if (keys.includes(item.name)) {
          bindList.value[platformMapping[item.name]].enable = item.enable
          if (!anyEnable && item.enable) {
            anyEnable = true
          }
        }
      })
      if (anyEnable) {
        queryBindStatus()
      }
    }
  })
}
const queryMfaBound = () => {
  const url = '/user/mfabound'
  request.get({ url }).then(res => {
    userMfaBound.value = !!res.data
  })
}
const refreshMfa = (val: boolean) => {
  userMfaBound.value = val
}
queryPlatformStatus()
queryGlobalMfaStatus()
queryMfaBound()

watch(
  () => router.currentRoute.value.query.tab,
  () => {
    const tab = router.currentRoute.value.query.tab
    if (tab) {
      const index = parseInt(tab + '')
      switchTab(index)
    }
  },
  {
    immediate: true
  }
)
</script>

<template>
  <div class="user-center flex-align-center">
    <div class="user-center-container">
      <div class="user-tabs">
        <div class="tabs-title flex-align-center">
          {{ t('commons.user_center') }}
        </div>
        <el-divider />
        <div
          @click="handleTabClick(tab.value)"
          :key="tab.value"
          v-for="tab in tabs"
          style="font-size: 14px"
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
              <el-button @click="edit" type="primary">{{ t('common.edit') }} </el-button>
            </div>
            <el-row>
              <el-col :span="12">
                <div class="base-info-item mt12">
                  <p class="label">{{ t('user.name') }}</p>
                  <p class="value">{{ staticForm.name }}</p>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="base-info-item">
                  <p class="label">{{ t('user.account') }}</p>
                  <p class="value">{{ staticForm.account }}</p>
                </div>
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="12">
                <div class="base-info-item">
                  <p class="label">{{ t('commons.email') }}</p>
                  <p class="value">{{ staticForm.email }}</p>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="base-info-item">
                  <p class="label">{{ t('commons.phone') }}</p>
                  <p class="value">{{ staticForm.phone || '-' }}</p>
                </div>
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="12">
                <div class="base-info-item">
                  <p class="label">{{ t('commons.role') }}</p>
                  <p class="value" v-if="staticForm.roleNames?.length">
                    <span v-for="roleName in staticForm.roleNames" :key="roleName" class="role">{{
                      roleName
                    }}</span>
                  </p>
                  <p v-else><span>-</span></p>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="base-info-item">
                  <p class="label">{{ t('user.state') }}</p>
                  <p class="value">
                    {{ staticForm.enable ? t('userCenter.enable') : t('userCenter.invalid') }}
                  </p>
                </div>
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="12">
                <div class="base-info-item">
                  <div class="mfa-label">
                    <span class="label">{{ t('setting_mfa.user_enable') }}</span>
                    <div v-if="userMfaBound" class="mfa-bind is-active">
                      <span>{{ t('setting_mfa.bind_ready') }}</span>
                    </div>
                    <div v-else class="mfa-bind">
                      <span>{{ t('setting_mfa.bind_unready') }}</span>
                    </div>
                  </div>
                  <p class="value">
                    {{ staticForm.mfaEnable ? t('commons.enable') : t('setting_mfa.status_0') }}
                  </p>
                </div>
              </el-col>
            </el-row>
          </div>

          <div class="base-info">
            <div class="info-title flex-align-center">
              <span class="title">
                {{ t('userCenter.binding_settings') }}
              </span>
            </div>
            <div
              class="bind-info"
              :class="{ 'hide-bind-info': !ele.enable }"
              v-for="(ele, index) in bindList"
              :key="ele.name"
            >
              <el-icon class="bind">
                <Icon>
                  <component :is="ele.icon"></component>
                </Icon>
              </el-icon>
              <div class="info">
                <p class="name">{{ ele.name || '-' }}</p>
                <p class="tip">{{ ele.tip || '-' }}</p>
              </div>
              <el-button @click="unbindHandler(index)" v-if="ele.bind" class="delete" secondary>
                {{ t('commons.unbind') }}
              </el-button>
              <el-button @click="bindHandler(index)" v-else type="primary" class="delete"
                >{{ t('commons.bind') }}
              </el-button>
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
      <div class="bind-qr-container" :class="`bind-qr-${optIndex}`">
        <wecom-qr v-if="bindDialogVisible && optIndex === 0" :is-bind="true" />
        <dingtalk-qr v-if="bindDialogVisible && optIndex === 1" :is-bind="true" />
        <lark-qr v-if="bindDialogVisible && optIndex === 2" :is-bind="true" />
        <larksuite-qr v-if="bindDialogVisible && optIndex === 3" :is-bind="true" />
      </div>
    </div>
    <div class="refresh-login flex-align-center">
      {{ loginTip }}
      <el-button text @click="refreshQr">
        <template #icon>
          <icon name="icon_replace_outlined">
            <icon_replace_outlined class="svg-icon" />
          </icon>
        </template>
        {{ t('commons.refresh') }}
      </el-button>
    </div>
  </el-dialog>
  <UserForm
    ref="userForm"
    :global-mfa-status="globalMfaStatus"
    :user-mfa-bound="userMfaBound"
    @saved="saved"
    @refresh-mfa="refreshMfa"
  ></UserForm>
</template>

<style lang="less" scoped>
.user-center {
  width: 100%;
  flex-direction: column;
  padding-top: 24px;

  .user-center-container {
    display: flex;
    font-family: var(--de-custom_font, 'PingFang');
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
      font-family: var(--de-custom_font, 'PingFang');
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

        .hide-bind-info {
          display: none !important;
        }

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
            font-family: var(--de-custom_font, 'PingFang');
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
        color: var(--ed-color-primary, #3370ff);
        border-radius: 2px;
        background: var(--ed-color-primary-33, rgba(51, 112, 255, 0.2));
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
    .mfa-label {
      display: flex;
      line-height: 22px;
      align-items: center;
      .label {
        color: #646a73;
      }
      .mfa-bind {
        margin-left: 4px;
        padding: 0 4px;
        line-height: 16px;
        border-radius: 2px;
        background-color: #1f23291a;
        color: #646a73;
        span {
          font-size: 10px;
        }
      }
      .is-active {
        background-color: #34c72433;
        color: #2ca91f;
      }
    }
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
  font-family: var(--de-custom_font, 'PingFang');
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

    .bind-qr-0 {
      width: 220px;
      z-index: 1;
      height: 220px;
      overflow: hidden;
      display: flex;
      justify-content: center;

      div {
        margin-top: -100px;
      }
    }

    .bind-qr-1 {
      width: 220px;
      z-index: 1;
      height: 220px;
      overflow: hidden;

      div {
        position: absolute;
        left: -30px;
        top: -40px;
      }
    }

    .bind-qr-2,
    .bind-qr-3 {
      width: 242px;
      height: 242px;
      overflow: hidden;

      div {
        left: -13px;
        top: -10px;
        position: absolute;
      }
    }

    .bind-qr-container {
      position: relative;
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
    min-height: 20px;

    .ed-icon {
      margin-left: 4px;
    }
  }
}
</style>

<template>
  <div class="authentication-content" v-loading="loading">
    <div class="auth-card-container">
      <div class="authentication-card" v-for="item in showInfos" :key="item.name">
        <div class="inner-card">
          <div class="inner-card-info">
            <span class="card-info-left">
              <span class="card-span">{{
                item.name === 'oauth2' ? 'OAuth2' : item.name.toLocaleUpperCase()
              }}</span>
              <span
                class="card-status"
                :class="{
                  'card-hidden-status': !item.id,
                  'valid-status': item.id && item.valid
                }"
                >{{ item.valid ? t('datasource.valid') : t('datasource.invalid') }}</span
              >
            </span>
            <el-tooltip
              class="box-item"
              effect="dark"
              :content="t('system.be_turned_on')"
              placement="top"
              v-if="!item.valid"
            >
              <el-switch
                :disabled="!item.valid"
                v-model="item.enable"
                @change="switchEnable(item)"
              />
            </el-tooltip>
            <el-switch v-else v-model="item.enable" @change="switchEnable(item)" />
          </div>
          <div class="inner-card-btn">
            <el-button secondary @click="editInfo(item)">{{ t('commons.edit') }}</el-button>
            <el-button class="card-validate-btn" secondary @click="validate(item.id)">{{
              t('commons.test_connect')
            }}</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
  <cas-editor ref="casEdit" @saved="init" />
  <oidc-editor ref="oidcEdit" @saved="init" />
  <ldap-editor ref="ldapEdit" @saved="init" />
  <oauth2-editor ref="oauth2Edit" @saved="init" />
  <saml2-editor ref="saml2Edit" @saved="init" />
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import request from '@/config/axios'
import CasEditor from '@/views/menu/setting/authentication/CasEditor.vue'
import LdapEditor from '@/views/menu/setting/authentication/LdapEditor.vue'
import OidcEditor from '@/views/menu/setting/authentication/OidcEditor.vue'
import Oauth2Editor from '@/views/menu/setting/authentication/Oauth2Editor.vue'
import Saml2Editor from '@/views/menu/setting/authentication/SAML2Editor.vue'
import { ElMessage } from 'element-plus-secondary'
const { t } = useI18n()
const loading = ref(false)
interface CardInfo {
  id: string
  name: string
  valid: boolean
  enable: boolean
}
const casEdit = ref()
const oidcEdit = ref()
const oauth2Edit = ref()
const saml2Edit = ref()
const ldapEdit = ref()
const infos = ref([] as CardInfo[])

const showInfos = ref([] as CardInfo[])

const init = (needLoading: boolean) => {
  if (needLoading) {
    loading.value = true
  }
  const url = '/setting/authentication/grid'
  request
    .get({ url })
    .then(res => {
      if (res?.data) {
        infos.value = [...(res.data as CardInfo[])]
        showInfos.value = [...infos.value]
      }
      loading.value = false
    })
    .catch(e => {
      console.error(e)
      loading.value = false
    })
}
const switchEnable = (item: CardInfo) => {
  const url = '/setting/authentication/switchEnable'
  const data = { id: item.id, enable: item.enable }
  loading.value = true
  request
    .post({ url, data })
    .then(() => {
      init(false)
    })
    .catch(e => {
      console.error(e)
    })
    .finally(() => {
      loading.value = false
    })
}
const editInfo = (item: CardInfo) => {
  if (item.name === 'oidc') {
    oidcEdit.value?.edit()
  } else if (item.name === 'cas') {
    casEdit.value?.edit()
  } else if (item.name === 'ldap') {
    ldapEdit.value?.edit()
  } else if (item.name === 'oauth2') {
    oauth2Edit.value?.edit()
  } else if (item.name === 'saml2') {
    saml2Edit.value?.edit()
  }
}
const validate = id => {
  if (!id) {
    ElMessage.error(
      `${t('commons.test_connect') + t('report.last_status_fail')}: ${t(
        'system.platform_information_first'
      )}`
    )
    return
  }
  loading.value = true
  request
    .post({ url: `/setting/authentication/validateId/${id}`, data: {} })
    .then(res => {
      if (res?.data === 'true') {
        ElMessage.success(t('commons.test_connect') + t('report.last_status_success'))
      } else {
        ElMessage.error(`${t('commons.test_connect') + t('report.last_status_fail')}: ${res.data}`)
      }
      init(false)
    })
    .finally(() => {
      loading.value = false
    })
}
init(true)
</script>

<style lang="less" scoped>
.authentication-content {
  padding: 8px 0 24px 0;
  width: 100%;
  height: 100%;
}
.auth-card-container {
  height: initial;
  display: flex;
  flex-wrap: wrap;
  gap: 16px;

  .authentication-card {
    width: calc(25% - 12px);
    min-width: 230px;
    height: 116px;
    padding: 24px;
    border-radius: 12px;
    background-color: #fff;
    .inner-card {
      position: relative;
      .inner-card-info {
        height: 24px;
        display: flex;
        align-items: center;
        .card-info-left {
          width: calc(100% - 40px);
          .card-span {
            font-family: var(--de-custom_font, 'PingFang');
            font-size: 16px;
            font-weight: 500;
            line-height: 24px;
            text-align: left;
          }
          .card-hidden-status {
            display: none;
          }
          .valid-status {
            background-color: #34c72433 !important;
            color: #2ca91f !important;
          }
          .card-status {
            margin-left: 8px;
            padding: 1px 6px;
            border-radius: 2px;
            background-color: #f54a4533;
            color: #d03f3b;
            line-height: 22px;
            font-size: 14px;
            font-weight: 400;
          }
        }
        .ed-switch {
          height: 22px;
        }
      }
      .inner-card-btn {
        float: left;
        height: 28px;
        margin-top: 16px;
        button {
          height: 28px;
          min-width: 46px !important;
          padding: 4px 11px !important;
          :deep(span) {
            height: 20px !important;
            line-height: 20px !important;
            font-size: 12px !important;
            display: inline-block;
            vertical-align: middle;
          }
        }
        .card-validate-btn {
          margin-left: 8px;
        }
      }
    }
  }
}
</style>

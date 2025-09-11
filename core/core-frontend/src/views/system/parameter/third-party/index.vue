<script lang="ts" setup>
import logo_dingtalk from '@/assets/svg/icon_sqlbot_colorful.svg'
import { ref, reactive } from 'vue'
import InfoTemplate from '@/views/system/common/InfoTemplate.vue'
import thirdEdit from './ThirdEdit.vue'
import request from '@/config/axios'
import { useI18n } from '@/hooks/web/useI18n'
import { ElMessage } from 'element-plus-secondary'
const { t } = useI18n()
const editor = ref()
const existInfo = ref(true)
const copyList = []
const settingList = reactive([
  {
    pkey: t('common.sqlbot_server_url'),
    pval: '',
    type: 'text',
    sort: 2
  },
  {
    pkey: t('common.application_id'),
    pval: '',
    type: 'text',
    sort: 3
  }
])
const info = ref({
  id: '',
  domain: '',
  enabled: false,
  valid: false
})
const mappingArray = ['domain', 'id']
const search = () => {
  const url = '/sysParameter/sqlbot'
  request.get({ url }).then(res => {
    if (res.data) {
      info.value = res.data
      for (let index = 0; index < settingList.length; index++) {
        const element = settingList[index]
        const key = mappingArray[index]
        element['pval'] = res.data[key] || '-'
      }
    }
  })
}

const switchEnableApi = () => {
  const param = { ...info.value }
  request.post({ url: '/sysParameter/sqlbot', data: param })
}
const edit = () => {
  editor?.value.edit(info.value)
}
const validate = () => {
  if (info.value?.id && info.value?.domain) {
    validateHandler()
  }
}
const save = () => {
  const param = { ...info.value }
  request.post({ url: '/sysParameter/sqlbot', data: param })
}

const validateHandler = () => {
  let url = `${
    info.value.domain.endsWith('/') ? info.value.domain : info.value.domain + '/'
  }api/v1/system/assistant/info/${info.value.id}`
  fetch(url)
    .then(response => {
      if (!response.ok) {
        throw new Error('Network response was not ok')
      }
      return response.json()
    })
    .then(() => {
      info.value.valid = true
      ElMessage.success(t('datasource.validate_success'))
    })
    .catch(() => {
      ElMessage.error(t('data_source.verification_failed'))
      info.value.enabled = false
      info.value.valid = false
    })
    .finally(() => {
      save()
    })
}
search()
</script>

<template>
  <div v-if="info.id" class="container-sys-platform">
    <div class="platform-head-container just-head">
      <div class="platform-setting-head">
        <div class="platform-setting-head-left">
          <div class="lead-left-icon">
            <el-icon size="24px">
              <Icon name="logo_dingtalk"><logo_dingtalk class="svg-icon" /></Icon>
            </el-icon>
            <span>SQLBot</span>
          </div>
          <div class="lead-left-status" :class="{ invalid: !info.valid }">
            <span>{{ info.valid ? t('datasource.valid') : t('datasource.invalid') }}</span>
          </div>
        </div>
        <div v-if="existInfo" class="platform-setting-head-right">
          <el-switch class="status-switch" v-model="info.enabled" @change="switchEnableApi" />
        </div>
        <div v-else class="platform-setting-head-right-btn">
          <el-button type="primary" @click="edit">{{ t('system.access') }}</el-button>
        </div>
      </div>
    </div>
    <InfoTemplate
      v-if="existInfo"
      class="platform-setting-main"
      :copy-list="copyList"
      setting-key="dingtalk"
      setting-title=""
      :hide-head="true"
      :setting-data="settingList"
      @edit="edit"
    />
    <div v-if="existInfo" class="platform-foot-container">
      <el-button type="primary" @click="edit">
        {{ t('commons.edit') }}
      </el-button>
      <el-button secondary :disabled="!info.id || !info.domain" @click="validate">{{
        t('commons.validate')
      }}</el-button>
    </div>
  </div>
  <div v-else class="no-params">
    <el-icon size="24px">
      <Icon name="logo_dingtalk"><logo_dingtalk class="svg-icon" /></Icon>
    </el-icon>
    <span style="margin-left: 8px">SQLBot</span>
    <el-button type="primary" @click="edit">
      {{ t('common.embed') }}
    </el-button>
  </div>
  <third-edit ref="editor" @saved="search" />
</template>

<style lang="less" scoped>
.no-params {
  height: 72px;
  border-radius: 4px;
  display: flex;
  padding: 0 24px;
  align-items: center;
  .ed-button {
    margin-left: auto;
  }
}
.container-sys-platform {
  padding: 24px;
  overflow: hidden;
  border-radius: 4px;
  background: var(--ContentBG, #ffffff);
}
.platform-head-container {
  height: 41px;
  border-bottom: 1px solid #1f232926;
}
.just-head {
  height: auto !important;
  border: none !important;
}
.platform-setting-head {
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;

  .platform-setting-head-left {
    display: flex;
    .lead-left-icon {
      display: flex;
      line-height: 24px;
      align-items: center;
      i {
        width: 24px;
        height: 24px;
        font-size: 20px;
      }
      span {
        margin-left: 4px;
        font-family: var(--de-custom_font, 'PingFang');
        font-size: 16px;
        font-style: normal;
        font-weight: 500;
        line-height: 24px;
      }
    }
    .lead-left-status {
      margin-left: 4px;
      width: 40px;
      height: 24px;
      background: #34c72433;
      padding: 0 6px;
      font-size: 14px;
      border-radius: 2px;
      overflow: hidden;
      span {
        line-height: 24px;
        color: #2ca91f;
      }
    }
    .invalid {
      background: #f54a4533 !important;
      span {
        color: #d03f3b !important;
      }
    }
  }
  .platform-setting-head-right-btn {
    height: 32px;
    line-height: 32px;
  }
  .platform-setting-head-right {
    height: 22px;
    line-height: 24px;
    display: flex;
    span {
      margin-right: 8px;
      font-size: 14px;
      height: 22px;
      line-height: 22px;
    }
    .status-switch {
      line-height: 22px !important;
      height: 22px !important;
    }
  }
}

.platform-setting-main {
  display: inline-block;
  width: 100%;
  padding: 16px 0 0 0 !important;
  ::v-deep(.info-template-content) {
    display: contents !important;
  }
}
.platform-foot-container {
  height: 32px;
  margin-top: -7px;
}
</style>

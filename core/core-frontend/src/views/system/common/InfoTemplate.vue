<template>
  <div class="info-template-container">
    <div class="info-template-header">
      <div class="info-template-title">
        <span>基础设置</span>
      </div>
      <div>
        <el-button type="primary" @click="edit">{{ t('commons.edit') }}</el-button>
      </div>
    </div>
    <div class="info-template-content">
      <div class="info-content-item" v-for="item in settingList" :key="item.pkey">
        <div class="info-item-label">
          <span>{{ item.pkey }}</span>
          <el-tooltip
            v-if="tooltipItem[item.pkey]"
            effect="dark"
            :content="tooltipItem[item.pkey]"
            placement="top"
          >
            <el-icon class="info-tips"><Icon name="dv-info"></Icon></el-icon>
          </el-tooltip>
        </div>
        <div class="info-item-content">
          <div class="info-item-pwd" v-if="item.type === 'pwd'">
            <span>{{ pwdItem[item.pkey]['hidden'] ? '********' : item.pval }}</span>
            <el-tooltip effect="dark" content="新页面预览" placement="top">
              <el-icon
                class="hover-icon hover-icon-in-table switch-pwd-icon"
                @click="switchPwd(item.pkey)"
              >
                <Icon :name="pwdItem[item.pkey]['hidden'] ? 'eye' : 'eye-open'"></Icon>
              </el-icon>
            </el-tooltip>
          </div>
          <span v-else>{{ item.pval }}</span>
        </div>
      </div>
    </div>
  </div>
</template>
<script lang="ts" setup>
import { ref, defineProps, PropType } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { SettingRecord, ToolTipRecord } from './SettingTemplate'
const { t } = useI18n()
const props = defineProps({
  settingKey: {
    type: String,
    default: 'basic'
  },
  labelTooltips: {
    type: Array as PropType<ToolTipRecord[]>,
    default: () => []
  }
})

const settingList = ref([] as SettingRecord[])

const loadBasic = () => {
  settingList.value.push({
    pkey: '请求超时时间',
    pval: '100',
    type: 'text',
    sort: 1
  })
  settingList.value.push({
    pkey: '数据源检测时间间隔',
    pval: '100',
    type: 'text',
    sort: 2
  })
  settingList.value.push({
    pkey: '默认登录方式',
    pval: '普通登录',
    type: 'text',
    sort: 3
  })
  settingList.value.push({
    pkey: '默认密码',
    pval: 'DataEase@123456',
    type: 'pwd',
    sort: 4
  })
}

const loadEmail = () => {
  settingList.value.push({
    pkey: 'SMTP主机',
    pval: 'smtp.exmail.qq.com',
    type: 'text',
    sort: 1
  })
  settingList.value.push({
    pkey: 'SMTP端口',
    pval: '465',
    type: 'text',
    sort: 2
  })
  settingList.value.push({
    pkey: 'SMTP账户',
    pval: 'test@fit2cloud.com',
    type: 'text',
    sort: 3
  })
  settingList.value.push({
    pkey: 'SMTP密码',
    pval: 'DataEase@123456',
    type: 'pwd',
    sort: 4
  })
  settingList.value.push({
    pkey: '测试收件人',
    pval: 'yawen.chen@fit2cloud.com',
    type: 'pwd',
    sort: 5
  })
  settingList.value.push({
    pkey: 'SSL',
    pval: '开启',
    type: 'text',
    sort: 6
  })
  settingList.value.push({
    pkey: 'TSL',
    pval: '未开启',
    type: 'text',
    sort: 7
  })
}

const init = () => {
  if (props.settingKey === 'basic') {
    loadBasic()
  }
  if (props.settingKey === 'email') {
    loadEmail()
  }
}
const pwdItem = ref({})

const formatPwd = () => {
  settingList.value.forEach(setting => {
    if (setting.type === 'pwd') {
      pwdItem.value[setting.pkey] = { hidden: true }
    }
  })
}

const tooltipItem = ref({})
const formatLabel = () => {
  props.labelTooltips?.length &&
    props.labelTooltips.forEach(tooltip => {
      tooltipItem.value[tooltip.key] = tooltip.val
    })
}

const switchPwd = (pkey: string) => {
  pwdItem.value[pkey]['hidden'] = !pwdItem.value[pkey]['hidden']
}

const emits = defineEmits(['edit'])
const edit = () => {
  emits('edit')
}
init()
formatPwd()
formatLabel()
</script>

<style lang="less" scope>
.info-template-container {
  padding: 24px;
  .info-template-header {
    display: flex;
    justify-content: space-between;
    .info-template-title {
      height: 24px;
      line-height: 23px;
      font-size: 16px;
      font-weight: 500;
      color: #1f2329;
    }
  }
  .info-template-content {
    width: 100%;
    margin: 8px 0;
    .info-content-item {
      width: 50%;
      height: 48px;
      float: left;
      margin-bottom: 16px;
      .info-item-label {
        height: 22px;
        line-height: 22px;
        display: flex;
        align-items: center;
        span {
          font-size: 14px;
          color: #646a73;
          font-weight: 400;
        }
        i {
          margin-left: 2px;
        }
      }
      .info-item-content {
        line-height: 22px;
        height: 22px;
        span {
          font-size: 14px;
          color: #1f2329;
          font-weight: 400;
        }

        .info-item-pwd {
          height: 22px;
          line-height: 22px;

          display: flex;
          align-items: center;
          i {
            margin-left: 2px;
          }
        }
      }
    }
  }
}
</style>

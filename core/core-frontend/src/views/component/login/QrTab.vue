<template>
  <el-tabs v-model="activeName" @tab-click="handleClick">
    <el-tab-pane v-if="props.wecom" :label="t('threshold.wecom')" name="wecom"></el-tab-pane>
    <el-tab-pane v-if="props.dingtalk" :label="t('threshold.dingtalk')" name="dingtalk"></el-tab-pane>
    <el-tab-pane v-if="props.lark" :label="t('threshold.lark')" name="lark"></el-tab-pane>
    <el-tab-pane v-if="props.larksuite" :label="t('system.international_feishu')" name="larksuite"></el-tab-pane>
  </el-tabs>
  <div class="login-qrcode" v-if="activeName === 'wecom'">
    <div class="title">
      <el-icon>
      <Icon name="logo_wechat-work"><logo_wechatWork class="svg-icon" /></Icon>
      </el-icon>
      {{ t('threshold.wecom') }}
    </div>
    <div class="qrcode">
      <wecom-qr v-if="activeName === 'wecom'"/>
    </div>
  </div>
  <div class="login-qrcode" v-if="activeName === 'dingtalk'">
    <div class="title">
      <el-icon>
      <Icon name="logo_dingtalk"><logo_dingtalk class="svg-icon" /></Icon>
      </el-icon>
      {{ t('threshold.dingtalk') }}
    </div>
    <div class="qrcode">
      <dingtalk-qr v-if="activeName === 'dingtalk'"/>
    </div>
  </div>
  <div class="login-qrcode" v-if="activeName === 'lark'">
    <div class="title">
      <el-icon>
      <Icon name="logo_lark"><logo_lark class="svg-icon" /></Icon>
      </el-icon>
      {{ t('threshold.lark') }}
    </div>
    <div class="qrcode">
      <lark-qr v-if="activeName === 'lark'"/>
    </div>
  </div>
  <div class="login-qrcode" v-if="activeName === 'larksuite'">
    <div class="title">
      <el-icon>
      <Icon name="logo_lark"><logo_lark class="svg-icon" /></Icon>
      </el-icon>
      {{ t('system.international_feishu') }}
    </div>
    <div class="qrcode">
      <larksuite-qr v-if="activeName === 'larksuite'"/>
    </div>
  </div>
</template>

<script lang="ts" setup>
import logo_wechatWork from '@/assets/svg/logo_wechat-work.svg'
import logo_dingtalk from '@/assets/svg/logo_dingtalk.svg'
import logo_lark from '@/assets/svg/logo_lark.svg'
import { ref } from 'vue'
import LarkQr from "./LarkQr.vue"
import LarksuiteQr from "./LarksuiteQr.vue"
import DingtalkQr from "./DingtalkQr.vue"
import WecomQr from "./WecomQr.vue"
import { propTypes } from '@/utils/propTypes'
import { useI18n } from '@/hooks/web/useI18n'

const { t } = useI18n()
const activeName = ref('')
const props = defineProps({
  wecom: propTypes.bool.def(false),
  lark: propTypes.bool.def(false),
  dingtalk: propTypes.bool.def(false),
  larksuite: propTypes.bool.def(false)
})
const initActive = () => {
  const qrArray = ['wecom', 'dingtalk', 'lark', 'larksuite']
  for(let i = 0; i < qrArray.length; i++) {
    const key = qrArray[i]
    if (props[key]) {
      activeName.value = key
      break
    }
  }
}
const handleClick = () => {
}
initActive()
</script>

<style lang="less" scoped>
.login-qrcode {
  height: 340px;
  display: flex;
  align-items: center;
  flex-direction: column;
  .qrcode {
    max-width: 286px;
    display: flex;
    overflow: hidden;
    justify-content: center;
    align-items: center;
    border-radius: 8px;
    background: #fff;
  }

  .title {
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 24px 0;
    font-family: var(--de-custom_font, 'PingFang');
    font-size: 18px;
    font-style: normal;
    font-weight: 500;
    line-height: 26px;
    height: 26px;
    .ed-icon {
      margin-right: 8px;
      font-size: 24px;
    }
  }
}
</style>

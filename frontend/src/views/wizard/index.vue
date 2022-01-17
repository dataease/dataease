<template>
  <el-row class="main_container">
    <el-row class="head">
      <span class="hint_head">{{ $t('wizard.welcome_title') }}</span> <br>
      <span class="hint_content">{{ $t('wizard.welcome_hint') }}</span>
    </el-row>
    <el-row class="card_container">
      <card v-for="(cardDetail,index) in cardList" :key="index" :head-info="cardDetail.headInfo">
        <component :is="cardDetail.component" />
      </card>
    </el-row>
  </el-row>
</template>

<script>

import Card from '@/views/wizard/card'
import DemoVideo from '@/views/wizard/details/DemoVideo'
import OnlineDocument from '@/views/wizard/details/OnlineDocument'
import LatestDevelopments from '@/views/wizard/details/LatestDevelopments'
import TeachingVideo from '@/views/wizard/details/TeachingVideo'
import EnterpriseEdition from '@/views/wizard/details/EnterpriseEdition'
import ContactUs from '@/views/wizard/details/ContactUs'
export default {
  name: 'Wizard',
  components: { Card, DemoVideo, OnlineDocument, LatestDevelopments, TeachingVideo, EnterpriseEdition, ContactUs },
  data() {
    return {
      cardList: [
        {
          headInfo: this.$t('wizard.demo_video'),
          component: 'DemoVideo'
        },
        {
          headInfo: this.$t('wizard.online_document'),
          component: 'OnlineDocument'
        },
        {
          headInfo: this.$t('wizard.latest_developments'),
          component: 'LatestDevelopments'
        },
        {
          headInfo: this.$t('wizard.teaching_video'),
          component: 'TeachingVideo'
        },
        {
          headInfo: this.$t('wizard.enterprise_edition'),
          component: 'EnterpriseEdition'
        },
        {
          headInfo: this.$t('wizard.contact_us'),
          component: 'ContactUs'
        }
      ]
    }
  }
}

</script>

<style lang="scss" scoped>
  .main_container {
  }
  .head {
    background-color: var(--MenuActiveBG, #409EFF);
    text-align: center;
    color: white;
    padding: 10px;
    margin-top: 25px;
  }
  .hint_head {
    line-height: 40px;
    font-weight: bold;
    font-size: 25px;
  }
  .hint_content {
    line-height: 40px;
    font-size: 15px;
  }

  .card_container {

  }
</style>

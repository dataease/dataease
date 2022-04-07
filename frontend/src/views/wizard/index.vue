<template>
  <el-row class="main_container">
    <el-row class="head">
      <span class="hint_head">{{ $t('wizard.welcome_title') }}</span> <br>
      <span class="hint_content">{{ $t('wizard.welcome_hint') }}</span>
    </el-row>
    <el-row class="card_container">
      <info-card v-for="(cardDetail,index) in cardList" :key="index">
        <component :is="cardDetail.component" :img-index="index" :details="cardDetail" />
      </info-card>
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
import InfoCard from '@/views/wizard/infoCard'
import CardDetail from '@/views/wizard/details/CardDetail'
import { blogLastActive } from '@/api/wizard/wizard'

export default {
  name: 'Wizard',
  components: { InfoCard, Card, DemoVideo, OnlineDocument, LatestDevelopments, TeachingVideo, EnterpriseEdition, ContactUs, CardDetail },
  data() {
    return {
      cardList: [
        {
          head: this.$t('wizard.quick_start'),
          content: this.$t('wizard.demo_video_hint'),
          bottom: '',
          href: 'https://www.bilibili.com/video/BV1UB4y1K7jA',
          component: 'CardDetail'
        },
        {
          head: this.$t('wizard.online_document'),
          content: this.$t('wizard.online_document_hint'),
          bottom: '',
          href: 'https://dataease.io/docs/index.html',
          component: 'CardDetail'
        },
        {
          head: this.$t('wizard.latest_developments'),
          content: '',
          bottom: '',
          href: 'https://blog.fit2cloud.com/?cat=321',
          component: 'CardDetail'
        },
        {
          head: this.$t('wizard.teaching_video'),
          content: '<a href="https://www.bilibili.com/video/BV1pb4y1E7Zg?spm_id_from=333.999.0.0" target="_blank">【DataEase教学视频】视图钻取</a><br><a href="https://www.bilibili.com/video/BV1rm4y1f7a7?spm_id_from=333.999.0.0" target="_blank">【DataEase教学视频】视图组件联动</a><br><a href="https://www.bilibili.com/video/BV1Xq4y187H9?spm_id_from=333.999.0.0" target="_blank">【DataEase教学视频】移动端布局设置</a>',
          bottom: '',
          href: 'https://space.bilibili.com/510493147/channel/collectiondetail?sid=262774',
          component: 'CardDetail'
        },
        {
          head: this.$t('wizard.enterprise_edition'),
          content: this.$t('wizard.enterprise_edition_hint1') + '<br>' + this.$t('wizard.enterprise_edition_hint2') + '<br>' + this.$t('wizard.enterprise_edition_hint3'),
          bottom: '',
          href: 'https://jinshuju.net/f/TK5TTd',
          component: 'CardDetail'
        },
        {
          head: this.$t('wizard.contact_us'),
          content: this.$t('wizard.email') + 'dataease@fit2cloud.com<br>' + this.$t('wizard.tel') + '400-052-0755<br>' + this.$t('wizard.web') + '<a target="_blank" href="https://www.dataease.io">www.dataease.io</a>',
          bottom: '',
          href: 'https://www.dataease.io',
          component: 'CardDetail'
        }
      ]
    }
  },
  created() {
    this.init()
  },
  methods: {
    init() {
      blogLastActive().then(res => {
        const blogsInfo = res.data[0]
        this.cardList[2].content = blogsInfo.title
        this.cardList[2].bottom = blogsInfo.time
      })
    }
  }
}

</script>

<style lang="scss" scoped>
  .main_container {
  }
  .head {
    text-align: center;
    color: white;
    padding: 10px;
    margin-top: 35px;
    background-size: 100% 100% !important;
    background-image: url('../../assets/banner.png');
  }
  .hint_head {
    line-height: 50px;
    font-weight: bold;
    font-size: 25px;
  }
  .hint_content {
    line-height: 50px;
    font-size: 15px;
  }

  .card_container {
   vertical-align: middle;

  }
</style>

<template>
  <div
    v-if="loading"
    v-loading="loading"
    style="position:absolute;top:55px;width: 100%;height: calc(100% - 55px);"
    class="main_outer"
  />
  <div v-else-if="homeLink">
    <iframe
      id="mobsf"
      :src="homeLink"
      frameborder="0"
      style="position:absolute;top:55px;width: 100%;height: calc(100% - 55px);"
    />
  </div>
  <el-row
    v-else
    class="main_container_outer"
  >
    <el-row class="main_container">
      <el-row class="main_content">
        <el-row class="content_top_banner">
          <div class="top_banner_content">
            <div class="hint_head">{{ $t('wizard.welcome_title') }}</div>
            <div class="hint_content">{{ $t('wizard.welcome_hint') }}</div>
          </div>
          <svg-icon
            class="top_banner_img"
            icon-class="wizard_main_bg_inner"
          />
        </el-row>
        <el-row class="top_banner_card">
          <wizard-card :details="cardList[0]"/>
          <wizard-card
            :details="cardList[1]"
            style="margin:0 24px 0 24px"
          />
          <wizard-card-enterprise :details="cardList[2]"/>
        </el-row>
        <el-row class="content_middle">
          <div class="content_middle_left">
            <el-row>
              <span class="content_middle_title">{{ $t('wizard.teaching_video') }}</span>
              <div class="content_middle_more"><a
                target="_blank"
                href="https://space.bilibili.com/510493147/channel/collectiondetail?sid=262774"
              >{{ $t('wizard.more') }}<i class="el-icon-arrow-right"/></a></div>
            </el-row>
            <el-row style="margin-top: 12px">
              <video-card :details="videoList[0]"/>
              <video-card
                style="margin:0 12px 0 12px"
                :details="videoList[1]"
              />
              <video-card :details="videoList[2]"/>
            </el-row>
          </div>
          <div class="content_middle_right">
            <el-row>
              <span class="content_middle_title">{{ $t('wizard.latest_developments') }}</span>
              <div class="content_middle_more"><a
                target="_blank"
                href="https://blog.fit2cloud.com/categories/dataease"
              >{{ $t('wizard.more') }}<i class="el-icon-arrow-right"/></a></div>
            </el-row>
            <el-row>
              <ul class="ul-custom">
                <li
                  v-for="blogItem in blogsInfo"
                  :key="blogItem.title"
                  class="li-custom"
                ><a
                  target="_blank"
                  class="li-a"
                  :href="blogItem.href"
                  :title="blogItem.title"
                >{{ blogItem.title }}</a></li>
              </ul>
            </el-row>
          </div>
        </el-row>

        <el-row class="content_bottom">
          <div class="content_bottom_contact">
            <el-row class="contact_title">
              <span>{{ $t('wizard.contact_us') }}</span>
            </el-row>
            <el-row class="contact_content">
              <span>{{ $t('wizard.email') }}dataease@fit2cloud.com</span>
            </el-row>
            <el-row class="contact_content">
              <span>{{ $t('wizard.tel') }}400-052-0755</span>
            </el-row>
            <el-row class="contact_content">
              {{ $t('wizard.web') }}<a
              style="text-decoration:underline;"
              target="_blank"
              href="https://www.dataease.io"
            >www.dataease.io</a>
            </el-row>
            <el-row class="contact_content">
              {{ $t('wizard.bbs') }}<a
              style="text-decoration:underline;"
              target="_blank"
              href="https://bbs.fit2cloud.com/c/de"
            >https://bbs.fit2cloud.com/c/de</a>
            </el-row>
          </div>

          <div class="content_bottom_qr_code">
            <div class="contact_wechat_train">
              <div class="contact_title_qr">{{ $t('wizard.f2c_train') }}</div>
              <img
                class="contact_wechat_train_img"
                src="@/assets/wizard_wechat-train.png"
              >
            </div>
            <div class="contact_wechat_official">
              <div class="contact_title_qr">{{ $t('wizard.weChat_official_account') }}</div>
              <img
                class="contact_wechat_official_img"
                src="@/assets/wizard_wechat-official.jpeg"
              >
            </div>
            <div class="contact_wechat_group">
              <div class="contact_title_qr">{{ $t('wizard.technical_group') }}</div>
              <img
                class="contact_wechat_group_img"
                src="../../assets/wizard_wechat-group.png"
              >
            </div>
          </div>
        </el-row>

      </el-row>
    </el-row>

  </el-row>
</template>

<script>

import { blogLastActive } from '@/api/wizard/wizard'
import WizardCard from '@/views/wizard/WizardCard'
import VideoCard from '@/views/wizard/VideoCard'
import WizardCardEnterprise from '@/views/wizard/WizardCardEnterprise'

export default {
  name: 'Wizard',
  components: {
    WizardCardEnterprise,
    VideoCard,
    WizardCard
  },
  data() {
    return {
      blogsInfo: [],
      cardList: [
        {
          head: this.$t('wizard.quick_start'),
          content: this.$t('wizard.demo_video_hint'),
          img: 'wizard_quick_start',
          bgColor: '#E7F2FF',
          href: 'https://www.bilibili.com/video/BV1qG4y1F7uc/'
        },
        {
          head: this.$t('wizard.online_document'),
          content: this.$t('wizard.online_document_hint'),
          img: 'wizard_help',
          bgColor: '#F3F2FF',
          href: 'https://dataease.io/docs/index.html'
        },
        {
          head: this.$t('wizard.enterprise_edition'),
          content: this.$t('wizard.enterprise_edition_hint1') + '<br>' + this.$t('wizard.enterprise_edition_hint2') + '<br>' + this.$t('wizard.enterprise_edition_hint3'),
          img: 'wizard_enterprise',
          bgColor: '#FFFAF0',
          href: 'https://jinshuju.net/f/TK5TTd'
        }
      ],
      videoList: [
        {
          content: '1.1 连接数据库并添加数据集',
          img: 'wizard_video1.png',
          href: 'https://www.bilibili.com/video/BV13V4y1P7FB/?spm_id_from=333.999.0.0'
        },
        {
          content: '1.2 Excel 数据集和 API 数据集',
          img: 'wizard_video2.png',
          href: 'https://www.bilibili.com/video/BV16P4y1Q7zT/?spm_id_from=333.999.0.0'
        },
        {
          content: '1.3 数据集整合',
          img: 'wizard_video3.png',
          href: 'https://www.bilibili.com/video/BV1fR4y1k7Dt/?spm_id_from=333.999.0.0'
        }
      ],
      loading: true

    }
  },
  computed: {
    homeLink() {
      if (this.$store.getters.uiInfo && this.$store.getters.uiInfo['ui.homeLink'] && this.$store.getters.uiInfo['ui.homeLink'].paramValue) {
        return this.$store.getters.uiInfo['ui.homeLink'].paramValue
      }
      return null
    }
  },
  mounted() {
    setTimeout(() => {
      this.loading = false
    }, 1000)
  },

  created() {
    this.init()
  },
  methods: {
    init() {
      blogLastActive().then(res => {
        this.blogsInfo = res.data
      })
    }
  }
}

</script>

<style lang="scss" scoped>
.main_outer {
  background-color: var(--MainBG, #f5f6f7)
}

.main_container {
  min-width: 1250px;
  padding: 0 24px 0 24px;
  overflow: auto;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  background-size: 100% 444px !important;
  background: url('../../assets/wizard_main_bg.png') no-repeat;
}

.main_content {
  width: 1200px;
}

.content_top_banner {
  color: var(--ContentBG, #FFFFFF);
  position: relative;
  width: 100%;
  height: 230px;
}

.top_banner_content {
  position: absolute;
  top: 62px;
  height: 230px;
}

.top_banner_img {
  position: absolute;
  width: 520px;
  height: 230px;
  top: 0;
  right: 50px;
}

.top_banner_card {
  position: relative;
  width: 100%;
  height: 214px;
}

.hint_head {
  line-height: 48px;
  font-weight: 600;
  font-size: 48px;
}

.hint_content {
  margin-top: 12px;
  line-height: 26px;
  font-weight: 400;
  font-size: 18px;
}

.content_middle {
  height: 290px;
  width: 100%;
  margin-top: 24px;
}

.content_middle_left {
  float: left;
  width: 792px;
  height: 290px;
  padding: 24px;
  border-radius: 4px;
  background-color: var(--ContentBG, #FFFFFF);
}

.content_middle_title {
  float: left;
  font-style: normal;
  font-weight: 500;
  font-size: 20px;
  line-height: 38px;
  color: var(--TextPrimary, #1F2329);
}

.content_middle_more {
  float: right;
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  color: #646A73;
  border-radius: 4px;
  height: 26px;
  padding: 2px;

  &:hover {
    background: rgba(31, 35, 41, 0.1);
    cursor: pointer;
  }
}

.content_middle_more i:hover {
  background: none;
}

.content_middle_more a:hover {
  background: none;
}

.content_middle_right {
  float: left;
  height: 290px;
  width: 384px;
  margin-left: 24px;
  padding: 24px;
  border-radius: 4px;
  background-color: var(--ContentBG, #FFFFFF);
}

.content_middle_left {
  float: left;
  width: 792px;
  height: 290px;
  padding: 24px;
  border-radius: 4px;
  background-color: var(--ContentBG, #FFFFFF);
}

.li-custom {
  margin-top: 16px;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  list-style-type: disc;
  list-style-position: inside;
  border-radius: 4px;
  padding-left: 12px;
  margin-left: -12px;

  &:hover {
    background: rgba(31, 35, 41, 0.1);
    cursor: pointer;
    color: #3370FF !important;

    .li-a {
      color: #3370FF !important;
    }
  }
}

.li-custom a:hover {
  background: none;
}

.li-a {
  color: var(--TextPrimary, #1F2329);
}

.ul-custom {
  padding-inline-start: 0px;
  color: #8F959E;
}

.content_bottom {
  width: 100%;
  height: 208px;
}

.content_bottom_contact {
  float: left;
  margin-left: 278px;
  width: 200px;
  margin-top: 40px;
}

.contact_title {
  font-style: normal;
  font-weight: 500;
  font-size: 14px;
  line-height: 20px;
  color: var(--TextPrimary, #1F2329);
  margin-bottom: 16px;
}

.contact_content {
  font-style: normal;
  font-weight: 400;
  font-size: 12px;
  line-height: 16px;
  color: #646A73;
  margin-top: 8px;
}

.contact_content a:hover {
  color: #3370FF;
}

.content_bottom_qr_code {
  width: 500px;
  float: right;
  text-align: right;
  margin-right: 180px;
  margin-top: 40px;
  padding-right: 100px;
}

.contact_title_qr {
  font-style: normal;
  font-weight: 400;
  font-size: 12px;
  line-height: 20px;
  text-align: center;
  color: #646A73;
  margin-bottom: 8px;
}

.contact_wechat_train {
  width: 100px;
  float: right;
}

.contact_wechat_train_img {
  box-sizing: border-box;
  width: 100px;
  height: 100px;
  border: 2px solid #FFFFFF;
}

.contact_wechat_official {
  width: 100px;
  float: right;
  margin-right: 40px;
}

.contact_wechat_official_img {
  box-sizing: border-box;
  width: 100px;
  height: 100px;
  border: 2px solid #FFFFFF;
}

.contact_wechat_group {
  width: 100px;
  float: right;
  margin-right: 40px;
}

.contact_wechat_group_img {
  box-sizing: border-box;
  width: 100px;
  height: 100px;
  border: 2px solid #FFFFFF;
}

.main_container_outer {
  width: 100%;
  height: calc(100vh - 56px);
  background-color: var(--MainBG, #f5f6f7);
  overflow: auto;
}

</style>

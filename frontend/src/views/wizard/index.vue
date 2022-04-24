<template>
  <el-row class="main_container">
    <el-row class="head">
      <span class="hint_head">{{ $t('wizard.welcome_title') }}</span> <br>
      <span class="hint_content">{{ $t('wizard.welcome_hint') }}</span>
    </el-row>
    <el-row :gutter="20" style="margin-top:100px">
      <el-col :span="6">
        <div class="info_class">
          <div class="outer_circle">
            <div class="circle">
              <div class="inner_circle">
                <span class="info" @click="jumpPage('/datasource/index')">
                  <i class="el-icon-connection" />
                  <div>{{ $t('wizard.data_connection') }}</div>
                </span>
              </div>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="info_class">
          <div class="outer_circle">
            <div class="circle" style="border-color:#cee;">
              <div class="inner_circle">

                <span class="info" @click="jumpPage('/dataset/index')">
                  <i class="el-icon-coin" />
                  <div>
                    {{ $t('wizard.data_preparation') }}
                  </div>
                </span>
              </div>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="info_class">
          <div class="outer_circle">
            <div class="circle" style="border-color:#ece;">
              <div class="inner_circle">

                <span class="info" @click="jumpPage('/panel/index')">
                  <i class="el-icon-finished" />
                  <div>
                    {{ $t('wizard.analysis_display') }}
                  </div>
                </span>
              </div>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="info_class">
          <div class="outer_circle">
            <div class="circle" style="border-color:#cfc;">
              <div class="inner_circle">

                <span class="info" @click="jumpPage('/dataset/index')">
                  <i class="el-icon-s-promotion" />
                  <div>
                    {{ $t('wizard.resource_publishing') }}
                  </div>
                </span>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
    <!-- <el-row class="card_container">
      <info-card v-for="(cardDetail,index) in cardList" :key="index">
        <component :is="cardDetail.component" :img-index="index" :details="cardDetail" />
      </info-card>
    </el-row> -->
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
          head: this.$t('wizard.add_datasource'),
          content: this.$t('wizard.demo_video_hint'),
          bottom: '',
          href: '/datasource/index',
          component: 'CardDetail'
        }
      ]
    }
  },
  created() {
    this.init()
  },
  methods: {
    jumpPage(key) {
      this.$router.push({ path: key })
    },
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
    background-image: url('../../assets/newBg.jpg');
    height: 100%;
  }
  .head {
    text-align: center;
    color: white;
    padding: 10px;
    margin-top: 35px;
    background-size: 100% 100% !important;
    // background-image: url('../../assets/banner.png');
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
  .info_class{
    text-align: center;
    display: flex;
    justify-content: center;
  }
  .outer_circle{
    width: 250px;
    height: 250px;
    border-radius: 50%;

    border:2px solid #efefef;
    // padding: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    // background: #99f;
  }
  .inner_circle{
    border:2px solid #efefef;
    background: rgba(0,0,0,0);
    border-radius: 50%;
     height: 210px;
     width: 210px;
    // line-height: 210px;
    color: #eee;
    display: flex;
    align-items: center;
    justify-content: center;
     .info{
        cursor: pointer;
        i{
          font-size: 50px;
        }
      }
  }

  .circle{
    border:18px solid #99f;
     border-radius: 50%;
     height: 246px;
     width: 246px;
     display: flex;
     align-items: center;
    justify-content: center;
  }
</style>

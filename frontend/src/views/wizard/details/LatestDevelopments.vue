<template>
  <el-row
    class="demo_main"
  >
    <div class="demo_title">
      <a
        target="_blank"
        :href="details.href"
      >
        <img
          :src="require('../../../assets/DataEase-' +imgIndex + '.jpg')"
          height="100%"
        >
      </a>
    </div>
    <div class="demo_content">
      <el-row class="head">
        <span>{{ details.head }}</span>
      </el-row>
      <el-row class="content">
        <span v-html="details.content" />
      </el-row>
      <el-row class="bottom">
        <span class="span-box">{{ details.bottom }}</span>
      </el-row>
    </div>
  </el-row>
</template>

<script>

import { blogLastActive } from '@/api/wizard/wizard'

export default {
  name: 'CardDetail',
  props: {
    imgIndex: {
      type: Number,
      required: true
    },
    details: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      blogsInfo: {},
      imgSrc: '../../../assets/DataEase-' + this.imgIndex + '.jpg'
    }
  },
  computed: {

  },
  mounted() {
    this.init()
  },
  methods: {
    init() {
      blogLastActive().then(res => {
        this.blogsInfo = res.data[0]
      })
    }
  }
}

</script>

<style lang="scss" scoped>
  .demo_main{
    height: 100%;
  }
  .demo_title{
    float: left;
    height: 100%;
  }
  .demo_content{
    margin: auto;
    padding-left: 15px;
    height: 100%;
    overflow: hidden;
    .head{
      padding-top: 24px;
      color: var(--TopTextColor, #000000);
    }
    .content{
      height: 50%;
      padding-top: 15px;
      padding-right: 20px;
      color: var(--TextPrimary, #6D6D6D);
      font-size: 12px;
      overflow: hidden;
    }
    .bottom{
      height: 25%;
      .span-box{
        color: var(--TextPrimary, #6D6D6D);
        font-size: 12px;
        position: absolute;
        bottom: 5px;
      }
    }
  }

</style>

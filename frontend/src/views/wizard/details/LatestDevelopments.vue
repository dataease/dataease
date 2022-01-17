<template>
  <el-row class="demo_main">
    <el-row class="demo_content">
      <a :href="href" target="_blank">{{ title }}</a>
      <br>
      <span style="color: lightgray">{{ time }}</span>
    </el-row>
    <el-row class="demo_bottom">
      <a href="https://blog.fit2cloud.com/?cat=321" target="_blank">{{ $t('wizard.show_more') }}</a>
    </el-row>
  </el-row>
</template>

<script>

import { blogLastActive } from '@/api/wizard/wizard'

export default {
  name: 'LatestDevelopments',
  data() {
    return {
      title: '',
      href: '',
      time: ''
    }
  },
  mounted() {
    this.init()
  },
  methods: {
    init() {
      blogLastActive().then(res => {
        this.title = res.data.title
        this.href = res.data.href
        this.time = res.data.time
      })
    }
  }
}

</script>

<style lang="scss" scoped>
  .demo_main{
    height: 100%;
    padding: 10px 20px 10px 20px;
  }
  .demo_content{
    padding-left: 10px;
    float: left;
    font-weight: 500;
    color: var(--MenuActiveBG, #409EFF);
    max-height: 80%;
    overflow: hidden;
  }

  .demo_bottom{
    position: absolute;
    left: 30px;
    bottom: 10px;
    font-weight: 500;
    color: var(--MenuActiveBG, #409EFF);
  }

</style>

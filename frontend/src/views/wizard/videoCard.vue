<template>
  <el-row class="video_main">
    <div class="video_top">
      <a
        :href="details.href"
        target="_blank"
      >
        <img
          :src="require('@/assets/'+details.img)"
          alt="404"
        >
        <svg-icon
          class="play-icon"
          icon-class="round_play"
        />
      </a>
    </div>
    <div class="video_content">
      <a
        :href="details.href"
        target="_blank"
      >
        {{ details.content }}
      </a>
    </div>
  </el-row>
</template>
<script>
export default {
  name: 'VideoCard',
  props: {
    details: {
      type: Object,
      required: false
    }
  },
  data() {
    return {}
  }
}

</script>

<style lang="scss" scoped>
  .video_main{
    width: 240px;
    height: 192px;
    border: 1px solid #DEE0E3;
    float: left;
    border-radius: 4px;
    &:hover{
      box-shadow: 0px 6px 24px rgba(31, 35, 41, 0.08);
    }
  }
  .video_top{
    position: relative;
    width: 240px;
    height: 135px;
  }

  .video_top img{
    width: 238px;
    height: 135px;
  }

  .video_content{
    width: 240px;
    padding: 12px;
    font-weight: 500;
    font-size: 16px;
    line-height: 24px;
    color: var(--TextPrimary, #1F2329);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .play-icon{
    position: absolute;
    width: 30px;
    height: 30px;
    left: 105px;
    top:53px
  }

</style>

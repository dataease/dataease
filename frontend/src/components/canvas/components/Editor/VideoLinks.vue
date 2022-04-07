<template>

  <el-popover
    ref="popover"
    width="400"
    trigger="click"
  >
    <el-row>
      <el-form ref="form" size="mini" label-width="70px">
        <!--        <el-form-item :label="$t('panel.video_type')">-->
        <!--          <el-radio-group v-model="linkInfoTemp.videoType">-->
        <!--            <el-radio :label="'web'">{{$t('panel.online_video')}}</el-radio>-->
        <!--&lt;!&ndash;            <el-radio :label="'hls'">HLS 直播</el-radio>&ndash;&gt;-->
        <!--            <el-radio :label="'rtmp'">{{$t('panel.streaming_media')}}</el-radio>-->
        <!--          </el-radio-group>-->
        <!--        </el-form-item>-->
        <el-form-item :label="$t('panel.auto_play')">
          <el-switch v-model="linkInfoTemp[linkInfoTemp.videoType].autoplay" size="mini" />
          <span style="color: #909399; font-size: 8px;margin-left: 3px">
            Tips:{{ $t('panel.video_tips') }}
          </span>
        </el-form-item>
        <el-form-item v-if="linkInfoTemp.videoType==='web'" :label="$t('panel.play_frequency')">
          <el-radio-group v-model="linkInfoTemp[linkInfoTemp.videoType].loop">
            <el-radio :label="false">{{ $t('panel.play_once') }}</el-radio>
            <el-radio :label="true">{{ $t('panel.play_circle') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('panel.video_links')">
          <el-input v-model="linkInfoTemp[linkInfoTemp.videoType].sources[0].src" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSubmit">{{ $t('panel.confirm') }}</el-button>
          <el-button @click="onClose">{{ $t('panel.cancel') }}</el-button>
        </el-form-item>
      </el-form>
    </el-row>
    <i slot="reference" class="icon iconfont icon-chaolianjie" />
  </el-popover>
</template>

<script>
import { mapState } from 'vuex'
import { deepCopy } from '@/components/canvas/utils/utils'
import { checkAddHttp } from '@/utils/urlUtils'
import bus from "@/utils/bus";

export default {
  props: {
    linkInfo: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      linkInfoTemp: null,
      componentType: null,
      linkageActiveStatus: false,
      editFilter: [
        'view',
        'custom'
      ]
    }
  },
  watch: {
    linkInfo: {
      handler: function() {
        this.init()
      },
      deep: true
    }
  },
  created() {
    this.init()
  },
  mounted() {
  },
  computed: {
    ...mapState([
      'curComponent'
    ])
  },
  methods: {
    init() {
      this.linkInfoTemp = deepCopy(this.linkInfo)
    },
    onSubmit() {
      this.linkInfoTemp[this.linkInfoTemp.videoType].sources[0].src = checkAddHttp(this.linkInfoTemp[this.linkInfoTemp.videoType].sources[0].src)
      this.curComponent.videoLinks = this.linkInfoTemp
      this.$store.state.styleChangeTimes++
      bus.$emit('videoLinksChange-' + this.curComponent.id)
      this.popoverClose()
    },
    onClose() {
      this.$emit('close')
      this.popoverClose()
    },
    popoverClose() {
      this.$refs.popover.showPopper = false
    }
  }
}
</script>

<style lang="scss" scoped>
  .slot-class{
    color: white;
  }

  .bottom {
    margin-top: 20px;
    text-align: center;

  }
  .ellip{
    /*width: 100%;*/
    margin-left: 10px;
    margin-right: 10px;
    overflow: hidden;/*超出部分隐藏*/
    white-space: nowrap;/*不换行*/
    text-overflow:ellipsis;/*超出部分文字以...显示*/
    background-color: #f7f8fa;
    color: #3d4d66;
    font-size: 12px;
    line-height: 24px;
    height: 24px;
    border-radius: 3px;
  }

  .select-filed{
    /*width: 100%;*/
    margin-left: 10px;
    margin-right: 10px;
    overflow: hidden;/*超出部分隐藏*/
    white-space: nowrap;/*不换行*/
    text-overflow:ellipsis;/*超出部分文字以...显示*/
    color: #3d4d66;
    font-size: 12px;
    line-height: 35px;
    height: 35px;
    border-radius: 3px;
  }
  >>>.el-popover{
    height: 200px;
    overflow: auto;
  }

</style>

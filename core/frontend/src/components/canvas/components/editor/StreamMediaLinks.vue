<template>

  <el-popover
    ref="popover"
    width="400"
    trigger="click"
  >
    <el-row>
      <el-form
        ref="form"
        size="mini"
        label-width="70px"
      >
        <el-form-item :label="$t('panel.video_type')">
          <el-radio-group v-model="streamMediaInfoTemp.videoType">
            <el-radio :label="'flv'">FLV</el-radio>
            <!--            <el-radio :label="'hls'">HLS 直播</el-radio>-->
            <!--                    <el-radio :label="'rtmp'">{{$t('panel.streaming_media')}}</el-radio>-->
          </el-radio-group>
          <span style="color: #909399; font-size: 8px;margin-left: 3px">
            Tips:{{ $t('panel.live_tips') }}
          </span>
        </el-form-item>
        <el-row v-if="streamMediaInfoTemp.videoType==='flv'">
          <el-form-item :label="$t('panel.is_live')">
            <el-radio-group v-model="streamMediaInfoTemp[streamMediaInfoTemp.videoType].isLive">
              <el-radio :label="true">{{ $t('panel.yes') }}</el-radio>
              <el-radio :label="false">{{ $t('panel.no') }}</el-radio>
            </el-radio-group>
          </el-form-item>
          <!--This button is currently disabled and temporarily masked-->
          <!--          <el-form-item v-if="!streamMediaInfoTemp[streamMediaInfoTemp.videoType].isLive" :label="$t('panel.auto_play')">-->
          <!--            <el-switch v-model="streamMediaInfoTemp[streamMediaInfoTemp.videoType].autoplay" size="mini" />-->
          <!--          </el-form-item>-->
          <el-form-item
            v-if="!streamMediaInfoTemp[streamMediaInfoTemp.videoType].isLive"
            :label="$t('panel.play_frequency')"
          >
            <el-radio-group v-model="streamMediaInfoTemp[streamMediaInfoTemp.videoType].loop">
              <el-radio :label="false">{{ $t('panel.play_once') }}</el-radio>
              <el-radio :label="true">{{ $t('panel.play_circle') }}</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item :label="$t('panel.video_links')">
            <el-input v-model="streamMediaInfoTemp[streamMediaInfoTemp.videoType].url" />
          </el-form-item>
        </el-row>
        <el-form-item>
          <el-button
            type="primary"
            @click="onSubmit"
          >{{ $t('panel.confirm') }}</el-button>
          <el-button @click="onClose">{{ $t('panel.cancel') }}</el-button>
        </el-form-item>
      </el-form>
    </el-row>
    <i
      slot="reference"
      class="icon iconfont icon-chaolianjie"
    />
  </el-popover>
</template>

<script>
import { mapState } from 'vuex'
import { deepCopy } from '@/components/canvas/utils/utils'
import bus from '@/utils/bus'
import { checkAddHttp } from '@/utils/urlUtils'

export default {
  props: {
    linkInfo: {
      type: Object,
      required: true
    },
    // 属性所属组件位置
    attrPosition: {
      type: String,
      required: false,
      default: 'panel'
    }
  },
  data() {
    return {
      streamMediaInfoTemp: null,
      componentType: null,
      linkageActiveStatus: false,
      editFilter: [
        'view',
        'custom'
      ]
    }
  },
  computed: {
    ...mapState([
      'curComponent',
      'curActiveTabInner'
    ])
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
  methods: {
    init() {
      this.streamMediaInfoTemp = deepCopy(this.linkInfo)
    },
    onSubmit() {
      this.streamMediaInfoTemp[this.streamMediaInfoTemp.videoType].url = checkAddHttp(this.streamMediaInfoTemp[this.streamMediaInfoTemp.videoType].url)
      if (this.attrPosition === 'panel') {
        this.curComponent.streamMediaLinks = this.streamMediaInfoTemp
      } else {
        this.curActiveTabInner.streamMediaLinks = this.streamMediaInfoTemp
      }
      this.$store.commit('canvasChange')
      bus.$emit('streamMediaLinksChange-' + this.curComponent.id)
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
  .slot-class {
    color: white;
  }

  .bottom {
    margin-top: 20px;
    text-align: center;

  }

  .ellip {
    /*width: 100%;*/
    margin-left: 10px;
    margin-right: 10px;
    overflow: hidden; /*超出部分隐藏*/
    white-space: nowrap; /*不换行*/
    text-overflow: ellipsis; /*超出部分文字以...显示*/
    background-color: #f7f8fa;
    color: #3d4d66;
    font-size: 12px;
    line-height: 24px;
    height: 24px;
    border-radius: 3px;
  }

  .select-filed {
    /*width: 100%;*/
    margin-left: 10px;
    margin-right: 10px;
    overflow: hidden; /*超出部分隐藏*/
    white-space: nowrap; /*不换行*/
    text-overflow: ellipsis; /*超出部分文字以...显示*/
    color: #3d4d66;
    font-size: 12px;
    line-height: 35px;
    height: 35px;
    border-radius: 3px;
  }

  ::v-deep .el-popover {
    height: 200px;
    overflow: auto;
  }

</style>

<template>

  <el-popover
    ref="popover"
    width="340"
    trigger="click"
  >
    <el-row>
      <el-form ref="form" size="mini" label-width="70px">
        <el-form-item :label="'自动播放'">
          <el-switch v-model="linkInfoTemp.autoplay" size="mini" />
          <span v-show="linkInfoTemp.autoplay" style="color: #909399; font-size: 8px;margin-left: 3px">
            <!--            Tips:{{ $t('panel.link_open_tips') }}-->
          </span>
        </el-form-item>
        <el-form-item :label="$t('panel.open_mode')">
          <el-radio-group v-model="linkInfoTemp.loop">
            <el-radio :label="false">播放一次</el-radio>
            <el-radio :label="true">循环播放</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('panel.hyperLinks')">
          <el-input v-model="linkInfoTemp.sources[0].src" />
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
  mounted() {
    this.linkInfoTemp = deepCopy(this.linkInfo)
  },
  computed: {
    ...mapState([
      'curComponent'
    ])
  },
  methods: {
    onSubmit() {
      this.curComponent.videoLinks = this.linkInfoTemp
      this.$store.state.styleChangeTimes++
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

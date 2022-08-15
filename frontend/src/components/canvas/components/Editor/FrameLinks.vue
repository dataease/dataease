<template>
  <el-popover
    ref="popover"
    width="400"
    trigger="click"
  >
    <el-row>
      <el-form ref="form" size="mini" label-width="70px">
        <el-form-item :label="'Tips:'">
          <span style="color: #909399; font-size: 8px;margin-left: 3px">
            {{ $t('panel.web_set_tips') }}
          </span>
        </el-form-item>
        <el-form-item :label="$t('panel.web_url')">
          <el-input v-model="linkInfoTemp.src" />
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
import bus from '@/utils/bus'

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
      'curComponent',
      'curActiveTabInner'
    ])
  },
  methods: {
    init() {
      this.linkInfoTemp = deepCopy(this.linkInfo)
    },
    onSubmit() {
      this.linkInfoTemp.src = checkAddHttp(this.linkInfoTemp.src)
      if (this.attrPosition === 'panel') {
        this.curComponent.frameLinks = this.linkInfoTemp
      } else {
        this.curActiveTabInner.frameLinks = this.linkInfoTemp
      }
      this.$store.commit('canvasChange')
      bus.$emit('frameLinksChange-' + this.curComponent.id)
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
  ::v-deep .el-popover{
    height: 200px;
    overflow: auto;
  }

</style>

<template>
  <div class="bar-main-left">
    <div>
      <!--上钻-->
      <i
        class="icon iconfont "
        :class="[
          {
            ['icon-shangzuan i-active']: drillUpStatus ,
            ['icon-quxiaoshangzuan i-on-active']: !drillUpStatus
          }
        ]"
        @click.stop="drillUpChange"
      />
      <!--下钻-->
      <i
        class="icon iconfont "
        :class="[
          {
            ['icon-xiazuan i-active']: drillDownStatus ,
            ['icon-quxiaoxiazuan i-on-active']: !drillDownStatus
          }
        ]"
        @click.stop="drillDownChange"
      />
      <!--上卷-->
      <i
        class="icon iconfont "
        :class="[
          {
            ['icon-linkage i-active']: linkageStatus ,
            ['icon-quxiaoliandong i-on-active']: !linkageStatus
          }
        ]"
        @click.stop="linkageChange"
      />
    </div>

  </div>
</template>

<script>
import { mapState } from 'vuex'

export default {

  props: {
    element: {
      type: Object,
      required: true
    },
    active: {
      type: Boolean,
      required: false,
      default: false
    },
    // 当前模式 preview 预览 edit 编辑，
    activeModel: {
      type: String,
      required: false,
      default: 'preview'
    }
  },
  data() {
    return {
      drillUpStatus: false,
      drillDownStatus: false,
      linkageStatus: false
    }
  },
  computed: {
    linkageInfo() {
      return this.targetLinkageInfo[this.element.propValue.viewId]
    },
    ...mapState([
      'menuTop',
      'menuLeft',
      'menuShow',
      'curComponent',
      'componentData',
      'canvasStyleData',
      'linkageSettingStatus',
      'targetLinkageInfo',
      'curLinkageView'
    ])
  },
  methods: {
    drillUpChange() {
      this.drillUpStatus = !this.drillUpStatus
    },
    drillDownChange() {
      this.drillDownStatus = !this.drillDownStatus
    },
    linkageChange() {
      this.linkageStatus = !this.linkageStatus
    }
  }
}
</script>

<style lang="scss" scoped>
  .bar-main-left{
    position: absolute;
    left: 0px;
    float:right;
    z-index: 2;
    border-radius:2px;
    padding-left: 5px;
    padding-right: 2px;
    cursor:pointer!important;
    background-color: #0a7be0;
  }
  .i-on-active{
    color: whitesmoke;
    float: right;
    margin-right: 3px;
  }

  .i-active{
    color: yellow;
    float: right;
    margin-right: 3px;
  }
</style>

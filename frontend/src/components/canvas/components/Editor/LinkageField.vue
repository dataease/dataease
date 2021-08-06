<template>

  <el-popover
    width="300"
    trigger="click"
  >
    <el-row>
      <el-col :span="10">
        <div class="ellip">联动视图</div>
      </el-col>
      <el-col :span="10">
        <div class="ellip">{{ linkageInfo.targetViewName }}</div>
      </el-col>
    </el-row>

    linkageInfo{{ linkageInfo }}

    <el-row class="bottom">
      <el-button size="mini" type="success" icon="el-icon-plus" round>追加联动依赖字段</el-button>
    </el-row>

    <!--    <el-button slot="reference">T</el-button>-->
    <i slot="reference" class="icon iconfont icon-edit slot-class" />

  </el-popover>
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
      componentType: null,
      linkageActiveStatus: false,
      editFilter: [
        'view',
        'custom'
      ]
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
    showViewDetails() {
      this.$emit('showViewDetails')
    },
    edit() {

    },
    linkageEdit() {

    }
  }
}
</script>

<style lang="scss" scoped>
  .slot-class{
    color: white;
  }

  .bottom {
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

</style>

<template>

  <el-popover
    v-model="isSetting"
    width="300"
    trigger="click"
  >
    <el-row>
      <el-col :span="10" />
      <el-col :span="10" />
    </el-row>

    this is test

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
      // 编辑时临时保存 当前修改的画布
      this.$store.dispatch('panel/setComponentDataTemp', JSON.stringify(this.componentData))
      this.$store.dispatch('panel/setCanvasStyleDataTemp', JSON.stringify(this.canvasStyleData))
      if (this.curComponent.type === 'view') {
        this.$store.dispatch('chart/setViewId', null)
        this.$store.dispatch('chart/setViewId', this.curComponent.propValue.viewId)
        bus.$emit('PanelSwitchComponent', { name: 'ChartEdit', param: { 'id': this.curComponent.propValue.viewId, 'optType': 'edit' }})
      }
      if (this.curComponent.type === 'custom') {
        bus.$emit('component-dialog-edit')
      }

      // 编辑样式组件

      if (this.curComponent.type === 'v-text' || this.curComponent.type === 'rect-shape') {
        bus.$emit('component-dialog-style')
      }
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

</style>

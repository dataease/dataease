<template>
  <div class="bar-main">
    <setting-menu v-if="activeModel==='edit'" style="float: right;height: 24px!important;">
      <i slot="icon" class="icon iconfont icon-shezhi" />
    </setting-menu>
    <i v-if="curComponent.type==='view'" class="icon iconfont icon-fangda" @click.stop="showViewDetails" />
    <!--    <el-checkbox />-->
  </div>
</template>

<script>
import { mapState } from 'vuex'
import eventBus from '@/components/canvas/utils/eventBus'
import SettingMenu from '@/components/canvas/components/Editor/SettingMenu'

export default {
  components: { SettingMenu },

  props: {
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
      editFilter: [
        'view',
        'custom'
      ]
    }
  },
  computed: mapState([
    'menuTop',
    'menuLeft',
    'menuShow',
    'curComponent',
    'componentData',
    'canvasStyleData'
  ]),
  methods: {
    showViewDetails() {
      this.$emit('showViewDetails')
    }
  }
}
</script>

<style lang="scss" scoped>
  .bar-main{
    position: absolute;
    right: 0px;
    float:right;
    z-index: 2;
    border-radius:2px;
    padding-left: 5px;
    padding-right: 2px;
    cursor:pointer!important;
    background-color: #0a7be0;
  }
  .bar-main i{
    color: white;
    float: right;
    margin-right: 3px;
  }

</style>

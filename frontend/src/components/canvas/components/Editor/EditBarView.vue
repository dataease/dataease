<template>
  <div class="bar-main">
    <div v-if="!positionCheck('multiplexing')">
      <span v-if="isEdit" :title="$t('panel.edit')">
        <i class="icon iconfont icon-edit" @click.stop="edit" />
      </span>
      <span :title="$t('panel.details')">
        <i class="icon iconfont icon-fuwenbenkuang" @click.stop="showViewDetails('details')" />
      </span>
    </div>
    <div v-if="positionCheck('multiplexing')" style="margin-right: -1px;width: 18px;z-index: 5">
      <el-checkbox v-model="multiplexingCheckModel" size="medium" @change="multiplexingCheck" />
    </div>
  </div>
</template>

<script>
import bus from '@/utils/bus'
import { mapState } from 'vuex'
export default {
  props: {
    element: {
      type: Object,
      default: null
    },
    viewId: {
      type: String,
      required: true
    },
    // Deprecated
    isEdit: {
      type: Boolean,
      required: false,
      default: true
    },
    showPosition: {
      type: String,
      required: false,
      default: 'NotProvided'
    }
  },
  data() {
    return {
      multiplexingCheckModel: false,
      componentType: null,
      linkageActiveStatus: false,
      editFilter: [
        'view',
        'custom'
      ],
      timer: null
    }
  },
  computed: {
    // gapStyle() {
    //   return {
    //     'right': this.curGap + 'px!important'
    //   }
    // },
    // curGap() {
    //   return (this.canvasStyleData.panel.gap === 'yes' && this.element.auxiliaryMatrix) ? this.componentGap : 0
    // },
    ...mapState([
      'linkageSettingStatus',
      'componentData',
      'canvasStyleData',
      'componentGap'
    ])
  },
  mounted() {
    if (this.showPosition === 'multiplexing-view') {
      this.multiplexingCheckModel = true
      this.multiplexingCheck(this.multiplexingCheckModel)
    }
  },
  beforeDestroy() {
  },
  methods: {
    edit() {
      this.$store.dispatch('chart/setViewId', null)
      this.$store.dispatch('chart/setViewId', this.viewId)
      bus.$emit('change_panel_right_draw', true)
    },
    linkageEdit() {

    },
    amRemoveItem() {
      this.$emit('amRemoveItem')
    },
    showViewDetails(params) {
      this.$emit('showViewDetails', params)
    },
    positionCheck(position) {
      return this.showPosition.includes(position)
    },
    multiplexingCheck(val) {
      if (val) {
        // push
        this.$store.commit('addCurMultiplexingComponent', { 'component': this.element, 'componentId': this.viewId })
      } else {
        // remove
        this.$store.commit('removeCurMultiplexingComponentWithId', this.viewId)
      }
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
    border-radius:2px!important;
    padding-left: 3px!important;
    padding-right: 0px!important;
    cursor:pointer!important;
    background-color: #0a7be0;
  }
  .bar-main i{
    color: white;
    float: right;
    margin-right: 3px;
  }

</style>

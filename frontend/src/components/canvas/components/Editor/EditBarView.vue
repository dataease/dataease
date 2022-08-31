<template>
  <div class="bar-main">
    <div v-if="!positionCheck('multiplexing') && !positionCheck('email-task')">
      <span v-if="isEdit" :title="$t('panel.edit')">
        <i class="icon iconfont icon-edit" @click.stop="edit" />
      </span>
      <span v-show="detailsShow" :title="$t('panel.details')">
        <i class="icon iconfont icon-chakan" @click.stop="showViewDetails('details')" />
      </span>
      <span v-show="enlargeShow" :title="$t('panel.enlarge')">
        <i class="icon iconfont icon-fangda" @click.stop="showViewDetails('enlarge')" />
      </span>
    </div>
    <div v-if="positionCheck('multiplexing')" style="margin-right: -1px;width: 18px;z-index: 5">
      <el-checkbox v-model="multiplexingCheckModel" size="medium" @change="multiplexingCheck" />
    </div>
    <div v-if="positionCheck('email-task') && element.component === 'user-view'" style="margin-right: -1px;width: 18px;z-index: 5">
      <el-checkbox v-model="isTaskChecked" size="medium" @change="emailTaskCheck" />
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
    },
    panelId: {
      type: String,
      default: null
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
      timer: null,
      isTaskChecked: false
    }
  },
  computed: {
    detailsShow(){
      return this.element.propValue.innerType !== 'richTextView'
    },
    enlargeShow(){
      return this.element.propValue.innerType !== 'richTextView'
    },
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
      'componentGap',
      'panelViews'
    ]),

    taskChecked() {
      const panelId = this.panelId
      return !!this.panelViews && !!this.panelViews[panelId] && !!this.panelViews[panelId].some(viewId => viewId === this.viewId)
    }
  },
  watch: {
    taskChecked(val) {
      this.isTaskChecked = val
    }
  },
  mounted() {
    if (this.showPosition === 'multiplexing-view') {
      this.multiplexingCheckModel = true
      this.multiplexingCheck(this.multiplexingCheckModel)
    }
    if (this.showPosition === 'email-task') {
      this.isTaskChecked = !!this.taskChecked
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
      this.$emit('showViewDetails', { openType: params })
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
    },
    emailTaskCheck(val) {
      if (val) {
        this.$store.dispatch('task/addView', { 'panelId': this.panelId, 'viewId': this.viewId })
      } else {
        this.$store.dispatch('task/delView', { 'panelId': this.panelId, 'viewId': this.viewId })
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
    background-color: var(--primary,#3370ff);
  }
  .bar-main i{
    color: white;
    float: right;
    margin-right: 3px;
  }

</style>

<template>
  <div class="bar-main">
    <div>
      <span v-if="isEdit" :title="$t('panel.edit')">
        <i class="icon iconfont icon-edit" @click.stop="edit" />
      </span>
      <span :title="$t('panel.details')">
        <i class="icon iconfont icon-fangda" @click.stop="showViewDetails" />
      </span>
    </div>

  </div>
</template>

<script>
import bus from '@/utils/bus'
import { mapState } from 'vuex'
export default {
  props: {
    viewId: {
      type: String,
      required: true
    },
    isEdit: {
      type: Boolean,
      required: false,
      default: true
    }
  },
  data() {
    return {
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
    ...mapState([
      'linkageSettingStatus',
      'componentData',
      'canvasStyleData'
    ])
  },
  mounted() {
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

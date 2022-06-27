<template>
  <div v-show="existLinkage" class="bar-main">
    <div>
      <el-button size="mini" type="warning" @click="clearAllLinkage"><i class="icon iconfont icon-quxiaoliandong" />{{ $t('panel.remove_all_linkage') }}</el-button>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import bus from '@/utils/bus'

export default {
  computed: {
    existLinkage() {
      let linkageFiltersCount = 0
      this.componentData.forEach(item => {
        if (item.linkageFilters && item.linkageFilters.length > 0) {
          linkageFiltersCount++
        }
      })
      return linkageFiltersCount
    },
    ...mapState([
      'componentData'
    ])
  },
  methods: {
    clearAllLinkage() {
      this.$store.commit('clearPanelLinkageInfo')
      bus.$emit('clear_panel_linkage', { viewId: 'all' })
    }
  }
}
</script>

<style lang="scss" scoped>
  .bar-main{
    position: absolute;
    right: 0px;
    z-index: 10;
    height: 20px;
    border-radius:2px;
    padding-left: 3px;
    padding-right: 0px;
    cursor:pointer!important;
    opacity: 0.8;
    /*background-color: #0a7be0;*/
  }

</style>

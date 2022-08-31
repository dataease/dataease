<template>
  <div class="bar-main">
    <div style="margin-right: -1px;width: 18px">
      <i class="icon iconfont icon-guanbi" @click.stop="closePreview" />
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import bus from '@/utils/bus'
import SettingMenu from '@/components/canvas/components/Editor/SettingMenu'
import LinkageField from '@/components/canvas/components/Editor/LinkageField'

export default {
  components: { },

  props: {

  },
  data() {
    return {

    }
  },
  computed: {

  },
  mounted() {
  },
  beforeDestroy() {
  },
  methods: {
    closePreview() {
      this.$emit('closePreview')
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
    padding-left: 3px;
    padding-right: 0px;
    cursor:pointer!important;
    background-color: var(--primary,#3370ff);
  }
  .bar-main i{
    color: white;
    float: right;
    margin-right: 3px;
  }

</style>

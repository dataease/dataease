<template>
  <el-tabs :class="['de-tabs',...tabClassName]" :style="tabStyle" v-bind="$attrs" v-on="$listeners">
    <slot />
  </el-tabs>
</template>

<script>
export default {
  name: 'DataeaseTabs',
  props: {

    fontColor: String,
    activeColor: String,
    borderColor: String,
    borderActiveColor: String,
    styleType: {
      type: String,
      default: '',
      validator: (val) => ['', 'radioGroup'].includes(val)
    }
  },
  data() {
    return {}
  },
  computed: {
    tabStyle() {
      return {
        '--font-color': this.fontColor,
        '--active-color': this.activeColor,
        '--border-color': this.borderColor,
        '--border-active-color': this.borderActiveColor
      }
    },
    tabClassName() {
      const classes = [
        this.styleType,
        this.fontColor && 'fontColor',
        this.activeColor && 'activeColor',
        this.noBorder ? 'noBorder' : this.borderColor && 'borderColor',
        this.borderActiveColor && 'borderActiveColor'
      ]
      return classes
    },
    noBorder() {
      return this.borderColor === 'none'
    },
    noBorderActive() {
      return this.borderActiveColor === 'none'
    }
  },
  created() {},
  methods: {}
}
</script>
<style lang="scss">
@import "../../styles/de-tabs";

</style>


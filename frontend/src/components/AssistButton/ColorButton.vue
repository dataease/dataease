<template>
  <div class="color-button-main">
    <div class="color-button-outer" :class="active?'color-button-active':''" @click.stop="colorButtonClick">
      <div class="color-button-inner" :class="'color-button-inner-'+colorType" />
    </div>
    <span class="text-area">
      <slot />
    </span>
  </div>
</template>

<script>

export default {
  name: 'ColorButton',
  inheritAttrs: true,
  props: {
    colorType: {
      type: String,
      default: 'light'
    },
    label: {
      type: String,
      default: null
    }
  },
  data() {
    return {}
  },
  computed: {
    active() {
      return this.label === this.colorType
    }
  },
  watch: {

  },
  created() {
  },
  methods: {
    colorButtonClick() {
      this.$emit('onClick', this.colorType)
    }
  }
}
</script>

<style lang="scss">

.color-button-main{
  float: left;
}

.color-button-outer {
  width: 24px;
  height: 24px;
  border-radius: 2px;
  border-width: 1px;
  border-color: #DEE0E3;
  cursor: pointer;
  border-style: solid;
  padding-left: 3px;
  padding-top: 3px;
  &:hover {
    padding-left: 2px;
    padding-top: 2px;
    border-width: 2px;
    border-color: #DEE0E3;
  }
}
.color-button-active{
  padding-left: 2px;
  padding-top: 2px;
  border-width: 2px;
  border-color: #3370FF!important;
}

.color-button-inner {
  width: 16px;
  height: 16px;
  border-radius: 2px;
  border: 1px solid #DEE0E3;
}
.color-button-inner-light{
  background-color: #FFFFFF;
}

.color-button-inner-dark{
  background-color: #0C296E;
}
.text-area{
  font-weight: 400;
  size: 14px;
  line-height: 22px;
  margin-top: 4px;
}

</style>

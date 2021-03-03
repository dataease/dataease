<template>
  <ms-tip-button
    :disabled="disabled || isReadOnly"
    :type="type"
    :tip="tip"
    :icon="icon"
    size="mini"
    circle
    @click="exec"
    @clickStop="clickStop"
  />
</template>

<script>
import MsTipButton from './MsTipButton'
import { checkoutTestManagerOrTestUser } from '@/metersphere/common/js/utils'
export default {
  name: 'MsTableOperatorButton',
  components: { MsTipButton },
  props: {
    icon: {
      type: String,
      default: 'el-icon-question'
    },
    type: {
      type: String,
      default: 'primary'
    },
    tip: {
      type: String,
      default: ''
    },
    disabled: {
      type: Boolean,
      default: false
    },
    isTesterPermission: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      isReadOnly: false
    }
  },
  mounted() {
    if (this.isTesterPermission && !checkoutTestManagerOrTestUser()) {
      this.isReadOnly = true
    }
  },
  methods: {
    exec() {
      this.$emit('exec')
    },
    clickStop() {
      this.$emit('clickStop')
    }
  }
}
</script>

<style scoped>
</style>

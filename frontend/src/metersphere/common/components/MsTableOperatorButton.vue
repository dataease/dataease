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
import MsTableButton from './MsTableButton'
import MsTipButton from './MsTipButton'
import { checkoutTestManagerOrTestUser, hasRoles } from '@/metersphere/common/js/utils'
import { ROLE_TEST_MANAGER, ROLE_TEST_USER } from '@/metersphere/common/js/constants'
export default {
  name: 'MsTableOperatorButton',
  components: { MsTipButton, MsTableButton },
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
      type: String
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

<template>
  <el-button :disabled="disabled" plain :type="type" :icon="icon" :size="size" @click="exec()">
    {{ content }}
  </el-button>
</template>

<script>
import { checkoutTestManagerOrTestUser } from '@/metersphere/common/js/utils'

export default {
  name: 'MsTableButton',
  props: {
    content: String,
    icon: {
      type: String,
      default: 'el-icon-question'
    },
    type: {
      type: String,
      default: null
    },
    effect: {
      type: String,
      default: 'dark'
    },
    size: {
      type: String,
      default: 'mini'
    },
    isTesterPermission: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      disabled: false
    }
  },
  mounted() {
    if (this.isTesterPermission && !checkoutTestManagerOrTestUser()) {
      this.disabled = true
    }
  },
  methods: {
    exec() {
      this.$emit('click')
    }
  }
}
</script>

<style scoped>

  .el-button {
    padding: 8px 10px;
  }
</style>

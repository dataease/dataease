<template>
  <el-tooltip :content="tooltip">
    <el-button circle class="fu-search-bar-button" :type="type" :size="size" @click="click">
      <slot>
        <i :class="icon" />
      </slot>
    </el-button>
  </el-tooltip>
</template>

<script>
export default {
  name: 'GridButton',
  props: {
    size: {
      type: String,
      default: 'mini'
    },
    icon: {
      type: String,
      default: null
    },
    type: {
      type: String,
      default: null
    },
    tooltip: {
      type: String,
      default: null
    }
  },
  methods: {
    click(e) {
      this.$emit('click', e)
    }
  }
}
</script>

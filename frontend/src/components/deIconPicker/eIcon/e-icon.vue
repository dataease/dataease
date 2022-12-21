<template>
  <i
    v-if="fontClass"
    :class="iconName"
    @click="click(iconName,$event)"
  />
  <svg
    v-else-if="svg"
    :class="svgClass"
    aria-hidden="true"
    @click="click(iconName,$event)"
  >
    <use :xlink:href="iconName" />
  </svg>
  <div
    v-else-if="isExternal"
    :style="styleExternalIcon"
    :class="className"
    class="icon external-icon"
    @click="click(iconName,$event)"
  />
</template>

<script>
import { isExternal } from '../utils/index'

export default {
  name: 'EIcon',
  props: {
    iconName: {
      type: String,
      required: true
    },
    className: {
      type: String,
      default: ''
    }
  },
  computed: {
    fontClass() {
      return this.iconName && this.iconName.trim().length > 2 && (!isExternal(this.iconName) && !this.iconName.startsWith('#'))
    },
    svg() {
      return this.iconName && this.iconName.trim().length > 2 && (!isExternal(this.iconName) && this.iconName.startsWith('#'))
    },
    isExternal() {
      return isExternal(this.iconName)
    },
    svgClass() {
      if (this.className) {
        return 'icon ' + this.className
      } else {
        return 'icon'
      }
    },
    styleExternalIcon() {
      return {
        'background-image': `url(${this.iconName})`,
        'background-repeat': 'no-repeat',
        'background-size': '100% 100%',
        '-moz-background-size': '100% 100%'
      }
    }
  },
  methods: {
    click(iconName, event) {
      if (event) event.preventDefault()
      this.$emit('click', iconName)
    }
  }
}
</script>

<style scoped>
.icon {
  width: 1em;
  height: 1em;
  vertical-align: -0.15em;
  fill: currentColor;
  overflow: hidden;
}

.external-icon {
  display: inline-block;
}
</style>

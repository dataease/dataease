<script lang="ts" setup>
import { isExternal } from '@/utils/validate'
import { propTypes } from '@/utils/propTypes'
defineProps({
  to: propTypes.string.def('')
})
const linkProps = (url: string) => {
  if (isExternal(url)) {
    return {
      is: 'a',
      href: url,
      target: '_blank',
      rel: 'noopener'
    }
  }
  return {
    is: 'router-link',
    to: url
  }
}
</script>

<template>
  <!-- eslint-disable vue/require-component-is -->
  <component v-bind="linkProps(to)">
    <slot />
  </component>
</template>

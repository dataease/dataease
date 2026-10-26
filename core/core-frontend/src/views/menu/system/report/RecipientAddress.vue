<script lang="ts" setup>
import { computed } from 'vue'

const props = defineProps<{
  recipientName: string
  recipientAddress: string
  channel: number
}>()
const groupName = computed(() =>
  [6, 8, 9].includes(props.channel) &&
  props.recipientName?.trim() &&
  props.recipientName !== props.recipientAddress
    ? props.recipientName
    : ''
)
</script>

<template>
  <div class="recipient-address">
    <template v-if="groupName">
      <div>{{ groupName }}</div>
      <small>{{ recipientAddress }}</small>
    </template>
    <template v-else>{{ recipientAddress }}</template>
  </div>
</template>

<style scoped>
small {
  color: var(--ed-text-color-secondary);
}
</style>

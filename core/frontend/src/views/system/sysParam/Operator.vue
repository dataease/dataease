<template>
  <div class="operator-bar">
    <p class="title">{{ $t(title) }}</p>
    <div class="btn-grounp">
      <slot />
    </div>
  </div>
</template>

<script>
export default {
  props: {
    title: {
      type: String,
      default: ''
    }
  }
}
</script>

<template>
  <div>
    <svg-icon icon-class="doc" @click="click" />
  </div>
</template>

<script>
export default {
  name: 'Doc',
  methods: {
    click() {
      window.open(process.env.VUE_APP_BASE_API + 'doc.html', '_blank')
    }
  }
}
</script>

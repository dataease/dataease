<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script>
export default {
  name: 'App',

  beforeCreate() {
    // document.body.className = 'blackTheme'
  }
}
</script>

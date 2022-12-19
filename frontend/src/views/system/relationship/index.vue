<template>
  <div>
    <h2>this is blood relationship page for shutong</h2>
    <span>{{ text }}</span>
  </div>
</template>
<script>
export default {
  name: 'Relationship',
  data() {
    return {
      text: ''
    }
  },
  created() {
    this.init
  },
  methods: {
    init() {
      this.text = 'shutong is a handsome guy'
    }
  }
}
</script>

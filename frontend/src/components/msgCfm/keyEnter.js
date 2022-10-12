export default {
  mounted() {
    document.addEventListener('keypress', this.entryKey)
  },
  destroyed() {
    document.removeEventListener('keypress', this.entryKey)
  },
  methods: {
    entryKey(event) {
      const keyCode = event.keyCode
      if (keyCode === 13) {
        this.$refs.search.blur()
      }
    }
  }
}

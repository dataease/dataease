<template>
  <div>
    <p>预览</p>
    <div>
      aaaaaaaaaaaaaaa
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'

export default {
  name: 'textPopShow',
  props: {
    element: {
      type: Object,
      require: true
    }
  },
  data() {
    return {
      
    }
  },
  computed: {
    ...mapState([
      'curComponent',
      'componentData'
    ])
  },
  mounted() {

  },
  methods: {

  }
}
</script>
<style lang="scss">
.root-class {
  margin: 10px 0px 5px;
  text-align: center;
}
</style>
<template>
  <div v-if="unionParam.type" style="height:400px;">
    <p>{{ unionParam.node.currentDs.name }}</p>
    <p>{{ unionParam.parent.currentDs.name }}</p>
  </div>
</template>

<script>
export default {
  name: 'UnionEdit',
  props: {
    unionParam: {
      type: Object,
      required: true
    }
  },
  data() {
    return {

    }
  },
  mounted() {
  },
  methods: {

  }
}
</script>

<style scoped>

</style>

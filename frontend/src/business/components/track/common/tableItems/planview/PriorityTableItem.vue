<template>
  <div>
    <ms-tag v-if="value == 'P0'" type="danger" :content="value"/>
    <ms-tag v-if="value == 'P1'" type="warning"  :content="value"/>
    <ms-tag v-if="value == 'P2'" effect="light" type="danger" :content="value"/>
    <ms-tag v-if="value == 'P3'" effect="light" type="warning" :content="value"/>
  </div>

</template>

<script>
    import MsTag from "../../../../common/components/MsTag";
    export default {
      name: "PriorityTableItem",
      components: {MsTag},
      props: {
        value: {
          type: String
        }
      }
    }
</script>

<style scoped>

</style>

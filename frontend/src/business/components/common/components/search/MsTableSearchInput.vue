<template>
  <ms-table-search-component v-model="component.operator.value" :component="component">
    <template v-slot="scope">
      <el-input v-model="scope.component.value" v-bind="props"
                :placeholder="$t('commons.input_content')" size="small"/>
    </template>
  </ms-table-search-component>
</template>

<script>
  import MsTableSearchComponent from "./MsTableSearchComponet";

  export default {
    name: "MsTableSearchInput",
    components: {MsTableSearchComponent},
    props: ['component'],
    data() {
      return {
        props: {
          maxlength: "60",
          showWordLimit: true,
          ...this.component.props
        }
      }
    }
  }
</script>

<style scoped>

</style>

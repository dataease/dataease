<template>

    <el-input
      class="search"
      type="text"
      size="small"
      :placeholder="tip"
      prefix-icon="el-icon-search"
      @change="search"
      maxlength="60"
      v-model="condition.name" clearable/>

</template>

<script>
    export default {
      name: "MsTableSearchBar",
      props: {
        condition: {
          type: Object
        },
        tip: {
          String,
          default() {
            return this.$t('commons.search_by_name');
          }
        }
      },
      methods: {
        search() {
          this.$emit('update:condition', this.condition);
          this.$emit('change');
        }
      }
    }
</script>

<style scoped>

</style>

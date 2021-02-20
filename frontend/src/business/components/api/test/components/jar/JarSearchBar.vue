<template>
  <el-input class="jar-header-search"
            v-model="condition.name"
            type="text"
            size="small"
            prefix-icon="el-icon-search"
            @blur="search"
            :placeholder="$t('project.search_by_name_jar')" clearable/>
</template>

<script>
    export default {
      name: "MsJarSearchBar",
      props: {
        condition: {
          type: Object,
          default() {
            return undefined
          },
        },
      },
      methods: {
        search() {
          this.$emit('search', true);
        }
      },
    }
</script>

<style scoped>
  .jar-header-search {
    margin-bottom: 10px;
    margin-top: 10px;
    min-width: 100px;
    max-width: 250px;
  }
</style>

<template>
  <ms-table-search-component v-model="component.operator.value" :component="component">
    <template v-slot="scope">
      <el-select v-model="scope.component.value" :placeholder="$t('commons.please_select')" size="small"
                 filterable v-bind="scope.component.props" class="search-select">
        <el-option v-for="op in options" :key="op.value" :label="label(op)" :value="op.value"></el-option>
      </el-select>
    </template>
  </ms-table-search-component>
</template>

<script>
  import MsTableSearchComponent from "./MsTableSearchComponet";

  export default {
    name: "MsTableSearchSelect",
    components: {MsTableSearchComponent},
    props: ['component'],
    data() {
      return {
        options: !(this.component.options instanceof Array) ? [] : this.component.options || []
      }
    },
    created() {
      if (!(this.component.options instanceof Array) && this.component.options.url) {
        this.$get(this.component.options.url, response => {
          if (response.data) {
            response.data.forEach(item => {
              this.options.push({
                label: item[this.component.options.labelKey],
                value: item[this.component.options.valueKey]
              })
            })
          }
        })
      }
    },
    computed: {
      label() {
        return op => {
          if (this.component.options.showLabel) {
            return this.component.options.showLabel(op);
          }
          return op.label.indexOf(".") !== -1 ? this.$t(op.label) : op.label;
        }
      }
    }
  }
</script>

<style scoped>
  .search-select {
    display: inline-block;
    width: 100%;
  }
</style>

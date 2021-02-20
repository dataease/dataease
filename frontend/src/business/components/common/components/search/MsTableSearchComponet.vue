<template>
  <div>
    <div class="search-label">{{$t(component.label)}}</div>

    <el-select class="search-operator" v-model="component.operator.value" :placeholder="$t('commons.please_select')"
               size="small"
               @change="change" @input="input" v-bind="component.operator.props">
      <el-option v-for="o in operators" :key="o.value" :label="$t(o.label)" :value="o.value"/>
    </el-select>

    <div class="search-content" v-if="showContent">
      <slot v-bind:component="component"></slot>
    </div>
  </div>
</template>

<script>
  export default {
    name: "MsTableSearchComponent",
    props: ['component'],
    data() {
      return {
        operators: this.component.operator.options || [],
      }
    },
    methods: {
      change(value) {
        if (this.component.operator.change) {
          this.component.operator.change(this.component, value)
        }
        this.$emit('change', value);
      },
      input(value) {
        this.$emit('input', value);
      }
    },
    computed: {
      showContent() {
        if (this.component.isShow) {
          return this.component.isShow(this.component.operator.value);
        }
        return true;
      }
    }
  }
</script>

<style scoped>
  .search-label {
    display: inline-block;
    width: 120px;
    box-sizing: border-box;
    padding-left: 5px;
  }

  .search-operator {
    display: inline-block;
    width: 120px;
  }

  .search-content {
    display: inline-block;
    padding: 0 5px 0 10px;
    width: calc(100% - 240px);
    box-sizing: border-box;
  }
</style>

<template>
  <div>
    <el-autocomplete
      size="small"
      class="input-with-autocomplete"
      v-model="mock.mock"
      :fetch-suggestions="funcSearch"
      :disabled="disabled"
      :placeholder="$t('api_test.value')"
      value-key="name"
      highlight-first-item
      @select="change">
      <i slot="suffix" class="el-input__icon el-icon-edit pointer" @click="advanced()"></i>
    </el-autocomplete>

    <ms-advance ref="variableAdvance" :current-item="mock"/>
  </div>
</template>

<script>
  import {JMETER_FUNC, MOCKJS_FUNC} from "@/common/js/constants";
  import MsAdvance from "./Advance";

  export default {
    name: 'MsMock',
    components: {MsAdvance},
    props: {
      schema: {
        type: Object,
        default: () => {
        }
      },
      disabled: Boolean,
    },
    data() {
      return {
        mock: {mock: ""}
      }
    },
    created() {
      if (this.schema.mock && Object.prototype.toString.call(this.schema.mock).match(/\[object (\w+)\]/)[1].toLowerCase() === 'object') {
        this.mock = this.schema.mock;
      } else {
        this.schema.mock = this.mock;
      }
      if (this.schema.type === 'object') {
        this.$delete(this.schema, 'mock')
      }
    },
    mounted() {
    },
    methods: {
      funcSearch(queryString, cb) {
        let funcs = MOCKJS_FUNC.concat(JMETER_FUNC);
        let results = queryString ? funcs.filter(this.funcFilter(queryString)) : funcs;
        // 调用 callback 返回建议列表的数据
        cb(results);
      },
      funcFilter(queryString) {
        return (func) => {
          return (func.name.toLowerCase().indexOf(queryString.toLowerCase()) > -1);
        };
      },
      change: function () {
      },
      advanced() {
        this.$refs.variableAdvance.open();
      },
      showEdit() {
        this.$emit('showEdit')
      },
      handleChange(e) {
        this.$emit('change', e)
      },
      querySearchAsync(queryString, cb) {
        const arr = this.mock || []
        const results = queryString
          ? arr.filter(this.createStateFilter(queryString))
          : arr

        cb(results)
      },
      createStateFilter(queryString) {
        return state => {
          return (
            state.value.toLowerCase().indexOf(queryString.toLowerCase()) === 0
          )
        }
      }
    }
  }
</script>

<style lang="scss" scoped>
</style>

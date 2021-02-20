<template>
  <api-base-component
    @copy="copyRow"
    @remove="remove"
    :data="controller"
    :show-collapse="false"
    :draggable="true"
    color="#E6A23C"
    background-color="#FCF6EE"
    :title="$t('api_test.automation.if_controller')">

    <template v-slot:headerLeft>

      <el-input draggable size="small" v-model="controller.variable" style="width: 20%" :placeholder="$t('api_test.request.condition_variable')"/>

      <el-select v-model="controller.operator" :placeholder="$t('commons.please_select')" size="small"
                 @change="change" style="width: 10%;margin-left: 10px">
        <el-option v-for="o in operators" :key="o.value" :label="$t(o.label)" :value="o.value"/>
      </el-select>

      <el-input draggable size="small" v-model="controller.value" :placeholder="$t('api_test.value')" v-if="!hasEmptyOperator" style="width: 20%;margin-left: 20px"/>
    </template>

  </api-base-component>
</template>

<script>
  import ApiBaseComponent from "../common/ApiBaseComponent";
  export default {
    name: "MsIfController",
    components: {ApiBaseComponent},
    props: {
      controller: {},
      node: {},
      index: Object,
      draggable: {
        type: Boolean,
        default: false,
      },
    },
    data() {
      return {
        operators: {
          EQ: {
            label: "commons.adv_search.operators.equals",
            value: "=="
          },
          NE: {
            label: "commons.adv_search.operators.not_equals",
            value: "!="
          },
          LIKE: {
            label: "commons.adv_search.operators.like",
            value: "=~"
          },
          NOT_LIKE: {
            label: "commons.adv_search.operators.not_like",
            value: "!~"
          },
          GT: {
            label: "commons.adv_search.operators.gt",
            value: ">"
          },
          LT: {
            label: "commons.adv_search.operators.lt",
            value: "<"
          },
          IS_EMPTY: {
            label: "commons.adv_search.operators.is_empty",
            value: "is empty"
          },
          IS_NOT_EMPTY: {
            label: "commons.adv_search.operators.is_not_empty",
            value: "is not empty"
          }
        }
      }
    },
    methods: {
      remove() {
        this.$emit('remove', this.controller, this.node);
      },
      copyRow() {
        this.$emit('copyRow', this.controller, this.node);
      },
      change(value) {
        if (value.indexOf("empty") > 0 && !!this.controller.value) {
          this.controller.value = "";
        }
      }
    },
    computed: {
      hasEmptyOperator() {
        return !!this.controller.operator && this.controller.operator.indexOf("empty") > 0;
      }
    }
  }
</script>

<style scoped>
</style>

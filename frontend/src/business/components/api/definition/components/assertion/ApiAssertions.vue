<template>
  <api-base-component
    v-loading="loading"
    @copy="copyRow"
    @remove="remove"
    @active="active"
    :data="assertions"
    :draggable="draggable"
    color="#A30014"
    background-color="#F7E6E9"
    :title="$t('api_test.definition.request.assertions_rule')">

    <div class="assertion-add" :draggable="draggable">
      <el-row :gutter="10">
        <el-col :span="4">
          <el-select :disabled="isReadOnly" class="assertion-item" v-model="type"
                     :placeholder="$t('api_test.request.assertions.select_type')"
                     size="small">
            <el-option :label="$t('api_test.request.assertions.regex')" :value="options.REGEX"/>
            <el-option :label="'JSONPath'" :value="options.JSON_PATH"/>
            <el-option :label="'XPath'" :value="options.XPATH2"/>
            <el-option :label="$t('api_test.request.assertions.response_time')" :value="options.DURATION"/>
            <el-option :label="$t('api_test.request.assertions.jsr223')" :value="options.JSR223"/>
          </el-select>
        </el-col>
        <el-col :span="20">
          <ms-api-assertion-regex :is-read-only="isReadOnly" :list="assertions.regex" v-if="type === options.REGEX"
                                  :callback="after"/>
          <ms-api-assertion-json-path :is-read-only="isReadOnly" :list="assertions.jsonPath"
                                      v-if="type === options.JSON_PATH" :callback="after"/>
          <ms-api-assertion-x-path2 :is-read-only="isReadOnly" :list="assertions.xpath2" v-if="type === options.XPATH2"
                                    :callback="after"/>
          <ms-api-assertion-duration :is-read-only="isReadOnly" v-model="time" :duration="assertions.duration"
                                     v-if="type === options.DURATION" :callback="after"/>
          <ms-api-assertion-jsr223 :is-read-only="isReadOnly" :list="assertions.jsr223" v-if="type === options.JSR223"
                                   :callback="after"/>
          <el-button v-if="!type" :disabled="true" type="primary" size="small">
            {{ $t('api_test.request.assertions.add') }}
          </el-button>
        </el-col>
      </el-row>
    </div>

    <api-json-path-suggest-button :open-tip="$t('api_test.request.assertions.json_path_suggest')"
                                  :clear-tip="$t('api_test.request.assertions.json_path_clear')" @open="suggestJsonOpen" @clear="clearJson"/>

    <ms-api-assertions-edit :is-read-only="isReadOnly" :assertions="assertions" :reloadData="reloadData" style="margin-bottom: 20px"/>

    <ms-api-jsonpath-suggest :tip="$t('api_test.request.extract.suggest_tip')" @addSuggest="addJsonPathSuggest" ref="jsonpathSuggest"/>

  </api-base-component>
</template>

<script>
  import MsApiAssertionText from "./ApiAssertionText";
  import MsApiAssertionRegex from "./ApiAssertionRegex";
  import MsApiAssertionDuration from "./ApiAssertionDuration";
  import {ASSERTION_TYPE, JSONPath} from "../../model/ApiTestModel";
  import MsApiAssertionsEdit from "./ApiAssertionsEdit";
  import MsApiAssertionJsonPath from "./ApiAssertionJsonPath";
  import MsApiAssertionJsr223 from "./ApiAssertionJsr223";
  import MsApiJsonpathSuggestList from "./ApiJsonpathSuggestList";
  import MsApiAssertionXPath2 from "./ApiAssertionXPath2";
  import {getUUID} from "@/common/js/utils";
  import ApiJsonPathSuggestButton from "./ApiJsonPathSuggestButton";
  import MsApiJsonpathSuggest from "./ApiJsonpathSuggest";
  import ApiBaseComponent from "../../../automation/scenario/common/ApiBaseComponent";

  export default {
    name: "MsApiAssertions",
    components: {
      ApiBaseComponent,
      MsApiJsonpathSuggest,
      ApiJsonPathSuggestButton,
      MsApiAssertionXPath2,
      MsApiAssertionJsr223,
      MsApiJsonpathSuggestList,
      MsApiAssertionJsonPath,
      MsApiAssertionsEdit, MsApiAssertionDuration, MsApiAssertionRegex, MsApiAssertionText
    },
    props: {
      draggable: {
        type: Boolean,
        default: false,
      },
      assertions: {},
      node: {},
      request: {},
      response: {},
      customizeStyle: {
        type: String,
        default: "margin-top: 10px"
      },
      isReadOnly: {
        type: Boolean,
        default: false
      }
    },
    data() {
      return {
        options: ASSERTION_TYPE,
        time: "",
        type: "",
        loading: false,
        reloadData: "",
      }
    },
    methods: {
      after() {
        this.type = "";
        this.reloadData = getUUID().substring(0, 8);
        this.reload();
      },
      copyRow() {
        this.$emit('copyRow', this.assertions, this.node);
      },
      suggestJsonOpen() {
        this.$emit('suggestClick');
        this.$nextTick(() => {
          if (!this.response || !this.response.responseResult || !this.response.responseResult.body) {
            this.$message(this.$t('api_test.request.assertions.debug_first'));
            return;
          }
          this.$refs.jsonpathSuggest.open(this.response.responseResult.body);
        })
      },
      reload() {
        this.loading = true
        this.$nextTick(() => {
          this.loading = false
        })
      },
      active() {
        this.assertions.active = !this.assertions.active;
        this.reload();
      },
      remove() {
        this.$emit('remove', this.assertions, this.node);
      },
      addJsonPathSuggest(data) {
        let jsonItem = new JSONPath();
        jsonItem.expression = data.path;
        jsonItem.expect = data.value;
        jsonItem.setJSONPathDescription();
        this.assertions.jsonPath.push(jsonItem);
      },
      clearJson() {
        this.assertions.jsonPath = [];
      }
    }
  }
</script>

<style scoped>
  .assertion-item {
    width: 100%;
  }

  .assertion-add {
    padding: 10px;
    margin: 5px 0;
    border-radius: 5px;
    border: #DCDFE6 solid 1px;
  }

  .icon.is-active {
    transform: rotate(90deg);
  }

  /deep/ .el-card__body {
    padding: 15px;
  }
</style>

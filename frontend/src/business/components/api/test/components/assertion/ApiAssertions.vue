<template>
  <div>
    <div class="assertion-add">
      <el-row :gutter="10">
        <el-col :span="4">
          <el-select :disabled="isReadOnly" class="assertion-item" v-model="type"
                     :placeholder="$t('api_test.request.assertions.select_type')"
                     size="small">
            <el-option :label="$t('api_test.request.assertions.text')" :value="options.TEXT"/>
            <el-option :label="$t('api_test.request.assertions.regex')" :value="options.REGEX"/>
            <el-option :label="'JSONPath'" :value="options.JSON_PATH"/>
            <el-option :label="'XPath'" :value="options.XPATH2"/>
            <el-option :label="$t('api_test.request.assertions.response_time')" :value="options.DURATION"/>
            <el-option :label="$t('api_test.request.assertions.jsr223')" :value="options.JSR223"/>
          </el-select>
        </el-col>
        <el-col :span="20">
          <ms-api-assertion-text :is-read-only="isReadOnly" :list="assertions.regex" v-if="type === options.TEXT"
                                 :callback="after"/>
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

    <div v-if="!scenario">
      <el-row :gutter="10" class="json-path-suggest-button">
        <el-button size="small" type="primary" @click="suggestJsonOpen">
          {{ $t('api_test.request.assertions.json_path_suggest') }}
        </el-button>
        <el-button size="small" type="danger" @click="clearJson">
          {{ $t('api_test.request.assertions.json_path_clear') }}
        </el-button>
      </el-row>
    </div>

    <ms-api-jsonpath-suggest-list @addJsonpathSuggest="addJsonpathSuggest" :request="request"
                                  ref="jsonpathSuggestList"/>

    <ms-api-assertions-edit :is-read-only="isReadOnly" :assertions="assertions"/>
  </div>
</template>

<script>
import MsApiAssertionText from "./ApiAssertionText";
import MsApiAssertionRegex from "./ApiAssertionRegex";
import MsApiAssertionDuration from "./ApiAssertionDuration";
import {ASSERTION_TYPE, Assertions, HttpRequest, JSONPath, Scenario} from "../../model/ScenarioModel";
import MsApiAssertionsEdit from "./ApiAssertionsEdit";
import MsApiAssertionJsonPath from "./ApiAssertionJsonPath";
import MsApiAssertionJsr223 from "@/business/components/api/test/components/assertion/ApiAssertionJsr223";
import MsApiJsonpathSuggestList from "./ApiJsonpathSuggestList";
import MsApiAssertionXPath2 from "./ApiAssertionXPath2";

export default {
  name: "MsApiAssertions",

  components: {
    MsApiAssertionXPath2,
    MsApiAssertionJsr223,
    MsApiJsonpathSuggestList,
    MsApiAssertionJsonPath,
    MsApiAssertionsEdit, MsApiAssertionDuration, MsApiAssertionRegex, MsApiAssertionText
  },

  props: {
    assertions: Assertions,
    request: HttpRequest,
    scenario: Scenario,
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
    }
  },

  methods: {
    after() {
      this.type = "";
    },
    suggestJsonOpen() {
      if (!this.request.debugRequestResult) {
        this.$message(this.$t('api_test.request.assertions.debug_first'));
        return;
      }
      this.$refs.jsonpathSuggestList.open();
    },
    addJsonpathSuggest(jsonPathList) {
      jsonPathList.forEach(jsonPath => {
        let jsonItem = new JSONPath();
        jsonItem.expression = jsonPath.json_path;
        jsonItem.expect = jsonPath.regular_expression;
        jsonItem.setJSONPathDescription();
        this.assertions.jsonPath.push(jsonItem);
      });
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
  border: #DCDFE6 solid 1px;
  margin: 5px 0;
  border-radius: 5px;
}

.bg-purple-dark {
  background: #99a9bf;
}

.bg-purple {
  background: #d3dce6;
}

.bg-purple-light {
  background: #e5e9f2;
}

.grid-content {
  border-radius: 4px;
  min-height: 36px;
}

.row-bg {
  padding: 10px 0;
  background-color: #f9fafc;
}

.json-path-suggest-button {
  text-align: right;
}

</style>

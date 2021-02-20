<template>
  <api-base-component
    @copy="copyRow"
    @remove="remove"
    @active="active"
    :data="extract"
    :draggable="draggable"
    color="#015478"
    background-color="#E6EEF2"
    :title="$t('api_test.definition.request.extract_param')">
    <div style="margin: 20px" v-loading="loading">
      <div class="extract-description">
        {{$t('api_test.request.extract.description')}}
      </div>
      <div class="extract-add">
        <el-row :gutter="10">
          <el-col :span="2">
            <el-select :disabled="isReadOnly" class="extract-item" v-model="type" :placeholder="$t('api_test.request.extract.select_type')"
                       size="small">
              <el-option :label="$t('api_test.request.extract.regex')" :value="options.REGEX"/>
              <el-option label="JSONPath" :value="options.JSON_PATH"/>
              <el-option label="XPath" :value="options.XPATH"/>
            </el-select>
          </el-col>
          <el-col :span="22">
            <ms-api-extract-common :is-read-only="isReadOnly" :extract-type="type" :list="list" v-if="type" :callback="after"/>
          </el-col>

          <el-button v-if="!type" :disabled="true" type="primary" size="small">{{$t('commons.add')}}</el-button>
        </el-row>
      </div>

      <api-json-path-suggest-button :open-tip="$t('api_test.request.extract.json_path_suggest')"
                                    :clear-tip="$t('api_test.request.extract.json_path_clear')" @open="suggestJsonOpen" @clear="clearJson"/>

      <ms-api-extract-edit :is-read-only="isReadOnly" :reloadData="reloadData" :extract="extract"/>
    </div>
    <ms-api-jsonpath-suggest :tip="$t('api_test.request.extract.suggest_tip')" @addSuggest="addJsonPathSuggest" ref="jsonpathSuggest"/>
  </api-base-component>
</template>

<script>
  import {EXTRACT_TYPE} from "../../model/ApiTestModel";
  import MsApiExtractEdit from "./ApiExtractEdit";
  import MsApiExtractCommon from "./ApiExtractCommon";
  import {getUUID} from "@/common/js/utils";
  import ApiJsonPathSuggestButton from "../assertion/ApiJsonPathSuggestButton";
  import MsApiJsonpathSuggest from "../assertion/ApiJsonpathSuggest";
  import {ExtractJSONPath} from "../../../test/model/ScenarioModel";
  import ApiBaseComponent from "../../../automation/scenario/common/ApiBaseComponent";

  export default {
    name: "MsApiExtract",
    components: {
      ApiBaseComponent,
      MsApiJsonpathSuggest,
      ApiJsonPathSuggestButton,
      MsApiExtractCommon,
      MsApiExtractEdit,
    },
    props: {
      extract: {},
      response: {},
      node: {},
      customizeStyle: {
        type: String,
        default: "margin-top: 10px"
      },
      isReadOnly: {
        type: Boolean,
        default: false
      },
      draggable: {
        type: Boolean,
        default: false,
      }
    },
    data() {
      return {
        options: EXTRACT_TYPE,
        type: "",
        reloadData: "",
        loading: false,
      }
    },
    methods: {
      after() {
        this.type = "";
        this.reloadData = getUUID().substring(0, 8);
      },
      remove() {
        this.$emit('remove', this.extract, this.node);
      },
      copyRow() {
        this.$emit('copyRow', this.extract, this.node);
      },
      reload() {
        this.loading = true
        this.$nextTick(() => {
          this.loading = false
        })
      },
      active() {
        this.extract.active = !this.extract.active;
        this.reload();
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
      addJsonPathSuggest(data) {
        let option = {};
        option.variable = data.key;
        option.expression = data.path;
        this.extract.json.push(new ExtractJSONPath(option));
      },
      clearJson() {
        this.extract.json = [];
      }
    },
    computed: {
      list() {
        switch (this.type) {
          case EXTRACT_TYPE.REGEX:
            return this.extract.regex;
          case EXTRACT_TYPE.JSON_PATH:
            return this.extract.json;
          case EXTRACT_TYPE.XPATH:
            return this.extract.xpath;
          default:
            return [];
        }
      }
    }
  }
</script>

<style scoped>
  .extract-description {
    font-size: 13px;
    margin-bottom: 10px;
  }

  .extract-item {
    width: 100%;
  }

  .extract-add {
    padding: 10px;
    border: #DCDFE6 solid 1px;
    margin: 5px 0;
    border-radius: 5px;
  }

  .icon.is-active {
    transform: rotate(90deg);
  }

  /deep/ .el-card__body {
    padding: 15px;
  }
</style>

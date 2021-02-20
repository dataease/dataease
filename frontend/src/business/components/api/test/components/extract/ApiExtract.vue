<template>
  <div>
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

        <el-button v-if="!type" :disabled="true" type="primary" size="small">Add</el-button>
      </el-row>
    </div>

    <ms-api-extract-edit :is-read-only="isReadOnly" :extract="extract"/>
  </div>
</template>

<script>
  import {EXTRACT_TYPE, Extract} from "../../model/ScenarioModel";
  import MsApiExtractEdit from "./ApiExtractEdit";
  import MsApiExtractCommon from "./ApiExtractCommon";

  export default {
    name: "MsApiExtract",

    components: {
      MsApiExtractCommon,
      MsApiExtractEdit,
    },

    props: {
      extract: Extract,
      isReadOnly: {
        type: Boolean,
        default: false
      }
    },

    data() {
      return {
        options: EXTRACT_TYPE,
        type: "",
      }
    },

    methods: {
      after() {
        this.type = "";
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
</style>

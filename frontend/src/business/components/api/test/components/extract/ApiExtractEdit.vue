<template>
  <div>
    <div class="extract-item-editing regex" v-if="extract.regex.length > 0">
      <div>
        {{$t("api_test.request.extract.regex")}}
      </div>
      <div class="regex-item" v-for="(regex, index) in extract.regex" :key="index">
        <ms-api-extract-common :is-read-only="isReadOnly" :extract-type="type.REGEX" :list="extract.regex" :common="regex"
                               :edit="true" :index="index"/>
      </div>
    </div>

    <div class="extract-item-editing json" v-if="extract.json.length > 0">
      <div>
        JSONPath
      </div>
      <div class="regex-item" v-for="(json, index) in extract.json" :key="index">
        <ms-api-extract-common :is-read-only="isReadOnly" :extract-type="type.JSON_PATH" :list="extract.json" :common="json"
                               :edit="true" :index="index"/>
      </div>
    </div>

    <div class="extract-item-editing xpath" v-if="extract.xpath.length > 0">
      <div>
        XPath
      </div>
      <div class="regex-item" v-for="(xpath, index) in extract.xpath" :key="index">
        <ms-api-extract-common :is-read-only="isReadOnly" :extract-type="type.XPATH" :list="extract.xpath" :common="xpath"
                               :edit="true" :index="index"/>
      </div>
    </div>
  </div>

</template>

<script>
  import {Extract, EXTRACT_TYPE} from "../../model/ScenarioModel";
  import MsApiExtractCommon from "./ApiExtractCommon";

  export default {
    name: "MsApiExtractEdit",

    components: {MsApiExtractCommon},

    props: {
      extract: Extract,
      isReadOnly: {
        type: Boolean,
        default: false
      }
    },

    data() {
      return {
        type: EXTRACT_TYPE
      }
    }
  }
</script>

<style scoped>
  .extract-item-editing {
    padding-left: 10px;
    margin-top: 10px;
  }

  .extract-item-editing.regex {
    border-left: 2px solid #7B0274;
  }

  .extract-item-editing.json {
    border-left: 2px solid #44B3D2;
  }

  .extract-item-editing.xpath {
    border-left: 2px solid #E6A200;
  }

  .regex-item {
    margin-top: 10px;
  }


</style>

<template>
  <div>
    <div class="assertion-item-editing regex" v-if="assertions.regex.length > 0">
      <div>
        {{ $t("api_test.request.assertions.regex") }}
      </div>
      <div class="regex-item" v-for="(regex, index) in assertions.regex" :key="index">
        <ms-api-assertion-regex :is-read-only="isReadOnly" :list="assertions.regex"
                                :regex="regex" :edit="true" :index="index"/>
      </div>
    </div>

    <div class="assertion-item-editing json_path" v-if="assertions.jsonPath.length > 0">
      <div>
        {{ 'JSONPath' }}
      </div>
      <div class="regex-item" v-for="(jsonPath, index) in assertions.jsonPath" :key="index">
        <ms-api-assertion-json-path :is-read-only="isReadOnly" :list="assertions.jsonPath"
                                    :json-path="jsonPath" :edit="true" :index="index"/>
      </div>
    </div>

    <div class="assertion-item-editing x_path" v-if="assertions.xpath2.length > 0">
      <div>
        {{ 'XPath' }}
      </div>
      <div class="regex-item" v-for="(xPath, index) in assertions.xpath2" :key="index">
        <ms-api-assertion-x-path2 :is-read-only="isReadOnly" :list="assertions.xpath2"
                                 :x-path2="xPath" :edit="true" :index="index"/>
      </div>
    </div>

    <div class="assertion-item-editing jsr223" v-if="assertions.jsr223.length > 0">
      <div>
        {{ $t("api_test.request.assertions.script") }}
      </div>
      <div class="regex-item" v-for="(assertion, index) in assertions.jsr223" :key="index">
        <ms-api-assertion-jsr223 :is-read-only="isReadOnly" :list="assertions.jsr223"
                                 :assertion="assertion" :edit="true" :index="index"/>
      </div>
    </div>

    <div class="assertion-item-editing response-time" v-if="isShow">
      <div>
        {{ $t("api_test.request.assertions.response_time") }}
      </div>
      <ms-api-assertion-duration :is-read-only="isReadOnly" v-model="assertions.duration.value"
                                 :duration="assertions.duration" :edit="true"/>
    </div>
  </div>

</template>

<script>
import MsApiAssertionRegex from "./ApiAssertionRegex";
import MsApiAssertionDuration from "./ApiAssertionDuration";
import {Assertions} from "../../model/ScenarioModel";
import MsApiAssertionJsonPath from "./ApiAssertionJsonPath";
import MsApiAssertionJsr223 from "@/business/components/api/test/components/assertion/ApiAssertionJsr223";
import MsApiAssertionXPath2 from "./ApiAssertionXPath2";

export default {
  name: "MsApiAssertionsEdit",

  components: {
    MsApiAssertionXPath2,
    MsApiAssertionJsr223, MsApiAssertionJsonPath, MsApiAssertionDuration, MsApiAssertionRegex},

  props: {
    assertions: Assertions,
    isReadOnly: {
      type: Boolean,
      default: false
    }
  },

  computed: {
    isShow() {
      let rt = this.assertions.duration;
      return rt.value !== undefined;
    }
  }
}
</script>

<style scoped>
.assertion-item-editing {
  padding-left: 10px;
  margin-top: 10px;
}

.assertion-item-editing.regex {
  border-left: 2px solid #7B0274;
}

.assertion-item-editing.json_path {
  border-left: 2px solid #44B3D2;
}

.assertion-item-editing.response-time {
  border-left: 2px solid #DD0240;
}

.assertion-item-editing.jsr223 {
  border-left: 2px solid #1FDD02;
}

.assertion-item-editing.x_path {
  border-left: 2px solid #fca130;
}

.regex-item {
  margin-top: 10px;
}


</style>

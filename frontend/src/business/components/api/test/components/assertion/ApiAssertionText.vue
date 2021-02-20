<template>
  <div>
    <el-row :gutter="10" type="flex" justify="space-between" align="middle">
      <el-col class="assertion-select">
        <el-select :disabled="isReadOnly" class="assertion-item" v-model="subject" size="small"
                   :placeholder="$t('api_test.request.assertions.select_subject')">
          <el-option label="Response Code" :value="subjects.RESPONSE_CODE"/>
          <el-option label="Response Headers" :value="subjects.RESPONSE_HEADERS"/>
          <el-option label="Response Data" :value="subjects.RESPONSE_DATA"/>
        </el-select>
      </el-col>
      <el-col class="assertion-select">
        <el-select :disabled="isReadOnly" class="assertion-item" v-model="condition" size="small"
                   :placeholder="$t('api_test.request.assertions.select_condition')">
          <el-option :label="$t('api_test.request.assertions.contains')" value="CONTAINS"/>
          <el-option :label="$t('api_test.request.assertions.not_contains')" value="NOT_CONTAINS"/>
          <el-option :label="$t('api_test.request.assertions.equals')" value="EQUALS"/>
          <el-option :label="$t('api_test.request.assertions.start_with')" value="START_WITH"/>
          <el-option :label="$t('api_test.request.assertions.end_with')" value="END_WITH"/>
        </el-select>
      </el-col>
      <el-col>
        <el-input :disabled="isReadOnly" v-model="value" maxlength="200" size="small" show-word-limit
                  :placeholder="$t('api_test.request.assertions.value')"/>
      </el-col>
      <el-col class="assertion-checkbox">
        <el-checkbox v-model="assumeSuccess" :disabled="isReadOnly">
          {{ $t('api_test.request.assertions.ignore_status') }}
        </el-checkbox>
      </el-col>
      <el-col class="assertion-btn">
        <el-button :disabled="isReadOnly" type="primary" size="small" @click="add">
          {{ $t('api_test.request.assertions.add') }}
        </el-button>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import {Regex, ASSERTION_REGEX_SUBJECT} from "../../model/ScenarioModel";

export default {
  name: "MsApiAssertionText",

  props: {
    list: Array,
    callback: Function,
    isReadOnly: {
      type: Boolean,
      default: false
    }
  },

  data() {
    return {
      subjects: ASSERTION_REGEX_SUBJECT,
      subject: "",
      condition: "",
      assumeSuccess: false,
      value: ""
    }
  },

  methods: {
    add: function () {
      this.list.push(this.toRegex());
      this.callback();
    },
    toRegex: function () {
      let expression = "";
      let description = this.subject;
      switch (this.condition) {
        case "CONTAINS":
          expression = ".*" + this.value + ".*";
          description += " contains: " + this.value;
          break;
        case "NOT_CONTAINS":
          expression = "(?s)^((?!" + this.value + ").)*$";
          description += " not contains: " + this.value;
          break;
        case "EQUALS":
          expression = "^" + this.value + "$";
          description += " equals: " + this.value;
          break;
        case "START_WITH":
          expression = "^" + this.value;
          description += " start with: " + this.value;
          break;
        case "END_WITH":
          expression = this.value + "$";
          description += " end with: " + this.value;
          break;
      }
      return new Regex({
          subject: this.subject,
          expression: expression,
          description: description,
          assumeSuccess: this.assumeSuccess
        }
      );
    }
  }
}
</script>

<style scoped>
.assertion-select {
  width: 250px;
}

.assertion-item {
  width: 100%;
}

.assertion-checkbox {
  text-align: center;
  width: 120px;
}

.assertion-btn {
  width: 60px;
}
</style>

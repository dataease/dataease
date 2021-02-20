<template>
  <div>
    <el-row :gutter="10" type="flex" justify="space-between" align="middle">
      <el-col>
        <el-input :disabled="isReadOnly" v-model="jsonPath.expression" maxlength="200" size="small" show-word-limit
                  :placeholder="$t('api_test.request.extract.json_path_expression')"/>
      </el-col>
      <el-col>
        <el-input :disabled="isReadOnly" v-model="jsonPath.expect" size="small" show-word-limit
                  :placeholder="$t('api_test.request.assertions.expect')"/>
      </el-col>
      <el-col class="assertion-btn">
        <el-button :disabled="isReadOnly" type="danger" size="mini" icon="el-icon-delete" circle @click="remove" v-if="edit"/>
        <el-button :disabled="isReadOnly" type="primary" size="small" @click="add" v-else>
          {{ $t('api_test.request.assertions.add') }}
        </el-button>
      </el-col>
    </el-row>
  </div>
</template>

<script>
  import {JSONPath} from "../../model/ScenarioModel";

  export default {
    name: "MsApiAssertionJsonPath",

    props: {
      jsonPath: {
        type: JSONPath,
        default: () => {
          return new JSONPath();
        }
      },
      edit: {
        type: Boolean,
        default: false
      },
      index: Number,
      list: Array,
      callback: Function,
      isReadOnly: {
        type: Boolean,
        default: false
      }
    },

    data() {
      return {
      }
    },

    watch: {
      'jsonPath.expect'() {
        this.setJSONPathDescription();
      },
      'jsonPath.expression'() {
        this.setJSONPathDescription();
      }
    },

    methods: {
      add: function () {
        this.list.push(this.getJSONPath());
        this.callback();
      },
      remove: function () {
        this.list.splice(this.index, 1);
      },
      getJSONPath() {
        let jsonPath = new JSONPath(this.jsonPath);
        jsonPath.description = jsonPath.expression + " expect: " + (jsonPath.expect ? jsonPath.expect : '');
        return jsonPath;
      },
      setJSONPathDescription() {
        this.jsonPath.description = this.jsonPath.expression + " expect: " + (this.jsonPath.expect ? this.jsonPath.expect : '');
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

  .assertion-btn {
    text-align: center;
    width: 60px;
  }
</style>

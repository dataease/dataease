<template>
  <div>
    <span class="kv-description" v-if="description">
      {{ description }}
    </span>

    <div class="kv-row" v-for="(item, index) in parameters" :key="index">
      <el-row type="flex" :gutter="20" justify="space-between" align="middle">

        <el-col class="kv-checkbox">
          <input type="checkbox" v-if="!isDisable(index)" v-model="item.enable"
                 :disabled="isReadOnly"/>
        </el-col>
        <el-col>

          <el-input v-if="!suggestions" :disabled="isReadOnly" v-model="item.name" size="small" maxlength="200"
                    @change="change" :placeholder="keyText" show-word-limit>
            <template v-slot:prepend>
              <el-select v-if="type === 'body'" :disabled="isReadOnly" class="kv-type" v-model="item.type" @change="typeChange(item)">
                <el-option value="text"/>
                <el-option value="file"/>
              </el-select>
            </template>
          </el-input>

          <el-autocomplete :disabled="isReadOnly" v-if="suggestions" v-model="item.name" size="small"
                           :fetch-suggestions="querySearch" @change="change" :placeholder="keyText" show-word-limit/>

        </el-col>
        <el-col v-if="item.type !== 'file'">
          <el-autocomplete
            :disabled="isReadOnly"
            size="small"
            class="input-with-autocomplete"
            v-model="item.value"
            :fetch-suggestions="funcSearch"
            :placeholder="valueText"
            value-key="name"
            highlight-first-item
            @select="change">
            <i slot="suffix" class="el-input__icon el-icon-edit pointer" @click="advanced(item)"></i>
          </el-autocomplete>
        </el-col>

        <el-col v-if="item.type === 'file'">
          <ms-api-body-file-upload :parameter="item"/>
        </el-col>

        <el-col v-if="type === 'body'">
          <el-input :disabled="isReadOnly" v-model="item.contentType" size="small" maxlength="100"
                    @change="change" :placeholder="$t('api_test.request.content_type')" show-word-limit>
          </el-input>
        </el-col>

        <el-col class="kv-delete">
          <el-button size="mini" class="el-icon-delete-solid" circle @click="remove(index)"
                     :disabled="isDisable(index) || isReadOnly"/>
        </el-col>
      </el-row>
    </div>
    <ms-api-variable-advance ref="variableAdvance" :environment="environment" :scenario="scenario"
                             :parameters="parameters"
                             :current-item="currentItem"/>
  </div>
</template>

<script>
  import {KeyValue, Scenario} from "../model/ScenarioModel";
  import {JMETER_FUNC, MOCKJS_FUNC} from "@/common/js/constants";
  import MsApiVariableAdvance from "@/business/components/api/test/components/ApiVariableAdvance";
  import MsApiBodyFileUpload from "./body/ApiBodyFileUpload";

  export default {
    name: "MsApiVariable",
    components: {MsApiBodyFileUpload, MsApiVariableAdvance},
    props: {
      keyPlaceholder: String,
      valuePlaceholder: String,
      description: String,
      parameters: Array,
      environment: Object,
      scenario: Scenario,
      type: {
        type: String,
        default: ''
      },
      isReadOnly: {
        type: Boolean,
        default: false
      },
      suggestions: Array
    },
    data() {
      return {
        currentItem: null,
      }
    },
    computed: {
      keyText() {
        return this.keyPlaceholder || this.$t("api_test.key");
      },
      valueText() {
        return this.valuePlaceholder || this.$t("api_test.value");
      }
    },
    methods: {
      remove: function (index) {
        // 移除整行输入控件及内容
        this.parameters.splice(index, 1);
        this.$emit('change', this.parameters);
      },
      change: function () {
        let isNeedCreate = true;
        let removeIndex = -1;
        this.parameters.forEach((item, index) => {
          if (!item.name && !item.value) {
            // 多余的空行
            if (index !== this.parameters.length - 1) {
              removeIndex = index;
            }
            // 没有空行，需要创建空行
            isNeedCreate = false;
          }
        });
        if (isNeedCreate) {
          this.parameters.push(new KeyValue({type: 'text', enable: true, uuid: this.uuid(), contentType: 'text/plain'}));
        }
        this.$emit('change', this.parameters);
        // TODO 检查key重复
      },
      isDisable: function (index) {
        return this.parameters.length - 1 === index;
      },
      querySearch(queryString, cb) {
        let suggestions = this.suggestions;
        let results = queryString ? suggestions.filter(this.createFilter(queryString)) : suggestions;
        cb(results);
      },
      createFilter(queryString) {
        return (restaurant) => {
          return (restaurant.value.toLowerCase().indexOf(queryString.toLowerCase()) === 0);
        };
      },
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
      uuid: function () {
        return (((1 + Math.random()) * 0x100000) | 0).toString(16).substring(1);
      },
      advanced(item) {
        this.$refs.variableAdvance.open();
        this.currentItem = item;
      },
      typeChange(item) {
        if (item.type === 'file') {
          item.contentType = 'application/octet-stream';
        } else {
          item.contentType = 'text/plain';
        }
      }
    },
    created() {
      if (this.parameters.length === 0 || this.parameters[this.parameters.length - 1].name) {
        this.parameters.push(new KeyValue( {type: 'text', enable: true, uuid: this.uuid(), contentType: 'text/plain'}));
      }
    }
  }
</script>

<style scoped>
  .kv-description {
    font-size: 13px;
  }

  .kv-row {
    margin-top: 10px;
  }

  .kv-delete {
    width: 60px;
  }

  .el-autocomplete {
    width: 100%;
  }

  .kv-checkbox {
    width: 20px;
    margin-right: 10px;
  }

  .advanced-item-value >>> .el-dialog__body {
    padding: 15px 25px;
  }

  .el-row {
    margin-bottom: 5px;
  }

  .kv-type {
    width: 70px;
  }

  .pointer {
    cursor: pointer;
    color: #1E90FF;
  }
</style>

<template>
  <div>
    <el-radio-group v-model="body.type" size="mini">
      <el-radio :disabled="isReadOnly" :label="type.FORM_DATA" @change="modeChange">
        {{ $t('api_test.definition.request.body_form_data') }}
      </el-radio>

      <el-radio :disabled="isReadOnly" :label="type.WWW_FORM" @change="modeChange">
        {{ $t('api_test.definition.request.body_x_www_from_urlencoded') }}
      </el-radio>

      <el-radio :disabled="isReadOnly" :label="type.JSON" @change="modeChange">
        {{ $t('api_test.definition.request.body_json') }}
      </el-radio>

      <el-radio :disabled="isReadOnly" :label="type.XML" @change="modeChange">
        {{ $t('api_test.definition.request.body_xml') }}
      </el-radio>

      <el-radio :disabled="isReadOnly" :label="type.RAW" @change="modeChange">
        {{ $t('api_test.definition.request.body_raw') }}
      </el-radio>

      <el-radio :disabled="isReadOnly" :label="type.BINARY" @change="modeChange">
        {{ $t('api_test.definition.request.body_binary') }}
      </el-radio>
    </el-radio-group>
    <div style="min-width: 1200px;" v-if="body.type == 'Form Data' || body.type == 'WWW_FORM'">
      <el-row v-if="body.type == 'Form Data' || body.type == 'WWW_FORM'">
        <el-link class="ms-el-link" @click="batchAdd"> {{$t("commons.batch_add")}}</el-link>
      </el-row>
      <ms-api-variable :is-read-only="isReadOnly"
                       :parameters="body.kvs"
                       :isShowEnable="isShowEnable" type="body"/>
    </div>
    <div v-if="body.type == 'JSON'">
      <div style="padding: 10px">
        <el-switch active-text="JSON-SCHEMA" v-model="body.format" active-value="JSON-SCHEMA"/>
      </div>
      <ms-json-code-edit v-if="body.format==='JSON-SCHEMA'" :body="body" ref="jsonCodeEdit"/>
      <ms-code-edit v-else :read-only="isReadOnly" height="400px" :data.sync="body.raw" :modes="modes" :mode="'json'" ref="codeEdit"/>
    </div>

    <div class="ms-body" v-if="body.type == 'XML'">
      <ms-code-edit :read-only="isReadOnly" :data.sync="body.raw" :modes="modes" :mode="'xml'" ref="codeEdit"/>
    </div>

    <div class="ms-body" v-if="body.type == 'Raw'">
      <ms-code-edit :read-only="isReadOnly" :data.sync="body.raw" :modes="modes" ref="codeEdit"/>
    </div>

    <ms-api-binary-variable :is-read-only="isReadOnly"
                            :parameters="body.binary"
                            :isShowEnable="isShowEnable"
                            type="body"
                            v-if="body.type == 'BINARY'"/>

    <batch-add-parameter @batchSave="batchSave" ref="batchAddParameter"/>

  </div>
</template>

<script>
  import MsApiKeyValue from "../ApiKeyValue";
  import {BODY_TYPE, KeyValue} from "../../model/ApiTestModel";
  import MsCodeEdit from "../../../../common/components/MsCodeEdit";
  import MsJsonCodeEdit from "../../../../common/json-schema/JsonSchemaEditor";
  import MsDropdown from "../../../../common/components/MsDropdown";
  import MsApiVariable from "../ApiVariable";
  import MsApiBinaryVariable from "./ApiBinaryVariable";
  import MsApiFromUrlVariable from "./ApiFromUrlVariable";
  import BatchAddParameter from "../basis/BatchAddParameter";

  export default {
    name: "MsApiBody",
    components: {
      MsApiVariable,
      MsDropdown,
      MsCodeEdit,
      MsApiKeyValue,
      MsApiBinaryVariable,
      MsApiFromUrlVariable,
      MsJsonCodeEdit,
      BatchAddParameter
    },
    props: {
      body: {},
      headers: Array,
      isReadOnly: {
        type: Boolean,
        default: false
      },
      isShowEnable: {
        type: Boolean,
        default: true
      }
    },
    data() {
      return {
        type: BODY_TYPE,
        modes: ['text', 'json', 'xml', 'html'],
        jsonSchema: "JSON",
      };
    },
    methods: {
      modeChange(mode) {
        switch (this.body.type) {
          case "JSON":
            this.setContentType("application/json");
            break;
          case "XML":
            this.setContentType("text/xml");
            break;
          case "WWW_FORM":
            this.setContentType("application/x-www-form-urlencoded");
            break;
          // todo from data
          case "BINARY":
            this.setContentType("application/octet-stream");
            break;
          default:
            this.removeContentType();
            break;
        }
      },
      setContentType(value) {
        let isType = false;
        this.headers.forEach(item => {
          if (item.name === "Content-Type") {
            item.value = value;
            isType = true;
          }
        })
        if (!isType) {
          this.headers.unshift(new KeyValue({name: "Content-Type", value: value}));
          this.$emit('headersChange');
        }
      },
      removeContentType() {
        for (let index in this.headers) {
          if (this.headers[index].name === "Content-Type") {
            this.headers.splice(index, 1);
            this.$emit('headersChange');
            return;
          }
        }
      },
      batchAdd() {
        this.$refs.batchAddParameter.open();
      },
      batchSave(data) {
        if (data) {
          let params = data.split("\n");
          let keyValues = [];
          params.forEach(item => {
            let line = item.split(/，|,/);
            let required = false;
            if (line[1] === '必填' || line[1] === 'true') {
              required = true;
            }
            keyValues.push(new KeyValue({name: line[0], required: required, value: line[2], description: line[3], type: "text", valid: false, file: false, encode: true, enable: true, contentType: "text/plain"}));
          })
          keyValues.forEach(item => {
            this.body.kvs.unshift(item);
          })
        }
      },

    },
    created() {
      if (!this.body.type) {
        this.body.type = BODY_TYPE.FORM_DATA;
      }
      if (this.body.kvs) {
        this.body.kvs.forEach(param => {
          if (!param.type) {
            param.type = 'text';
          }
        });
      }
    }
  }
</script>

<style scoped>
  .textarea {
    margin-top: 10px;
  }

  .ms-body {
    padding: 15px 0;
    height: 400px;
  }

  .el-dropdown {
    margin-left: 20px;
    line-height: 30px;
  }

  .ace_editor {
    border-radius: 5px;
  }

  .el-radio-group {
    margin: 10px 10px;
    margin-top: 15px;
  }

  .ms-el-link {
    float: right;
    margin-right: 45px;
  }
</style>

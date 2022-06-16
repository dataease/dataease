<template>
  <div>
    <el-radio-group v-model="body.type" size="mini">
      <el-radio :disabled="isReadOnly" :label="type.FORM_DATA" @change="modeChange">
        {{ $t('datasource.body_form_data') }}
      </el-radio>

      <el-radio :disabled="isReadOnly" :label="type.WWW_FORM" @change="modeChange">
        {{ $t('datasource.body_x_www_from_urlencoded') }}
      </el-radio>

      <el-radio :disabled="isReadOnly" :label="type.JSON" @change="modeChange">
        {{ $t('datasource.body_json') }}
      </el-radio>

      <el-radio :disabled="isReadOnly" :label="type.XML" @change="modeChange">
        {{ $t('datasource.body_xml') }}
      </el-radio>

      <el-radio :disabled="isReadOnly" :label="type.RAW" @change="modeChange">
        {{ $t('datasource.body_raw') }}
      </el-radio>

    </el-radio-group>
    <div style="min-width: 1200px;" v-if="body.type == 'Form_Data' || body.type == 'WWW_FORM'">
      <api-variable
        :with-mor-setting="true"
        :is-read-only="isReadOnly"
        :parameters="body.kvs"
        :isShowEnable="isShowEnable"
        type="body"/>
    </div>
    <div v-if="body.type == 'JSON'">
      <code-edit
        :read-only="isReadOnly"
        :data.sync="body.raw"
        :modes="modes"
        :mode="'json'"
        height="400px"
        ref="codeEdit"/>
    </div>

    <div class="ms-body" v-if="body.type == 'XML'">
      <code-edit
        :read-only="isReadOnly"
        :data.sync="body.raw"
        :modes="modes"
        :mode="'text'"
        ref="codeEdit"/>
    </div>

    <div class="ms-body" v-if="body.type == 'Raw'">
      <code-edit
        :read-only="isReadOnly"
        :data.sync="body.raw"
        :modes="modes"
        ref="codeEdit"/>
    </div>
  </div>
</template>

<script>
import ApiKeyValue from "./ApiKeyValue";
import {BODY_TYPE, KeyValue} from "./ApiTestModel";
import CodeEdit from "./CodeEdit";
import ApiVariable from "./ApiVariable";
import Convert from "./convert";


export default {
  name: "ApiBody",
  components: {
    ApiVariable,
    CodeEdit,
    ApiKeyValue
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
      default: false
    }
  },
  data() {
    return {
      type: BODY_TYPE,
      modes: ['text', 'json', 'xml', 'html'],
      jsonSchema: "JSON",
      codeEditActive: true,
      hasOwnProperty: Object.prototype.hasOwnProperty,
      propIsEnumerable: Object.prototype.propertyIsEnumerable
    };
  },

  watch: {
    'body.typeChange'() {
      this.reloadCodeEdit();
    },
    'body.raw'() {
      if (this.body.format !== 'JSON-SCHEMA' && this.body.raw) {
        try {
          const MsConvert = new Convert();
          let data = MsConvert.format(JSON.parse(this.body.raw));
          if (this.body.jsonSchema) {
            this.body.jsonSchema = this.deepAssign(data);
          } else {
            this.body.jsonSchema = data;
          }
        } catch (ex) {
          this.body.jsonSchema = "";
        }
      }
    },
  },
  methods: {
    isObj(x) {
      let type = typeof x;
      return x !== null && (type === 'object' || type === 'function');
    },

    toObject(val) {
      if (val === null || val === undefined) {
        return;
      }
      return Object(val);
    },

    assignKey(to, from, key) {
      let val = from[key];
      if (val === undefined || val === null) {
        return;
      }

      if (!this.hasOwnProperty.call(to, key) || !this.isObj(val)) {
        to[key] = val;
      } else {
        to[key] = this.assign(Object(to[key]), from[key]);
      }
    },

    assign(to, from) {
      if (to === from) {
        return to;
      }
      from = Object(from);
      for (let key in from) {
        if (this.hasOwnProperty.call(from, key)) {
          this.assignKey(to, from, key);
        }
      }

      if (Object.getOwnPropertySymbols) {
        let symbols = Object.getOwnPropertySymbols(from);

        for (let i = 0; i < symbols.length; i++) {
          if (this.propIsEnumerable.call(from, symbols[i])) {
            this.assignKey(to, from, symbols[i]);
          }
        }
      }

      return to;
    },

    deepAssign(target) {
      target = this.toObject(target);
      for (let s = 1; s < arguments.length; s++) {
        this.assign(target, arguments[s]);
      }
      return target;
    },
    reloadCodeEdit() {
      this.codeEditActive = false;
      this.$nextTick(() => {
        this.codeEditActive = true;
      });
    },
    formatChange() {
      const MsConvert = new Convert();

      if (this.body.format === 'JSON-SCHEMA') {
        if (this.body.raw && !this.body.jsonSchema) {
          this.body.jsonSchema = MsConvert.format(JSON.parse(this.body.raw));
        }
      } else {
        if (this.body.jsonSchema) {
          MsConvert.schemaToJsonStr(this.body.jsonSchema, (result) => {
            this.$set(this.body, 'raw', result);
            this.reloadCodeEdit();
          });
        }
      }
    },
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
    format(array, obj) {
      if (array) {
        let isAdd = true;
        for (let i in array) {
          let item = array[i];
          if (item.name === obj.name) {
            item.value = obj.value;
            isAdd = false;
          }
        }
        if (isAdd) {
          this.body.kvs.unshift(obj);
        }
      }
    },
    batchSave(data) {
      if (data) {
        let params = data.split("\n");
        let keyValues = [];
        params.forEach(item => {
          let line = [];
          line[0] = item.substring(0, item.indexOf(":"));
          line[1] = item.substring(item.indexOf(":") + 1, item.length);
          let required = false;
          keyValues.unshift(new KeyValue({
            name: line[0],
            required: required,
            value: line[1],
            description: line[2],
            type: "text",
            valid: false,
            file: false,
            encode: true,
            enable: true,
            contentType: "text/plain"
          }));
        })
        keyValues.forEach(item => {
          this.format(this.body.kvs, item);
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

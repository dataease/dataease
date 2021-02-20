<template>
  <div>
    <el-radio-group v-model="body.type" size="mini">
      <el-radio-button :disabled="isReadOnly" :label="type.KV">
        {{ $t('api_test.request.body_kv') }}
      </el-radio-button>
      <el-radio-button :disabled="isReadOnly" :label="type.RAW">
        {{ $t('api_test.request.body_text') }}
      </el-radio-button>
    </el-radio-group>

    <ms-dropdown :default-command="body.format" v-if="body.type == 'Raw'" :commands="modes" @command="modeChange"/>

    <ms-api-variable :is-read-only="isReadOnly"
                     :parameters="body.kvs"
                     :environment="environment"
                     :scenario="scenario"
                     :extract="extract"
                     type="body"
                     v-if="body.isKV()"/>
    <div class="body-raw" v-if="body.type == 'Raw'">
      <ms-code-edit v-if="isCodeEditAlive" :mode="body.format" :read-only="isReadOnly" :data.sync="body.raw" :modes="modes" ref="codeEdit"/>
    </div>

  </div>
</template>

<script>
import MsApiKeyValue from "../ApiKeyValue";
import {Body, BODY_FORMAT, BODY_TYPE, Scenario} from "../../model/ScenarioModel";
import MsCodeEdit from "../../../../common/components/MsCodeEdit";
import MsDropdown from "../../../../common/components/MsDropdown";
import MsApiVariable from "@/business/components/api/test/components/ApiVariable";

export default {
  name: "MsApiBody",
  components: {MsApiVariable, MsDropdown, MsCodeEdit, MsApiKeyValue},
  props: {
    body: Body,
    scenario: Scenario,
    environment: Object,
    extract: Object,
    isReadOnly: {
      type: Boolean,
      default: false
    },
  },

  data() {
    return {
      type: BODY_TYPE,
      modes: ['text', 'json', 'xml', 'html'],
      isCodeEditAlive: true
    };
  },

  methods: {
    modeChange(mode) {
      this.body.format = mode;
      this.reload();
    },
    reload() {
      this.isCodeEditAlive = false;
      this.$nextTick(() => (this.isCodeEditAlive = true));
    },
  },

  created() {
    if (!this.body.type) {
      this.body.type = BODY_TYPE.KV;
    }
    if (!this.body.format) {
      this.body.format = BODY_FORMAT.TEXT;
    }
    this.body.kvs.forEach(param => {
      if (!param.type) {
        param.type = 'text';
      }
    });
  }
}
</script>

<style scoped>
.textarea {
  margin-top: 10px;
}

.body-raw {
  padding: 15px 0;
  height: 300px;
}

.el-dropdown {
  margin-left: 20px;
  line-height: 30px;
}

.ace_editor {
  border-radius: 5px;
}

</style>

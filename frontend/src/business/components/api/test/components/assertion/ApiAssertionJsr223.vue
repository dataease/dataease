<template>
  <div>
    <el-row type="flex" align="middle" v-if="!edit">
      <div class="assertion-item label">
        {{ assertion.desc }}
      </div>
      <div class="assertion-item btn">
        <el-button :disabled="isReadOnly" type="success" size="small" @click="detail">
          {{ $t('commons.edit') }}
        </el-button>
        <el-button :disabled="isReadOnly" type="primary" size="small" @click="add">
          {{ $t('api_test.request.assertions.add') }}
        </el-button>
      </div>
    </el-row>
    <el-row type="flex" justify="space-between" align="middle" v-else>
      <div class="assertion-item label">
        {{ assertion.desc }}
      </div>
      <div class="assertion-item btn circle">
        <el-button :disabled="isReadOnly" type="success" size="mini" icon="el-icon-edit" circle @click="detail"/>
        <el-button :disabled="isReadOnly" type="danger" size="mini" icon="el-icon-delete" circle @click="remove"/>
      </div>
    </el-row>

    <el-dialog :title="$t('api_test.request.assertions.script')" :visible.sync="visible" width="900px">
      <el-row type="flex" justify="space-between" align="middle" class="quick-script-block">
        <div class="assertion-item input">
          <el-input size="small" v-model="assertion.variable"
                    :placeholder="$t('api_test.request.assertions.variable_name')" @change="quickScript"/>
        </div>

        <div class="assertion-item select">
          <el-select v-model="assertion.operator" :placeholder="$t('commons.please_select')" size="small"
                     @change="changeOperator">
            <el-option v-for="o in operators" :key="o.value" :label="$t(o.label)" :value="o.value"/>
          </el-select>
        </div>
        <div class="assertion-item input">
          <el-input size="small" v-model="assertion.value" :placeholder="$t('api_test.value')"
                    @change="quickScript" v-if="!hasEmptyOperator"/>
        </div>
      </el-row>
      <el-input size="small" v-model="assertion.desc" :placeholder="$t('api_test.request.assertions.script_name')"
                class="quick-script-block"/>
      <ms-jsr233-processor ref="jsr233" :is-read-only="isReadOnly" :jsr223-processor="assertion" :templates="templates"
                           :height="300"/>
      <template v-slot:footer v-if="!edit">
        <ms-dialog-footer
          @cancel="close"
          @confirm="confirm"/>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import {AssertionJSR223} from "../../model/ScenarioModel";
import MsDialogFooter from "@/business/components/common/components/MsDialogFooter";
import MsJsr233Processor from "../../../automation/scenario/component/Jsr233Processor";

export default {
  name: "MsApiAssertionJsr223",
  components: {MsJsr233Processor, MsDialogFooter},
  props: {
    assertion: {
      type: AssertionJSR223,
      default: () => {
        return new AssertionJSR223();
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
      visible: false,
      operators: {
        EQ: {
          label: "commons.adv_search.operators.equals",
          value: "=="
        },
        NE: {
          label: "commons.adv_search.operators.not_equals",
          value: "!="
        },
        CONTAINS: {
          label: "commons.adv_search.operators.like",
          value: "contains"
        },
        NOT_CONTAINS: {
          label: "commons.adv_search.operators.not_like",
          value: "not contains"
        },
        GT: {
          label: "commons.adv_search.operators.gt",
          value: ">"
        },
        LT: {
          label: "commons.adv_search.operators.lt",
          value: "<"
        },
        IS_EMPTY: {
          label: "commons.adv_search.operators.is_empty",
          value: "is empty"
        },
        IS_NOT_EMPTY: {
          label: "commons.adv_search.operators.is_not_empty",
          value: "is not empty"
        }
      },
      templates: [
        {
          title: this.$t('api_test.request.assertions.set_failure_status'),
          value: 'AssertionResult.setFailure(true)',
        },
        {
          title: this.$t('api_test.request.assertions.set_failure_msg'),
          value: 'AssertionResult.setFailureMessage("msg")',
        }
      ],
    }
  },

  methods: {
    add() {
      this.list.push(new AssertionJSR223(this.assertion));
      this.callback();
    },
    remove() {
      this.list.splice(this.index, 1);
    },
    changeOperator(value) {
      if (value.indexOf("empty") > 0 && !!this.assertion.value) {
        this.assertion.value = "";
      }
      this.quickScript();
    },
    quickScript() {
      if (this.assertion.variable && this.assertion.operator) {
        let variable = this.assertion.variable;
        let operator = this.assertion.operator;
        let value = this.assertion.value || "";
        let desc = "${" + variable + "} " + operator + " '" + value + "'";
        let script = "value = vars.get(\"" + variable + "\");\n"
        switch (this.assertion.operator) {
          case "==":
            script += "result = \"" + value + "\".equals(value);\n";
            break;
          case "!=":
            script += "result = !\"" + value + "\".equals(value);\n";
            break;
          case "contains":
            script += "result = value.contains(\"" + value + "\");\n";
            break;
          case "not contains":
            script += "result = !value.contains(\"" + value + "\");\n";
            break;
          case ">":
            desc = "${" + variable + "} " + operator + " " + value;
            script += "number = Integer.parseInt(value);\n" +
              "result = number > " + value + ";\n";
            break;
          case "<":
            desc = "${" + variable + "} " + operator + " " + value;
            script += "number = Integer.parseInt(value);\n" +
              "result = number < " + value + ";\n";
            break;
          case "is empty":
            desc = "${" + variable + "} " + operator
            script += "result = value == void || value.length() == 0;\n";
            break;
          case "is not empty":
            desc = "${" + variable + "} " + operator
            script += "result = value != void && value.length() > 0;\n";
            break;
        }
        let msg = "assertion [" + desc + "]: false;"
        script += "if (!result){\n" +
          "\tmsg = \"" + msg + "\";\n" +
          "\tAssertionResult.setFailureMessage(msg);\n" +
          "\tAssertionResult.setFailure(true);\n" +
          "}";

        this.assertion.desc = desc;
        this.assertion.script = script;
        this.$refs.jsr233.reload();
      }

    },
    detail() {
      this.visible = true;
    },
    close() {
      this.visible = false;
    },
    confirm() {
      if (!this.edit) {
        this.add();
      }
      if (!this.assertion.desc) {
        this.assertion.desc = this.assertion.script;
      }
      this.close();
    }
  },

  computed: {
    hasEmptyOperator() {
      return !!this.assertion.operator && this.assertion.operator.indexOf("empty") > 0;
    }
  }
}
</script>

<style scoped>
.assertion-item {
  display: inline-block;
}

.assertion-item + .assertion-item {
  margin-left: 10px;
}

.assertion-item.input {
  width: 100%;
}

.assertion-item.select {
  min-width: 150px;
}

.assertion-item.label {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.assertion-item.btn {
  min-width: 130px;
}

.assertion-item.btn.circle {
  text-align: right;
  min-width: 80px;
}

.quick-script-block {
  margin-bottom: 10px;
}
</style>

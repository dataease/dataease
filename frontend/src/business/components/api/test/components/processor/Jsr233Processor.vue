<template>
  <div>
    <el-row>
      <el-col :span="20">
        <ms-code-edit v-if="isCodeEditAlive" :mode="codeEditModeMap[jsr223Processor.language]" :read-only="isReadOnly"
                      :data.sync="jsr223Processor.script" theme="eclipse" :modes="['java','python']" ref="codeEdit"
                      :height="height"/>
      </el-col>
      <el-col :span="4" class="script-index">
        <ms-dropdown :default-command="jsr223Processor.language" :commands="languages" @command="languageChange"/>
        <div class="template-title">{{ $t('api_test.request.processor.code_template') }}</div>
        <div v-for="(template, index) in codeTemplates" :key="index" class="code-template">
          <el-link :disabled="template.disabled" @click="addTemplate(template)">{{ template.title }}</el-link>
        </div>
        <div class="document-url">
          <el-link href="https://jmeter.apache.org/usermanual/component_reference.html#BeanShell_PostProcessor"
                   type="primary">{{ $t('commons.reference_documentation') }}
          </el-link>
          <ms-instructions-icon :content="$t('api_test.request.processor.bean_shell_processor_tip')"/>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import MsCodeEdit from "../../../../common/components/MsCodeEdit";
import MsInstructionsIcon from "../../../../common/components/MsInstructionsIcon";
import MsDropdown from "../../../../common/components/MsDropdown";

export default {
  name: "MsJsr233Processor",
  components: {MsDropdown, MsInstructionsIcon, MsCodeEdit},
  props: {
    type: {
      type: String,
    },
    isReadOnly: {
      type: Boolean,
      default: false
    },
    jsr223Processor: {
      type: Object,
    },
    isPreProcessor: {
      type: Boolean,
      default: false
    },
    templates: {
      type: Array,
      default: () => []
    },
    height: {
      type: [String, Number],
      default: 230
    }
  },
  data() {
    return {
      codeTemplates: [
        {
          title: this.$t('api_test.request.processor.code_template_get_variable'),
          value: 'vars.get("variable_name")',
        },
        {
          title: this.$t('api_test.request.processor.code_template_set_variable'),
          value: 'vars.put("variable_name", "variable_value")',
        },
        {
          title: this.$t('api_test.request.processor.code_template_get_global_variable'),
          value: 'props.get("variable_name")',
        },
        {
          title: this.$t('api_test.request.processor.code_template_set_global_variable'),
          value: 'props.put("variable_name", "variable_value")',
        },
        {
          title: this.$t('api_test.request.processor.code_template_get_response_header'),
          value: 'prev.getResponseHeaders()',
          disabled: this.isPreProcessor
        },
        {
          title: this.$t('api_test.request.processor.code_template_get_response_code'),
          value: 'prev.getResponseCode()',
          disabled: this.isPreProcessor
        },
        {
          title: this.$t('api_test.request.processor.code_template_get_response_result'),
          value: 'prev.getResponseDataAsString()',
          disabled: this.isPreProcessor
        },
        ...this.templates
      ],
      isCodeEditAlive: true,
      languages: [
        'beanshell', "python", "groovy", "javascript"
      ],
      codeEditModeMap: {
        beanshell: 'java',
        python: 'python',
        groovy: 'java',
        javascript: 'javascript',
      }
    }
  },
  watch: {
    jsr223Processor() {
      this.reload();
    }
  },
  methods: {
    addTemplate(template) {
      if (!this.jsr223Processor.script) {
        this.jsr223Processor.script = "";
      }
      this.jsr223Processor.script += template.value;
      if (this.jsr223Processor.language === 'beanshell') {
        this.jsr223Processor.script += ';';
      }
      this.reload();
    },
    reload() {
      this.isCodeEditAlive = false;
      this.$nextTick(() => (this.isCodeEditAlive = true));
    },
    languageChange(language) {
      this.jsr223Processor.language = language;
    }
  }
}
</script>

<style scoped>

.ace_editor {
  border-radius: 5px;
}

.script-index {
  padding: 0 20px;
}

.template-title {
  margin-bottom: 5px;
  font-weight: bold;
  font-size: 15px;
}

.document-url {
  margin-top: 10px;
}

.instructions-icon {
  margin-left: 5px;
}

.ms-dropdown {
  margin-bottom: 20px;
}

</style>

<template>
    <div>
      <el-row style="margin:0px 10px 10px">
        <el-col>
          <div class="document-url">
            <el-link href="https://jmeter.apache.org/usermanual/component_reference.html#BeanShell_PostProcessor"
                     target="componentReferenceDoc"
                     type="primary">{{$t('commons.reference_documentation')}}
            </el-link>
          </div>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="20" class="script-content">
          <ms-code-edit v-if="isCodeEditAlive" :mode="codeEditModeMap[jsr223ProcessorData.scriptLanguage]"
                        :read-only="isReadOnly"
                        :data.sync="jsr223ProcessorData.script" theme="eclipse" :modes="['java','python']"
                        ref="codeEdit"/>
        </el-col>
        <el-col :span="4" class="script-index">
          <ms-dropdown :default-command="jsr223ProcessorData.scriptLanguage" :commands="languages" @command="languageChange"/>
          <div class="template-title">{{$t('api_test.request.processor.code_template')}}</div>
          <div v-for="(template, index) in codeTemplates" :key="index" class="code-template">
            <el-link :disabled="template.disabled" @click="addTemplate(template)">{{template.title}}</el-link>
          </div>
        </el-col>
      </el-row>
    </div>
</template>

<script>
    import MsCodeEdit from "../../../definition/components/MsCodeEdit";
    import MsDropdown from "../../../../common/components/MsDropdown";
    export default {
        name: "Jsr233ProcessorContent",
      components: {MsDropdown, MsCodeEdit},
      data() {
        return {
          jsr223ProcessorData: {},
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
            }
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
      created() {
        this.jsr223ProcessorData = this.jsr223Processor;
      },
      props: {
        isReadOnly: {
          type: Boolean,
          default:
            false
        },
        jsr223Processor: {
          type: Object,
        },
        isPreProcessor: {
          type: Boolean,
          default:
            false
        },
        node: {},
      },
      watch: {
        jsr223Processor() {
          this.reload();
        }
      }
      ,
      methods: {
        addTemplate(template) {
          if (!this.jsr223ProcessorData.script) {
            this.jsr223ProcessorData.script = "";
          }
          this.jsr223ProcessorData.script += template.value;
          if (this.jsr223ProcessorData.scriptLanguage === 'beanshell') {
            this.jsr223ProcessorData.script += ';';
          }
          this.reload();
        },
        reload() {
          this.isCodeEditAlive = false;
          this.$nextTick(() => (this.isCodeEditAlive = true));
        },
        languageChange(language) {
          this.jsr223ProcessorData.scriptLanguage = language;
        },
      }
    }
</script>

<style scoped>
  .ace_editor {
    border-radius: 5px;
  }

  .script-content {
    height: calc(100vh - 570px);
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

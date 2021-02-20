<template>
  <ms-drawer class="json-path-picker" :visible="visible" :size="30" @close="close" direction="right">
    <template v-slot:header>
      <ms-instructions-icon :content="tip"/>
      {{tip}}
    </template>
    <jsonpath-picker :code="data" v-on:path="pathChangeHandler" ref="jsonpathPicker"/>
  </ms-drawer>
</template>

<script>
import MsDrawer from "../../../../common/components/MsDrawer";
import MsInstructionsIcon from "../../../../common/components/MsInstructionsIcon";

let dotReplace = "#DOT_MASK#";
export default {
  name: "MsApiJsonpathSuggest",
  components: {MsInstructionsIcon, MsDrawer},
  data() {
    return {
      visible: false,
      isCheckAll: false,
      data: {},
    };
  },
  props: {
    tip: {
      type: String,
      default() {
        return ""
      }
    },
  },
    methods: {
      close() {
        this.visible = false;
      },
      open(objStr) {
        this.data = {};
        try {
          let param = JSON.parse(objStr);
          if (param instanceof Array) {
            this.$warning(this.$t('api_test.request.assertions.json_path_err'));
            return;
          }
          this.data = param;
        } catch (e) {
          this.$warning(this.$t('api_test.request.assertions.json_path_err'));
          return;
        }
        this.visible = true;
      },
      pathChangeHandler(data) {
        let paramNames = [];
        let result = {};
        try {
          paramNames = this.parseSpecialChar(data);
          result = this.getParamValue(this.data, 0, paramNames);
        } catch (e) {
          result = {};
          result.key = 'var';
        }
        result.path = '$.' + data;
        this.$emit('addSuggest', result);
      },
      // 替换. 等特殊字符
      parseSpecialChar(data) {
        let paramNames = [];
        let reg = /\['.*'\]/;
        let searchStr = reg.exec(data);
        if (searchStr) {
          searchStr.forEach(item => {
            if (data.startsWith("['")) {
              data = data.replace(item, item.replace('.', dotReplace));
            } else {
              data = data.replace(item, '.' + item.replace('.', dotReplace));
            }
          });
          paramNames = data.split('.');
        } else {
          paramNames = data.split('.');
        }
        for (let i in paramNames) {
          if (paramNames[i].search(reg) > -1) {
            paramNames[i] = paramNames[i].substring(2, paramNames[i].length - 2);
          }
          paramNames[i] = paramNames[i].replace(dotReplace, '.');
        }
        return paramNames;
      },
      getParamValue(obj, index, params) {
        if (params.length < 1) {
          return {};
        }

        let param = params[index];
        let childObj;

        let reg = /\[\d\]$/;
        let regIndex = param.search(reg);
        if (regIndex > -1) {
          let paramName = param.substring(0, regIndex);
          let paramIndex = param.substring(regIndex + 1, param.length - 1);
          param =  paramIndex;
          childObj = obj[paramName][paramIndex];
        } else {
          childObj = obj[params[index]];
        }
        if (index === params.length - 1) {
          if (childObj instanceof Object) {
            childObj = JSON.stringify(childObj);
          } else {
            childObj = childObj + "";
          }
          return {
            key: param,
            value: childObj
          };
        }
        index++;
        return this.getParamValue(childObj, index, params);
      }
    }
  }
</script>

<style scoped>

  .json-path-picker {
    padding: 10px 13px;
  }

  .json-path-picker >>> .json-tree {
    margin-top: 0px;
    margin-left: 6px;
  }

</style>

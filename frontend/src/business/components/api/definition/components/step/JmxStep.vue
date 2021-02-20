<template>
  <div v-if="request.hashTree && request.hashTree.length > 0">
    <p class="tip">
      {{$t('test_track.plan_view.step')}}
    </p>
    <!-- HTTP 请求参数 -->
    <div style="height: 100%;border-radius: 4px ;width: 100%" v-loading="isReloadData" v-if="request.hashTree && request.hashTree.length>0">

      <div v-for="row in request.hashTree" :key="row.id">
        <!--前置脚本-->
        <ms-jsr233-processor v-if="row.type==='JSR223PreProcessor'" @remove="remove" @copyRow="copyRow" :title="$t('api_test.definition.request.pre_script')"
                             :jsr223-processor="row" color="#B8741A" background-color="#F9F1EA"/>
        <!--后置脚本-->
        <ms-jsr233-processor v-if="row.label ==='JSR223 PostProcessor'" @copyRow="copyRow" @remove="remove" :is-read-only="false" :title="$t('api_test.definition.request.post_script')"
                             :jsr223-processor="row" color="#783887" background-color="#F2ECF3"/>
        <!--断言规则-->
        <div style="margin-top: 10px">
          <ms-api-assertions :response="response" v-if="row.type==='Assertions'" @copyRow="copyRow" @remove="remove" :is-read-only="isReadOnly" :assertions="row"/>
        </div>
        <!--提取规则-->
        <div style="margin-top: 10px">
          <ms-api-extract :response="response" :is-read-only="isReadOnly" @copyRow="copyRow" @remove="remove" v-if="row.type==='Extract'" :extract="row"/>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
  import {REQUEST_HEADERS} from "@/common/js/constants";
  import {createComponent} from "../jmeter/components";
  import MsApiAssertions from "../assertion/ApiAssertions";
  import MsApiExtract from "../extract/ApiExtract";
  import {Assertions, Body, Extract, KeyValue} from "../../model/ApiTestModel";
  import {getUUID} from "@/common/js/utils";
  import BatchAddParameter from "../basis/BatchAddParameter";
  import MsJsr233Processor from "../../../automation/scenario/component/Jsr233Processor";

  export default {
    name: "MsJmxStep",
    components: {
      MsJsr233Processor,
      BatchAddParameter,
      MsApiExtract,
      MsApiAssertions
    },
    props: {
      request: {},
      response: {},
      showScript: {
        type: Boolean,
        default: true,
      },
      headers: {
        type: Array,
        default() {
          return [];
        }
      },
      referenced: {
        type: Boolean,
        default: false,
      },
      isShowEnable: Boolean,
      jsonPathList: Array,
      isReadOnly: {
        type: Boolean,
        default: false
      }
    },

    data() {
      let validateURL = (rule, value, callback) => {
        try {
          new URL(this.addProtocol(this.request.url));
        } catch (e) {
          callback(this.$t('api_test.request.url_invalid'));
        }
      };
      return {
        activeName: "headers",
        rules: {
          name: [
            {max: 300, message: this.$t('commons.input_limit', [1, 300]), trigger: 'blur'}
          ],
          url: [
            {max: 500, required: true, message: this.$t('commons.input_limit', [1, 500]), trigger: 'blur'},
            {validator: validateURL, trigger: 'blur'}
          ],
          path: [
            {max: 500, message: this.$t('commons.input_limit', [0, 500]), trigger: 'blur'},
          ]
        },
        headerSuggestions: REQUEST_HEADERS,
        isReloadData: false,
        isBodyShow: true,
        dialogVisible: false,
      }
    },

    created() {
      this.init();
    },

    methods: {
      addPre() {
        let jsr223PreProcessor = createComponent("JSR223PreProcessor");
        if (!this.request.hashTree) {
          this.request.hashTree = [];
        }
        this.request.hashTree.push(jsr223PreProcessor);
        this.reload();
      },
      addPost() {
        let jsr223PostProcessor = createComponent("JSR223PostProcessor");
        if (!this.request.hashTree) {
          this.request.hashTree = [];
        }
        this.request.hashTree.push(jsr223PostProcessor);
        this.reload();
      },
      addAssertions() {
        let assertions = new Assertions();
        if (!this.request.hashTree) {
          this.request.hashTree = [];
        }
        this.request.hashTree.push(assertions);
        this.reload();
      },
      addExtract() {
        let jsonPostProcessor = new Extract();
        if (!this.request.hashTree) {
          this.request.hashTree = [];
        }
        this.request.hashTree.push(jsonPostProcessor);
        this.reload();
      },
      remove(row) {
        let index = this.request.hashTree.indexOf(row);
        this.request.hashTree.splice(index, 1);
        this.reload();
      },
      copyRow(row) {
        let obj = JSON.parse(JSON.stringify(row));
        obj.id = getUUID();
        const index = this.request.hashTree.findIndex(d => d.id === row.id);
        if (index != -1) {
          this.request.hashTree.splice(index, 0, obj);
        } else {
          this.request.hashTree.push(obj);
        }
        this.reload();
      },
      reload() {
        this.isReloadData = true
        this.$nextTick(() => {
          this.isReloadData = false
        })
      },
      init() {
        if (!this.request.body) {
          this.request.body = new Body();
        }
        if (!this.request.body.kvs) {
          this.request.body.kvs = [];
        }
        if (!this.request.rest) {
          this.request.rest = [];
        }
        if (!this.request.arguments) {
          this.request.arguments = [];
        }
      },
      // 解决修改请求头后 body 显示错位
      reloadBody() {
        this.isBodyShow = false;
        this.$nextTick(() => {
          this.isBodyShow = true;
        });
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
            switch (this.activeName) {
              case "parameters":
                this.request.arguments.unshift(item);
                break;
              case "rest":
                this.request.rest.unshift(item);
                break;
              case "headers":
                this.request.headers.unshift(item);
                break;
              default:
                break;
            }
          })
        }
      }
    }
  }
</script>

<style scoped>
  .ms-left-cell .el-button:nth-of-type(1) {
    color: #B8741A;
    background-color: #F9F1EA;
    border: #F9F1EA;
  }

  .ms-left-cell .el-button:nth-of-type(2) {
    color: #783887;
    background-color: #F2ECF3;
    border: #F2ECF3;
  }

  .ms-left-cell .el-button:nth-of-type(3) {
    color: #A30014;
    background-color: #F7E6E9;
    border: #F7E6E9;
  }

  .ms-left-cell .el-button:nth-of-type(4) {
    color: #015478;
    background-color: #E6EEF2;
    border: #E6EEF2;
  }

  .tip {
    padding: 3px 5px;
    font-size: 16px;
    border-radius: 4px;
    border-left: 4px solid #783887;
    margin: 20px 0;
  }

</style>

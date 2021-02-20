<template>
  <el-col :span="3" class="ms-left-cell">
    <el-button class="ms-left-buttion" size="small" @click="addPre">+{{$t('api_test.definition.request.pre_script')}}</el-button>
    <br/>
    <el-button class="ms-left-buttion" size="small" @click="addPost">+{{$t('api_test.definition.request.post_script')}}</el-button>
    <br/>
    <el-button class="ms-left-buttion" size="small" @click="addAssertions">+{{$t('api_test.definition.request.assertions_rule')}}</el-button>
    <br/>
    <el-button class="ms-left-buttion" size="small" @click="addExtract">+{{$t('api_test.definition.request.extract_param')}}</el-button>
  </el-col>
</template>

<script>
    import {createComponent} from "../../jmeter/components";
    import {Assertions, Extract} from "../../../model/ApiTestModel";

    export default {
      name: "ApiDefinitionStepButton",
      props: {
        request: {
          type: Object,
          default() {
            return {}
          }
        }
      },
      methods: {
        addPre() {
          let jsr223PreProcessor = createComponent("JSR223PreProcessor");
          if (!this.request.hashTree) {
            this.request.hashTree = [];
          }
          this.request.hashTree.push(jsr223PreProcessor);
        },
        addPost() {
          let jsr223PostProcessor = createComponent("JSR223PostProcessor");
          if (!this.request.hashTree) {
            this.request.hashTree = [];
          }
          this.request.hashTree.push(jsr223PostProcessor);
        },
        addAssertions() {
          let assertions = new Assertions();
          if (!this.request.hashTree) {
            this.request.hashTree = [];
          }
          this.request.hashTree.push(assertions);
        },
        addExtract() {
          let jsonPostProcessor = new Extract();
          if (!this.request.hashTree) {
            this.request.hashTree = [];
          }
          this.request.hashTree.push(jsonPostProcessor);
        },
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

  .ms-left-cell {
    margin-top: 30px;
  }

  .ms-left-buttion {
    margin: 6px 0px 8px 30px;
  }

</style>

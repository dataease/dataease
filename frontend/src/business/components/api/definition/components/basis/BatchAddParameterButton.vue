<template>
  <el-row>
    <el-link class="ms-el-link" @click="open"> {{$t("commons.batch_add")}}</el-link>
    <batch-add-parameter @batchSave="batchSave" ref="batchAddParameter"/>
  </el-row>
</template>

<script>
    import BatchAddParameter from "./BatchAddParameter";
    import {KeyValue} from "../../model/ApiTestModel";
    export default {
      name: "BatchAddParameterButton",
      components: {BatchAddParameter},
      props: ['data'],
      methods: {
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
              this.data.unshift(item);
            })
          }
        },
        open() {
          this.$refs.batchAddParameter.open();
        }
      }
    }
</script>

<style scoped>

  .ms-el-link {
    float: right;
    margin-right: 45px;
  }

</style>

<template>
  <div id="app" v-loading="loading">
    <el-tabs v-model="activeName" @tab-click="handleClick">
      <el-tab-pane :label="$t('organization.message.template')" name="apiTemplate">
        <el-button type="primary" size="mini" style="margin: 10px 10px 0px" @click="openOneClickOperation">导入</el-button>
        <div style="min-height: 200px">
          <json-schema-editor class="schema" :value="schema" lang="zh_CN" custom/>
        </div>
      </el-tab-pane>
      <el-tab-pane :label="$t('schema.preview')" name="preview">
        <div style="min-height: 200px">
          <pre>{{this.preview}}</pre>
        </div>
      </el-tab-pane>
    </el-tabs>

    <ms-import-json ref="importJson" @jsonData="jsonData"/>
  </div>
</template>

<script>
  import {schemaToJson} from './common';
  import MsImportJson from './import/ImportJson';

  const GenerateSchema = require('generate-schema/src/schemas/json.js');

  export default {
    name: 'App',
    components: {MsImportJson},
    props: {
      body: {},
    },
    created() {
      if (!this.body.jsonSchema && this.body.raw && this.checkIsJson(this.body.raw)) {
        let obj = {"root": GenerateSchema(JSON.parse(this.body.raw))}
        this.schema = obj;
      }
      else if (this.body.jsonSchema) {
        this.schema = {"root": this.body.jsonSchema};
      }
      this.body.jsonSchema = this.schema.root;
    },
    data() {
      return {
        schema:
          {
            "root": {
              "type": "object",
              "properties": {},
            }
          },
        loading: false,
        preview: null,
        activeName: "apiTemplate",
      }
    },
    methods: {
      handleClick() {
        if (this.activeName === 'preview') {
          // 前端转换
          //this.preview = schemaToJson(this.schema.root);
          this.loading = true;
          // 后台转换
          this.$post('/api/definition/preview', this.schema.root, response => {
            this.preview = response.data;
            this.loading = false;
          });
        }
      },
      openOneClickOperation() {
        this.$refs.importJson.openOneClickOperation();
      },
      checkIsJson(json) {
        try {
          JSON.parse(json);
          return true;
        } catch (e) {
          return false;
        }
      },
      jsonData(data) {
        let obj = {"root": data}
        this.schema = obj;
      }
    }
  }
</script>
<style>

</style>

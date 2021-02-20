<template>
  <el-dialog
    title="导入"
    :visible.sync="importVisible"
    width="50%"
    append-to-body
    show-close
    :close-on-click-modal="false"
    @closed="handleClose">

    <el-tabs v-model="activeName">
      <el-tab-pane label="JSON" name="JSON">
        <div style="height: 400px">
          <ms-code-edit :mode="mode"
                        :data.sync="json" theme="eclipse" :modes="[]"
                        ref="codeEdit"/>
        </div>
      </el-tab-pane>
      <el-tab-pane label="JSON-SCHEMA" name="JSON-SCHEMA">
        <div style="height: 400px">
          <ms-code-edit :mode="mode"
                        :data.sync="jsonSchema" theme="eclipse" :modes="[]"
                        ref="codeEdit"/>
        </div>
      </el-tab-pane>
    </el-tabs>
    <span slot="footer" class="dialog-footer">
     <ms-dialog-footer
       @cancel="importVisible = false"
       @confirm="saveConfirm"/>
    </span>
  </el-dialog>
</template>

<script>
  import MsDialogFooter from '../../../common/components/MsDialogFooter'
  import MsCodeEdit from "../../../common/components/MsCodeEdit";
  import json5 from 'json5';

  const GenerateSchema = require('generate-schema/src/schemas/json.js');

  export default {
    name: "MsImportJson",
    components: {MsDialogFooter, MsCodeEdit},
    data() {
      return {
        importVisible: false,
        activeName: "JSON",
        mode: "json",
        json: "",
        jsonSchema: "",
      };
    },
    watch: {},
    props: {},
    methods: {
      openOneClickOperation() {
        this.importVisible = true;
      },
      checkIsJson(json) {
        try {
          json5.parse(json);
          return true;
        } catch (e) {
          return false;
        }
      },
      checkIsJsonSchema(json) {
        try {
          json = json5.parse(json);
          if (json.properties && typeof json.properties === 'object' && !json.type) {
            json.type = 'object';
          }
          if (json.items && typeof json.items === 'object' && !json.type) {
            json.type = 'array';
          }
          if (!json.type) {
            return false;
          }
          json.type = json.type.toLowerCase();
          let types = ['object', 'string', 'number', 'array', 'boolean', 'integer'];
          if (types.indexOf(json.type) === -1) {
            return false;
          }
          return JSON.stringify(json);
        } catch (e) {
          return false;
        }
      },
      saveConfirm() {
        if (this.activeName === 'JSON') {
          if (!this.checkIsJson(this.json)) {
            this.$error("导入的数据非JSON格式");
            return;
          }
          let jsonData = GenerateSchema(json5.parse(this.json));
          this.$emit('jsonData', jsonData);
        } else {
          if (!this.checkIsJsonSchema(this.jsonSchema)) {
            this.$error("导入的数据非JSON-SCHEMA 格式");
            return;
          }
          let obj = json5.parse(this.jsonSchema);
          this.$emit('jsonData', obj);
        }
        this.importVisible = false;
      },
      handleClose() {
        this.importVisible = false;
      },
    }
  }
</script>

<style scoped>

</style>

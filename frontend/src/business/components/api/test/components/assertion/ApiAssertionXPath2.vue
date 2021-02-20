<template>
  <div>
    <el-row :gutter="10" type="flex" justify="space-between" align="middle">
      <el-col>
        <el-input :disabled="isReadOnly" v-model="xPath2.expression" maxlength="200" size="small" show-word-limit
                  :placeholder="$t('api_test.request.extract.xpath_expression')"/>
      </el-col>
      <el-col class="assertion-btn">
        <el-button :disabled="isReadOnly" type="danger" size="mini" icon="el-icon-delete" circle @click="remove" v-if="edit"/>
        <el-button :disabled="isReadOnly" type="primary" size="small" @click="add" v-else>
          {{ $t('api_test.request.assertions.add') }}
        </el-button>
      </el-col>
    </el-row>
  </div>
</template>

<script>
  import {XPath2} from "../../model/ScenarioModel";

  export default {
    name: "MsApiAssertionXPath2",

    props: {
      xPath2: {
        type: XPath2,
        default: () => {
          return new XPath2();
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

    methods: {
      add: function () {
        this.list.push(this.getXPath2());
        this.callback();
      },
      remove: function () {
        this.list.splice(this.index, 1);
      },
      getXPath2() {
        return new XPath2(this.xPath2);
      },
    }
  }
</script>

<style scoped>
  .assertion-select {
    width: 250px;
  }

  .assertion-item {
    width: 100%;
  }

  .assertion-btn {
    text-align: center;
    width: 60px;
  }
</style>

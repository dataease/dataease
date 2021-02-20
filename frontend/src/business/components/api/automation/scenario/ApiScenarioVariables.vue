<template>
  <div>
    <span class="kv-description" v-if="description">
      {{description}}
    </span>
    <div class="kv-row" v-for="(item, index) in items" :key="index">
      <el-row type="flex" :gutter="20" justify="space-between" align="middle">
        <el-col class="kv-checkbox">
          <input type="checkbox" v-if="!isDisable(index)" @change="change" :value="item.uuid" v-model="item.enable"
                 :disabled="isDisable(index) || isReadOnly"/>
        </el-col>

        <el-col>
          <ms-api-variable-input :show-copy="showCopy" :show-variable="showVariable" :is-read-only="isReadOnly" v-model="item.name" size="small" maxlength="200" @change="change"
                                 :placeholder="$t('api_test.variable_name')" show-word-limit/>
        </el-col>
        <el-col>
          <el-input :disabled="isReadOnly" v-model="item.value" size="small" @change="change"
                    :placeholder="$t('api_test.value')" show-word-limit/>
        </el-col>
        <el-col class="kv-delete">
          <el-button size="mini" class="el-icon-delete-solid" circle @click="remove(index)"
                     :disabled="isDisable(index) || isReadOnly"/>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
  import {KeyValue} from "../../definition/model/ApiTestModel";
  import MsApiVariableInput from "./ApiVariableInput";

  export default {
    name: "MsApiScenarioVariables",
    components: {MsApiVariableInput},
    props: {
      description: String,
      items: Array,
      isReadOnly: {
        type: Boolean,
        default: false
      },
      showVariable: {
        type: Boolean,
        default: true
      },
      showCopy: {
        type: Boolean,
        default: true
      },
    },
    data() {
      return {
      }
    },
    methods: {
      remove: function (index) {
        this.items.splice(index, 1);
        this.$emit('change', this.items);
      },
      change: function () {
        let isNeedCreate = true;
        let removeIndex = -1;
        this.items.forEach((item, index) => {
          if (!item.name && !item.value) {
            // 多余的空行
            if (index !== this.items.length - 1) {
              removeIndex = index;
            }
            // 没有空行，需要创建空行
            isNeedCreate = false;
          }
        });
        if (isNeedCreate) {
          this.items.push(new KeyValue({enable: true}));
        }
        this.$emit('change', this.items);
        // TODO 检查key重复
      },
      isDisable: function (index) {
        return this.items.length - 1 === index;
      }
    },

    created() {
      if (this.items.length === 0) {
        this.items.push(new KeyValue({enable: true}));
      }
    }
  }
</script>

<style scoped>
  .kv-description {
    font-size: 13px;
  }

  .kv-checkbox {
    width: 20px;
    margin-right: 10px;
  }

  .kv-row {
    margin-top: 10px;
  }

  .kv-delete {
    width: 60px;
  }
</style>

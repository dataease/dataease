<template>
  <div>
    <el-row :gutter="10" type="flex" justify="space-between" align="middle">
      <el-col>
        <el-input :disabled="isReadOnly" :value="value" v-bind="$attrs" step="100" size="small" type="number" @change="change" @input="input" :min="0"
                  :placeholder="$t('api_test.request.assertions.response_in_time')"/>
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

  import {Duration} from "../../model/ScenarioModel";

  export default {
    name: "MsApiAssertionDuration",

    props: {
      duration: Duration,
      value: [Number, String],
      edit: Boolean,
      callback: Function,
      isReadOnly: {
        type: Boolean,
        default: false
      }
    },

    methods: {
      add() {
        if (this.validate()) {
          this.duration.value = this.value;
          this.callback();
        }
      },
      remove() {
        this.duration.value = undefined;
      },
      change(value) {
        if (this.validate()) {
          this.$emit('change', value);
        }
      },
      input(value) {
        this.$emit('input', value);
      },
      validate() {
        if (Number(this.value) < 0 || this.value=='') {
          this.$error(this.$t('commons.formatErr'));
          return false;
        }
        return true;
      }
    }
  }
</script>

<style scoped>
  .assertion-btn {
    text-align: center;
    width: 60px;
  }
</style>

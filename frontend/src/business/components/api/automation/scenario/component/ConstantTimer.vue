<template>
  <api-base-component
    @copy="copyRow"
    @remove="remove"
    :data="timer"
    :draggable="true"
    :show-collapse="false"
    color="#67C23A"
    background-color="#F2F9EE"
    :title="$t('api_test.automation.wait_controller')">

    <template v-slot:headerLeft>
      <el-input-number class="time-input" size="small" v-model="timer.delay" :min="0" :step="1000" ref="nameInput"/>
      ms
    </template>

  </api-base-component>
</template>

<script>
  import ApiBaseComponent from "../common/ApiBaseComponent";

  export default {
    name: "MsConstantTimer",
    components: {ApiBaseComponent},
    props: {
      timer: {},
      node: {},
      draggable: {
        type: Boolean,
        default: false,
      },
    },
    data() {
      return {}
    },
    created() {
      this.$nextTick(() => {
        this.$refs.nameInput.focus();
      });
    },
    methods: {
      remove() {
        this.$emit('remove', this.timer, this.node);
      },
      copyRow() {
        this.$emit('copyRow', this.timer, this.node);
      },
    }
  }
</script>

<style scoped>
  .width-100 {
    width: 40%
  }

  .ms-api-col {
    background-color: #F2F9EE;
    border-color: #67C23A;
    margin-right: 10px;
    color: #67C23A;
  }

  .ms-title-buttion {
    background-color: #F2F9EE;
    color: #67C23A;
    margin-right: 20px;
  }

  /deep/ .el-card__body {
    padding: 15px;
  }

  .time-input {
    width: 30%;
    /*margin-left: 20px;*/
  }
</style>

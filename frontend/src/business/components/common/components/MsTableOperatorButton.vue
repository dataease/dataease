<template>
  <ms-tip-button
    :disabled="disabled || isReadOnly"
    @click="exec"
    @clickStop="clickStop"
    :type="type"
    :tip="tip"
    :icon="icon" size="mini" circle/>
</template>

<script>
  import MsTableButton from "./MsTableButton";
  import MsTipButton from "./MsTipButton";
  import {checkoutTestManagerOrTestUser, hasRoles} from "../../../../common/js/utils";
  import {ROLE_TEST_MANAGER, ROLE_TEST_USER} from "../../../../common/js/constants";
  export default {
    name: "MsTableOperatorButton",
    components: {MsTipButton, MsTableButton},
    data() {
      return {
        isReadOnly: false
      }
    },
    props: {
      icon: {
        type: String,
        default: 'el-icon-question'
      },
      type: {
        type: String,
        default: 'primary'
      },
      tip: {
        type: String
      },
      disabled: {
        type: Boolean,
        default: false
      },
      isTesterPermission: {
        type: Boolean,
        default: false
      }
    },
    mounted() {
      if (this.isTesterPermission && !checkoutTestManagerOrTestUser()) {
        this.isReadOnly = true;
      }
    },
    methods: {
      exec() {
        this.$emit('exec');
      },
      clickStop() {
        this.$emit('clickStop');
      }
    }
  }
</script>

<style scoped>
</style>

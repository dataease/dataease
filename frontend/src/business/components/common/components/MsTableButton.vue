<template>
  <el-button :disabled="disabled" @click="exec()" plain :type="type" :icon="icon" :size="size">
    {{content}}
  </el-button>
</template>

<script>
  import {checkoutTestManagerOrTestUser, hasRoles} from "../../../../common/js/utils";
    import {ROLE_TEST_MANAGER, ROLE_TEST_USER} from "../../../../common/js/constants";

    export default {
      name: "MsTableButton",
      data() {
        return {
          disabled: false
        }
      },
      props: {
        content: String,
        icon: {
          type: String,
          default: 'el-icon-question'
        },
        type: {
          type: String,
          default: null
        },
        effect: {
          type: String,
          default: 'dark'
        },
        size: {
          type: String,
          default: 'mini'
        },
        isTesterPermission: {
          type: Boolean,
          default: false
        }
      },
      mounted() {
        if (this.isTesterPermission && !checkoutTestManagerOrTestUser()) {
          this.disabled = true;
        }
      },
      methods: {
        exec() {
          this.$emit('click');
        }
      }
    }
</script>

<style scoped>

  .el-button {
    padding: 8px 10px;
  }
</style>

<template>
  <el-input
    :value="value"
    :placeholder="placeholder"
    :type="type"
    :disabled="disabled"
    class="de-pwd-input"
    @input="handleInput"
    @change="handleChange"
  >
    <svg-icon
      @click="handleClick"
      v-if="!showPwd || buttonDisabled"
      slot="suffix"
      icon-class="de_pwd_invisible"
    />
    <svg-icon
      @click="handleClick"
      v-else
      slot="suffix"
      icon-class="de_pwd_visible"
    />
  </el-input>
</template>

<script>
export default {
  name: "DePwd",
  inject: {
    elForm: {
      default: "",
    },
  },
  props: {
    disabled: Boolean,
    placeholder: String,
    value: String,
  },
  data() {
    return {
      showPwd: false,
    };
  },
  computed: {
    buttonDisabled() {
      return this.$options.propsData.hasOwnProperty("disabled")
        ? this.disabled
        : (this.elForm || {}).disabled;
    },
    type() {
      return !this.showPwd || this.buttonDisabled ? "password" : "text";
    },
  },
  methods: {
    handleClick() {
      if (this.buttonDisabled) return;
      this.showPwd = !this.showPwd;
    },
    handleInput(val) {
      this.$emit("input", val);
    },
    handleChange(val) {
      this.$emit("change", val);
    },
  },
};
</script>
<style lang="scss">
.de-pwd-input {
  .el-input__suffix {
    right: 12px;
    font-size: 16px;
  }
}
</style>
<template>
  <div class="variable-input" :class="{'show-copy': !showCopy}">
    <el-input class="el-input__inner_pd" :disabled="isReadOnly" :value="value" v-bind="$attrs" :size="size" @change="change" @input="input"/>
    <div :class="{'hidden': !showVariable}" class="variable-combine" v-if="value">
      <div v-if="showCopy" class="variable">{{variable}}</div>
      <el-tooltip v-if="showCopy" :content="$t('api_test.copied')" manual v-model="visible" placement="top" :visible-arrow="false">
        <i class="el-icon-copy-document copy" @click="copy"/>
      </el-tooltip>
    </div>
  </div>
</template>

<script>
  export default {
    name: "MsApiVariableInput",

    props: {
      value: String,
      size: String,
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
      showCopyTipWithMultiple: {
        type: Boolean,
        default: false
      },
    },

    data() {
      return {
        visible: false
      }
    },

    methods: {
      copy() {
        let input = document.createElement("input");
        document.body.appendChild(input);
        input.value = this.variable;
        input.select();
        if (input.setSelectionRange) {
          input.setSelectionRange(0, input.value.length);
        }
        document.execCommand("copy");
        document.body.removeChild(input);
        this.visible = true;
        setTimeout(() => {
          this.visible = false;
        }, 1000);
      },
      change(value) {
        this.$emit('change', value);
      },
      input(value) {
        this.$emit('input', value);
      }
    },

    computed: {
      variable() {
        return "${" + (this.showCopyTipWithMultiple ? (this.value + "_n") : this.value) + "}";
      }
    }

  }
</script>

<style scoped>

  .variable-input {
    position: relative;
  }

  .el-input__inner_pd >>> .el-input__inner {
    padding-right: 135px;
  }

  .show-copy .el-input__inner_pd >>> .el-input__inner {
    padding-right: 0px;
  }


  .variable-combine {
    color: #7F7F7F;
    max-width: 80px;
    line-height: 32px;
    position: absolute;
    top: 0;
    right: 70px;
    margin-right: -20px;
    display: flex;
    align-items: center;
  }

  .variable-combine .variable {
    display: inline-block;
    max-width: 60px;
    margin-right: 10px;
    overflow-x: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .variable-combine .copy {
    font-size: 14px;
    cursor: pointer;
    color: #1E90FF;
  }

  .hidden {
    visibility: hidden;
  }

</style>

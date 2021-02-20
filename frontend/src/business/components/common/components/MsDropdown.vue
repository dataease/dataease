<template>
  <el-dropdown @command="handleCommand" class="ms-dropdown">
    <slot>
      <span class="el-dropdown-link">
        {{currentCommand}}
        <i class="el-icon-arrow-down el-icon--right"></i>
      </span>
    </slot>
    <el-dropdown-menu slot="dropdown" chang>
      <el-dropdown-item  v-for="(command, index) in commands" :key="index" :command="command">
        {{command}}
      </el-dropdown-item>
    </el-dropdown-menu>
  </el-dropdown>
</template>

<script>
    export default {
      name: "MsDropdown",
      data() {
        return {
          currentCommand: ''
        }
      },
      props: {
        commands: {
          type: Array
        },
        defaultCommand: {
          type: String
        }
      },
      created() {
        if (this.defaultCommand) {
          this.currentCommand = this.defaultCommand;
        } else if (this.commands && this.commands.length > 0) {
          this.currentCommand = this.commands [0];
        }
      },
      methods: {
        handleCommand(command) {
          this.currentCommand = command;
          this.$emit('command', command);
        }
      }
    }
</script>

<style scoped>

  .el-dropdown-link {
    cursor: pointer;
    color: #409EFF;
  }

  .el-icon-arrow-down {
    font-size: 12px;
  }

</style>

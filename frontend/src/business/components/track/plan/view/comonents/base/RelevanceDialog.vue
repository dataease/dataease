<template>
  <el-dialog :title="title"
             :visible.sync="dialogVisible"
             @close="close"
             width="60%" v-loading="result.loading"
             :close-on-click-modal="false"
             top="50px">

    <el-container class="main-content">

      <el-aside class="tree-aside" width="250px">
        <slot name="aside"></slot>
      </el-aside>

      <el-container>
        <el-main class="case-content">
          <slot></slot>
        </el-main>
      </el-container>

    </el-container>

    <template v-slot:footer>
      <slot name="footer"></slot>
    </template>

  </el-dialog>
</template>

<script>

  import MsDialogFooter from '../../../../../common/components/MsDialogFooter'
  import SelectMenu from "../../../../common/SelectMenu";

  export default {
    name: "RelevanceDialog",
    components: {
      SelectMenu,
      MsDialogFooter,
    },
    data() {
      return {
        result: {},
        dialogVisible: false,
      };
    },
    props: ['title'],
    methods: {
      open() {
        this.dialogVisible = true;
      },
      close() {
        this.dialogVisible = false;
      },
    }
  }
</script>

<style scoped>

  .el-dialog {
    min-height: 600px;
  }

  .tree-aside {
    max-height: 600px;
  }

  .el-dialog >>> .el-dialog__body {
    padding: 10px 20px;
  }

</style>

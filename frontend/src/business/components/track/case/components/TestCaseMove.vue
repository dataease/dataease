<template>

  <el-dialog :title="$t('test_track.case.move')"
             :visible.sync="dialogVisible"
             :before-close="close"
             width="20%">

    <el-select v-model="module"
               :placeholder="$t('test_track.case.move')"
               filterable>
      <el-option v-for="item in moduleOptions" :key="item.id"
                 :label="item.path" :value="item.id"></el-option>
    </el-select>

    <template v-slot:footer>
      <ms-dialog-footer
        @cancel="close"
        @confirm="save"/>
    </template>


  </el-dialog>

</template>

<script>
  import MsDialogFooter from '../../../common/components/MsDialogFooter';
  import {listenGoBack, removeGoBackListener} from "../../../../../common/js/utils";

  export default {
    name: "TestCaseMove",
    components: {MsDialogFooter},
    data() {
      return {
        dialogVisible: false,
        module: '',
        moduleOptions: [],
        selectIds: []
      }
    },
    methods: {
      save() {
        if (this.module === '') {
          this.$warning(this.$t('test_track.case.input_module'));
          return;
        }
        let param = {};
        param.nodeId = this.module;
        this.moduleOptions.forEach(item => {
          if (item.id === this.module) {
            param.nodePath = item.path;
          }
        });
        param.ids = [...this.selectIds];
        this.$post('/test/case/batch/edit' , param, () => {
          this.$success(this.$t('commons.save_success'));
          this.close();
          this.$emit('refresh');
        });

      },
      open(moduleOptions, selectIds) {
        this.moduleOptions = moduleOptions;
        this.selectIds = selectIds;
        this.dialogVisible = true;
        listenGoBack(this.close);
      },
      close() {
        this.module = '';
        this.selectIds = [];
        this.dialogVisible = false;
        removeGoBackListener(this.close);
      }
    }
  }
</script>

<style scoped>

</style>

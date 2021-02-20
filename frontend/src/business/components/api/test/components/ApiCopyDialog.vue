<template>

  <el-dialog :title="$t('api_test.copy')"
             :visible.sync="dialogFormVisible"
             :before-close="close"
             width="30%">

    <el-row type="flex" justify="center">
      <el-col :span="18">
        <el-form :model="form" :rules="rules" ref="testForm" @submit.native.prevent>
          <el-form-item
            :label="$t('schedule.test_name')"
            :label-width="formLabelWidth"
            prop="name">
            <el-input v-model="form.name"></el-input>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>

    <template v-slot:footer>
      <ms-dialog-footer
        @cancel="close"
        @confirm="saveTest"/>
    </template>


  </el-dialog>

</template>

<script>


  import {listenGoBack, removeGoBackListener} from "../../../../../common/js/utils";
    import MsDialogFooter from "../../../common/components/MsDialogFooter";

    export default {
      components: {MsDialogFooter},
      data() {
        return {
          name: "MsApiCopyDialog",
          form: {
            name: '',
          },
          rules:{
            name :[
              {required: true, message: this.$t('test_track.case.input_name'), trigger: 'blur'},
              { max: 60, message: this.$t('test_track.length_less_than') + '60', trigger: 'blur' }
            ]
          },
          formLabelWidth: '80px',
          dialogFormVisible: false,
          test: {}
        }
      },
      props: {
        treeNodes: {
          type: Array
        },
        currentProject: {
          type: Object
        }
      },
      methods: {
        open(test) {
          listenGoBack(this.close);
          this.test = test;
          this.dialogFormVisible = true;
        },
        saveTest() {
          this.$refs['testForm'].validate((valid) => {
            if (valid) {
              this.$post("/api/copy", {projectId: this.test.projectId, id: this.test.id, name: this.form.name}, () => {
                this.$success(this.$t('commons.copy_success'));
                this.dialogFormVisible = false;
                this.$emit("refresh");
              });
            } else {
              return false;
            }
          });
        },
        close() {
          this.form.name = '';
          removeGoBackListener(this.close);
          this.dialogFormVisible = false;
        }
      }
    }
</script>

<style scoped>

</style>

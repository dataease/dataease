<template>

  <el-dialog :title="type == 'edit' ? $t('test_track.module.rename') : $t('test_track.module.add_module')"
             :visible.sync="dialogFormVisible"
             :before-close="close"
             width="30%">

    <el-row type="flex" justify="center">
      <el-col :span="18">
        <el-form :model="form" :rules="rules" ref="nodeForm" @submit.native.prevent>
          <el-form-item
            :label="$t('test_track.module.name')"
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
        @confirm="saveNode"/>
    </template>


  </el-dialog>

</template>

<script>
  import MsDialogFooter from '../../common/components/MsDialogFooter';
  import {listenGoBack, removeGoBackListener} from "../../../../common/js/utils";

    export default {
      components: {MsDialogFooter},
      data() {
        return {
          name: "NodeEdit",
          form: {
            name: '',
          },
          rules:{
            name :[
              {required: true, message: this.$t('test_track.case.input_name'), trigger: 'blur'},
              { max: 30, message: this.$t('test_track.length_less_than') + '30', trigger: 'blur' }
            ]
          },
          type: '',
          node: {},
          nodeIds: [],
          formLabelWidth: '80px',
          dialogFormVisible: false,
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
        open(type, data, nodeIds) {
          this.type = type;
          this.node = data;
          if (type == 'edit') {
            this.form.name = this.node.name;
          }
          this.nodeIds = nodeIds;
          listenGoBack(this.close);
          this.dialogFormVisible = true;
        },
        saveNode() {
          this.$refs['nodeForm'].validate((valid) => {
            if (valid) {
              let param = {};
              if (!this.currentProject) {
                this.$warning(this.$t('test_track.case.please_create_project'));
                return;
              }
              let url = this.buildParam(param);
              if (param.name.trim() == '') {
                this.$warning(this.$t('test_track.case.input_name'));
                return;
              }
              this.$post(url, param, () => {
                this.$success(this.$t('commons.save_success'));
                this.$emit('refresh');
                this.close();
              });
            } else {
              return false;
            }
          });
        },
        buildParam(param) {
          let url = '';
          if (this.type === 'add') {
            url = '/case/node/add';
            param.level = 1;
            if (this.node) {
              //非根节点
              param.parentId = this.node.id;
              param.level = this.node.level + 1;
            }
          } else if (this.type === 'edit') {
            url = '/case/node/edit';
            param.id = this.node.id;
            param.level = this.node.level;
            param.nodeIds = this.nodeIds;
          }
          param.name = this.form.name.trim();
          param.label = this.form.name;
          param.projectId = this.currentProject.id;
          return url;
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

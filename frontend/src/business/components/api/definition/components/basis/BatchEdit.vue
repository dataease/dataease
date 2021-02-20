<template>
  <div>
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="25%"
      class="batch-edit-dialog"
      :destroy-on-close="true"
      @close="handleClose"
    >
      <el-form :model="form" label-position="right" label-width="150px" size="medium" ref="form" :rules="rules">
        <el-form-item :label="$t('test_track.case.batch_update', [size])" prop="type">
          <el-select v-model="form.type" style="width: 80%" @change="changeType">
            <el-option v-for="(type, index) in typeArr" :key="index" :value="type.id" :label="type.name"/>
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.type ==='path'" :label="$t('test_track.case.updated_attr_value')" prop="value">
          <el-input size="small" v-model="form.value"/>
        </el-form-item>
        <el-form-item v-else :label="$t('test_track.case.updated_attr_value')" prop="value">
          <el-select v-model="form.value" style="width: 80%" :filterable="filterable">
            <el-option v-for="(option, index) in options" :key="index" :value="option.id" :label="option.label">
              <div v-if="option.email">
                <span>{{option.id}}({{option.name}})</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template v-slot:footer>
        <ms-dialog-footer
          @cancel="dialogVisible = false"
          @confirm="submit('form')"/>
      </template>
    </el-dialog>
  </div>
</template>

<script>
  import MsDialogFooter from "../../../../common/components/MsDialogFooter";
  import {listenGoBack, removeGoBackListener} from "@/common/js/utils";

  export default {
    name: "BatchEdit",
    components: {
      MsDialogFooter
    },
    props: {
      typeArr: Array,
      valueArr: Object,
      dialogTitle: {
        type: String,
        default() {
          return this.$t('api_test.definition.request.batch_edit')
        }
      }
    },
    data() {
      return {
        dialogVisible: false,
        form: {},
        size: 0,
        rules: {
          type: {required: true, message: this.$t('test_track.case.please_select_attr'), trigger: ['blur', 'change']},
          value: {required: true, message: this.$t('test_track.case.please_select_attr_value'), trigger: ['blur', 'change']}
        },
        options: [],
        filterable: false,
      }
    },
    methods: {
      submit(form) {
        this.$refs[form].validate((valid) => {
          if (valid) {
            this.$emit("batchEdit", this.form);
            this.dialogVisible = false;
          } else {
            return false;
          }
        });
      },
      open() {
        this.dialogVisible = true;
        this.size = this.$parent.selectDataCounts;
        listenGoBack(this.handleClose);
      },
      handleClose() {
        this.form = {};
        this.options = [];
        removeGoBackListener(this.handleClose);
      },
      changeType(val) {
        this.$set(this.form, "value", "");
        this.filterable = val === "maintainerOptions";
        this.options = this.valueArr[val];
      }
    }
  }
</script>

<style scoped>

</style>

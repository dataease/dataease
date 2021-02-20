<template>
  <el-dialog :close-on-click-modal="false"
             :title="title"
             :visible.sync="dialogVisible"
             class="delete-confirm" >

    <el-row>
      <el-col>
        <span>{{$t('commons.delete_confirm')}}</span>
        <span class="delete-tip"> DELETE-{{record.name}}</span>
        <br/>
      </el-col>
    </el-row>
    <el-row class="tip" v-if="withTip">
      <span>
       <slot class="tip"></slot>
      </span>
    </el-row>
    <el-row>
      <el-col :span="15">
        <el-input v-model="value" :placeholder="$t('commons.input_content')"/>
      </el-col>
    </el-row>

    <span slot="footer" class="dialog-footer">
      <el-button @click="dialogVisible = false">{{$t('commons.cancel')}}</el-button>
      <el-button type="primary" @click="confirm">{{$t('commons.confirm')}}</el-button>
    </span>

  </el-dialog>
</template>

<script>
    export default {
      name: "MsDeleteConfirm",
      data() {
        return {
          dialogVisible: false,
          value: '',
          record: {},
        }
      },
      props: {
        title: {
          type: String,
          default() {
            return this.$t('commons.title')
          }
        },
        withTip: {
          type: Boolean,
          default() {
            return false
          }
        }
      },
      methods: {
        open(record) {
          this.dialogVisible = true;
          this.value = '';
          this.record = record;
        },
        confirm() {
          if (this.value.trim() != 'DELETE-' + this.record.name) {
            this.$warning(this.$t('commons.incorrect_input'));
            return;
          }
          this.$emit('delete', this.record);
          this.dialogVisible = false;
        }
      }
    }
</script>

<style scoped>

  .delete-confirm >>> .el-dialog {
    width: 500px;
  }

  .delete-confirm .el-dialog:first-child {
    margin-bottom: 10px;
  }

  .delete-confirm .el-row:first-child {
    margin-bottom: 20px;
  }

  .delete-tip {
    font-style: italic;
    font-weight: bold;
  }

  .tip {
    margin-bottom: 20px;
    color: red;
  }

</style>

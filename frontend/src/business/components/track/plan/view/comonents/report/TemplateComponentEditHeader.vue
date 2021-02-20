<template>
  <el-row type="flex" class="head-bar">

    <el-col :span="12">
      <div class="name-edit">
        <el-input :placeholder="$t('test_track.plan_view.input_template_name')" v-model="template.name" @change="change"/>
        <span v-if="template.name" class="title">{{template.name}}</span>
        <span class="name-tip" v-if="!template.name">{{$t('test_track.plan_view.input_template_name')}}</span>
      </div>
    </el-col>
    <el-col :span="12" class="head-right">
      <el-button plain size="mini" @click="handleCancel">{{$t('test_track.return')}}</el-button>
      <el-button type="primary" size="mini" @click="handleSave">{{$t('test_track.save')}}</el-button>
    </el-col>

  </el-row>

</template>

<script>
    export default {
      name: "TemplateComponentEditHeader",
      data() {
        return {

        }
      },
      props: {
        template: {
          type: Object,
          default() {
            return {}
          }
        },
      },
      methods: {
        handleCancel() {
          this.$emit('cancel');
        },
        handleSave() {
          this.$emit('save');
        },
        change() {
          this.$emit('update:template', this.templateName);
        }
      }
    }
</script>

<style scoped>

  .head-right {
    text-align: right;
  }

  .head-bar {
    background: white;
    height: 45px;
    line-height: 45px;
    padding: 0 10px;
    border: 1px solid #EBEEF5;
    box-shadow: 0 0 2px 0 rgba(31,31,31,0.15), 0 1px 2px 0 rgba(31,31,31,0.15);
  }

  .name-edit:hover span {
    display: none;
  }

  .name-edit .el-input {
    display: none;
    width: 200px;
  }

  .name-edit:hover .el-input{
    display: inline-block;
  }

  .name-tip {
    color: #909399;
  }

</style>

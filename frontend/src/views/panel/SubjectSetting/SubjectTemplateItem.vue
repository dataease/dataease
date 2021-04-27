<template>
  <div class="subject-template">
    <div class="vertical-layout">
      <i class="el-icon-error" />
      <i class="el-icon-edit" />
      <!-- 背景-->
      <div style="inset: 0px; position: absolute; background: 0% 0% / cover rgb(239, 241, 244);" />

      <!-- 视图组件 背景-->
      <div style="inset: 17px 10px 10px; position: absolute; background: 0% 0% / cover rgb(255, 255, 255);" />
      <!-- 视图组件 主题-->
      <div style="inset: 20px 13px 15px; position: absolute;">
        <div style="position: absolute; inset: 0px 4px; width: auto; height: auto;">
          <!--柱形-->
          <div style="left: 0px; top: 11px; bottom: 0px; width: 3px; position: absolute; background-color: rgb(255, 191, 83);" />
          <div style="left: 5px; top: 6px; bottom: 0px; width: 3px; position: absolute; background-color: rgb(4, 177, 194);" />
          <div style="left: 10px; top: 2px; bottom: 0px; width: 3px; position: absolute; background-color: rgb(240, 116, 116);" />

          <!--柱形-->
          <div style="left: 20px; top: 11px; bottom: 0px; width: 3px; position: absolute; background-color: rgb(255, 191, 83);" />
          <div style="left: 25px; top: 2px; bottom: 0px; width: 3px; position: absolute; background-color: rgb(4, 177, 194);" />
          <div style="left: 30px; top: 6px; bottom: 0px; width: 3px; position: absolute; background-color: rgb(240, 116, 116);" />

          <!--柱形-->
          <div style="left: 40px; top: 2px; bottom: 0px; width: 3px; position: absolute; background-color: rgb(255, 191, 83);" />
          <div style="left: 45px; top: 6px; bottom: 0px; width: 3px; position: absolute; background-color: rgb(4, 177, 194);" />
          <div style="left: 50px; top: 11px; bottom: 0px; width: 3px; position: absolute; background-color: rgb(240, 116, 116);" />

          <!--柱形-->
          <div style="left: 60px; top: 6px; bottom: 0px; width: 3px; position: absolute; background-color: rgb(255, 191, 83);" />
          <div style="left: 65px; top: 11px; bottom: 0px; width: 3px; position: absolute; background-color: rgb(4, 177, 194);" />
          <div style="left: 70px; top: 2px; bottom: 0px; width: 3px; position: absolute; background-color: rgb(240, 116, 116);" />
        </div>
      </div>

      <!-- 表格表头颜色 -->
      <div style="left: 10px; right: 10px; top: 10px; height: 6px; position: absolute; background: 0% 0% / cover rgb(255, 255, 255);" />

      <!-- 字体颜色 -->
      <div style="left: 14px; top: 10px; height: 6px; position: absolute; vertical-align: middle">
        <div style="width: 1px; height: 2px; position: relative; flex-shrink: 0; margin-top: 2px;margin-right: 1px; background-color: rgb(26, 26, 26);float: left;" />
        <div style="width: 1px; height: 2px; position: relative; flex-shrink: 0; margin-top: 2px;margin-right: 1px; background-color: rgb(26, 26, 26);float: left;" />
        <div style="width: 1px; height: 2px; position: relative; flex-shrink: 0; margin-top: 2px;margin-right: 1px; background-color: rgb(26, 26, 26);float: left;" />
        <div style="width: 1px; height: 2px; position: relative; flex-shrink: 0; margin-top: 2px;margin-right: 1px; background-color: rgb(26, 26, 26);float: left;" />
        <div style="width: 1px; height: 2px; position: relative; flex-shrink: 0; margin-top: 2px;margin-right: 1px; background-color: rgb(26, 26, 26);float: left;" />
        <div style="width: 1px; height: 2px; position: relative; flex-shrink: 0; margin-top: 2px;margin-right: 1px; background-color: rgb(26, 26, 26);float: left;" />
      </div>

    </div>
    <div style="position: absolute; left: 0px; right: 0px; bottom: 0px; height: 30px;">
      <div style=" background-color:#f7f8fa;color:#3d4d66;font-size:12px;height: 30px; line-height: 30px; text-align: center; white-space: pre; text-overflow: ellipsis; margin-left: 1px; margin-right: 1px;">
        <span style="margin-top: 8px">Preset style 1</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'StyleTemplateItem',
  props: {
    template: {
      type: Object,
      default() {
        return {}
      }
    }
  },
  computed: {
    classBackground() {
      return {
        background: `url(${this.template.snapshot}) no-repeat`,
        'background-size': `100% 100%`
      }
    }
  },
  methods: {
    templateDelete() {
      this.$alert('是否删除模板：' + this.template.name + '？', '', {
        confirmButtonText: '确认',
        callback: (action) => {
          if (action === 'confirm') {
            this.$emit('templateDelete', this.template.id)
          }
        }
      })
    },
    templateEdit() {
      this.$emit('templateEdit', this.template)
    },
    handleDelete() {
      console.log('handleDelete')
    }
  }
}
</script>

<style scoped>

  .subject-template {
    width: 110px;
    height: 90px;
    position: relative;
    float: left;
    margin: 5px;
    display: inline-block;
  }

  .demonstration {
    display: block;
    text-align: center;
    margin: 10px auto;
    width: 150px;
    white-space:nowrap;
    overflow:hidden;
    text-overflow:ellipsis;
  }

  .vertical-layout {
    overflow: hidden auto;
    position: absolute;
    inset: 0px 0px 30px;
    width: 108px; height: 58px;
    margin: 0 auto;
    box-shadow: 0 0 2px 0 rgba(31,31,31,0.15), 0 1px 2px 0 rgba(31,31,31,0.15);
    border: solid 1px #fff;
    box-sizing: border-box;
    border-radius: 3px;
  }

  .vertical-layout:hover {
    border: solid 1px #4b8fdf;
    border-radius: 3px;
    color: deepskyblue;
    cursor: pointer;
  }

  .vertical-layout > i{
    float: right;
    color: gray;
    margin: 2px;
    position: relative;
  }

  .vertical-layout > i:hover {
    color: red;
  }

  .vertical-layout:hover > .el-icon-error {
    z-index: 10;
  }

  .vertical-layout:hover > .el-icon-edit {
    z-index: 10;
  }

</style>

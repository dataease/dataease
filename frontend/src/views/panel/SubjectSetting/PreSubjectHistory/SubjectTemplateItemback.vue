<template>
  <div class="subject-template">
    <div class="vertical-layout">
      <i class="el-icon-error" />
      <i class="el-icon-edit" />
      <!-- 背景-->
      <div style="inset: 0px; position: absolute;" :style="customBackground" />
      <!-- 视图组件 背景-->
      <div style="inset: 17px 10px 10px; position: absolute; background: 0% 0% / cover rgb(255, 255, 255);" />
      <!-- 视图组件 主题-->
      <div style="inset: 20px 13px 15px; position: absolute;">
        <div style="position: absolute; inset: 0px 4px; width: auto; height: auto;">
          <!--柱形-->
          <div style="left: 0px; top: 11px; bottom: 0px; width: 3px; position: absolute;" :style="columnBackgroundLeft" />
          <div style="left: 5px; top: 6px; bottom: 0px; width: 3px; position: absolute; " :style="columnBackgroundMiddle" />
          <div style="left: 10px; top: 2px; bottom: 0px; width: 3px; position: absolute;" :style="columnBackgroundRight" />

          <!--柱形-->
          <div style="left: 20px; top: 11px; bottom: 0px; width: 3px; position: absolute;" :style="columnBackgroundLeft" />
          <div style="left: 25px; top: 2px; bottom: 0px; width: 3px; position: absolute; " :style="columnBackgroundMiddle" />
          <div style="left: 30px; top: 6px; bottom: 0px; width: 3px; position: absolute; " :style="columnBackgroundRight" />

          <!--柱形-->
          <div style="left: 40px; top: 2px; bottom: 0px; width: 3px; position: absolute;" :style="columnBackgroundLeft" />
          <div style="left: 45px; top: 6px; bottom: 0px; width: 3px; position: absolute; " :style="columnBackgroundMiddle" />
          <div style="left: 50px; top: 11px; bottom: 0px; width: 3px; position: absolute; " :style="columnBackgroundRight" />

          <!--柱形-->
          <div style="left: 60px; top: 6px; bottom: 0px; width: 3px; position: absolute;" :style="columnBackgroundLeft" />
          <div style="left: 65px; top: 11px; bottom: 0px; width: 3px; position: absolute; " :style="columnBackgroundMiddle" />
          <div style="left: 70px; top: 2px; bottom: 0px; width: 3px; position: absolute; " :style="columnBackgroundRight" />
        </div>
      </div>

      <!-- 表格表头颜色 -->
      <div style="left: 10px; right: 10px; top: 10px; height: 6px; position: absolute;" :style="tableHeadBackground" />

      <!-- 字体颜色 -->
      <div style="left: 14px; top: 10px; height: 6px; position: absolute; vertical-align: middle">
        <div style="width: 1px; height: 2px; position: relative; flex-shrink: 0; margin-top: 2px;margin-right: 1px; float: left;" :style="tableFontColor" />
        <div style="width: 1px; height: 2px; position: relative; flex-shrink: 0; margin-top: 2px;margin-right: 1px; float: left;" :style="tableFontColor" />
        <div style="width: 1px; height: 2px; position: relative; flex-shrink: 0; margin-top: 2px;margin-right: 1px; float: left;" :style="tableFontColor" />
        <div style="width: 1px; height: 2px; position: relative; flex-shrink: 0; margin-top: 2px;margin-right: 1px; float: left;" :style="tableFontColor" />
        <div style="width: 1px; height: 2px; position: relative; flex-shrink: 0; margin-top: 2px;margin-right: 1px; float: left;" :style="tableFontColor" />
        <div style="width: 1px; height: 2px; position: relative; flex-shrink: 0; margin-top: 2px;margin-right: 1px; float: left;" :style="tableFontColor" />
      </div>

    </div>
    <div style="position: absolute; left: 0px; right: 0px; bottom: 0px; height: 30px;">
      <div style=" background-color:#f7f8fa;color:#3d4d66;font-size:12px;height: 30px; line-height: 30px; text-align: center; white-space: pre; text-overflow: ellipsis; margin-left: 1px; margin-right: 1px;">
        <span style="margin-top: 8px">{{ subjectItem.name }}</span>
      </div>
    </div>
  </div>
</template>

<script>
import { chartTransStr2Object } from '@/views/panel/panel'
import {imgUrlTrans} from "@/components/canvas/utils/utils";
export default {
  name: 'StyleTemplateItem',
  props: {
    subjectItem: {
      type: Object,
      default() {
        return null
      }
    }
  },
  data() {
    return {
      defaultSubject: {

      },
      subjectItemDetails: null
    }
  },
  computed: {
    customBackground() {
      let style = {
        background: '0% 0% / cover rgb(239, 241, 244)'
      }
      if (this.subjectItemDetails) {
        if (this.subjectItemDetails.panel.backgroundType === 'image' && this.subjectItemDetails.panel.imageUrl) {
          style = {
            width: '100%',
            height: '100%',
            background: `url(${imgUrlTrans(this.subjectItemDetails.panel.imageUrl)}) no-repeat`
          }
        } else {
          style = {
            width: '100%',
            height: '100%',
            background: this.subjectItemDetails.panel.color
          }
        }
      }
      return style
    },
    columnBackgroundLeft() {
      let style = {}
      if (this.subjectItemDetails) {
        style = {
          background: this.subjectItemDetails.chart.customAttr.color.colors[0]
        }
      }
      return style
    },
    columnBackgroundMiddle() {
      let style = {}
      if (this.subjectItemDetails) {
        style = {
          background: this.subjectItemDetails.chart.customAttr.color.colors[1]
        }
      }
      return style
    },
    columnBackgroundRight() {
      let style = {}
      if (this.subjectItemDetails) {
        style = {
          background: this.subjectItemDetails.chart.customAttr.color.colors[2]
        }
      }
      return style
    },
    tableHeadBackground() {
      let style = {}
      if (this.subjectItemDetails) {
        style = {
          background: this.subjectItemDetails.chart.customAttr.color.tableHeaderBgColor
        }
      }
      return style
    },
    tableFontColor() {
      let style = {}
      if (this.subjectItemDetails) {
        style = {
          background: this.subjectItemDetails.chart.customAttr.color.tableFontColor
        }
      }
      return style
    }
  },
  watch: {
    subjectItem: {
      handler(newVal, oldVla) {
        this.subjectItemDetails = chartTransStr2Object(newVal.details, 'Y')
      },
      deep: true
    },
    resize() {
      this.drawEcharts()
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

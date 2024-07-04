<template>
  <span style="display: inline-block">
    <el-popover
      placement="bottom"
      trigger="hover"
      width="300"
      :popper-class="previewVisible ? 'remark-pop' : ''"
      :visible-arrow="false"
    >
      <div
        class="remark-style"
        :style="{ backgroundColor: remarkCfg.bgFill }"
        v-html="xssRemarkCfgContent"
      />
      <i
        slot="reference"
        class="el-icon-info"
        style="
          cursor: pointer;
          color: gray;
          font-size: 12px;
          position: relative;
          z-index: 10;
        "
      />
    </el-popover>
  </span>
</template>

<script>
import { mapState } from "vuex";
import xss from "xss";
export default {
  name: "TitleRemark",
  props: {
    remarkCfg: {
      type: Object,
      required: true,
    },
  },
  computed: {
    ...mapState(["previewVisible"]),
    xssRemarkCfgContent() {
      const myXss = new xss.FilterXSS({
        css: {
          whiteList: {
            "background-color": true,
            "text-align": true,
            color: true,
            "margin-top": true,
            "margin-bottom": true,
            "line-height": true,
            "box-sizing": true,
            "padding-top": true,
            "padding-bottom": true,
          },
        },
        whiteList: {
          ...xss.whiteList,
          p: ["style"],
          span: ["style"],
        },
      });
      return myXss.process(this.remarkCfg.content);
    },
  },
};
</script>

<style scoped>
.remark-style {
  position: relative;
  z-index: 10;
  min-height: 100px;
  max-height: 200px;
  overflow-y: auto;
  padding: 10px;
  color: #000000;
  border-radius: 4px;
}
::-webkit-scrollbar {
  width: 0px !important;
  height: 0px !important;
}
::v-deep ol {
  display: block !important;
  list-style-type: decimal;
  margin-block-start: 1em !important;
  margin-block-end: 1em !important;
  margin-inline-start: 0px !important;
  margin-inline-end: 0px !important;
  padding-inline-start: 40px !important;
}
::v-deep ul {
  display: block !important;
  list-style-type: disc;
  margin-block-start: 1em !important;
  margin-block-end: 1em !important;
  margin-inline-start: 0px !important;
  margin-inline-end: 0px !important;
  padding-inline-start: 40px !important;
}
::v-deep li {
  display: list-item !important;
  text-align: -webkit-match-parent !important;
}
</style>

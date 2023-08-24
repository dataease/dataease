<template>
  <div
    style="
      overflow: auto;
      border-right: 1px solid #e6e6e6;
      height: 100%;
      width: 100%;
    "
    class="attr-style theme-border-class"
  >
    <el-row>
      <span class="padding-lr">{{ $t("chart.shape_attr") }}</span>
      <el-collapse v-model="attrActiveNames" class="style-collapse">
        <el-collapse-item name="color" :title="$t('chart.color')">
          <color-selector
            :param="param"
            class="attr-selector"
            :chart="chart"
            @onColorChange="onColorChange"
          />
        </el-collapse-item>

        <el-collapse-item name="label" :title="$t('chart.label')">
          <label-selector
            :param="param"
            class="attr-selector"
            :chart="chart"
            @onLabelChange="onLabelChange"
          />
        </el-collapse-item>

        <el-collapse-item name="tooltip" :title="$t('chart.tooltip')">
          <tooltip-selector
            :param="param"
            class="attr-selector"
            :chart="chart"
            @onTooltipChange="onTooltipChange"
          />
        </el-collapse-item>

        <el-collapse-item name="suspension" :title="$t('chart.suspension')">
          <suspension-selector
            :param="param"
            class="attr-selector"
            :chart="chart"
            @onSuspensionChange="onSuspensionChange"
          />
        </el-collapse-item>
      </el-collapse>
    </el-row>

    <el-row>
      <span class="padding-lr">{{ $t("chart.module_style") }}</span>
      <el-collapse v-model="styleActiveNames" class="style-collapse">
        <el-collapse-item
          v-show="view.type"
          name="title"
          :title="$t('chart.title')"
        >
          <title-selector
            :param="param"
            class="attr-selector"
            :chart="chart"
            @onTextChange="onTextChange"
          />
        </el-collapse-item>
      </el-collapse>
    </el-row>
  </div>
</template>

<script>
import ColorSelector from "@/components/selector/ColorSelector";
import LabelSelector from "@/components/selector/LabelSelector";
import TitleSelector from "@/components/selector/TitleSelector";
import TooltipSelector from "@/components/selector/TooltipSelector";
import SuspensionSelector from "@/components/selector/SuspensionSelector";
export default {
  components: {
    ColorSelector,
    LabelSelector,
    TitleSelector,
    TooltipSelector,
    SuspensionSelector,
  },
  data() {
    return {
      attrActiveNames: [],
      styleActiveNames: [],
    };
  },
  props: {
    obj: {
      type: Object,
      default: () => {},
    },
  },

  computed: {
    param() {
      return this.obj.param;
    },
    view() {
      return this.obj.view;
    },
    chart() {
      return this.obj.chart;
    },
  },
  methods: {
    onColorChange(val) {
      this.view.customAttr.color = val;
      this.calcStyle();
    },
    onLabelChange(val) {
      this.view.customAttr.label = val;
      this.calcStyle();
    },

    onTooltipChange(val) {
      this.view.customAttr.tooltip = val;
      this.calcStyle();
    },
    onSuspensionChange(val) {
      this.view.customAttr.suspension = val;
      this.calcStyle();
    },
    onTextChange(val) {
      this.view.customStyle.text = val;
      this.view.title = val.title;
      this.calcStyle();
    },
    onChangeBackgroundForm(val) {
      this.view.customStyle.background = val;
      this.calcStyle();
    },
    calcStyle() {
      this.$emit("plugin-call-back", {
        eventName: "plugins-calc-style",
        eventParam: this.view,
      });
    },
  },
};
</script>

<style lang="scss" scoped>
.padding-lr {
  padding: 0 6px;
}
span {
  font-size: 12px;
}
.el-radio {
  margin: 5px;
}
.radio-span ::v-deep .el-radio__label {
  margin-left: 2px;
}
</style>

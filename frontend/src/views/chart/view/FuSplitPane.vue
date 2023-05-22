<template>
  <div :style="{ cursor, userSelect }" class="fu-split-pane" ref="outerWrapper">
    <div
      :class="[`is-${direction}`, 'fu-split-pane__left']"
      :style="{
        [attr]: isReverse ? valueAnother : `${value}px`,
        'padding-right': padding,
      }"
    >
      <slot :name="isHorizontal ? 'left' : 'top'"></slot>
    </div>
    <div
      :class="resizerClasses"
      :style="{ [resizerAttr]: `${value}px`, ...resizerStyle }"
      @mousedown="onMouseDown"
      @mouseover="hover = true"
      @mouseleave="hover = false"
    >
      <div class="icon" v-if="resizerType === 'resizer'">
        <slot name="resizer">
          <i class="el-icon-more"></i>
        </slot>
      </div>
    </div>
    <div
      :class="[`is-${direction}`, 'fu-split-pane__right']"
      :style="{
        [attr]: isReverse ? `${value}px` : valueAnother,
        'padding-left': padding,
      }"
    >
      <slot :name="isHorizontal ? 'right' : 'bottom'"></slot>
    </div>
  </div>
</template>

<script>
export default {
  name: "FuSplitPane",
  props: {
    min: {
      type: [Number, String],
      default: "10px",
    },
    left: [Number, String],
    right: [Number, String],
    top: [Number, String],
    bottom: [Number, String],
    direction: {
      validator: (val) => ["vertical", "horizontal"].includes(val),
      default: "horizontal",
    },
    localKey: String,
    resizable: {
      type: Boolean,
      default: true,
    },
    resizerType: {
      validator: (val) => ["resizer", "line"].includes(val),
      default: "resizer",
    },
    resizerClass: String,
    resizerStyle: Object,
    resizerHoverClass: String,
  },
  watch: {
    left: {
      immediate: true,
      handler: function (newValue, oldValue) {
        if (newValue !== oldValue) {
          this.$nextTick(() => {
            this.value = this.defaultValue;
          });
        }
      },
    },
    bottom: {
      immediate: true,
      handler: function (newValue, oldValue) {
        if (newValue !== oldValue) {
          this.$nextTick(() => {
            this.value = this.defaultValue;
          });
        }
      },
    },
  },
  computed: {
    isReverse() {
      return this.right || this.bottom;
    },
    isHorizontal() {
      return this.direction === "horizontal";
    },
    userSelect() {
      return this.active ? "none" : "";
    },
    cursor() {
      return this.active && this.resizable
        ? this.isHorizontal
          ? "col-resize"
          : "row-resize"
        : "";
    },
    outerWrapperSize() {
      return this.$refs.outerWrapper[this.offsetSize];
    },
    offsetSize() {
      return this.isHorizontal ? "offsetWidth" : "offsetHeight";
    },
    defaultValue() {
      if (this.isHorizontal) {
        return this.left
          ? this.getMin(this.percentToValue(this.left))
          : (this.right && this.getMin(this.percentToValue(this.right))) ||
              this.outerWrapperSize / 2;
      } else {
        return this.top
          ? this.getMin(this.percentToValue(this.top))
          : (this.bottom && this.getMin(this.percentToValue(this.bottom))) ||
              this.outerWrapperSize / 2;
      }
    },
    valueAnother() {
      return `calc(100% - ${this.value}px)`;
    },
    attr() {
      return this.isHorizontal ? "width" : "height";
    },
    resizerAttr() {
      return this.isHorizontal
        ? this.isReverse
          ? "right"
          : "left"
        : this.isReverse
        ? "bottom"
        : "top";
    },
    saveKey({ localKey }) {
      return "Fu-SP-" + localKey;
    },
    resizerClasses() {
      const classes = [
        `fu-split-pane__${this.resizerType}`,
        `is-${this.direction}`,
        this.resizable && "is-resizable",
        this.resizerClass,
        this.hover && (this.resizerHoverClass || "hover"),
      ];
      return classes;
    },
    padding() {
      return this.resizerType === "resizer" && "3px";
    },
  },
  data() {
    return {
      active: false,
      value: 0,
      oldValue: 0,
      initOffset: 0,
      hover: false,
    };
  },
  mounted() {
    this.readValue();
  },
  methods: {
    onMouseDown(e) {
      this.initOffset = this.isHorizontal ? e.pageX : e.pageY;
      this.oldValue = this.value;
      this.active = true;
      document.addEventListener("mousemove", this.onMouseMove);
      document.addEventListener("mouseup", this.onMouseUp);
    },
    onMouseUp() {
      this.active = false;
      document.removeEventListener("mousemove", this.onMouseMove);
      document.removeEventListener("mouseup", this.onMouseUp);
      this.$emit("changeSplit", this.value);
    },
    onMouseMove(e) {
      if (!this.resizable) return;
      if (this.active) {
        const currentPage = this.isHorizontal ? e.pageX : e.pageY;
        const offset = currentPage - this.initOffset;
        const value = this.isReverse
          ? this.oldValue - offset
          : this.oldValue + offset;
        if (
          value > this.percentToValue(this.min) &&
          value < this.outerWrapperSize - this.percentToValue(this.min)
        ) {
          this.value = value;
          this.writeValue();
        }
      }
    },
    // 百分比换算成像素
    percentToValue(val) {
      const size = this.$refs.outerWrapper[this.offsetSize];
      if (typeof val === "string" && val.includes("%")) {
        return (parseInt(val) / 100) * size;
      } else {
        return parseInt(val);
      }
    },
    // 是否取最小值
    getMin(val) {
      return val < this.percentToValue(this.min)
        ? this.percentToValue(this.min)
        : val;
    },
    // localStorage储存数值
    writeValue() {
      const obj = {
        [this.resizerAttr]: this.value,
      };
      if (this.localKey) {
        localStorage.setItem(this.saveKey, JSON.stringify(obj));
      }
    },
    readValue() {
      if (this.localKey) {
        const local = localStorage.getItem(this.saveKey);
        if (local && local[this.resizerAttr]) {
          this.value = parseInt(local) || this.defaultValue;
        } else {
          this.value = this.defaultValue;
        }
      } else {
        this.value = this.defaultValue;
      }
    },
  },
};
</script>

<template>
  <button
    class="de-button"
    @click="handleClick"
    :disabled="buttonDisabled || loading"
    :class="[
      type ? 'de-button--' + type : '',
      {
        'is-de-plain': plain,
        'is-de-secondary': secondary,
        'is-de-disabled': buttonDisabled,
        'is-de-loading': loading,
      },
    ]"
  >
    <!-- <svg
      class="de-circular"
      v-if="loading"
      width="14"
      height="14"
      viewBox="0 0 14 14"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
    >
      <path
        d="M3.04575 3.00511C3.11074 2.94007 3.1879 2.88847 3.27283 2.85326C3.35776 2.81804 3.4488 2.7999 3.54074 2.79987C3.63269 2.79983 3.72374 2.81791 3.80869 2.85306C3.89365 2.88822 3.97085 2.93976 4.03589 3.00475C4.10093 3.06974 4.15253 3.14691 4.18774 3.23184C4.22296 3.31677 4.2411 3.40781 4.24113 3.49975C4.24117 3.5917 4.22309 3.68275 4.18794 3.7677C4.15278 3.85266 4.10124 3.92986 4.03625 3.9949C3.64356 4.38689 3.33222 4.85263 3.12013 5.36535C2.90804 5.87807 2.79938 6.42765 2.80039 6.9825C2.80039 9.31246 4.68128 11.2 7.00039 11.2C9.3195 11.2 11.2004 9.31246 11.2004 6.9825C11.2004 6.79685 11.2741 6.6188 11.4054 6.48753C11.5367 6.35625 11.7147 6.2825 11.9004 6.2825C12.086 6.2825 12.2641 6.35625 12.3954 6.48753C12.5266 6.6188 12.6004 6.79685 12.6004 6.9825C12.6004 10.0846 10.0937 12.6 7.00039 12.6C3.90709 12.6 1.40039 10.0846 1.40039 6.9825C1.40039 5.4705 1.99959 4.05195 3.04575 3.00511Z"
        fill="#3370ff"
      />
    </svg> -->
    <svg v-if="loading" viewBox="25 25 50 50" class="de-circular">
      <circle cx="50" cy="50" r="20" fill="none" class="path"></circle>
    </svg>
    <i :class="icon" v-if="icon && !loading"></i>
    <span :class="[{'de-btn-mar5': icon || loading }]" v-if="$slots.default"><slot></slot></span>
  </button>
</template>
<script>
export default {
  name: "DeButton",
  inject: {
    elForm: {
      default: "",
    },
  },
  props: {
    type: {
      type: String,
      default: "default",
    },
    size: String,
    icon: {
      type: String,
      default: "",
    },
    loading: Boolean,
    disabled: Boolean,
    plain: Boolean,
    secondary: Boolean,
  },

  computed: {
    buttonDisabled() {
      return this.$options.propsData.hasOwnProperty("disabled")
        ? this.disabled
        : (this.elForm || {}).disabled;
    },
  },

  methods: {
    handleClick(evt) {
      this.$emit("click", evt);
    },
  },
};
</script>
<style lang="scss">
$namespace: "de";
$state-prefix: "is-de-";
$modifier-separator: "--";
@mixin b($block) {
  $B: $namespace + "-" + $block !global;

  .#{$B} {
    @content;
  }
}
@mixin when($state) {
  @at-root {
    &.#{$state-prefix + $state} {
      @content;
    }
  }
}

@mixin m($modifier) {
  $selector: &;
  $currentSelector: "";
  @each $unit in $modifier {
    $currentSelector: #{$currentSelector +
      & +
      $modifier-separator +
      $unit +
      ","};
  }

  @at-root {
    #{$currentSelector} {
      @content;
    }
  }
}

@mixin button-variant($color, $background-color, $hover, $active, $loading) {
  color: $color;
  background-color: $background-color;
  border-color: $background-color;

  &:hover,
  &:focus {
    background: $hover;
    border-color: $hover;
    color: $color;
  }

  &:active {
    background: $active;
    border-color: $active;
    color: $color;
    outline: none;
  }

  &.is-de-loading {
    &,
    &:hover,
    &:focus,
    &:active {
      color: $color;
      background-color: $loading;
      border-color: $loading;
    }
  }

  &.is-de-disabled {
    &,
    &:hover,
    &:focus,
    &:active {
      color: $color;
      background-color: mix(#fff, #bbbfc4, 40%);
      border-color: mix(#fff, #bbbfc4, 40%);
    }
  }
}
@include b(button) {
  font-family: PingFang SC;
  font-size: 14px;
  font-weight: 400;
  line-height: 1;
  white-space: nowrap;
  cursor: pointer;
  -webkit-appearance: none;
  text-align: center;
  box-sizing: border-box;
  outline: none;
  margin: 0;
  height: 32px;
  transition: 0.1s;
  min-width: 80px;
  display: inline-flex;
  justify-content: center;
  align-items: center;
  border: none;
  border-radius: 4px;
  padding: 0 16px;

  .de-circular {
    height: 14px;
    width: 14px;
    animation: loading-rotate 2s linear infinite;
  }

  .path {
    animation: loading-dash 1.5s ease-in-out infinite;
    stroke-dasharray: 90, 150;
    stroke-dashoffset: 0;
    stroke-width: 3;
    stroke: #3370ff;
    stroke-linecap: round;
  }

  & + & {
    margin-left: 12px;
  }

  &::-moz-focus-inner {
    border: 0;
  }

  .de-btn-mar5 {
    margin-left: 5.17px;
  }

  @include when(secondary) {
    color: var(--deTextPrimary, #1F2329);
    background-color: var(--deWhite, #ffffff);
    border: 1px solid;
    border-color: var(--deComponentStrokeColor, #BBBFC4);

    &:hover,
    &:focus {
      background: var(--deComBorderColorHover, #ffffff);
      border-color: var(--deComponentStrokeColor, #BBBFC4);
    }

    &:active {
      background: var(--deComBorderColorActive, #ffffff);
      border-color: var(--deComponentStrokeColor, #BBBFC4);
      outline: none;
    }

    &.is-de-loading {
      &,
      &:hover,
      &:focus,
      &:active {
        color: var(--deTextPlaceholder, #3370FF);
        background-color: var(--deWhite, #ffffff);
        border-color: var(--deComponentStrokeColor, #BBBFC4);
      }
    }

    &.is-de-disabled {
      &,
      &:hover,
      &:focus,
      &:active {
        color: #8f959e;
        background-color: #ffffff;
        border-color: #8f959e;
      }
    }
  }
  

  @include when(plain) {
    color: var(--primary, #3370FF);
    background-color: var(--deWhite, #ffffff);
    border: 1px solid;
    border-color: var(--primary, #3370FF);

    &:hover,
    &:focus {
      background: var(--deWhiteHover, #ffffff);
      border-color: var(--primary, #3370FF);
    }

    &:active {
      background: var(--deWhiteActive, #ffffff);
      border-color: var(--primary, #3370FF);
      outline: none;
    }

    &.is-de-loading {
      &,
      &:hover,
      &:focus,
      &:active {
        color: var(--primary, #3370FF);
        background-color: var(--deWhite, #ffffff);
        border-color: var(--primary, #3370FF);
        opacity: .6;
      }
    }

    &.is-de-disabled {
      &,
      &:hover,
      &:focus,
      &:active {
        color: #8f959e;
        background-color: #ffffff;
        border-color: #8f959e;
      }
    }
  }


    @include when(disabled) {
      &,
      &:hover,
      &:focus {
        cursor: not-allowed;
        background-image: none;
      }

    }

    // @include when(loading) {
    //   position: relative;
    //   pointer-events: none;

    //   &:before {
    //     pointer-events: none;
    //     content: '';
    //     position: absolute;
    //     left: -1px;
    //     top: -1px;
    //     right: -1px;
    //     bottom: -1px;
    //     border-radius: inherit;
    //     background-color: rgba(255,255,255,.35);
    //   }
    // }
  @include m(primary) {
    @include button-variant(#fff, var(--primary, #3370ff), var(--primaryHover, #3370ff), var(--primaryActive, #3370ff), var(--primaryloading, #3370ff));
  }
  //   @include m(success) {
  //     @include button-variant($--button-success-font-color, $--button-success-background-color, $--button-success-border-color);
  //   }
  //   @include m(warning) {
  //     @include button-variant($--button-warning-font-color, $--button-warning-background-color, $--button-warning-border-color);
  //   }
  @include m(danger) {
    @include button-variant(#fff, var(--deDanger, #f54a45), var(--deDangerHover, #f54a45), var(--deDangerActive, #f54a45), var(--deDangerloading, #f54a45));
  }
  //   @include m(info) {
  //     @include button-variant($--button-info-font-color, $--button-info-background-color, $--button-info-border-color);
  //   }
  //   @include m(text) {
  //     border-color: transparent;
  //     color: $--color-primary;
  //     background: transparent;
  //     padding-left: 0;
  //     padding-right: 0;

  //     &:hover,
  //     &:focus {
  //       color: mix($--color-white, $--color-primary, $--button-hover-tint-percent);
  //       border-color: transparent;
  //       background-color: transparent;
  //     }
  //     &:active {
  //       color: mix($--color-black, $--color-primary, $--button-active-shade-percent);
  //       border-color: transparent;
  //       background-color: transparent;
  //     }

  //     &.is-disabled,
  //     &.is-disabled:hover,
  //     &.is-disabled:focus {
  //       border-color: transparent;
  //     }
  //   }
}
</style>
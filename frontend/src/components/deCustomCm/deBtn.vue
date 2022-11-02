<template>
  <button
    class="de-button"
    type="button"
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
    @click="handleClick"
  >
    <svg
      v-if="loading"
      viewBox="25 25 50 50"
      class="de-circular"
    >
      <circle
        cx="50"
        cy="50"
        r="20"
        fill="none"
        class="path"
      />
    </svg>
    <i
      v-if="icon && !loading"
      :class="icon"
    />
    <span
      v-if="$slots.default"
      :class="[{'de-btn-mar5': icon || loading }]"
    ><slot /></span>
  </button>
</template>
<script>
export default {
  name: 'DeButton',
  inject: {
    elForm: {
      default: ''
    }
  },
  props: {
    type: {
      type: String,
      default: 'default'
    },
    size: String,
    icon: {
      type: String,
      default: ''
    },
    loading: Boolean,
    disabled: Boolean,
    plain: Boolean,
    secondary: Boolean
  },

  computed: {
    buttonDisabled() {
      return Object.prototype.hasOwnProperty.call(this.$options.propsData, 'disabled')
        ? this.disabled
        : (this.elForm || {}).disabled
    }
  },

  methods: {
    handleClick(evt) {
      this.$emit('click', evt)
    }
  }
}
</script>
<style lang="scss">
$namespace: "de";
$state-prefix: "is-de-";
$modifier-separator: "--";
$B: null;
@mixin b($block) {
  $B: $namespace + "-" + $block;
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

<template>
  <div class="el-collapse-item"
       :class="{'is-active': isActive, 'is-disabled': disabled }">
    <div
      role="tab"
      :aria-expanded="isActive"
      :aria-controls="`el-collapse-content-${id}`"
      :aria-describedby="`el-collapse-content-${id}`"
      @click="handleHeaderClick"
    >
      <div
        class="el-collapse-item__header"
        role="button"
        :id="`el-collapse-head-${id}`"
        :tabindex="disabled ? undefined : 0"
        @keyup.space.enter.stop="handleEnterClick"
        :class="{
          'focusing': focusing,
          'is-active': isActive
        }"
        @focus="handleFocus"
        @blur="focusing = false"
      >
        <div @click.stop="handleCollapseClick">
          <i class="el-collapse-item__arrow el-icon-arrow-right"
             :class="{'is-active': isActive}">
          </i>
        </div>
        <slot name="title">{{title}}</slot>
      </div>
    </div>
    <el-collapse-transition>
      <div
        class="el-collapse-item__wrap"
        v-show="isActive"
        role="tabpanel"
        :aria-hidden="!isActive"
        :aria-labelledby="`el-collapse-head-${id}`"
        :id="`el-collapse-content-${id}`"
      >
        <div class="el-collapse-item__content">
          <slot></slot>
        </div>
      </div>
    </el-collapse-transition>
  </div>
</template>
<script>
  import Emitter from 'element-ui/src/mixins/emitter';
  import {generateId} from 'element-ui/src/utils/util';

  export default {
    name: 'MsApiCollapseItem',

    componentName: 'MsApiCollapseItem',

    mixins: [Emitter],

    data() {
      return {
        contentWrapStyle: {
          height: 'auto',
          display: 'block'
        },
        contentHeight: 0,
        focusing: false,
        isClick: false,
        id: generateId()
      };
    },

    inject: ['collapse'],

    props: {
      title: String,
      name: {
        type: [String, Number],
        default() {
          return this._uid;
        }
      },
      disabled: Boolean
    },

    computed: {
      isActive() {
        return this.collapse.activeNames.indexOf(this.name) > -1;
      }
    },

    methods: {
      handleFocus() {
        setTimeout(() => {
          if (!this.isClick) {
            this.focusing = true;
          } else {
            this.isClick = false;
          }
        }, 50);
      },
      handleHeaderClick() {
        if (this.disabled) return;
        this.dispatch('MsApiCollapse', 'item-click', this);
        this.focusing = false;
        this.isClick = true;
      },
      handleCollapseClick() {
        if (this.disabled) return;
        this.dispatch('MsApiCollapse', 'collapse-click', this);
        this.focusing = false;
        this.isClick = true;
      },
      handleEnterClick() {
        this.dispatch('MsApiCollapse', 'item-click', this);
      }
    }
  };
</script>

<style scoped>
  .el-collapse-item__header {
    padding-left: 7px;
    border-right: 2px solid #409eff;
  }

  .el-collapse-item__header.is-active {
    background-color: #E9E9E9;
  }

  .el-collapse-item__content {
    padding-bottom: 0;
  }

</style>

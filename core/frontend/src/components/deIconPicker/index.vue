<template>
  <div class="ui-fas">
    <div
      v-if="!!name"
      class="selected-icon-container"
    >
      <span
        title="icon"
        :style="{color: color}"
      >
        <e-icon
          :icon-name="name"
          :title="name"
          class="e-icon"
        />
      </span>
    </div>
    <el-popover
      ref="popover"
      v-model="visible"
      :disabled="disabled"
      :placement="myPlacement"
      popper-class="el-icon-popper"
      :width="popoverWidth"
      show-arrow
      trigger="manual"
      @show="setTempSelected"
    >
      <template slot="reference">
        <slot
          name="default"
          :data="{prefixIcon,visible,placeholder,disabled,clearable,readonly,size}"
        >

          <el-input
            ref="input"
            v-model="proxyValue"
            class="de-icon-picker-input"
            :placeholder="dynamicPlaceholder"
            :style="styles"
            :clearable="clearable"
            :disabled="disabled"
            :readonly="true"
            :size="size"
            @input="_change"
            @clear="_initIcon(false)"
            @focus="_popoverShowFun(false)"
          >
            <template
              v-if="showPrefix"
              slot="prepend"
            >
              <slot
                name="prepend"
                :icon="prefixIcon"
              >
                <e-icon
                  :icon-name="prefixIcon"
                  class="e-icon"
                />
              </slot>
            </template>
          </el-input>
        </slot>
      </template>
      <div class="de-icon-picker-container">
        <div class="top-container">
          <el-color-picker
            v-model="curColor"
            show-alpha
            @change="colorChange"
          />

          <div class="top-sure-button-div">
            <el-button
              type="primary"
              size="mini"
              icon="el-icon-check"
              @click.stop="sure"
            />
          </div>

        </div>
        <el-divider class="top-divider" />

        <el-scrollbar
          v-if="!destroy"
          ref="eScrollbar"
          tag="div"
          wrap-class="el-select-dropdown__wrap"
          view-class="el-select-dropdown__list"
          :class="'is-empty-'+id"
        >
          <ul
            v-if="dataList && dataList.length > 0"
            ref="fasIconList"
            class="fas-icon-list"
          >
            <li
              v-for="(item, index) in dataList"
              :key="index"
              class="picker-li"
              :style="tempSelected === item && (highLightColor !== '' || !!curColor) ? {color: highLightColor || curColor, border: '1px solid ' + curColor} : ''"
              @click="_selectedIcon(item)"
            >
              <slot
                name="icon"
                :icon="item"
              >
                <e-icon
                  :icon-name="item"
                  :title="item"
                  class="e-icon"
                />
              </slot>
            </li>
          </ul>
          <span
            v-else
            class="fas-no-data"
            v-text="emptyText"
          />
        </el-scrollbar>
      </div>
    </el-popover>
  </div>
</template>

<script>
import iconList, { eIconList, elementUI, fontAwesome } from './iconList'
import { off, on } from './utils/index'
import EIcon from './eIcon/e-icon'
import ElInput from 'element-ui/lib/input'
import ElPopover from 'element-ui/lib/popover'
import ElScrollbar from 'element-ui/lib/scrollbar'
import { PopupManager } from 'element-ui/lib/utils/popup'

export default {
  name: 'EIconPicker',
  components: {
    EIcon,
    ElInput,
    ElPopover,
    ElScrollbar
  },
  props: {
    disabled: {
      type: Boolean,
      default() {
        return false
      }
    },
    readonly: {
      type: Boolean,
      default() {
        return false
      }
    },
    clearable: {
      type: Boolean,
      default() {
        return false
      }
    },
    styles: {
      type: Object,
      default() {
        return {}
      }
    },
    placement: {
      type: String,
      default() {
        return 'bottom'
      }
    },
    value: {
      type: String,
      default() {
        return ''
      }
    },
    options: {
      type: Object,
      default: () => {}
    },
    width: {
      type: Number,
      default() {
        return -1
      }
    },
    size: {
      type: String,
      default() {
        return 'medium'
      }
    },
    placeholder: {
      type: String,
      default() {
        return '请选择图标'
      }
    },
    defaultIcon: {
      type: String,
      default() {
        return 'eiconfont e-icon-bi'
      }
    },
    emptyText: {
      type: String,
      default() {
        return '暂无可选图标'
      }
    },
    highLightColor: {
      type: String,
      default() {
        return ''
      }
    },
    zIndex: {
      type: Number,
      default() {
        return null
      }
    },
    showPrefix: {
      type: Boolean,
      default() {
        return false
      }
    },
    color: {
      type: String,
      default() {
        return 'rgba(255, 0, 0, 1.0)'
      }
    }
  },
  data() {
    return {
      iconList: [],
      visible: false,
      prefixIcon: 'eiconfont e-icon-bi',
      name: '',
      icon: {},
      myPlacement: 'bottom',
      popoverWidth: 200,
      destroy: false,
      id: new Date().getTime(),
      proxyValue: '',
      curColor: '',
      tempSelected: ''
    }
  },
  computed: {
    dataList: function() {
      const arr1 = []
      for (let i = 0, len = this.iconList.length; i < len; i++) {
        if (arr1.indexOf(this.iconList[i]) === -1) {
          arr1.push(this.iconList[i])
        }
      }
      return arr1
    },
    dynamicPlaceholder() {
      return this.name ? '' : this.placeholder
    }

  },
  watch: {
    value: function(val) {
      setTimeout(() => {
        this.name = val
        this.prefixIcon = this.name ? this.name : this.defaultIcon
      }, 50)
    },
    visible: function(val) {
      if (val === false) {
        this.$nextTick(() => {
          off(document, 'mouseup', this._popoverHideFun)
        })
      } else {
        this.$nextTick(() => {
          this.createIconList()
          on(document, 'mouseup', this._popoverHideFun)
        })
      }
    },

    options: {
      handler(newV, oldV) {
        const self = this
        setTimeout(() => {
          self._initIcon(true)
        }, 50)
      },
      deep: true
    }
  },
  mounted() {
    this._updateW()
  },
  beforeDestroy() {
    off(document, 'mouseup', this._popoverHideFun)
    this.destroyIconList()
  },
  created() {
    this.createIconList()
    this._initIcon(true)
    this.curColor = this.color
  },
  methods: {
    colorChange(c) {
      this.color = c
      this.$emit('set-color', c)
    },
    setTempSelected() {
      this.tempSelected = this.name
    },
    sure() {
      this.visible = false
      this.name = this.tempSelected
      this.prefixIcon = this.name
      this.color = this.curColor
      this._emitFun(this.name)
      this.$emit('set-color', this.curColor)
    },
    _change(val) {
      this.iconList = this.icon.list.filter(function(i) {
        return i.indexOf(val) !== -1
      })
    },
    _initIcon(type) {
      this.prefixIcon = this.value && type && type === true ? this.value : this.defaultIcon
      this.name = type === true ? this.value : ''
      this.icon = Object.assign({}, iconList)
      if (this.options) {
        this.icon.list = []
        if (this.options.addIconList !== undefined &&
            this.options.addIconList &&
            this.options.addIconList.length > 0) {
          this.icon.addIcon(this.options.addIconList)
        }
        if (this.options.removeIconList !== undefined &&
            this.options.removeIconList &&
            this.options.removeIconList.length > 0) {
          this.icon.removeIcon(this.options.removeIconList)
        }
        if (this.options.FontAwesome === true) {
          this.icon.addIcon(fontAwesome)
        }
        if (this.options.ElementUI === true) {
          this.icon.addIcon(elementUI)
        }
        if (this.options.eIcon === true) {
          if (this.options.eIconSymbol) {
            const list = eIconList.map((item) => {
              return item.replace('eiconfont ', '#')
            })
            this.icon.addIcon(list)
          } else {
            this.icon.addIcon(eIconList)
          }
        }
      }
      this.iconList = this.icon.list

      if (this.placement && (this.placement === 'bottom' || this.placement === 'top')) {
        this.myPlacement = this.placement
      }
      if (type === false) {
        this._emitFun('')
      }
    },

    addIcon(item = []) {
      if (item !== undefined && item && item.length > 0) {
        this.icon.addIcon(item)
        this.iconList = this.icon.list
      }
    },
    removeIcon(item = []) {
      if (item !== undefined && item && item.length > 0) {
        this.icon.removeIcon(item)
        this.iconList = this.icon.list
      }
    },
    updatePopper(zIndex) {
      if (zIndex) {
        PopupManager.zIndex = zIndex
      }
      this._popoverShowFun(true)
      setTimeout(() => {
        this.$refs.popover.updatePopper()
      }, 100)
    },
    _selectedIcon(item) {
      this.tempSelected = item
    },
    _updateW() {
      this.$nextTick(() => {
        if (this.width === -1 && this.$refs.input && this.$refs.input.$el) {
          this.popoverWidth = this.$refs.input.$el.getBoundingClientRect().width - 36
        } else {
          this.popoverWidth = this.width
        }
        if (this.$refs.eScrollbar && this.$refs.eScrollbar.wrap) {
          this.$refs.eScrollbar.wrap.scrollTop = 0
          this.$refs.eScrollbar.handleScroll()
          this.$refs.eScrollbar.update()
        }
      })
    },
    _popoverShowFun(flag) {
      const _this = this
      if (_this.readonly !== true && _this.disabled !== true) {
        if (!flag && _this.zIndex) {
          PopupManager.zIndex = this.zIndex
        }
        _this.visible = true
        _this._updateW()
      }
    },
    _popoverHideFun(e) {
      const path = e.path || (e.composedPath && e.composedPath())

      const isInside = path.some(list => {
        return list.className && typeof list.className === 'string' && (list.className.indexOf('ui-fas') !== -1 || list.className.indexOf('de-icon-picker-container') !== -1)
      })

      if (!isInside) {
        this.visible = false
      }

      const isInput = path.some(list => {
        return list.className && typeof list.className === 'string' && list.className.indexOf('de-icon-picker-input') !== -1
      })

      if (this.visible && isInput) {
        // this.visible = false
      }
    },
    _emitFun(val) {
      this.$emit('input', val)
      this.$emit('change', val)
    },

    destroyIconList() {
      this.destroy = true
    },

    createIconList() {
      this.destroy = false
    },
    show() {
      this._popoverShowFun(false)
    },
    hide() {
      this.visible = false
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~element-ui/lib/theme-chalk/input.css';
@import '~element-ui/lib/theme-chalk/popover.css';
@import '~element-ui/lib/theme-chalk/scrollbar.css';
@import '~element-ui/lib/theme-chalk/select-dropdown.css';

.fas-icon-list {
  list-style-type: none;
  margin: 0 0 0 -4px;
  padding: 0;
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
}

.ui-fas .el-input__inner {
  cursor: pointer;
}

.fas-icon-list li {
  width: 30px;
  height: 30px;
  margin: 5px;
}

.fas-icon-list li i, .fas-icon-list li svg {
  font-size: 20px;
  cursor: pointer;
}

.el-icon-popper {
  max-height: 400px;
  overflow: auto;
  overflow-x: hidden;
  overflow-y: hidden;
}

.el-icon-popper[x-placement^="bottom"] {
  margin-top: 5px;
}

.fas-no-data {
  display: block;
}

.e-icon {
  font-size: 16px;
}
.top-divider {
  margin: 5px 0 0 0 !important;
}
.top-container {
  display: flex;
}
.top-sure-button-div {
  margin: 4px 15px 0 auto;
  ::v-deep button {
    width: 25px;
    height: 20px;
    padding: 4px 5px;
  }
}
.selected-icon-container {
  position: absolute;
  z-index: 9;
  width: 21%;
  text-align: center;
  margin-top: 2px;
  pointer-events: none;
}
.picker-li {
  text-align: center;
  line-height: 30px;
}
</style>

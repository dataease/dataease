<template>
  <div>
    <div v-if="colorDto.value" class="color-div-base selected-div">
      <div style="float: left;display: flex;align-items: center;">
        <span v-for="(c,index) in colorDto.colors" :key="index" class="color-span-base" :style="{background: formatBgColor(c, true)}" />
      </div>
      <span style="margin-left: 4px;">
      </span>
      <span class="reset-span" @click="reset">
        <el-tooltip class="item" effect="dark" content="重置" placement="top">
          <i class="el-icon-refresh" />
        </el-tooltip>
      </span>
    </div>
    <el-popover
      placement="bottom"
      popper-class="gradient-popper"
      :width="popWidth"
      :append-to-body="true"
      @show="whenShow"
      trigger="click"
    >
      <div class="custom-switch-div">
        <el-switch
          v-model="enableCustom"
          active-text="自定义"
          inactive-text="">
        </el-switch>
      </div>
      <el-tabs v-model="activeName" @tab-click="handleClick">
        <el-tab-pane v-for="(pane, i) in tabPanes" :key="i" :label="pane.label" :name="pane.name">
          <div class="color-tab-content" :id="'color-tab-content-' + i" @click="handler">
            <div v-for="option in pane.datas" :key="option.value" class="el-select-dropdown__item color-div-base" :class="option.value === colorDto.value ? 'selected hover editor' : ''" @click="selectNode(option)">
              <div v-if="option.value !== colorDto.value || !enableCustom" style="float: left;display: flex;align-items: center;">
                <span v-for="(c,index) in option.colors" :key="index" class="color-span-base" :style="{background: formatBgColor(c)}" />
              </div>
              <span v-if="option.value !== colorDto.value || !enableCustom" style="margin-left: 4px;">{{ option.name }}</span>

              <span v-else v-for="(co,index) in option.colors" :key="index" class="color-span-base is-editor" >
                <el-color-picker v-if="i === 0" v-model="option.colors[index]" @change="switchColorItem(option.colors, index)"/>
                <de-color-picker ref="de-color-picker" :id="option.value + index" :base-id="option.value + index" show-alpha color-format="rgb" v-else v-model="option.colors[index]" @change="switchColorItem(option.colors, index)"/>
              </span>
              
            </div>
          </div>
        </el-tab-pane>
        
      </el-tabs>

      <el-select
        ref="colorSelect"
        slot="reference"
        v-model="colorDto.value"
        
        class="form-gender-select"
        popper-class="gradient-tree-select"
        :placeholder="$t('commons.please_select')"
        @focus="_popoverShowFun"
      >
        <el-option
          v-for="item in selectOptions"
          :key="item.label"
          :label="item.label"
          :value="item.id"
        />
          
      </el-select>
    </el-popover>
  </div>
</template>

<script>
import {colorCases, gradientColorCases} from './base'
import DeColorPicker from './DeColorPicker'
export default{
  name: 'GradientColorSelector',
  components: {
    DeColorPicker
  },
  props: {
    colorDto: {
      type: Object,
      default: () => {
        return {
          value: null,
          colors: []
        }
      }
    }
  },
  data() {
    return {
      value: null,
      selectOptions: [],
      popWidth: 260,
      colorCases: JSON.parse(JSON.stringify(colorCases)),
      gradientColorCases: JSON.parse(JSON.stringify(gradientColorCases)),
      enableCustom: false,
      activeName: 'simple',
      tabPanes: [
        {
          label: '纯色',
          name: 'simple',
          datas: JSON.parse(JSON.stringify(colorCases))
        },
        {
          label: '渐变',
          name: 'gradient',
          datas: JSON.parse(JSON.stringify(gradientColorCases))
        }
      ],
      testColor: '#5470c6'
    }
  },
  computed: {
    
  },
  mounted() {
    this._updateH()
  },
  created() {
    this.initcolorDto()
  },
  methods: {
    whenShow() {
      this.$nextTick(() => {
        this.initcolorDto()
        this.scrollToSelected()
      })
    },
    scrollToSelected() {
      const index = this.activeName === 'simple' ? 0 : 1
      const parents = document.getElementById("color-tab-content-" + index)
      if(!parents) return
      const items = parents.getElementsByClassName("color-div-base selected")
      if(items && items.length) {
        const top = items[0].offsetTop || 0
        parents.scrollTo(0, top)
      }
    },
    switchColorItem(colors, index) {
      this.colorDto.colors = JSON.parse(JSON.stringify(colors))
      this.$emit('color-change', JSON.parse(JSON.stringify(this.colorDto)))
    },
    initcolorDto() {
      let haspPropValue = true
      if (!this.colorDto.value) {
        this.colorDto.value = this.colorCases[0].value
        this.colorDto.colors = this.colorCases[0].colors
        haspPropValue = false
      }
      this.activeName = this.colorCases.some(item => item.value === this.colorDto.value) ? 'simple' : 'gradient' 
      if(haspPropValue) {
        this.tabPanes[this.activeName === 'simple' ? 0 : 1].datas.forEach(item => {
          if (item.value === this.colorDto.value) {
            item.colors = JSON.parse(JSON.stringify(this.colorDto.colors))
          }
        })
      }
    },
    handler(e) {
      const whiteClassName = 'c__block el-popover__reference'
      if(!e || !e.target || !e.target.className) return
      if(!this.$refs || !this.$refs['de-color-picker']) return
      let id = null
      if (e.target.className === whiteClassName) {
        if(!e.target.parentElement || !e.target.parentElement.parentElement || !e.target.parentElement.parentElement.parentElement || !e.target.parentElement.parentElement.parentElement.id) return
        id = e.target.parentElement.parentElement.parentElement.id
      }
      
      const widget = this.$refs['de-color-picker'] 
      if (Array.isArray(widget)) {

        widget.forEach(item => {
          (!id || id !== item.$el.id) && item.triggerHide && item.triggerHide()
        })
        return
      }
      (!id || id !== widget.$el.id) && widget.triggerHide && widget.triggerHide()
      
    },
    handleClick() {
      this.enableCustom = false
      this.$nextTick(() => {
        this.scrollToSelected()
      })
      const widget = this.$refs['de-color-picker']
      if(!widget) return
      if (Array.isArray(widget)) {

        widget.forEach(item => {
          item.triggerHide && item.triggerHide()
        })
        return
      }
      widget.triggerHide && widget.triggerHide()
    },
    _updateH() {
      this.$nextTick(() => {
        this.popWidth = this.$refs.colorSelect.$el.getBoundingClientRect().width
      })
    },
    _popoverShowFun(val) {
      this._updateH()
      this.$emit('onFoucs')
    },
    fillGradientColor() {
      this.gradientColorCases.forEach(item => {
        item.colors = item.colors.map(color => {
          const str = 'linear-gradient(0.0deg,' + color[0] + ' 0.0,' + color[1] + ' 100.0%)'
          return str
        })
      })
      this.tabPanes[1].datas = JSON.parse(JSON.stringify(this.gradientColorCases))
    },
    formatBgColor(color, useValue) {
      let activeName = this.activeName
      if(useValue) {
        activeName = this.colorCases.some(item => item.value === this.colorDto.value) ? 'simple' : 'gradient'
      }
      if(activeName === 'simple') {
        return color
      }
      return 'linear-gradient(0.0deg,' + color[0] + ' 0.0,' + color[1] + ' 100.0%)'
    },
    selectNode(option) {
      this.selectOptions = [
        {
          id: option.value,
          label: option.name,
          colors: option.colors
        }
      ]
      this.colorDto.value = option.value
      this.colorDto.colors = option.colors
      this.$emit('color-change', JSON.parse(JSON.stringify(this.colorDto)))
    },
    reset() {
      if(this.colorDto.value) {
        let activeName = 'simple'
        if(this.gradientColorCases.some(item => item.value === this.colorDto.value)) {
          activeName = 'gradient'
        }
        (activeName === 'simple' ? colorCases : gradientColorCases).forEach(curcase => {
          if(curcase.value === this.colorDto.value) {
            this.colorDto.colors = JSON.parse(JSON.stringify(curcase.colors))
            this.$emit('color-change', JSON.parse(JSON.stringify(this.colorDto)))
          }
        })
      }
    }
  }
} 
</script>

<style lang="scss">
.gradient-tree-select {
  display: none !important;
}
.gradient-popper {
  background: #fff;
  padding: 0 10px;
  margin-top: 1px !important;
  border-top: none;
  height: 300px;
  
  .popper__arrow {
    display: none;
  }
  .el-tabs__header {
    margin: 0 0 2px !important;
  }
}

.form-gender-select {
  width: 100%;
  .el-input__suffix {
    display: none;
  }
}
.color-span-base {
  width: 20px;
  height: 20px;
  display:inline-block;
}
.is-editor {
  width:23px !important;
  height: 28px !important;
}

.color-div-base {
  display: flex !important;
  align-items: center !important;
  cursor: pointer;
  padding-left: 5px !important;
}
.custom-switch-div {
  position: absolute;
  top: 8px;
  right: 10px;
  z-index: 999;
}
.color-tab-content {
  height: 255px;
  overflow: auto;
  .editor {
    border: 1px dashed var(--primary,#3370ff);
  }
}
.selected-div {
  position: absolute;
  z-index: 1;
  top: 5px;
  pointer-events: none;
  .reset-span {
    position: absolute;
    left: 235px;
    pointer-events: auto;
    &:hover {
      color: var(--primary,#3370ff);
    }
  }
}
</style>
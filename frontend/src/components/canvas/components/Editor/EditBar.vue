<template>
  <div class="bar-main">
    <div v-if="linkageSettingStatus&&element!==curLinkageView&&element.type==='view'" style="margin-right: -1px;width: 18px">
      <el-checkbox v-model="linkageInfo.linkageActive" />
      <linkage-field v-if="linkageInfo.linkageActive" :element="element" />
    </div>
    <div v-else-if="!linkageSettingStatus">
      <setting-menu v-if="activeModel==='edit'" style="float: right;height: 24px!important;" @amRemoveItem="amRemoveItem" @linkJumpSet="linkJumpSet">
        <span slot="icon" :title="$t('panel.setting')">
          <i class="icon iconfont icon-shezhi" style="margin-top:2px" />
        </span>
      </setting-menu>
      <span :title="$t('panel.edit')">
        <i v-if="activeModel==='edit'&&curComponent&&editFilter.includes(curComponent.type)" class="icon iconfont icon-edit" @click.stop="edit" />
      </span>
      <span :title="$t('panel.matrix')">
        <i v-if="activeModel==='edit'&&curComponent.auxiliaryMatrix" class="icon iconfont icon-shujujuzhen" @click.stop="auxiliaryMatrixChange" />
      </span>
      <span :title="$t('panel.suspension')">
        <i v-if="activeModel==='edit'&&!curComponent.auxiliaryMatrix" class="icon iconfont icon-xuanfuanniu" @click.stop="auxiliaryMatrixChange" />
      </span>
      <span :title="$t('panel.details')">
        <i v-if="curComponent.type==='view'" class="icon iconfont icon-fangda" @click.stop="showViewDetails" />
      </span>
      <span :title="$t('panel.cancel_linkage')">
        <i v-if="curComponent.type==='view'&&existLinkage" class="icon iconfont icon-quxiaoliandong" @click.stop="clearLinkage" />
      </span>
    </div>

  </div>
</template>

<script>
import { mapState } from 'vuex'
import bus from '@/utils/bus'
import SettingMenu from '@/components/canvas/components/Editor/SettingMenu'
import LinkageField from '@/components/canvas/components/Editor/LinkageField'

export default {
  components: { SettingMenu, LinkageField },

  props: {
    element: {
      type: Object,
      required: true
    },
    active: {
      type: Boolean,
      required: false,
      default: false
    },
    // 当前模式 preview 预览 edit 编辑，
    activeModel: {
      type: String,
      required: false,
      default: 'preview'
    }
  },
  data() {
    return {
      componentType: null,
      linkageActiveStatus: false,
      editFilter: [
        'view',
        'custom'
      ],
      timer: null
    }
  },
  mounted() {
  },
  computed: {
    existLinkage() {
      let linkageFiltersCount = 0
      this.componentData.forEach(item => {
        if (item.linkageFilters && item.linkageFilters.length > 0) {
          item.linkageFilters.forEach(linkage => {
            if (this.element.propValue.viewId === linkage.sourceViewId) {
              linkageFiltersCount++
            }
          })
        }
      })
      return linkageFiltersCount
    },
    linkageInfo() {
      return this.targetLinkageInfo[this.element.propValue.viewId]
    },
    ...mapState([
      'menuTop',
      'menuLeft',
      'menuShow',
      'curComponent',
      'componentData',
      'canvasStyleData',
      'linkageSettingStatus',
      'targetLinkageInfo',
      'curLinkageView',
      'curCanvasScale'
    ])
  },
  beforeDestroy() {
  },
  methods: {
    createTimer() {
      if (!this.timer) {
        this.timer = setInterval(() => {
          console.log('t=' + this.curComponent.auxiliaryMatrix)
        }, 5000)
      }
    },
    destroyTimer() {
      if (this.timer) {
        clearInterval(this.timer)
        this.timer = null
      }
    },
    showViewDetails() {
      this.$emit('showViewDetails')
    },
    auxiliaryMatrixChange() {
      if (this.curComponent.auxiliaryMatrix) {
        this.curComponent.auxiliaryMatrix = false
        this.$emit('amRemoveItem')
      } else {
        this.curComponent.x = Math.round(this.curComponent.style.left / this.curCanvasScale.matrixStyleOriginWidth) + 1
        this.curComponent.y = Math.round(this.curComponent.style.top / this.curCanvasScale.matrixStyleOriginHeight) + 1
        this.curComponent.sizex = Math.round(this.curComponent.style.width / this.curCanvasScale.matrixStyleOriginWidth)
        this.curComponent.sizey = Math.round(this.curComponent.style.height / this.curCanvasScale.matrixStyleOriginHeight)
        this.curComponent.auxiliaryMatrix = true
        this.$emit('amAddItem')
      }
      setTimeout(() => {
        this.recordMatrixCurShadowStyle()
      }, 50)
      this.$store.state.styleChangeTimes++
      bus.$emit('auxiliaryMatrixChange')
    },
    // 记录当前样式 跟随阴影位置 矩阵处理
    recordMatrixCurShadowStyle() {
      // debugger
      const left = (this.curComponent.x - 1) * this.curCanvasScale.matrixStyleWidth
      const top = (this.curComponent.y - 1) * this.curCanvasScale.matrixStyleHeight
      const width = this.curComponent.sizex * this.curCanvasScale.matrixStyleWidth
      const height = this.curComponent.sizey * this.curCanvasScale.matrixStyleHeight
      const style = {
        left: left,
        top: top,
        width: width,
        height: height
      }
      this.$store.commit('setShapeStyle', style)
      // resize
      this.$emit('resizeView')
    },
    edit() {
      // 编辑时临时保存 当前修改的画布
      this.$store.dispatch('panel/setComponentDataTemp', JSON.stringify(this.componentData))
      this.$store.dispatch('panel/setCanvasStyleDataTemp', JSON.stringify(this.canvasStyleData))
      if (this.curComponent.type === 'view') {
        this.$store.dispatch('chart/setViewId', null)
        this.$store.dispatch('chart/setViewId', this.curComponent.propValue.viewId)
        bus.$emit('PanelSwitchComponent', { name: 'ChartEdit', param: { 'id': this.curComponent.propValue.viewId, 'optType': 'edit' }})
      }
      if (this.curComponent.type === 'custom') {
        bus.$emit('component-dialog-edit')
      }
      // 编辑样式组件
      if (this.curComponent.type === 'v-text' || this.curComponent.type === 'rect-shape') {
        bus.$emit('component-dialog-style')
      }
    },
    linkageEdit() {

    },
    amRemoveItem() {
      this.$emit('amRemoveItem')
    },
    // 清除相同sourceViewId 的 联动条件
    clearLinkage() {
      this.componentData.forEach(item => {
        if (item.linkageFilters && item.linkageFilters.length > 0) {
          const newList = item.linkageFilters.filter(linkage => linkage.sourceViewId !== this.element.propValue.viewId)
          item.linkageFilters.splice(0, item.linkageFilters.length)
          // 重新push 可保证数组指针不变 可以watch到
          if (newList.length > 0) {
            newList.forEach(newLinkage => {
              item.linkageFilters.push(newLinkage)
            })
          }
        }
      })
    },
    linkJumpSet() {
      this.$emit('linkJumpSet')
    }
  }
}
</script>

<style lang="scss" scoped>
  .bar-main{
    position: absolute;
    right: 0px;
    float:right;
    z-index: 2;
    border-radius:2px;
    padding-left: 5px;
    padding-right: 2px;
    cursor:pointer!important;
    background-color: #0a7be0;
  }
  .bar-main i{
    color: white;
    float: right;
    margin-right: 3px;
  }

</style>

<template>
  <div class="bar-main" :class="showEditPosition" @mousedown="showLabelInfo">
    <input id="input" ref="files" type="file" accept="image/*" hidden @click="e => {e.target.value = '';}" @change="handleFileChange">
    <div v-if="linkageAreaShow" style="margin-right: -1px;width: 20px">
      <el-checkbox v-model="linkageInfo.linkageActive" size="medium" />
      <linkage-field v-if="linkageInfo.linkageActive" :element="element" />
    </div>
    <div v-if="positionCheck('multiplexing') && showMultiplexingCheck" style="margin-right: 1px;width: 18px;z-index: 5">
      <el-checkbox v-model="multiplexingCheckModel" size="medium" @change="multiplexingCheck" />
    </div>
    <div v-if="batchOptAreaShow" style="margin-right: -1px;width: 20px;z-index: 5">
      <el-checkbox size="medium" @change="batchOptChange" />
    </div>
    <div v-if="normalAreaShow">
      <span :title="$t('panel.edit')">
        <i v-if="activeModel==='edit'&&curComponent&&editFilter.includes(curComponent.type)" class="icon iconfont icon-edit" @click.stop="edit" />
      </span>
      <span :title="$t('panel.matrix')">
        <i v-if="activeModel==='edit'&&curComponent.auxiliaryMatrix" class="icon iconfont icon-shujujuzhen" @click.stop="auxiliaryMatrixChange" />
      </span>
      <span :title="$t('panel.suspension')">
        <i v-if="activeModel==='edit'&&!curComponent.auxiliaryMatrix" class="icon iconfont icon-xuanfuanniu" @click.stop="auxiliaryMatrixChange" />
      </span>
      <span :title="$t('panel.enlarge')">
        <i v-if="enlargeShow" class="icon iconfont icon-fangda" @click.stop="showViewDetails('enlarge')" />
      </span>
      <span :title="$t('panel.details')">
        <i v-if="detailsShow" class="icon iconfont icon-chakan" @click.stop="showViewDetails('details')" />
      </span>
      <setting-menu v-if="activeModel==='edit'" style="float: right;height: 24px!important;" @amRemoveItem="amRemoveItem" @linkJumpSet="linkJumpSet" @boardSet="boardSet">
        <span slot="icon" :title="$t('panel.setting')">
          <i class="icon iconfont icon-shezhi" style="margin-top:2px" />
        </span>
      </setting-menu>
      <span :title="$t('panel.cancel_linkage')">
        <i v-if="curComponent.type==='view'&&existLinkage" class="icon iconfont icon-quxiaoliandong" @click.stop="clearLinkage" />
      </span>
      <span :title="$t('panel.switch_picture')">
        <i v-if="activeModel==='edit'&&curComponent&&curComponent.type==='picture-add'" class="icon iconfont icon-genghuan" @click.stop="goFile" />
      </span>
      <el-popover
        v-if="selectFieldShow"
        width="200"
        trigger="click"
        @mousedown="fieldsAreaDown"
      >
        <fields-list :fields="curFields" :element="element" />
        <i slot="reference" :disabled="element.editing" :title="$t('panel.select_field')" class="icon iconfont icon-datasource-select" style="margin-left: 4px;cursor: pointer;font-size: 14px;" />
      </el-popover>
      <span :title="$t('panel.jump')">
        <a v-if="showJumpFlag" :title="curComponent.hyperlinks.content " :target="curComponent.hyperlinks.openMode " :href="curComponent.hyperlinks.content ">
          <i class="icon iconfont icon-com-jump" />
        </a>
      </span>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import bus from '@/utils/bus'
import SettingMenu from '@/components/canvas/components/Editor/SettingMenu'
import LinkageField from '@/components/canvas/components/Editor/LinkageField'
import toast from '@/components/canvas/utils/toast'
import FieldsList from '@/components/canvas/components/Editor/fieldsList'

export default {
  components: { FieldsList, SettingMenu, LinkageField },

  props: {
    terminal: {
      type: String,
      default: 'pc'
    },
    sourceElement: {
      type: Object,
      required: true
    },
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
    },
    previewVisible: {
      type: Boolean,
      default: false
    },
    showPosition: {
      type: String,
      required: false,
      default: 'NotProvided'
    }
  },
  data() {
    return {
      curFields: [],
      multiplexingCheckModel: false,
      barWidth: 24,
      componentType: null,
      linkageActiveStatus: false,
      editFilter: [
        'view',
        'custom',
        'custom-button'
      ],
      timer: null,
      viewXArray: []
    }
  },
  mounted() {
    this.initCurFields()
    if (this.element.type === 'view') {
      bus.$on('initCurFields-' + this.element.id, this.initCurFields)
    }
  },
  computed: {
    detailsShow() {
      return this.curComponent.type === 'view' && this.terminal === 'pc' && this.curComponent.propValue.innerType !== 'richTextView'
    },
    enlargeShow() {
      return this.curComponent.type === 'view' && this.curComponent.propValue.innerType !== 'richTextView'
    },
    selectFieldShow() {
      return this.activeModel === 'edit' && this.curComponent.type === 'view' && this.curComponent.propValue.innerType === 'richTextView' && this.curComponent.editing
    },
    curComponentTypes() {
      const types = []
      this.componentData.forEach(component => {
        types.push(component.type)
      })
      return types
    },
    showMultiplexingCheck() {
      return this.element.type !== 'custom-button' || (this.element.type === 'custom-button' && !this.curComponentTypes.includes('custom-button'))
    },
    showEditPosition() {
      if (this.activeModel === 'edit' && !this.linkageAreaShow && !this.batchOptAreaShow) {
        const toRight = (this.canvasStyleData.width - this.element.style.left - this.element.style.width) * this.curCanvasScale.scalePointWidth
        const toLeft = this.element.style.left * this.curCanvasScale.scalePointWidth
        if (this.barWidth < toRight) {
          return 'bar-main-right'
        } else if (this.barWidth > toRight && this.barWidth > toLeft) {
          return 'bar-main-left-inner'
        } else {
          return 'bar-main-left-outer'
        }
      } else {
        return 'bar-main-preview'
      }
    },
    showJumpFlag() {
      return this.curComponent && this.curComponent.hyperlinks && this.curComponent.hyperlinks.enable
    },
    // batch operation area
    batchOptAreaShow() {
      return this.batchOptStatus && this.element.type === 'view' && !this.element.isPlugin
    },
    // 联动区域按钮显示
    linkageAreaShow() {
      return this.linkageSettingStatus && this.element !== this.curLinkageView && this.element.type === 'view'
    },
    // 编辑或预览区域显示
    normalAreaShow() {
      return !this.linkageSettingStatus && !this.batchOptStatus && !this.positionCheck('multiplexing')
    },
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
    miniHeight() {
      return this.mobileLayoutStatus ? 1 : 4
    },
    miniWidth() {
      return this.mobileLayoutStatus ? 1 : 4
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
      'curCanvasScale',
      'batchOptStatus',
      'mobileLayoutStatus',
      'curBatchOptComponents',
      'panelViewDetailsInfo'
    ])
  },
  beforeDestroy() {
  },
  methods: {
    fieldsAreaDown(e) {
      // ignore
      e.preventDefault()
    },
    initCurFields() {
      if (this.element.type === 'view') {
        const chartInfo = this.panelViewDetailsInfo[this.element.propValue.viewId]
        if (chartInfo) {
          this.curFields = []
          const chartDetails = JSON.parse(chartInfo)
          if (chartDetails.type === 'richTextView' && chartDetails.data) {
            this.curFields = chartDetails.data.fields
          }
        }
      }
    },
    positionCheck(position) {
      return this.showPosition.includes(position)
    },
    multiplexingCheck(val) {
      if (val) {
        // push
        this.$store.commit('addCurMultiplexingComponent', { 'component': this.sourceElement, 'componentId': this.element.id })
      } else {
        // remove
        this.$store.commit('removeCurMultiplexingComponentWithId', this.element.id)
      }
    },
    closePreview() {
      this.$emit('closePreview')
    },
    destroyTimer() {
      if (this.timer) {
        clearInterval(this.timer)
        this.timer = null
      }
    },
    showViewDetails(openType = 'details') {
      this.$emit('showViewDetails', { openType: openType })
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
        this.curComponent.sizey = this.curComponent.sizey > this.miniHeight ? this.curComponent.sizey : this.miniHeight
        this.curComponent.sizex = this.curComponent.sizex > this.miniWidth ? this.curComponent.sizex : this.miniWidth
        this.curComponent.auxiliaryMatrix = true
        this.$emit('amAddItem')
      }
      setTimeout(() => {
        this.recordMatrixCurShadowStyle()
      }, 50)
      this.$store.commit('canvasChange')
      bus.$emit('auxiliaryMatrixChange')
    },
    // 记录当前样式 跟随阴影位置 矩阵处理
    recordMatrixCurShadowStyle() {
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
      if (this.curComponent.type === 'custom') {
        bus.$emit('component-dialog-edit', 'update')
      } else if (this.curComponent.type === 'custom-button') {
        bus.$emit('button-dialog-edit')
      } else if (this.curComponent.type === 'v-text' || this.curComponent.type === 'de-rich-text' || this.curComponent.type === 'rect-shape') {
        bus.$emit('component-dialog-style')
      } else { bus.$emit('change_panel_right_draw', true) }
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
      bus.$emit('clear_panel_linkage', { viewId: this.element.propValue.viewId })
    },
    linkJumpSet() {
      this.$emit('linkJumpSet')
    },
    goFile() {
      this.$refs.files.click()
    },
    showLabelInfo(e) {
      // ignore
      e.preventDefault()
    },
    handleFileChange(e) {
      const file = e.target.files[0]
      if (!file.type.includes('image')) {
        toast('只能插入图片')
        return
      }
      const reader = new FileReader()
      reader.onload = (res) => {
        const fileResult = res.target.result
        this.curComponent.propValue = fileResult
        this.$store.commit('recordSnapshot', 'handleFileChange')
      }

      reader.readAsDataURL(file)
    },
    boardSet() {
      this.$emit('boardSet')
    },
    batchOptChange(val) {
      if (val) {
        // push
        this.$store.commit('addCurBatchComponent', this.element.propValue.viewId)
      } else {
        // remove
        this.$store.commit('removeCurBatchComponentWithId', this.element.propValue.viewId)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
  .bar-main{
    position: absolute;
    float:right;
    z-index: 2;
    border-radius:2px;
    padding-left: 3px;
    padding-right: 0px;
    cursor:pointer!important;
    background-color: var(--primary,#3370ff);
  }
  .bar-main i{
    color: white;
    float: right;
    margin-right: 3px;
  }

  .bar-main ::v-deep .el-checkbox__inner{
    width: 16px;
    height: 16px;
  }

  .bar-main ::v-deep .el-checkbox__inner::after{
    width: 4.5px;
  }
  .bar-main-right{
    width: 22px;
    right: -25px;
  }
  .bar-main-left-inner{
    width: 22px;
    left: 0px;
  }

  .bar-main-left-outer{
    width: 22px;
    left: -25px;
  }

  .bar-main-preview{
    right: 0px;
  }

</style>

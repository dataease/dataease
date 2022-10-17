<template>
  <div class="bar-main" :style="setNewValue">
    <input id="input" ref="files" type="file" accept="image/*" hidden @click="e => {e.target.value = '';}" @change="handleFileChange">
    <div v-if="linkageAreaShow" style="margin-right: -1px;">
      <el-checkbox v-model="linkageInfo.linkageActive" />
      <linkage-field v-if="linkageInfo.linkageActive" :element="element" />
    </div>
    <div v-if="checkboxShow" style="margin-right: -1px;widht: 18px" :title="element.isLock? '此组件已锁定': ''">
      <el-checkbox v-model="element.isCheck" @change="checkChange" />
    </div>
    <div v-if="normalAreaShow">
      <setting-menu v-if="activeModel==='edit'" style="float: right;height: 24px!important;" @tabRelation="tabRelation" @amRemoveItem="amRemoveItem" @linkJumpSet="linkJumpSet" @boardSet="boardSet">
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
      <!-- <span :title="$t('panel.suspension')">
        <i v-if="activeModel==='edit'&&!curComponent.auxiliaryMatrix" class="icon iconfont icon-xuanfuanniu" @click.stop="auxiliaryMatrixChange" />
      </span> -->
      <span :title="$t('panel.details')">
        <i v-if="curComponent.type==='view'" class="icon iconfont icon-fangda" @click.stop="showViewDetails" />
      </span>
      <span :title="$t('panel.cancel_linkage')">
        <i v-if="curComponent.type==='view'&&existLinkage" class="icon iconfont icon-quxiaoliandong" @click.stop="clearLinkage" />
      </span>
      <span :title="$t('panel.switch_picture')">
        <i v-if="activeModel==='edit'&&curComponent&&curComponent.type==='picture-add'" class="icon iconfont icon-genghuan" @click.stop="goFile" />
      </span>
      <span :title="$t('panel.switch_picture')">
        <i v-if="activeModel==='edit'&&curComponent&&curComponent.type==='de-banner'" class="icon iconfont icon-genghuan" @click.stop="goBannerFile" />
      </span>
      <span :title="$t('panel.switch_picture')">
        <i v-if="activeModel==='edit'&&curComponent&&curComponent.type==='de-nav'" class="icon iconfont icon-genghuan" @click.stop="setNavInfo" />
      </span>
      <span :title="$t('panel.switch_picture')">
        <i v-if="activeModel==='edit'&&curComponent&&curComponent.type==='de-icons'" class="icon iconfont icon-genghuan" @click.stop="setFontIcon" />
      </span>
      <span :title="$t('panel.switch_picture')">
        <i v-if="activeModel==='edit'&&curComponent&&curComponent.type==='de-picture'" class="icon iconfont icon-genghuan" @click.stop="setPicture" />
      </span>
      <span :title="$t('panel.switch_picture')">
        <i v-if="activeModel==='edit'&&curComponent&&curComponent.type==='de-weather'" class="icon iconfont icon-genghuan" @click.stop="setWeather" />
      </span>
      <span :title="$t('panel.switch_picture')">
        <i v-if="activeModel==='edit'&&curComponent&&curComponent.type==='customBottm'" class="icon iconfont icon-genghuan" @click.stop="setCustom" />
      </span>
      <span :title="'跳转配置'">
        <i v-if="activeModel==='edit'&&curComponent&&curComponent.type==='de-jump'" class="icon iconfont icon-genghuan" @click.stop="setJump" />
      </span>
      <span :title="'锁定'">
        <svg-icon v-if="activeModel==='edit'&&curComponent&&lockValue" :icon-class="'locking'" class="icon" style="color:#fff" @click.stop="setLockout(false)" />
      </span>
      <span :title="'解锁'">
        <svg-icon v-if="activeModel==='edit'&&curComponent&&!lockValue" :icon-class="'Unlock'" class="icon" style="color:#fff" @click.stop="setLockout(true)" />
      </span>
    </div>
    <!-- 轮播图的数据修改 -->
    <el-dialog
      title="提示"
      :visible.sync="dialogVisible"
      width="30%"
    >
      <span>这是一段信息</span>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="dialogVisible = false">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import bus from '@/utils/bus'
import SettingMenu from '@/components/canvas/components/Editor/SettingMenu'
import LinkageField from '@/components/canvas/components/Editor/LinkageField'
import toast from '@/components/canvas/utils/toast'
import { deepCopy } from '../../utils/utils'

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
    },
    previewVisible: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      componentType: null,
      dialogVisible: false,
      linkageActiveStatus: false,
      editFilter: [
        'view',
        'custom',
        'customBottm'
      ],
      timer: null,
      check: false
    }
  },
  mounted() {
  },
  computed: {
    // 联动区域按钮显示
    linkageAreaShow() {
      return this.linkageSettingStatus && this.element !== this.curLinkageView && this.element.type === 'view'
    },
    // 多选框 显示
    checkboxShow() {
      console.log('checkShow::::::', this.element)
      return this.checkboxStatus
    },
    // 编辑或预览区域显示
    normalAreaShow() {
      return !this.linkageSettingStatus && !this.checkboxStatus
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
      console.log('link11111111111', this.targetLinkageInfo, this.element)
      return this.targetLinkageInfo[this.element.propValue.viewId]
    },
    miniHeight() {
      let miniHeight = this.curComponent.miniSizey || 1
      if (this.element.component === 'de-number-range') {
        miniHeight = this.curComponent.miniSizey || 2
      }
      return miniHeight
    },
    miniWidth() {
      return this.curComponent.miniSizex || 1
    },
    lockValue() {
      console.log('213123', this.curComponent)
      return this.curComponent.isLock
    },
    setNewValue() {
      const style = {}
      console.log('标题数据1', this.curComponent)
      if (this.curComponent && this.curComponent.type === 'v-text' && !this.checkboxStatus) {
        style.right = '-40px'
      }
      return style
    },
    ...mapState([
      'menuTop',
      'menuLeft',
      'menuShow',
      'curComponent',
      'componentData',
      'canvasStyleData',
      'linkageSettingStatus',
      'checkboxStatus',
      'targetLinkageInfo',
      'curLinkageView',
      'curCanvasScale'
    ])
  },
  beforeDestroy() {
  },
  methods: {
    checkChange(boo) {
      console.log('isCheck', boo, this.element.id,)
      const componentData = deepCopy(this.componentData)
      componentData.map(item => {
        if (this.element.id === item.id) {
          item.isCheck = boo
        }
        if (item.commonBackground.boxWidth && item.commonBackground.boxHeight) {
          item.commonBackground.boxWidth = Math.floor(item.commonBackground.boxWidth)
          item.commonBackground.boxHeight = Math.floor(item.commonBackground.boxHeight)
        } else {
          var info = document.getElementById('eleId' + item.id)
          item.commonBackground.boxWidth = Math.floor(info.offsetWidth)
          item.commonBackground.boxHeight = Math.floor(info.offsetHeight)
        }
        // console.log('eleId',item)
      })
      this.$store.commit('setComponentData', componentData)
      console.log('这个 值？', this.componentData)

      const arr = this.componentData.filter(item => item.isCheck && !item.isLock)
      if (arr.length === 2) {
        this.$store.commit('setUniformityStatus', true)
      } else {
        this.$store.commit('setUniformityStatus', false)
      }
    },
    closePreview() {
      this.$emit('closePreview')
    },
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
      this.curComponent.auxiliaryMatrix = false
      if (this.curComponent.auxiliaryMatrix) {
        // this.curComponent.auxiliaryMatrix = false
        // this.$emit('amRemoveItem')
      } else {
        // this.curComponent.auxiliaryMatrix = false
        // this.$emit('amRemoveItem')
        // this.curComponent.x = Math.round(this.curComponent.style.left / this.curCanvasScale.matrixStyleOriginWidth) + 1
        // this.curComponent.y = Math.round(this.curComponent.style.top / this.curCanvasScale.matrixStyleOriginHeight) + 1
        // this.curComponent.sizex = Math.round(this.curComponent.style.width / this.curCanvasScale.matrixStyleOriginWidth)
        // this.curComponent.sizey = Math.round(this.curComponent.style.height / this.curCanvasScale.matrixStyleOriginHeight)
        // this.curComponent.sizey = this.curComponent.sizey > this.miniHeight ? this.curComponent.sizey : this.miniHeight
        // this.curComponent.sizex = this.curComponent.sizex > this.miniWidth ? this.curComponent.sizex : this.miniWidth
        // this.curComponent.auxiliaryMatrix = false
        // this.$emit('amAddItem')
      }
      // setTimeout(() => {
      // this.recordMatrixCurShadowStyle()
      // }, 50)
      // this.$store.state.styleChangeTimes++
      // bus.$emit('auxiliaryMatrixChange')
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
      console.log('矩阵处理style====', style)
      this.$store.commit('setShapeStyle', style)
      // resize
      this.$emit('resizeView')
    },
    // edit() {
    //   // 编辑时临时保存 当前修改的画布
    //   this.$store.dispatch('panel/setComponentDataTemp', JSON.stringify(this.componentData))
    //   this.$store.dispatch('panel/setCanvasStyleDataTemp', JSON.stringify(this.canvasStyleData))
    //   if (this.curComponent.type === 'view') {
    //     this.$store.dispatch('chart/setViewId', null)
    //     this.$store.dispatch('chart/setViewId', this.curComponent.propValue.viewId)
    //     bus.$emit('PanelSwitchComponent', { name: 'ChartEdit', param: { 'id': this.curComponent.propValue.viewId, 'optType': 'edit' }})
    //   }
    //   if (this.curComponent.type === 'custom') {
    //     bus.$emit('component-dialog-edit')
    //   }
    //   // 编辑样式组件
    //   if (this.curComponent.type === 'v-text' || this.curComponent.type === 'rect-shape') {
    //     bus.$emit('component-dialog-style')
    //   }
    // },
    edit() {
      if (this.curComponent.type === 'custom' || this.curComponent.type === 'customBottm') {
        bus.$emit('component-dialog-edit')
      } else if (this.curComponent.type === 'v-text' || this.curComponent.type === 'rect-shape') {
        bus.$emit('component-dialog-style')
      } else { bus.$emit('change_panel_right_draw', true) }
    },
    linkageEdit() {

    },
    amRemoveItem() {
      console.log('设置点击', 2222)
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
      console.log('设置点击', 3333)
      this.$emit('linkJumpSet')
    },
    goFile() {
      this.$refs.files.click()
    },
    goBannerFile() {
      // this.dialogVisible = true
      console.log('-------------------------------------------------------', this.element)
      this.$emit('bannerImg')
    },
    setNavInfo() {
      this.$emit('setNavInfo')
    },
    setFontIcon() {
      this.$emit('setFontIcon')
    },
    setPicture() {
      this.$emit('setPicture')
    },
    setWeather() {
      this.$emit('setWeather')
    },
    setJump() {
      this.$emit('setJump')
    },
    setCustom() {
      console.log('触发此处？？？？？')
      this.$emit('setCustom')
    },
    setLockout(key) {
      // this.$emit('setLockout')
      this.curComponent.isLock = key
      console.log('this.curComponent', key)
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
      console.log('添加设置功能', 1111)
      this.$emit('boardSet')
    },

    tabRelation() {
      console.log('第二层', 1111)
      this.$emit('tabRelation')
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
    // bottom:-25px;
  }
  .bar-main i{
    color: white;
    float: right;
    margin-right: 3px;
  }

</style>

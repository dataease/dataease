<template>
  <div class="portal-drawer-container">
    <el-drawer
      :visible.sync="syncVisible"
      size="80%"
      direction="rtl"
      :show-close="false"
      :with-header="false"
    >
      <div class="portal-drawer-container-container">
        <div class="header">
          <div class="headerleft">
            <i class="el-icon-close" @click="syncVisible = false" />
            <div class="name">{{ portalName || "未命名站点" }}</div>
          </div>
          <div class="headerright">
            <div class="wrapper">
              <i class="el-icon-setting" @click="handleOpenConfigDrawer" />
            </div>
            <div class="wrapper">
              <el-radio-group
                v-model="activeTab"
                size="mini"
                round
                @change="handleEditPreivewTab"
              >
                <el-radio-button label="edit">编辑</el-radio-button>
                <el-radio-button label="preview" :disabled="true">预览1</el-radio-button>
              </el-radio-group>
            </div>
            <div class="wrapper">
              <el-button
                type="primary"
                round
                size="mini"
                @click="handleSave"
              >保 存</el-button>
            </div>
          </div>
        </div>
        <div class="content">
          <el-container class="config-container">
            <el-header
              v-if="
                (navLayoutStyle == 0 || navLayoutStyle == 2) &&
                  topNavPosRadio == 'top'
              "
              class="config-header"
              :style="{ backgroundColor: themeColor,justifyContent:floatPosition,backgroundImage:`url(${navBageImg})`,height:titleFontSet.height+'px' }"
            >
              <div v-if="headerNavStyle==='1'&&( floatPosition ==='center'||floatPosition ==='flex-end')" :style="menuStyleSet">一级菜单</div>
              <div v-if="headerNavStyle==='1'" :style="titleStyleSet">{{ portalName || "未命名站点" }}</div>
              <div v-if="headerNavStyle==='1'&&( floatPosition ==='center'||floatPosition ==='flex-start')" :style="menuStyleSet">一级菜单</div>
              <div v-if="headerNavStyle==='0'" class="title">{{ portalName || "未命名站点" }}</div>
              <div v-if="headerNavStyle==='0'" class="tabs">
                <el-menu
                  :default-active="topActiveTab"
                  class="top-nav-menu"
                  mode="horizontal"
                  :background-color="themeColor"
                  active-text-color="#333"
                  @select="handleTopSelect"
                >
                  <el-menu-item index="0">一级菜单</el-menu-item>
                </el-menu>
              </div>
            </el-header>
            <el-container>
              <el-aside
                v-if="navLayoutStyle == 0 || navLayoutStyle == 1"
                class="config-aside"
                width="200px"
                :style="{ backgroundColor: themeColor }"
              >
                <el-menu
                  default-active="0-1"
                  class="el-menu-vertical-demo"
                  :background-color="themeColor"
                  text-color="#333"
                >
                  <el-submenu index="0">
                    <template slot="title">
                      <i class="el-icon-menu" />
                      <span slot="title">二级菜单</span>
                    </template>
                    <el-menu-item index="0-1">
                      <span slot="title">三级菜单</span>
                    </el-menu-item>
                  </el-submenu>
                </el-menu>
              </el-aside>
              <el-main class="config-main">
                <img src="./portal-trend-bg.png" alt="">
              </el-main>
            </el-container>
            <el-header
              v-if="
                (navLayoutStyle == 0 || navLayoutStyle == 2) &&
                  topNavPosRadio == 'bottom'
              "
              class="config-header"
              :style="{ backgroundColor: themeColor }"
            >
              <!-- <div>底部菜单</div> -->
              <div class="title">{{ portalName || "未命名站点" }}</div>
              <div class="tabs">
                <el-menu
                  :default-active="topActiveTab"
                  class="top-nav-menu"
                  mode="horizontal"
                  :background-color="themeColor"
                  active-text-color="#333"
                  @select="handleTopSelect"
                >
                  <el-menu-item
                    index="0"
                    :style="{ borderColor: topActiveTab == 0 ? '#429eff' : '' }"
                  >一级菜单</el-menu-item>
                  <!-- <el-menu-item index="1" :style="{ borderColor: topActiveTab == 1 ? '#429eff' : '' }">二级菜单</el-menu-item> -->
                </el-menu>
              </div>
            </el-header>
          </el-container>
          <div class="right-config-container">
            <div class="title">门户配置</div>
            <div class="config-wrapper" :style="{height:configHeight()}">
              <div class="wrapper horization">
                <div class="name">标题设置</div>
                <el-input
                  v-model="portalName"
                  size="mini"
                  placeholder="未命名标题"
                />
              </div>
              <div class="wrapper">
                <div class="name">PC端样式</div>
                <div class="layout">
                  <div class="item" @click="setNavLayoutStyle(0)">
                    <!-- <img
                      :class="{ active: navLayoutStyle == 0 }"
                      src="./left-top-layout.png"
                      alt=""
                    /> -->
                    <svg-icon
                      icon-class="pltl"
                      class="icon-layout"
                      :class="{ active: navLayoutStyle == 0 }"
                    />
                    <span class="item-name">双导航布局</span>
                  </div>
                  <div class="item" @click="setNavLayoutStyle(1)">
                    <!-- <img
                      :class="{ active: navLayoutStyle == 1 }"
                      src="./only-left-layout.png"
                      alt=""
                    /> -->
                    <svg-icon
                      icon-class="poll"
                      class="icon-layout"
                      :class="{ active: navLayoutStyle == 1 }"
                    />
                    <span class="item-name">左导航布局</span>
                  </div>
                  <div class="item" @click="setNavLayoutStyle(2)">
                    <!-- <img
                      :class="{ active: navLayoutStyle == 2 }"
                      src="./only-top-layout.png"
                      alt=""
                    /> -->
                    <svg-icon
                      icon-class="potl"
                      class="icon-layout"
                      :class="{ active: navLayoutStyle == 2 }"
                    />
                    <span class="item-name">顶部导航布局</span>
                  </div>
                </div>
              </div>
              <div class="wrapper">
                <div class="name">一级导航位置</div>
                <el-radio-group
                  v-model="topNavPosRadio"
                  @change="handleChangeTopNavPosRadio"
                >
                  <el-radio label="top">顶部</el-radio>
                  <el-radio label="bottom">底部</el-radio>
                </el-radio-group>
              </div>
              <div class="wrapper">
                <div class="name">主题设置</div>
                <el-color-picker
                  v-model="themeColor"
                  size="small"
                  @change="handleChangeThemeColor"
                />
              </div>

              <div v-if="navLayoutStyle==='2'" class="wrapper">
                <div class="name">顶部导航类型</div>
                <el-radio-group
                  v-model="headerNavStyle"
                >
                  <el-radio label="0">默认</el-radio>
                  <el-radio label="1">浮动</el-radio>
                </el-radio-group>
              </div>

              <div v-if="navLayoutStyle==='2'&&headerNavStyle==='1'" class="wrapper">
                <div class="name">浮动位置</div>
                <el-radio-group
                  v-model="floatPosition"
                >
                  <el-radio label="flex-start">居左</el-radio>
                  <el-radio label="center">居中</el-radio>
                  <el-radio label="flex-end">居右</el-radio>
                </el-radio-group>
              </div>
              <div v-if="navLayoutStyle==='2'&&headerNavStyle==='1'" class="wrapper">
                <div class="name">标题空间宽度(单位%)</div>
                <div>
                  <el-slider
                    v-model="titleWidth"
                    :min="10"
                    :max="50"
                  />
                </div>
              </div>
              <div v-if="navLayoutStyle==='2'&&headerNavStyle==='1'" class="wrapper">
                <div class="font_set" style="margin-top:10px;">
                  <span style="margin-right:10px;">导航高度</span>
                  <span style="width:60%">
                    <el-input-number v-model="titleFontSet.height" :min="10" />
                  </span>
                </div>
                <div class="name">标题字体样式设置</div>
                <div class="font_set">
                  <span style="margin-right:10px;">隐藏标题</span>
                  <el-radio-group
                    v-model="titleFontSet.opacity"
                  >
                    <el-radio label="0">是</el-radio>
                    <el-radio label="1">否</el-radio>

                  </el-radio-group>
                </div>
                <div class="font_set">
                  <span style="margin-right:10px;">字体颜色</span>
                  <el-color-picker
                    v-model="titleFontSet.color"
                    size="small"
                  />
                </div>
                <div class="font_set" style="margin-top:10px;">
                  <span style="margin-right:10px;">字体大小</span>
                  <span style="width:60%">
                    <el-input-number v-model="titleFontSet.fontSize" :min="12" />
                  </span>
                </div>
                <div class="font_set" style="margin-top:10px;">
                  <span style="margin-right:10px;">字体类型</span>
                  <span style="width:60%">
                    <el-select v-model="titleFontSet.fontFamily" placeholder="请选择">
                      <el-option
                        v-for="items in fontOptions"
                        :key="items"
                        :label="items"
                        :value="items"
                      />
                    </el-select>
                  </span>
                </div>
              </div>
              <div v-if="navLayoutStyle==='2'&&headerNavStyle==='1'" class="wrapper">
                <div class="name">菜单空间宽度(单位%)</div>
                <div>
                  <el-slider
                    v-model="menuWidth"
                    :min="30"
                    :max="200"
                    :format-tooltip="formatTooltip"
                  />
                </div>
              </div>
              <div v-if="navLayoutStyle==='2'&&headerNavStyle==='1'" class="wrapper">
                <div class="name">菜单字体样式设置</div>
                <div class="font_set">
                  <span style="margin-right:10px;">字体颜色</span>
                  <el-color-picker
                    v-model="menuFontSet.color"
                    size="small"
                  />
                </div>
                <div class="font_set" style="margin-top:10px;">
                  <span style="margin-right:10px;">字体大小</span>
                  <span style="width:60%">
                    <el-input-number v-model="menuFontSet.fontSize" :min="12" />
                  </span>
                </div>
                <div class="font_set" style="margin-top:10px;">
                  <span style="margin-right:10px;">字体类型</span>
                  <span style="width:60%">
                    <el-select v-model="menuFontSet.fontFamily" placeholder="请选择">
                      <el-option
                        v-for="items in fontOptions"
                        :key="items"
                        :label="items"
                        :value="items"
                      />
                    </el-select>
                  </span>
                </div>
              </div>

              <div v-if="navLayoutStyle==='2'&&headerNavStyle==='1'" class="wrapper">
                <div class="name">导航背景图</div>
                <div>
                  <el-upload
                    action=""
                    accept=".jpeg,.jpg,.png,.gif,.svg"
                    class="avatar-uploader"
                    list-type="picture-card"
                    :class="{disabled:uploadDisabled}"

                    :on-remove="handleRemove"
                    :http-request="upload"
                    :file-list="fileList"
                    :on-change="onChange"
                  >
                    <i class="el-icon-plus" />
                  </el-upload>

                  <!-- <el-dialog top="25vh" width="600px" :modal-append-to-body="true" :visible.sync="dialogVisible">
                    <img width="100%" :src="dialogImageUrl" alt="">
                  </el-dialog> -->
                </div>
              </div>

            </div>

          </div>
        </div>
      </div>
    </el-drawer>
    <template v-if="openType == 'add'">
      <PortConfigDrawerComponent
        :visible.sync="showPortConfigDrawerComponent"
        :portal-name="portalName"
        :theme-color="themeColor"
        :nav-layout-style="navLayoutStyle"
        :top-nav-pos-radio="topNavPosRadio"
        :last-tree-id="lastTreeId"
        :open-type="openType"
        :config="{}"
        :portal-id="null"
        :header-nav-style="headerNavStyle"
        :title-width="titleWidth"
        :menu-width="menuWidth"
        :float-position="floatPosition"
        :title-font-set="titleFontSet"
        :menu-font-set="menuFontSet"
        :nav-bage-img="navBageImg"
        @treeData="handleGetTreeData"
        @close="syncVisible = false"
      />
    </template>
    <template v-else>
      <PortConfigDrawerComponent
        :visible.sync="showPortConfigDrawerComponent"
        :portal-name="portalName"
        :theme-color="themeColor"
        :nav-layout-style="navLayoutStyle"
        :top-nav-pos-radio="topNavPosRadio"
        :last-tree-id="lastTreeId"
        :open-type="openType"
        :config="item.config"
        :portal-id="item.id"
        :header-nav-style="headerNavStyle"
        :title-width="titleWidth"
        :menu-width="menuWidth"
        :float-position="floatPosition"
        :title-font-set="titleFontSet"
        :menu-font-set="menuFontSet"
        :nav-bage-img="navBageImg"
        @treeData="handleGetTreeData"
        @close="syncVisible = false"
      />
    </template>
    <template v-if="privewPortal">
      <!-- <div>222222222222222</div> -->
      <PanelViewShow
        ref="panelViewShow"
        :portal="privewPortal"
        @update="update"
      />
    </template>
  </div>
</template>

<script>
import PortConfigDrawerComponent from './PortalConfigDrawerComponent.vue'
import PanelViewShow from '@/views/panel/list/PanelViewShow.vue'
import bus from '@/utils/bus'
export default {
  components: {
    PortConfigDrawerComponent,
    PanelViewShow
  },
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    openType: {
      type: String,
      default: 'add' // edit
    },
    item: {
      type: Object,
      default: () => {}
    }
  },

  data() {
    return {
      fileList: [],
      fontOptions: ['宋体', '楷体', '黑体', '仿宋', '新宋体'],
      uploadDisabled: false,
      dialogImageUrl: '',
      showPortConfigDrawerComponent: false,
      dialogVisible: false,
      activeTab: 'edit', // 当前最顶部nav中是编辑还是预览
      topActiveTab: '0', // 当前顶部导航栏选择的下标选项
      topNavPosRadio: 'top', //  当前一级导航的位置
      themeColor: '#f1f3f8', // 当前导航的颜色
      navLayoutStyle: '0', // 0 top-left 1 left 2 top
      portalName: '', // 当前门户的名称
      lastTreeId: 0, // 当前的树节点的跟节点
      tmpTreeData: null, // 获取配置页面的treeData
      privewPortal: null,
      priviewBtnEnable: false,
      // 新增模式
      headerNavStyle: '0', // 0 默认 1浮动
      titleWidth: 10, // 标题宽度
      menuWidth: 5, // 菜单宽度
      floatPosition: 'flex-start', // 浮动位置
      titleFontSet: {
        height: 60,
        color: '#000',
        fontSize: 20,
        fontFamily: '',
        opacity: '1'
      },
      menuFontSet: {
        color: '#000',
        fontSize: 20,
        fontFamily: ''
      },
      navBageImg: ''// 导航背景图  style['background'] = `url(${this.element.commonBackground.outerImage}) no-repeat`
    }
  },

  computed: {
    // ...mapGetters([
    //   "navLayoutStyle", // 0 top-left 1 left 2 top
    // ]),
    syncVisible: {
      get() {
        return this.visible
      },
      set(val) {
        this.$emit('update:visible', val)
      }
    },
    titleStyleSet() {
      // {width:titleWidth+'%',textAlign:'center'}
      console.log('this.titleFontSet', this.titleFontSet)
      const style = {}
      style.width = this.titleWidth + '%'
      style.textAlign = 'center'
      style.color = this.titleFontSet.color
      style.fontSize = this.titleFontSet.fontSize + 'px'
      style.fontFamily = this.titleFontSet.fontFamily
      style.opacity = this.titleFontSet.opacity
      return style
    },
    menuStyleSet() {
      // {width:menuWidth+'%',textAlign:'center'}
      const style = {}
      style.width = this.menuWidth / 10 + '%'
      style.textAlign = 'center'
      style.color = this.menuFontSet.color
      style.fontSize = this.menuFontSet.fontSize + 'px'
      style.fontFamily = this.menuFontSet.fontFamily
      return style
    }
  },

  watch: {
    syncVisible(val) {
      if (val) {
        // 如果是新打开的状态
        this.__initData()
      }
    }
  },

  methods: {
    configHeight() {
      // let a = document.documentElement.clientHeight
      console.log('document.body.offsetHeight', document.body.offsetHeight, document.documentElement.clientHeight)
      return (document.body.offsetHeight - 180) + 'px'
    },
    formatTooltip(val) {
      return val / 10
    },
    handleRemove(file, fileList) {
      this.uploadDisabled = false
      this.navBageImg = ''
      this.panel.imageUrl = null
      this.fileList = []
      // this.commitStyle()
    },
    handlePictureCardPreview(file) {
      this.dialogImageUrl = file.url
      this.dialogVisible = true
    },
    onChange(file, fileList) {
      if (file.size / 1024 / 1024 > 10) {
        this.$message.error('上传的文件大小不能超过 10MB!')
        this.fileList = []
        return
      }
      var _this = this
      _this.uploadDisabled = true
      const reader = new FileReader()
      reader.onload = function() {
        console.log('图片数据', reader.result)
        // _this.curComponent.commonBackground.outerImage = reader.result
        _this.navBageImg = reader.result
      }
      reader.readAsDataURL(file.raw)
    },
    upload(file) {
      // console.log('this is upload')
    },
    // ...mapMutations({
    //   setTopNavPosRadio: "SET_TOP_NAV_POS_RADIO",
    //   setThemeColor: "SET_THEME_COLOR",
    //   setNavLayoutStyle: "SET_NAV_LAYOUT_STYLE",
    // }),

    __initData() {
      this.activeTab = 'edit' // 当前最顶部nav中是编辑还是预览
      this.topActiveTab = '0' // 当前顶部导航栏选择的下标选项
      this.fileList = []
      this.tmpTreeData = null
      if (this.openType === 'add') {
        this.topNavPosRadio = 'top' //  当前一级导航的位置
        this.themeColor = '#f1f3f8' // 当前导航的颜色
        this.navLayoutStyle = '0' // 0 top-left 1 left 2 top
        this.portalName = '' // 当前门户的名称
        this.lastTreeId = 0
      } else {
        this.topNavPosRadio = this.item.topNavPosRadio //  当前一级导航的位置
        this.themeColor = this.item.themeColor // 当前导航的颜色
        this.navLayoutStyle = this.item.navLayoutStyle // 0 top-left 1 left 2 top
        this.portalName = this.item.portalName // 当前门户的名称
        this.lastTreeId = this.item.lastTreeId
        this.headerNavStyle = this.item.headerNavStyle// 0 默认 1浮动
        this.titleWidth = this.item.titleWidth // 标题宽度
        this.menuWidth = this.item.menuWidth // 菜单宽度
        this.floatPosition = this.item.floatPosition // 浮动位置
        this.navBageImg = this.item.navBageImg
        if (this.item.titleFontSet) {
          this.titleFontSet = this.item.titleFontSet
        }
        if (this.item.menuFontSet) {
          this.menuFontSet = this.item.menuFontSet
        }

        if (this.item.navBageImg !== '') {
          this.fileList.push({ url: this.item.navBageImg })
        }
        // this.fileList.push({ url: this.item.navBageImg })
        if (JSON.stringify(this.fileList) !== '[]') {
          this.uploadDisabled = true
        }
      }
      // this.priviewBtnEnable = !!this.item.config.treeData || !!this.tmpTreeData
    },

    /** *************** 预览 start ****************/
    handleEditPreivewTab(evt) {
      console.log('evt -- ', evt)
      if (evt == 'edit') {
        return
      }
      this.privewPortal = {
        navLayoutStyle: this.navLayoutStyle, // 0-双导航布局 1-左导航布局 2-顶部导航布局
        topNavPosRadio: this.topNavPosRadio, // top-底部 bottom-底部
        themeColor: this.themeColor, // 默认
        portalName: this.portalName || '未命名站点', // 站点名称
        config: {
          treeData: Object.assign({}, this.item.config.treeData, this.tmpTreeData)
        }
      }
      this.$nextTick(() => {
        console.log('this.$refs.panelViewShow', this.$refs.panelViewShow)
        if (this.$refs.panelViewShow) {
          this.$refs.panelViewShow.clickFullscreen()

          this.$watch(
            () => this.$refs.panelViewShow.fullscreen,
            (val) => {
              if (!val) {
                this.activeTab = 'edit'
                this.privewPortal = null
              }
            }
          )
        }
      })
    },

    update(trendId) {
      if (trendId) {
        if (Object.prototype.toString.call(trendId) == '[object Array]') {
          trendId = trendId[trendId.length - 1]
        }
        initPanelData(trendId, function(response) {
          bus.$emit('set-panel-show-type', 0)
        })
      } else {
        this.$refs.panelViewShow.showMain = false
      }
    },

    /** *************** 预览 end ****************/

    // 一级导航位置
    handleChangeTopNavPosRadio(radio) {
      console.log('radio', radio)
      // this.setTopNavPosRadio(radio);
    },
    // 选择主题设置
    handleChangeThemeColor(color) {
      console.log('color', color)
      // this.setThemeColor(color);
    },
    setNavLayoutStyle(style) {
      this.navLayoutStyle = style.toString()
    },
    // 选择一级菜单
    handleTopSelect(active) {
      console.log('active', active)
      this.topActiveTab = active
    },

    // 获取配置页面的treeData
    handleGetTreeData(treeData) {
      this.tmpTreeData = treeData
    },
    // 打开配置
    handleOpenConfigDrawer() {
      this.showPortConfigDrawerComponent = true
    },
    // 保存
    handleSave() {
      const getTreeData = () => {
        if (this.tmpTreeData) {
          return this.tmpTreeData
        }
        if (this.item && this.item.config && this.item.config.treeData) {
          return this.item.config.treeData
        }
        return []
      }
      const params = {
        navLayoutStyle: this.navLayoutStyle, // 0-双导航布局 1-左导航布局 2-顶部导航布局
        topNavPosRadio: this.topNavPosRadio, // top-底部 bottom-底部
        themeColor: this.themeColor, // 默认
        portalName: this.portalName || '未命名站点', // 站点名称
        lastTreeId: this.lastTreeId,
        headerNavStyle: this.headerNavStyle, // 0 默认 1浮动
        floatPosition: this.floatPosition, // 浮动位置
        titleWidth: this.titleWidth, // 标题宽度
        menuWidth: this.menuWidth, // 菜单宽度
        navBageImg: this.navBageImg,
        titleFontSet: this.titleFontSet,
        menuFontSet: this.menuFontSet,
        config: {
          treeData: getTreeData()
        }
      }
      const positionJson = JSON.stringify(params)
      if (this.openType === 'add') {
        bus.$emit('savePortal', { positionJson })
      } else {
        bus.$emit('updatePortal', { id: this.item.id, positionJson })
      }
      this.syncVisible = false
    }
  }
}
</script>

<style lang="less" scoped>
.portal-drawer-container {
  ::v-deep .el-menu,
  .el-menu.el-menu--horizontal {
    border: unset;
  }
  ::v-deep .el-header {
    padding: 0;
  }
   /deep/ .avatar-uploader .el-upload {
    width: 120px;
    height: 80px;
    line-height: 90px;
  }
  /deep/ .avatar-uploader .el-upload-list li{
    width: 120px !important;
    height: 80px !important;
  }
  /deep/ .disabled .el-upload--picture-card {
    display: none;
  }
  .font_set{
    display:flex;
    align-items:center;
    /deep/ .el-input-number--medium{
      width:130px;
    }
  }
  i {
    cursor: pointer;
  }
  .portal-drawer-container-container {
    position: relative;
    .header {
      position: sticky;
      left: 0;
      right: 0;
      padding: 16px;
      box-sizing: border-box;
      border-bottom: 1px solid var(--TopBG, #e6e6e6);
      background-color: var(--TopBG, #f1f3f8);
      box-sizing: border-box;
      display: flex;
      align-items: center;
      justify-content: space-between;
      .headerleft {
        display: flex;
        align-items: center;
        .name {
          margin-left: 14px;
        }
      }
      .headerright {
        display: flex;
        align-items: center;
        .wrapper {
          margin-right: 10px;
        }
        .wrapper:last-child {
          margin-right: 0;
        }
      }
    }
    .content {
      display: flex;
      justify-content: space-between;
      font-size: 14px;
      color: #333;
      min-height: calc(100vh - 60px);

      .config-container {
        flex: 3;
        .config-header {
          background-color: var(--TopBG, #f1f3f8);
          background-size:100% 100%;
          background-repeat: no-repeat;
          // min-height: 60px;
          display: flex;
          align-items:center;
          .title {
            // background-color: green;
            width: 200px;
            line-height: 60px;
            text-align: center;
            border-bottom: 1px solid var(--TopBG, #e6e6e6);
          }
        }

        .config-aside {
          height: 100%;
        }

        .config-main {
          display: flex;
          align-items: center;
          justify-content: center;
          background-color: var(--TopBG, #e6e6e6);
          img {
            width: 400px;
          }
        }
      }

      .right-config-container {
        flex: 1;
        background-color: var(--TopBG, #f1f3f8);
        .title {
          // background-color: var(--TopBG, #e6e6e6);
          border-bottom: 1px solid var(--TopBG, #e6e6e6);
          border-left: 1px solid var(--TopBG, #e6e6e6);
          height: 60px;
          line-height: 60px;
          padding-left: 20px;
          box-sizing: border-box;
        }
        .config-wrapper {
          // max-height:580px;
          overflow-y:scroll;
          .wrapper.horization {
            display: flex;
            align-items: center;
            .name {
              margin-right: 10px;
              margin-bottom: 0;
              flex-shrink: 0;
            }
          }
          padding: 14px;
          box-sizing: border-box;
          .wrapper {
            margin-bottom: 20px;
            .name {
              margin-bottom: 8px;
            }
            .layout {
              display: flex;
              align-items: center;
              justify-content: space-around;
              .item {
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                .icon-layout {
                  font-size: 69px;
                  font-weight: 400;
                  color: #666;
                  margin-bottom: 8px;
                }
                .active {
                  color: #409eff;
                  // border: 2px solid #409eff;
                  // border-radius: 4px;
                  // box-sizing: border-box;
                }
              }
            }
          }
        }
      }
    }
  }
}
</style>

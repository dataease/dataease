<template>
  <div>
    <div v-show="editControlButton" class="toolbar">
      <span v-show="mobileLayoutStatus" style="float: right;">
        <el-button v-if="mobileLayoutStatus" size="mini" @click="editReset">
          {{ $t('commons.reset') }}
        </el-button>
        <el-button size="mini" @click="editSave">
          {{ $t('commons.confirm') }}
        </el-button>
        <el-button size="mini" @click="editCancel">
          {{ $t('commons.cancel') }}
        </el-button>
      </span>
      <span v-show="linkageSettingStatus" style="float: right;">
        <el-button size="mini" @click="editSave">
          {{ $t('commons.confirm') }}
        </el-button>
        <el-button size="mini" @click="editCancel">
          {{ $t('commons.cancel') }}
        </el-button>
      </span>
      <span v-show="checkboxStatus" style="float: right;">
        <el-tooltip :content="isMove? $t('commons.position.move') : $t('commons.back')">
          <el-button :class="isMove? 'el-icon-rank' : 'el-icon-d-arrow-left'" size="mini" circle    @click="moveClick" />
        </el-tooltip>
        <span v-if="isMove" style="padding: 0px 10px;">
          <el-tooltip :content="$t('commons.position.horizontally')">
            <el-button size="mini" circle @click="positionChange('horizontally')">
              <svg-icon icon-class="spjz" class="chart-icon" />
            </el-button>
          </el-tooltip>
          <el-tooltip :content="$t('commons.position.vertically')">
            <el-button size="mini" circle @click="positionChange('vertically')">
              <svg-icon icon-class="czjz" class="chart-icon" />
            </el-button>
          </el-tooltip>
          <el-tooltip :content="$t('commons.position.transverse')">
            <el-button :disabled="isUniformity" size="mini" circle @click="positionChange('transverse')">
              <svg-icon icon-class="hxfb" class="chart-icon" />
            </el-button>
          </el-tooltip>
          <el-tooltip :content="$t('commons.position.longitudinal')">
            <el-button size="mini" :disabled="isUniformity" circle @click="positionChange('longitudinal')">
              <svg-icon icon-class="zxfb" class="chart-icon" />
            </el-button>
          </el-tooltip>
          <el-tooltip :content="$t('commons.position.left')">
            <el-button size="mini" circle @click="positionChange('left')">
              <svg-icon icon-class="jzdq" class="chart-icon" />
            </el-button>
          </el-tooltip>
          <el-tooltip :content="$t('commons.position.right')">
            <el-button size="mini" circle @click="positionChange('right')">
              <svg-icon icon-class="jydq" class="chart-icon" />
            </el-button>
          </el-tooltip>
          <el-tooltip :content="$t('commons.position.top')">
            <el-button size="mini" circle @click="positionChange('top')">
              <svg-icon icon-class="dbdq" class="chart-icon" />
            </el-button>
          </el-tooltip>
          <el-tooltip :content="$t('commons.position.bottom')">
            <el-button size="mini" circle @click="positionChange('bottom')">
              <svg-icon icon-class="dibdq" class="chart-icon" />
            </el-button>
          </el-tooltip>
        </span>
        <span v-else style="padding: 0px 10px;">
          <el-input-number v-model="moveSize" :min="1" :max="1000" size="mini" style="width: 100px;margin-right: 10px;"></el-input-number>
          <el-tooltip :content="$t('commons.move.left')">
            <el-button class="el-icon-back" size="mini" circle @click="moveChange('left')" />
          </el-tooltip>
          <el-tooltip :content="$t('commons.move.right')">
            <el-button class="el-icon-right" size="mini" circle @click="moveChange('right')" />
          </el-tooltip>
          <el-tooltip :content="$t('commons.move.top')">
            <el-button class="el-icon-top" size="mini" circle @click="moveChange('top')" />
          </el-tooltip>
          <el-tooltip :content="$t('commons.move.bottom')">
            <el-button class="el-icon-bottom" size="mini" circle @click="moveChange('bottom')" />
          </el-tooltip>
        </span>
        <el-tooltip :content="$t('panel.undo') ">
          <el-button class="el-icon-refresh-right" size="mini" circle @click="undo" />
        </el-tooltip>
        <el-button size="mini" @click="checkDel">
          {{ $t('commons.delete') }}
        </el-button>
        <el-button size="mini" @click="checkBack">
          {{ $t('commons.back') }}
        </el-button>
      </span>
    </div>
    <div v-show="!editControlButton" class="toolbar">
      <el-tooltip :content="$t('panel.mobile_layout')">
        <el-button class="icon iconfont-tb icon-yidongduan" size="mini" circle @click="openMobileLayout" />
      </el-tooltip>
      <el-tooltip v-if="!canvasStyleData.auxiliaryMatrix" :content="$t('panel.new_element_distribution')+':'+$t('panel.suspension')">
        <el-button class="icon iconfont-tb icon-xuanfuanniu" size="mini" circle @click="auxiliaryMatrixChange" />
      </el-tooltip>
      <el-tooltip v-if="canvasStyleData.auxiliaryMatrix" :content="$t('panel.new_element_distribution')+':'+$t('panel.matrix')+'12321'">
        <el-button class="icon iconfont-tb icon-shujujuzhen" size="mini" circle @click="auxiliaryMatrixChange" />
      </el-tooltip>
      <el-tooltip :content="$t('panel.style')">
        <el-button class="el-icon-magic-stick" size="mini" circle @click="showPanel" />
      </el-tooltip>
      <el-tooltip :content="$t('panel.undo') ">
        <el-button class="el-icon-refresh-right" size="mini" circle @click="undo" />
      </el-tooltip>
      <el-tooltip :content="$t('panel.redo') ">
        <el-button class="el-icon-refresh-left" size="mini" circle @click="redo" />
      </el-tooltip>
      <el-tooltip :content="$t('panel.clean_canvas')">
        <el-button class="el-icon-document-delete" size="mini" circle @click="clearCanvas" />
      </el-tooltip>
      <el-tooltip :content="$t('panel.fullscreen_preview')">
        <el-button class="el-icon-view" size="mini" circle @click="clickPreview" />
      </el-tooltip>
      <el-tooltip :content="$t('panel.params_setting')">
        <el-button class="icon iconfont-tb icon-canshu" size="mini" circle @click="openOuterParamsSet" />
      </el-tooltip>
      <el-tooltip :content="$t('panel.params_checkbox')">
        <el-button class="el-icon-connection icon-duoxuan" size="mini" circle @click="clickCheckbox" />
      </el-tooltip>
      <el-tooltip :content="$t('panel.paste')" v-if="isCopyToPaste">
        <el-button class="el-icon-document-copy" size="mini" circle @click="clickPaste" />
      </el-tooltip>
      <span style="float: right;margin-left: 10px">
        <el-button size="mini" :disabled="saveButtonDisabled" @click="save(false)">
          {{ $t('commons.save') }}
        </el-button>
        <el-button size="mini" @click="closePanelEdit">
          {{ $t('commons.close') }}
        </el-button>
      </span>
    </div>

    <!--关闭弹框-->
    <el-dialog :visible.sync="closePanelVisible" :title="$t('panel.panel_save_tips')" :show-close="false" width="30%" class="dialog-css">
      <el-row style="height: 20px">
        <el-col :span="4">
          <svg-icon icon-class="warn-tre" style="width: 20px;height: 20px;float: right" />
        </el-col>
        <el-col :span="20">
          <span style="font-size: 13px;margin-left: 10px;font-weight: bold;line-height: 20px">{{ $t('panel.panel_save_warn_tips') }}</span>
        </el-col>
      </el-row>
      <div slot="footer" class="dialog-footer">
        <el-button style="float: left" size="mini" @click="closeNotSave()">{{ $t('panel.do_not_save') }}</el-button>
        <el-button size="mini" @click="closePanelVisible=false">{{ $t('panel.cancel') }}</el-button>
        <el-button type="primary" size="mini" @click="save(true)">{{ $t('panel.save') }}
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import generateID from '@/components/canvas/utils/generateID'
import toast from '@/components/canvas/utils/toast'
import { mapState } from 'vuex'
import { commonStyle, commonAttr } from '@/components/canvas/custom-component/component-list'
import eventBus from '@/components/canvas/utils/eventBus'
import { deepCopy, mobile2MainCanvas } from '@/components/canvas/utils/utils'
import { panelSave } from '@/api/panel/panel'
import { saveLinkage, getPanelAllLinkageInfo } from '@/api/panel/linkage'
import bus from '@/utils/bus'
import {
  DEFAULT_COMMON_CANVAS_STYLE_STRING
} from '@/views/panel/panel'
import { queryPanelJumpInfo } from '@/api/panel/linkJump'

export default {
  name: 'Toolbar',
  props: {
    styleButtonActive: Boolean,
    aidedButtonActive: Boolean
  },
  data() {
    return {
      isShowPreview: false,
      needToChange: [
        'top',
        'left',
        'width',
        'height',
        'fontSize',
        'borderWidth'
      ],
      scale: '100%',
      timer: null,
      changes: 0,
      closePanelVisible: false,
      isMove: true,
      moveSize: 50,
    }
  },
  computed: {
    saveButtonDisabled() {
      return this.changeTimes === 0 || this.snapshotIndex === this.lastSaveSnapshotIndex
    },
    editControlButton() {
      console.log('panduan1shisd=================', this.linkageSettingStatus, this.mobileLayoutStatus, this.checkboxStatus)
      return this.linkageSettingStatus || this.mobileLayoutStatus || this.checkboxStatus
    },
    ...mapState([
      'componentData',
      'canvasStyleData',
      'isCopyToPaste',
      'areaData',
      'curComponent',
      'changeTimes',
      'snapshotIndex',
      'lastSaveSnapshotIndex',
      'linkageSettingStatus',
      'checkboxStatus',
      'isUniformity',
      'curLinkageView',
      'targetLinkageInfo',
      'mobileLayoutStatus',
      'mobileComponentData',
      'componentDataCache'
    ])
  },
  created() {
    eventBus.$on('preview', this.preview)
    eventBus.$on('save', this.save)
    eventBus.$on('clearCanvas', this.clearCanvas)
    this.scale = this.canvasStyleData.scale
  },
  methods: {
    close() {
      // 关闭页面清理缓存
      this.clearCanvas()
      this.$emit('close-left-panel')
      this.$nextTick(() => {
        bus.$emit('PanelSwitchComponent', { name: 'PanelMain' })
      })
      this.$store.commit('setPanelStatus',false)
    },
    closePanelEdit() {
      if (this.changeTimes === 0 || this.snapshotIndex === this.lastSaveSnapshotIndex) { // 已保存
        this.close()
      } else {
        this.closePanelVisible = true
      }

      this.$store.commit('setPanelStatus',false)
    },
    goFile() {
      this.$refs.files.click()
    },
    format(value) {
      const scale = this.scale
      return value * scale / 100
    },
    getOriginStyle(value) {
      const scale = this.canvasStyleData.scale
      const result = value / (scale / 100)
      return result
    },
    handleScaleChange() {
      clearTimeout(this.timer)
      setTimeout(() => {
        const componentData = deepCopy(this.componentData)
        componentData.forEach(component => {
          Object.keys(component.style).forEach(key => {
            if (this.needToChange.includes(key)) {
              // 根据原来的比例获取样式原来的尺寸
              // 再用原来的尺寸 * 现在的比例得出新的尺寸
              component.style[key] = this.format(this.getOriginStyle(component.style[key]))
            }
          })
        })
        this.$store.commit('setComponentData', componentData)
        this.$store.commit('setCanvasStyle', {
          ...this.canvasStyleData,
          scale: this.scale
        })
      }, 1000)
    },

    lock() {
      this.$store.commit('lock')
    },

    unlock() {
      this.$store.commit('unlock')
    },

    compose() {
      this.$store.commit('compose')
      this.$store.commit('recordSnapshot', 'compose')
    },

    decompose() {
      this.$store.commit('decompose')
      this.$store.commit('recordSnapshot', 'decompose')
    },

    undo() {
      this.$store.commit('undo')
    },

    redo() {
      this.$store.commit('redo')
    },

    showPanel() {
      this.$emit('showPanel', 2)
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
        const img = new Image()
        img.onload = () => {
          const scaleWith = img.width / 400
          const scaleHeight = img.height / 200
          let scale = scaleWith > scaleHeight ? scaleWith : scaleHeight
          scale = scale > 1 ? scale : 1
          this.$store.commit('addComponent', {
            component: {
              ...commonAttr,
              id: generateID(),
              component: 'Picture',
              label: '图片',
              icon: '',
              propValue: fileResult,
              style: {
                ...commonStyle,
                top: 0,
                left: 0,
                width: img.width / scale,
                height: img.height / scale
              }
            }
          })
          this.$store.commit('recordSnapshot', 'handleFileChange')
        }
        img.src = fileResult
      }

      reader.readAsDataURL(file)
    },

    preview() {
      this.isShowPreview = true
      this.$store.commit('setEditMode', 'preview')
    },

    save(withClose) {
      // 清理联动信息
      this.$store.commit('clearPanelLinkageInfo')
      // 保存到数据库
      // console.log('this.canvasStyleData', this.componentData,this.canvasStyleData)
      this.componentData.forEach(ele => {
        // console.log('width', document.getElementById('eleId' + ele.id).offsetWidth)
        // ele.commonBackground.boxWidth = document.getElementById('eleId' + ele.id).offsetWidth
        // ele.commonBackground.boxHeight = document.getElementById('eleId' + ele.id).offsetHeight
        // ele.style.width = document.getElementById('eleId' + ele.id).offsetWidth
        // ele.style.height = document.getElementById('eleId' + ele.id).offsetHeight

        // console.log('获取盒子到左边和右边的距离', document.getElementById('eleId' + ele.id).offsetTop, document.getElementById('eleId' + ele.id).offsetLeft)
      })
      const requestInfo = {
        id: this.$store.state.panel.panelInfo.id,
        panelStyle: JSON.stringify(this.canvasStyleData),
        panelData: JSON.stringify(this.componentData)
      }
      const components = deepCopy(this.componentData)
      components.forEach(view => {
        if (view.filters && view.filters.length > 0) { view.filters = [] }
        if (view.type === 'de-tabs') {
          view.options.tabList && view.options.tabList.length > 0 && view.options.tabList.forEach(tab => {
            if (tab.content && tab.content.filters && tab.content.filters.length > 0) {
              tab.content.filters = []
            }
          })
        }
      })
      // 无需保存条件
      console.log(components)
      requestInfo.panelData = JSON.stringify(components)
      console.log('保存的数据',requestInfo)
      panelSave(requestInfo).then(response => {
        this.$store.commit('refreshSaveStatus')
        this.$message({
          message: this.$t('commons.save_success'),
          type: 'success',
          showClose: true
        })
        if (withClose) {
          this.close()
        }
      })
      this.$store.commit('setTemplateStatus', false)
      this.$store.commit('setPriorityStatus', false)
    },
    clearCanvas() {
      this.$store.commit('setComponentData', [])
      this.$store.commit('setCanvasStyle', DEFAULT_COMMON_CANVAS_STYLE_STRING)
      this.$store.commit('recordSnapshot', 'clearCanvas')
      this.$store.commit('setTemplateStatus', false)
      this.$store.commit('setPriorityStatus', false)
    },

    handlePreviewChange() {
      this.$store.commit('setEditMode', 'edit')
    },

    clickPreview() {
      this.$emit('previewFullScreen')
    },
    openOuterParamsSet() {
      this.$emit('outerParamsSetVisibleChange', true)
    },
    //  粘贴
    clickPaste() {
      console.log('粘贴',this.isCopyToPaste)
      this.$store.commit('setCopyToPaste',false)
      this.$store.commit('paste', true)
      this.$store.commit('recordSnapshot', 'paste')
    },
    clickCheckbox() {
      console.log('checkbox')
      if(this.componentData.length)  {
        this.$store.commit('setCurComponent',{ component: this.componentData[0], index: 0 })
      }
      this.$store.commit('setCheckBoxStatus', true)
    },
    checkDel() {
      console.log('deleteCheck')
      if(!this.componentData.length) {
        return
      }

      const componentData = deepCopy(this.componentData)
      let arr = []
      let arr2 = []
      componentData.map((item,index) => {
        if(!item.isCheck) {
          arr.push(item)
        } else {
          arr2.push(item)
        }
      })
      if(!arr2.length) { //未选择要删除的组件
        return
      }
      this.$confirm('此操作将删除勾选组件, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        console.log('arrrr',arr)
        this.$store.commit('setComponentData',arr)
        this.$store.commit('recordSnapshot')
        this.$store.commit('setCurComponent', { component: null, index: null })
        this.$store.commit('setCheckBoxStatus',false)
        // console.log('删除 后的',this.componentData)

        this.$message({
          type: 'success',
          message: '删除成功!'
        });
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消删除'
        });          
      });
    },
    checkBack() {
      const componentData = deepCopy(this.componentData)
      componentData.map(item => {
        item.isCheck  = false
      });
      this.isMove = true
      this.$store.commit('setComponentData',componentData)
      this.$store.commit('setCheckBoxStatus',false)
    },
    // 组件对齐
    positionChange(value) {
      console.log('position:::',value)
      
      const componentData = deepCopy(this.componentData)
      const arr = componentData.filter(item => item.isCheck&&!item.isLock) // 勾选中锁定状态的组件不支持对齐
      if (!arr.length) {
        return
      }
      console.log(arr)
      if(value === 'left') {
        if (arr.length === 1) {
          componentData.map(item => {
            if (item.isCheck &&!item.isLock) {
              item.style.left = 1
            }
          })
        } else {
          let lefts = arr.map(item => {return item.style.left}) // 组件left值
          let left = Math.min(...lefts)
          componentData.map(item => {
            if (item.isCheck &&!item.isLock) {
              item.style.left = left
            }
          })
        }

      } else if (value === 'right') {
        if (arr.length === 1) {
          componentData.map(item => {
            if (item.isCheck &&!item.isLock) {
              item.style.left = (this.canvasStyleData.width - item.style.width)
            }
          })
        } else {
          let rights = arr.map(item => {return (item.style.left + item.style.width)}) // 
          let right = Math.max(...rights)
          // console.log(right)
          componentData.map(item => {
            if (item.isCheck &&!item.isLock) {
              item.style.left = (right - item.style.width)
            }
          })
        }
        
      } else if (value === 'top') {
        if (arr.length === 1) {
          componentData.map(item => {
            if (item.isCheck &&!item.isLock) {
              item.style.top = 1
            }
          })
        } else {
          let tops = arr.map(item => {return item.style.top})
          let top = Math.min(...tops)
          componentData.map(item => {
            if (item.isCheck &&!item.isLock) {
              item.style.top = top
            }
          })
        }

      } else if (value === 'bottom') {
        if (arr.length === 1) {
          componentData.map(item => {
            if (item.isCheck &&!item.isLock) {
              item.style.top = (this.canvasStyleData.height - item.style.height)
            }
          })
        } else {
          let bottoms = arr.map(item => {return (item.style.top + item.style.height)})
          let bottom = Math.max(...bottoms)
          // console.log(bottom)
          componentData.map(item => {
            if (item.isCheck &&!item.isLock) {
              item.style.top = (bottom - item.style.height)
            }
          })
        }

      } else if (value === 'transverse') { // 横向
        if(arr.length === 1) {
          componentData.map(item => {
            if (item.isCheck && !item.isLock) {
              item.style.left = Math.floor((parseInt(this.canvasStyleData.width) - parseInt(item.style.width))/2)
            }
          })
        } else {
          let list = []
          arr.map(item => {
            list.push(item.style.left)
            list.push((item.style.left + item.style.width))
          });
          let min = Math.floor(Math.min(...list))
          let max = Math.floor(Math.max(...list))
          let pip = max -min
          arr.map(item => {
            pip = pip - parseInt(item.style.width)
          })
          let avg = Math.floor(pip/(arr.length - 1))
          console.log('数值：',min,max,pip,avg)
          arr.sort((a,b) => {return a.style.left - b.style.left})
          let leftList = [] // 横向分布组件的left
          for(let i=0;i<arr.length;i++) {
            if(i === 0) {
              arr[i].style.left = min
              leftList.push(min)
            } else {
              arr[i].style.left = Math.floor(arr[i-1].style.left + arr[i-1].style.width + avg)
              leftList.push(arr[i].style.left)
            }
          }
          // console.log('赋值后：；',arr,leftList)
          componentData.sort((a,b) => {return a.style.left - b.style.left}) // 排序
          let n = 0;
          componentData.map(item => {
            if(item.isCheck && !item.isLock) {
              item.style.left = leftList[n]
              n++
            }
          });
          console.log('横向分布',componentData)
        }

      } else if (value === 'longitudinal') { // 纵向
        if(arr.length === 1) {
          componentData.map(item => {
            if (item.isCheck && !item.isLock) {
              item.style.top = Math.floor((this.canvasStyleData.height - item.style.height)/2)
            }
          })
        } else {
          let list = []
          arr.map(item => {
            list.push(item.style.top)
            list.push((item.style.top + item.style.height))
          });
          // console.log(list)
          let min = Math.floor(Math.min(...list))
          let max = Math.floor(Math.max(...list))
          let pip = max -min
          arr.map(item => {
            pip = pip - parseInt(item.style.height)
          })
          let avg = Math.floor(pip/(arr.length - 1))
          console.log('数值：',min,max,pip,avg)
          arr.sort((a,b) => {return a.style.top - b.style.top})
          let topList = [] // 横向分布组件的top
          for(let i=0;i<arr.length;i++) {
            if(i === 0) {
              arr[i].style.top = min
              topList.push(min)
            } else {
              arr[i].style.top = parseInt(arr[i-1].style.top + arr[i-1].style.height + avg)
              topList.push(arr[i].style.top)
            }
          }
          // console.log('赋值后：；',topList)
          componentData.sort((a,b) => {return a.style.top - b.style.top}) // 排序
          let n = 0;
          componentData.map(item => {
            if(item.isCheck && !item.isLock) {
              item.style.top = topList[n]
              n++
            }
          });
          // console.log('纵向分布',componentData)
        }

      } else if (value === 'horizontally') { // 水平居中
        if(arr.length === 1) {
          componentData.map(item => {
            if(item.isCheck && !item.isLock) {
              item.style.left = Math.floor(parseInt(this.canvasStyleData.width - item.style.width)/2)
            }
          })
        }else {
          let obj = arr[arr.length -1] // 获取最后一个组件对象
          let levelCentral = obj.style.left + parseInt(obj.style.width/2)  // 获取组件水平的中轴线值
          console.log(levelCentral)
          componentData.map(item => {
            if(item.isCheck && !item.isLock) {
              if(item.style.left !== obj.style.left) {
                item.style.left = levelCentral - parseInt(item.style.width/2)
              }
            }
          })
        }
      } else if (value === 'vertically') { // 垂直居中
        if(arr.length === 1) {
          componentData.map(item => {
            if(item.isCheck && !item.isLock) {
              item.style.top = Math.floor(parseInt(this.canvasStyleData.height - item.style.height)/2)
            }
          })
        } else {
          let obj = arr[arr.length -1] // 获取最后一个组件对象
          let verticalCentral = obj.style.top + parseInt(obj.style.height/2) // 获取组件垂直的中轴线值
          console.log(verticalCentral)
          componentData.map(item => {
            if(item.isCheck && !item.isLock) {
              if(item.style.top != obj.style.top) {
                item.style.top = verticalCentral - parseInt(item.style.height/2)
              }
            }
          })
        }
      }
      this.$store.commit('setComponentData',componentData)
      this.$store.commit('recordSnapshot')
    },
    // 组件移动
    moveClick() {
      this.isMove = !this.isMove
      console.log(this.isMove)
    },
    // 组件移动改变
    moveChange(value) {
      console.log(value,this.moveSize)

      const componentData = deepCopy(this.componentData)
      let arr = componentData.filter(item => item.isCheck && !item.isLock)
      if (!arr.length) {
        return
      }
      if(value === 'left') {
        let list = arr.filter(item => parseInt(item.style.left) === 0)
        if(list.length) {
          return
        }
        arr.sort((a,b) => {return a.style.left - b.style.left}) // 由小到大排序
        // 获取到最左边组件的left 左移动 moveSize距离后的值
        let left = Math.floor(parseInt(arr[0].style.left) - this.moveSize) < 0 ? 0 : Math.floor(parseInt(arr[0].style.left) - this.moveSize)
        console.log('left',left)
        let spaceList = [] // 向左移动的组件间的间隔差
        for(let i=0;i<arr.length;i++) {
          if(i === 0) {
            spaceList.push(0)
          } else {
            let c = parseInt(arr[i].style.left) - parseInt(arr[i-1].style.left + arr[i-1].style.width)
            spaceList.push(c)
          }
        }
        // console.log('组件间隔差：',spaceList)
        let leftList = [] // 组件移动后left集合
        for(let i=0;i<arr.length;i++) {
          if(i === 0) {
            leftList.push((left))
          } else {
            leftList.push(Math.floor(leftList[i-1] + arr[i-1].style.width + spaceList[i]))
          }
        }
        console.log('left,list',leftList)
        componentData.sort((a,b) => {return a.style.left - b.style.left})
        let n = 0;
        componentData.map(item => {
          if(item.isCheck && !item.isLock) {
            item.style.left = leftList[n]
            n++
          }
        })
      } else if(value === 'right') {
        let list = arr.filter(item => parseInt(item.style.left + item.style.width) === this.canvasStyleData.width)
        if(list.length) {
          return
        }
        arr.sort((a,b) => {return b.style.left - a.style.left}) // 由大到小排序
        // 获取到最右边组件的left 右移动 moveSize距离后的值
        let right = Math.floor(arr[0].style.left + arr[0].style.width + this.moveSize) > this.canvasStyleData.width ? 
          (this.canvasStyleData.width - Math.floor(arr[0].style.width)) : Math.floor(arr[0].style.left + this.moveSize)
        console.log('right',right)
        let spaceList = [] // 向右移动的组件间的间隔差
        for(let i=0;i<arr.length;i++) {
          if(i === 0) {
            spaceList.push(0)
          } else {
            // arr 是有大到小排序
            let c = parseInt(arr[i-1].style.left) - parseInt(arr[i].style.left + arr[i].style.width)
            spaceList.push(c)
          }
        }
        // console.log('间隔',spaceList)
        let rightList = [] // 组件移动后left集合
        for(let i=0;i<arr.length;i++) {
          if(i === 0) {
            rightList.push((right))
          } else {
            rightList.push(Math.floor(rightList[i-1] - parseInt(arr[i].style.width) - spaceList[i]))
          }
        }
        console.log('right,list',rightList)
        componentData.sort((a,b) => {return b.style.left - a.style.left}) // 由大到小
        let n = 0;
        componentData.map(item => {
          if(item.isCheck && !item.isLock) {
            item.style.left = rightList[n]
            n++
          }
        })
      } else if(value === 'top') {
        let list = arr.filter(item => parseInt(item.style.top) === 0)
        if(list.length) {
          return
        }
        arr.sort((a,b) => {return a.style.top - b.style.top}) // 由小到大
        // 获取到最上边组件的top 上移动 moveSize距离后的值
        let top = Math.floor(parseInt(arr[0].style.top) - this.moveSize) < 0 ? 0 : Math.floor(parseInt(arr[0].style.top) - this.moveSize)
        console.log('top',top)
        let spaceList = [] // 向上移动的组件间的间隔差
        for(let i=0;i<arr.length;i++) {
          if(i === 0) {
            spaceList.push(0)
          } else {
            let c = parseInt(arr[i].style.top) - parseInt(arr[i-1].style.top + arr[i-1].style.height)
            spaceList.push(c)
          }
        }
        // console.log('组件间隔差：',spaceList)
        let topList = [] // 组件移动后top集合
        for(let i=0;i<arr.length;i++) {
          if(i === 0) {
            topList.push((top))
          } else {
            topList.push(Math.floor(topList[i-1] + arr[i-1].style.height + spaceList[i]))
          }
        }
        console.log('top,list',topList)
        componentData.sort((a,b) => {return a.style.top - b.style.top})
        let n = 0;
        componentData.map(item => {
          if(item.isCheck && !item.isLock) {
            item.style.top = topList[n]
            n++
          }
        })
      } else if(value === 'bottom') {
        let list = arr.filter(item => parseInt(item.style.top + item.style.height) === this.canvasStyleData.height)
        if(list.length) {
          return
        }
        arr.sort((a,b) => {return parseInt(b.style.top + b.style.height) - parseInt(a.style.top + a.style.height)}) // 由大到小排序
        // 获取到最下面边组件的top 下移动 moveSize距离后的值
        let bottom = Math.floor(arr[0].style.top + arr[0].style.height + this.moveSize) > this.canvasStyleData.height ? 
          (this.canvasStyleData.height - Math.floor(arr[0].style.height)) : Math.floor(arr[0].style.top + this.moveSize)
        console.log('bottom',bottom)
        let spaceList = [] // 向下移动的组件间的间隔差
        for(let i=0;i<arr.length;i++) {
          if(i === 0) {
            spaceList.push(0)
          } else {
            // arr 是有大到小排序
            let c = parseInt(arr[i-1].style.top) - parseInt(arr[i].style.top + arr[i].style.height)
            spaceList.push(c)
          }
        }
        console.log('间隔',spaceList)
        let bottomtList = [] // 组件移动后top集合
        for(let i=0;i<arr.length;i++) {
          if(i === 0) {
            bottomtList.push((bottom))
          } else {
            bottomtList.push(Math.floor(bottomtList[i-1] - parseInt(arr[i].style.height) - spaceList[i]))
          }
        }
        console.log('bottom,list',bottomtList)
        componentData.sort((a,b) => {return parseInt(b.style.top + b.style.height) - parseInt(a.style.top + a.style.height)}) // 由大到小
        let n = 0;
        componentData.map(item => {
          if(item.isCheck && !item.isLock) {
            item.style.top = bottomtList[n]
            n++
          }
        })
      }
      this.$store.commit('setComponentData',componentData)
      this.$store.commit('recordSnapshot')
    },
    changeAidedDesign() {
      this.$emit('changeAidedDesign')
    },
    closeNotSave() {
      this.close()
    },
    saveLinkage() {
      // 字段检查
      for (const key in this.targetLinkageInfo) {
        let subCheckCount = 0
        const linkageInfo = this.targetLinkageInfo[key]
        const linkageFields = linkageInfo['linkageFields']
        if (linkageFields) {
          linkageFields.forEach(function(linkage) {
            if (!(linkage.sourceField && linkage.targetField)) {
              subCheckCount++
            }
          })
        }

        if (subCheckCount > 0) {
          this.$message({
            message: this.$t('chart.datalist') + '【' + linkageInfo.targetViewName + '】' + this.$t('panel.exit_un_march_linkage_field'),
            type: 'error',
            showClose: true
          })
          return
        }
      }
      const request = {
        panelId: this.$store.state.panel.panelInfo.id,
        sourceViewId: this.curLinkageView.propValue.viewId,
        linkageInfo: this.targetLinkageInfo
      }
      saveLinkage(request).then(rsp => {
        // 刷新联动信息
        getPanelAllLinkageInfo(this.$store.state.panel.panelInfo.id).then(rsp => {
          this.$store.commit('setNowPanelTrackInfo', rsp.data)
        })
        this.cancelLinkageSettingStatus()
        // 刷新跳转信息
        queryPanelJumpInfo(this.$store.state.panel.panelInfo.id).then(rsp => {
          this.$store.commit('setNowPanelJumpInfo', rsp.data)
        })
      })
    },
    cancelMobileLayoutStatue(sourceComponentData) {
      this.$store.commit('setComponentData', sourceComponentData)
      this.$store.commit('setMobileLayoutStatus', false)
    },
    cancelLinkage() {
      this.cancelLinkageSettingStatus()
    },
    cancelLinkageSettingStatus() {
      this.$store.commit('clearLinkageSettingInfo')
    },
    auxiliaryMatrixChange() {
      this.canvasStyleData.auxiliaryMatrix = false
    },
    // 启用移动端布局
    openMobileLayout() {
      this.$store.commit('openMobileLayout')
    },
    editSave() {
      if (this.mobileLayoutStatus) {
        this.mobileLayoutSave()
      } else {
        this.saveLinkage()
      }
    },
    editReset() {
      this.cancelMobileLayoutStatue(JSON.parse(this.componentDataCache))
      this.$store.commit('openMobileLayout')
    },
    editCancel() {
      if (this.mobileLayoutStatus) {
        this.cancelMobileLayoutStatue(JSON.parse(this.componentDataCache))
      } else {
        this.cancelLinkageSettingStatus()
      }
    },
    // 移动端布局保存
    mobileLayoutSave() {
      this.$store.state.styleChangeTimes++
      const mobileDataObj = {}
      this.componentData.forEach(item => {
        mobileDataObj[item.id] = item
      })
      const sourceComponentData = JSON.parse(this.componentDataCache)
      this.$store.commit('setComponentDataCache', null)
      sourceComponentData.forEach(item => {
        if (mobileDataObj[item.id]) {
          mobile2MainCanvas(item, mobileDataObj[item.id])
        } else {
          item.mobileSelected = false
        }
      })
      this.cancelMobileLayoutStatue(sourceComponentData)
    }
  }
}
</script>

<style lang="scss" scoped>
  .toolbar {
    float: right;
    height: 35px;
    line-height: 35px;
    min-width: 400px;
    .canvas-config {
      display: inline-block;
      margin-left: 10px;
      font-size: 14px;

      input {
        width: 50px;
        margin-left: 10px;
        outline: none;
        padding: 0 5px;
        border: 1px solid #ddd;
      }

      span {
        margin-left: 10px;
      }
    }

    .insert {
      display: inline-block;
      line-height: 1;
      white-space: nowrap;
      cursor: pointer;
      background: #FFF;
      border: 1px solid #DCDFE6;
      color: var(--TextPrimary, #606266);
      -webkit-appearance: none;
      text-align: center;
      box-sizing: border-box;
      outline: 0;
      margin: 0;
      transition: .1s;
      font-weight: 500;
      padding: 9px 15px;
      font-size: 12px;
      border-radius: 3px;
      margin-left: 10px;

      &:active {
        color: #3a8ee6;
        border-color: #3a8ee6;
        outline: 0;
      }

      &:hover {
        background-color: #ecf5ff;
        color: #3a8ee6;
      }
    }
  }

  .button-show{
    background-color: #ebf2fe!important;
  }

  .button-closed{
    background-color: #ffffff!important;
  }

   >>>.el-switch__core{
     width:30px!important;
     height:15px;
   }
  /*设置圆*/
  >>>.el-switch__core::after{
    width:14px;
    height:14px;
    margin-top:-1px;
    margin-bottom: 2px;
  }

  .iconfont-tb {
    font-family: "iconfont" !important;
    font-size: 12px;
    font-style: normal;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
  }

</style>

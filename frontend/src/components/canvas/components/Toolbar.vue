<template>
  <div>
    <!--    linkageActiveStatus:{{ linkageActiveStatus }}-->
    <div v-if="linkageSettingStatus" class="toolbar">
      <span style="float: right;">
        <el-button size="mini" @click="saveLinkage">
          {{ $t('commons.confirm') }}
        </el-button>
        <el-button size="mini" @click="cancelLinkage">
          {{ $t('commons.cancel') }}
        </el-button>
      </span>
    </div>
    <div v-else class="toolbar">

      <div class="canvas-config" style="margin-right: 10px">
        <el-switch v-model="canvasStyleData.auxiliaryMatrix" :width="35" name="auxiliaryMatrix" />
        <span>{{ $t('panel.matrix_design') }}</span>
      </div>

      <div class="canvas-config" style="margin-right: 10px">
        <el-switch v-model="canvasStyleData.selfAdaption" :width="35" name="selfAdaption" />
        <span>{{ $t('panel.canvas_self_adaption') }} </span>
      </div>

      <div class="canvas-config" style="margin-right: 55px">
        <span> {{ $t('panel.canvas_size') }} </span>
        <input v-model="canvasStyleData.width" :disabled="canvasStyleData.selfAdaption">
        <span>*</span>
        <input v-model="canvasStyleData.height" :disabled="canvasStyleData.selfAdaption">
      </div>
      <!--      <div class="canvas-config" style="margin-right: 10px">-->
      <!--        <span> {{ $t('panel.canvas_scale') }} </span>-->
      <!--        <input v-model="scale" @input="handleScaleChange"> %-->
      <!--      </div>-->

      <el-tooltip :content="$t('panel.style')">
        <el-button :class="styleButtonActive?'button-show':'button-closed'" class="el-icon-magic-stick" size="mini" circle @click="showPanel" />
      </el-tooltip>

      <!--      <el-tooltip v-if="!aidedButtonActive" :content="$t('panel.open_aided_design') ">-->
      <!--        <el-button class="el-icon-help button-closed" size="mini" circle @click="changeAidedDesign" />-->
      <!--      </el-tooltip>-->

      <!--      <el-tooltip v-if="aidedButtonActive" :content="$t('panel.close_aided_design') ">-->
      <!--        <el-button class="el-icon-help button-show" size="mini" circle @click="changeAidedDesign" />-->
      <!--      </el-tooltip>-->

      <el-tooltip :content="$t('panel.undo') ">
        <el-button class="el-icon-refresh-right" size="mini" circle @click="undo" />
      </el-tooltip>
      <el-tooltip :content="$t('panel.redo') ">
        <el-button class="el-icon-refresh-left" size="mini" circle @click="redo" />
      </el-tooltip>
      <!--      <el-tooltip :content="$t('panel.insert_picture') ">-->
      <!--        <el-button class="el-icon-upload" size="mini" circle @click="goFile" />-->
      <!--      </el-tooltip>-->
      <el-tooltip :content="$t('panel.clean_canvas')">
        <el-button class="el-icon-document-delete" size="mini" circle @click="clearCanvas" />
      </el-tooltip>
      <!--      <input id="input" ref="files" type="file" hidden @change="handleFileChange">-->
      <el-tooltip :content="$t('panel.fullscreen_preview')">
        <el-button class="el-icon-view" size="mini" circle @click="clickPreview" />
      </el-tooltip>

      <span style="float: right;margin-left: 10px">
        <el-button size="mini" :disabled="changeTimes===0||snapshotIndex===lastSaveSnapshotIndex" @click="save(false)">
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
        <el-col :span="6">
          <svg-icon icon-class="warn-tre" style="width: 20px;height: 20px;float: right" />
        </el-col>
        <el-col :span="16">
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
import { deepCopy } from '@/components/canvas/utils/utils'
import { panelSave } from '@/api/panel/panel'
import bus from '@/utils/bus'
import {
  DEFAULT_COMMON_CANVAS_STYLE_STRING
} from '@/views/panel/panel'

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
      closePanelVisible: false
    }
  },
  computed: mapState([
    'componentData',
    'canvasStyleData',
    'areaData',
    'curComponent',
    'changeTimes',
    'snapshotIndex',
    'lastSaveSnapshotIndex',
    'linkageSettingStatus'
  ]),

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
    },
    closePanelEdit() {
      if (this.changeTimes === 0 || this.snapshotIndex === this.lastSaveSnapshotIndex) { // 已保存
        this.close()
      } else {
        this.closePanelVisible = true
      }
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
      this.$store.commit('recordSnapshot')
    },

    decompose() {
      this.$store.commit('decompose')
      this.$store.commit('recordSnapshot')
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

          this.$store.commit('recordSnapshot')
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
      // 保存到数据库
      const requestInfo = {
        id: this.$store.state.panel.panelInfo.id,
        panelStyle: JSON.stringify(this.canvasStyleData),
        panelData: JSON.stringify(this.componentData)
      }
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
    },
    clearCanvas() {
      this.$store.commit('setComponentData', [])
      this.$store.commit('setCanvasStyle', DEFAULT_COMMON_CANVAS_STYLE_STRING)
      this.$store.commit('recordSnapshot')
    },

    handlePreviewChange() {
      this.$store.commit('setEditMode', 'edit')
    },

    clickPreview() {
      this.$emit('previewFullScreen')
    },
    changeAidedDesign() {
      this.$emit('changeAidedDesign')
    },
    closeNotSave() {
      this.close()
    },
    saveLinkage() {
      this.cancelLinkageSettingStatus()
    },
    cancelLinkage() {
      this.cancelLinkageSettingStatus()
    },
    cancelLinkageSettingStatus() {
      this.$store.commit('setLinkageSettingStatus', false)
    }
  }
}
</script>

<style lang="scss" scoped>
  .toolbar {
    float: right;
    height: 35px;
    line-height: 35px;
    min-width: 900px;
    /*background: #fff;*/
    /*border-bottom: 1px solid #ddd;*/

    .canvas-config {
      display: inline-block;
      margin-left: 10px;
      font-size: 14px;
      color: #606266;

      input {
        width: 50px;
        margin-left: 10px;
        outline: none;
        padding: 0 5px;
        border: 1px solid #ddd;
        color: #606266;
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
      color: #606266;
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
     /*color:#409EFF;*/
   }
  /*设置圆*/
  >>>.el-switch__core::after{
    width:14px;
    height:14px;
    margin-top:-1px;
    margin-bottom: 2px;
  }

</style>

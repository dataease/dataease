<template>
  <div>
    <div class="toolbar">

      <div class="canvas-config">
        <span>画布大小</span>
        <input v-model="canvasStyleData.width">
        <span>*</span>
        <input v-model="canvasStyleData.height">
      </div>
      <div class="canvas-config" style="margin-right: 10px">
        <span>画布比例</span>
        <input v-model="scale" @input="handleScaleChange"> %
      </div>
      <el-tooltip content="撤消">
        <el-button class="el-icon-refresh-right" size="mini" circle @click="undo" />
      </el-tooltip>
      <el-tooltip content="重做">
        <el-button class="el-icon-refresh-left" size="mini" circle @click="redo" />
      </el-tooltip>
      <el-tooltip content="插入图片">
        <el-button class="el-icon-upload" size="mini" circle @click="goFile" />
      </el-tooltip>
      <el-tooltip content="清空画布" style="margin-right: 10px">
        <el-button class="el-icon-document-delete" size="mini" circle @click="clearCanvas" />
      </el-tooltip>
      <input id="input" ref="files" type="file" hidden @change="handleFileChange">
      <el-tooltip content="保存">
        <el-button class="el-icon-circle-check" size="mini" circle @click="save" />
      </el-tooltip>
      <el-tooltip content="预览">
        <el-button class="el-icon-view" size="mini" circle @click="clickPreview" />
      </el-tooltip>

      <span style="float: right;margin-left: 10px">
        <el-button size="mini" @click="closePanelEdit">
          关闭
        </el-button>
      </span>
    </div>

    <!-- 预览 -->
    <!--    <PreviewEject v-model="isShowPreview" @change="handlePreviewChange" />-->
  </div>
</template>

<script>
import generateID from '@/components/canvas/utils/generateID'
import toast from '@/components/canvas/utils/toast'
import PreviewEject from '@/components/canvas/components/Editor/PreviewEject'
import { mapState } from 'vuex'
import { commonStyle, commonAttr } from '@/components/canvas/custom-component/component-list'
import eventBus from '@/components/canvas/utils/eventBus'
import { deepCopy } from '@/components/canvas/utils/utils'
import { post } from '@/api/panel/panel'
import bus from '@/utils/bus'

export default {
  components: { PreviewEject },
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
      timer: null
    }
  },
  computed: mapState([
    'componentData',
    'canvasStyleData',
    'areaData',
    'curComponent'
  ]),
  created() {
    eventBus.$on('preview', this.preview)
    eventBus.$on('save', this.save)
    eventBus.$on('clearCanvas', this.clearCanvas)

    this.scale = this.canvasStyleData.scale
  },
  methods: {
    closePanelEdit() {
      debugger
      bus.$emit('PanelSwitchComponent', { name: 'PanelMain' })
    },
    goFile() {
      this.$refs.files.click()
    },
    format(value) {
      const scale = this.scale
      return value * parseInt(scale) / 100
    },

    getOriginStyle(value) {
      const scale = this.canvasStyleData.scale
      const result = value / (parseInt(scale) / 100)
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
        eventBus.$emit('resizing', '')
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
                width: img.width,
                height: img.height
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

    save() {
      localStorage.setItem('canvasData', JSON.stringify(this.componentData))
      localStorage.setItem('canvasStyle', JSON.stringify(this.canvasStyleData))
      // 保存到数据库
      const requestInfo = {
        id: this.$store.state.panel.panelInfo.id,
        panelStyle: JSON.stringify(this.canvasStyleData),
        panelData: JSON.stringify(this.componentData)
      }
      post('panel/group/save', requestInfo, () => {})
      this.$message.success('保存成功')
    },
    clearCanvas() {
      this.$store.commit('setComponentData', [])
      this.$store.commit('recordSnapshot')
    },

    handlePreviewChange() {
      this.$store.commit('setEditMode', 'edit')
    },

    clickPreview() {
      localStorage.setItem('canvasData', JSON.stringify(this.componentData))
      localStorage.setItem('canvasStyle', JSON.stringify(this.canvasStyleData))
      const url = '#/preview'
      window.open(url, '_blank')
    }
  }
}
</script>

<style lang="scss" scoped>
  .toolbar {
    float: right;
    height: 35px;
    line-height: 35px;
    background: #fff;
    border-bottom: 1px solid #ddd;

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
</style>

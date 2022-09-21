<template>
  <el-row v-loading="loading">
    <el-row v-if="editPanel.optType==='new' && editPanel.panelInfo.nodeType==='panel'">
      <el-col :span="18" style="height: 40px">
        <el-radio v-model="inputType" label="new"> {{ $t('panel.custom') }}</el-radio>
        <el-radio v-model="inputType" label="new_outer_template">{{ $t('panel.import_template') }}  </el-radio>
        <el-radio v-model="inputType" label="new_inner_template" @click.native="getTree">{{ $t('panel.copy_template') }}  </el-radio>
      </el-col>
      <el-col v-if="inputType==='new_outer_template'" :span="6">
        <el-button class="el-icon-upload" size="small" type="primary" @click="goFile">{{ $t('panel.upload_template') }}</el-button>
        <input id="input" ref="files" type="file" accept=".DET" hidden @change="handleFileChange">
      </el-col>
    </el-row>
    <el-row style="margin-top: 5px">
      <el-col :span="4">{{ editPanel.titleSuf }}{{ $t('commons.name') }}</el-col>
      <el-col :span="20">
        <el-input v-model="editPanel.panelInfo.name" clearable size="mini" />
      </el-col>
    </el-row>
    <el-row v-if="editPanel.optType==='new' && editPanel.panelInfo.nodeType==='panel' && inputType === 'new'" style="margin-top: 5px;">
      <el-form ref="form" class="panel_form" :model="panelObj" label-width="120px" label-position="left">
        <el-col :span="12">
          <el-form-item :label="$t('chart.canvasWith')">
            <el-input-number size="small" v-model="panelObj.canvasWidth" @change="handleChange" :min="1" :max="10000"></el-input-number>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item :label="$t('chart.canvasHeight')">
            <el-input-number size="small" v-model="panelObj.canvasHeight" :min="1" :max="10000" @change="handleChange" />
          </el-form-item>
        </el-col>
        <el-col>
          <el-form-item :label="$t('chart.text_style')">
            <el-select v-model="panelObj.panel.fontFamily" size="small" placeholder="请选择" @change="handleChange">
              <el-option
                v-for="item in fontOptions"
                :key="item"
                :label="item"
                :value="item"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col>
          <el-form-item :label="$t('chart.background')">
            <el-radio-group v-model="panelObj.panel.backgroundType" @change="handleChange" style="width: 100%">
              <el-col :span="12">
                <el-radio label="color" >{{ $t('chart.color') }}</el-radio>
              </el-col>
              <el-col :span="12">
                <el-radio label="image" >{{ $t('panel.photo') }}</el-radio>
              </el-col>
            </el-radio-group> 
            <el-row style="margin-top:5px;">
              <el-col :span="12">
                <el-color-picker v-model="panelObj.panel.color" :predefine="predefineColors" size="mini" style="cursor: pointer;z-index: 1004;" @change="handleChange" />
              </el-col>
              <el-col :span="12">
                <el-upload
                  action=""
                  accept=".jpeg,.jpg,.png,.gif"
                  class="avatar-uploader"
                  list-type="picture-card"
                  :class="{disabled:uploadDisabled}"
                  :on-preview="handlePictureCardPreview"
                  :on-remove="handleRemove"
                  :before-upload="beforeAvatarUpload"
                  :http-request="upload"
                  :file-list="fileList"
                  :on-change="onChange"
                >
                  <i class="el-icon-plus" />
                </el-upload>
                <el-dialog top="25vh" width="600px" :modal-append-to-body="false" :visible.sync="dialogVisible">
                  <img width="100%" :src="dialogImageUrl" alt="">
                </el-dialog>
                <span>
                  <i class="el-icon-warning" /> <span>上传的文件大小不能超过10MB!</span>
                </span>
              </el-col>
            </el-row>
          </el-form-item>
        </el-col>
        <el-col>
          <el-form-item :label="$t('panel.component_gap')">
            <el-radio-group v-model="panelObj.panel.gap" @change="handleChange">
              <el-radio label="yes">{{ $t('panel.gap') }}</el-radio>
              <el-radio label="no">{{ $t('panel.no_gap') }}</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col>
          <el-form-item label="视图加载提示">
            <el-checkbox v-model="panelObj.refreshViewLoading" @change="handleChange"></el-checkbox>
          </el-form-item>
        </el-col>
        <el-col>
          <el-form-item label="刷新时间单位">
            <el-radio-group v-model="panelObj.refreshUnit" @change="handleChange">
              <el-radio label="second">秒</el-radio>
              <el-radio label="minute">分</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col>
          <el-form-item label="刷新时间频率">
            <el-slider
              v-model="panelObj.refreshTime"
              show-input
              :show-input-controls="false"
              input-size="mini"
              :min="1"
              :max="3600"
              @change="handleChange"
            />
          </el-form-item>
        </el-col>
        <el-col>
          <el-form-item :label="$t('panel.panel_view_result_show')">
            <el-radio-group v-model="panelObj.panel.resultMode" @change="handleChange" style="width: 100%;">
              <el-radio label="all">{{ $t('panel.view') }}</el-radio>
              <el-radio label="custom">{{ $t('panel.panel') }}</el-radio>
            </el-radio-group>
            <el-slider
              v-model="panelObj.panel.resultCount"
              :disabled="panelObj.panel.resultMode==='all'"
              style="margin-left: 5px"
              show-input
              :show-input-controls="false"
              :show-tooltip="false"
              input-size="mini"
              :min="1"
              :max="10000"
              @change="handleChange"
            />
            <el-row>
              <span style="color: #909399; font-size: 8px;margin-left: 3px">
                Tips: {{ $t('panel.panel_view_result_tips') }}
              </span>
            </el-row>
          </el-form-item>
        </el-col>
      </el-form>
    </el-row>
    <el-row v-if="inputType==='new_inner_template'" class="preview">
      <el-col :span="8" style="height:100%;overflow-y: auto">
        <template-all-list :template-list="templateList" @showCurrentTemplateInfo="showCurrentTemplateInfo" />
      </el-col>
      <el-col :span="16" :style="classBackground" class="preview-show" />
    </el-row>
        <el-row v-if="inputType==='new_outer_template'" class="preview" :style="classBackground" />
    <el-row class="root-class">
      <el-button size="mini" @click="cancel()">{{ $t('commons.cancel') }}</el-button>
      <el-button type="primary" size="mini" @click="save()">{{ $t('commons.confirm') }}</el-button>
    </el-row>
  </el-row>
</template>

<script>
import { panelSave } from '@/api/panel/panel'
import { showTemplateList } from '@/api/system/template'
import TemplateAllList from './TemplateAllList'
import { deepCopy } from '@/components/canvas/utils/utils'

import { COLOR_PANEL } from '@/views/chart/chart/chart'

export default {
  components: { 
    TemplateAllList,
   },
  props: {
    editPanelOut: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      loading: false,
      inputType: 'new',
      fieldName: 'name',
      tableRadio: null,
      keyWordSearch: '',
      columnLabel: this.$t('panel.belong_to_category'),
      templateList: [],
      importTemplateInfo: {
        snapshot: ''
      },
      editPanel: null,

      panelObj: {
        canvasWidth: 1600,
        canvasHeight: 900,
        panel: {
          fontFamily: '',
          backgroundType: 'image',
          color: '#ffffff',
          imageUrl: null,
          gap: 'yes',
          resultMode: 'all',
          resultCount: 1000,
        },
        refreshViewLoading: true,
        refreshUnit: 'minute',
        refreshTime: 5,
      },
      fontOptions: ['宋体', '楷体', '黑体', '仿宋', '新宋体'],
      predefineColors: COLOR_PANEL,
      fileList: [],
      dialogImageUrl: '',
      dialogVisible: false,
      uploadDisabled: false,
    }
  },
  computed: {
    classBackground() {
      if (this.importTemplateInfo.snapshot) {
        return {
          background: `url(${this.importTemplateInfo.snapshot}) no-repeat`
        }
      } else {
        return {}
      }
    }
  },
  watch: {
    inputType(newVal) {
      if (newVal === 'new') {
        this.editPanel = deepCopy(this.editPanelOut)
      } else {
        this.editPanel.panelInfo.name = null
        this.editPanel.panelInfo.panelStyle = null
        this.editPanel.panelInfo.panelData = null
        this.importTemplateInfo.snapshot = null
        this.editPanel.panelInfo.templateId = null
      }
    }
  },
  created() {
    this.editPanel = deepCopy(this.editPanelOut)
    // this.getTree()
  },
  mounted() {
    this.bindKey()
  },
  destroyed() {
    this.unBindKey()
  },
  methods: {
    commitStyle() {
      // const canvasStyleData = deepCopy(this.canvasStyleData)
      // canvasStyleData.panel = this.panel
      // this.$store.commit('setCanvasStyle', canvasStyleData)
      this.$store.commit('setPanelStyleData',this.panelObj)
      this.$store.commit('setPanelStatus',true)
      this.$store.commit('recordSnapshot', 'commitStyle')
    },
    handleChange() {
      console.log('数据',this.panelObj)
      this.commitStyle()
    },
    handlePictureCardPreview(file) {
      this.dialogImageUrl = file.url
      this.dialogVisible = true
    },
    handleRemove(file, fileList) {
      this.uploadDisabled = false
      this.panelObj.panel.imageUrl = null
      this.fileList = []
      this.commitStyle()
    },
    beforeAvatarUpload(file) {
      console.log('file.size', file.size)
      const isLt10M = file.size / 1024 / 1024 < 10
      if (!isLt10M) {
        // this.$message.error('上传的文件大小不能超过 1MB!')
        return false
      }
      return isLt10M
    },
    onChange(file, fileList) {
      console.log('file-----', file, file.size / 1024 / 1024)
      if (file.size / 1024 / 1024 > 10) {
        this.$message.error('上传的文件大小不能超过 10MB!')
        this.fileList = []
        return
      }

      var _this = this
      _this.uploadDisabled = true
      const reader = new FileReader()
      reader.onload = function() {
        _this.panelObj.panel.imageUrl = reader.result
        _this.commitStyle()
      }
      this.$store.state.styleChangeTimes++
      reader.readAsDataURL(file.raw)
    },

    upload(file) {
      // console.log('this is upload')
    },

    entryKey(event) {
      const keyCode = event.keyCode
      if (keyCode === 13) {
        this.save()
      }
    },
    bindKey() {
      document.addEventListener('keypress', this.entryKey)
    },
    unBindKey() {
      document.removeEventListener('keypress', this.entryKey)
    },
    showCurrentTemplateInfo(data) {
      this.editPanel.panelInfo.templateId = data.id
      this.editPanel.panelInfo.name = data.name
      // this.editPanel.panelInfo.panelStyle = data.templateStyle
      // this.editPanel.panelInfo.panelData = data.templateData
      this.importTemplateInfo.snapshot = data.snapshot
    },
    getTree() {
      const request = {
        level: '-1',
        withChildren: true
      }
      this.loading = true
      showTemplateList(request).then(res => {
        this.templateList = res.data
        this.loading = false
      })
    },
    handleExceed(file) {
    },
    cancel() {
      this.$emit('closeEditPanelDialog')
    },
    save() {
      if (!this.editPanel.panelInfo.name) {
        this.$warning(this.$t('chart.name_can_not_empty'))
        return false
      }

      if (this.editPanel.panelInfo.name.length > 50) {
        this.$warning(this.$t('commons.char_can_not_more_50'))
        return false
      }

      if (!this.editPanel.panelInfo.templateId && this.editPanel.optType === 'new' && this.inputType === 'new_inner_template') {
        this.$warning(this.$t('chart.template_can_not_empty'))
        return false
      }
      this.editPanel.panelInfo['newFrom'] = this.inputType
      this.loading = true
      panelSave(this.editPanel.panelInfo).then(response => {
        this.$message({
          message: this.$t('commons.save_success'),
          type: 'success',
          showClose: true
        })
        this.loading = false
        this.$emit('closeEditPanelDialog', response.data)
      }).catch(() => {
        this.loading = false
      })
    },
    handleFileChange(e) {
      const file = e.target.files[0]
      const reader = new FileReader()
      reader.onload = (res) => {
        const result = res.target.result
        this.importTemplateInfo = JSON.parse(result)
        this.editPanel.panelInfo.name = this.importTemplateInfo.name
        this.editPanel.panelInfo.panelStyle = this.importTemplateInfo.panelStyle
        this.editPanel.panelInfo.panelData = this.importTemplateInfo.panelData
        this.editPanel.panelInfo.dynamicData = this.importTemplateInfo.dynamicData
      }
      reader.readAsText(file)
    },
    goFile() {
      this.$refs.files.click()
    }

  }
}
</script>

<style scoped>
.canvas_row {
  margin-bottom: 5px;
}
.panel_form>>> .el-form-item {
  margin-bottom: 0px;
}

.avatar-uploader>>>.el-upload {
  width: 100px;
  height: 60px;
  line-height: 70px;
}
.avatar-uploader>>>.el-upload-list li{
  width: 100px !important;
  height: 60px !important;
}

.my_table >>> .el-table__row>td{
  /* 去除表格线 */
  border: none;
  padding: 0 0;
}
.my_table >>> .el-table th.is-leaf {
  /* 去除上边框 */
    border: none;
}
.my_table >>> .el-table::before{
  /* 去除下边框 */
  height: 0;
}

  .root-class {
    margin: 15px 0px 5px;
    text-align: center;
  }
  .preview {
    margin-top: 5px;
    border:1px solid #E6E6E6;
    height:250px !important;
    overflow:hidden;
    background-size: 100% 100% !important;
  }
  .preview-show {
    border-left:1px solid #E6E6E6;
    height:250px;
    background-size: 100% 100% !important;
  }
</style>

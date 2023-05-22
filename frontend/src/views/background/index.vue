<template>
  <el-row>
    <el-row>
      <el-col :span="24">
        <span style="font-weight:600;margin-right: 20px;font-size: 14px">{{ $t('panel.component_style') }}</span>
      </el-col>
    </el-row>
    <el-row
      class="main-content"
      disabled="!curComponent.commonBackground.enable"
    >

      <el-row style="height: 50px;overflow: hidden">
        <el-col :span="3">
          <span class="params-title">{{ $t('panel.inner_padding') }}</span>
        </el-col>
        <el-col :span="15">
          <el-slider
            v-model="curComponent.commonBackground.innerPadding"
            show-input
            :show-input-controls="false"
            input-size="mini"
            :max="50"
          />
        </el-col>
      </el-row>
      <el-row style="height: 50px;overflow: hidden">
        <el-col :span="3">
          <span class="params-title">{{ $t('panel.board_radio') }}</span>
        </el-col>
        <el-col :span="15">
          <el-slider
            v-model="curComponent.commonBackground.borderRadius"
            show-input
            :show-input-controls="false"
            input-size="mini"
          />
        </el-col>
      </el-row>

      <el-row style="height: 40px;overflow: hidden;">
        <el-col
          :span="3"
          style="padding-left: 10px;padding-top: 5px"
        >
          <el-checkbox v-model="curComponent.commonBackground.backgroundColorSelect">{{ $t('chart.color') }}</el-checkbox>
        </el-col>
        <el-col
          :span="1"
          style="padding-top: 5px"
        >
          <el-color-picker
            ref="colorPicker"
            @change="colorChange"
            v-model="curComponent.commonBackground.color"
            :disabled="!curComponent.commonBackground.backgroundColorSelect"
            size="mini"
            class="color-picker-style"
            :predefine="predefineColors"
          />
        </el-col>
        <el-col :span="3">
          <span class="params-title-small">{{ $t('chart.not_alpha') }}</span>
        </el-col>
        <el-col :span="11">
          <el-slider
            v-model="curComponent.commonBackground.alpha"
            :disabled="!curComponent.commonBackground.backgroundColorSelect"
            show-input
            :show-input-controls="false"
            input-size="mini"
          />
        </el-col>
      </el-row>

      <el-row style="height: 50px">
        <el-col
          :span="3"
          style="padding-left: 10px;padding-top: 5px"
        >
          <el-checkbox v-model="curComponent.commonBackground.enable">{{ $t('panel.background') }}</el-checkbox>
        </el-col>
        <el-col :span="21">
          <span style="color: #909399; font-size: 8px;margin-left: 3px;line-height: 30px">
            Tips:{{ $t('panel.choose_background_tips') }}
          </span>
        </el-col>
      </el-row>
      <el-row
        v-if="curComponent.commonBackground.enable"
        style="padding-left: 10px"
      >
        <el-row style="height: 80px;margin-top:0px;margin-bottom:20px;overflow: hidden">
          <el-col
            :span="4"
            style="padding-left: 10px"
          >
            <el-radio
              v-model="curComponent.commonBackground.backgroundType"
              label="outerImage"
              @change="onChangeType"
            >{{ $t('panel.photo') }}
            </el-radio>
          </el-col>
          <el-col style="width: 130px!important;">
            <el-upload
              action=""
              accept=".jpeg,.jpg,.png,.gif,.svg"
              class="avatar-uploader"
              list-type="picture-card"
              :class="{disabled:uploadDisabled}"
              :on-preview="handlePictureCardPreview"
              :on-remove="handleRemove"
              :http-request="upload"
              :file-list="fileList"
            >
              <i class="el-icon-plus" />
            </el-upload>
            <el-dialog
              top="25vh"
              width="600px"
              :append-to-body="true"
              :destroy-on-close="true"
              :visible.sync="dialogVisible"
            >
              <img
                width="100%"
                :src="dialogImageUrl"
              >
            </el-dialog>
          </el-col>
        </el-row>
        <el-row>
          <el-col
            :span="4"
            style="padding-left: 10px;position: relative"
          >
            <el-radio
              v-model="curComponent.commonBackground.backgroundType"
              label="innerImage"
              @change="onChangeType"
            >{{ $t('panel.board') }}
            </el-radio>
            <el-color-picker
              v-model="curComponent.commonBackground.innerImageColor"
              :title="'边框配色'"
              style="position: absolute;right:15px;top: -3px"
              size="mini"
              class="color-picker-style"
              :predefine="predefineColors"
            />
          </el-col>
          <el-col
            :span="20"
            :style="customStyle"
            class="main-row"
          >
            <el-row
              v-for="(value, key) in BackgroundShowMap"
              :key="key"
            >

              <el-col
                v-for="item in value"
                :key="item.id"
                :span="6"
              >
                <background-item
                  :template="item"
                />
              </el-col>
            </el-row>
          </el-col>
        </el-row>
      </el-row>
      <el-row
        v-if="isFilterComponent"
        style="height: 40px;overflow: hidden;"
      >
        <el-col
          :span="5"
          style="padding-left: 10px;padding-top: 8px"
        >
          {{ $t('panel.input_style') }}:
        </el-col>
        <el-col
          :span="2"
          style="padding-left: 10px;padding-top: 8px"
        >
          {{ $t('panel.board') }}
        </el-col>
        <el-col
          :span="3"
          style="padding-top: 5px"
        >
          <el-color-picker
            v-model="curComponent.style.brColor"
            size="mini"
            class="color-picker-style"
            :predefine="predefineColors"
          />
        </el-col>
        <el-col
          :span="2"
          style="padding-left: 10px;padding-top: 8px"
        >
          {{ $t('panel.text') }}
        </el-col>
        <el-col
          :span="3"
          style="padding-top: 5px"
        >
          <el-color-picker
            v-model="curComponent.style.wordColor"
            size="mini"
            class="color-picker-style"
            :predefine="predefineColors"
          />
        </el-col>
        <el-col
          :span="2"
          style="padding-left: 10px;padding-top: 8px"
        >
          {{ $t('panel.background') }}
        </el-col>
        <el-col
          :span="3"
          style="padding-top: 5px"
        >
          <el-color-picker
            v-model="curComponent.style.innerBgColor"
            size="mini"
            class="color-picker-style"
            :predefine="predefineColors"
          />
        </el-col>
      </el-row>

    </el-row>
    <el-row class="root-class">
      <el-col :span="24">
        <el-button
          size="mini"
          @click="cancel()"
        >{{ $t('commons.cancel') }}
        </el-button>
        <el-button
          type="primary"
          size="mini"
          @click="save()"
        >{{ $t('commons.confirm') }}
        </el-button>
      </el-col>
    </el-row>
  </el-row>
</template>

<script>
import { queryBackground } from '@/api/background/background'
import BackgroundItem from '@/views/background/BackgroundItem'
import { mapState } from 'vuex'
import { deepCopy, imgUrlTrans } from '@/components/canvas/utils/utils'
import { COLOR_PANEL } from '@/views/chart/chart/chart'
import { uploadFileResult } from '@/api/staticResource/staticResource'
import { COMMON_BACKGROUND_NONE } from '@/components/canvas/customComponent/component-list'

export default {
  name: 'Background',
  components: { BackgroundItem },
  data() {
    return {
      BackgroundShowMap: {},
      checked: false,
      backgroundOrigin: {},
      fileList: [],
      dialogImageUrl: '',
      dialogVisible: false,
      uploadDisabled: false,
      panel: null,
      predefineColors: COLOR_PANEL
    }
  },
  computed: {
    customStyle() {
      let style = {}
      if (this.canvasStyleData.openCommonStyle) {
        if (this.canvasStyleData.panel.backgroundType === 'image' && this.canvasStyleData.panel.imageUrl) {
          style = {
            background: `url(${imgUrlTrans(this.canvasStyleData.panel.imageUrl)}) no-repeat`,
            ...style
          }
        } else if (this.canvasStyleData.panel.backgroundType === 'color') {
          style = {
            background: this.canvasStyleData.panel.color,
            ...style
          }
        }
      }
      if (!style.background) {
        style.background = '#FFFFFF'
      }
      return style
    },
    ...mapState([
      'curComponent',
      'canvasStyleData',
      'componentData'
    ]),
    isFilterComponent() {
      return ['de-select', 'de-select-grid', 'de-date', 'de-input-search', 'de-number-range', 'de-select-tree'].includes(this.curComponent.component)
    }
  },
  created() {
    this.init()
  },
  methods: {
    colorChange(val) {
      if (val === null) {
        this.$refs.colorPicker.color.value = ''
        this.curComponent.commonBackground.color = ''
      }
    },
    init() {
      if (this.curComponent && this.curComponent.commonBackground && this.curComponent.commonBackground.outerImage && typeof (this.curComponent.commonBackground.outerImage) === 'string') {
        this.fileList.push({ url: imgUrlTrans(this.curComponent.commonBackground.outerImage) })
      }
      this.backgroundOrigin = deepCopy(this.curComponent.commonBackground ? this.curComponent.commonBackground : COMMON_BACKGROUND_NONE)
      this.queryBackground()
    },
    queryBackground() {
      queryBackground().then(response => {
        this.BackgroundShowMap = response.data
      })
    },
    cancel() {
      this.curComponent.commonBackground.enable = this.backgroundOrigin.enable
      this.curComponent.commonBackground.backgroundType = this.backgroundOrigin.backgroundType
      this.curComponent.commonBackground.color = this.backgroundOrigin.color
      this.curComponent.commonBackground.innerImage = this.backgroundOrigin.innerImage
      this.curComponent.commonBackground.outerImage = this.backgroundOrigin.outerImage
      this.curComponent.commonBackground.alpha = this.backgroundOrigin.alpha
      this.curComponent.commonBackground.borderRadius = this.backgroundOrigin.borderRadius
      this.curComponent.commonBackground.innerPadding = this.backgroundOrigin.innerPadding
      this.$emit('backgroundSetClose')
    },
    save() {
      this.$store.commit('recordSnapshot')
      this.$emit('backgroundSetClose')
    },
    commitStyle() {
      this.$store.commit('recordSnapshot')
    },
    onChangeType() {
      this.commitStyle()
    },
    handleRemove(file, fileList) {
      this.uploadDisabled = false
      this.curComponent.commonBackground.outerImage = null
      this.fileList = []
      this.commitStyle()
    },
    handlePictureCardPreview(file) {
      this.dialogImageUrl = file.url
      this.dialogVisible = true
    },
    upload(file) {
      const _this = this
      uploadFileResult(file.file, (fileUrl) => {
        _this.curComponent.commonBackground.outerImage = fileUrl
      })
    }

  }
}
</script>

<style scoped>
.el-card-template {
  min-width: 260px;
  min-width: 460px;
  width: 100%;
  height: 100%;
}

.main-row {
  background-size: 100% 100% !important;
  padding-left: 10px;
  height: 250px;
  overflow-y: auto;
}

.root-class {
  margin: 15px 0px 5px;
  text-align: center;
}

.avatar-uploader ::v-deep .el-upload {
  width: 120px;
  height: 80px;
  line-height: 90px;
}

.avatar-uploader ::v-deep .el-upload-list li {
  width: 120px !important;
  height: 80px !important;
}

.disabled ::v-deep .el-upload--picture-card {
  display: none;
}

.shape-item {
  padding: 6px;
  border: none;
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-item-slider ::v-deep .el-form-item__label {
  font-size: 12px;
  line-height: 38px;
}

.form-item ::v-deep .el-form-item__label {
  font-size: 12px;
}

.el-select-dropdown__item {
  padding: 0 20px;
}

span {
  font-size: 12px
}

.el-form-item {
  margin-bottom: 6px;
}

.main-content {
  border: 1px solid #E6E6E6;
}

.params-title {
  font-weight: bold;
  line-height: 40px;
  margin-left: 10px;
  font-size: 14px;
}

.params-title-small {
  font-weight: bold;
  line-height: 40px;
  margin-left: 10px;
  font-size: 12px;
}
</style>

<template>
  <el-row>
    <el-row>
      <el-row style="height: 50px;overflow: hidden">
        <el-col :span="8">
          <span class="params-title">{{ $t('panel.inner_padding') }}</span>
        </el-col>
        <el-col :span="16">
          <el-slider
            v-model="commonBackground.innerPadding"
            show-input
            :show-input-controls="false"
            input-size="mini"
            :max="50"
            @change="themeChange('innerPadding')"
          />
        </el-col>
      </el-row>
      <el-row style="height: 50px;overflow: hidden">
        <el-col :span="8">
          <span class="params-title">{{ $t('panel.board_radio') }}</span>
        </el-col>
        <el-col :span="16">
          <el-slider
            v-model="commonBackground.borderRadius"
            show-input
            :show-input-controls="false"
            input-size="mini"
            @change="themeChange('borderRadius')"
          />
        </el-col>
      </el-row>

      <el-row style="height: 40px;overflow: hidden;">
        <el-col
          :span="6"
          style="padding-top: 5px"
        >
          <el-checkbox
            v-model="commonBackground.backgroundColorSelect"
            @change="themeChange('backgroundColorSelect')"
          >
            {{
              $t('chart.color')
            }}
          </el-checkbox>
        </el-col>
        <el-col
          :span="3"
          style="padding-top: 5px"
        >
          <el-color-picker
            v-model="commonBackground.color"
            :disabled="!commonBackground.backgroundColorSelect"
            size="mini"
            class="color-picker-style"
            :predefine="predefineColors"
            @change="themeChange('color')"
          />
        </el-col>
        <el-col :span="5">
          <span class="params-title-small">{{ $t('chart.not_alpha') }}</span>
        </el-col>
        <el-col :span="10">
          <el-slider
            v-model="commonBackground.alpha"
            :disabled="!commonBackground.backgroundColorSelect"
            show-input
            :show-input-controls="false"
            input-size="mini"
            @change="themeChange('alpha')"
          />
        </el-col>
      </el-row>

      <el-row style="height: 50px">
        <el-col
          :span="4"
          style="padding-top: 5px"
        >
          <el-checkbox
            v-model="commonBackground.enable"
            @change="themeChange('enable')"
          >{{
            $t('panel.background')
          }}
          </el-checkbox>
        </el-col>
        <el-col :span="20">
          <span style="color: #909399; font-size: 8px;margin-left:10px;line-height: 40px">
            Tips:{{ $t('panel.choose_background_tips') }}
          </span>
        </el-col>
      </el-row>
      <el-row
        v-if="commonBackground.enable"
        style="padding-left: 10px"
      >
        <el-row style="height: 80px;margin-top:0px;margin-bottom:20px;overflow: hidden">
          <el-col
            :span="5"
          >
            <el-radio
              v-model="commonBackground.backgroundType"
              label="outerImage"
              @change="themeChange('backgroundType')"
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
          <el-col style="position: relative">
            <el-radio
              v-model="commonBackground.backgroundType"
              label="innerImage"
              @change="themeChange('backgroundType')"
            >{{ $t('panel.board') }}
            </el-radio>
            <el-color-picker
              v-model="commonBackground.innerImageColor"
              :title="$t('panel.border_color_setting')"
              style="position: absolute;left:60px;top: -3px"
              size="mini"
              class="color-picker-style"
              :predefine="predefineColors"
              @change="themeChange('innerImageColor')"
            />
          </el-col>
        </el-row>
        <el-row>
          <el-col
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
                :span="12"
              >
                <background-item-overall
                  :common-background="commonBackground"
                  :template="item"
                  @borderChange="themeChange('innerImage')"
                />
              </el-col>
            </el-row>
          </el-col>
        </el-row>
      </el-row>
    </el-row>
  </el-row>
</template>

<script>
import { queryBackground } from '@/api/background/background'
import BackgroundItem from '@/views/background/BackgroundItem'
import { mapState } from 'vuex'
import { imgUrlTrans } from '@/components/canvas/utils/utils'
import { COLOR_PANEL } from '@/views/chart/chart/chart'
import { uploadFileResult } from '@/api/staticResource/staticResource'
import bus from '@/utils/bus'
import BackgroundItemOverall from '@/views/background/BackgroundItemOverall'

export default {
  name: 'BackgroundOverall',
  // eslint-disable-next-line
  components: { BackgroundItemOverall, BackgroundItem },
  props: {
    position: {
      type: String,
      required: false,
      default: 'component'
    }
  },
  data() {
    return {
      commonBackground: null,
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
      'componentData',
      'canvasStyleData'
    ])
  },
  created() {
    this.init()
    bus.$on('onThemeColorChange', this.init)
  },
  beforeDestroy() {
    bus.$off('onThemeColorChange', this.init)
  },
  methods: {
    init() {
      this.commonBackground = this.canvasStyleData.chartInfo.chartCommonStyle
      if (this.commonBackground && this.commonBackground.outerImage && typeof (this.commonBackground.outerImage) === 'string') {
        this.fileList.push({ url: imgUrlTrans(this.commonBackground.outerImage) })
      }
      this.queryBackground()
    },
    queryBackground() {
      queryBackground().then(response => {
        this.BackgroundShowMap = response.data
      })
    },
    commitStyle() {
      this.$store.commit('recordSnapshot')
    },
    onChangeType() {
      this.commitStyle()
    },
    handleRemove(file, fileList) {
      this.uploadDisabled = false
      this.commonBackground.outerImage = null
      this.themeChange('outerImage')
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
        _this.commonBackground.outerImage = fileUrl
        this.themeChange('outerImage')
      })
    },
    themeChange(modifyName) {
      this.componentData.forEach((item, index) => {
        if (item.type === 'view') {
          item.commonBackground[modifyName] = this.commonBackground[modifyName]
        }
      })
      this.$store.commit('recordSnapshot')
    }
  }
}
</script>

<style scoped>
.el-card-template {
  width: 100%;
  height: 100%;
}

.main-row {
  background-size: 100% 100% !important;
  padding-left: 10px;
  margin-top: 10px;
  height: 230px;
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
}

.params-title {
  font-weight: 400 !important;
  font-size: 14px !important;
  color: var(--TextPrimary, #1F2329) !important;
  line-height: 40px;
}

.params-title-small {
  font-size: 12px !important;
  color: var(--TextPrimary, #1F2329) !important;
  line-height: 40px;
}

::v-deep .el-slider__input {
  width: 40px;
  padding-left: 0px;
  padding-right: 0px;
}

::v-deep .el-input__inner {
  padding: 0px !important;
}

::v-deep .el-slider__runway {
  margin-right: 60px !important;
}
</style>

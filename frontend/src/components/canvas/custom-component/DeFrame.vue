<template>
  <el-row class="main-frame">
    <div v-if="element.frameLinks.src" class="main-frame">
      <iframe v-if="frameShow" id="iframe" :src="element.frameLinks.src" scrolling="auto" frameborder="0" class="main-frame" @load="loaded" @error="onError" />
      <div v-if="editMode==='edit'" class="frame-mask edit-mask">
        <span style="opacity: 1;">
          <span style="font-weight: bold;color: lawngreen;">{{ $t('panel.edit_web_tips') }}</span>
        </span>
      </div>
      <!--Here are three 15px wide masks(left top right) for easy clicking on the display jump button-->
      <div v-if="editMode!=='edit'" class="frame-mask preview-top-mask" />
      <div v-if="editMode!=='edit'" class="frame-mask preview-right-mask" />
      <div v-if="editMode!=='edit'" class="frame-mask preview-left-mask" />
      <div v-if="screenShot" class="frame-mask" />
    </div>
    <div v-else class="info-class">
      {{ $t('panel.web_add_tips') }}
    </div>
  </el-row>
</template>

<script>
import { mapState } from 'vuex'
import bus from '@/utils/bus'
import eventBus from '@/components/canvas/utils/eventBus'
export default {
  name: 'DeFrame',
  props: {
    propValue: {
      type: String,
      require: true
    },
    element: {
      type: Object
    },
    editMode: {
      type: String,
      require: false,
      default: 'edit'
    },
    active: {
      type: Boolean,
      require: false,
      default: false
    },
    screenShot: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      pOption: {},
      frameShow: true
    }
  },

  computed: {
    ...mapState([
      'componentGap',
      'canvasStyleData'
    ])
  },
  watch: {
    h(newVal, oldVla) {
    }
  },
  created() {
  },
  mounted() {
    bus.$on('frameLinksChange-' + this.element.id, this.frameLinksChange)
    eventBus.$on('startMoveIn',this.frameLinksChange)
  },
  beforeDestroy() {
    bus.$off('frameLinksChange-' + this.element.id, this.frameLinksChange)
  },
  methods: {
    frameLinksChange() {
      this.frameShow = false
      this.$nextTick(() => {
        this.frameShow = true
      })
    },
    loaded(e) {
    },
    onError(e) {
    }

  }
}
</script>

<style>
  .info-class{
    text-align: center;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: rgba(255,255,255,0.3);
    font-size: 12px;
    color: #9ea6b2;
  }

  .main-frame{
    height: 100%;
    width: 100%;
  }
  .frame-mask {
    display: flex;
    opacity: 0.5;
    position:absolute;
    top:0px;
    z-index: 1;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .edit-mask{
    left: 0px;
    background-color: #5c5e61;
    height: 100%!important;
    width: 100% !important;
  }
  .preview-top-mask{
    left: 0px;
    height: 15px!important;
    width: 100% !important;
  }
  .preview-right-mask{
    right: 0px;
    height: 100%!important;
    width: 15px !important;
  }
  .preview-left-mask{
    left: 0px;
    height: 100%!important;
    width: 15px !important;
  }
</style>


<template>
  <el-row class="main-frame">
    <div v-if="element.frameLinks.src" class="main-frame">
      <iframe v-if="frameShow" id="iframe" :src="element.frameLinks.src" scrolling="auto" frameborder="0" class="main-frame" @load="loaded" @error="onError" />
      <div v-if="editMode==='edit'" class="frame-mask">
        <span style="opacity: 1;">
          <span style="font-weight: bold;color: lawngreen;">{{ $t('panel.edit_web_tips') }}</span>
        </span>
      </div>
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
    bus.$on('frameLinksChange-' + this.element.id, () => {
      this.frameShow = false
      this.$nextTick(() => {
        this.frameShow = true
      })
    })
  },
  methods: {
    loaded(e) {
      console.log('loaded:', e)
    },
    onError(e) {
      console.log('onError:', e)
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
    background-color: #FFFFFF;
    font-size: 12px;
    color: #9ea6b2;
  }

  .main-frame{
    height: 100%;
    width: 100%;
  }
  .frame-mask {
    display: flex;
    height: 100%!important;
    width: 100% !important;
    background-color: #5c5e61;
    opacity: 0.5;
    position:absolute;
    top:0px;
    left: 0px;
    z-index: 2;
    display: flex;
    align-items: center;
    justify-content: center;
  }
</style>


<template>
  <el-row class="main-frame">
    <div v-if="element.frameLinks.src" class="main-frame">
      <iframe id="iframe" :src="element.frameLinks.src" scrolling="auto" frameborder="0" class="main-frame" />
      <div v-if="editMode==='edit'" class="frame-mask">
        <span style="opacity: 1;">
          <span style="font-weight: bold;color: lawngreen;">{{ $t('panel.edit_web_tips') }}</span>
        </span>
      </div>
    </div>
    <div v-else class="info-class">
      {{ $t('panel.web_add_tips') }}
    </div>
  </el-row>
</template>

<script>
import { mapState } from 'vuex'
import NProgress from 'nprogress'
export default {
  name: 'DeVideo',
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
    }
  },
  data() {
    return {
      pOption: {}
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
  },
  methods: {

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


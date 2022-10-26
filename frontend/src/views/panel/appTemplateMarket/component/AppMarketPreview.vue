<template>
  <el-row>
    <el-row style="height: 56px">
      <el-col
        :span="12"
        style="text-align: left;line-height: 56px"
      >
        <svg-icon
          icon-class="icon_left_outlined"
          class="toolbar-icon-active icon20 margin-left20"
          @click="closePreview"
        />
        <span class="text16 margin-left12">
          {{ templateInfo.name }}
        </span>
      </el-col>
      <el-col
        :span="12"
        style="text-align: right;line-height: 56px;padding-right: 24px"
      >
        <el-button
          size="small"
          type="primary"
          width="80px"
          @click="appApply"
        >{{ $t('panel.apply') }}
        </el-button>
      </el-col>
    </el-row>
    <el-row class="img-main">
      <img
        width="100%"
        :src="templateInfo.snapshot"
        alt=""
      >
    </el-row>
  </el-row>
</template>

<script>
export default {
  name: 'AppMarketPreview',
  components: { },
  props: {
    templateInfo: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      noneImg: require('@/assets/None.png')
    }
  },
  computed: {

  },
  watch: {

  },
  mounted() {

  },
  methods: {
    appApply() {
      this.$emit('appApply')
    },
    closePreview() {
      this.$emit('closePreview')
    }
  }
}
</script>

<style lang="scss" scoped>
.img-main{
  border-radius: 4px;
  overflow-x: auto;
  overflow-y: auto;
  text-align: center;
  height: calc(100vh - 113px)!important;
}

.toolbar-icon-active {
  cursor: pointer;
  transition: .1s;
  border-radius: 3px;

  &:active {
    color: #000;
    border-color: #3a8ee6;
    background-color: red;
    outline: 0;
  }

  &:hover {
    background-color: rgba(31, 35, 41, 0.1);
    color: #3a8ee6;
  }
}

.icon20 {
  font-size: 20px;
  color: var(--TextPrimary, #1F2329);
}

.icon16 {
  font-size: 16px!important;
  color: var(--TextPrimary, #1F2329);
}

.text16 {
  font-size: 16px;
  font-weight: 500;
  line-height: 24px;
  color: var(--TextPrimary, #1F2329);
}

.margin-left12 {
  margin-left: 12px !important;
}
.margin-left20 {
  margin-left: 12px !important;
}
</style>

<template>
  <div class="testcase-template">
    <div class="template-img" :style="classBackground" @click.stop="templatePreview" />
    <el-row class="bottom-area">
      <el-row>
        <span class="demonstration">{{ template.title }}</span>
      </el-row>
    </el-row>
    <el-row class="template-button">
      <el-button size="mini" style="width: 141px" @click="templatePreview">{{ $t('panel.preview') }}</el-button>
      <el-button size="mini" style="width: 141px" type="primary" @click="apply">{{ $t('panel.apply') }}</el-button>
    </el-row>
  </div>
</template>

<script>
import {imgUrlTrans} from "@/components/canvas/utils/utils";

export default {
  name: 'TemplateMarketItem',
  props: {
    template: {
      type: Object,
      default() {
        return {}
      }
    },
    baseUrl: {
      type: String
    },
    width: {
      type: Number
    }
  },
  data() {
    return {
    }
  },
  computed: {
    classBackground() {
      return {
        width: this.width + 'px',
        height: this.width * 0.58 + 'px',
        background: `url(${this.template.snapshot}) no-repeat`,
        'background-size': `100% 100%`
      }
    }
  },
  methods: {

    init(){

    },

    handleDelete() {

    },
    apply() {
      this.$emit('templateApply', this.template)
    },
    templatePreview() {
      this.$emit('templatePreview', this.template.id)
    }
  }
}
</script>

<style scoped>

  .testcase-template {
    position: relative;
    display: inline-block;
    margin: 0;
    box-shadow: 0 0 2px 0 rgba(31,31,31,0.15), 0 1px 2px 0 rgba(31,31,31,0.15);
    border: solid 2px #fff;
    box-sizing: border-box;
    border-radius: 4px;
    width: 100%;
  }

  .demonstration {
    display: block;
    font-size: 16px;
    text-align: left;
    margin-left: 12px;
    margin-top: 12px;
    white-space:nowrap;
    overflow:hidden;
    text-overflow:ellipsis;
    color: var(--TextPrimary, #1F2329);
  }

  .template-img {
    background-size: 100% 100%;
    margin: 0 auto;
    border: solid 2px #fff;
    box-sizing: border-box;
  }

  .template-img:hover {
    border: solid 1px #4b8fdf;
    border-radius: 4px;
    color: deepskyblue;
    cursor: pointer;
  }
  .testcase-template:hover ::v-deep .template-button{
    display: inline;
  }

  .template-button {
    display: none;
    text-align: center;
    position:absolute;
    bottom: 5px;
    left: 0px;
    width: 100%;
  }

  .bottom-area{
    height: 75px;
  }
</style>

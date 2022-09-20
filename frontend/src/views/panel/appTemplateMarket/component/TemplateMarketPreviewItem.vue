<template>
  <div
    :class="[
      {
        ['template-item-main-active']: active
      },
      'template-item-main'
    ]"
    @click.stop="previewTemplate"
  >
    <div class="template-item-img" :style="classBackground" />
    <span class="demonstration">{{ template.name }}</span>
  </div>
</template>

<script>

export default {
  name: 'TemplateMarketPreviewItem',
  props: {
    template: {
      type: Object,
      default() {
        return {}
      }
    },
    active: {
      type: Boolean,
      required: false,
      default: false
    }
  },
  data() {
    return {
    }
  },
  computed: {
    classBackground() {
      return {
        background: `url(${this.template.snapshot}) no-repeat`,
        'background-size': `100% 100%`
      }
    }
  },
  methods: {
    previewTemplate() {
      this.$emit('previewTemplate', this.template)
    }
  }
}
</script>

<style scoped>

  .template-item-main {
    margin: 0 24px 0 0;
    float: left;
    position: relative;
    box-sizing: border-box;
    width: 250px;
    height: 180px;
    background-color: var(--ContentBG,#ffffff);
    border: 1px solid #DEE0E3 ;
    border-radius: 4px;
    flex: none;
    order: 0;
    flex-grow: 0;
    cursor: pointer;
    overflow: hidden;
  }

  .template-item-main-active{
    border: 2px solid #3370FF!important; ;

  }
  .template-item-img{
    position: absolute;
    width: 248px;
    height: 138px;
    left: 0px;
    top: 0px;
  }

  .demonstration {
    position: absolute;
    width: 166px;
    height: 20px;
    left: 8px;
    top: 148px;
    font-style: normal;
    font-weight: 400;
    font-size: 12px;
    line-height: 20px;
    display: block;
    white-space:nowrap;
    overflow:hidden;
    text-overflow:ellipsis;
  }

  .template-item-main:hover {
    border: solid 1px #3370FF;
  }

</style>

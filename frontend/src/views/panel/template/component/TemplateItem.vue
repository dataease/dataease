<template>
  <div class="testcase-template">
    <div :style="classBackground" class="template-img">
      <i class="el-icon-error" @click.stop="templateDelete" />
    </div>
    <span class="demonstration">{{ template.name }}</span>
  </div>
</template>

<script>
import { get, post } from '@/api/panel/panel'
export default {
  name: 'TemplateItem',
  props: {
    template: {
      type: Object,
      default() {
        return {}
      }
    }
  },
  computed: {
    classBackground() {
      return {
        background: `url(${this.template.snapshot}) no-repeat`,
        'background-size': '100%,100%'
      }
    }
  },
  methods: {
    templateDelete() {
      console.log('templateDelete')
    },
    handleDelete() {
      console.log('handleDelete')
    }
  }
}
</script>

<style scoped>

  .testcase-template {
    display: inline-block;
    margin: 10px 30px;
    width: 150px;
  }

  .demonstration {
    display: block;
    text-align: center;
    margin: 10px auto;
    width: 150px;
    white-space:nowrap;
    overflow:hidden;
    text-overflow:ellipsis;
  }

  .template-img {
    height: 150px;
    width: 200px;
    margin: 0 auto;
    box-shadow: 0 0 2px 0 rgba(31,31,31,0.15), 0 1px 2px 0 rgba(31,31,31,0.15);
    border: solid 2px #fff;
    box-sizing: border-box;
    border-radius: 3px;
    background-size: 100%,100%;
  }

  .template-img:hover {
    border: solid 1px #4b8fdf;
    border-radius: 3px;
    color: deepskyblue;
    cursor: pointer;
  }

  .template-img > i{
    display:none;
    float: right;
    color: gray;
    margin: 2px;
  }

  .template-img > i:hover {
    color: red;
  }

  .template-img:hover > .el-icon-error {
    display: inline;
  }

</style>

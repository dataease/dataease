<template>
  <div class="testcase-template">
    <div class="template-img" :style="classBackground">
      <i class="el-icon-error" @click.stop="templateDelete" />
      <i class="el-icon-edit" @click.stop="templateEdit" />
    </div>
    <el-tooltip class="item" effect="light" :content="template.name" placement="bottom">
      <span class="demonstration">{{ template.name }}</span>
    </el-tooltip>
  </div>
</template>

<script>
// eslint-disable-next-line no-unused-vars
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
      if (this.template.snapshot) {
        return {
          background: `url(${this.template.snapshot}) no-repeat`,
          'background-size': `100% 100%`
        }
      } else {
        return {}
      }
    }
  },
  methods: {
    templateDelete() {
      this.$alert(this.$t('panel.confirm_delete') + this.$t('panel.template') + ': ' + this.template.name + '？', '', {
        confirmButtonText: this.$t('panel.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            this.$emit('templateDelete', this.template.id)
          }
        }
      })
    },
    templateEdit() {
      this.$emit('templateEdit', this.template)
    },
    handleDelete() {
      // console.log('handleDelete')
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
    font-size: 14px;
    color: gray;
    text-align: center;
    margin: 10px auto;
    width: 150px;
    white-space:nowrap;
    overflow:hidden;
    text-overflow:ellipsis;
  }

  .template-img {
    height: 112px;
    width: 200px;
    margin: 0 auto;
    box-shadow: 0 0 2px 0 rgba(31,31,31,0.15), 0 1px 2px 0 rgba(31,31,31,0.15);
    border: solid 2px #fff;
    box-sizing: border-box;
    border-radius: 3px;
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

  .template-img:hover > .el-icon-edit {
    display: inline;
  }

</style>

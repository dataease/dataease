<template>
  <div
    class="el-input-tag input-tag-wrapper"
    :class="[size ? 'el-input-tag--' + size : '']"
    style="height: auto"
    @click="foucusTagInput">

    <el-tag
      class="ms-top"
      v-for="(tag, idx) in innerTags"
      v-bind="$attrs"
      type="info"
      :key="tag"
      :size="size"
      :closable="!readOnly"
      :disable-transitions="false"
      @close="remove(idx)">
      {{tag && tag.length>10? tag.substring(0,10)+"...":tag}}
    </el-tag>
    <input
      v-if="!readOnly"
      class="tag-input el-input"
      v-model="newTag"
      :placeholder="$t('commons.tag_tip')"
      @keydown.delete.stop="removeLastTag"
      @keydown="addNew"
      @blur="addNew"/>
  </div>
</template>

<script>
  export default {
    name: 'MsInputTag',
    props: {
      currentScenario: {},
      addTagOnKeys: {
        type: Array,
        default: () => [13, 188, 9]
      },
      readOnly: {
        type: Boolean,
        default: false
      },
      size: {type: String, default: "small"}
    },
    created() {
      if (!this.currentScenario.tags) {
        this.currentScenario.tags = [];
      }
    },
    data() {
      return {
        newTag: '',
        innerTags: this.currentScenario.tags ? [...this.currentScenario.tags] : []
      }
    },
    watch: {
      innerTags() {
        this.currentScenario.tags = this.innerTags;
      }
    },
    methods: {
      foucusTagInput() {
        if (this.readOnly || !this.$el.querySelector('.tag-input')) {
          console.log()
        } else {
          this.$el.querySelector('.tag-input').focus()
        }
      },
      addNew(e) {
        if (e && (!this.addTagOnKeys.includes(e.keyCode)) && (e.type !== 'blur')) {
          return
        }
        if (e) {
          e.stopPropagation()
          e.preventDefault()
        }
        let addSuucess = false
        if (this.newTag.includes(',')) {
          this.newTag.split(',').forEach(item => {
            if (this.addTag(item.trim())) {
              addSuucess = true
            }
          })
        } else {
          if (this.addTag(this.newTag.trim())) {
            addSuucess = true
          }
        }
        if (addSuucess) {
          this.tagChange()
          this.newTag = ''
        }
      },
      addTag(tag) {
        tag = tag.trim()
        if (tag && !this.innerTags.includes(tag)) {
          this.innerTags.push(tag)
          return true
        }
        return false
      },
      remove(index) {
        this.innerTags.splice(index, 1)
        this.tagChange()
      },
      removeLastTag() {
        if (this.newTag) {
          return
        }
        this.innerTags.pop()
        this.tagChange()
      },
      tagChange() {
        this.$emit('input', this.innerTags)
      }
    }
  }
</script>

<style scoped>
  .input-tag-wrapper {
    position: relative;
    font-size: 14px;
    background-color: #fff;
    background-image: none;
    border-radius: 4px;
    border: 1px solid #dcdfe6;
    box-sizing: border-box;
    color: #606266;
    display: inline-block;
    outline: none;
    padding: 0 10px 0 5px;
    transition: border-color .2s cubic-bezier(.645, .045, .355, 1);
    width: 100%;
  }

  .el-tag {
    margin-right: 4px;
  }

  .tag-input {
    background: transparent;
    border: 0;
    color: #303133;
    font-size: 12px;
    font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", Arial, sans-serif;
    outline: none;
    padding-left: 0;
    width: 100px;
  }

  .el-input-tag {
    height: 40px;
    line-height: 40px;
  }

  .el-input-tag--mini {
    height: 28px;
    line-height: 28px;
    font-size: 12px;
  }

  .el-input-tag--small {
    line-height: 30px;
  }

  .el-input-tag--medium {
    height: 36px;
    line-height: 36px;
  }

</style>

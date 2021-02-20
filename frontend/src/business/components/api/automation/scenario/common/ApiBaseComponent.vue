<template>
  <el-card class="api-component">

    <div class="header" @click="active(data)">

      <slot name="beforeHeaderLeft">
        <div v-if="data.index" class="el-step__icon is-text" style="margin-right: 10px;" :style="{'color': color, 'background-color': backgroundColor}">
          <div class="el-step__icon-inner">{{data.index}}</div>
        </div>
        <el-button class="ms-left-buttion" size="small" :style="{'color': color, 'background-color': backgroundColor}">{{title}}</el-button>
      </slot>

      <span @click.stop>
        <slot name="headerLeft">
          <i class="icon el-icon-arrow-right" :class="{'is-active': data.active}"
             @click="active(data)" v-if="data.type!='scenario'"/>
          <el-input :draggable="draggable" v-if="isShowInput && isShowNameInput" size="small" v-model="data.name" class="name-input"
                    @blur="isShowInput = false" :placeholder="$t('commons.input_name')" ref="nameEdit" :disabled="data.disabled"/>
          <span v-else>
            {{data.name}}
            <i class="el-icon-edit" style="cursor:pointer" @click="editName" v-tester v-if="data.referenced!='REF' && !data.disabled"/>
          </span>
        </slot>
        <slot name="behindHeaderLeft"></slot>
      </span>

      <div class="header-right" @click.stop>
        <slot name="message"></slot>
        <el-tooltip :content="$t('test_resource_pool.enable_disable')" placement="top">
          <el-switch v-model="data.enable" class="enable-switch"/>
        </el-tooltip>
        <slot name="button"></slot>
        <el-tooltip content="Copy" placement="top">
          <el-button size="mini" icon="el-icon-copy-document" circle @click="copyRow"/>
        </el-tooltip>
        <el-tooltip :content="$t('commons.remove')" placement="top">
          <el-button size="mini" icon="el-icon-delete" type="danger" circle @click="remove"/>
        </el-tooltip>
      </div>

    </div>
    <div class="header">
      <fieldset :disabled="data.disabled" class="ms-fieldset">
        <el-collapse-transition>
          <div v-if="data.active && showCollapse" :draggable="draggable">
            <el-divider></el-divider>
            <slot></slot>
          </div>
        </el-collapse-transition>
      </fieldset>
    </div>

  </el-card>
</template>

<script>
  export default {
    name: "ApiBaseComponent",
    data() {
      return {
        isShowInput: false
      }
    },
    props: {
      draggable: Boolean,
      data: {
        type: Object,
        default() {
          return {}
        },
      },
      color: {
        type: String,
        default() {
          return "#B8741A"
        }
      },
      backgroundColor: {
        type: String,
        default() {
          return "#F9F1EA"
        }
      },
      showCollapse: {
        type: Boolean,
        default() {
          return true
        }
      },
      isShowNameInput: {
        type: Boolean,
        default() {
          return true
        }
      },
      title: String
    },
    created() {
      if (!this.data.name) {
        this.isShowInput = true;
      }
      if (this.$refs.nameEdit) {
        this.$nextTick(() => {
          this.$refs.nameEdit.focus();
        });
      }
    },
    methods: {
      active() {
        // 这种写法性能极差，不要再放开了
        //this.$set(this.data, 'active', !this.data.active);
        this.$emit('active');
      },
      copyRow() {
        this.$emit('copy');
      },
      remove() {
        this.$emit('remove');
      },
      editName() {
        this.isShowInput = true;
        this.$nextTick(() => {
          this.$refs.nameEdit.focus();
        });
      }
    }
  }
</script>

<style scoped>

  .icon.is-active {
    transform: rotate(90deg);
  }

  .name-input {
    width: 30%;
  }

  .el-icon-arrow-right {
    margin-right: 5px;
  }

  .ms-left-buttion {
    margin-right: 15px;
  }

  .header-right {
    margin-right: 20px;
    float: right;
  }

  .enable-switch {
    margin-right: 10px;
  }

  fieldset {
    padding: 0px;
    margin: 0px;
    min-width: 100%;
    min-inline-size: 0px;
    border: 0px;
  }

</style>

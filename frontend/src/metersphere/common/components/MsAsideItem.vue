<template>
  <ms-aside-container :enable-aside-hidden="false" :width="width + 'px'">
    <div class="title-bar" :style="{'height': titleBarHeight + 'px'}">
      <slot name="title">
        <span :style="{'line-height': titleBarHeight - 10 + 'px'}" class="title-left">
          {{title}}
        </span>
        <span :style="{'line-height': titleBarHeight - 10 + 'px'}" class="title-right">
          <i class="el-icon-plus" @click="addFuc"/>
        </span>
      </slot>
    </div>
    <div :style="{'height': itemBarHeight + 'px'}" v-for="(item, index) in data" :key="index" class="item-bar" @click="itemSelected(index, item)" :class="{'item-selected' : index == selectIndex}">
      <input class="item-input" :style="{'height': itemBarHeight - 12 + 'px', 'line-height': itemBarHeight - 12 + 'px', 'width': width - 90 + 'px'}" v-model="item.name" :placeholder="$t('commons.input_content')"/>
      <span :style="{'line-height': itemBarHeight - 10 + 'px'}" class="item-right">
        <i v-for="(operator, operatorIndex) in itemOperators" :key="operatorIndex" :class="operator.icon" @click.stop="operator.func(item, index)"/>
      </span>
    </div>
  </ms-aside-container>
</template>

<script>
    import MsAsideContainer from "./MsAsideContainer";

    export default {
      name: "MsAsideItem",
      components: {MsAsideContainer},
      data() {
        return {
          selectIndex: -1
        }
      },
      props: {
        width: {
          type: Number,
          default: 200
        },
        titleBarHeight: {
          type: Number,
          default: 40
        },
        itemBarHeight: {
          type: Number,
          default: 35
        },
        title: String,
        data: Array,
        deleteFuc: Function,
        addFuc: Function,
        itemOperators: {
          type: Array,
          default() {
            return [
              {
                icon: 'el-icon-delete',
                func: this.deleteFuc
              }
            ];
          }
        },
        enableAsideHidden: {
          type: Boolean,
          default: true
        },
      },
      methods: {
        itemSelected(index, item) {
          this.selectIndex = index;
          this.$emit('itemSelected', item);
        }
      }
    }
</script>

<style scoped>

  .ms-aside-container {
    padding: 0;
  }

  .title-bar {
    width: 100%;
    background: #e9ebef;
    padding: 5px 10px;
    box-sizing: border-box;
  }

  .item-bar {
    width: 100%;
    background: #F9F9F9;
    padding: 5px 10px;
    box-sizing: border-box;
    border: solid 1px #e6e6e6;
  }

  .item-bar:hover .item-right {
    visibility: visible;
  }

  .title-right,.item-right {
    float: right;
  }

  .item-right {
    visibility: hidden;
  }

  .item-right i {
    margin: 5px;
  }

  i:hover {
    color: #409EFF;
    font-size: large;
  }

  .item-selected {
    background: #ECF5FF;
    border-left: solid #409EFF 5px;
  }

  .item-selected .item-right {
    visibility: visible;
  }

  .item-input {
    border: hidden;
    display: inline;
    background-color:rgba(0,0,0,0);
  }

  .item-input:focus{
    outline:none;
  }

</style>

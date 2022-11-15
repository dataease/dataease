<template>
  <div
    class="logic"
    :style="marginLeft"
  >
    <div class="logic-left">
      <div class="operate-title">
        <span
          v-if="x"
          style="
             {
              color: '#bfbfbf';
            }
          "
          class="mrg-title"
        >
          {{ logic === 'or' ? "OR" : "AND" }}
        </span>
        <el-dropdown
          v-else
          trigger="click"
          @command="handleCommand"
        >
          <span
            style="
               {
                color: 'rgba(0,0,0,.65)';
              }
            "
            class="mrg-title"
          >
            {{ logic === 'or' ? "OR" : "AND" }}<i class="el-icon-arrow-down" />
          </span>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="and">AND</el-dropdown-item>
            <el-dropdown-item command="or">OR</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
      <span
        v-if="x"
        class="operate-icon"
      >
        <i
          class="el-icon-delete"
          @click="$emit('removeRelationList')"
        />
      </span>
    </div>
    <div class="logic-right">
      <template v-for="(item, index) in relationList">
        <logic-relation
          v-if="item.child"
          :key="index"
          :x="item.x"
          :logic="item.logic"
          :relation-list="item.child"
          @del="(idx) => del(idx, item.child)"
          @addCondReal="(type, logic) => add(type, item.child, logic)"
          @removeRelationList="removeRelationList(index)"
        />
        <filter-filed
          v-else
          :key="index"
          :item="item"
          :index="index"
          @del="$emit('del', index)"
        />
      </template>
      <div class="logic-right-add">
        <button
          class="operand-btn"
          @click="addCondReal('condition')"
        >
          + {{ $t('auth.add_condition') }}
        </button>
        <button
          v-if="x < 2"
          class="operand-btn"
          @click="addCondReal('relation')"
        >
          + {{ $t('auth.add_relationship') }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import filterFiled from './filterFiled'
export default {
  name: 'LogicRelation',
  components: {
    filterFiled
  },
  props: {
    relationList: {
      type: Array,
      default: () => []
    },
    x: {
      type: Number,
      default: 0
    },
    logic: {
      type: String,
      default: 'or'
    }
  },
  computed: {
    marginLeft() {
      return {
        marginLeft: this.x ? '20px' : 0
      }
    }
  },
  methods: {
    handleCommand(type) {
      this.$emit('update:logic', type)
      this.$emit('changeAndOrDfs', type)
    },
    removeRelationList(index) {
      this.relationList.splice(index, 1)
    },
    addCondReal(type) {
      this.$emit('addCondReal', type, this.logic === 'or' ? 'and' : 'or')
    },
    add(type, child, logic) {
      child.push(type === 'condition' ? { fieldId: '', value: '', enumValue: '', term: '', filterType: 'logic', name: '', deType: '' } : { child: [], logic })
    },
    del(index, child) {
      child.splice(index, 1)
    }
  }
}
</script>

<style lang="scss" scope>
.logic {
  display: flex;
  align-items: center;
  position: relative;
  z-index: 1;
  width: 100%;

  .logic-left {
    box-sizing: border-box;
    width: 48px;
    display: flex;
    position: relative;
    flex-direction: row;
    z-index: 10;

    .operate-title {
      font-family: PingFang SC, Hiragino Sans GB, Microsoft YaHei, sans-serif;
      word-wrap: break-word;
      box-sizing: border-box;
      color: rgba(0, 0, 0, 0.65);
      font-size: 12px;
      display: inline-block;
      white-space: nowrap;
      margin: 0;
      padding: 0;
      width: 65px;
      background-color: #f8f8fa;
      line-height: 28px;
      position: relative;
      z-index: 1;
      height: 28px;

      .mrg-title {
        text-align: left;
        box-sizing: border-box;
        position: relative;
        display: block;
        margin-left: 11px;
        margin-right: 11px;
        line-height: 28px;
        height: 28px;

        i {
        font-size: 12px;
      }
      }
    }

    &:hover {
      .operate-icon {
        display: flex;
        align-items: center;
      }

      .operate-title {
          .mrg-title {
            margin-right: 0 !important;
          }
      }

    }

    .operate-icon {
      width: 40px;
      height: 28px;
      line-height: 28px;
      background-color: #f8f8fa;
      z-index: 1;
      display: none;

      i {
        font-size: 12px;
        font-style: normal;
        display: unset;
        padding: 5px 3px;
        cursor: pointer;
        position: relative;
        z-index: 10;
      }
    }
  }

  .logic-right-add {
    display: flex;
    height: 41.4px;
    align-items: center;
    padding-left: 26px;

    .operand-btn {
      box-sizing: border-box;
      font-weight: 400;
      text-align: center;
      box-shadow: 0 2px 0 rgba(0, 0, 0, 0.015);
      transition: all 0.3s cubic-bezier(0.645, 0.045, 0.355, 1);
      outline: 0;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      line-height: 1;
      -webkit-appearance: button;
      cursor: pointer;
      height: 28px;
      padding: 0 10px;
      margin-right: 10px;
      font-size: 12px;
      color: #246dff;
      background: #fff;
      border: 1px solid #246dff;
      border-radius: 2px;
    }
  }
}
</style>

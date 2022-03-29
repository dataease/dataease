<template>
  <div>
    <div style="width: 100%;">
      <el-dropdown trigger="click" @mouseup="handleMouseUp">
        <slot name="icon" />
        <el-dropdown-menu v-if="curComponent">
          <el-dropdown-item v-if="editFilter.includes(curComponent.type)" icon="el-icon-edit-outline" @click.native="edit">{{ $t('panel.edit') }}</el-dropdown-item>
          <el-dropdown-item icon="el-icon-document-copy" @click.native="copy">{{ $t('panel.copy') }}</el-dropdown-item>
          <el-dropdown-item icon="el-icon-delete" @click.native="deleteComponent">{{ $t('panel.delete') }}</el-dropdown-item>
          <el-dropdown-item icon="el-icon-upload2" @click.native="topComponent">{{ $t('panel.topComponent') }}</el-dropdown-item>
          <el-dropdown-item icon="el-icon-download" @click.native="bottomComponent">{{ $t('panel.bottomComponent') }}</el-dropdown-item>
          <el-dropdown-item icon="el-icon-arrow-up" @click.native="upComponent">{{ $t('panel.upComponent') }}</el-dropdown-item>
          <el-dropdown-item icon="el-icon-arrow-down" @click.native="downComponent">{{ $t('panel.downComponent') }}</el-dropdown-item>
          <el-dropdown-item v-if="'view'===curComponent.type" icon="el-icon-link" @click.native="linkageSetting">{{ $t('panel.linkage_setting') }}</el-dropdown-item>
          <el-dropdown-item v-if="'de-tabs'===curComponent.type" icon="el-icon-link" @click.native="addTab">{{ $t('panel.add_tab') }}</el-dropdown-item>
          <el-dropdown-item v-if="'view'===curComponent.type" icon="el-icon-connection" @click.native="linkJumpSet">{{ $t('panel.setting_jump') }}</el-dropdown-item>
          <el-dropdown-item icon="el-icon-magic-stick" @click.native="boardSet">{{ $t('panel.component_style') }}</el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import bus from '@/utils/bus'
import { getViewLinkageGather } from '@/api/panel/linkage'

export default {
  data() {
    return {
      copyData: null,
      editFilter: [
        'view',
        'custom'
      ]
    }
  },
  computed: mapState([
    'menuTop',
    'menuLeft',
    'menuShow',
    'curComponent',
    'componentData',
    'canvasStyleData'
  ]),
  methods: {
    edit() {
      if (this.curComponent.type === 'custom') {
        bus.$emit('component-dialog-edit')
      } else if (this.curComponent.type === 'v-text' || this.curComponent.type === 'rect-shape') {
        bus.$emit('component-dialog-style')
      } else { bus.$emit('change_panel_right_draw', true) }
    },
    lock() {
      this.$store.commit('lock')
    },

    unlock() {
      this.$store.commit('unlock')
    },

    // 点击菜单时不取消当前组件的选中状态
    handleMouseUp() {
      this.$store.commit('setClickComponentStatus', true)
    },

    cut() {
      this.deleteCurCondition()
      this.$store.commit('cut')
    },

    copy() {
      this.$store.commit('copy')
      this.paste()
    },

    paste() {
      this.$store.commit('paste', true)
      this.$store.commit('recordSnapshot', 'paste')
    },

    deleteComponent() {
      this.$emit('amRemoveItem')
      this.deleteCurCondition()
      this.$store.commit('deleteComponent')
      this.$store.commit('recordSnapshot', 'deleteComponent')
      this.$store.commit('setCurComponent', { component: null, index: null })
    },

    deleteCurCondition() {
      if (this.curComponent.type === 'custom') {
        this.$store.commit('removeViewFilter', this.curComponent.id)
        bus.$emit('delete-condition', { componentId: this.curComponent.id })
      }
    },

    upComponent() {
      this.$store.commit('upComponent')
      this.$store.commit('recordSnapshot', 'upComponent')
    },

    downComponent() {
      this.$store.commit('downComponent')
      this.$store.commit('recordSnapshot', 'downComponent')
    },

    topComponent() {
      this.$store.commit('topComponent')
      this.$store.commit('recordSnapshot', 'topComponent')
    },

    bottomComponent() {
      this.$store.commit('bottomComponent')
      this.$store.commit('recordSnapshot', 'bottomComponent')
    },
    linkageSetting() {
      // sourceViewId 也加入查询
      const targetViewIds = this.componentData.filter(item => item.type === 'view' && item.propValue && item.propValue.viewId)
        .map(item => item.propValue.viewId)

      // 获取当前仪表板当前视图联动信息
      const requestInfo = {
        'panelId': this.$store.state.panel.panelInfo.id,
        'sourceViewId': this.curComponent.propValue.viewId,
        'targetViewIds': targetViewIds
      }
      getViewLinkageGather(requestInfo).then(rsp => {
        this.$store.commit('setLinkageInfo', rsp.data)
      })
    },
    addTab() {
      bus.$emit('add-new-tab', this.curComponent.id)
    },
    // 跳转设置
    linkJumpSet() {
      this.$emit('linkJumpSet')
    },
    // 设置边框
    boardSet() {
      this.$emit('boardSet')
    }
  }
}
</script>

<style lang="scss" scoped>
  .contextmenu {
    position: absolute;
    z-index: 1000;

    ul {
      border: 1px solid #e4e7ed;
      border-radius: 4px;
      background-color: #fff;
      box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);
      box-sizing: border-box;
      margin: 5px 0;
      padding: 6px 0;

      li {
        font-size: 14px;
        padding: 0 20px;
        position: relative;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        color: #606266;
        height: 34px;
        line-height: 34px;
        box-sizing: border-box;
        cursor: pointer;

        &:hover {
          background-color: var(--background-color-base, #f5f7fa);
        }
      }
    }
  }
</style>

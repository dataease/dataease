<template>
  <div class="filter-container">
    <div class="filter-widget-content">
      <div
        v-for="(item, index) in tabUseList"
        :key="index"
        :data-id="item.id"
        :data-index="index"
        :class="[
          {
            ['selected-class']:selectedId==item.id
          },
          'filter-widget '+ (item.defaultClass || '')
        ]"
        @click="componentSelect(item)"
      >
        <div class="filter-widget-icon">
          <i :class="(item.icon || 'el-icon-setting') + ' widget-icon-i'" />
        </div>
        <div class="filter-widget-text">{{ item.label }}</div>
      </div>
    </div>
  </div>

</template>

<script>
import { tabUseList } from '@/components/canvas/customComponent/component-list'
import { mapState } from 'vuex'

export default {
  name: 'TabUseList',
  props: {
    sourceSelectedId: {
      type: String,
      required: false
    }
  },
  data() {
    return {
      tabUseList,
      selectedId: this.sourceSelectedId
    }
  },
  computed: {
    ...mapState([
      'canvasStyleData'
    ])
  },

  methods: {
    componentSelect(item) {
      this.selectedId = item.id
    },
    getCurSelectedComponent() {
      if (this.selectedId !== this.sourceSelectedId) {
        return this.selectedId
      } else {
        return null
      }
    }
  }
}
</script>

<style lang="scss" scoped>

.filter-container {
  overflow: hidden auto;
  min-height: 24px;
  padding-top: 0px;
  padding-bottom: 0px;
  position: relative;
}

.filter-header {
  overflow: hidden;
  position: relative;
  margin-top: 24px;
  margin-left: 15px;
  align-items: center;
  word-break: break-all;
  display: flex;
  flex-direction: row;
  justify-content: flex-start;
  flex-wrap: nowrap;
}

.filter-header-text {
  font-size: 14px;
  max-width: 100%;
  color: var(--TextPrimary, gray);
  text-align: left;
  white-space: pre;
  text-overflow: ellipsis;
  position: relative;
  flex-shrink: 0;
  box-sizing: border-box;
  overflow: hidden;
  overflow-x: hidden;
  overflow-y: hidden;
  word-break: break-all;
}

.filter-widget-content {
  position: relative;
  margin-left: 5px;
}

.filter-widget {
  width: 107px;
  height: 36px;
  position: relative;
  float: left;
  margin-top: 10px;
  margin-left: 10px;
  filter: progid:DXImageTransform.Microsoft.gradient(startColorstr=#1a3685f2, endColorstr=#1a3685f2);
  font-size: 12px;
  border-radius: 10px;
  cursor: pointer;
  overflow: hidden;
}

.time-filter {
  background-color: rgba(54, 133, 242, .1);

  .filter-widget-icon {
    color: #3685f2;
  }

  .filter-widget-text {
    color: var(--TextActive, #3d4d66);
  }
}

.time-filter:hover {
  background-color: #3685f2;
  color: #fff;

  .filter-widget-icon {
    background-color: #3685f2;
    color: #fff;
  }

  .filter-widget-text {
    color: #fff;
  }
}

.text-filter {
  background-color: rgba(35, 190, 239, .1);

  .filter-widget-icon {
    color: #23beef;
  }

  .filter-widget-text {
    color: var(--TextActive, #3d4d66);
  }
}

.text-filter:hover {
  background-color: #23beef;
  color: #fff;

  .filter-widget-icon {
    background-color: #23beef;
    color: #fff;
  }

  .filter-widget-text {
    color: #fff;
  }
}

.tree-filter {
  background-color: rgba(22, 160, 132, .1);

  .filter-widget-icon {
    color: #37b4aa;
  }

  .filter-widget-text {
    color: var(--TextActive, #3d4d66);
  }
}

.tree-filter:hover {
  background-color: #37b4aa;

  .filter-widget-icon {
    color: #37b4aa;
  }

  .filter-widget-text {
    color: #fff;
  }
}

.filter-widget-icon {
  width: 40px;
  height: 36px;
  text-align: center;
  line-height: 1;
  position: absolute;
  top: 0px;
  bottom: 0px;
  left: 0px;
  justify-content: center;
  align-items: center;
  flex-direction: row;
  flex-wrap: nowrap;
  display: flex;

  .widget-icon-i {
    width: 24px;
    height: 24px;
    position: relative;
    flex-shrink: 0;
    font-size: 24px !important;
    margin: auto;
    font-family: fineui;
    font-style: normal;
    -webkit-font-smoothing: antialiased;
    text-align: center;

  }
}

.filter-widget-text {
  font-size: 14px;
  height: 36px;
  line-height: 36px;
  text-align: left;
  white-space: pre;
  text-overflow: ellipsis;
  position: absolute;
  /* inset: 0px 0px 0px 40px; */
  margin-left: 40px;
  box-sizing: border-box;
  overflow: hidden;
  overflow-x: hidden;
  overflow-y: hidden;
  word-break: break-all;
  cursor: pointer;

}

.widget-subject {
  display: flow-root;
}

.selected-class {
  background-color: #23beef;

  .filter-widget-icon {
    color: #fff;
  }

  .filter-widget-text {
    color: #fff;
  }
}

</style>

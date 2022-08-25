<template>
  <div class="filter-container" @dragstart="handleDragStart" @dragend="handleDragEnd()">
    <div v-for="(item, key) in widgetSubjects" :key="key" class="widget-subject">
      <div class="filter-header">
        <div class="filter-header-text"> {{ key }} </div>
      </div>
      <div class="filter-widget-content">
        <div
          v-for="(widget, index) in item"
          :key="widget.widgetName+index"
          :data-id="widget.widgetName"
          :draggable="(widget.widgetName !== 'buttonSureWidget' && widget.widgetName !== 'buttonResetWidget') || (widget.widgetName === 'buttonSureWidget' && !searchButtonExist) || (widget.widgetName === 'buttonResetWidget' && searchButtonExist && !resetButtonExist)"
          :data-index="index"
          :class="('filter-widget '+ (widget.widgetName === 'buttonSureWidget' ? sureButtonClass : widget.widgetName === 'buttonResetWidget' ? resetButtonClass : widget.defaultClass))"
        >
          <div class="filter-widget-icon">
            <i :class="(widget.icon || 'el-icon-setting') + ' widget-icon-i'" />
          </div>
          <div class="filter-widget-text">{{ $t(widget.label) || widget.label }}</div>
        </div>
      </div>
    </div>

  </div>

</template>

<script>
import { ApplicationContext } from '@/utils/ApplicationContext'
import { deepCopy } from '@/components/canvas/utils/utils'
import eventBus from '@/components/canvas/utils/eventBus'
import { mapState } from 'vuex'
export default {
  name: 'FilterGroup',
  data() {
    return {
      panelInfo: this.$store.state.panel.panelInfo,

      widgetSubjects: {
        '时间过滤组件': [
          'timeYearWidget',
          'timeMonthWidget',
          'timeDateWidget',
          'timeDateRangeWidget'

        ],
        '文本过滤组件': [
          'textSelectWidget',
          'textSelectGridWidget',
          'textInputWidget',
          'textSelectTreeWidget'
        ],
        '数字过滤组件': [
          'numberSelectWidget',
          'numberSelectGridWidget',
          'numberRangeWidget'
        ],
        '按钮': [
          'buttonSureWidget',
          'buttonResetWidget'
        ]
      }
    }
  },
  computed: {
    ...mapState([
      'canvasStyleData',
      'curCanvasScale',
      'componentData'
    ]),
    searchButtonExist() {
      return this.componentData && this.componentData.some(component => component.type === 'custom-button' && component.serviceName === 'buttonSureWidget')
    },
    resetButtonExist() {
      return this.componentData && this.componentData.some(component => component.type === 'custom-button' && component.serviceName === 'buttonResetWidget')
    },
    sureButtonClass() {
      return this.searchButtonExist ? 'button-disable-filter' : 'time-filter'
    },
    resetButtonClass() {
      return (this.searchButtonExist && !this.resetButtonExist) ? 'time-filter' : 'button-disable-filter'
    }

  },

  created() {
    this.init()
  },

  methods: {
    init() {
      for (const key in this.widgetSubjects) {
        const widgetNames = this.widgetSubjects[key]
        this.widgetSubjects[key] = widgetNames.map(widgetName => {
          const widget = ApplicationContext.getService(widgetName)
          const result = { widgetName: widgetName }
          Object.assign(result, widget.getLeftPanel())
          if (this.searchButtonExist && widgetName === 'buttonSureWidget') {
            result.defaultClass = 'button-disable-filter'
          }
          if (!this.searchButtonExist && widgetName === 'buttonResetWidget') {
            result.defaultClass = ' button-disable-filter'
          }
          return result
        })
      }
    },
    handleDragStart(ev) {
      // 记录拖拽信息
      const dragComponentInfo = deepCopy(ApplicationContext.getService(ev.target.dataset.id).getDrawPanel())
      // 设置矩阵标记点
      dragComponentInfo.x = 1
      dragComponentInfo.y = 1
      dragComponentInfo.sizex = Math.round(dragComponentInfo.style.width / this.curCanvasScale.matrixStyleOriginWidth)
      dragComponentInfo.sizey = Math.round(dragComponentInfo.style.height / this.curCanvasScale.matrixStyleOriginHeight)
      dragComponentInfo.auxiliaryMatrix = this.canvasStyleData.auxiliaryMatrix
      dragComponentInfo.moveStatus = 'start'
      this.$store.commit('setDragComponentInfo', dragComponentInfo)
      ev.dataTransfer.effectAllowed = 'copy'
      const dataTrans = {
        type: 'other',
        id: ev.target.dataset.id
      }
      ev.dataTransfer.setData('componentInfo', JSON.stringify(dataTrans))
      eventBus.$emit('startMoveIn')
    },
    handleDragEnd(ev) {
      this.$store.commit('clearDragComponentInfo')
    }
  }
}
</script>

<style lang="scss" scoped>

  .filter-container {
    width: 240px;
    overflow: hidden auto;
    min-height: 24px;
    padding-top: 0px;
    padding-bottom: 0px;
    position: relative;
    // height: 940px;
    max-height: 976px;
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
    filter: progid:DXImageTransform.Microsoft.gradient(startColorstr=#1a3685f2,endColorstr=#1a3685f2);
    font-size: 12px;
    border-radius: 10px;
    cursor: pointer;
    overflow: hidden;
  }

  .button-disable-filter {
    background-color: #ecf5ff;
    .filter-widget-icon {
        color: #8cc5ff;
    }
    .filter-widget-text {
        color: var(--TextActive, #8cc5ff);
    }
  }

  .time-filter {
    background-color: rgba(54,133,242,.1);
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
    background-color: rgba(35,190,239,.1);
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
    background-color: rgba(22,160,132,.1);
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
        background-color: #37b4aa;
        color: #fff;
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

</style>

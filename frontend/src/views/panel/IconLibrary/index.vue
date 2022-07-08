<template>
  <div class="filter-container" @dragstart="handleDragStart" @dragend="handleDragEnd()">
    <div class="widget-subject">
      <div class="filter-header-text"> 图标 </div>
    </div>
    <div class="filter-widget-content">
      <div
        v-for="(item,index) in imgData"
        :key="index"
        :data-id="item.id"
        :data-index="index"
        draggable
        :class="'filter-widget-icon '+ (item.defaultClass || '')"
      >
        <div class="content-Img">
          <img :src="require('@/assets/icon_lib'+item.url)" :alt="item.url" style="width: 120px;">
        </div>
        <div class="content-text">{{ item.url.substring(1) }}</div>
      </div>
    </div>
  </div>
</template>
<script>
import componentList, { iconImgLibrary } from '@/components/canvas/custom-component/component-list'
import { deepCopy } from '@/components/canvas/utils/utils'
import eventBus from '@/components/canvas/utils/eventBus'
import { mapState } from 'vuex'
import { resolve } from 'fit2cloud-ui'
export default {
  name: 'IconLibrary',
  data() {
    return {
      iconImgLibrary,
      imgData: []
    }
  },
  computed: {
    ...mapState([
      'canvasStyleData'
    ])
  },
  mounted() {
    const arr = []
    const files = require.context('@/assets/icon_lib', true, /\.+jpg|jpeg|png$/).keys()
    console.log(files)
    files.forEach((item, index) => {
      arr.push({
        id: '40001',
        url: item.substring(1),
        component: 'de-icon',
        type: 'de-icon',
        defaultClass: 'text-filter'
      })
    })
    console.log(arr)
    this.imgData = arr
  },
  methods: {
    handleDragStart(ev) {
      console.log('======拖拽触发事件', ev, ev.target.dataset.id)
      if (ev.target.dataset.id === undefined) return
      sessionStorage.setItem('iconUrl', ev.target.innerText)
      this.getImgWH(ev.target.innerText)
      this.$store.commit('setDragComponentInfo', this.componentInfo(ev.target.dataset.id))
      ev.dataTransfer.effectAllowed = 'copy'
      const dataTrans = {
        type: 'assist',
        id: ev.target.dataset.id
      }
      ev.dataTransfer.setData('componentInfo', JSON.stringify(dataTrans))
      eventBus.$emit('startMoveIn')
    },
    handleDragEnd(ev) {
      this.$store.commit('clearDragComponentInfo')
    },
    async getImgWH(name) {
      console.log(name)
      const obj = await this.getImgSize(name)
      console.log(obj)
      sessionStorage.setItem('imgWidth', obj.width)
      sessionStorage.setItem('imgHeight', obj.height)
    },
    getImgSize(name) {
      return new Promise((resolve, reject) => {
        const imgObj = new Image()
        imgObj.src = require('@/assets/icon_lib/' + name)
        imgObj.onload = () => {
          console.log(imgObj)
          resolve({
            width: imgObj.width,
            height: imgObj.height
          })
        }
      })
    },
    componentInfo(id) {
      console.log(id)
      // 辅助设计组件
      let component
      console.log('componentList', componentList)
      componentList.forEach(componentTemp => {
        if (id === componentTemp.id) {
          component = deepCopy(componentTemp)
        }
      })
      // 图片移入是 不支持矩阵 暂时无法监听窗口取消事件
      if (component.type !== 'picture-add' && component.type !== 'de-icon') {
        component.auxiliaryMatrix = this.canvasStyleData.auxiliaryMatrix
      } else {
        component.auxiliaryMatrix = false
      }
      component.moveStatus = 'start'
      console.log('图标组件数据::::::', component)
      return component
    }
  }
}
</script>
<style lang="less" scoped>
  .filter-container {
    width: 240px;
    overflow: hidden auto;
    min-height: 24px;
    padding-top: 0px;
    padding-bottom: 0px;
    position: relative;
    height: 940px;
    max-height: 976px;
  }
  .widget-subject {
    display: flow-root;
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

    .content-Img {
      text-align: center;
    }

    .content-text {
      font-size: 14px;
      text-align: center;
      margin-bottom: 10px;
    }
  }

  .text-filter {
    // background-color: rgba(35,190,239,.1);
    .filter-widget-icon {
        color: #23beef;
    }
  }
  .text-filter:hover {
    // background-color: #23beef;
    color: #23beef;
    .filter-widget-icon {
        background-color: #23beef;
        // color: #fff;
        color: #23beef;
    }
  }

</style>

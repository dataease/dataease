<template>
  <div class="home">
    <Toolbar/>
    <main>
      <section class="center">
        <div
          class="content"
          @drop="handleDrop"
          @dragover="handleDragOver"
          @mousedown="handleMouseDown"
          @mouseup="deselectCurComponent"
        >
          <Editor/>
        </div>
      </section>

    </main>
  </div>
</template>

<script>
import Editor from '@/components/Editor/index'
import componentList from '@/components/canvas/custom-component/component-list' // 左侧列表数据
import Toolbar from '@/components/Toolbar'
import {deepCopy} from '@/utils/utils'
import {mapState} from 'vuex'
import generateID from '@/utils/generateID'

export default {
  components: {Editor, Toolbar},
  data() {
    return {
      activeName: 'attr',
      reSelectAnimateIndex: undefined
    }
  },
  computed: mapState([
    'componentData',
    'curComponent',
    'isClickComponent',
    'canvasStyleData'
  ]),
  created() {
    this.restore()
  },
  methods: {
    restore() {
      // 用保存的数据恢复画布
      if (localStorage.getItem('canvasData')) {
        this.$store.commit('setComponentData', this.resetID(JSON.parse(localStorage.getItem('canvasData'))))
      }

      if (localStorage.getItem('canvasStyle')) {
        this.$store.commit('setCanvasStyle', JSON.parse(localStorage.getItem('canvasStyle')))
      }
    },

    resetID(data) {
      if (data) {
        data.forEach(item => {
          item.type !== 'custom' && (item.id = uuid.v1())
        })
      }

      return data
    },

    handleDrop(e) {
      e.preventDefault()
      e.stopPropagation()

      let component
      const id = e.dataTransfer.getData('componentId')
      componentList.forEach(componentTemp => {
        if (id === componentTemp.id) {
          component = deepCopy(componentTemp)
        }
      })
      // const component = deepCopy(componentList[e.dataTransfer.getData('index')])
      component.style.top = e.offsetY
      component.style.left = e.offsetX
      component.id = generateID()
      this.$store.commit('addComponent', {component})
      this.$store.commit('recordSnapshot', 'handleDrop')
    },

    handleDragOver(e) {
      e.preventDefault()
      e.dataTransfer.dropEffect = 'copy'
    },

    handleMouseDown() {
      this.$store.commit('setClickComponentStatus', false)
    },

    deselectCurComponent(e) {
      if (!this.isClickComponent) {
        this.$store.commit('setCurComponent', {component: null, index: null})
      }

      // 0 左击 1 滚轮 2 右击
      if (e.button !== 2) {
        this.$store.commit('hideContextMenu')
      }
    }
  }
}
</script>

<style lang="scss">
.home {
  height: 100vh;
  background: #fff;

  main {
    height: calc(100% - 64px);
    position: relative;

    .left {
      position: absolute;
      height: 100%;
      width: 200px;
      left: 0;
      top: 0;
      padding-top: 10px;
    }

    .right {
      position: absolute;
      height: 100%;
      width: 262px;
      right: 0;
      top: 0;
    }

    .center {
      margin-left: 200px;
      margin-right: 262px;
      background: #f5f5f5;
      height: 100%;
      overflow: auto;
      padding: 20px;

      .content {
        width: 100%;
        height: 100%;
        overflow: auto;
      }
    }
  }

  .placeholder {
    text-align: center;
    color: #333;
  }
}
</style>

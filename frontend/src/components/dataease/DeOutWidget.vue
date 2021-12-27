<template>
  <div ref="myContainer" class="my-container">
    <div ref="conditionMain" class="condition-main" :class="mainClass">
      <div v-if="element.options.attrs.title" ref="deTitleContainer" class="condition-title">
        <div class="condition-title-absolute">
          <div class="first-title">
            <div class="span-container">
              <span ref="deTitle">{{ element.options.attrs.title }}</span>
            </div>
          </div>
        </div>
      </div>
      <div
        ref="deContentContainer"
        class="condition-content"
        :class="element.options.attrs.title ? '' : 'condition-content-default'"
      >
        <div class="condition-content-container">
          <div class="first-element">
            <div
              :class="element.component === 'de-select-grid' ? 'first-element-grid-contaner': ''"
              class="first-element-contaner"
            >

              <component
                :is="element.component"
                v-if="element.type==='custom'"
                :id="'component' + element.id"
                class="component-custom"
                :out-style="element.style"
                :element="element"
                :in-draw="inDraw"
                :in-screen="inScreen"
                :size="sizeInfo"
              />
            </div>
          </div>

        </div>
      </div>
    </div>
  </div>
</template>

<script>
import elementResizeDetectorMaker from 'element-resize-detector'
export default {
  name: 'DeOutWidget',
  props: {
    element: {
      type: Object,
      default: () => {}
    },
    inDraw: {
      type: Boolean,
      default: true
    },
    inScreen: {
      type: Boolean,
      required: false,
      default: true
    },
    h: {
      type: Number,
      default: 50
    }
  },
  data() {
    return {
      inputMaxSize: 46,
      inputLargeSize: 42,
      inputSmallSize: 38,
      inputMiniSize: 34,
      options: null,
      showNumber: false,
      mainClass: ''
    }
  },
  computed: {
    sizeInfo() {
      let size
      if (this.h > this.inputMaxSize) {
        return size
      } else if (this.h > this.inputLargeSize) {
        size = 'medium'
      } else if (this.h > this.inputSmallSize) {
        size = 'small'
      } else {
        size = 'mini'
      }
      return size
    }
  },
  mounted() {
    this.watchSize()
  },
  created() {
    // console.log('aaaaaa')
  },
  methods: {

    watchSize() {
      const erd = elementResizeDetectorMaker()
      erd.listenTo(this.$refs.myContainer, ele => {
        if (!this.element.options.attrs.title) {
          return
        }
        const height = ele.offsetHeight
        const titleWidth = this.$refs.deTitle.offsetWidth
        const deContentContainer = this.$refs.deContentContainer
        this.$nextTick(() => {
          let min = this.element.style.fontSize * 2 + 50
          if (this.element.component === 'de-number-range') {
            min = this.element.style.fontSize * 2 + 80
          }
          if (height < min) {
            this.mainClass = 'condition-main-line'

            if (deContentContainer) {
              deContentContainer.style.top = '0px'
              deContentContainer.style.marginLeft = (titleWidth + 15) + 'px'
            }
          } else {
            this.mainClass = ''
            if (deContentContainer) {
              deContentContainer.style.top = '2em'
              deContentContainer.style.marginLeft = '0px'
            }
          }
        })
      })
    }
  }
}

</script>

<style lang="scss" scoped>
  .my-container {
    position: absolute;
    overflow: auto;
    top: 0px;
    right: 0px;
    bottom: 0px;
    left: 0px;
  }

  .ccondition-main {
    position: absolute;
    overflow: auto;
    top: 0px;
    right: 0px;
    bottom: 0px;
    left: 0px;
  }

  .condition-title {
    top: 0px;
    right: 0px;
    bottom: 0px;
    left: 0px;
    position: absolute;
    height: 2em;
    cursor: -webkit-grab;
  }

  .first-title {
    width: 100%;
    overflow: hidden;
    position: absolute;
    color: inherit;
    display: flex;
    align-items: center;
  }

  .condition-title-absolute {
    right: 0px;
    bottom: 0px;
    position: absolute;
    top: 0px;
    left: 4px;
    display: flex;
    align-items: flex-end;
  }

  .span-container {
    overflow: hidden auto;
    position: relative;
    padding: 0 5px;
  }

  .condition-content {
    overflow: auto hidden;
    top: 2em;
    left: 0px;
    right: 0px;
    bottom: 0px;
    position: absolute;
    letter-spacing: 0px !important;
  }

  .condition-content-container {
    position: relative;
    display: table;
    width: 100%;
    height: 100%;
    white-space: nowrap;
  }

  .first-element {
    position: relative;
    display: table-cell;
    vertical-align: middle;
    margin: 0px;
    padding: 0px;
    height: 100%;
  }

  .first-element-contaner {
    width: calc(100% - 10px);
    background: initial;
    position: absolute;
    bottom: 5px;
    margin: 0 4px;

    div {
      width: 100%;
    }

    display: flex;
    align-items: flex-end;
  }

  .first-element-grid-contaner {
    background: #fff;
    border: 1px solid #d7dae2;
    top: 5px;
  }

  .condition-main-line {
    height: 40px !important;
  }

  .condition-content-default {
    inset: 0px 0px 0px !important;
  }

</style>

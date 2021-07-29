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
      <div ref="deContentContainer" class="condition-content" :class="element.options.attrs.title ? '' : 'condition-content-default'">
        <div class="condition-content-container">
          <div class="first-element">
            <div :class="element.component === 'de-select-grid' ? 'first-element-grid-contaner': ''" class="first-element-contaner">
              <component
                :is="element.component"
                v-if="element.type==='custom'"
                :id="'component' + element.id"
                class="component-custom"
                :out-style="element.style"
                :element="element"
                :in-draw="inDraw"
                :in-screen="inScreen"
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
      default: null
    },
    inDraw: {
      type: Boolean,
      default: true
    },
    inScreen: {
      type: Boolean,
      required: false,
      default: true
    }
  },
  data() {
    return {
      options: null,
      showNumber: false,
      mainClass: ''
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
          if (height < 75) {
            // console.log(titleWidth)
            this.mainClass = 'condition-main-line'
            deContentContainer && (deContentContainer.style.inset = '0 0 0 ' + (titleWidth + 15) + 'px')
          } else {
            this.mainClass = ''
            deContentContainer && (deContentContainer.style.inset = '33px 0px 0px')
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
    inset: 0px;
  }
  .ccondition-main {
    position: absolute;
    overflow: auto;
    inset: 0px;
  }
  .condition-title {
    inset: 0;
    position: absolute;
    height: 35px;
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
    inset: 0px 0px;
    position: absolute;
  }

  .span-container {
    overflow: hidden auto;
    position: relative;
    padding: 0 5px;
  }

  .condition-content {
    overflow: auto hidden;
    inset: 33px 0px 0px;
    position: absolute;
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
      position:absolute;
      bottom: 5px;
      margin: 0 4px;
      div {
          width: 100%;
      }
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

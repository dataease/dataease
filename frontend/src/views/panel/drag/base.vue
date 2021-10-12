<template>
  <div id="demo">
    <!-- <button @click="getList">getList</button> -->
    <div class="head">
      <router-link to="/" class="arrow">&larr;</router-link>
      <span>基本应用</span>
    </div>
    <power-drag
      ref="cyGridster"
      :your-list="myList"
      :base-margin-left="baseMarginLeft"
      :base-margin-top="baseMarginTop"
      :base-width="baseWidth"
      :base-height="baseHeight"
    >
      <!-- <div v-for="(item,index) in myList" :slot="'slot'+index">

            </div> -->
      <div draggable="true" style="width: 200px;height: 200px;background: #1b6d85">
        this is test
      </div>
    </power-drag>
  </div>
</template>

<script>
import drag from '@/components/DeDrag/drag.vue'
import mock from 'mockjs'
import _ from 'lodash'

export default {
  name: 'App',
  components: {
    'power-drag': drag
  },
  data() {
    const list = mock.mock({
      // "myList|10": [{
      //     "id|+1": 1,
      //     x: '@integer(1,5)',
      //     y: '@integer(1,5)',
      //     sizex: '@integer(1,3)',
      //     sizey: '@integer(1,3)',
      // }]
      myList: [{
        'id': 3,
        'x': 6,
        'y': 2,
        'sizex': 1,
        'sizey': 1
      }, {
        'id': 5,
        'x': 4,
        'y': 2,
        'sizex': 2,
        'sizey': 1
      }, {
        'id': 7,
        'x': 1,
        'y': 2,
        'sizex': 1,
        'sizey': 1
      }, {
        'id': 9,
        'x': 7,
        'y': 2,
        'sizex': 1,
        'sizey': 1
      }]
    })
    return {
      myList: list.myList,
      baseWidth: 0,
      baseHeight: 0
    }
  },
  created() {
    // 屏幕适配，使得当前布局能在所有分辨率下适用，示例是在1366*638分辨率下完成
    const screenWidth = window.innerWidth
    const screenHeight = window.innerHeight
    this.baseWidth = 90.8333 * (screenWidth / 1366)
    this.baseHeight = 100 * (screenHeight / 638)
    this.baseMarginLeft = 0
    this.baseMarginTop = 0

    this.$nextTick(function() {
      $('.dragAndResize').css('width', 'calc(100% - ' + (this.baseMarginLeft) + 'px)')
    })
  },
  mounted() {
    const gridster = this.$refs['cyGridster'] // 获取gridster实例
    gridster.init() // 在适当的时候初始化布局组件
  },
  methods: {
    getList() {
      const gridster = this.$refs['cyGridster'] // 获取gridster实例
      console.log(JSON.stringify(gridster.getList()))
    }
  }
}

</script>

<style lang='less' scoped>
    body {
        overflow-x: hidden;
        & * {
            box-sizing: border-box;
        }
    }

    #demo {
        width: 100%;
        padding: 1.5em 0 1.5em 0;

        .head {
            border-bottom: 1px dashed;
            width: 100%;

            padding-left: 20px;

            height: 50px;

            a {
                text-decoration: none;
                color: black;
            }
        }

        .arrow {
            font-size: 20px;

            position: relative;
            margin-right: 10px;
            top: 2px;
        }
    }

</style>

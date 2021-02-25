<template>
<div>
  <div>{{drag?'拖拽中':'拖拽停止'}}</div>
  <!--使用draggable组件-->
  <draggable v-model="myArray" chosenClass="chosen" forceFallback="true" group="people" animation="1000"
             @start="onStart" @end="onEnd">
    <transition-group>
      <div class="item" v-for="element in myArray" :key="element.id">{{element.name}}</div>
    </transition-group>
  </draggable>
</div>
</template>

<script>
import draggable from "vuedraggable";

export default {
  name: "ChartEdit",
  components: {draggable},
  data() {
    return {
      drag:false,
      list1: [
        {name: "John", id: 1},
        {name: "Joao", id: 2},
        {name: "Jean", id: 3},
        {name: "Gerard", id: 4}
      ],
      list2: [
        {name: "Juan", id: 5},
        {name: "Edgard", id: 6},
        {name: "Johnson", id: 7}
      ],
      myArray: [
        {people: 'cn', id: 1, name: 'www.itxst.com'},
        {people: 'cn', id: 2, name: 'www.baidu.com'},
        {people: 'cn', id: 3, name: 'www.taobao.com'},
        {people: 'us', id: 4, name: 'www.google.com'}
      ]
    }
  },
  created() {
  },
  mounted() {
  },
  activated() {
  },
  computed: {},
  methods: {
    //开始拖拽事件
    onStart(){
      this.drag=true;
    },
    //拖拽结束事件
    onEnd() {
      this.drag=false;
    },
  },
  watch: {}
}
</script>

<style scoped>
  .form-item {
    margin-bottom: 6px;
  }

  /*被拖拽对象的样式*/
  .item {
    padding: 6px;
    background-color: #fdfdfd;
    border: solid 1px #eee;
    margin-bottom: 10px;
    cursor: move;
  }

  /*选中样式*/
  .chosen {
    border: solid 2px #3089dc !important;
  }
</style>

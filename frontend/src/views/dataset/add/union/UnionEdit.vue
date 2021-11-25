<template>
  <div v-if="unionParam.type" style="height:400px;">
    <div class="field-style">
      <div class="fields">
        <p>{{ unionParam.parent.currentDs.name }}</p>
        <div style="height: 150px;width: 100%;overflow-y: auto">
          <p v-for="item in parentField" :key="item.id">
            {{ item.name }}
          </p>
        </div>
      </div>
      <div class="fields">
        <p>{{ unionParam.node.currentDs.name }}</p>
      </div>
    </div>
  </div>
</template>

<script>
import { post } from '@/api/dataset/dataset'

export default {
  name: 'UnionEdit',
  props: {
    unionParam: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      parentField: [],
      nodeField: []
    }
  },
  watch: {
    'unionParam': function() {

    }
  },
  mounted() {
    this.getParentFieldList()
    this.getNodeFieldList()
  },
  methods: {
    getParentFieldList() {
      post('/dataset/field/list/' + this.unionParam.parent.currentDs.id, null, true).then(response => {
        this.parentField = JSON.parse(JSON.stringify(response.data)).filter(ele => ele.extField === 0)
      })
    },
    getNodeFieldList() {
      post('/dataset/field/list/' + this.unionParam.node.currentDs.id, null, true).then(response => {
        this.nodeField = JSON.parse(JSON.stringify(response.data)).filter(ele => ele.extField === 0)
      })
    }
  }
}
</script>

<style scoped>
.field-style{
  height: 180px;
}
.fields{
  box-sizing: border-box;
  -moz-box-sizing: border-box; /* Firefox */
  -webkit-box-sizing: border-box; /* Safari */
  width: 50%;
  float: left;
  padding: 0 4px;
}
p{
  font-size: 12px;
}
</style>

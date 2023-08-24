<template>
  <div>
    <p>{{ node.currentDs.name }}</p>
    <union-field-list
      :field-list="fieldList"
      :node="node"
      @checkedFields="changeFields"
    />
  </div>
</template>

<script>
import { post } from '@/api/dataset/dataset'
import UnionFieldList from '@/views/dataset/add/union/UnionFieldList'

export default {
  name: 'UnionFieldEdit',
  components: { UnionFieldList },
  props: {
    node: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      fieldList: []
    }
  },
  mounted() {
    this.getFieldList()
  },
  methods: {
    getFieldList() {
      post('/dataset/field/list/' + this.node.currentDs.id, null, true).then(
        (response) => {
          this.fieldList = JSON.parse(JSON.stringify(response.data)).filter(
            (ele) => ele.extField === 0
          )
        }
      )
    },
    changeFields(val) {
      this.node.currentDsField = val
    }
  }
}
</script>

<style scoped>
</style>

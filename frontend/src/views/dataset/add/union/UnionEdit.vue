<template>
  <div
    v-if="unionParam.type"
    style="height: calc(100% - 55px); overflow-y: auto"
  >
    <div class="field-style">
      <div class="fields">
        <p :title="unionParam.parent.currentDs.name">
          {{ unionParam.parent.currentDs.name }}
        </p>
        <union-field-list
          :field-list="parentField"
          :node="unionParam.parent"
          @checkedFields="changeParentFields"
        />
      </div>
      <div class="fields">
        <p :title="unionParam.parent.currentDs.name">
          {{ unionParam.node.currentDs.name }}
        </p>
        <union-field-list
          :field-list="nodeField"
          :node="unionParam.node"
          @checkedFields="changeNodeFields"
        />
      </div>
    </div>
    <union-item-edit
      :parent-field-list="parentField"
      :node-field-list="nodeField"
      :union-param="unionParam"
    />
  </div>
</template>

<script>
import { post } from '@/api/dataset/dataset'
import UnionFieldList from '@/views/dataset/add/union/UnionFieldList'
import UnionItemEdit from '@/views/dataset/add/union/UnionItemEdit'

export default {
  name: 'UnionEdit',
  components: { UnionItemEdit, UnionFieldList },
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
    unionParam: function() {}
  },
  mounted() {
    this.getParentFieldList()
    this.getNodeFieldList()
  },
  methods: {
    getParentFieldList() {
      post(
        '/dataset/field/list/' + this.unionParam.parent.currentDs.id,
        null,
        true
      ).then((response) => {
        this.parentField = JSON.parse(JSON.stringify(response.data)).filter(
          (ele) => ele.extField === 0
        )
      })
    },
    getNodeFieldList() {
      post(
        '/dataset/field/list/' + this.unionParam.node.currentDs.id,
        null,
        true
      ).then((response) => {
        this.nodeField = JSON.parse(JSON.stringify(response.data)).filter(
          (ele) => ele.extField === 0
        )
      })
    },

    changeParentFields(val) {
      this.unionParam.parent.currentDsField = val
    },
    changeNodeFields(val) {
      this.unionParam.node.currentDsField = val
    }
  }
}
</script>

<style lang="scss" scoped>
.field-style {
  height: 430px;
  border: 1px solid var(--deCardStrokeColor, #dee0e3);
  border-radius: 4px;
  width: 100%;
  box-sizing: border-box;
  display: flex;
  margin-bottom: 36px;
}
.fields {
  box-sizing: border-box;
  padding: 16px;
  width: 50%;

  & > p {
    font-family: PingFang SC;
    font-size: 14px;
    font-weight: 500;
    margin: 0;
    margin-bottom: 16px;
    text-overflow: ellipsis;
    overflow: hidden;
    white-space: nowrap;
    width: 100%;
    color: var(--deTextPrimary, #1f2329);
  }
  &:nth-child(1) {
    border-right: 1px solid var(--deCardStrokeColor, #dee0e3);
  }
}
</style>

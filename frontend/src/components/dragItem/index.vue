<template>
  <span class="">
    <el-tag
      size="small"
      closable
      class="item-axis"
      @close="removeItem"
    >
      {{ item.name }}
    </el-tag>
  </span>
</template>

<script>
export default {
  name: 'DragItem',
  props: {
    item: {
      type: Object,
      required: true
    },
    index: {
      type: Number,
      required: true
    }

  },
  data() {
    return {

    }
  },
  computed: {

  },
  watch: {
    /* index(val, old) {
      if (val !== old) {
        this.sortChange('none')
      }
    } */
  },
  mounted() {
  },
  created() {

  },
  methods: {

    removeItem() {
      this.item.index = this.index
      this.$emit('closeItem', this.item)
    }

  }
}
</script>

<style scoped lang="scss">
.item-axis {
  padding: 1px 6px;
  margin: 0 3px 2px 3px;
  text-align: left;
  height: 24px;
  line-height: 22px;
  display: inline-block;
  border-radius: 4px;
  box-sizing: border-box;
  white-space: nowrap;
}

.item-axis:hover {
  background-color: #fdfdfd;
  cursor: pointer;
}

span {
  font-size: 12px;
}

</style>

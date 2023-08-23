<template>
  <div class="rowAuth">
    <rowAuthTree
      :relation-list="relationList"
      :logic.sync="logic"
      @del="(idx) => del(idx)"
      @addCondReal="addCondReal"
      @removeRelationList="removeRelationList"
      @changeAndOrDfs="(type) => changeAndOrDfs(relationList, type)"
    />
    <svg
      width="388"
      height="100%"
      class="real-line"
    >
      <path
        stroke-linejoin="round"
        stroke-linecap="round"
        :d="svgRealinePath"
        fill="none"
        stroke="#CCCCCC"
        stroke-width="0.5"
      />
    </svg>
    <svg
      width="388"
      height="100%"
      class="dash-line"
    >
      <path
        stroke-linejoin="round"
        stroke-linecap="round"
        :d="svgDashinePath"
        fill="none"
        stroke="#CCCCCC"
        stroke-width="0.5"
        stroke-dasharray="4,4"
      />
    </svg>
  </div>
</template>

<script>
import rowAuthTree from './rowAuthTree.vue'

export default {
  name: 'RowAuth',
  components: {
    rowAuthTree
  },
  data() {
    return {
      relationList: [],
      logic: 'or',
      errorMessage: ''
    }
  },
  computed: {
    svgRealinePath() {
      const lg = this.relationList.length
      const a = { x: 0, y: 0, child: this.relationList }
      a.y = Math.floor(this.dfsXY(a, 0) / 2)
      if (!lg) return ''
      const path = this.calculateDepth(a)
      return path
    },
    svgDashinePath() {
      const lg = this.relationList.length
      const a = { x: 0, y: 0, child: this.relationList }
      a.y = Math.floor(this.dfsXY(a, 0) / 2)
      if (!lg) return `M48 20 L68 20`
      const path = this.calculateDepthDash(a)
      return path
    }
  },
  methods: {
    init(expressionTree) {
      const { logic = 'or', items = [] } = expressionTree
      this.logic = logic
      this.relationList = this.dfsInit(items)
    },
    submit() {
      this.errorMessage = ''
      return {
        logic: this.logic,
        items: this.dfsSubmit(this.relationList),
        errorMessage: this.errorMessage
      }
    },
    errorDetected({ enumValue, deType, filterType, term, value }) {
      if (filterType === 'logic') {
        if (
          !term.includes('null') &&
          !term.includes('empty') &&
          (value === '')
        ) {
          this.errorMessage = this.$t('chart.filter_value_can_null')
          return
        }
        if ([2, 3].includes(deType)) {
          if (parseFloat(value).toString() === 'NaN') {
            this.errorMessage = this.$t('chart.filter_value_can_not_str')
            return
          }
        }
      }
      if (filterType === 'enum') {
        if (enumValue.length < 1) {
          this.errorMessage = this.$t('chart.enum_value_can_not_null')
          return
        }
      }
    },
    dfsInit(arr) {
      const elementList = []
      arr.forEach((ele) => {
        const { subTree } = ele
        if (subTree) {
          const { items, logic } = subTree
          const child = this.dfsInit(items)
          elementList.push({ logic, child })
        } else {
          const {
            enumValue,
            fieldId,
            filterType,
            term,
            value,
            field
          } = ele
          const { name, deType } = (field || {})
          elementList.push({
            enumValue: enumValue.join(','),
            fieldId,
            filterType,
            term,
            value,
            name,
            deType
          })
        }
      })
      return elementList
    },
    dfsSubmit(arr) {
      const items = []
      arr.forEach((ele) => {
        const { child = [] } = ele
        if (child.length) {
          const { logic } = ele
          const subTree = this.dfsSubmit(child)
          items.push({
            enumValue: [],
            fieldId: '',
            filterType: '',
            term: '',
            type: 'tree',
            value: '', subTree: { logic, items: subTree }})
        } else {
          const { enumValue, fieldId, filterType, deType, term, value } = ele
          this.errorDetected({ deType, enumValue, filterType, term, value })
          if (fieldId) {
            items.push({
              enumValue: enumValue ? enumValue.split(',') : [],
              fieldId,
              filterType,
              term,
              value,
              type: 'item',
              subTree: null
            })
          }
        }
      })
      return items
    },
    removeRelationList() {
      this.relationList = []
    },
    getY(arr) {
      const [a] = arr
      if (a.child?.length) {
        return this.getY(a.child)
      }
      return a.y
    },
    getLastY(arr) {
      const a = arr[arr.length]
      if (a.child?.length) {
        return this.getLastY(a.child)
      }
      return a.y
    },
    calculateDepthDash(obj) {
      const lg = obj.child?.length
      let path = ''
      if (!lg && Array.isArray(obj.child)) {
        const { x, y } = obj
        path += `M${48 + x * 68} ${y * 41.4 + 20} L${88 + x * 68} ${
          y * 41.4 + 20
        }`
      } else if (obj.child?.length) {
        const y = Math.max(
          this.dfsY(obj, 0),
          this.dfs(obj.child, 0) + this.getY(obj.child) - 1
        )
        const parent =
          (this.dfs(obj.child, 0) * 41.4) / 2 +
          (this.getY(obj.child) || 0) * 41.4
        const { x } = obj
        path += `M${24 + x * 68} ${parent} L${24 + x * 68} ${y * 41.4 + 20} L${
          64 + x * 68
        } ${y * 41.4 + 20}`
        obj.child.forEach((item) => {
          path += this.calculateDepthDash(item)
        })
      }

      return path
    },
    calculateDepth(obj) {
      const lg = obj.child.length
      if (!lg) return ''
      let path = ''
      const { x: depth, y } = obj
      obj.child.forEach((item, index) => {
        const { y: sibingLg, z } = item
        if (item.child?.length) {
          const parent =
            (this.dfs(obj.child, 0) * 41.4) / 2 +
            (this.getY(obj.child) || 0) * 41.4
          const children =
            (this.dfs(item.child, 0) * 41.4) / 2 + this.getY(item.child) * 41.4
          let path1 = 0
          let path2 = 0
          if (parent < children) {
            path1 = parent
            path2 = children
          } else {
            [path1, path2] = [children, parent]
          }
          if (y >= sibingLg) {
            path1 = parent
            path2 = children
          }
          path += `M${24 + depth * 68} ${path1} L${24 + depth * 68} ${path2} L${
            68 + depth * 68
          } ${path2}`
          path += this.calculateDepth(item)
        }
        if (!item.child?.length) {
          if (sibingLg >= y) {
            path += `M${24 + depth * 68} ${y * 40} L${24 + depth * 68} ${
              (sibingLg + 1) * 41.4 - 20.69921875
            } L${68 + depth * 68} ${(sibingLg + 1) * 41.4 - 20.69921875}`
          } else {
            path += `M${24 + depth * 68} ${
              (sibingLg +
                (lg === 1 && index === 0 ? 0 : 1) +
                (obj.child[index + 1]?.child?.length ? y - sibingLg - 1 : 0)) *
                41.4 +
              20 +
              (lg === 1 && index === 0 ? 26 : 0)
            } L${24 + depth * 68} ${
              (sibingLg + 1) * 41.4 -
              20.69921875 -
              (lg === 1 && index === 0 ? (z || 0) * 1.4 : 0)
            } L${68 + depth * 68} ${
              (sibingLg + 1) * 41.4 -
              20.69921875 -
              (lg === 1 && index === 0 ? (z || 0) * 1.4 : 0)
            }`
          }
        }
      })
      return path
    },
    changeAndOrDfs(arr, logic) {
      arr.forEach((ele) => {
        if (ele.child) {
          ele.logic = logic === 'and' ? 'or' : 'and'
          this.changeAndOrDfs(ele.child, ele.logic)
        }
      })
    },
    dfs(arr, count) {
      arr.forEach((ele) => {
        if (ele.child?.length) {
          count = this.dfs(ele.child, count)
        } else {
          count += 1
        }
      })
      count += 1
      return count
    },
    dfsY(obj, count) {
      obj.child.forEach((ele) => {
        if (ele.child?.length) {
          count = this.dfsY(ele, count)
        } else {
          count = Math.max(count, ele.y, obj.y)
        }
      })
      return count
    },
    dfsXY(obj, count) {
      obj.child.forEach((ele) => {
        ele.x = obj.x + 1
        if (ele.child?.length) {
          const l = this.dfs(ele.child, 0)
          ele.y = Math.floor(l / 2) + count
          count = this.dfsXY(ele, count)
        } else {
          count += 1
          ele.y = count - 1
        }
      })
      count += 1
      return count
    },
    addCondReal(type, logic) {
      this.relationList.push(
        type === 'condition'
          ? { fieldId: '', value: '', enumValue: '', term: '', filterType: 'logic', name: '', deType: '' }
          : { child: [], logic }
      )
    },
    del(index) {
      this.relationList.splice(index, 1)
    }
  }
}
</script>

<style>
.rowAuth {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  text-align: center;
  color: #2c3e50;
  position: relative;
}
.real-line,
.dash-line {
  position: absolute;
  top: 0;
  left: 0;
  user-select: none;
}
</style>

<template>
  <ul
    class="el-pager"
    @click="onPagerClick"
  >
    <li
      v-if="pageCount > 0"
      :class="{ active: currentPage === 1, disabled }"
      :style="customStyle"
      class="number"
    >1</li>
    <li
      v-if="showPrevMore"
      class="el-icon more btn-quickprev"
      :class="[quickprevIconClass, { disabled }]"
      :style="customStyle"
      @mouseenter="onMouseenter('left')"
      @mouseleave="quickprevIconClass = 'el-icon-more'"
    />
    <li
      v-for="pager in pagers"
      :key="pager"
      :style="{ color: currentPage === pager? '#409eff' : customStyle.color }"
      :class="{ active: currentPage === pager, disabled }"
      class="number"
    >{{ pager }}</li>
    <li
      v-if="showNextMore"
      class="el-icon more btn-quicknext"
      :class="[quicknextIconClass, { disabled }]"
      :style="customStyle"
      @mouseenter="onMouseenter('right')"
      @mouseleave="quicknextIconClass = 'el-icon-more'"
    />
    <li
      v-if="pageCount > 1"
      :class="{ active: currentPage === pageCount, disabled }"
      :style="customStyle"
      class="number"
    >{{ pageCount }}</li>
  </ul>
</template>

<script type="text/babel">
export default {
  name: 'ElPager',

  props: {
    currentPage: Number,

    pageCount: Number,

    pagerCount: Number,
    customStyle: {
      default: () => {},
      type: Object
    },

    disabled: Boolean
  },

  data() {
    return {
      current: null,
      showPrevMore: false,
      showNextMore: false,
      quicknextIconClass: 'el-icon-more',
      quickprevIconClass: 'el-icon-more'
    }
  },

  computed: {
    pagers() {
      const pagerCount = this.pagerCount
      const halfPagerCount = (pagerCount - 1) / 2

      const currentPage = Number(this.currentPage)
      const pageCount = Number(this.pageCount)

      let showPrevMore = false
      let showNextMore = false

      if (pageCount > pagerCount) {
        if (currentPage > pagerCount - halfPagerCount) {
          showPrevMore = true
        }

        if (currentPage < pageCount - halfPagerCount) {
          showNextMore = true
        }
      }

      const array = []

      if (showPrevMore && !showNextMore) {
        const startPage = pageCount - (pagerCount - 2)
        for (let i = startPage; i < pageCount; i++) {
          array.push(i)
        }
      } else if (!showPrevMore && showNextMore) {
        for (let i = 2; i < pagerCount; i++) {
          array.push(i)
        }
      } else if (showPrevMore && showNextMore) {
        const offset = Math.floor(pagerCount / 2) - 1
        for (let i = currentPage - offset; i <= currentPage + offset; i++) {
          array.push(i)
        }
      } else {
        for (let i = 2; i < pageCount; i++) {
          array.push(i)
        }
      }

      // eslint-disable-next-line
      this.showPrevMore = showPrevMore
      // eslint-disable-next-line
      this.showNextMore = showNextMore

      return array
    }
  },

  watch: {
    showPrevMore(val) {
      if (!val) this.quickprevIconClass = 'el-icon-more'
    },

    showNextMore(val) {
      if (!val) this.quicknextIconClass = 'el-icon-more'
    }
  },

  methods: {
    onPagerClick(event) {
      const target = event.target
      if (target.tagName === 'UL' || this.disabled) {
        return
      }

      let newPage = Number(event.target.textContent)
      const pageCount = this.pageCount
      const currentPage = this.currentPage
      const pagerCountOffset = this.pagerCount - 2

      if (target.className.indexOf('more') !== -1) {
        if (target.className.indexOf('quickprev') !== -1) {
          newPage = currentPage - pagerCountOffset
        } else if (target.className.indexOf('quicknext') !== -1) {
          newPage = currentPage + pagerCountOffset
        }
      }

      /* istanbul ignore if */
      if (!isNaN(newPage)) {
        if (newPage < 1) {
          newPage = 1
        }

        if (newPage > pageCount) {
          newPage = pageCount
        }
      }

      if (newPage !== currentPage) {
        this.$emit('change', newPage)
      }
    },

    onMouseenter(direction) {
      if (this.disabled) return
      if (direction === 'left') {
        this.quickprevIconClass = 'el-icon-d-arrow-left'
      } else {
        this.quicknextIconClass = 'el-icon-d-arrow-right'
      }
    }
  }
}
</script>

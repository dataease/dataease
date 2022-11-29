import Pager from './DePager.vue'

export default {
  name: 'DePagination',

  props: {
    pageSize: {
      type: Number,
      default: 10
    },

    customStyle: {
      default: () => {},
      type: Object
    },

    small: Boolean,

    total: Number,

    pageCount: Number,

    pagerCount: {
      type: Number,
      validator(value) {
        return (value | 0) === value && value > 4 && value < 22 && (value % 2) === 1
      },
      default: 7
    },

    currentPage: {
      type: Number,
      default: 1
    },

    layout: {
      default: 'prev, pager, next'
    },

    popperClass: String,

    prevText: String,

    nextText: String,

    background: Boolean,

    disabled: Boolean,

    hideOnSinglePage: Boolean
  },

  data() {
    return {
      internalCurrentPage: 1,
      internalPageSize: 0,
      lastEmittedPage: -1,
      userChangePageSize: false
    }
  },

  render(h) {
    const layout = this.layout
    if (!layout) return null
    if (this.hideOnSinglePage && (!this.internalPageCount || this.internalPageCount === 1)) return null

    const template = <div class={['el-pagination', {
      'is-background': this.background,
      'el-pagination--small': this.small
    }] }></div>
    const TEMPLATE_MAP = {
      prev: <prev customStyle={ this.customStyle } ></prev>,
      pager: <pager currentPage={ this.internalCurrentPage } customStyle={ this.customStyle } pageCount={ this.internalPageCount } pagerCount={ this.pagerCount } on-change={ this.handleCurrentChange } disabled={ this.disabled }></pager>,
      next: <next customStyle={ this.customStyle } ></next>
    }
    const components = layout.split(',').map((item) => item.trim())

    template.children = template.children || []
    components.forEach(compo => {
      template.children.push(TEMPLATE_MAP[compo])
    })
    return template
  },

  components: {
    Prev: {
      props: {
        customStyle: {
          default: () => {},
          type: Object
        }
      },
      render(h) {
        return (
          <button
            type='button'
            class='btn-prev'
            disabled={ this.$parent.disabled || this.$parent.internalCurrentPage <= 1 }
            on-click={ this.$parent.prev }>
            {
              this.$parent.prevText
                ? <span>{ this.$parent.prevText }</span>
                : <i style={ this.customStyle } class='el-icon el-icon-arrow-left'></i>
            }
          </button>
        )
      }
    },

    Next: {
      props: {
        customStyle: {
          default: () => {},
          type: Object
        }
      },
      render(h) {
        return (
          <button
            type='button'
            class='btn-next'
            disabled={ this.$parent.disabled || this.$parent.internalCurrentPage === this.$parent.internalPageCount || this.$parent.internalPageCount === 0 }
            on-click={ this.$parent.next }>
            {
              this.$parent.nextText
                ? <span>{ this.$parent.nextText }</span>
                : <i style={ this.customStyle } class='el-icon el-icon-arrow-right'></i>
            }
          </button>
        )
      }
    },
    Pager
  },

  methods: {
    handleCurrentChange(val) {
      this.internalCurrentPage = this.getValidCurrentPage(val)
      this.userChangePageSize = true
      this.emitChange()
    },

    prev() {
      if (this.disabled) return
      const newVal = this.internalCurrentPage - 1
      this.internalCurrentPage = this.getValidCurrentPage(newVal)
      this.$emit('prev-click', this.internalCurrentPage)
      this.emitChange()
    },

    next() {
      if (this.disabled) return
      const newVal = this.internalCurrentPage + 1
      this.internalCurrentPage = this.getValidCurrentPage(newVal)
      this.$emit('next-click', this.internalCurrentPage)
      this.emitChange()
    },

    getValidCurrentPage(value) {
      value = parseInt(value, 10)

      const havePageCount = typeof this.internalPageCount === 'number'

      let resetValue
      if (!havePageCount) {
        if (isNaN(value) || value < 1) resetValue = 1
      } else {
        if (value < 1) {
          resetValue = 1
        } else if (value > this.internalPageCount) {
          resetValue = this.internalPageCount
        }
      }

      if (resetValue === undefined && isNaN(value)) {
        resetValue = 1
      } else if (resetValue === 0) {
        resetValue = 1
      }

      return resetValue === undefined ? value : resetValue
    },

    emitChange() {
      this.$nextTick(() => {
        if (this.internalCurrentPage !== this.lastEmittedPage || this.userChangePageSize) {
          this.$emit('current-change', this.internalCurrentPage)
          this.lastEmittedPage = this.internalCurrentPage
          this.userChangePageSize = false
        }
      })
    }
  },

  computed: {
    internalPageCount() {
      if (typeof this.total === 'number') {
        return Math.max(1, Math.ceil(this.total / this.internalPageSize))
      } else if (typeof this.pageCount === 'number') {
        return Math.max(1, this.pageCount)
      }
      return null
    }
  },

  watch: {
    currentPage: {
      immediate: true,
      handler(val) {
        this.internalCurrentPage = this.getValidCurrentPage(val)
      }
    },

    pageSize: {
      immediate: true,
      handler(val) {
        this.internalPageSize = isNaN(val) ? 10 : val
      }
    },

    internalCurrentPage: {
      immediate: true,
      handler(newVal) {
        this.$emit('update:currentPage', newVal)
        this.lastEmittedPage = -1
      }
    },

    internalPageCount(newVal) {
      /* istanbul ignore if */
      const oldPage = this.internalCurrentPage
      if (newVal > 0 && oldPage === 0) {
        this.internalCurrentPage = 1
      } else if (oldPage > newVal) {
        this.internalCurrentPage = newVal === 0 ? 1 : newVal
        this.userChangePageSize && this.emitChange()
      }
      this.userChangePageSize = false
    }
  }
}

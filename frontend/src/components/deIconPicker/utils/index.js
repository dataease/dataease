(function(e, d, w) {
  if (!e.composedPath) {
    e.composedPath = function() {
      if (this.path) {
        return this.path
      }
      let target = this.target

      this.path = []
      while (target.parentNode !== null) {
        this.path.push(target)
        target = target.parentNode
      }
      this.path.push(d, w)
      return this.path
    }
  }
  if (!String.prototype.startsWith) {
    // eslint-disable-next-line no-extend-native
    String.prototype.startsWith = function(search, pos) {
      return this.substr(!pos || pos < 0 ? 0 : +pos, search.length) === search
    }
  }
})(Event.prototype, document, window)

export * from './util'
export * from './dom'
export * from './TypeUtil'

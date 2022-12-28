
export default {
  methods: {
    openMessageSuccess(text, type) {
      const h = this.$createElement
      const iconClass = `el-icon-${type || 'success'}`
      const customClass = `de-message-${type || 'success'} de-message`
      this.$message({
        message: h('p', null, [h('span', null, this.$t(text))]),
        iconClass,
        customClass
      })
    },
    handlerConfirm(options, confirmButtonTextInfo) {
      const { title, content, type = 'danger', cb, confirmButtonText = confirmButtonTextInfo || this.$t('commons.delete'), showCancelButton = true, cancelButtonText = this.$t('commons.cancel'), cancelCb = () => {}, finallyCb = () => {} } = options
      const text = content ? `<span>${this.$t(title)}</span><br><span class="use-html">${this.$t(content)}</span>` : this.$t(title)
      const dangerouslyUseHTMLString = Boolean(content)
      const customClass = `de-confirm de-confirm-fail ${dangerouslyUseHTMLString && 'de-use-html'}`
      const confirmButtonClass = `de-confirm-${type}-btn de-confirm-btn`
      this.$confirm(
        text,
        '',
        {
          confirmButtonText,
          cancelButtonText,
          showCancelButton,
          cancelButtonClass: 'de-confirm-btn de-confirm-plain-cancel',
          dangerouslyUseHTMLString,
          confirmButtonClass,
          customClass,
          iconClass: 'el-icon-warning'
        }
      )
        .then(() => {
          cb()
        })
        .catch((action) => {
          cancelCb(action)
        })
        .finally(() => {
          finallyCb()
        })
    },
    withLink(options, confirmButtonTextInfo) {
      const h = this.$createElement;
      const that = this
      const { title, content, type = 'danger', cb, confirmButtonText = confirmButtonTextInfo || this.$t('commons.delete'), showCancelButton = true, cancelButtonText = this.$t('commons.cancel'), cancelCb = () => {}, finallyCb = () => {}, link = '', templateDel, linkTo } = options
      const customClass = `de-confirm de-confirm-fail de-use-html`
      const confirmButtonClass = `de-confirm-${type}-btn de-confirm-btn`
      this.$msgbox({
        message: h('p', null, [
          h(templateDel, {
            props: {
              someProp: {
                title,
                content,
                link
              },
            },
            on: {
              change: () => {
                linkTo()
              }
            }
          }),
        ]),
        duration: 0,
        confirmButtonText,
        cancelButtonText,
        showCancelButton,
        cancelButtonClass: 'de-confirm-btn de-confirm-plain-cancel',
        confirmButtonClass,
        customClass,
        iconClass: 'el-icon-warning',
      }).then(action => {
        if ('confirm' === action) {
          cb()
        }
      })
      .catch((action) => {
        cancelCb(action)
      })
      .finally(() => {
        finallyCb()
      })
    }
  }
}

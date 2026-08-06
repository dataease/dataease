import { type Editor } from 'tinymce'

const LETTERSPACING_ICON =
  '<svg t="1610616201691" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" width="24" height="24">' +
  '<path d="M682.666667 704l128 106.666667-128 106.666666v-85.333333H341.333333v85.333333L213.333333 810.666667l128-106.666667v85.333333h341.333334v-85.333333z' +
  'M170.666667 170.666667v682.666666H85.333333V170.666667h85.333334z m768 0v682.666666h-85.333334V170.666667h85.333334z' +
  'm-394.666667 0l202.666667 469.333333h-89.6l-38.4-93.866667h-213.333334L366.933333 640H277.333333l202.666667-469.333333h64z' +
  'M512 255.146667L432.213333 469.333333h159.573334L512 255.146667z" fill="#222f3e"></path>' +
  '</svg>'

const DEFAULT_SPACING_VALUES = '0px 1px 2px 4px 6px 8px 10px 20px 40px'

export default {
  name: 'letterspacing',
  plugin: function (editor: Editor) {
    // TinyMCE 6: register custom option via editor.options.register()
    editor.options.register('letterspacing', {
      processor: 'string',
      default: DEFAULT_SPACING_VALUES
    })

    editor.on('init', function () {
      editor.formatter.register({
        letterspacing: {
          inline: 'span',
          toggle: false,
          styles: { 'letter-spacing': '%value' },
          clear_child_styles: true
        }
      })
    })

    if (!editor.ui.registry.getAll().icons['letterspacing']) {
      editor.ui.registry.addIcon('letterspacing', LETTERSPACING_ICON)
    }

    editor.ui.registry.addMenuButton('letterspacing', {
      icon: 'letterspacing',
      tooltip: '设置间距',
      fetch: callback => {
        const spacingValues = editor.options.get('letterspacing') as string
        const items = spacingValues.split(' ').map(value => ({
          type: 'togglemenuitem' as const,
          text: value,
          onAction: () => {
            editor.focus()
            editor.formatter.apply('letterspacing', { value })
          }
        }))
        callback(items)
      }
    })

    return {
      getMetadata: function () {
        return {
          name: '设置间距',
          url: 'https://github.com/Five-great/tinymce-plugins'
        }
      }
    }
  }
}

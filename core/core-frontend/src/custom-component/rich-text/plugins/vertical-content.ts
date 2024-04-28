import { type Editor } from 'tinymce'
import tinymce from 'tinymce/tinymce'
import { useEmitt } from '@/hooks/web/useEmitt'
const { emitter } = useEmitt()
const TOP_ALIGN_BTN =
  '<svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">' +
  '<path d="M2.5 3C2.22386 3 2 3.22386 2 3.5V4.5C2 4.77614 2.22386 5 2.5 5H21.5C21.7761 5 22 4.77614 22 4.5V3.5C22 3.22386 21.7761 3 21.5 3H2.5Z" fill="currentColor"/>' +
  '<path d="M6.19133 14.4465L11 9.63788V21.467C11 21.7615 11.2388 22.0003 11.5333 22.0003H12.4667C12.7612 22.0003 13 21.7615 13 21.467V9.61452L17.832 14.4465C18.0403 14.6548 18.378 14.6548 18.5863 14.4465L19.2462 13.7866C19.4545 13.5783 19.4545 13.2406 19.2462 13.0323L12.458 6.2441C12.3362 6.12232 12.1702 6.07174 12.0117 6.09237C11.8531 6.07174 11.6871 6.12232 11.5653 6.2441L4.77712 13.0323C4.56884 13.2406 4.56884 13.5783 4.77712 13.7866L5.43709 14.4465C5.64537 14.6548 5.98305 14.6548 6.19133 14.4465Z" fill="currentColor"/>' +
  '</svg>'
const CENTER_ALIGN_BTN =
  '<svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">' +
  '<path d="M11 6.36207L9.19133 4.5534C8.98305 4.34513 8.64537 4.34513 8.43709 4.55341L7.77712 5.21337C7.56884 5.42165 7.56884 5.75934 7.77712 5.96762L11.5653 9.75584C11.6871 9.87762 11.8531 9.9282 12.0117 9.90757C12.1702 9.9282 12.3362 9.87762 12.458 9.75584L16.2462 5.96762C16.4545 5.75934 16.4545 5.42165 16.2462 5.21337L15.5863 4.55341C15.378 4.34513 15.0403 4.34513 14.832 4.5534L13 6.38542V1.53297C13 1.23841 12.7612 0.999634 12.4667 0.999634H11.5333C11.2388 0.999634 11 1.23841 11 1.53297V6.36207Z" fill="currentColor"/>' +
  '<path d="M11 17.5499L9.19133 19.3586C8.98305 19.5669 8.64537 19.5669 8.43709 19.3586L7.77712 18.6986C7.56884 18.4903 7.56884 18.1527 7.77712 17.9444L11.5653 14.1562C11.6871 14.0344 11.8531 13.9838 12.0117 14.0044C12.1702 13.9838 12.3362 14.0344 12.458 14.1562L16.2462 17.9444C16.4545 18.1527 16.4545 18.4903 16.2462 18.6986L15.5863 19.3586C15.378 19.5669 15.0403 19.5669 14.832 19.3586L13 17.5266V22.379C13 22.6736 12.7612 22.9124 12.4667 22.9124H11.5333C11.2388 22.9124 11 22.6736 11 22.379V17.5499Z" fill="currentColor"/>' +
  '<path d="M2.5 10.9999C2.22386 10.9999 2 11.2238 2 11.4999V12.4999C2 12.7761 2.22386 12.9999 2.5 12.9999H21.5C21.7761 12.9999 22 12.7761 22 12.4999V11.4999C22 11.2238 21.7761 10.9999 21.5 10.9999H2.5Z" fill="currentColor"/>' +
  '</svg>'
const BOTTOM_ALIGN_BTN =
  '<svg t="1713518245725" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="17410" width="24" height="24">' +
  '<path d="M508.842667 42.666667a42.666667 42.666667 0 0 1 42.666666 42.666666V725.333333a42.666667 42.666667 0 0 1-85.333333 0l0-640a42.666667 42.666667 0 0 1 42.666667-42.666666z" fill="#373C43" p-id="17411"></path><path d="M502.314667 707.84l275.541333-266.069333a42.666667 42.666667 0 1 1 59.306667 61.44L531.029333 798.634667a42.538667 42.538667 0 0 1-60.16-0.853334l-291.242666-298.666666a42.666667 42.666667 0 0 1 61.098666-59.605334l261.589334 268.245334z" fill="#373C43" p-id="17412"></path><path d="M935.509333 981.333333m-42.666666 0l-768 0q-42.666667 0-42.666667-42.666666l0 0q0-42.666667 42.666667-42.666667l768 0q42.666667 0 42.666666 42.666667l0 0q0 42.666667-42.666666 42.666666Z" fill="#8D9399" p-id="17413">' +
  '</path>' +
  '</svg>'
const pack = tinymce.IconManager.has('vertical-content')
if (!pack) {
  tinymce.IconManager.add('vertical-content', {
    icons: {
      'top-align': TOP_ALIGN_BTN,
      'center-align': CENTER_ALIGN_BTN,
      'bottom-align': BOTTOM_ALIGN_BTN
    }
  })
}

export default {
  name: 'vertical-content',
  plugin: function (editor: Editor) {
    const wrapperDom = editor.targetElm
    const verticalAlign = editor.settings.vertical_align
    const btnMap = {
      'top-align': {
        component: null,
        tooltip: '置顶'
      },
      'center-align': {
        component: null,
        tooltip: '居中,只在内容未溢出时生效'
      },
      'bottom-align': {
        component: null,
        tooltip: '置底,只在内容未溢出时生效'
      }
    }
    for (const key in btnMap) {
      editor.ui.registry.addToggleButton(key, {
        icon: key,
        tooltip: btnMap[key].tooltip,
        onAction: api => {
          for (const btnKey in btnMap) {
            if (btnKey === key) {
              // 反选清空样式
              const align = api.isActive() ? '' : key
              emitter.emit('vertical-change-' + wrapperDom.id, align)
              btnMap[key].component.setActive(!api.isActive())
            } else {
              const active = btnMap[btnKey].component.isActive()
              if (active) {
                btnMap[btnKey].component.setActive(false)
              }
            }
          }
        },
        onSetup(a) {
          if (verticalAlign === key) {
            a.setActive(true)
          }
          return api => (btnMap[key].component = api)
        }
      })
    }
    return {
      getMetadata: function () {
        return {
          name: 'Vertical align',
          url: 'https://dataease.io'
        }
      }
    }
  }
}

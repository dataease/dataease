import { defineStore } from 'pinia'
import { store } from '@/store/index'
import { defaultFont, list } from '@/api/font'
import { uiLoadApi } from '@/api/login'
import { useCache } from '@/hooks/web/useCache'
import colorFunctions from 'less/lib/less/functions/color.js'
import colorTree from 'less/lib/less/tree/color.js'
import { setTitle } from '@/utils/utils'

const basePath = import.meta.env.VITE_API_BASEPATH
const baseUrl = basePath + '/appearance/image/'
import { isBtnShow } from '@/utils/utils'
interface AppearanceState {
  themeColor?: string
  customColor?: string
  navigateBg?: string
  navigate?: string
  mobileLogin?: string
  mobileLoginBg?: string
  help?: string
  showAi?: string
  showCopilot?: string
  showDoc?: string
  showAbout?: string
  bg?: string
  login?: string
  slogan?: string
  web?: string
  name?: string
  foot?: string
  footContent?: string
  loaded: boolean
  showDemoTips?: boolean
  demoTipsContent?: string
  community: boolean
  fontList: Array<{ name: string; id: string; isDefault: boolean }>
}
const { wsCache } = useCache()
export const useAppearanceStore = defineStore('appearanceStore', {
  state: (): AppearanceState => {
    return {
      themeColor: '',
      customColor: '',
      navigateBg: '',
      navigate: '',
      mobileLogin: '',
      mobileLoginBg: '',
      help: '',
      showDoc: '0',
      showAi: '0',
      showCopilot: '0',
      showAbout: '0',
      bg: '',
      login: '',
      slogan: '',
      web: '',
      name: '',
      foot: 'false',
      footContent: '',
      loaded: false,
      showDemoTips: false,
      demoTipsContent: '',
      community: true,
      fontList: []
    }
  },
  getters: {
    getNavigate(): string {
      if (this.navigate) {
        return baseUrl + this.navigate
      }
      return null
    },
    getMobileLogin(): string {
      if (this.mobileLogin) {
        return baseUrl + this.mobileLogin
      }
      return null
    },
    getMobileLoginBg(): string {
      if (this.mobileLoginBg) {
        return baseUrl + this.mobileLoginBg
      }
      return null
    },
    getHelp(): string {
      return this.help
    },
    getThemeColor(): string {
      return this.themeColor
    },
    getCustomColor(): string {
      return this.customColor
    },
    getNavigateBg(): string {
      return this.navigateBg
    },
    getBg(): string {
      if (this.bg) {
        return baseUrl + this.bg
      }
      return null
    },
    getLogin(): string {
      if (this.login) {
        return baseUrl + this.login
      }
      return null
    },
    getSlogan(): string {
      return this.slogan
    },
    getWeb(): string {
      if (this.web) {
        return baseUrl + this.web
      }
      return null
    },
    getName(): string {
      return this.name
    },
    getLoaded(): boolean {
      return this.loaded
    },
    getFoot(): string {
      return this.foot
    },
    getFootContent(): string {
      return this.footContent
    },
    getShowDemoTips(): boolean {
      return this.showDemoTips
    },
    getDemoTipsContent(): string {
      return this.demoTipsContent
    },
    getCommunity(): boolean {
      return this.community
    },
    getShowAi(): boolean {
      return isBtnShow(this.showAi)
    },
    getShowCopilot(): boolean {
      return isBtnShow(this.showCopilot)
    },
    getShowDoc(): boolean {
      return isBtnShow(this.showDoc)
    },
    getShowAbout(): boolean {
      return isBtnShow(this.showAbout)
    }
  },
  actions: {
    setNavigate(data: string) {
      this.navigate = data
    },
    setMobileLogin(data: string) {
      this.mobileLogin = data
    },
    async setFontList() {
      const res = await list()
      this.fontList = res || []
    },
    setCurrentFont(name) {
      const currentFont = this.fontList.find(ele => ele.name === name)
      if (currentFont) {
        let fontStyleElement = document.querySelector(`#de-custom_font${name}`)
        if (!fontStyleElement) {
          fontStyleElement = document.createElement('style')
          fontStyleElement.setAttribute('id', `de-custom_font${name}`)
          document.querySelector('head').appendChild(fontStyleElement)
        }
        fontStyleElement.innerHTML = `@font-face {
            font-family: '${name}';
            src: url(${basePath}/typeface/download/${currentFont.fileTransName});
            font-weight: normal;
            font-style: normal;
            }`
      }
    },
    setMobileLoginBg(data: string) {
      this.mobileLoginBg = data
    },
    setHelp(data: string) {
      this.help = data
    },
    setNavigateBg(data: string) {
      this.navigateBg = data
    },
    setThemeColor(data: string) {
      this.themeColor = data
    },
    setCustomColor(data: string) {
      this.customColor = data
    },
    setLoaded(data: boolean) {
      this.loaded = data
    },
    async setAppearance(isDataEaseBi?: boolean) {
      const desktop = wsCache.get('app.desktop')
      if (desktop) {
        this.loaded = true
        this.community = true
      }
      if (this.loaded) {
        return
      }
      defaultFont().then(res => {
        const [font] = res || []
        setDefaultFont(
          `${basePath}/typeface/download/${font?.fileTransName}`,
          font?.name,
          font?.fileTransName
        )
        function setDefaultFont(url, name, fileTransName) {
          let fontStyleElement = document.querySelector('#de-custom_font')
          if (!fontStyleElement) {
            fontStyleElement = document.createElement('style')
            fontStyleElement.setAttribute('id', 'de-custom_font')
            document.querySelector('head').appendChild(fontStyleElement)
          }
          fontStyleElement.innerHTML =
            name && fileTransName
              ? `@font-face {
                font-family: '${name}';
                src: url(${url});
                font-weight: normal;
                font-style: normal;
                }`
              : ''
          document.documentElement.style.setProperty('--de-custom_font', `${name}`)
          document.documentElement.style.setProperty('--van-base-font', `${name}`)
        }
      })
      if (!isDataEaseBi) {
        document.title = ''
      }
      const res = await uiLoadApi()
      this.loaded = true
      const resData = res.data
      if (!resData?.length) {
        if (!isDataEaseBi) {
          document.title = 'DataEase'
          setLinkIcon()
        }
        return
      }
      const data: AppearanceState = { loaded: false, community: true }
      let isCommunity = false
      resData.forEach(item => {
        data[item.pkey] = item.pval
        if (item.pkey === 'community') {
          isCommunity = true
        }
      })
      data.community = isCommunity
      this.community = data.community
      if (this.community) {
        this.showDemoTips = data.showDemoTips
        this.demoTipsContent = data.demoTipsContent
        this.loaded = true
        setLinkIcon()
        return
      }
      this.navigate = data.navigate
      this.mobileLogin = data.mobileLogin
      this.mobileLoginBg = data.mobileLoginBg
      this.help = data.help
      this.showAi = data.showAi
      this.showCopilot = data.showCopilot
      this.showDoc = data.showDoc
      this.showAbout = data.showAbout
      this.navigateBg = data.navigateBg
      this.themeColor = data.themeColor
      this.customColor = data.customColor
      if (this.themeColor === 'custom' && this.customColor) {
        document.documentElement.style.setProperty('--ed-color-primary', this.customColor)
        document.documentElement.style.setProperty('--van-blue', this.customColor)
        document.documentElement.style.setProperty(
          '--ed-color-primary-light-5',
          colorFunctions
            .mix(new colorTree('ffffff'), new colorTree(this.customColor.substr(1)), { value: 40 })
            .toRGB()
        )
        document.documentElement.style.setProperty(
          '--ed-color-primary-light-3',
          colorFunctions
            .mix(new colorTree('ffffff'), new colorTree(this.customColor.substr(1)), { value: 15 })
            .toRGB()
        )
        document.documentElement.style.setProperty('--ed-color-primary-1a', `${this.customColor}1a`)
        document.documentElement.style.setProperty('--ed-color-primary-33', `${this.customColor}33`)
        document.documentElement.style.setProperty('--ed-color-primary-99', `${this.customColor}99`)
        document.documentElement.style.setProperty(
          '--ed-color-primary-dark-2',
          colorFunctions
            .mix(new colorTree('000000'), new colorTree(this.customColor.substr(1)), { value: 15 })
            .toRGB()
        )
      } else if (document.documentElement.style.getPropertyValue('--ed-color-primary')) {
        document.documentElement.style.setProperty('--ed-color-primary', '#3370FF')
        document.documentElement.style.removeProperty('--ed-color-primary-light-3')
        document.documentElement.style.removeProperty('--ed-color-primary-light-5')
        document.documentElement.style.removeProperty('--ed-color-primary-1a')
        document.documentElement.style.removeProperty('--ed-color-primary-33')
        document.documentElement.style.removeProperty('--ed-color-primary-99')
        document.documentElement.style.removeProperty('--ed-color-primary-dark-2')
      }
      this.bg = data.bg
      this.login = data.login
      this.slogan = data.slogan
      this.web = data.web
      this.name = data.name
      this.foot = data.foot
      this.footContent = data.footContent
      if (isDataEaseBi) return
      if (this.name) {
        document.title = this.name
        setTitle(this.name)
      } else {
        document.title = 'DataEase'
        setTitle('DataEase')
      }
      setLinkIcon(this.web)
    }
  }
})

const setLinkIcon = (linkWeb?: string) => {
  const link = document.querySelector('link[rel="icon"]')
  if (link) {
    if (linkWeb) {
      link['href'] = baseUrl + linkWeb
    } else {
      link['href'] = '/dataease.svg'
    }
  }
}

export const useAppearanceStoreWithOut = () => {
  return useAppearanceStore(store)
}

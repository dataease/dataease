import { defineStore } from 'pinia'
import { store } from '@/store/index'
import { uiLoadApi } from '@/api/login'
import { useCache } from '@/hooks/web/useCache'
const basePath = import.meta.env.VITE_API_BASEPATH
const baseUrl = basePath + '/appearance/image/'
interface AppearanceState {
  themeColor?: string
  customColor?: string
  navigateBg?: string
  navigate?: string
  help?: string
  bg?: string
  login?: string
  slogan?: string
  web?: string
  name?: string
  loaded: boolean
}
const { wsCache } = useCache()
export const useAppearanceStore = defineStore('appearanceStore', {
  state: (): AppearanceState => {
    return {
      themeColor: '',
      customColor: '',
      navigateBg: '',
      navigate: '',
      help: '',
      bg: '',
      login: '',
      slogan: '',
      web: '',
      name: '',
      loaded: false
    }
  },
  getters: {
    getNavigate(): string {
      if (this.navigate) {
        return baseUrl + this.navigate
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
    }
  },
  actions: {
    setNavigate(data: string) {
      this.navigate = data
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
    async setAppearance() {
      const desktop = wsCache.get('app.desktop')
      if (desktop) {
        this.loaded = true
      }
      if (this.loaded) {
        return
      }
      const res = await uiLoadApi()
      this.loaded = true
      const resData = res.data
      if (!resData?.length) {
        return
      }
      const data: AppearanceState = { loaded: false }
      resData.forEach(item => {
        data[item.pkey] = item.pval
      })
      this.navigate = data.navigate
      this.help = data.help
      this.navigateBg = data.navigateBg
      this.themeColor = data.themeColor
      this.customColor = data.customColor
      if (this.themeColor === 'custom' && this.customColor) {
        document.documentElement.style.setProperty('--ed-color-primary', this.customColor)
      } else if (document.documentElement.style.getPropertyValue('--ed-color-primary')) {
        document.documentElement.style.setProperty('--ed-color-primary', '#3370FF')
      }
      this.bg = data.bg
      this.login = data.login
      this.slogan = data.slogan
      this.web = data.web
      this.name = data.name
      if (this.name) {
        document.title = this.name
      }
      const link = document.querySelector('link[rel="icon"]')
      if (link) {
        if (this.web) {
          link['href'] = baseUrl + this.web
        } else {
          link['href'] = '/dataease.svg'
        }
      }
    }
  }
})

export const useAppearanceStoreWithOut = () => {
  return useAppearanceStore(store)
}

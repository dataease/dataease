<template>
  <div class="appearance-table__content">
    <el-scrollbar>
      <div class="theme">
        <div class="platform-theme">
          {{ t('system.platform_display_theme') }}
        </div>
        <div class="navigate-bg">
          {{ t('system.navigation_background_color') }}
        </div>
        <div class="color-type">
          <div
            class="color-item"
            :class="navigateBg === 'dark' && 'active'"
            @click="navigateClick('dark')"
          >
            <img :src="DarkBg" alt="" />
            <div class="color-item-label">
              <el-radio v-model="navigateBg" @change="navigateBgChange" label="dark">{{
                t('system.dark_color')
              }}</el-radio>
            </div>
          </div>
          <div
            class="color-item"
            :class="navigateBg === 'light' && 'active'"
            @click="navigateClick('light')"
          >
            <img :src="LightBg" alt="" />
            <div class="color-item-label">
              <el-radio v-model="navigateBg" @change="navigateBgChange" label="light">{{
                t('system.light_color')
              }}</el-radio>
            </div>
          </div>
        </div>
        <div class="theme-bg">{{ t('system.theme_color') }}</div>
        <div class="theme-color">
          <el-radio-group v-model="themeColor" @change="themeColorChange">
            <el-radio label="default">{{ t('system.default_blue') }}</el-radio>
            <el-radio label="custom">{{ t('data_set.customize') }}</el-radio>
          </el-radio-group>
        </div>

        <template v-if="themeColor === 'custom'">
          <div class="theme-bg">{{ t('system.custom_color_value') }}</div>
          <el-color-picker
            :trigger-width="108"
            v-model="customColor"
            :predefine="COLOR_PANEL"
            is-custom
            effect="light"
            @change="customColorChange"
          />
        </template>
      </div>
      <div class="login">
        <div class="platform-login">
          {{ t('system.platform_login_settings') }}
        </div>
        <div class="page-preview">
          <div class="title">
            <span class="left">{{ t('system.page_preview') }}</span>
            <el-button text @click="resetLoginForm(true)">{{
              t('system.restore_default')
            }}</el-button>
          </div>
          <div class="page-setting">
            <div class="page-content">
              <!-- <img :src="loginPreview" alt="" /> -->
              <login-preview
                :navigate-bg="navigateBg"
                :theme-color="themeColor"
                :custom-color="customColor"
                :name="loginForm.name"
                :slogan="loginForm.slogan"
                :show-slogan="loginForm.showSlogan"
                :web="web"
                :bg="bg"
                :login="login"
                :height="navigateHeight"
                :foot="loginForm.foot"
                :foot-content="loginForm.footContent"
              />
              <div class="tips-page">
                {{
                  t('system.supports_custom_settings', {
                    msg: loginForm.name || 'DataEase'
                  })
                }}
              </div>
            </div>
            <div class="config-list">
              <div class="config-item" v-for="ele in configList" :key="ele.type">
                <div class="config-logo">
                  <span class="logo">{{ ele.logo }}</span>
                  <el-upload
                    :name="ele.type"
                    :show-file-list="false"
                    class="upload-demo"
                    accept=".jpeg,.jpg,.png,.gif,.svg"
                    :before-upload="e => beforeUpload(e, ele.type)"
                    :http-request="uploadImg"
                  >
                    <el-button secondary>{{ t('system.replace_image') }}</el-button>
                  </el-upload>
                </div>
                <div class="tips">{{ ele.tips }}</div>
              </div>
              <el-form
                ref="loginFormRef"
                :model="loginForm"
                label-position="top"
                :rules="rules"
                require-asterisk-position="right"
                label-width="120px"
                class="page-Form"
              >
                <el-form-item :label="t('system.website_name')" prop="name">
                  <el-input maxlength="20" v-model="loginForm.name" />
                  <div class="form-tips">{{ t('system.web_page_tab') }}</div>
                </el-form-item>
                <el-form-item label="Slogan" prop="showSlogan">
                  <el-switch
                    active-value="true"
                    inactive-value="false"
                    v-model="loginForm.showSlogan"
                  />
                </el-form-item>
                <el-form-item
                  v-if="loginForm.showSlogan === 'true'"
                  :label="t('system.slogan_content')"
                  prop="slogan"
                >
                  <el-input maxlength="50" v-model="loginForm.slogan" />
                  <div class="form-tips">
                    {{ t('system.under_product_logo') }}
                  </div>
                </el-form-item>
                <el-form-item :label="t('system.footer')" prop="foot">
                  <el-switch active-value="true" inactive-value="false" v-model="loginForm.foot" />
                </el-form-item>
                <el-form-item
                  :label="t('system.footer_content')"
                  prop="footContent"
                  v-if="loginForm.foot === 'true'"
                >
                  <tinymce-editor
                    v-if="loginForm.foot === 'true'"
                    v-model="loginForm.footContent"
                    style="height: 92px !important"
                  />
                </el-form-item>
              </el-form>
            </div>
          </div>
        </div>
      </div>
      <div class="login">
        <div class="platform-login">{{ t('system.platform_settings') }}</div>
        <div class="page-preview">
          <div class="title">
            <span class="left">{{ t('system.page_preview') }}</span>
            <el-button text @click="resetTopForm(true)">{{
              t('system.restore_default')
            }}</el-button>
          </div>
          <div class="page-setting">
            <div class="page-content">
              <!-- <div class="navigate-preview" :style="{'height': `${navigateHeight}px`}"> -->
              <div class="navigate-preview" style="height: 425px">
                <div
                  class="navigate-head"
                  :class="{
                    'light-head': navigateBg && navigateBg === 'light'
                  }"
                >
                  <img
                    class="logo"
                    v-if="navigate"
                    :src="navigate.startsWith('blob') ? navigate : baseUrl + navigate"
                    alt=""
                  />
                  <Icon v-else><logo class="svg-icon logo" /></Icon>
                  <el-divider direction="vertical" />
                </div>
                <div class="navigate-content" />
              </div>
              <div class="tips-page">
                {{ t('system.platform', { msg: loginForm.name || 'DataEase' }) }}
              </div>
            </div>
            <div class="config-list">
              <div class="config-item">
                <div class="config-logo">
                  <span class="logo">{{ t('system.top_navigation_logo') }}</span>
                  <el-upload
                    class="upload-demo"
                    :show-file-list="false"
                    accept=".jpeg,.jpg,.png,.gif,.svg"
                    :before-upload="e => beforeUpload(e, 'navigate')"
                    :http-request="uploadImg"
                  >
                    <el-button secondary>{{ t('system.replace_image') }}</el-button>
                  </el-upload>
                </div>
                <div class="tips">
                  {{ t('system.not_exceeding_200kb') }}
                </div>
              </div>
              <el-form
                ref="topFormRef"
                :model="topForm"
                label-position="top"
                :rules="topRules"
                require-asterisk-position="right"
                label-width="120px"
                class="page-Form"
              >
                <el-form-item
                  style="margin-bottom: 14px"
                  :label="t('system.help_document')"
                  prop="help"
                >
                  <el-input v-model="topForm.help" />
                </el-form-item>
                <el-form-item
                  :label="t('system.document_button')"
                  prop="showDoc"
                  class="appearance-radio-item"
                >
                  <el-radio-group v-model="topForm.showDoc">
                    <el-radio
                      v-for="option in btnShowOptions"
                      :key="option.label"
                      :label="option.label"
                      >{{ option.name }}</el-radio
                    >
                  </el-radio-group>
                </el-form-item>

                <el-form-item
                  :label="t('system.about_button')"
                  prop="showAbout"
                  class="appearance-radio-item"
                >
                  <el-radio-group v-model="topForm.showAbout">
                    <el-radio
                      v-for="option in btnShowOptions"
                      :key="option.label"
                      :label="option.label"
                      >{{ option.name }}</el-radio
                    >
                  </el-radio-group>
                </el-form-item>
              </el-form>
            </div>
          </div>
        </div>
      </div>
      <div class="login" style="border-bottom-right-radius: 0; border-bottom-left-radius: 0">
        <div class="platform-login">
          {{ t('system.mobile_login_settings') }}
        </div>
        <div class="page-preview">
          <div class="title">
            <span class="left">{{ t('system.page_preview') }}</span>
            <el-button text @click="resetMobileForm(true)">{{
              t('system.restore_default')
            }}</el-button>
          </div>
          <div class="page-setting">
            <div class="page-content">
              <div class="navigate-preview" style="height: 325px; overflow-y: hidden">
                <div class="mobile-fake">
                  <div class="mobile-canvas">
                    <div class="mobile-header">
                      <img :src="mobileHeader" alt="" srcset="" />
                    </div>
                    <div class="config-panel-title">{{ loginForm.name }}</div>
                    <div class="config-panel-content" :class="!mobileLoginBg && 'with-bg'">
                      <div class="login-mobile_color">
                        <img
                          class="mobile-login_bg"
                          v-if="mobileLoginBg"
                          :src="
                            mobileLoginBg.startsWith('blob')
                              ? mobileLoginBg
                              : baseUrl + mobileLoginBg
                          "
                          alt=""
                        />
                        <div class="mobile-login-content">
                          <img
                            v-if="mobileLogin"
                            width="120"
                            height="31"
                            :src="
                              mobileLogin.startsWith('blob') ? mobileLogin : baseUrl + mobileLogin
                            "
                            alt=""
                          />
                          <img v-else width="120" height="31" :src="mobileDeTop" alt="" />
                          <div class="mobile-login-welcome">
                            {{ t('system.user_login') }}
                          </div>
                          <div class="login-input_fake">
                            {{ t('system.in_user_name') }}
                          </div>
                          <div class="login-input_fake">
                            {{ t('system.fill_in_password') }}
                          </div>
                        </div>
                      </div>
                    </div>
                    <div class="config-panel-foot"></div>
                  </div>
                </div>
              </div>
              <div class="tips-page">
                {{
                  t('system.supports_custom_settings_de', {
                    msg: loginForm.name || 'DataEase'
                  })
                }}
              </div>
            </div>
            <div class="config-list">
              <div class="config-item">
                <div class="config-logo">
                  <span class="logo">{{ t('system.login_logo') }}</span>
                  <el-upload
                    class="upload-demo"
                    :show-file-list="false"
                    accept=".jpeg,.jpg,.png,.gif,.svg"
                    :before-upload="e => beforeUpload(e, 'mobileLogin')"
                    :http-request="uploadImg"
                  >
                    <el-button secondary>{{ t('system.replace_image') }}</el-button>
                  </el-upload>
                </div>
                <div class="tips">
                  {{ t('system.not_exceeding_200kb_de') }}
                </div>
              </div>
              <div class="config-item">
                <div class="config-logo">
                  <span class="logo">{{ t('system.login_background_image') }}</span>
                  <el-upload
                    class="upload-demo"
                    :show-file-list="false"
                    accept=".jpeg,.jpg,.png,.gif,.svg"
                    :before-upload="e => beforeUpload(e, 'mobileLoginBg')"
                    :http-request="uploadImg"
                  >
                    <el-button secondary>{{ t('system.replace_image') }}</el-button>
                  </el-upload>
                </div>
                <div class="tips">
                  {{ t('system.not_exceeding_5m') }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-scrollbar>
  </div>
  <div class="appearance-foot">
    <el-button secondary @click="giveUp">{{ t('appearance.give_up') }}</el-button>
    <el-button type="primary" v-if="showSaveButton" @click="saveHandler">{{
      t('appearance.save_apply')
    }}</el-button>
  </div>
</template>

<script lang="ts" setup>
import logo from '@/assets/svg/logo.svg'
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import DarkBg from '@/assets/img/dark-theme-bg.png'
import LightBg from '@/assets/img/light-theme-bg.png'
import mobileHeader from '@/assets/img/mobile-header.png'
import mobileDeTop from '@/assets/img/mobile-de-top.png'
import {
  type FormInstance,
  type FormRules,
  type UploadUserFile,
  ElMessage
} from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import request from '@/config/axios'
import { useAppearanceStoreWithOut } from '@/store/modules/appearance'
import LoginPreview from '@/views/menu/setting/appearance/LoginPreview.vue'
import TinymceEditor from '@/components/rich-text/TinymceEditor.vue'
const appearanceStore = useAppearanceStoreWithOut()
const { t } = useI18n()
interface LoginForm {
  name: string
  slogan: string
  foot: string
  footContent?: string
  showSlogan: string
}
interface ConfigItem {
  pkey: string
  pval: string
  type: string
  sort: number
}
const btnShowOptions = [
  { label: '0', name: t('chart.show') },
  { label: '1', name: t('chart.hide') },
  { label: '2', name: t('system.hidden_in_iframe') }
]
const COLOR_PANEL = [
  '#FF4500',
  '#FF8C00',
  '#FFD700',
  '#71AE46',
  '#00CED1',
  '#1E90FF',
  '#C71585',
  '#999999',
  '#000000',
  '#FFFFFF'
]
const basePath = import.meta.env.VITE_API_BASEPATH
const baseUrl = basePath + '/appearance/image/'
const fileList = ref<UploadUserFile[]>([])
const navigateBg = ref('dark')
const themeColor = ref('default')
const customColor = ref('#307eff')
const web = ref('')
const bg = ref('')
const login = ref('')
const navigate = ref('')
const mobileLogin = ref('')
const mobileLoginBg = ref('')
const navigateHeight = ref(400)

const changedItemArray = ref<ConfigItem[]>([])

const loginFormRef = ref<FormInstance>()
const defaultLoginForm = reactive<LoginForm>({
  name: 'DataEase',
  slogan: t('system.available_to_everyone'),
  foot: 'false',
  footContent: '',
  showSlogan: 'true'
})
const loginForm = reactive<LoginForm>({
  name: 'DataEase',
  slogan: t('system.available_to_everyone'),
  foot: 'false',
  footContent: '',
  showSlogan: 'true'
})

const rules = reactive<FormRules>({
  name: [{ required: true, message: t('system.the_website_name'), trigger: 'blur' }],
  slogan: [
    {
      required: true,
      message: t('system.enter_the_slogan'),
      trigger: 'blur'
    }
  ],
  foot: [
    {
      required: true,
      message: '',
      trigger: 'change'
    }
  ],
  showSlogan: [
    {
      required: true,
      message: '',
      trigger: 'change'
    }
  ]
})

const topForm = reactive<{
  help: string
  showAi: string
  showDoc: string
  showAbout: string
}>({
  help: 'https://dataease.io/docs/',
  showAi: '0',
  showDoc: '0',
  showAbout: '0'
})

const defaultTopForm = reactive<{
  help: string
  showAi: string
  showDoc: string
  showAbout: string
}>({
  help: 'https://dataease.io/docs/',
  showAi: '0',
  showDoc: '0',
  showAbout: '0'
})

const topRules = reactive<FormRules>({
  help: [{ required: true, message: t('system.the_help_document'), trigger: 'blur' }],
  showAi: [{ required: true, message: t('system.assistant'), trigger: 'change' }],
  showDoc: [
    {
      required: true,
      message: t('system.display_the_document'),
      trigger: 'change'
    }
  ],
  showAbout: [
    {
      required: true,
      message: t('system.display_the_about'),
      trigger: 'change'
    }
  ]
})
const configList = [
  {
    logo: t('system.website_logo'),
    type: 'web',
    tips: t('system.not_exceeding_200kb_de_')
  },
  {
    logo: t('system.login_logo'),
    type: 'login',
    tips: t('system.not_exceeding_200kb_de_right')
  },
  {
    logo: t('system.login_background_image'),
    type: 'bg',
    tips: t('system.not_exceeding_5m_de')
  }
]

const giveUp = () => {
  resetLoginForm(false)
  resetTopForm(false)
  resetMobileForm(false)
  init()
}
const topFormRef = ref()
const showSaveButton = ref(true)
const saveHandler = () => {
  topFormRef.value.validate(val => {
    if (val) {
      loginFormRef.value?.validate(valLogin => {
        if (valLogin) {
          const param = buildParam()
          const url = '/appearance/save'
          request.post({ url, data: param, headersType: 'multipart/form-data;' }).then(res => {
            if (!res.msg) {
              ElMessage.success(t('common.save_success'))
              appearanceStore.setLoaded(false)
              appearanceStore.setAppearance()
              showSaveButton.value = false
              nextTick(() => {
                showSaveButton.value = true
              })
            }
          })
        }
      })
    }
  })
}
const buildParam = () => {
  for (const key in loginForm) {
    const item = loginForm[key]
    if (key === 'footContent') {
      addChangeArray(key, item, 'blob')
    } else {
      addChangeArray(key, item)
    }
  }
  for (const key in topForm) {
    const item = topForm[key]
    addChangeArray(key, item)
  }
  const formData = new FormData()
  if (fileList.value.length) {
    fileList.value.forEach(file => {
      const name = file.name + ',' + file['flag']
      const fileArray = [file]
      const newfile = new File(fileArray, name, { type: file['type'] })
      formData.append('files', newfile)
    })
  }
  formData.append(
    'request',
    new Blob([JSON.stringify(changedItemArray.value)], {
      type: 'application/json'
    })
  )
  return formData
}
const init = () => {
  const url = '/appearance/query'
  changedItemArray.value = []
  fileList.value = []
  request
    .get({ url })
    .then(res => {
      const list = res.data
      if (!list.length) {
        return
      }
      list.forEach(item => {
        const pkey = item.pkey
        const pval = item.pval
        if (pkey === 'navigateBg') {
          navigateBg.value = pval
        } else if (pkey === 'themeColor') {
          themeColor.value = pval
        } else if (pkey === 'customColor') {
          customColor.value = pval
        } else if (pkey === 'web') {
          web.value = pval
        } else if (pkey === 'login') {
          login.value = pval
        } else if (pkey === 'bg') {
          bg.value = pval
        } else if (pkey === 'navigate') {
          navigate.value = pval
        } else if (loginForm.hasOwnProperty(pkey)) {
          loginForm[pkey] = pval
        } else if (topForm.hasOwnProperty(pkey)) {
          topForm[pkey] = pval
        } else if (pkey === 'mobileLogin') {
          mobileLogin.value = pval
        } else if (pkey === 'mobileLoginBg') {
          mobileLoginBg.value = pval
        }
      })
    })
    .finally(() => {
      nextTick(() => {
        if (themeColor.value === 'custom') {
          setPageCustomColor(customColor.value)
        } else {
          setPageCustomColor('#3370FF')
        }
      })
    })
}
const addChangeArray = (key: string, val: string, type?: string) => {
  let len = changedItemArray.value.length
  let match = false
  while (len--) {
    const item = changedItemArray.value[len]
    if (item['pkey'] === key) {
      changedItemArray.value[len] = {
        pkey: key,
        pval: val,
        type: type || 'text',
        sort: 1
      }
      match = true
    }
  }
  if (!match) {
    changedItemArray.value.push({
      pkey: key,
      pval: val,
      type: type || 'text',
      sort: 1
    })
  }
}
const navigateBgChange = val => {
  addChangeArray('navigateBg', val)
}
const navigateClick = val => {
  navigateBg.value = val
  navigateBgChange(val)
}
const themeColorChange = val => {
  addChangeArray('themeColor', val)
  if (themeColor.value === 'custom') {
    setPageCustomColor(customColor.value)
  } else {
    setPageCustomColor('#3370FF')
  }
}
const customColorChange = val => {
  addChangeArray('customColor', val)
  setPageCustomColor(val)
}
const setPageCustomColor = val => {
  document
    .getElementsByClassName('appearance-table__content')[0]
    ?.style.setProperty('--ed-color-primary', val)
}
const resetLoginForm = (reset2Default?: boolean) => {
  for (const key in loginForm) {
    loginForm[key] = defaultLoginForm[key]
  }
  clearFiles(['web', 'login', 'bg'])
  if (reset2Default) {
    addChangeArray('web', '', 'file')
    addChangeArray('login', '', 'file')
    addChangeArray('bg', '', 'file')
    web.value = ''
    login.value = ''
    bg.value = ''
  }
}
const resetTopForm = (reset2Default?: boolean) => {
  for (const key in topForm) {
    topForm[key] = defaultTopForm[key]
  }
  clearFiles(['navigate'])
  if (reset2Default) {
    addChangeArray('navigate', '', 'file')
    navigate.value = ''
  }
}

const resetMobileForm = (reset2Default?: boolean) => {
  clearFiles(['mobileLogin', 'mobileLoginBg'])
  if (reset2Default) {
    addChangeArray('mobileLogin', '', 'file')
    addChangeArray('mobileLoginBg', '', 'file')
    mobileLogin.value = ''
    mobileLoginBg.value = ''
  }
}

const uploadImg = options => {
  const file = options.file
  if (file['flag'] === 'web') {
    web.value = URL.createObjectURL(file)
  } else if (file['flag'] === 'bg') {
    bg.value = URL.createObjectURL(file)
  } else if (file['flag'] === 'login') {
    login.value = URL.createObjectURL(file)
  } else if (file['flag'] === 'navigate') {
    navigate.value = URL.createObjectURL(file)
  } else if (file['flag'] === 'mobileLogin') {
    mobileLogin.value = URL.createObjectURL(file)
  } else if (file['flag'] === 'mobileLoginBg') {
    mobileLoginBg.value = URL.createObjectURL(file)
  }
}
const beforeUpload = (file, type) => {
  addChangeArray(type, file.uid, 'file')
  let len = fileList.value?.length
  let match = false
  file.flag = type
  while (len--) {
    const tfile = fileList.value[len]
    if (type == tfile['flag']) {
      fileList.value[len] = file
      match = true
    }
  }
  if (!match) {
    fileList.value?.push(file)
  }
  return true
}

const clearFiles = (array?: string[]) => {
  if (!array?.length || !fileList.value?.length) {
    fileList.value = []
    return
  }
  let len = fileList.value.length
  while (len--) {
    const file = fileList.value[len]
    if (array.includes(file['flag'])) {
      fileList.value.splice(len, 1)
    }
  }
}

const getHeight = () => {
  const dom = document.getElementsByClassName('navigate-preview')
  const width = dom[0].clientWidth
  navigateHeight.value = parseInt((width * 0.625).toString())
}

onMounted(() => {
  init()
  nextTick(() => {
    getHeight()
  })
  window.addEventListener('resize', getHeight)
})
onUnmounted(() => {
  window.removeEventListener('resize', getHeight)
})
</script>

<style lang="less" scoped>
.appearance-table__content {
  width: 100%;
  min-width: 840px;
  height: calc(100vh - 172px);
  overflow: hidden;
  margin-top: 8px;

  :deep(.ed-form-item__error) {
    top: 88%;
  }
  :deep(.ed-form-item__label) {
    line-height: 22px !important;
    height: 22px;
  }

  .theme,
  .login,
  .setting {
    background: var(--ContentBG, #ffffff);
    padding: 24px;
    width: 100%;
    border-radius: 12px;

    & > :nth-child(1) {
      font-size: 16px;
      font-weight: 500;
      line-height: 24px;
    }
  }

  .theme {
    .navigate-bg {
      font-size: 14px;
      font-weight: 400;
      line-height: 22px;
      margin: 16px 0 8px 0;
    }
    .theme-bg {
      font-size: 14px;
      font-weight: 400;
      line-height: 22px;
      margin: 16px 0 8px 0;
    }
    :deep(.ed-color-picker) {
      height: 32px;
      .ed-color-picker__trigger {
        height: 100%;
        padding: 8px;
      }
    }

    .color-type {
      display: flex;
      .color-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: space-between;
        padding-top: 10px;
        width: 258px;
        height: 184px;
        border-radius: 12px;
        border: 1px solid #dee0e3;
        background-color: #f5f6f7;
        margin-right: 17px;
        &:hover {
          cursor: pointer;
        }
        img {
          width: 180px;
          height: 120px;
        }

        .color-item-label {
          height: 40px;
          width: 100%;
          border-top: 1px solid #dddedf;
          display: flex;
          align-items: center;
          padding-left: 12px;
          background-color: #fff;
          border-bottom-left-radius: 12px;
          border-bottom-right-radius: 12px;
        }
        &.active {
          border-color: var(--ed-color-primary);
          .color-item-label {
            background-color: var(--ed-color-primary-1a, rgba(51, 112, 255, 0.1));
          }
        }
      }
    }

    .theme-color {
      font-size: 14px;
      font-weight: 400;
      line-height: 22px;
      height: 22px;
      .ed-radio-group {
        height: 22px;
      }
      :deep(label) {
        height: 22px;
      }
    }
  }

  .login,
  .setting {
    margin-top: 16px;
  }

  .login {
    .page-preview {
      background-color: #f5f6f7;
      margin-top: 16px;
      padding: 16px;
      border-radius: 12px;
      .title {
        margin-bottom: 16px;
        display: flex;
        align-items: center;
        justify-content: space-between;
        .left {
          font-size: 14px;
          font-weight: 500;
          line-height: 22px;
        }
      }

      .page-setting {
        display: flex;
        justify-content: space-between;
        .page-content {
          width: calc(100% - 378px);
          .navigate-preview {
            height: calc(100% - 28px);
            .light-head {
              background-color: #ffffff !important;
              box-shadow: 0px 0.5px 0px 0px #1f232926 !important;
              .ed-divider {
                border-color: #1f232926 !important;
              }
              .logo {
                color: #3371ff !important;
              }
            }
            .navigate-head {
              height: 45px;
              margin-bottom: 1px;
              display: flex;
              align-items: center;
              background-color: #050e21;
              padding: 0 15px;
              .logo {
                width: 120px;
                height: 28px;
                color: #fff;
              }

              .ed-divider {
                margin: 0 17px;
                border-color: rgba(255, 255, 255, 0.3);
              }
            }
            .navigate-content {
              height: calc(100% - 45px);
              background-color: #fff;
            }
          }
          .mobile-fake {
            display: flex;
            align-items: center;
            justify-content: center;
            background: #ffffff;
            border-radius: 12px;
            padding-top: 24px;
            .mobile-canvas {
              transform: scale(0.6);
              transform-origin: top;
              border-radius: 30px;
              width: 419px;
              height: 852px;
              overflow: hidden;
              background-size: 100% 100% !important;
              position: relative;
              background-image: url(@/assets/img/mobile-bg-pc.png);
              padding: 0 22px;

              .mobile-header {
                margin-top: 20px;
                height: 43px;
                display: flex;
                img {
                  height: 100%;
                  width: 100%;
                }
              }

              .config-panel-title {
                height: 44px;
                display: flex;
                align-items: center;
                justify-content: center;
                background: #fff;
                position: relative;
                border-top-right-radius: 4px;
                border-top-left-radius: 4px;
              }

              .config-panel-content {
                width: 100%;
                height: calc(100% - 127px);
                border-bottom-left-radius: 45px;
                border-bottom-right-radius: 45px;
                overflow: hidden;

                .login-mobile_color {
                  width: 100%;
                  height: 100%;
                  position: relative;
                  .mobile-login-content {
                    background: linear-gradient(
                      180deg,
                      rgba(255, 255, 255, 0.94) 0%,
                      #ffffff 58.86%
                    );
                    position: absolute;
                    bottom: 0;
                    left: 0;
                    border-top-right-radius: 20px;
                    border-top-left-radius: 20px;
                    overflow: hidden;
                    width: 100%;
                    height: 70%;
                    padding: 24px 16px;
                    z-index: 10;

                    .mobile-login-welcome {
                      font-size: 22px;
                      font-weight: 500;
                      line-height: 30px;
                      margin-top: 10px;
                    }

                    .login-input_fake {
                      position: relative;
                      width: 100%;
                      height: 48px;
                      border: 1px solid #bbbfc4;
                      border-radius: 4px;
                      margin: 16px 0;
                      padding: 12px 16px;
                      border-color: rgb(187, 191, 196);
                    }
                  }
                }

                &.with-bg {
                  background-size: contain;
                  background-repeat: no-repeat;
                  background-image: url(@/assets/img/bg-mobile.png);
                }

                .mobile-login_bg {
                  width: 100%;
                  height: 100%;
                  position: relative;
                  z-index: 1;
                }
              }
            }
          }
          .tips-page {
            font-size: 14px;
            font-weight: 400;
            line-height: 22px;
            color: #8f959e;
            margin-top: 6px;
          }
        }

        .config-list {
          width: 378px;
          margin-left: 16px;

          .config-item {
            min-height: 104px;
            margin-bottom: 8px;
            padding: 16px;
            border-radius: 6px;
            border: 1px solid #dee0e3;
            background: #fff;
            .config-logo {
              display: flex;
              align-items: center;
              justify-content: space-between;
              margin-bottom: 8px;
              .logo {
                font-size: 14px;
                font-weight: 400;
                line-height: 22px;
              }
              .ed-button {
                min-width: 64px;
                height: 28px;
                line-height: 28px;
                padding: 4px 7px;
                font-size: 12px;
                font-weight: 400;
              }
            }

            .tips {
              font-size: 12px;
              font-weight: 400;
              line-height: 18px;
              white-space: pre-wrap;
              color: #8f959e;
            }
          }

          .page-Form {
            .form-tips {
              font-size: 14px;
              font-weight: 400;
              line-height: 22px;
              color: #8f959e;
            }

            .ed-form-item {
              margin-bottom: 8px;
            }
            .appearance-radio-item {
              :deep(.ed-form-item__content) {
                line-height: 22px;
              }
              :deep(label) {
                height: 22px;
                margin-right: 24px;
              }
            }
          }
        }
      }
    }
  }
}
.appearance-foot {
  display: flex;
  justify-content: flex-end;
  padding: 16px 24px;
  height: 64px;
  background: var(--ContentBG, #ffffff);
  box-shadow: 0px -2px 4px 0px #1f232914;
  margin-top: 1px;
  border-bottom-right-radius: 12px;
  border-bottom-left-radius: 12px;
}
</style>

<style lang="less">
body:has(.appearance-table__content) .tox-toolbar__overflow {
  max-width: fit-content !important;
}
</style>

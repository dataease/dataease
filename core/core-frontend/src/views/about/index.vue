<template>
  <div style="display: flex; width: 100%; justify-content: center" v-loading="loading">
    <el-card class="box-card about-card" :class="dynamicCardClass">
      <template #header>
        <div class="clearfix license-header">
          <img src="@/assets/img/DataEase-white.png" alt="" width="300" />
        </div>
      </template>
      <div class="license-content">
        <div v-if="license.status === 'Fail'">{{ $t('about.invalid_license') }}</div>
        <div v-if="license.status !== 'Fail'">
          <table>
            <tr>
              <th>{{ $t('about.auth_to') }}</th>
              <td>{{ license.corporation }}</td>
            </tr>
            <tr>
              <th>{{ $t('about.expiration_time') }}</th>
              <td>
                <label v-if="license.status === 'expired'" style="color: red"
                  >{{ license.expired }} {{ $t('about.expirationed') }}</label
                >
                <label v-if="license.status === 'valid'">{{ license.expired }}</label>
              </td>
            </tr>
            <tr>
              <th>{{ $t('about.auth_num') }}</th>
              <td>{{ license.count }}</td>
            </tr>
            <tr>
              <th>{{ $t('about.version') }}</th>
              <td>
                <span v-if="license.edition">
                  <span v-if="license.edition === 'Standard'">{{ $t('about.standard') }}</span>
                  <span v-if="license.edition === 'Enterprise'">{{ $t('about.enterprise') }}</span>
                </span>
              </td>
            </tr>
            <tr>
              <th>{{ $t('about.version_num') }}</th>
              <td>
                <span>{{ build }}</span>
              </td>
            </tr>
            <tr v-if="license.serialNo">
              <th>{{ $t('about.serial_no') }}</th>
              <td>
                <span>{{ license.serialNo }}</span>
              </td>
            </tr>
            <tr v-if="license.remark">
              <th>{{ $t('about.remark') }}</th>
              <td>
                <span>{{ license.remark }}</span>
              </td>
            </tr>
          </table>
        </div>

        <div class="md-padding" />
        <div v-if="isAdmin" layout="row" layout-align="space-between center" class="lic_rooter">
          <el-upload
            action=""
            :multiple="false"
            :show-file-list="false"
            :file-list="fileList"
            accept=".key"
            name="file"
            :before-upload="beforeUpload"
          >
            <a class="md-primary pointer">{{ $t('about.update_license') }}</a>
          </el-upload>

          <a class="md-primary pointer" @click="support">{{ $t('about.support') }}</a>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStoreWithOut } from '@/store/modules/user'
import { F2CLicense } from './index'
import { validateApi, buildVersionApi, updateInfoApi } from '@/api/about'
import { ElMessage } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
const { t } = useI18n()
const userStore = useUserStoreWithOut()
const license: F2CLicense = reactive({
  status: '',
  corporation: '',
  expired: '',
  count: 0,
  version: '',
  edition: '',
  serialNo: '',
  remark: ''
})
const build = ref('')
const isAdmin = ref(false)
const fileList = reactive([])
const dynamicCardClass = ref('')
const loading = ref(false)
onMounted(() => {
  isAdmin.value = userStore.getUid === '1'
  initVersion()
  getLicenseInfo()
})

const initVersion = () => {
  buildVersionApi().then(res => {
    build.value = res.data
  })
}
const beforeUpload = file => {
  importLic(file)
  return false
}

const support = () => {
  const url = 'https://support.fit2cloud.com/'
  window.open(url, '_blank')
}

const getLicenseInfo = () => {
  validateHandler({}, res => {
    const info = getLicense(res.data)
    setLicense(info)
  })
}
const setLicense = lic => {
  for (const key in license) {
    if (Object.prototype.hasOwnProperty.call(license, key)) {
      license[key] = lic[key]
    }
  }
  if (license?.serialNo && license?.remark) {
    dynamicCardClass.value = 'about-card-max'
  } else if (!license?.serialNo && !license?.remark) {
    dynamicCardClass.value = ''
  } else {
    dynamicCardClass.value = 'about-card-medium'
  }
}

const importLic = file => {
  const reader = new FileReader()
  reader.onload = function (e) {
    const licKey = e.target.result
    validateHandler({ license: licKey }, () => {
      update(licKey)
    })
  }.bind(this)
  reader.readAsText(file)
}
const validateHandler = (param, success) => {
  validateApi(param).then(success)
}
const getLicense = result => {
  return {
    status: result.status,
    corporation: result.license ? result.license.corporation : '',
    expired: result.license ? result.license.expired : '',
    count: result.license ? result.license.count : '',
    version: result.license ? result.license.version : '',
    edition: result.license ? result.license.edition : '',
    serialNo: result.license ? result.license.serialNo : '',
    remark: result.license ? result.license.remark : ''
  }
}
const update = (licKey: string) => {
  const param = { license: licKey }
  loading.value = true
  updateInfoApi(param).then(response => {
    loading.value = false
    if (response.data.status === 'valid') {
      ElMessage.success(t('about.update_success'))
      const info = getLicense(response.data)
      setLicense(info)
    } else {
      ElMessage.warning(response.data.message)
    }
  })
}
</script>
<style lang="less">
.about-card {
  background: inherit;
  margin-top: 5%;
  flex-direction: row;
  width: 640px;
  min-width: 640px;
  height: 400px;
  position: relative;
  .ed-card__header {
    padding: 0;
  }
}

.about-card-medium {
  height: 415px !important;
}
.about-card-max {
  height: 430px !important;
}
.license-header {
  height: 100px;
  background-image: url('@/assets/img/license_header.png');
  text-align: center;
  padding: 10px 0;
  background-size: 100% 100%;
}

.license-content {
  font-size: 16px;
  padding: 50px;
  table {
    width: 100%;
  }

  th {
    text-align: left;
    width: 45%;
  }
  td {
    display: table-cell;
    vertical-align: inherit;
    label {
      font-weight: 700;
    }
  }
}
.md-padding {
  padding: 10px;
}
.lic_rooter {
  flex-direction: row;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  align-content: center;
  max-width: 100%;
  justify-content: space-between;

  a {
    color: rgb(10, 123, 224);
    cursor: pointer;
  }
}
</style>

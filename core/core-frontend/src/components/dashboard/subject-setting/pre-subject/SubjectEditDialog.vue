<script lang="ts" setup>
import { computed, ref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { deepCopy } from '@/utils/utils'
import DeUpload from '@/components/visualization/common/DeUpload.vue'
const { t } = useI18n()
const loading = ref(false)

const rules = {
  name: [
    {
      required: true,
      message: t('commons.input_content'),
      trigger: 'change'
    },
    {
      max: 50,
      message: t('commons.char_can_not_more_50'),
      trigger: 'change'
    },
    { required: true, trigger: 'blur' }
  ],
  coverUrl: [
    {
      required: true,
      message: t('commons.input_content'),
      trigger: 'change'
    }
  ]
}
const subject = ref()
const subjectDialogShow = ref(false)
const optType = ref('new')

const resetForm = () => {
  subject.value.clearValidate()
  subjectDialogShow.value = false
}
const subjectForm = ref(null)
const title = computed(() => (optType.value === 'new' ? '新建主题' : '编辑主题'))

const optInit = (subjectItem, opt) => {
  optType.value = opt
  subjectDialogShow.value = true
  subjectForm.value = deepCopy(subjectItem)
}

const saveSubject = () => {
  emits('finish', subjectForm.value)
  resetForm()
}

defineExpose({
  optInit
})

const emits = defineEmits(['finish'])
const onImgChange = imgUrl => {
  subjectForm.value.coverUrl = imgUrl
}
</script>

<template>
  <el-dialog
    v-loading="loading"
    :title="title"
    v-model="subjectDialogShow"
    width="400px"
    :before-close="resetForm"
  >
    <el-form
      label-position="top"
      require-asterisk-position="right"
      ref="subject"
      :model="subjectForm"
      :rules="rules"
    >
      <el-form-item :label="'名称'" prop="name">
        <el-input v-model="subjectForm.name" />
      </el-form-item>
      <el-form-item :label="'封面'" prop="coverUrl">
        <de-upload :img-url="subjectForm.coverUrl" @onImgChange="onImgChange"></de-upload>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button secondary @click="resetForm()">取消 </el-button>
      <el-button type="primary" @click="saveSubject()">确认 </el-button>
    </template>
  </el-dialog>
</template>

<style lang="less" scoped>
:deep(.ed-dialog__header) {
  text-align: left;
}
</style>

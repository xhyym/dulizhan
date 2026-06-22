<!-- 系统设置 - 基本信息 -->
<template>
  <div class="site-basic art-full-height">
    <ElCard>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>基本信息</span>
          <ElButton type="primary" @click="handleSave" :loading="saving" v-ripple>保存</ElButton>
        </div>
      </template>

      <ElForm :model="formData" label-width="120px" style="max-width: 600px" v-loading="loading">
        <ElFormItem label="网站标题">
          <ElInput v-model="formData.site_title" placeholder="请输入网站标题" />
        </ElFormItem>
        <ElFormItem label="网站Logo">
          <ElUpload :http-request="handleLogoUpload" :show-file-list="false" accept="image/*">
            <ElImage v-if="formData.site_logo" :src="formData.site_logo" style="width: 120px; height: 40px"
              fit="contain" />
            <ElButton v-else size="small">上传Logo</ElButton>
          </ElUpload>
        </ElFormItem>
        <ElFormItem label="联系邮箱">
          <ElInput v-model="formData.contact_email" placeholder="请输入联系邮箱" />
        </ElFormItem>
        <ElFormItem label="联系WhatsApp">
          <ElInput v-model="formData.contact_whatsapp" placeholder="请输入WhatsApp号码" />
        </ElFormItem>
      </ElForm>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { fetchGetSiteConfig, fetchUpdateSiteConfig } from '@/api/site-config'
import { uploadImage } from '@/api/upload'

defineOptions({ name: 'SiteBasic' })

const loading = ref(false)
const saving = ref(false)

const formData = ref({
  site_title: '',
  site_logo: '',
  contact_email: '',
  contact_whatsapp: ''
})

async function loadData() {
  loading.value = true
  try {
    const { data } = await fetchGetSiteConfig()
    if (data) {
      Object.keys(formData.value).forEach(key => {
        if (data[key]) {
          try {
            ;(formData.value as any)[key] = JSON.parse(data[key])
          } catch {
            ;(formData.value as any)[key] = data[key]
          }
        }
      })
    }
  } finally {
    loading.value = false
  }
}

async function handleLogoUpload(options: any) {
  try {
    formData.value.site_logo = await uploadImage(options.file)
    ElMessage.success('Logo上传成功')
  } catch (e: any) {
    ElMessage.error(e.message || '上传失败')
  }
}

async function handleSave() {
  saving.value = true
  try {
    const configMap: Record<string, string> = {}
    Object.entries(formData.value).forEach(([key, value]) => {
      configMap[key] = typeof value === 'string' ? value : JSON.stringify(value)
    })
    await fetchUpdateSiteConfig(configMap)
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}

onMounted(() => loadData())
</script>

<!-- 系统设置 - 统计代码 -->
<template>
  <div class="site-analytics art-full-height">
    <ElCard>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>统计代码</span>
          <ElButton type="primary" @click="handleSave" :loading="saving" v-ripple>保存</ElButton>
        </div>
      </template>

      <ElForm :model="formData" label-width="120px" style="max-width: 700px" v-loading="loading">
        <ElDivider content-position="left">Google Analytics</ElDivider>

        <ElFormItem label="GA Tracking ID">
          <ElInput v-model="formData.ga_tracking_id" placeholder="如: G-XXXXXXXXXX 或 UA-XXXXXXXX-X" />
        </ElFormItem>
        <ElFormItem label="GA 代码">
          <ElInput v-model="formData.ga_code" type="textarea" :rows="5" placeholder="粘贴 Google Analytics 完整代码（可选，优先使用 Tracking ID）" />
        </ElFormItem>

        <ElDivider content-position="left">Facebook Pixel</ElDivider>

        <ElFormItem label="Pixel ID">
          <ElInput v-model="formData.fb_pixel_id" placeholder="如: 123456789012345" />
        </ElFormItem>
        <ElFormItem label="Pixel 代码">
          <ElInput v-model="formData.fb_pixel_code" type="textarea" :rows="5" placeholder="粘贴 Facebook Pixel 完整代码（可选，优先使用 Pixel ID）" />
        </ElFormItem>

        <ElDivider content-position="left">其他统计</ElDivider>

        <ElFormItem label="Google Tag Manager">
          <ElInput v-model="formData.gtm_id" placeholder="如: GTM-XXXXXXX" />
        </ElFormItem>
        <ElFormItem label="TikTok Pixel">
          <ElInput v-model="formData.tiktok_pixel_id" placeholder="TikTok Pixel ID" />
        </ElFormItem>

        <ElDivider content-position="left">自定义代码</ElDivider>

        <ElFormItem label="Head 自定义代码">
          <ElInput v-model="formData.custom_head" type="textarea" :rows="5" placeholder="插入到 &lt;head&gt; 标签内的自定义代码" />
        </ElFormItem>
        <ElFormItem label="Body 自定义代码">
          <ElInput v-model="formData.custom_body" type="textarea" :rows="5" placeholder="插入到 &lt;body&gt; 标签开始处的自定义代码" />
        </ElFormItem>
      </ElForm>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { fetchGetSiteConfig, fetchUpdateSiteConfig } from '@/api/site-config'

defineOptions({ name: 'SiteAnalytics' })

const loading = ref(false)
const saving = ref(false)

const formData = ref({
  ga_tracking_id: '',
  ga_code: '',
  fb_pixel_id: '',
  fb_pixel_code: '',
  gtm_id: '',
  tiktok_pixel_id: '',
  custom_head: '',
  custom_body: ''
})

async function loadData() {
  loading.value = true
  try {
    const data = await fetchGetSiteConfig()
    if (data?.analytics_config) {
      try {
        const config = JSON.parse(data.analytics_config)
        Object.assign(formData.value, config)
      } catch { }
    }
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  saving.value = true
  try {
    await fetchUpdateSiteConfig({
      analytics_config: JSON.stringify(formData.value)
    })
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}

onMounted(() => loadData())
</script>

<style scoped lang="scss">
.site-analytics {
  :deep(.el-card) {
    flex: 1;
    overflow: hidden;
    display: flex;
    flex-direction: column;
  }

  :deep(.el-card__body) {
    flex: 1;
    overflow-y: auto;
  }
}
</style>

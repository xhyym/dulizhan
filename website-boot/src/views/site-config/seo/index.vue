<!-- 系统设置 - SEO 设置 -->
<template>
  <div class="site-seo art-full-height">
    <ElCard>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>SEO 设置</span>
          <ElButton type="primary" @click="handleSave" :loading="saving" v-ripple>保存</ElButton>
        </div>
      </template>

      <ElForm :model="formData" label-width="120px" style="max-width: 700px" v-loading="loading">
        <ElDivider content-position="left">首页 SEO</ElDivider>

        <ElFormItem label="首页标题">
          <ElInput v-model="formData.home_title" placeholder="网站首页标题，建议60个字符以内" />
        </ElFormItem>
        <ElFormItem label="首页描述">
          <ElInput v-model="formData.home_description" type="textarea" :rows="3" placeholder="网站首页描述，建议160个字符以内" />
        </ElFormItem>
        <ElFormItem label="首页关键词">
          <ElInput v-model="formData.home_keywords" type="textarea" :rows="2" placeholder="多个关键词用英文逗号分隔" />
        </ElFormItem>

        <ElDivider content-position="left">商品页 SEO</ElDivider>

        <ElFormItem label="商品页标题模板">
          <ElInput v-model="formData.product_title_template" placeholder="如: {productName} - {siteName}" />
          <div class="form-tip">可用变量: {productName} {siteName} {categoryName}</div>
        </ElFormItem>
        <ElFormItem label="商品页描述模板">
          <ElInput v-model="formData.product_description_template" type="textarea" :rows="2" placeholder="如: 购买{productName}，{siteName}提供优质产品和服务" />
        </ElFormItem>

        <ElDivider content-position="left">全站 SEO</ElDivider>

        <ElFormItem label="Robots.txt">
          <ElInput v-model="formData.robots" type="textarea" :rows="5" placeholder="User-agent: *&#10;Allow: /" />
        </ElFormItem>
        <ElFormItem label="Sitemap">
          <ElSwitch v-model="formData.sitemap_enabled" />
          <span style="margin-left: 12px; color: #909399">自动生成 sitemap.xml</span>
        </ElFormItem>
      </ElForm>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { fetchGetSiteConfig, fetchUpdateSiteConfig } from '@/api/site-config'

defineOptions({ name: 'SiteSeo' })

const loading = ref(false)
const saving = ref(false)

const formData = ref({
  home_title: '',
  home_description: '',
  home_keywords: '',
  product_title_template: '',
  product_description_template: '',
  robots: '',
  sitemap_enabled: true
})

async function loadData() {
  loading.value = true
  try {
    const { data } = await fetchGetSiteConfig()
    if (data?.seo_config) {
      try {
        const config = JSON.parse(data.seo_config)
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
      seo_config: JSON.stringify(formData.value)
    })
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}

onMounted(() => loadData())
</script>

<style scoped lang="scss">
.site-seo {
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

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>

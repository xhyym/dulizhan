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
        <ElFormItem label="首页上方文案">
          <ElInput v-model="formData.hero_tagline" placeholder="请输入首页轮播图第一行文案" />
        </ElFormItem>
        <ElFormItem label="首页主标题">
          <ElInput
            v-model="formData.hero_title"
            type="textarea"
            :rows="2"
            placeholder="请输入首页轮播图第二行主标题"
          />
        </ElFormItem>
        <ElFormItem label="首页副标题">
          <ElInput
            v-model="formData.hero_subtitle"
            type="textarea"
            :rows="3"
            placeholder="请输入首页轮播图第三行副标题"
          />
        </ElFormItem>
        <ElFormItem label="网站Logo">
          <ElUpload :http-request="handleLogoUpload" :show-file-list="false" accept="image/*">
            <ElImage
              v-if="formData.site_logo"
              :src="formData.site_logo"
              style="width: 120px; height: 40px"
              fit="contain"
            />
            <ElButton v-else size="small">上传Logo</ElButton>
          </ElUpload>
        </ElFormItem>
        <ElFormItem label="Shop背景图">
          <ElUpload
            :http-request="handleProductsBannerUpload"
            :show-file-list="false"
            accept="image/*"
          >
            <ElImage
              v-if="formData.products_banner_image"
              :src="formData.products_banner_image"
              style="width: 120px; height: 60px"
              fit="cover"
            />
            <ElButton v-else size="small">上传Shop背景图</ElButton>
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
    hero_tagline: '',
    hero_title: '',
    hero_subtitle: '',
    site_logo: '',
    products_banner_image: '',
    contact_email: '',
    contact_whatsapp: ''
  })

  async function loadData() {
    loading.value = true
    try {
      const data = await fetchGetSiteConfig()
      if (data) {
        Object.keys(formData.value).forEach((key) => {
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

  async function handleProductsBannerUpload(options: any) {
    try {
      formData.value.products_banner_image = await uploadImage(options.file)
      ElMessage.success('Shop背景图上传成功')
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

<style scoped lang="scss">
  .site-basic {
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

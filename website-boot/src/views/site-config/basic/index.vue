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

      <ElForm :model="formData" label-width="120px" style="max-width: 680px" v-loading="loading">
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

        <ElFormItem label="Header Logo">
          <div class="upload-row">
            <ElUpload :http-request="handleLogoUpload" :show-file-list="false" accept="image/*">
              <div class="upload-box logo-box">
                <ElImage
                  v-if="formData.site_logo"
                  :src="formData.site_logo"
                  class="upload-image"
                  fit="contain"
                />
                <div v-else class="upload-placeholder">
                  <ElIcon class="upload-placeholder-icon"><Picture /></ElIcon>
                  <span>点击上传</span>
                </div>
              </div>
            </ElUpload>
            <ElButton
              v-if="formData.site_logo"
              type="danger"
              size="small"
              :icon="Delete"
              circle
              @click="handleDeleteLogo"
            />
          </div>
          <div class="upload-tip">图片比例需接近 3:1</div>
        </ElFormItem>

        <ElFormItem label="Title Logo">
          <div class="upload-row">
            <ElUpload :http-request="handleFaviconUpload" :show-file-list="false" accept="image/*">
              <div class="upload-box favicon-box">
                <ElImage
                  v-if="formData.site_favicon"
                  :src="formData.site_favicon"
                  class="upload-image"
                  fit="contain"
                />
                <div v-else class="upload-placeholder">
                  <ElIcon class="upload-placeholder-icon"><Picture /></ElIcon>
                  <span>点击上传</span>
                </div>
              </div>
            </ElUpload>
            <ElButton
              v-if="formData.site_favicon"
              type="danger"
              size="small"
              :icon="Delete"
              circle
              @click="handleDeleteFavicon"
            />
          </div>
          <div class="upload-tip">图片比例需接近 1:1</div>
        </ElFormItem>

        <ElFormItem label="Contact背景图">
          <div class="upload-row">
            <ElUpload :http-request="handleContactBannerUpload" :show-file-list="false" accept="image/*">
              <div class="upload-box banner-box">
                <ElImage
                  v-if="formData.contact_banner_image"
                  :src="formData.contact_banner_image"
                  class="upload-image"
                  fit="cover"
                />
                <div v-else class="upload-placeholder">
                  <ElIcon class="upload-placeholder-icon"><Picture /></ElIcon>
                  <span>点击上传 Contact 背景图</span>
                </div>
              </div>
            </ElUpload>
            <ElButton
              v-if="formData.contact_banner_image"
              type="danger"
              size="small"
              :icon="Delete"
              circle
              @click="handleDeleteContactBanner"
            />
          </div>
          <div class="upload-tip">图片比例需接近 21:9</div>
        </ElFormItem>

        <ElFormItem label="联系邮箱">
          <ElInput v-model="formData.contact_email" placeholder="请输入联系邮箱" />
        </ElFormItem>
        <ElFormItem label="联系WhatsApp">
          <ElInput v-model="formData.contact_whatsapp" placeholder="请输入WhatsApp号码" />
        </ElFormItem>
        <ElFormItem label="通知邮箱">
          <ElInput v-model="formData.notification_email" placeholder="用户提交询盘后发送通知到此邮箱" />
        </ElFormItem>
      </ElForm>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount } from 'vue'
import { onBeforeRouteLeave } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Delete, Picture } from '@element-plus/icons-vue'
import { fetchGetSiteConfig, fetchUpdateSiteConfig } from '@/api/site-config'
import { deleteImage, uploadImage } from '@/api/upload'
import { validateImageUpload, type ImageUploadRule } from '@/utils/ui/image-upload'

defineOptions({ name: 'SiteBasic' })

const loading = ref(false)
const saving = ref(false)
type ManagedImageField =
  | 'site_logo'
  | 'site_favicon'
  | 'contact_banner_image'

const SITE_LOGO_RULE: ImageUploadRule = {
  purpose: 'site_logo',
  fieldLabel: '网站 Logo',
  ratioLabel: '3:1',
  minRatio: 2.7,
  maxRatio: 3.3,
  maxSizeInBytes: 3 * 1024 * 1024
}

const SITE_FAVICON_RULE: ImageUploadRule = {
  purpose: 'site_favicon',
  fieldLabel: 'Title Logo',
  ratioLabel: '1:1',
  minRatio: 0.95,
  maxRatio: 1.05,
  maxSizeInBytes: 2 * 1024 * 1024
}

const PAGE_BANNER_RULE: ImageUploadRule = {
  purpose: 'site_page_banner',
  fieldLabel: 'Contact背景图',
  ratioLabel: '21:9',
  minRatio: 2.28,
  maxRatio: 2.39,
  maxSizeInBytes: 8 * 1024 * 1024
}

const formData = ref({
  site_title: '',
  hero_tagline: '',
  hero_title: '',
  hero_subtitle: '',
  site_logo: '',
  site_favicon: '',
  contact_banner_image: '',
  contact_email: '',
  contact_whatsapp: '',
  notification_email: ''
})

const pendingUploadUrls = ref<string[]>([])

function trackPendingUpload(fileUrl: string) {
  if (!fileUrl) {
    return
  }

  if (!pendingUploadUrls.value.includes(fileUrl)) {
    pendingUploadUrls.value.push(fileUrl)
  }
}

function untrackPendingUpload(fileUrl: string) {
  pendingUploadUrls.value = pendingUploadUrls.value.filter((item) => item !== fileUrl)
}

async function deletePendingUploadIfNeeded(fileUrl?: string) {
  const normalizedFileUrl = fileUrl?.trim() || ''
  if (!normalizedFileUrl || !pendingUploadUrls.value.includes(normalizedFileUrl)) {
    return
  }

  try {
    await deleteImage(normalizedFileUrl)
  } catch (error) {
    console.error('删除未保存站点图片失败', error)
  } finally {
    untrackPendingUpload(normalizedFileUrl)
  }
}

async function cleanupPendingUploads() {
  const temporaryFileUrls = [...new Set(pendingUploadUrls.value)]
  for (const fileUrl of temporaryFileUrls) {
    await deletePendingUploadIfNeeded(fileUrl)
  }
}

async function replaceManagedImage(field: ManagedImageField, newFileUrl: string) {
  const previousFileUrl = formData.value[field]
  await deletePendingUploadIfNeeded(previousFileUrl)
  formData.value[field] = newFileUrl
  trackPendingUpload(newFileUrl)
}

async function clearManagedImage(field: ManagedImageField, successMessage: string) {
  await deletePendingUploadIfNeeded(formData.value[field])
  formData.value[field] = ''
  ElMessage.success(successMessage)
}

async function loadData() {
  loading.value = true
  try {
    const data = await fetchGetSiteConfig()
    if (data) {
      Object.keys(formData.value).forEach((key) => {
        if (data[key] !== undefined) {
          ;(formData.value as Record<string, string>)[key] = data[key]
        }
      })
    }
  } finally {
    loading.value = false
  }
}

async function handleLogoUpload(options: any) {
  try {
    const imageDimensions = await validateImageUpload(options.file, SITE_LOGO_RULE)
    const fileUrl = await uploadImage(options.file, SITE_LOGO_RULE.purpose, imageDimensions)
    await replaceManagedImage('site_logo', fileUrl)
    ElMessage.success('Logo上传成功')
  } catch (e: any) {
    ElMessage.error(e.message || '上传失败')
  }
}

async function handleFaviconUpload(options: any) {
  try {
    const imageDimensions = await validateImageUpload(options.file, SITE_FAVICON_RULE)
    const fileUrl = await uploadImage(options.file, SITE_FAVICON_RULE.purpose, imageDimensions)
    await replaceManagedImage('site_favicon', fileUrl)
    ElMessage.success('Title Logo 上传成功')
  } catch (e: any) {
    ElMessage.error(e.message || '上传失败')
  }
}

async function handleContactBannerUpload(options: any) {
  try {
    const imageDimensions = await validateImageUpload(options.file, PAGE_BANNER_RULE)
    const fileUrl = await uploadImage(options.file, PAGE_BANNER_RULE.purpose, imageDimensions)
    await replaceManagedImage('contact_banner_image', fileUrl)
    ElMessage.success('Contact背景图上传成功')
  } catch (e: any) {
    ElMessage.error(e.message || '上传失败')
  }
}

async function handleDeleteLogo() {
  await clearManagedImage('site_logo', 'Logo 已删除')
}

async function handleDeleteFavicon() {
  await clearManagedImage('site_favicon', 'Title Logo 已删除')
}

async function handleDeleteContactBanner() {
  await clearManagedImage('contact_banner_image', 'Contact背景图已删除')
}

async function handleSave() {
  saving.value = true
  try {
    await fetchUpdateSiteConfig({
      ...formData.value,
      products_banner_image: '',
      about_banner_image: ''
    })
    pendingUploadUrls.value = []
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}

onMounted(() => loadData())
onBeforeRouteLeave(async () => {
  await cleanupPendingUploads()
})
onBeforeUnmount(() => {
  void cleanupPendingUploads()
})
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

.upload-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.upload-box {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  overflow: hidden;
  background: var(--el-fill-color-lighter);
  cursor: pointer;
}

.logo-box {
  width: 180px;
  height: 72px;
}

.favicon-box {
  width: 72px;
  height: 72px;
}

.banner-box {
  width: 180px;
  height: 72px;
}

.upload-image {
  width: 100%;
  height: 100%;
  display: block;
}

.upload-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  text-align: center;
  padding: 8px;
  line-height: 1.4;
}

.upload-placeholder-icon {
  font-size: 18px;
}

.upload-tip {
  margin-top: 6px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.6;
}
</style>

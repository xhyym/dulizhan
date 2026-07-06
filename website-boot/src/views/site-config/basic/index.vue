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
          <div class="upload-tip">建议不低于 360×120，图片比例接近 3:1</div>
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
          <div class="upload-tip">建议不低于 256×256，图片比例接近 1:1</div>
        </ElFormItem>

        <ElFormItem label="Shop背景图">
          <div class="upload-row">
            <ElUpload :http-request="handleProductsBannerUpload" :show-file-list="false" accept="image/*">
              <div class="upload-box banner-box">
                <ElImage
                  v-if="formData.products_banner_image"
                  :src="formData.products_banner_image"
                  class="upload-image"
                  fit="cover"
                />
                <div v-else class="upload-placeholder">
                  <ElIcon class="upload-placeholder-icon"><Picture /></ElIcon>
                  <span>点击上传</span>
                </div>
              </div>
            </ElUpload>
            <ElButton
              v-if="formData.products_banner_image"
              type="danger"
              size="small"
              :icon="Delete"
              circle
              @click="handleDeleteProductsBanner"
            />
          </div>
          <div class="upload-tip">建议不低于 1920×600，图片比例接近 16:5</div>
        </ElFormItem>

        <ElFormItem label="About背景图">
          <div class="upload-row">
            <ElUpload :http-request="handleAboutBannerUpload" :show-file-list="false" accept="image/*">
              <div class="upload-box banner-box">
                <ElImage
                  v-if="formData.about_banner_image"
                  :src="formData.about_banner_image"
                  class="upload-image"
                  fit="cover"
                />
                <div v-else class="upload-placeholder">
                  <ElIcon class="upload-placeholder-icon"><Picture /></ElIcon>
                  <span>点击上传</span>
                </div>
              </div>
            </ElUpload>
            <ElButton
              v-if="formData.about_banner_image"
              type="danger"
              size="small"
              :icon="Delete"
              circle
              @click="handleDeleteAboutBanner"
            />
          </div>
          <div class="upload-tip">建议不低于 1920×600，图片比例接近 16:5</div>
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
          <div class="upload-tip">建议不低于 1920×600，图片比例接近 16:5</div>
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
import { ElMessage } from 'element-plus'
import { Delete, Picture } from '@element-plus/icons-vue'
import { fetchGetSiteConfig, fetchUpdateSiteConfig } from '@/api/site-config'
import { uploadImage } from '@/api/upload'

defineOptions({ name: 'SiteBasic' })

interface ImageSize {
  width: number
  height: number
}

const loading = ref(false)
const saving = ref(false)

const formData = ref({
  site_title: '',
  hero_tagline: '',
  hero_title: '',
  hero_subtitle: '',
  site_logo: '',
  site_favicon: '',
  products_banner_image: '',
  about_banner_image: '',
  contact_banner_image: '',
  contact_email: '',
  contact_whatsapp: '',
  notification_email: ''
})

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

function readImageSize(file: File): Promise<ImageSize> {
  return new Promise((resolve, reject) => {
    const imageUrl = URL.createObjectURL(file)
    const image = new Image()

    image.onload = () => {
      const imageSize = { width: image.width, height: image.height }
      URL.revokeObjectURL(imageUrl)
      resolve(imageSize)
    }

    image.onerror = () => {
      URL.revokeObjectURL(imageUrl)
      reject(new Error('图片解析失败，请更换文件后重试'))
    }

    image.src = imageUrl
  })
}

async function validateImage(file: File, minWidth: number, minHeight: number, minRatio: number, maxRatio: number, label: string, ratioLabel: string) {
  const { width, height } = await readImageSize(file)
  if (width < minWidth || height < minHeight) {
    throw new Error(`${label}分辨率不能低于 ${minWidth}×${minHeight}`)
  }

  const ratio = width / height
  if (ratio < minRatio || ratio > maxRatio) {
    throw new Error(`${label}比例不符合要求，请上传接近 ${ratioLabel} 的图片`)
  }
}

async function handleLogoUpload(options: any) {
  try {
    await validateImage(options.file, 360, 120, 2.7, 3.3, '网站 Logo', '3:1')
    formData.value.site_logo = await uploadImage(options.file)
    ElMessage.success('Logo上传成功')
  } catch (e: any) {
    ElMessage.error(e.message || '上传失败')
  }
}

async function handleFaviconUpload(options: any) {
  try {
    await validateImage(options.file, 256, 256, 0.95, 1.05, 'Title Logo', '1:1')
    formData.value.site_favicon = await uploadImage(options.file)
    ElMessage.success('Title Logo 上传成功')
  } catch (e: any) {
    ElMessage.error(e.message || '上传失败')
  }
}

async function handleProductsBannerUpload(options: any) {
  try {
    await validateImage(options.file, 1920, 600, 3.1, 3.3, 'Shop背景图', '16:5')
    formData.value.products_banner_image = await uploadImage(options.file)
    ElMessage.success('Shop背景图上传成功')
  } catch (e: any) {
    ElMessage.error(e.message || '上传失败')
  }
}

async function handleAboutBannerUpload(options: any) {
  try {
    await validateImage(options.file, 1920, 600, 3.1, 3.3, 'About背景图', '16:5')
    formData.value.about_banner_image = await uploadImage(options.file)
    ElMessage.success('About背景图上传成功')
  } catch (e: any) {
    ElMessage.error(e.message || '上传失败')
  }
}

async function handleContactBannerUpload(options: any) {
  try {
    await validateImage(options.file, 1920, 600, 3.1, 3.3, 'Contact背景图', '16:5')
    formData.value.contact_banner_image = await uploadImage(options.file)
    ElMessage.success('Contact背景图上传成功')
  } catch (e: any) {
    ElMessage.error(e.message || '上传失败')
  }
}

function handleDeleteLogo() {
  formData.value.site_logo = ''
  ElMessage.success('Logo 已删除')
}

function handleDeleteFavicon() {
  formData.value.site_favicon = ''
  ElMessage.success('Title Logo 已删除')
}

function handleDeleteProductsBanner() {
  formData.value.products_banner_image = ''
  ElMessage.success('Shop背景图已删除')
}

function handleDeleteAboutBanner() {
  formData.value.about_banner_image = ''
  ElMessage.success('About背景图已删除')
}

function handleDeleteContactBanner() {
  formData.value.contact_banner_image = ''
  ElMessage.success('Contact背景图已删除')
}

async function handleSave() {
  saving.value = true
  try {
    await fetchUpdateSiteConfig({ ...formData.value })
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

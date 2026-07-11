<!-- 系统设置 - 轮播图管理 -->
<template>
  <div class="site-banner art-full-height">
    <ElCard>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>轮播图管理</span>
          <ElSpace>
            <ElButton type="primary" @click="addBanner" v-ripple>添加轮播图</ElButton>
            <ElButton type="success" @click="handleSave" :loading="saving" v-ripple>保存</ElButton>
          </ElSpace>
        </div>
      </template>

      <div v-loading="loading">
        <ElEmpty v-if="banners.length === 0" description="暂无轮播图" />

        <div v-else class="banner-grid">
          <ElCard v-for="(banner, index) in banners" :key="index" class="banner-item" shadow="hover">
            <ElImage :src="banner.image" class="banner-image" fit="cover" />
            <div class="banner-form">
              <ElInput v-model="banner.title" placeholder="标题(可选)" size="small" class="banner-input" />
              <ElInput v-model="banner.link" placeholder="链接(可选)" size="small" class="banner-input" />
              <div class="banner-tip">建议使用 16:9 横版图片，避免首页轮播裁切变形</div>
              <div class="banner-actions">
                <ElUpload :http-request="(opt: any) => handleBannerUpload(opt, index)" :show-file-list="false"
                  accept="image/*">
                  <ElButton size="small" type="primary" link>更换图片</ElButton>
                </ElUpload>
                <ElButton size="small" type="danger" link @click="handleRemoveBanner(index)">删除</ElButton>
              </div>
            </div>
          </ElCard>
        </div>
      </div>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount } from 'vue'
import { onBeforeRouteLeave } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchGetSiteConfig, fetchUpdateSiteConfig } from '@/api/site-config'
import { deleteImage, uploadImage } from '@/api/upload'
import { validateImageUpload, type ImageUploadRule } from '@/utils/ui/image-upload'

defineOptions({ name: 'SiteBanner' })

const loading = ref(false)
const saving = ref(false)
const pendingUploadUrls = ref<string[]>([])

interface Banner {
  image: string
  title: string
  link: string
}

const banners = ref<Banner[]>([])
const HOME_BANNER_RULE: ImageUploadRule = {
  purpose: 'home_banner',
  fieldLabel: '轮播图',
  minWidth: 1600,
  minHeight: 900,
  ratioLabel: '16:9',
  minRatio: 1.72,
  maxRatio: 1.83,
  maxSizeInBytes: 10 * 1024 * 1024
}

async function loadData() {
  loading.value = true
  try {
    const data = await fetchGetSiteConfig()
    if (data?.banner_images) {
      try {
        banners.value = JSON.parse(data.banner_images)
      } catch {
        banners.value = []
      }
    }
  } finally {
    loading.value = false
  }
}

function addBanner() {
  banners.value.push({ image: '', title: '', link: '' })
}

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
    console.error('删除未保存轮播图失败', error)
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

async function handleBannerUpload(options: any, index: number) {
  try {
    await validateImageUpload(options.file, HOME_BANNER_RULE)
    const fileUrl = await uploadImage(options.file, HOME_BANNER_RULE.purpose)
    await deletePendingUploadIfNeeded(banners.value[index]?.image)
    trackPendingUpload(fileUrl)
    banners.value[index].image = fileUrl
    ElMessage.success('上传成功')
  } catch (e: any) {
    ElMessage.error(e.message || '上传失败')
  }
}

async function handleRemoveBanner(index: number) {
  const removedBanner = banners.value[index]
  if (removedBanner?.image) {
    await deletePendingUploadIfNeeded(removedBanner.image)
  }
  banners.value.splice(index, 1)
}

async function handleSave() {
  saving.value = true
  try {
    await fetchUpdateSiteConfig({
      banner_images: JSON.stringify(banners.value)
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
.site-banner {
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

.banner-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
  align-items: start;
}

.banner-item {
  min-width: 0;
}

.banner-image {
  width: 100%;
  height: 160px;
}

.banner-form {
  margin-top: 12px;
}

.banner-input {
  margin-bottom: 8px;
}

.banner-tip {
  margin-bottom: 8px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.banner-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
</style>

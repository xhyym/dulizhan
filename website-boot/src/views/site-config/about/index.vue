<!-- 系统设置 - 关于我们 -->
<template>
  <div class="site-about art-full-height">
    <ElCard>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>关于我们</span>
          <ElButton type="primary" @click="handleSave" :loading="saving" v-ripple>保存</ElButton>
        </div>
      </template>

      <div v-loading="loading" class="about-form">
        <!-- 顶部 Banner -->
        <ElDivider content-position="left">顶部 Banner</ElDivider>
        <ElForm label-width="120px">
          <ElFormItem label="Banner图片">
            <div class="image-upload-area">
              <ElImage
                v-if="formData.bannerImage"
                :src="formData.bannerImage"
                style="width: 300px; height: 150px"
                fit="cover"
                :preview-src-list="[formData.bannerImage]"
                preview-teleported
              />
              <ElUpload
                v-else
                :http-request="(e: any) => handleImageUpload(e, 'bannerImage')"
                :show-file-list="false"
                accept="image/*"
              >
                <ElIcon class="upload-trigger"><Plus /></ElIcon>
              </ElUpload>
              <ElIcon
                v-if="formData.bannerImage"
                class="delete-btn"
                @click="formData.bannerImage = ''"
              >
                <CircleClose />
              </ElIcon>
            </div>
            <div class="image-tip">建议尺寸不低于 1920×600，图片比例接近 16:5</div>
          </ElFormItem>
        </ElForm>

        <!-- 品牌故事 -->
        <ElDivider content-position="left">品牌故事</ElDivider>
        <ElForm label-width="120px">
          <ElFormItem label="标题">
            <ElInput v-model="formData.storyTitle" placeholder="如: OUR STORY" />
          </ElFormItem>
          <ElFormItem label="故事图片">
            <div class="image-upload-area">
              <ElImage
                v-if="formData.storyImage"
                :src="formData.storyImage"
                style="width: 200px; height: 200px"
                fit="cover"
                :preview-src-list="[formData.storyImage]"
                preview-teleported
              />
              <ElUpload
                v-else
                :http-request="(e: any) => handleImageUpload(e, 'storyImage')"
                :show-file-list="false"
                accept="image/*"
              >
                <ElIcon class="upload-trigger"><Plus /></ElIcon>
              </ElUpload>
              <ElIcon
                v-if="formData.storyImage"
                class="delete-btn"
                @click="formData.storyImage = ''"
              >
                <CircleClose />
              </ElIcon>
            </div>
            <div class="image-tip">建议尺寸不低于 800×1000，图片比例接近 4:5</div>
          </ElFormItem>
          <ElFormItem label="故事内容">
            <ElInput
              v-model="formData.storyContent"
              type="textarea"
              :rows="4"
              placeholder="品牌故事描述"
            />
          </ElFormItem>
        </ElForm>

        <!-- 品牌理念 -->
        <ElDivider content-position="left">品牌理念（3项）</ElDivider>
        <div class="philosophy-list">
          <div v-for="(item, index) in formData.philosophy" :key="index" class="philosophy-item">
            <ElForm label-width="80px">
              <ElFormItem label="图标">
                <ElSelect v-model="item.icon" placeholder="选择图标">
                  <ElOption label="品质" value="quality" />
                  <ElOption label="简约" value="simple" />
                  <ElOption label="可持续" value="sustainable" />
                  <ElOption label="工艺" value="craft" />
                  <ElOption label="设计" value="design" />
                  <ElOption label="服务" value="service" />
                </ElSelect>
              </ElFormItem>
              <ElFormItem label="标题">
                <ElInput v-model="item.title" placeholder="如: QUALITY" />
              </ElFormItem>
              <ElFormItem label="描述">
                <ElInput v-model="item.desc" placeholder="简短描述" />
              </ElFormItem>
            </ElForm>
          </div>
        </div>

        <!-- 工艺展示 -->
        <ElDivider content-position="left">工艺展示</ElDivider>
        <ElForm label-width="120px">
          <ElFormItem label="展示图片">
            <div class="image-upload-area">
              <ElImage
                v-if="formData.craftImage"
                :src="formData.craftImage"
                style="width: 300px; height: 200px"
                fit="cover"
                :preview-src-list="[formData.craftImage]"
                preview-teleported
              />
              <ElUpload
                v-else
                :http-request="(e: any) => handleImageUpload(e, 'craftImage')"
                :show-file-list="false"
                accept="image/*"
              >
                <ElIcon class="upload-trigger"><Plus /></ElIcon>
              </ElUpload>
              <ElIcon
                v-if="formData.craftImage"
                class="delete-btn"
                @click="formData.craftImage = ''"
              >
                <CircleClose />
              </ElIcon>
            </div>
            <div class="image-tip">建议尺寸不低于 1200×800，图片比例接近 3:2</div>
          </ElFormItem>
          <ElFormItem label="标题">
            <ElInput v-model="formData.craftTitle" placeholder="如: HANDCRAFTED WITH CARE" />
          </ElFormItem>
          <ElFormItem label="内容">
            <ElInput
              v-model="formData.craftContent"
              type="textarea"
              :rows="3"
              placeholder="工艺描述"
            />
          </ElFormItem>
        </ElForm>

        <!-- 数据统计 -->
        <ElDivider content-position="left">数据统计（3项）</ElDivider>
        <div class="stats-list">
          <div v-for="(item, index) in formData.stats" :key="index" class="stats-item">
            <ElForm label-width="80px">
              <ElFormItem label="数字">
                <ElInput v-model="item.number" placeholder="如: 500+" />
              </ElFormItem>
              <ElFormItem label="标签">
                <ElInput v-model="item.label" placeholder="如: Products Sold" />
              </ElFormItem>
            </ElForm>
          </div>
        </div>

        <!-- 底部 CTA -->
        <ElDivider content-position="left">底部 CTA</ElDivider>
        <ElForm label-width="120px">
          <ElFormItem label="CTA文案">
            <ElInput v-model="formData.ctaText" placeholder="如: READY TO FURNISH YOUR SPACE?" />
          </ElFormItem>
          <ElFormItem label="按钮文字">
            <ElInput v-model="formData.ctaButtonText" placeholder="如: EXPLORE OUR COLLECTION" />
          </ElFormItem>
        </ElForm>
      </div>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { Plus, CircleClose } from '@element-plus/icons-vue'
import { fetchGetSiteConfig, fetchUpdateSiteConfig } from '@/api/site-config'
import { uploadImage } from '@/api/upload'
import { validateImageUpload } from '@/utils/ui/image-upload'

defineOptions({ name: 'SiteAbout' })

const loading = ref(false)
const saving = ref(false)

interface PhilosophyItem {
  icon: string
  title: string
  desc: string
}

interface StatsItem {
  number: string
  label: string
}

interface ImageRule {
  purpose: 'site_page_banner' | 'about_story' | 'about_craft'
  fieldLabel: string
  minWidth: number
  minHeight: number
  ratioLabel: string
  minRatio: number
  maxRatio: number
}

const ABOUT_IMAGE_RULES: Record<'bannerImage' | 'storyImage' | 'craftImage', ImageRule> = {
  bannerImage: {
    purpose: 'site_page_banner',
    fieldLabel: '顶部Banner图片',
    minWidth: 1920,
    minHeight: 600,
    ratioLabel: '16:5',
    minRatio: 3.1,
    maxRatio: 3.3
  },
  storyImage: {
    purpose: 'about_story',
    fieldLabel: '品牌故事图片',
    minWidth: 800,
    minHeight: 1000,
    ratioLabel: '4:5',
    minRatio: 0.76,
    maxRatio: 0.84
  },
  craftImage: {
    purpose: 'about_craft',
    fieldLabel: '工艺展示图片',
    minWidth: 1200,
    minHeight: 800,
    ratioLabel: '3:2',
    minRatio: 1.45,
    maxRatio: 1.55
  }
}

const formData = ref({
  bannerImage: '',
  storyImage: '',
  storyTitle: 'OUR STORY',
  storyContent: '',
  philosophy: [
    { icon: 'quality', title: 'QUALITY', desc: '' },
    { icon: 'simple', title: 'SIMPLE', desc: '' },
    { icon: 'sustainable', title: 'SUSTAINABLE', desc: '' }
  ] as PhilosophyItem[],
  craftImage: '',
  craftTitle: 'HANDCRAFTED WITH CARE',
  craftContent: '',
  stats: [
    { number: '', label: '' },
    { number: '', label: '' },
    { number: '', label: '' }
  ] as StatsItem[],
  ctaText: '',
  ctaButtonText: ''
})

async function loadData() {
  loading.value = true
  try {
    const data = await fetchGetSiteConfig()
    if (data && data.about_us) {
      try {
        const aboutData = JSON.parse(data.about_us)
        Object.assign(formData.value, aboutData)
      } catch {
        // 解析失败使用默认值
      }
    }
  } finally {
    loading.value = false
  }
}

async function handleImageUpload(options: any, field: 'bannerImage' | 'storyImage' | 'craftImage') {
  try {
    const imageRule = ABOUT_IMAGE_RULES[field]
    await validateImageUpload(options.file, {
      ...imageRule,
      maxSizeInBytes: imageRule.purpose === 'about_story' ? 6 * 1024 * 1024 : 8 * 1024 * 1024
    })
    const url = await uploadImage(options.file, imageRule.purpose)
    formData.value[field] = url
    ElMessage.success('图片上传成功')
  } catch (e: any) {
    ElMessage.error(e.message || '上传失败')
  }
}

async function handleSave() {
  saving.value = true
  try {
    await fetchUpdateSiteConfig({
      about_us: JSON.stringify(formData.value)
    })
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}

onMounted(() => loadData())
</script>

<style scoped lang="scss">
.site-about {
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

.about-form {
  max-width: 800px;
}

.philosophy-list,
.stats-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.philosophy-item,
.stats-item {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 4px;
}

.image-upload-area {
  position: relative;
  display: inline-block;

  .delete-btn {
    position: absolute;
    top: -6px;
    right: -6px;
    cursor: pointer;
    color: #f56c6c;
    font-size: 18px;
    background: #fff;
    border-radius: 50%;
    z-index: 1;
  }
}

.upload-trigger {
  width: 100px;
  height: 100px;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #8c939d;
  transition: border-color 0.3s;

  &:hover {
    border-color: #409eff;
    color: #409eff;
  }
}

.image-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}
</style>

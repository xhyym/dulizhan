<!-- 分类管理页面 -->
<template>
  <div class="category-page art-full-height">
    <!-- 搜索栏 -->
    <ElCard class="art-search-card">
      <ElForm :model="searchForm" inline class="search-form-compact">
        <ElFormItem label="分类名称">
          <ElInput v-model="searchForm.name" placeholder="请输入分类名称" clearable style="width: 180px"
            @keyup.enter="loadData" />
        </ElFormItem>
        <ElFormItem label="状态">
          <ElSelect v-model="searchForm.status" placeholder="全部" clearable style="width: 100px">
            <ElOption label="正常" :value="1" />
            <ElOption label="禁用" :value="0" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" @click="loadData" v-ripple>搜索</ElButton>
          <ElButton @click="resetSearch" v-ripple>重置</ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <!-- 表格 -->
    <ElCard class="art-table-card">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="loadData">
        <template #left>
          <ElButton type="primary" @click="showDialog('add')" v-ripple>新增分类</ElButton>
        </template>
      </ArtTableHeader>

      <ArtTable
        :loading="loading"
        :data="filteredData"
        :columns="columns"
        row-key="id"
        default-expand-all
      >
        <template #rowNumber="{ row }">
          <span class="row-number-cell">{{ rowIndexMap.get(row.id) ?? '-' }}</span>
        </template>

        <template #featured="{ row }">
          <div class="featured-cell">
            <ElCheckbox
              v-if="isRootCategory(row)"
              :model-value="featuredCategoryIds.includes(row.id)"
              :disabled="savingFeatured"
              @change="(checked) => handleFeaturedCategoryChange(row, Boolean(checked))"
            />
            <span v-else class="featured-placeholder">仅一级分类</span>
          </div>
        </template>
      </ArtTable>
    </ElCard>

    <!-- 分类弹窗 -->
    <ElDialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增分类' : '编辑分类'"
      width="500px"
      :before-close="handleDialogBeforeClose"
    >
      <ElForm :model="formData" label-width="80px">
        <ElFormItem label="分类名称">
          <ElInput v-model="formData.name" placeholder="请输入分类名称" />
        </ElFormItem>
        <ElFormItem label="分类图片">
          <div class="category-image-upload">
            <div v-if="!isTopLevelCategory" class="image-tip">
              当前为二级分类，仅一级分类支持上传分类图片
            </div>
            <div v-else-if="formData.image" class="image-preview">
              <img :src="formData.image" alt="分类图片" />
              <ElIcon class="remove-btn" @click="handleDeleteCategoryImage"><Close /></ElIcon>
            </div>
            <ElUpload
              v-else
              :http-request="handleImageUpload"
              :show-file-list="false"
              accept="image/*"
            >
              <div class="upload-trigger">
                <ElIcon><Plus /></ElIcon>
                <span>上传图片</span>
              </div>
            </ElUpload>
            <div class="upload-tip">仅一级分类支持上传分类图片，建议尺寸不低于 600×800，比例接近 3:4</div>
          </div>
        </ElFormItem>
        <ElFormItem label="父级分类">
          <ElTreeSelect v-model="formData.parentId" :data="treeData" :props="{ label: 'name', value: 'id' }"
            placeholder="顶级分类" clearable check-strictly />
        </ElFormItem>
        <ElFormItem v-if="!isTopLevelCategory" label="层级说明">
          <div class="level-tip">当前分类将作为二级分类创建，系统不允许继续向下创建三级分类。</div>
        </ElFormItem>
        <ElFormItem label="状态">
          <ElRadioGroup v-model="formData.status">
            <ElRadio :value="1">正常</ElRadio>
            <ElRadio :value="0">禁用</ElRadio>
          </ElRadioGroup>
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="handleCancelDialog">取消</ElButton>
        <ElButton type="primary" @click="handleSubmit" :loading="submitting">确定</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
import { computed, h, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { onBeforeRouteLeave } from 'vue-router'
import { ElMessageBox, ElMessage, ElTag } from 'element-plus'
import { useTableColumns } from '@/hooks/core/useTableColumns'
import { fetchGetCategoryList, fetchCreateCategory, fetchUpdateCategory, fetchDeleteCategory } from '@/api/product'
import { fetchGetSiteConfig, fetchUpdateSiteConfig } from '@/api/site-config'
import { deleteImage, uploadImage } from '@/api/upload'
import { Plus, Close } from '@element-plus/icons-vue'
import { validateImageUpload, type ImageUploadRule } from '@/utils/ui/image-upload'
import type { DialogBeforeCloseFn } from 'element-plus'

defineOptions({ name: 'Category' })

const loading = ref(false)
const submitting = ref(false)
const savingFeatured = ref(false)
const dialogVisible = ref(false)
const dialogType = ref<'add' | 'edit'>('add')
const tableData = ref<Api.Product.Category[]>([])
const editingCategoryHasChildren = ref(false)
const featuredCategoryIds = ref<number[]>([])
const pendingUploadedImageUrl = ref('')

const CATEGORY_IMAGE_RULE: ImageUploadRule = {
  purpose: 'category_image',
  fieldLabel: '分类图片',
  minWidth: 600,
  minHeight: 800,
  ratioLabel: '3:4',
  minRatio: 0.72,
  maxRatio: 0.78,
  maxSizeInBytes: 5 * 1024 * 1024
}

// 搜索表单
const searchForm = ref({ name: '', status: undefined as number | undefined })

// 搜索后的过滤数据
const filteredData = computed(() => {
  let result = tableData.value
  if (searchForm.value.name) {
    result = filterTree(result, (item) =>
      item.name.toLowerCase().includes(searchForm.value.name!.toLowerCase())
    )
  }
  if (searchForm.value.status !== undefined && searchForm.value.status !== null) {
    result = filterTree(result, (item) => item.status === searchForm.value.status)
  }
  return result
})

const rowIndexMap = computed(() => {
  const indexMap = new Map<number, number>()
  let currentIndex = 1

  const walkTree = (rows: Api.Product.Category[]) => {
    rows.forEach((row) => {
      indexMap.set(row.id, currentIndex)
      currentIndex += 1
      if (Array.isArray(row.children) && row.children.length > 0) {
        walkTree(row.children)
      }
    })
  }

  walkTree(filteredData.value)
  return indexMap
})

// 递归过滤树形数据
function filterTree(tree: Api.Product.Category[], predicate: (item: Api.Product.Category) => boolean): Api.Product.Category[] {
  return tree
    .map((item) => {
      const children = item.children ? filterTree(item.children, predicate) : []
      return { ...item, children }
    })
    .filter((item) => predicate(item) || (item.children && item.children.length > 0))
}

// 表单数据
const formData = ref<Api.Product.CategoryDTO>({
  id: undefined as number | undefined,
  name: '',
  image: '',
  parentId: 0,
  sort: 0,
  status: 1
})

const treeData = computed(() => {
  const topLevelCategories = tableData.value
    .filter((item) => item.parentId === 0 && item.id !== formData.value.id)
    .map((item) => ({ ...item, children: [] }))

  return [{ id: 0, name: '顶级分类', children: topLevelCategories }]
})

const isTopLevelCategory = computed(() => Number(formData.value.parentId ?? 0) === 0)

function formatDateTime(dateTime?: string) {
  if (!dateTime) {
    return '-'
  }
  return dateTime.replace('T', ' ').replace(/\//g, '-')
}

// 列配置
const { columns, columnChecks } = useTableColumns(() => [
  {
    prop: 'rowNumber',
    label: '序号',
    width: 90,
    useSlot: true
  },
  {
    prop: 'image',
    label: '图片',
    width: 80,
    formatter: (row: any) =>
      row.image
        ? h('img', { src: row.image, style: 'width:40px;height:40px;object-fit:cover;border-radius:4px' })
        : h('span', { style: 'color:#ccc;font-size:12px' }, '无')
  },
  { prop: 'name', label: '分类名称', minWidth: 200 },
  {
    prop: 'featured',
    label: '首页展示',
    width: 120,
    align: 'center',
    useSlot: true
  },
  {
    prop: 'status',
    label: '状态',
    width: 100,
    align: 'center',
    formatter: (row: any) =>
      h(ElTag, { type: row.status === 1 ? 'success' : 'info' }, () =>
        row.status === 1 ? '正常' : '禁用'
      )
  },
  {
    prop: 'createTime',
    label: '创建时间',
    width: 180,
    formatter: (row: Api.Product.Category) => formatDateTime(row.createTime)
  },
  {
    prop: 'operation',
    label: '操作',
    width: 200,
    fixed: 'right',
    formatter: (row: any) =>
      h('div', [
        h('button', {
          class: 'el-button el-button--primary is-link',
          onClick: () => showDialog('edit', row)
        }, '编辑'),
        h('button', {
          class: 'el-button el-button--danger is-link',
          onClick: () => handleDelete(row)
        }, '删除')
      ])
  }
])

async function loadData() {
  loading.value = true
  try {
    const [categoryData, siteConfig] = await Promise.all([
      fetchGetCategoryList(),
      fetchGetSiteConfig()
    ])
    tableData.value = categoryData || []
    const normalizedFeaturedCategoryIds = normalizeFeaturedCategoryIds(
      siteConfig?.featured_category_ids,
      tableData.value
    )
    featuredCategoryIds.value = normalizedFeaturedCategoryIds

    if (shouldSyncFeaturedCategoryIds(siteConfig?.featured_category_ids, normalizedFeaturedCategoryIds)) {
      await persistFeaturedCategoryIds(normalizedFeaturedCategoryIds, true)
    }
  } catch (error) {
    ElMessage.error('分类数据加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

function resetSearch() {
  searchForm.value = { name: '', status: undefined }
}

function showDialog(type: 'add' | 'edit', row?: any) {
  dialogType.value = type
  pendingUploadedImageUrl.value = ''
  if (type === 'edit' && row) {
    editingCategoryHasChildren.value = Array.isArray(row.children) && row.children.length > 0
    formData.value = {
      id: row.id,
      name: row.name,
      image: row.parentId === 0 ? row.image || '' : '',
      parentId: row.parentId,
      sort: row.sort,
      status: row.status
    }
  } else {
    editingCategoryHasChildren.value = false
    formData.value = { id: undefined, name: '', image: '', parentId: 0, sort: 0, status: 1 }
  }
  dialogVisible.value = true
}

function isRootCategory(row: Api.Product.Category) {
  return Number(row.parentId) === 0
}

function parseFeaturedCategoryIds(value?: string) {
  if (!value?.trim()) {
    return []
  }

  try {
    const parsed = JSON.parse(value) as unknown
    if (!Array.isArray(parsed)) {
      return []
    }

    return parsed
      .map((item) => Number(item))
      .filter((item) => Number.isInteger(item) && item > 0)
  } catch (error) {
    return value
      .split(',')
      .map((item) => Number(item.trim()))
      .filter((item) => Number.isInteger(item) && item > 0)
  }
}

function getRootCategories(categories: Api.Product.Category[]) {
  return categories.filter((category) => isRootCategory(category))
}

function sortByCreateTimeDesc(categories: Api.Product.Category[]) {
  return [...categories].sort(
    (left, right) => new Date(right.createTime).getTime() - new Date(left.createTime).getTime()
  )
}

function normalizeFeaturedCategoryIds(value: string | undefined, categories: Api.Product.Category[]) {
  const rootCategories = getRootCategories(categories)
  const rootCategoryIdSet = new Set(rootCategories.map((category) => category.id))
  const normalizedIds: number[] = []

  parseFeaturedCategoryIds(value).forEach((categoryId) => {
    if (rootCategoryIdSet.has(categoryId) && !normalizedIds.includes(categoryId) && normalizedIds.length < 4) {
      normalizedIds.push(categoryId)
    }
  })

  if (normalizedIds.length > 0 || rootCategories.length === 0) {
    return normalizedIds
  }

  return sortByCreateTimeDesc(rootCategories)
    .slice(0, 4)
    .map((category) => category.id)
}

function shouldSyncFeaturedCategoryIds(rawValue: string | undefined, categoryIds: number[]) {
  const normalizedValue = JSON.stringify(categoryIds)
  return normalizedValue !== JSON.stringify(parseFeaturedCategoryIds(rawValue))
}

async function persistFeaturedCategoryIds(categoryIds: number[], silent = false) {
  await fetchUpdateSiteConfig({
    featured_category_ids: JSON.stringify(categoryIds)
  })

  if (!silent) {
    ElMessage.success('首页展示分类已更新')
  }
}

async function handleFeaturedCategoryChange(row: Api.Product.Category, checked: boolean) {
  if (!isRootCategory(row)) {
    return
  }

  const currentIds = [...featuredCategoryIds.value]
  let nextIds = currentIds

  if (checked) {
    if (currentIds.includes(row.id)) {
      return
    }
    if (currentIds.length >= 4) {
      ElMessage.warning('首页最多只能选择 4 个一级分类')
      return
    }
    nextIds = [...currentIds, row.id]
  } else {
    if (!currentIds.includes(row.id)) {
      return
    }
    if (currentIds.length <= 1) {
      ElMessage.warning('首页展示分类至少保留 1 个')
      return
    }
    nextIds = currentIds.filter((categoryId) => categoryId !== row.id)
  }

  featuredCategoryIds.value = nextIds
  savingFeatured.value = true
  try {
    await persistFeaturedCategoryIds(nextIds)
  } catch (error) {
    featuredCategoryIds.value = currentIds
    ElMessage.error('首页展示分类保存失败，请稍后重试')
  } finally {
    savingFeatured.value = false
  }
}

async function handleSubmit() {
  if (!formData.value.name.trim()) {
    ElMessage.warning('请输入分类名称')
    return
  }

  if (isTopLevelCategory.value && !formData.value.image) {
    ElMessage.warning('顶级分类必须上传分类图片')
    return
  }

  if (!isTopLevelCategory.value && formData.value.image) {
    ElMessage.warning('二级分类不能上传分类图片')
    return
  }

  if (!isTopLevelCategory.value && editingCategoryHasChildren.value) {
    ElMessage.warning('当前分类下存在子分类，不能调整为二级分类')
    return
  }

  submitting.value = true
  try {
    if (dialogType.value === 'add') {
      await fetchCreateCategory(formData.value)
    } else {
      await fetchUpdateCategory(formData.value.id!, formData.value)
    }
    ElMessage.success(dialogType.value === 'add' ? '新增成功' : '编辑成功')
    pendingUploadedImageUrl.value = ''
    dialogVisible.value = false
    await loadData()
  } finally {
    submitting.value = false
  }
}

async function deletePendingCategoryImageIfNeeded(fileUrl?: string) {
  const normalizedFileUrl = fileUrl?.trim() || ''
  if (!normalizedFileUrl || normalizedFileUrl !== pendingUploadedImageUrl.value) {
    return
  }

  try {
    await deleteImage(normalizedFileUrl)
  } catch (error) {
    console.error('删除未保存分类图片失败', error)
  } finally {
    if (pendingUploadedImageUrl.value === normalizedFileUrl) {
      pendingUploadedImageUrl.value = ''
    }
  }
}

async function cleanupPendingCategoryImage() {
  await deletePendingCategoryImageIfNeeded(pendingUploadedImageUrl.value)
}

async function handleDeleteCategoryImage() {
  await deletePendingCategoryImageIfNeeded(formData.value.image)
  formData.value.image = ''
}

async function handleCancelDialog() {
  await cleanupPendingCategoryImage()
  dialogVisible.value = false
}

const handleDialogBeforeClose: DialogBeforeCloseFn = async (done) => {
  await cleanupPendingCategoryImage()
  done()
}

async function handleImageUpload(options: any) {
  if (!isTopLevelCategory.value) {
    ElMessage.warning('仅一级分类支持上传分类图片')
    return
  }

  try {
    await validateImageUpload(options.file, CATEGORY_IMAGE_RULE)
    const url = await uploadImage(options.file, CATEGORY_IMAGE_RULE.purpose)
    await deletePendingCategoryImageIfNeeded(formData.value.image)
    pendingUploadedImageUrl.value = url
    formData.value.image = url
    ElMessage.success('图片上传成功')
  } catch (e: any) {
    ElMessage.error(e.message || '上传失败')
  }
}

async function handleDelete(row: any) {
  await ElMessageBox.confirm(`确定删除分类「${row.name}」吗？若该分类下仍有子分类或商品，将无法删除。`, '提示', { type: 'warning' })
  await fetchDeleteCategory(row.id)
  ElMessage.success('删除成功')
  await loadData()
}

watch(
  () => formData.value.parentId,
  (parentId, previousParentId) => {
    const normalizedParentId = Number(parentId ?? 0)
    if (normalizedParentId !== 0 && formData.value.image) {
      void deletePendingCategoryImageIfNeeded(formData.value.image)
      formData.value.image = ''
      if (dialogVisible.value && previousParentId !== undefined) {
        ElMessage.warning('二级分类不支持分类图片，已自动清空当前图片')
      }
    }
  }
)

onMounted(() => loadData())
onBeforeRouteLeave(async () => {
  await cleanupPendingCategoryImage()
})
onBeforeUnmount(() => {
  void cleanupPendingCategoryImage()
})
</script>

<style scoped lang="scss">
.search-form-compact {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 0;

  :deep(.el-form-item) {
    margin-bottom: 8px;
  }

  :deep(.el-input__wrapper) {
    min-height: 32px;
  }

  :deep(.el-select__wrapper) {
    min-height: 32px;
  }

  :deep(.el-button) {
    height: 32px;
    padding: 0 15px;
  }
}

.category-image-upload {
  .image-tip,
  .upload-tip,
  .level-tip {
    font-size: 12px;
    line-height: 1.6;
    color: var(--el-text-color-secondary);
  }

  .image-tip {
    padding: 10px 12px;
    border-radius: 6px;
    background: var(--el-fill-color-light);
    border: 1px dashed var(--el-border-color);
  }

  .upload-tip {
    margin-top: 8px;
  }

  .image-preview {
    position: relative;
    width: 100px;
    height: 100px;
    border-radius: 6px;
    overflow: hidden;
    border: 1px solid var(--el-border-color);

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .remove-btn {
      position: absolute;
      top: 4px;
      right: 4px;
      width: 20px;
      height: 20px;
      background: rgba(0, 0, 0, 0.5);
      color: #fff;
      border-radius: 50%;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 12px;
    }
  }

  .upload-trigger {
    width: 100px;
    height: 100px;
    border: 1px dashed var(--el-border-color);
    border-radius: 6px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    color: var(--el-text-color-secondary);
    font-size: 12px;
    gap: 4px;

    &:hover {
      border-color: var(--el-color-primary);
      color: var(--el-color-primary);
    }

    .el-icon {
      font-size: 20px;
    }
  }
}

.level-tip {
  color: var(--el-color-warning);
}

.row-number-cell {
  display: inline-flex;
  align-items: center;
  color: var(--el-text-color-regular);
}

.featured-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 24px;
}

.featured-placeholder {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>

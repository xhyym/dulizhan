<!-- 商品列表页面 -->
<template>
  <div class="product-page art-full-height">
    <!-- 搜索栏 -->
    <ElCard class="art-search-card">
      <ElForm :model="searchForm" inline class="search-form-compact">
        <ElFormItem label="商品分类">
          <ElTreeSelect
            v-model="searchForm.categoryId"
            :data="categoryOptions"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            placeholder="请选择分类"
            clearable
            check-strictly
            style="width: 160px"
          />
        </ElFormItem>
        <ElFormItem label="商品名称">
          <ElInput
            v-model="searchForm.name"
            placeholder="请输入商品名称"
            clearable
            style="width: 160px"
            @keyup.enter="loadData"
          />
        </ElFormItem>
        <ElFormItem label="创建时间">
          <ElDatePicker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </ElFormItem>
        <ElFormItem label="SKU编码">
          <ElInput
            v-model="searchForm.skuCode"
            placeholder="请输入SKU编码"
            clearable
            style="width: 140px"
            @keyup.enter="loadData"
          />
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
          <ElButton type="primary" @click="openDialog()" v-ripple>新增商品</ElButton>
        </template>
      </ArtTableHeader>

      <ArtTable
        :loading="loading"
        :data="tableData"
        :columns="columns"
        :pagination="pagination"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>

    <!-- 商品编辑弹窗 -->
    <ElDialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑商品' : '新增商品'"
      width="1100px"
      :close-on-click-modal="false"
      @close="resetForm"
    >
      <div class="dialog-content">
        <!-- 第一栏：基本信息 -->
        <div class="dialog-column">
          <h4 class="column-title">基本信息</h4>
          <ElForm :model="formData" label-width="80px" size="small">
            <ElFormItem label="商品名称" required>
              <ElInput v-model="formData.name" placeholder="请输入商品名称" />
            </ElFormItem>
            <ElFormItem label="商品简介">
              <ElInput v-model="formData.description" type="textarea" :rows="2" placeholder="请输入商品简介" />
            </ElFormItem>
            <ElFormItem label="商品分类">
              <ElTreeSelect
                v-model="formData.categoryId"
                :data="categoryOptions"
                :props="{ label: 'name', value: 'id', children: 'children' }"
                placeholder="请选择分类"
                clearable
                check-strictly
                style="width: 100%"
              />
            </ElFormItem>
            <ElFormItem label="原价" required>
              <ElInputNumber v-model="formData.price" :min="0" :precision="2" style="width: 100%" />
            </ElFormItem>
            <ElFormItem label="折后价">
              <ElInputNumber v-model="formData.discountPrice" :min="0" :precision="2" style="width: 100%" />
            </ElFormItem>
            <ElFormItem label="SKU编码">
              <ElInput v-model="formData.skuCode" placeholder="请输入SKU编码" />
            </ElFormItem>
            <ElFormItem label="状态">
              <ElRadioGroup v-model="formData.status">
                <ElRadio :value="1">上架</ElRadio>
                <ElRadio :value="0">下架</ElRadio>
              </ElRadioGroup>
            </ElFormItem>
            <ElFormItem label="排序">
              <ElInputNumber v-model="formData.sort" :min="0" :max="999" style="width: 100%" />
            </ElFormItem>
          </ElForm>
        </div>

        <!-- 第二栏：商品图片 -->
        <div class="dialog-column">
          <h4 class="column-title">商品图片</h4>
          <div class="image-upload-area">
            <div v-for="(img, index) in allImages" :key="index" class="image-item">
              <ElImage
                :src="img"
                style="width: 100px; height: 100px"
                fit="cover"
                :preview-src-list="allImages"
                :initial-index="index"
                preview-teleported
              />
              <div v-if="index === 0" class="main-tag">主图</div>
              <ElIcon class="delete-btn" @click="removeImage(index)">
                <CircleClose />
              </ElIcon>
            </div>
            <ElUpload
              :http-request="handleImageUpload"
              :show-file-list="false"
              accept="image/*"
              multiple
            >
              <ElIcon class="upload-trigger">
                <Plus />
              </ElIcon>
            </ElUpload>
          </div>
          <div class="image-tip">支持多张图片上传，第一张将作为商品主图</div>
        </div>

        <!-- 第三栏：SKU信息 -->
        <div class="dialog-column">
          <h4 class="column-title">SKU信息</h4>
          <div class="sku-area">
            <ElButton size="small" type="primary" @click="addSku" plain>添加SKU</ElButton>
            <div class="sku-list" v-if="formData.skus.length">
              <div v-for="(sku, index) in formData.skus" :key="index" class="sku-item">
                <div class="sku-header">
                  <span>SKU {{ index + 1 }}</span>
                  <ElButton type="danger" link size="small" @click="formData.skus.splice(index, 1)">删除</ElButton>
                </div>
                <ElForm :model="sku" label-width="60px" size="small">
                  <ElFormItem label="规格">
                    <ElInput v-model="sku.specName" placeholder="如: 颜色" style="width: 48%; margin-right: 4%" />
                    <ElInput v-model="sku.specValue" placeholder="如: 红色" style="width: 48%" />
                  </ElFormItem>
                  <ElFormItem label="价格">
                    <ElInputNumber v-model="sku.price" :min="0" :precision="2" style="width: 100%" />
                  </ElFormItem>
                  <ElFormItem label="库存">
                    <ElInputNumber v-model="sku.stock" :min="0" style="width: 100%" />
                  </ElFormItem>
                </ElForm>
              </div>
            </div>
            <ElEmpty v-else description="暂无SKU" :image-size="60" />
          </div>
        </div>
      </div>

      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="handleSubmit" :loading="submitting">确定</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
import { h } from 'vue'
import { ElMessageBox, ElMessage, ElTag, ElImage } from 'element-plus'
import { Plus, CircleClose } from '@element-plus/icons-vue'
import { useTableColumns } from '@/hooks/core/useTableColumns'
import {
  fetchGetProductList,
  fetchDeleteProduct,
  fetchGetCategoryList,
  fetchGetProductDetail,
  fetchCreateProduct,
  fetchUpdateProduct
} from '@/api/product'
import { uploadImage } from '@/api/upload'

defineOptions({ name: 'ProductList' })

const loading = ref(false)
const submitting = ref(false)
const tableData = ref<any[]>([])
const categoryOptions = ref<Api.Product.Category[]>([])
const dialogVisible = ref(false)

const searchForm = ref({
  name: '',
  categoryId: undefined as number | undefined,
  skuCode: '',
  dateRange: [] as string[]
})
const pagination = ref({ current: 1, size: 10, total: 0 })

const formData = ref({
  id: undefined as number | undefined,
  name: '',
  description: '',
  categoryId: undefined as number | undefined,
  price: 0,
  discountPrice: undefined as number | undefined,
  skuCode: '',
  mainImage: '',
  images: [] as string[],
  skus: [] as any[],
  status: 1,
  sort: 0
})

const isEdit = computed(() => !!formData.value.id)

// 所有图片列表（主图 + 副图）
const allImages = computed(() => {
  const images: string[] = []
  if (formData.value.mainImage) {
    images.push(formData.value.mainImage)
  }
  images.push(...formData.value.images)
  return images
})

// 列配置
const { columns, columnChecks } = useTableColumns(() => [
  { prop: 'id', label: 'ID', width: 80 },
  {
    prop: 'mainImage',
    label: '商品图片',
    width: 100,
    formatter: (row: any) =>
      h(ElImage, {
        src: row.mainImage,
        style: 'width: 60px; height: 60px',
        fit: 'cover',
        previewSrcList: [row.mainImage],
        previewTeleported: true
      })
  },
  { prop: 'name', label: '商品名称', minWidth: 200, showOverflowTooltip: true },
  {
    prop: 'price',
    label: '原价',
    width: 100,
    align: 'right',
    formatter: (row: any) => `¥${row.price}`
  },
  {
    prop: 'discountPrice',
    label: '折后价',
    width: 100,
    align: 'right',
    formatter: (row: any) =>
      row.discountPrice
        ? h('span', { style: 'color: #f56c6c' }, `¥${row.discountPrice}`)
        : h('span', {}, '-')
  },
  {
    prop: 'status',
    label: '状态',
    width: 80,
    align: 'center',
    formatter: (row: any) =>
      h(ElTag, { type: row.status === 1 ? 'success' : 'info' }, () =>
        row.status === 1 ? '上架' : '下架'
      )
  },
  { prop: 'createTime', label: '创建时间', width: 180 },
  {
    prop: 'operation',
    label: '操作',
    width: 120,
    fixed: 'right',
    formatter: (row: any) =>
      h('div', [
        h(
          'button',
          {
            class: 'el-button el-button--primary is-link',
            onClick: () => openDialog(row.id)
          },
          '编辑'
        )
      ])
  }
])

// 加载分类选项
async function loadCategories() {
  try {
    const { data } = await fetchGetCategoryList()
    categoryOptions.value = data || []
  } catch (error) {
    console.error('加载分类失败', error)
  }
}

async function loadData() {
  loading.value = true
  try {
    const params: any = {
      current: pagination.value.current,
      size: pagination.value.size,
      name: searchForm.value.name,
      categoryId: searchForm.value.categoryId,
      skuCode: searchForm.value.skuCode
    }
    if (searchForm.value.dateRange && searchForm.value.dateRange.length === 2) {
      params.startTime = searchForm.value.dateRange[0]
      params.endTime = searchForm.value.dateRange[1]
    }
    const { data } = await fetchGetProductList(params)
    tableData.value = data.records || []
    pagination.value.total = data.total
  } finally {
    loading.value = false
  }
}

function handleSizeChange(size: number) {
  pagination.value.size = size
  pagination.value.current = 1
  loadData()
}

function handleCurrentChange(current: number) {
  pagination.value.current = current
  loadData()
}

function resetSearch() {
  searchForm.value = {
    name: '',
    categoryId: undefined,
    skuCode: '',
    dateRange: []
  }
  pagination.value.current = 1
  loadData()
}

// 打开弹窗
async function openDialog(id?: number) {
  if (id) {
    try {
      const { data } = await fetchGetProductDetail(id)
      formData.value = {
        id: data.id,
        name: data.name,
        description: data.description || '',
        categoryId: data.categoryId,
        price: data.price,
        discountPrice: data.discountPrice,
        skuCode: data.skuCode || '',
        mainImage: data.mainImage || '',
        images: data.images || [],
        skus: (data.skus || []).map(s => ({ ...s })),
        status: data.status,
        sort: data.sort || 0
      }
    } catch (error) {
      ElMessage.error('加载商品详情失败')
      return
    }
  } else {
    resetForm()
  }
  dialogVisible.value = true
}

// 重置表单
function resetForm() {
  formData.value = {
    id: undefined,
    name: '',
    description: '',
    categoryId: undefined,
    price: 0,
    discountPrice: undefined,
    skuCode: '',
    mainImage: '',
    images: [],
    skus: [],
    status: 1,
    sort: 0
  }
}

// 上传图片
async function handleImageUpload(options: any) {
  try {
    const url = await uploadImage(options.file)
    if (!formData.value.mainImage) {
      formData.value.mainImage = url
    } else {
      formData.value.images.push(url)
    }
    ElMessage.success('图片上传成功')
  } catch (e: any) {
    ElMessage.error(e.message || '上传失败')
  }
}

// 删除图片
function removeImage(index: number) {
  if (index === 0 && formData.value.mainImage) {
    formData.value.mainImage = ''
    if (formData.value.images.length > 0) {
      formData.value.mainImage = formData.value.images.shift()!
    }
  } else {
    const subIndex = formData.value.mainImage ? index - 1 : index
    formData.value.images.splice(subIndex, 1)
  }
}

// 添加SKU
function addSku() {
  formData.value.skus.push({
    skuCode: '',
    specName: '',
    specValue: '',
    price: 0,
    stock: 0,
    status: 1
  })
}

// 提交表单
async function handleSubmit() {
  if (!formData.value.name) return ElMessage.warning('请输入商品名称')
  if (!formData.value.price) return ElMessage.warning('请输入商品价格')

  submitting.value = true
  try {
    if (isEdit.value) {
      await fetchUpdateProduct(formData.value.id!, formData.value)
      ElMessage.success('编辑成功')
    } else {
      await fetchCreateProduct(formData.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row: any) {
  await ElMessageBox.confirm(`确定删除商品「${row.name}」吗？`, '提示', { type: 'warning' })
  await fetchDeleteProduct(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => {
  loadCategories()
  loadData()
})
</script>

<style scoped lang="scss">
.product-page {
  display: flex;
  flex-direction: column;
}

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

.dialog-content {
  display: flex;
  gap: 20px;
  max-height: 60vh;
  overflow-y: auto;
}

.dialog-column {
  flex: 1;
  min-width: 0;

  &:nth-child(2) {
    flex: 0 0 240px;
  }

  &:nth-child(3) {
    flex: 0 0 240px;
  }
}

.column-title {
  margin: 0 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #e4e7ed;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.image-upload-area {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.image-item {
  position: relative;
  border-radius: 4px;
  overflow: hidden;
  border: 1px solid #dcdfe6;

  .main-tag {
    position: absolute;
    top: 0;
    left: 0;
    background: #409eff;
    color: #fff;
    font-size: 12px;
    padding: 2px 6px;
    border-radius: 0 0 4px 0;
  }

  .delete-btn {
    position: absolute;
    top: -6px;
    right: -6px;
    cursor: pointer;
    color: #f56c6c;
    font-size: 18px;
    background: #fff;
    border-radius: 50%;
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

.sku-area {
  .sku-list {
    margin-top: 12px;
  }

  .sku-item {
    padding: 12px;
    margin-bottom: 12px;
    background: #f5f7fa;
    border-radius: 4px;

    .sku-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;
      font-size: 13px;
      font-weight: 500;
    }

    :deep(.el-form-item) {
      margin-bottom: 8px;
    }
  }
}
</style>

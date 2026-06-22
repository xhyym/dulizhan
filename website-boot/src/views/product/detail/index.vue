<!-- 商品编辑页面 -->
<template>
  <div class="product-detail art-full-height">
    <ElCard>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>{{ isEdit ? '编辑商品' : '新增商品' }}</span>
          <ElButton @click="router.back()">返回</ElButton>
        </div>
      </template>

      <ElForm :model="formData" label-width="100px" style="max-width: 800px">
        <ElFormItem label="商品名称" required>
          <ElInput v-model="formData.name" placeholder="请输入商品名称" />
        </ElFormItem>

        <ElFormItem label="商品简介">
          <ElInput v-model="formData.description" type="textarea" :rows="3" placeholder="请输入商品简介" />
        </ElFormItem>

        <ElFormItem label="商品分类">
          <ElTreeSelect v-model="formData.categoryId" :data="categoryTree" :props="{ label: 'name', value: 'id' }"
            placeholder="请选择分类" clearable check-strictly style="width: 100%" />
        </ElFormItem>

        <ElFormItem label="原价" required>
          <ElInputNumber v-model="formData.price" :min="0" :precision="2" style="width: 200px" />
        </ElFormItem>

        <ElFormItem label="折后价">
          <ElInputNumber v-model="formData.discountPrice" :min="0" :precision="2" style="width: 200px" />
        </ElFormItem>

        <ElFormItem label="SKU编码">
          <ElInput v-model="formData.skuCode" placeholder="请输入SKU编码" />
        </ElFormItem>

        <ElFormItem label="商品主图">
          <ElUpload :http-request="handleMainImageUpload" :show-file-list="false" accept="image/*">
            <ElImage v-if="formData.mainImage" :src="formData.mainImage" style="width: 150px; height: 150px"
              fit="cover" />
            <ElIcon v-else style="width: 150px; height: 150px; border: 1px dashed #dcdfe6; border-radius: 6px">
              <Plus />
            </ElIcon>
          </ElUpload>
        </ElFormItem>

        <ElFormItem label="商品海报">
          <ElUpload :http-request="handlePosterUpload" :show-file-list="false" accept="image/*">
            <ElImage v-if="formData.posterImage" :src="formData.posterImage" style="width: 150px; height: 150px"
              fit="cover" />
            <ElIcon v-else style="width: 150px; height: 150px; border: 1px dashed #dcdfe6; border-radius: 6px">
              <Plus />
            </ElIcon>
          </ElUpload>
        </ElFormItem>

        <ElFormItem label="商品副图">
          <div style="display: flex; flex-wrap: wrap; gap: 8px">
            <div v-for="(img, index) in formData.images" :key="index" style="position: relative">
              <ElImage :src="img" style="width: 100px; height: 100px" fit="cover" />
              <ElIcon style="position: absolute; top: -8px; right: -8px; cursor: pointer; color: #f56c6c"
                @click="formData.images.splice(index, 1)">
                <CircleClose />
              </ElIcon>
            </div>
            <ElUpload :http-request="handleSubImageUpload" :show-file-list="false" accept="image/*">
              <ElIcon
                style="width: 100px; height: 100px; border: 1px dashed #dcdfe6; border-radius: 6px; cursor: pointer">
                <Plus />
              </ElIcon>
            </ElUpload>
          </div>
        </ElFormItem>

        <ElFormItem label="SKU信息">
          <div style="width: 100%">
            <ElButton size="small" @click="addSku">添加SKU</ElButton>
            <ElTable :data="formData.skus" style="margin-top: 8px" v-if="formData.skus.length">
              <ElTableColumn label="规格名称" min-width="120">
                <template #default="{ row }">
                  <ElInput v-model="row.specName" size="small" placeholder="如: 颜色" />
                </template>
              </ElTableColumn>
              <ElTableColumn label="规格值" min-width="120">
                <template #default="{ row }">
                  <ElInput v-model="row.specValue" size="small" placeholder="如: 红色" />
                </template>
              </ElTableColumn>
              <ElTableColumn label="价格" width="120">
                <template #default="{ row }">
                  <ElInputNumber v-model="row.price" size="small" :min="0" :precision="2" />
                </template>
              </ElTableColumn>
              <ElTableColumn label="库存" width="100">
                <template #default="{ row }">
                  <ElInputNumber v-model="row.stock" size="small" :min="0" />
                </template>
              </ElTableColumn>
              <ElTableColumn label="操作" width="80">
                <template #default="{ $index }">
                  <ElButton type="danger" link size="small" @click="formData.skus.splice($index, 1)">删除</ElButton>
                </template>
              </ElTableColumn>
            </ElTable>
          </div>
        </ElFormItem>

        <ElFormItem label="状态">
          <ElRadioGroup v-model="formData.status">
            <ElRadio :value="1">上架</ElRadio>
            <ElRadio :value="0">下架</ElRadio>
          </ElRadioGroup>
        </ElFormItem>

        <ElFormItem label="排序">
          <ElInputNumber v-model="formData.sort" :min="0" :max="999" />
        </ElFormItem>

        <ElFormItem>
          <ElButton type="primary" @click="handleSubmit" :loading="submitting">保存</ElButton>
          <ElButton @click="router.back()">取消</ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, CircleClose } from '@element-plus/icons-vue'
import { fetchGetProductDetail, fetchCreateProduct, fetchUpdateProduct, fetchGetCategoryList } from '@/api/product'
import { uploadImage } from '@/api/upload'

defineOptions({ name: 'ProductDetail' })

const router = useRouter()
const route = useRoute()
const submitting = ref(false)
const categoryTree = ref<any[]>([])

const isEdit = computed(() => !!route.query.id)

const formData = ref({
  id: undefined as number | undefined,
  name: '',
  description: '',
  categoryId: undefined as number | undefined,
  price: 0,
  discountPrice: undefined as number | undefined,
  skuCode: '',
  mainImage: '',
  posterImage: '',
  images: [] as string[],
  skus: [] as any[],
  status: 1,
  sort: 0
})

async function loadCategories() {
  const { data } = await fetchGetCategoryList()
  categoryTree.value = data || []
}

async function loadProduct() {
  if (!route.query.id) return
  const { data } = await fetchGetProductDetail(Number(route.query.id))
  formData.value = {
    id: data.id,
    name: data.name,
    description: data.description || '',
    categoryId: data.categoryId,
    price: data.price,
    discountPrice: data.discountPrice,
    skuCode: data.skuCode || '',
    mainImage: data.mainImage || '',
    posterImage: data.posterImage || '',
    images: data.images || [],
    skus: (data.skus || []).map(s => ({ ...s })),
    status: data.status,
    sort: data.sort || 0
  }
}

async function handleMainImageUpload(options: any) {
  try {
    formData.value.mainImage = await uploadImage(options.file)
    ElMessage.success('主图上传成功')
  } catch (e: any) {
    ElMessage.error(e.message || '上传失败')
  }
}

async function handlePosterUpload(options: any) {
  try {
    formData.value.posterImage = await uploadImage(options.file)
    ElMessage.success('海报上传成功')
  } catch (e: any) {
    ElMessage.error(e.message || '上传失败')
  }
}

async function handleSubImageUpload(options: any) {
  try {
    const url = await uploadImage(options.file)
    formData.value.images.push(url)
    ElMessage.success('副图上传成功')
  } catch (e: any) {
    ElMessage.error(e.message || '上传失败')
  }
}

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
    router.push('/product/list')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await loadCategories()
  if (isEdit.value) await loadProduct()
})
</script>

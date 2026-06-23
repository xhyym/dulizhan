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
      />
    </ElCard>

    <!-- 分类弹窗 -->
    <ElDialog v-model="dialogVisible" :title="dialogType === 'add' ? '新增分类' : '编辑分类'" width="500px">
      <ElForm :model="formData" label-width="80px">
        <ElFormItem label="分类名称">
          <ElInput v-model="formData.name" placeholder="请输入分类名称" />
        </ElFormItem>
        <ElFormItem label="父级分类">
          <ElTreeSelect v-model="formData.parentId" :data="treeData" :props="{ label: 'name', value: 'id' }"
            placeholder="顶级分类" clearable check-strictly />
        </ElFormItem>
        <ElFormItem label="排序">
          <ElInputNumber v-model="formData.sort" :min="0" :max="999" />
        </ElFormItem>
        <ElFormItem label="状态">
          <ElRadioGroup v-model="formData.status">
            <ElRadio :value="1">正常</ElRadio>
            <ElRadio :value="0">禁用</ElRadio>
          </ElRadioGroup>
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="handleSubmit" :loading="submitting">确定</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
import { h } from 'vue'
import { ElMessageBox, ElMessage, ElTag } from 'element-plus'
import { useTableColumns } from '@/hooks/core/useTableColumns'
import { fetchGetCategoryList, fetchCreateCategory, fetchUpdateCategory, fetchDeleteCategory } from '@/api/product'

defineOptions({ name: 'Category' })

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogType = ref<'add' | 'edit'>('add')
const tableData = ref<any[]>([])

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

// 递归过滤树形数据
function filterTree(tree: any[], predicate: (item: any) => boolean): any[] {
  return tree
    .map((item) => {
      const children = item.children ? filterTree(item.children, predicate) : []
      return { ...item, children }
    })
    .filter((item) => predicate(item) || (item.children && item.children.length > 0))
}

// 表单数据
const formData = ref({
  id: undefined as number | undefined,
  name: '',
  parentId: 0,
  sort: 0,
  status: 1
})

const treeData = computed(() => {
  return [{ id: 0, name: '顶级分类', children: tableData.value }]
})

// 列配置
const { columns, columnChecks } = useTableColumns(() => [
  { prop: 'name', label: '分类名称', minWidth: 200 },
  { prop: 'sort', label: '排序', width: 100, align: 'center' },
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
  { prop: 'createTime', label: '创建时间', width: 180 },
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
    const { data } = await fetchGetCategoryList()
    tableData.value = data || []
  } finally {
    loading.value = false
  }
}

function resetSearch() {
  searchForm.value = { name: '', status: undefined }
}

function showDialog(type: 'add' | 'edit', row?: any) {
  dialogType.value = type
  if (type === 'edit' && row) {
    formData.value = { id: row.id, name: row.name, parentId: row.parentId, sort: row.sort, status: row.status }
  } else {
    formData.value = { id: undefined, name: '', parentId: 0, sort: 0, status: 1 }
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  submitting.value = true
  try {
    if (dialogType.value === 'add') {
      await fetchCreateCategory(formData.value)
    } else {
      await fetchUpdateCategory(formData.value.id!, formData.value)
    }
    ElMessage.success(dialogType.value === 'add' ? '新增成功' : '编辑成功')
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row: any) {
  await ElMessageBox.confirm('确定删除该分类吗？', '提示', { type: 'warning' })
  await fetchDeleteCategory(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => loadData())
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
</style>

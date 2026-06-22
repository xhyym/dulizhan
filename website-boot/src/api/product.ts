import request from '@/utils/http'

/** 获取商品分页列表 */
export function fetchGetProductList(params: Api.Product.ProductSearchParams) {
  return request.get<Api.Product.ProductList>({
    url: '/api/admin/products',
    params
  })
}

/** 获取商品详情 */
export function fetchGetProductDetail(id: number) {
  return request.get<Api.Product.ProductDetail>({
    url: `/api/admin/products/${id}`
  })
}

/** 新增商品 */
export function fetchCreateProduct(data: Api.Product.ProductDTO) {
  return request.post({
    url: '/api/admin/products',
    data
  })
}

/** 编辑商品 */
export function fetchUpdateProduct(id: number, data: Api.Product.ProductDTO) {
  return request.put({
    url: `/api/admin/products/${id}`,
    data
  })
}

/** 删除商品 */
export function fetchDeleteProduct(id: number) {
  return request.delete({
    url: `/api/admin/products/${id}`
  })
}

/** 获取分类列表 (树形) */
export function fetchGetCategoryList() {
  return request.get<Api.Product.Category[]>({
    url: '/api/admin/categories'
  })
}

/** 新增分类 */
export function fetchCreateCategory(data: Api.Product.CategoryDTO) {
  return request.post({
    url: '/api/admin/categories',
    data
  })
}

/** 编辑分类 */
export function fetchUpdateCategory(id: number, data: Api.Product.CategoryDTO) {
  return request.put({
    url: `/api/admin/categories/${id}`,
    data
  })
}

/** 删除分类 */
export function fetchDeleteCategory(id: number) {
  return request.delete({
    url: `/api/admin/categories/${id}`
  })
}

/**
 * API 接口类型定义模块
 *
 * 提供所有后端接口的类型定义
 *
 * ## 主要功能
 *
 * - 通用类型（分页参数、响应结构等）
 * - 认证类型（登录、用户信息等）
 * - 系统管理类型（用户、角色等）
 * - 全局命名空间声明
 *
 * ## 使用场景
 *
 * - API 请求参数类型约束
 * - API 响应数据类型定义
 * - 接口文档类型同步
 *
 * ## 注意事项
 *
 * - 在 .vue 文件使用需要在 eslint.config.mjs 中配置 globals: { Api: 'readonly' }
 * - 使用全局命名空间，无需导入即可使用
 *
 * ## 使用方式
 *
 * ```typescript
 * const params: Api.Auth.LoginParams = { userName: 'admin', password: '123456' }
 * const response: Api.Auth.UserInfo = await fetchUserInfo()
 * ```
 *
 * @module types/api/api
 * @author Art Design Pro Team
 */

declare namespace Api {
  /** 通用类型 */
  namespace Common {
    /** 分页参数 */
    interface PaginationParams {
      /** 当前页码 */
      current: number
      /** 每页条数 */
      size: number
      /** 总条数 */
      total: number
    }

    /** 通用搜索参数 */
    type CommonSearchParams = Pick<PaginationParams, 'current' | 'size'>

    /** 分页响应基础结构 */
    interface PaginatedResponse<T = any> {
      records: T[]
      current: number
      size: number
      total: number
    }

    /** 启用状态 */
    type EnableStatus = '1' | '2'
  }

  /** 认证类型 */
  namespace Auth {
    /** 登录参数 */
    interface LoginParams {
      userName: string
      password: string
    }

    /** 登录响应 */
    interface LoginResponse {
      token: string
      refreshToken: string
    }

    /** 用户信息 */
    interface UserInfo {
      buttons: string[]
      roles: string[]
      userId: number
      userName: string
      email: string
      avatar?: string
    }
  }

  /** 系统管理类型 */
  namespace SystemManage {
    /** 用户列表 */
    type UserList = Api.Common.PaginatedResponse<UserListItem>

    /** 用户列表项 */
    interface UserListItem {
      id: number
      avatar: string
      status: string
      userName: string
      userGender: string
      nickName: string
      userPhone: string
      userEmail: string
      userRoles: string[]
      createBy: string
      createTime: string
      updateBy: string
      updateTime: string
    }

    /** 用户搜索参数 */
    type UserSearchParams = Partial<
      Pick<UserListItem, 'id' | 'userName' | 'userGender' | 'userPhone' | 'userEmail' | 'status'> &
        Api.Common.CommonSearchParams
    >

    /** 角色列表 */
    type RoleList = Api.Common.PaginatedResponse<RoleListItem>

    /** 角色列表项 */
    interface RoleListItem {
      roleId: number
      roleName: string
      roleCode: string
      description: string
      enabled: boolean
      createTime: string
    }

    /** 角色搜索参数 */
    type RoleSearchParams = Partial<
      Pick<RoleListItem, 'roleId' | 'roleName' | 'roleCode' | 'description' | 'enabled'> &
        Api.Common.CommonSearchParams & {
          startTime: string | null
          endTime: string | null
        }
    >
  }

  /** 商品管理类型 */
  namespace Product {
    /** 分类 */
    interface Category {
      id: number
      name: string
      image?: string
      parentId: number
      sort: number
      status: number
      createTime: string
      updateTime: string
      children?: Category[]
    }

    /** 分类DTO */
    interface CategoryDTO {
      id?: number
      name: string
      image?: string
      parentId?: number
      sort?: number
      status?: number
    }

    /** 商品列表项 */
    interface ProductItem {
      id: number
      name: string
      description: string
      categoryId: number
      price: number
      discountPrice: number
      skuCode: string
      mainImage: string
      posterImage: string
      status: number
      sort: number
      createTime: string
      updateTime: string
    }

    /** 商品详情 */
    interface ProductDetail {
      id: number
      name: string
      description: string
      categoryId: number
      price: number
      discountPrice: number
      skuCode: string
      mainImage: string
      posterImage: string
      detailImage: string
      status: number
      sort: number
      images: string[]
      skus: SkuItem[]
    }

    /** SKU项 */
    interface SkuItem {
      id?: number
      skuCode: string
      specName: string
      specValue: string
      price: number
      stock: number
      status: number
    }

    /** 商品DTO (新增/编辑) */
    interface ProductDTO {
      id?: number
      name: string
      description?: string
      categoryId?: number
      price: number
      discountPrice?: number
      skuCode?: string
      mainImage?: string
      posterImage?: string
      detailImage?: string
      status?: number
      sort?: number
      images?: string[]
      skus?: SkuItem[]
    }

    /** 商品列表响应 */
    type ProductList = Api.Common.PaginatedResponse<ProductItem>

    /** 商品搜索参数 */
    type ProductSearchParams = Partial<{
      current: number
      size: number
      name: string
      categoryId: number
      status: number
      skuCode: string
      startTime: string
      endTime: string
    }>
  }

  /** 客户管理类型 */
  namespace User {
    /** 客户列表项 */
    interface UserItem {
      id: number
      username: string
      email: string
      whatsapp: string
      avatar: string
      status: number
      createTime: string
      updateTime: string
    }
  }

  /** 询盘管理类型 */
  namespace Inquiry {
    /** 询盘商品明细 */
    interface InquiryItem {
      id: number
      inquiryId: number
      productId: number
      productName: string
      productImage: string
      skuId?: number
      skuSpec?: string
      price: number
      quantity: number
    }

    /** 询盘 */
    interface Inquiry {
      id: number
      inquiryNo: string
      userId: number
      userName: string
      userEmail: string
      userWhatsapp: string
      totalAmount: number
      remark: string
      status: number
      adminRemark: string
      createTime: string
      updateTime: string
      totalQuantity?: number
      items?: InquiryItem[]
    }

    /** 询盘列表响应 */
    type InquiryList = Api.Common.PaginatedResponse<Inquiry>

    /** 询盘搜索参数 */
    type InquirySearchParams = Partial<{
      current: number
      size: number
      inquiryNo: string
      userName: string
      status: number
    }>
  }

  /** 系统配置类型 */
  namespace SiteConfig {
    /** 配置Map */
    type ConfigMap = Record<string, string>
  }

  /** 文件上传类型 */
  namespace Upload {
    /** 预签名URL响应 */
    interface PresignedUrlVo {
      uploadUrl: string
      fileUrl: string
      key: string
    }
  }

  /** 仪表盘类型 */
  namespace Dashboard {
    /** 仪表盘数据 */
    interface DashboardVO {
      todayPv: number
      todayUv: number
      productCount: number
      pendingInquiryCount: number
      monthNewCustomerCount: number
      monthInquiryCount: number
      visitTrend: Record<string, any>[]
      countryTop10: Record<string, any>[]
      deviceDistribution: Record<string, any>[]
      recentInquiries: Record<string, any>[]
    }

    /** 访客统计详情 */
    interface VisitorStatsVO {
      totalPv: number
      totalUv: number
      dailyTrend: Record<string, any>[]
      countryTop10: Record<string, any>[]
      cityTop10: Record<string, any>[]
      deviceDistribution: Record<string, any>[]
      topPages: Record<string, any>[]
      hourlyHeatmap: Record<string, any>[]
    }
  }
}

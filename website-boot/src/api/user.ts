import request from '@/utils/http'

/** 获取客户分页列表 */
export function fetchGetUserList(params: {
  current: number
  size: number
  username?: string
  email?: string
  status?: number
}) {
  return request.get<Api.Common.PaginatedResponse<Api.User.UserItem>>({
    url: '/api/admin/users',
    params
  })
}

/** 获取客户详情 */
export function fetchGetUserDetail(id: number) {
  return request.get<Api.User.UserItem>({
    url: `/api/admin/users/${id}`
  })
}

/** 获取客户的询盘列表 */
export function fetchGetUserInquiries(id: number) {
  return request.get<Api.Inquiry.Inquiry[]>({
    url: `/api/admin/users/${id}/inquiries`
  })
}

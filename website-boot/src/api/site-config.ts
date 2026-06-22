import request from '@/utils/http'

/** 获取所有系统配置 */
export function fetchGetSiteConfig() {
  return request.get<Record<string, string>>({
    url: '/api/admin/site-config'
  })
}

/** 批量更新系统配置 */
export function fetchUpdateSiteConfig(data: Record<string, string>) {
  return request.put({
    url: '/api/admin/site-config',
    data
  })
}

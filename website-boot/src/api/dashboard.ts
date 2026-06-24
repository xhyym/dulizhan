import request from '@/utils/http'

/** 获取仪表盘统计数据 */
export function fetchGetDashboard() {
  return request.get<Api.Dashboard.DashboardVO>({
    url: '/api/admin/dashboard'
  })
}

/** 获取访客统计详情 */
export function fetchGetVisitorStats(startDate: string, endDate: string) {
  return request.get<Api.Dashboard.VisitorStatsVO>({
    url: '/api/admin/dashboard/visitor-stats',
    params: { startDate, endDate }
  })
}

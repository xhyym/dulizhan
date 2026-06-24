import request from '@/utils/http'

/** 记录访客访问（门户端调用） */
export function fetchRecordVisit(pageUrl: string) {
  return request.post({
    url: '/api/portal/visit/record',
    params: { pageUrl }
  })
}

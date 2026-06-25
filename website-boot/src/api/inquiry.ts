import request from '@/utils/http'

/** 获取询盘分页列表 */
export function fetchGetInquiryList(params: Api.Inquiry.InquirySearchParams) {
  return request.get<Api.Inquiry.InquiryList>({
    url: '/api/admin/inquiries',
    params
  })
}

/** 获取询盘详情 */
export function fetchGetInquiryDetail(id: number) {
  return request.get<Api.Inquiry.Inquiry>({
    url: `/api/admin/inquiries/${id}`
  })
}

/** 更新询盘状态 */
export function fetchUpdateInquiryStatus(id: number, data: { status: number; adminRemark?: string }) {
  return request.put({
    url: `/api/admin/inquiries/${id}/status`,
    data
  })
}

/** 生成询盘转换单 PDF */
export function fetchGenerateInquiryPdf(id: number) {
  return request.get<Blob>({
    url: `/api/admin/inquiries/${id}/pdf`,
    responseType: 'blob'
  })
}

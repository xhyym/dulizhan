package com.indiestation.service;

import com.indiestation.entity.Inquiry;
import com.indiestation.entity.InquiryItem;

import java.io.OutputStream;
import java.util.List;

/**
 * PDF 生成服务
 *
 * @author IndieStation
 */
public interface PdfService {

    /**
     * 生成询盘转换单 PDF
     */
    void generateInquiryPdf(Inquiry inquiry, List<InquiryItem> items, OutputStream outputStream);
}

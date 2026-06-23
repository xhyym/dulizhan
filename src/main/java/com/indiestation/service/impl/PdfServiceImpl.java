package com.indiestation.service.impl;

import com.indiestation.entity.Inquiry;
import com.indiestation.entity.InquiryItem;
import com.indiestation.service.PdfService;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * PDF 生成服务实现
 *
 * @author IndieStation
 */
@Slf4j
@Service
public class PdfServiceImpl implements PdfService {

    private static final DeviceRgb HEADER_BG_COLOR = new DeviceRgb(41, 128, 185);
    private static final DeviceRgb HEADER_TEXT_COLOR = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb LIGHT_GRAY = new DeviceRgb(245, 245, 245);

    @Override
    public void generateInquiryPdf(Inquiry inquiry, List<InquiryItem> items, OutputStream outputStream) {
        try {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // 标题
            document.add(new Paragraph("询盘转换单")
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));

            // 询盘基本信息
            document.add(createSectionTitle("询盘信息"));
            document.add(createInfoTable(inquiry));

            // 客户信息
            document.add(createSectionTitle("客户信息"));
            document.add(createCustomerTable(inquiry));

            // 商品明细
            if (items != null && !items.isEmpty()) {
                document.add(createSectionTitle("商品明细"));
                document.add(createItemTable(items));
            }

            // 备注
            if (inquiry.getRemark() != null || inquiry.getAdminRemark() != null) {
                document.add(createSectionTitle("备注信息"));
                if (inquiry.getRemark() != null) {
                    document.add(new Paragraph("客户备注: " + inquiry.getRemark()).setMarginBottom(5));
                }
                if (inquiry.getAdminRemark() != null) {
                    document.add(new Paragraph("管理员备注: " + inquiry.getAdminRemark()));
                }
            }

            document.close();
        } catch (Exception e) {
            log.error("生成PDF失败", e);
            throw new RuntimeException("生成PDF失败: " + e.getMessage());
        }
    }

    private Paragraph createSectionTitle(String title) {
        return new Paragraph(title)
                .setFontSize(14)
                .setBold()
                .setMarginTop(15)
                .setMarginBottom(10)
                .setBorderBottom(new SolidBorder(HEADER_BG_COLOR, 1));
    }

    private Table createInfoTable(Inquiry inquiry) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                .useAllAvailableWidth()
                .setMarginBottom(10);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        addRow(table, "询盘编号", inquiry.getInquiryNo());
        addRow(table, "总金额", "¥" + inquiry.getTotalAmount());
        addRow(table, "状态", getStatusText(inquiry.getStatus()));
        addRow(table, "创建时间", inquiry.getCreateTime() != null ? inquiry.getCreateTime().format(formatter) : "-");

        return table;
    }

    private Table createCustomerTable(Inquiry inquiry) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                .useAllAvailableWidth()
                .setMarginBottom(10);

        addRow(table, "用户名", inquiry.getUserName());
        addRow(table, "邮箱", inquiry.getUserEmail());
        addRow(table, "WhatsApp", inquiry.getUserWhatsapp());

        return table;
    }

    private Table createItemTable(List<InquiryItem> items) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{5, 30, 25, 15, 10, 15}))
                .useAllAvailableWidth();

        // 表头
        addHeaderCell(table, "#");
        addHeaderCell(table, "商品名称");
        addHeaderCell(table, "SKU规格");
        addHeaderCell(table, "单价");
        addHeaderCell(table, "数量");
        addHeaderCell(table, "小计");

        // 数据行
        for (int i = 0; i < items.size(); i++) {
            InquiryItem item = items.get(i);
            addCell(table, String.valueOf(i + 1));
            addCell(table, item.getProductName());
            addCell(table, item.getSkuSpec() != null ? item.getSkuSpec() : "-");
            addCell(table, "¥" + item.getPrice());
            addCell(table, String.valueOf(item.getQuantity()));
            addCell(table, "¥" + item.getPrice().multiply(new java.math.BigDecimal(item.getQuantity())));
        }

        return table;
    }

    private void addRow(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label).setBold()).setBorder(Border.NO_BORDER).setPadding(5));
        table.addCell(new Cell().add(new Paragraph(value != null ? value : "-")).setBorder(Border.NO_BORDER).setPadding(5));
    }

    private void addHeaderCell(Table table, String text) {
        table.addCell(new Cell()
                .add(new Paragraph(text).setBold().setFontColor(HEADER_TEXT_COLOR))
                .setBackgroundColor(HEADER_BG_COLOR)
                .setPadding(5)
                .setTextAlignment(TextAlignment.CENTER));
    }

    private void addCell(Table table, String text) {
        table.addCell(new Cell()
                .add(new Paragraph(text))
                .setPadding(5)
                .setTextAlignment(TextAlignment.CENTER));
    }

    private String getStatusText(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "待处理";
            case 1 -> "已联系";
            case 2 -> "已完成";
            case 3 -> "已取消";
            default -> "未知";
        };
    }
}

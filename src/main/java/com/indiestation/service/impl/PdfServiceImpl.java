package com.indiestation.service.impl;

import com.indiestation.entity.Inquiry;
import com.indiestation.entity.InquiryItem;
import com.indiestation.service.PdfService;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.svg.converter.SvgConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

    private static final DeviceRgb PRIMARY_TEXT_COLOR = new DeviceRgb(26, 26, 26);
    private static final DeviceRgb SECONDARY_TEXT_COLOR = new DeviceRgb(136, 136, 136);
    private static final DeviceRgb LINE_COLOR = new DeviceRgb(224, 224, 224);
    private static final DeviceRgb SECTION_BG_COLOR = new DeviceRgb(249, 249, 249);
    private static final DeviceRgb ACCENT_GOLD_COLOR = new DeviceRgb(201, 169, 110);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String DEFAULT_TEXT = "-";
    private static final String PREPARED_BY_TEXT = "OSEN Admin";

    @Override
    public void generateInquiryPdf(Inquiry inquiry, List<InquiryItem> items, OutputStream outputStream) {
        try {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4);
            document.setMargins(42, 42, 42, 42);

            PdfFont bodyFont = createBodyFont();
            PdfFont latinFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont latinBoldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            document.setFont(bodyFont);
            document.setFontSize(10);
            document.setFontColor(PRIMARY_TEXT_COLOR);

            // 顶部品牌头与标题区域
            document.add(createHeaderTable(pdfDoc, bodyFont, latinFont, latinBoldFont));
            document.add(createDivider(0.8f, 8, 14));

            // 单据基础信息区域
            document.add(createMetaTable(inquiry, bodyFont, latinFont, latinBoldFont));
            document.add(createSectionBlock(
                    "CUSTOMER",
                    createCustomerTable(inquiry, bodyFont, latinFont, latinBoldFont),
                    latinBoldFont
            ));
            document.add(createSectionBlock(
                    "PRODUCTS",
                    createProductBlock(inquiry, items, bodyFont, latinFont, latinBoldFont),
                    latinBoldFont
            ));
            document.add(createSectionBlock(
                    "CUSTOMER REMARK",
                    createNoteTable(inquiry.getRemark(), bodyFont),
                    latinBoldFont
            ));
            document.add(createSectionBlock(
                    "INTERNAL NOTE",
                    createNoteTable(inquiry.getAdminRemark(), bodyFont),
                    latinBoldFont
            ));
            document.add(createSignatureTable(bodyFont, latinFont, latinBoldFont));
            document.add(createDivider(0.6f, 14, 8));
            document.add(createFooterParagraph(bodyFont, latinFont));

            document.close();
        } catch (Exception e) {
            String inquiryNo = inquiry != null ? defaultText(inquiry.getInquiryNo()) : DEFAULT_TEXT;
            int itemCount = items != null ? items.size() : 0;
            log.error("生成询盘下发单PDF失败，询盘编号：{}，商品条目数量：{}", inquiryNo, itemCount, e);
            throw new RuntimeException("生成询盘下发单PDF失败: " + e.getMessage(), e);
        }
    }

    /**
     * 创建文档主字体，确保中文内容在本地与 Docker 环境中都可以稳定渲染。
     */
    private PdfFont createBodyFont() throws java.io.IOException {
        return PdfFontFactory.createFont("STSongStd-Light", "UniGB-UCS2-H");
    }

    /**
     * 创建顶部品牌头，优先使用用户提供的 SVG Logo，失败时自动回退为文本品牌头。
     */
    private Table createHeaderTable(PdfDocument pdfDoc, PdfFont bodyFont, PdfFont latinFont, PdfFont latinBoldFont) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{58, 42}))
                .useAllAvailableWidth()
                .setMarginBottom(4);

        Cell logoCell = createPlainCell()
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPaddingRight(12);

        Image logoImage = createLogoImage(pdfDoc);
        if (logoImage != null) {
            logoCell.add(logoImage);
        } else {
            logoCell.add(new Paragraph("OSEN")
                    .setFont(latinBoldFont)
                    .setFontSize(26)
                    .setFontColor(PRIMARY_TEXT_COLOR)
                    .setMargin(0));
            logoCell.add(new Paragraph("FURNITURE")
                    .setFont(latinFont)
                    .setFontSize(11)
                    .setFontColor(SECONDARY_TEXT_COLOR)
                    .setMarginTop(4)
                    .setMarginBottom(0));
        }

        Paragraph titleParagraph = new Paragraph()
                .add(new com.itextpdf.layout.element.Text("INQUIRY DISPATCH SHEET")
                        .setFont(latinBoldFont)
                        .setFontSize(16)
                        .setFontColor(PRIMARY_TEXT_COLOR))
                .add("\n")
                .add(new com.itextpdf.layout.element.Text("询盘下发单")
                        .setFont(bodyFont)
                        .setFontSize(9)
                        .setFontColor(SECONDARY_TEXT_COLOR));

        Cell titleCell = createPlainCell()
                .setTextAlignment(TextAlignment.RIGHT)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .add(titleParagraph);

        table.addCell(logoCell);
        table.addCell(titleCell);
        return table;
    }

    /**
     * 创建单据基础信息表，集中展示单号、时间、状态等核心字段。
     */
    private Table createMetaTable(Inquiry inquiry, PdfFont bodyFont, PdfFont latinFont, PdfFont latinBoldFont) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{18, 32, 18, 32}))
                .useAllAvailableWidth()
                .setMarginBottom(18);

        addMetaCell(table, "Inquiry No.", defaultText(inquiry.getInquiryNo()), latinFont, latinBoldFont);
        addMetaCell(table, "Date", formatDateTime(inquiry.getCreateTime()), latinFont, latinBoldFont);
        addMetaCell(table, "Status", getStatusDisplayText(inquiry.getStatus()), latinFont, bodyFont);
        addMetaCell(table, "Prepared By", PREPARED_BY_TEXT, latinFont, latinBoldFont);
        addMetaCell(table, "Total Qty", String.valueOf(calculateTotalQuantity(inquiry, null)), latinFont, latinBoldFont);
        addMetaCell(table, "Total Amount", formatCurrency(inquiry.getTotalAmount()), latinFont, latinBoldFont);

        return table;
    }

    /**
     * 创建标准分区块，保证整个 PDF 的结构统一且打印时层次清晰。
     */
    private Table createSectionBlock(String title, Table contentTable, PdfFont latinBoldFont) {
        Table sectionTable = new Table(1)
                .useAllAvailableWidth()
                .setMarginBottom(16);

        Cell titleCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setPaddingTop(0)
                .setPaddingBottom(8)
                .setPaddingLeft(0)
                .setPaddingRight(0)
                .add(new Paragraph(title)
                        .setFont(latinBoldFont)
                        .setFontSize(9)
                        .setFontColor(SECONDARY_TEXT_COLOR)
                        .setMargin(0));

        Cell contentCell = new Cell()
                .setBorderTop(new SolidBorder(LINE_COLOR, 0.8f))
                .setBorderBottom(Border.NO_BORDER)
                .setBorderLeft(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER)
                .setPaddingTop(12)
                .setPaddingBottom(0)
                .setPaddingLeft(0)
                .setPaddingRight(0)
                .add(contentTable);

        sectionTable.addCell(titleCell);
        sectionTable.addCell(contentCell);
        return sectionTable;
    }

    /**
     * 创建客户信息表，保持信息区域留白充足，方便纸面阅读。
     */
    private Table createCustomerTable(Inquiry inquiry, PdfFont bodyFont, PdfFont latinFont, PdfFont latinBoldFont) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{20, 80}))
                .useAllAvailableWidth();

        addInfoRow(table, "Customer Name", defaultText(inquiry.getUserName()), latinFont, bodyFont);
        addInfoRow(table, "Email", defaultText(inquiry.getUserEmail()), latinFont, bodyFont);
        addInfoRow(table, "WhatsApp", defaultText(inquiry.getUserWhatsapp()), latinFont, bodyFont);
        addInfoRow(table, "Customer Note", defaultText(inquiry.getRemark()), latinFont, bodyFont);

        return table;
    }

    /**
     * 创建商品区域，包含商品表格与右侧汇总信息。
     */
    private Table createProductBlock(Inquiry inquiry, List<InquiryItem> items, PdfFont bodyFont, PdfFont latinFont, PdfFont latinBoldFont) {
        Table wrapperTable = new Table(1)
                .useAllAvailableWidth();

        wrapperTable.addCell(createPlainCell().add(createItemTable(items, bodyFont, latinFont, latinBoldFont)));
        wrapperTable.addCell(createPlainCell().setPaddingTop(10).add(createSummaryTable(inquiry, items, latinFont, latinBoldFont)));
        return wrapperTable;
    }

    /**
     * 创建商品明细表，采用轻量表格线条，兼顾品牌感与扫描效率。
     */
    private Table createItemTable(List<InquiryItem> items, PdfFont bodyFont, PdfFont latinFont, PdfFont latinBoldFont) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{8, 32, 24, 14, 8, 14}))
                .useAllAvailableWidth();

        addHeaderCell(table, "No.", latinBoldFont);
        addHeaderCell(table, "Product", latinBoldFont);
        addHeaderCell(table, "Spec / SKU", latinBoldFont);
        addHeaderCell(table, "Unit Price", latinBoldFont);
        addHeaderCell(table, "Qty", latinBoldFont);
        addHeaderCell(table, "Amount", latinBoldFont);

        if (items == null || items.isEmpty()) {
            Cell emptyCell = new Cell(1, 6)
                    .setBorderTop(Border.NO_BORDER)
                    .setBorderLeft(Border.NO_BORDER)
                    .setBorderRight(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(LINE_COLOR, 0.8f))
                    .setPaddingTop(14)
                    .setPaddingBottom(14)
                    .setPaddingLeft(8)
                    .setPaddingRight(8)
                    .add(new Paragraph("暂无商品明细")
                            .setFont(bodyFont)
                            .setFontSize(10)
                            .setFontColor(SECONDARY_TEXT_COLOR)
                            .setTextAlignment(TextAlignment.CENTER));
            table.addCell(emptyCell);
            return table;
        }

        for (int i = 0; i < items.size(); i++) {
            InquiryItem item = items.get(i);
            addBodyCell(table, String.format("%02d", i + 1), bodyFont, TextAlignment.CENTER);
            addBodyCell(table, defaultText(item.getProductName()), bodyFont, TextAlignment.LEFT);
            addBodyCell(table, defaultText(item.getSkuSpec()), bodyFont, TextAlignment.LEFT);
            addBodyCell(table, formatCurrency(item.getPrice()), bodyFont, TextAlignment.RIGHT);
            addBodyCell(table, String.valueOf(item.getQuantity() != null ? item.getQuantity() : 0), bodyFont, TextAlignment.CENTER);
            addBodyCell(table, formatCurrency(calculateLineAmount(item)), bodyFont, TextAlignment.RIGHT);
        }

        return table;
    }

    /**
     * 创建右侧汇总信息区域，用于强调总数量与总金额。
     */
    private Table createSummaryTable(Inquiry inquiry, List<InquiryItem> items, PdfFont latinFont, PdfFont latinBoldFont) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{52, 24, 24}))
                .setWidth(220)
                .setHorizontalAlignment(HorizontalAlignment.RIGHT);

        BigDecimal totalAmount = items == null || items.isEmpty()
                ? (inquiry != null ? defaultAmount(inquiry.getTotalAmount()) : BigDecimal.ZERO)
                : calculateTotalAmount(items);
        int totalQuantity = calculateTotalQuantity(inquiry, items);

        table.addCell(createSummarySpacerCell());
        table.addCell(createSummaryLabelCell("Total Qty", latinFont));
        table.addCell(createSummaryValueCell(String.valueOf(totalQuantity), latinBoldFont, PRIMARY_TEXT_COLOR));

        table.addCell(createSummarySpacerCell());
        table.addCell(createSummaryLabelCell("Total Amount", latinFont));
        table.addCell(createSummaryValueCell(formatCurrency(totalAmount), latinBoldFont, ACCENT_GOLD_COLOR));

        return table;
    }

    /**
     * 创建备注区域，使用盒状留白提升纸质文档的正式感。
     */
    private Table createNoteTable(String note, PdfFont bodyFont) {
        Table table = new Table(1)
                .useAllAvailableWidth();

        Cell noteCell = new Cell()
                .setBorder(new SolidBorder(LINE_COLOR, 0.8f))
                .setBackgroundColor(SECTION_BG_COLOR)
                .setPaddingTop(12)
                .setPaddingBottom(12)
                .setPaddingLeft(12)
                .setPaddingRight(12)
                .setMinHeight(64)
                .add(new Paragraph(defaultText(note))
                        .setFont(bodyFont)
                        .setFontSize(10)
                        .setFontColor(PRIMARY_TEXT_COLOR)
                        .setMargin(0)
                        .setMultipliedLeading(1.35f));
        table.addCell(noteCell);
        return table;
    }

    /**
     * 创建签字区域，为后续内部确认与纸面流转预留位置。
     */
    private Table createSignatureTable(PdfFont bodyFont, PdfFont latinFont, PdfFont latinBoldFont) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .useAllAvailableWidth()
                .setMarginTop(4)
                .setMarginBottom(0);

        table.addCell(createSignatureCell("Handler Signature", latinFont, latinBoldFont, bodyFont));
        table.addCell(createSignatureCell("Reviewer Signature", latinFont, latinBoldFont, bodyFont));
        return table;
    }

    private Cell createSignatureCell(String title, PdfFont latinFont, PdfFont latinBoldFont, PdfFont bodyFont) {
        Table innerTable = new Table(1)
                .useAllAvailableWidth();

        innerTable.addCell(createPlainCell().setPaddingBottom(12).add(new Paragraph(title)
                .setFont(latinFont)
                .setFontSize(9)
                .setFontColor(SECONDARY_TEXT_COLOR)
                .setMargin(0)));
        innerTable.addCell(new Cell()
                .setBorderTop(Border.NO_BORDER)
                .setBorderLeft(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(LINE_COLOR, 0.8f))
                .setHeight(30)
                .setPadding(0)
                .add(new Paragraph(" ")
                        .setFont(latinBoldFont)
                        .setFontSize(10)
                        .setMargin(0)));
        innerTable.addCell(createPlainCell().setPaddingTop(8).add(new Paragraph("签字后可用于内部流转确认")
                .setFont(bodyFont)
                .setFontSize(8)
                .setFontColor(SECONDARY_TEXT_COLOR)
                .setMargin(0)));

        return new Cell()
                .setBorder(Border.NO_BORDER)
                .setPaddingLeft(0)
                .setPaddingRight(18)
                .add(innerTable);
    }

    /**
     * 创建页脚信息，保持品牌露出但不过度打扰正文。
     */
    private Paragraph createFooterParagraph(PdfFont bodyFont, PdfFont latinFont) {
        return new Paragraph()
                .add(new com.itextpdf.layout.element.Text("OSEN FURNITURE")
                        .setFont(latinFont)
                        .setFontSize(8)
                        .setFontColor(PRIMARY_TEXT_COLOR))
                .add("  ")
                .add(new com.itextpdf.layout.element.Text("询盘下发单导出时间：")
                        .setFont(bodyFont)
                        .setFontSize(8)
                        .setFontColor(SECONDARY_TEXT_COLOR))
                .add(new com.itextpdf.layout.element.Text(java.time.LocalDateTime.now().format(DATE_TIME_FORMATTER))
                        .setFont(latinFont)
                        .setFontSize(8)
                        .setFontColor(SECONDARY_TEXT_COLOR))
                .setTextAlignment(TextAlignment.RIGHT)
                .setMargin(0);
    }

    private Image createLogoImage(PdfDocument pdfDoc) {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("static/logo.svg");
        if (inputStream == null) {
            log.warn("未找到品牌Logo资源文件，将回退为文本品牌头");
            return null;
        }

        try (inputStream) {
            Image logoImage = SvgConverter.convertToImage(inputStream, pdfDoc);
            logoImage.scaleToFit(180, 54);
            logoImage.setAutoScale(false);
            return logoImage;
        } catch (Exception e) {
            log.warn("加载品牌Logo失败，将回退为文本品牌头，原因：{}", e.getMessage());
            return null;
        }
    }

    private void addMetaCell(Table table, String label, String value, PdfFont labelFont, PdfFont valueFont) {
        table.addCell(new Cell()
                .setBorder(Border.NO_BORDER)
                .setPaddingTop(2)
                .setPaddingBottom(8)
                .setPaddingLeft(0)
                .setPaddingRight(8)
                .add(new Paragraph(label)
                        .setFont(labelFont)
                        .setFontSize(8)
                        .setFontColor(SECONDARY_TEXT_COLOR)
                        .setMargin(0)));
        table.addCell(new Cell()
                .setBorder(Border.NO_BORDER)
                .setPaddingTop(2)
                .setPaddingBottom(8)
                .setPaddingLeft(0)
                .setPaddingRight(18)
                .add(new Paragraph(value)
                        .setFont(valueFont)
                        .setFontSize(10)
                        .setFontColor(PRIMARY_TEXT_COLOR)
                        .setMargin(0)));
    }

    private void addInfoRow(Table table, String label, String value, PdfFont labelFont, PdfFont valueFont) {
        table.addCell(new Cell()
                .setBorderTop(Border.NO_BORDER)
                .setBorderLeft(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(LINE_COLOR, 0.6f))
                .setPaddingTop(10)
                .setPaddingBottom(10)
                .setPaddingLeft(0)
                .setPaddingRight(12)
                .add(new Paragraph(label)
                        .setFont(labelFont)
                        .setFontSize(9)
                        .setFontColor(SECONDARY_TEXT_COLOR)
                        .setMargin(0)));
        table.addCell(new Cell()
                .setBorderTop(Border.NO_BORDER)
                .setBorderLeft(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(LINE_COLOR, 0.6f))
                .setPaddingTop(10)
                .setPaddingBottom(10)
                .setPaddingLeft(0)
                .setPaddingRight(0)
                .add(new Paragraph(value)
                        .setFont(valueFont)
                        .setFontSize(10)
                        .setFontColor(PRIMARY_TEXT_COLOR)
                        .setMargin(0)));
    }

    private void addHeaderCell(Table table, String text, PdfFont font) {
        table.addCell(new Cell()
                .setBackgroundColor(SECTION_BG_COLOR)
                .setBorderTop(new SolidBorder(ACCENT_GOLD_COLOR, 0.8f))
                .setBorderBottom(new SolidBorder(LINE_COLOR, 0.8f))
                .setBorderLeft(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER)
                .setPaddingTop(8)
                .setPaddingBottom(8)
                .setPaddingLeft(6)
                .setPaddingRight(6)
                .add(new Paragraph(text)
                        .setFont(font)
                        .setFontSize(9)
                        .setFontColor(PRIMARY_TEXT_COLOR)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMargin(0)));
    }

    private void addBodyCell(Table table, String text, PdfFont font, TextAlignment textAlignment) {
        table.addCell(new Cell()
                .setBorderTop(Border.NO_BORDER)
                .setBorderLeft(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(LINE_COLOR, 0.6f))
                .setPaddingTop(10)
                .setPaddingBottom(10)
                .setPaddingLeft(6)
                .setPaddingRight(6)
                .add(new Paragraph(text)
                        .setFont(font)
                        .setFontSize(10)
                        .setFontColor(PRIMARY_TEXT_COLOR)
                        .setTextAlignment(textAlignment)
                        .setMargin(0)));
    }

    private Cell createSummarySpacerCell() {
        return new Cell()
                .setBorder(Border.NO_BORDER)
                .setPadding(0)
                .add(new Paragraph(" ").setMargin(0));
    }

    private Cell createSummaryLabelCell(String text, PdfFont font) {
        return new Cell()
                .setBorder(Border.NO_BORDER)
                .setPaddingTop(4)
                .setPaddingBottom(4)
                .setPaddingLeft(0)
                .setPaddingRight(8)
                .add(new Paragraph(text)
                        .setFont(font)
                        .setFontSize(8)
                        .setFontColor(SECONDARY_TEXT_COLOR)
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setMargin(0));
    }

    private Cell createSummaryValueCell(String text, PdfFont font, DeviceRgb color) {
        return new Cell()
                .setBorder(Border.NO_BORDER)
                .setPaddingTop(4)
                .setPaddingBottom(4)
                .setPaddingLeft(0)
                .setPaddingRight(0)
                .add(new Paragraph(text)
                        .setFont(font)
                        .setFontSize(10)
                        .setFontColor(color)
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setMargin(0));
    }

    private Cell createPlainCell() {
        return new Cell()
                .setBorder(Border.NO_BORDER)
                .setPadding(0);
    }

    private LineSeparator createDivider(float thickness, float marginTop, float marginBottom) {
        SolidLine line = new SolidLine(thickness);
        line.setColor(ACCENT_GOLD_COLOR);
        return new LineSeparator(line)
                .setMarginTop(marginTop)
                .setMarginBottom(marginBottom);
    }

    private BigDecimal calculateLineAmount(InquiryItem item) {
        if (item == null || item.getPrice() == null || item.getQuantity() == null) {
            return BigDecimal.ZERO;
        }
        return item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
    }

    private BigDecimal calculateTotalAmount(List<InquiryItem> items) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (items == null) {
            return totalAmount;
        }
        for (InquiryItem item : items) {
            totalAmount = totalAmount.add(calculateLineAmount(item));
        }
        return totalAmount;
    }

    private int calculateTotalQuantity(Inquiry inquiry, List<InquiryItem> items) {
        if (inquiry != null && inquiry.getTotalQuantity() != null) {
            return inquiry.getTotalQuantity();
        }

        int totalQuantity = 0;
        if (items == null) {
            return totalQuantity;
        }
        for (InquiryItem item : items) {
            totalQuantity += item != null && item.getQuantity() != null ? item.getQuantity() : 0;
        }
        return totalQuantity;
    }

    private String formatCurrency(BigDecimal amount) {
        return "¥" + defaultAmount(amount).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String formatDateTime(java.time.LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : DEFAULT_TEXT;
    }

    private String defaultText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return DEFAULT_TEXT;
        }
        return text.trim();
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return amount != null ? amount : BigDecimal.ZERO;
    }

    private String getStatusDisplayText(Integer status) {
        if (status == null) {
            return "未知 / Unknown";
        }
        return switch (status) {
            case 0 -> "待处理 / Pending";
            case 1 -> "已联系 / Contacted";
            case 2 -> "已完成 / Completed";
            case 3 -> "已取消 / Cancelled";
            default -> "未知 / Unknown";
        };
    }
}

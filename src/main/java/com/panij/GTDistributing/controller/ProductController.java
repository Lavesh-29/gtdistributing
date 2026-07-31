package com.panij.GTDistributing.controller;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.borders.DashedBorder;

import com.panij.GTDistributing.model.*;
import com.panij.GTDistributing.repository.CustomerRepository;
import com.panij.GTDistributing.repository.OrderLineRepository;
import com.panij.GTDistributing.repository.OrderRepository;
import com.panij.GTDistributing.repository.ProductRepo;
import com.panij.GTDistributing.service.ProductService;
import com.panij.GTDistributing.service.ReCaptchaService;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.property.UnitValue;
import com.itextpdf.layout.property.TextAlignment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.mail.MessagingException;
import javax.servlet.http.HttpSession;

import java.math.BigDecimal;
import java.security.Principal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ProductController {

    private final ProductService productService;
    private final ReCaptchaService reCaptchaService; // Inject ReCaptchaService

    @Value("${recaptcha.secret.key}")
    private String recaptchaSecretKey;

    @Autowired
    public ProductController(ProductService productService, ReCaptchaService reCaptchaService) {
        this.productService = productService;
        this.reCaptchaService = reCaptchaService;
    }
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderLineRepository orderLineRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private ProductRepo productRepo;
    @GetMapping("/products")
    public String showProductList(Model model) {
        List<Product> products = productService.getAllProductsSorted();
        model.addAttribute("products", products);
        model.addAttribute("categories", productService.getDistinctCategories());
        return "Product/index";
    }
    @PostMapping("/bulk-update-quantity")
    public ResponseEntity<?> bulkUpdateQuantity(@RequestBody List<QuantityUpdateRequest> updates) {
        for (QuantityUpdateRequest update : updates) {
            productRepo.findById(update.getId()).ifPresent(product -> {
                product.setQuantity(update.getQuantity());
                productRepo.save(product);
            });
        }
        return ResponseEntity.ok("Quantities updated successfully");
    }

    // DTO class
    @Setter
    @Getter
    public static class QuantityUpdateRequest {
        private Integer id;       // ✅ Make sure this is Integer, not Long
        private Integer quantity; // ✅ Also Integer

    }

    @GetMapping("/admin")
    public String productList(Model model) {
        List<Product> products = productService.getAllProductsSorted();
        model.addAttribute("products", products);
        return "Admin/index";
    }

    @GetMapping("/create")
    public String showCreatePage(Model model) {
        model.addAttribute("product", new Product());
        return "Admin/addproduct";
    }

    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<?> createProduct(@ModelAttribute Product product,
                                           @RequestParam("imageFile") MultipartFile imageFile) {
        try {
            productService.saveOrUpdateProduct(product, imageFile);
            return ResponseEntity.ok().body("Product added successfully!");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving image file: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating product: " + e.getMessage());
        }
    }

    @GetMapping("/edit/{id}")
    public String editProductPage(@PathVariable("id") Integer id, Model model) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return "redirect:/admin";
        }
        model.addAttribute("imagePath", product.getImage_Path());
        model.addAttribute("product", product);
        return "Edit/index";
    }

    @PostMapping("/edit/{id}")
    public String editProduct(@PathVariable Integer id, @ModelAttribute Product product,
                              @RequestParam("imageFile") MultipartFile imageFile) throws IOException {
        // Check if a new image was uploaded
        if (!imageFile.isEmpty()) {
            // If a new image is uploaded, save it and set the new image path
            productService.saveOrUpdateProduct(product, imageFile); // pass imageFile for saving
        } else {
            // If no new image is uploaded, keep the existing image path
            Product existingProduct = productService.getProductById(id);
            product.setImage_Path(existingProduct.getImage_Path()); // preserve the existing image path
            productService.saveOrUpdateProduct(product, null); // Pass null for imageFile if no image is uploaded
        }

        // Save or update the product with the image path
        return "redirect:/admin";
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable("id") Integer id) {
        productService.deleteProductById(id);
        return ResponseEntity.ok("Product deleted successfully");
    }

    @GetMapping("/index")
    public String getHomePage() {
        return "index";
    }

    @GetMapping("/return")
    public String returnPage() {
        return "Return/index";
    }

    @GetMapping("/contact")
    public String contactPage() {
        return "Contact/index";
    }

    @GetMapping("/OrderForm")
    public String OrderFormPage() {
        return "OrderForm/index";
    }

    @GetMapping("/Promotion")
    public String PromotionPage() {
        return "Promotion/index";
    }

    @GetMapping("/registration")
    public String registrationPage() {
        return "Registration/index";
    }
    @GetMapping("/products/zero-qty")
    public ResponseEntity<InputStreamResource> downloadZeroQtyProductPdf() {
        ByteArrayInputStream bis = productService.generatePdfForZeroQtyProducts();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=zero_qty_products.pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }

    @GetMapping("/order")
    public String showProducts(Model model) {
        List<Product> products = productService.getAllProductsSorted()
                .stream()
                .filter(p -> p.getQuantity() != null && p.getQuantity() > 0)
                .collect(Collectors.toList());

        model.addAttribute("products", products);
        model.addAttribute("categories", productService.getDistinctCategories());
        return "Order/index";
    }


    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/sendOrder")
    public ResponseEntity<?> sendOrder(@RequestBody OrderRequest orderRequest) {
        try {
            String customerEmail = orderRequest.getEmailId();
            System.out.println("Received order request with email: " + customerEmail);

            if (customerEmail != null && !customerEmail.trim().isEmpty() &&
                    !EmailValidator.getInstance().isValid(customerEmail)) {
                System.out.println("Invalid email address: " + customerEmail);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid email address provided.");
            }

            // Get custNo from request and fetch customer from DB
            Integer custNo;
            try {
                custNo = Integer.parseInt(orderRequest.getAccountNo());
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body("Invalid account number format.");
            }

            Optional<Customer> customerOpt = customerRepository.findByCustNo(custNo);
            if (customerOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Customer not found for account no: " + custNo);
            }
            Customer customer = customerOpt.get();
            String password = customer.getPassword();
            /*
             * SAVE ORDER HEADER
             */
            OrderHeader order = new OrderHeader();

            order.setCustomerNo(orderRequest.getAccountNo());
            order.setCustomerName(orderRequest.getCustomerName());
            order.setEmail(orderRequest.getEmailId());
            order.setOrderDate(LocalDateTime.now());

            order.setTotalPrice(
                    BigDecimal.valueOf(orderRequest.getTotalPrice())
            );

            order.setStatus("NEW");

            order = orderRepository.save(order);

            /*
             * SAVE ORDER LINES
             */
            for (OrderItem item : orderRequest.getItems()) {

                OrderLine line = new OrderLine();

                line.setOrderId(order.getId());
                line.setItemRef(item.getItem_Ref());
                line.setDescription(item.getDescription());
                line.setQty(item.getQty());

                orderLineRepository.save(line);
            }
            // PDF Generation
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter pdfWriter = new PdfWriter(baos);
            PdfDocument pdfDocument = new PdfDocument(pdfWriter);
            Document document = new Document(pdfDocument, PageSize.A4);
            document.setMargins(30, 30, 30, 30); // top, right, bottom, left

            document.add(new Paragraph("Customer : " + orderRequest.getCustomerName().toUpperCase()).setBold().setFontSize(12));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
            ZoneId estZoneId = ZoneId.of("America/New_York");
            String formattedDateTime = ZonedDateTime.now(estZoneId).format(formatter);

            document.add(new Paragraph("Customer No: " + orderRequest.getAccountNo() + "    Account No: " + password + "    " + formattedDateTime).setBold().setFontSize(11));
            document.add(new Paragraph("Order Details").setBold().setFontSize(10));

            List<OrderItem> items = orderRequest.getItems();
            items.sort(Comparator.comparing(OrderItem::getItem_Ref));

            Table table = new Table(6);
            table.setWidth(UnitValue.createPercentValue(100));

            // Add headers
            for (int i = 0; i < 2; i++) {
                table.addCell(new Cell().add(new Paragraph("Item No").setBold().setFontSize(10)));
                table.addCell(new Cell().add(new Paragraph("Description").setBold().setFontSize(10)));
                table.addCell(new Cell().add(new Paragraph("Qty").setBold().setFontSize(10)));
            }

            // Split into two columns
            int halfSize = (int) Math.ceil(items.size() / 2.0);
            List<OrderItem> column1Items = items.subList(0, halfSize);
            List<OrderItem> column2Items = items.subList(halfSize, items.size());
            int maxRows = Math.max(column1Items.size(), column2Items.size());

            for (int i = 0; i < maxRows; i++) {
                if (i < column1Items.size()) {
                    OrderItem item1 = column1Items.get(i);
                    table.addCell(new Cell().add(new Paragraph(item1.getItem_Ref()).setFontSize(10)));
                    table.addCell(new Cell().add(new Paragraph(item1.getDescription()).setFontSize(10)));
                    table.addCell(new Cell().add(new Paragraph(String.valueOf(item1.getQty())).setFontSize(10)));
                } else {
                    table.addCell(new Cell());
                    table.addCell(new Cell());
                    table.addCell(new Cell());
                }

                if (i < column2Items.size()) {
                    OrderItem item2 = column2Items.get(i);
                    table.addCell(new Cell().add(new Paragraph(item2.getItem_Ref()).setFontSize(10)));
                    table.addCell(new Cell().add(new Paragraph(item2.getDescription()).setFontSize(10)));
                    table.addCell(new Cell().add(new Paragraph(String.valueOf(item2.getQty())).setFontSize(10)));
                } else {
                    table.addCell(new Cell());
                    table.addCell(new Cell());
                    table.addCell(new Cell());
                }
            }

            document.add(table);
            // Summary section
            Div summaryDiv = new Div().setKeepTogether(true);
//            summaryDiv.add(new Paragraph("Total Quantity: " + orderRequest.getTotalQuantity()).setFontSize(11).setBold());

            DecimalFormat decimalFormat = new DecimalFormat("$#,##0.00");
            String formattedTotalPrice = decimalFormat.format(orderRequest.getTotalPrice());
            summaryDiv.add(new Paragraph("Approx Order Total: " + formattedTotalPrice).setFontSize(11).setBold());

            document.add(summaryDiv);

            // Footer
            Table footerTable = new Table(UnitValue.createPercentArray(new float[]{1, 1})).useAllAvailableWidth();
            footerTable.setMarginTop(10f);

            footerTable.addCell(new Cell().add(new Paragraph("Order #: ________________")).setBorder(Border.NO_BORDER).setFontSize(10));
            footerTable.addCell(new Cell().add(new Paragraph("Count: ________________")).setBorder(Border.NO_BORDER).setFontSize(10));
            footerTable.addCell(new Cell().add(new Paragraph("Pulled By: ______________")).setBorder(Border.NO_BORDER).setFontSize(10));
            footerTable.addCell(new Cell().add(new Paragraph("Checked By: ____________")).setBorder(Border.NO_BORDER).setFontSize(10));
            footerTable.addCell(new Cell().add(new Paragraph("Box: __________________")).setBorder(Border.NO_BORDER).setFontSize(10));
            footerTable.addCell(new Cell().add(new Paragraph("Tote: __________________")).setBorder(Border.NO_BORDER).setFontSize(10));

            document.add(footerTable);

            // Finalize PDF
            document.close();
            pdfDocument.close();
            pdfWriter.close();

            byte[] pdfBytes = baos.toByteArray();
            baos.close();

            productService.sendOrderWithAttachment(pdfBytes, orderRequest.getEmailId(), orderRequest.getCustomerName());


            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to generate PDF: " + e.getMessage());
        }
    }

    private ByteArrayOutputStream createRegistrationPdf(RegistrationForm form) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        // Reduced margins to fit everything tightly onto a single page
        document.setMargins(15, 15, 15, 15);

        document.add(new Paragraph("GT Distributing").setBold().setFontSize(14f).setTextAlignment(TextAlignment.CENTER).setMarginBottom(0));
        document.add(new Paragraph("385 SW 60th Avenue | Ocala, Florida 34474").setFontSize(9f).setTextAlignment(TextAlignment.CENTER).setMarginBottom(0));
        document.add(new Paragraph("Ph (352) 873-0400 | Email: sales@gtdistributing.com | www.gtdistributing.com").setFontSize(9f).setTextAlignment(TextAlignment.CENTER).setMarginBottom(4));
        document.add(new Paragraph("CUSTOMER ACCOUNT INFORMATION & PAYMENT AGREEMENT").setBold().setFontSize(11f).setTextAlignment(TextAlignment.CENTER).setMarginBottom(4));

        Table formTable = new Table(UnitValue.createPercentArray(new float[]{1, 2, 1, 2})).useAllAvailableWidth();

        formTable.addCell(cell("Legal Name:", true));
        formTable.addCell(cell(form.getLegalName()));
        formTable.addCell(cell("DBA:", true));
        formTable.addCell(cell(form.getDba()));

        formTable.addCell(cell("Address:", true));
        formTable.addCell(cell(form.getAddress()));
        formTable.addCell(cell("City, State, Zip:", true));
        formTable.addCell(cell(form.getCity() + ", " + form.getState() + ", " + form.getZip()));

        formTable.addCell(cell("Tobacco Lic #:", true));
        formTable.addCell(cell(form.getLicense()));
        formTable.addCell(cell("Tax ID or SS#:", true));
        formTable.addCell(cell(form.getTaxId()));

        formTable.addCell(cell("Sales Tax#:", true));
        formTable.addCell(cell(form.getSalesTax()));
        formTable.addCell(cell("Payment Method:", true));
        formTable.addCell(cell("ach".equals(form.getPaymentMethod()) ? "ACH / Bank Draft" : "Cash / Written Check"));

        formTable.addCell(cell("Type of Business:", true));
        formTable.addCell(cell(form.getBusinessType()));
        formTable.addCell(cell("Owner/Officer:", true));
        formTable.addCell(cell(form.getContactPerson()));

        formTable.addCell(cell("Phone:", true));
        formTable.addCell(cell(form.getBusinessPhone()));
        formTable.addCell(cell("Title:", true));
        formTable.addCell(cell(form.getTitle()));

        formTable.addCell(cell("Cell:", true));
        formTable.addCell(cell(form.getCellPhone()));
        formTable.addCell(cell("Fax:", true));
        formTable.addCell(cell(form.getFax()));

        formTable.addCell(cell("Email:", true));
        formTable.addCell(cell(form.getEmail()));
        formTable.addCell(cell(""));
        formTable.addCell(cell(""));

        document.add(formTable);

        // Conditional Check Draft Authorization Section for PDF (Included only if ACH is chosen)
        if ("ach".equals(form.getPaymentMethod())) {
            document.add(new Paragraph("Check Draft Authorization Agreement").setBold().setFontSize(10f).setMarginTop(4f).setMarginBottom(2f));

            String contactName = (form.getContactPerson() != null && !form.getContactPerson().isEmpty()) ? form.getContactPerson() : "Authorized Representative";
            String storeName = (form.getDba() != null && !form.getDba().isEmpty()) ? form.getDba() : form.getLegalName();

            String checkAuthText = "I, " + contactName + ", as the authorized account holder, do hereby authorize GT Distributing to duplicate the attached, or otherwise provided check, in bank draft form. " +
                    "This is an open authorization to allow debits to my account in check form for amounts which will vary per transaction based on the order amount at delivery. " +
                    "In lieu of a check number, GT Distributing will use the invoice number as a reference for payments processed. " +
                    "I have read and agree to all the terms and conditions on this page " +
                    "I understand that this is a legal binding agreement between GT Distributing and " + storeName + ". " +
                    "This agreement remains in effect until written cancellation is received.";

            document.add(new Paragraph(checkAuthText).setFontSize(7.5f).setMarginTop(0f).setMarginBottom(4f));

            Table checkTable = new Table(2).useAllAvailableWidth().setMarginTop(2f);
            checkTable.addCell(cell("Bank Name:", true));
            checkTable.addCell(cell(form.getBankName()));
            checkTable.addCell(cell("Bank Location:", true));
            checkTable.addCell(cell(form.getBankLocation()));
            checkTable.addCell(cell("Checking Account #:", true));
            checkTable.addCell(cell(form.getAccountNumber()));
            checkTable.addCell(cell("Routing #:", true));
            checkTable.addCell(cell(form.getRoutingNumber()));

            document.add(checkTable);
        } else {
            document.add(new Paragraph("Payment Method: Cash / Written Check (Bank info skipped)").setBold().setFontSize(9f).setMarginTop(4f));
        }

        document.add(new Paragraph("TERM & CONDITION: By signing this Agreement, Customer accepts all Terms and Conditions as stated.").setFontSize(8f).setMarginTop(4f).setMarginBottom(2f));

        Table signatureTable = new Table(2).useAllAvailableWidth().setMarginTop(2f);
        signatureTable.addCell(cell("Signature of Authorized Representative", true));
        signatureTable.addCell(cell("Date: " + java.time.LocalDate.now().toString(), true));

        document.add(signatureTable);

        // 🖊️ Signature Image scaled smaller to guarantee one-page fit
        if (form.getSignatureDataUrl() != null && form.getSignatureDataUrl().startsWith("data:image")) {
            String base64 = form.getSignatureDataUrl().split(",")[1];
            byte[] decoded = Base64.getDecoder().decode(base64);
            ImageData imageData = ImageDataFactory.create(decoded);
            Image signatureImage = new Image(imageData).scaleToFit(120, 35).setMarginTop(2f);
            document.add(signatureImage);
        }

        document.close();
        return outputStream;
    }

    // 🔧 Helper for clean, compact table cells
    private Cell cell(String content) {
        return new Cell().add(new Paragraph(content != null ? content : "").setFontSize(8f)).setBorder(Border.NO_BORDER).setPadding(1f);
    }

    private Cell cell(String content, boolean bold) {
        Paragraph paragraph = new Paragraph(content != null ? content : "").setFontSize(8f);
        if (bold) paragraph.setBold();
        return new Cell().add(paragraph).setBorder(Border.NO_BORDER).setPadding(1f);
    }
    @GetMapping("/api/validateItem/{upc}")
    @ResponseBody // tells Spring to return JSON, not HTML
    public ResponseEntity<Product> validateItem(@PathVariable String upc) {
        return productService.getProductByUpc(upc)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/warehouse/orders")
    public String warehouseOrders(HttpSession session, Model model) {

        if (session.getAttribute("employeeName") == null) {
            return "redirect:/emplogin";
        }

        model.addAttribute(
                "orders",
                orderRepository.findAllByOrderByIdDesc()
        );

        return "warehouse-orders";
    }
    @GetMapping("/warehouse/order/{id}")
    public String viewOrder(@PathVariable Long id, Model model) {

        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        List<OrderLine> items = orderLineRepository.findByOrderId(id);

        Customer customer = null;
        try {
            Integer custNo = Integer.parseInt(order.getCustomerNo());
            customer = customerRepository.findByCustNo(custNo).orElse(null);
        } catch (Exception e) {
            // ignore
        }

        model.addAttribute("order", order);
        model.addAttribute("items", items);

        if (customer != null) {
            model.addAttribute("accountNo", customer.getPassword());
        }

        return "warehouse-pick";
    }
    @PostMapping("/warehouse/start/{id}")
    public String startPicking(@PathVariable Long id,
                               HttpSession session) {

        String employee =
                (String) session.getAttribute("employeeName");

        System.out.println("DEBUG employee from session = " + employee);

        OrderHeader order =
                orderRepository.findById(id).orElseThrow();

        order.setStatus("PICKING");
        order.setAssignedEmployee(employee); // IMPORTANT
        order.setStartTime(LocalDateTime.now());

        orderRepository.save(order);

        System.out.println("SAVED employee = " + order.getAssignedEmployee());

        return "redirect:/warehouse/order/" + id;
    }
    @GetMapping("/printOrderPdf/{orderId}")
    public ResponseEntity<byte[]> printOrderPdf(@PathVariable Long orderId) {

        try {
            // ---------------- FETCH ORDER ----------------
            OrderHeader order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            List<OrderLine> lines = orderLineRepository.findByOrderId(orderId);

            // ---------------- CONVERT TO ORDER ITEM LIST ----------------
            List<OrderItem> items = new ArrayList<>();

            for (OrderLine l : lines) {
                OrderItem item = new OrderItem();
                item.setItem_Ref(l.getItemRef());
                item.setDescription(l.getDescription());
                item.setQty(l.getQty());
                items.add(item);
            }

            items.sort(Comparator.comparing(OrderItem::getItem_Ref));

            // ---------------- PDF SETUP ----------------
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(30, 30, 30, 30);

            // ---------------- HEADER ----------------
            document.add(new Paragraph(
                    "Customer : " + order.getCustomerName().toUpperCase()
            ).setBold().setFontSize(12));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
            ZoneId estZoneId = ZoneId.of("America/New_York");

            String formattedDateTime = ZonedDateTime.now(estZoneId).format(formatter);

            Customer customer = customerRepository.findByCustNo(
                    Integer.parseInt(order.getCustomerNo())
            ).orElse(null);

            String password = customer != null ? customer.getPassword() : "";

            document.add(new Paragraph(
                    "Customer No: " + order.getCustomerNo()
                            + "    Account No: " + password
                            + "    " + formattedDateTime
            ).setBold().setFontSize(11));

            document.add(new Paragraph("Order Details").setBold().setFontSize(10));

            // ---------------- TWO COLUMN TABLE ----------------
            Table table = new Table(6);
            table.setWidth(UnitValue.createPercentValue(100));

            // headers (2 sets)
            for (int i = 0; i < 2; i++) {
                table.addCell(new Cell().add(new Paragraph("Item No").setBold().setFontSize(10)));
                table.addCell(new Cell().add(new Paragraph("Description").setBold().setFontSize(10)));
                table.addCell(new Cell().add(new Paragraph("Qty").setBold().setFontSize(10)));
            }

            int halfSize = (int) Math.ceil(items.size() / 2.0);

            List<OrderItem> col1 = items.subList(0, halfSize);
            List<OrderItem> col2 = items.subList(halfSize, items.size());

            int maxRows = Math.max(col1.size(), col2.size());

            for (int i = 0; i < maxRows; i++) {

                // LEFT COLUMN
                if (i < col1.size()) {
                    OrderItem it = col1.get(i);
                    table.addCell(new Cell().add(new Paragraph(it.getItem_Ref()).setFontSize(10)));
                    table.addCell(new Cell().add(new Paragraph(it.getDescription()).setFontSize(10)));
                    table.addCell(new Cell().add(new Paragraph(String.valueOf(it.getQty())).setFontSize(10)));
                } else {
                    table.addCell(new Cell());
                    table.addCell(new Cell());
                    table.addCell(new Cell());
                }

                // RIGHT COLUMN
                if (i < col2.size()) {
                    OrderItem it = col2.get(i);
                    table.addCell(new Cell().add(new Paragraph(it.getItem_Ref()).setFontSize(10)));
                    table.addCell(new Cell().add(new Paragraph(it.getDescription()).setFontSize(10)));
                    table.addCell(new Cell().add(new Paragraph(String.valueOf(it.getQty())).setFontSize(10)));
                } else {
                    table.addCell(new Cell());
                    table.addCell(new Cell());
                    table.addCell(new Cell());
                }
            }

            document.add(table);

            // ---------------- SUMMARY ----------------
            DecimalFormat df = new DecimalFormat("$#,##0.00");

            Div summaryDiv = new Div().setKeepTogether(true);
            summaryDiv.add(new Paragraph(
                    "Approx Order Total: " + df.format(order.getTotalPrice())
            ).setBold().setFontSize(11));

            document.add(summaryDiv);

            // ---------------- FOOTER ----------------
            Table footer = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                    .useAllAvailableWidth();

            footer.setMarginTop(10f);

            footer.addCell(new Cell().add(new Paragraph("Order #: ________________"))
                    .setBorder(Border.NO_BORDER).setFontSize(10));
            footer.addCell(new Cell().add(new Paragraph("Count: ________________"))
                    .setBorder(Border.NO_BORDER).setFontSize(10));
            footer.addCell(new Cell().add(new Paragraph("Pulled By: ______________"))
                    .setBorder(Border.NO_BORDER).setFontSize(10));
            footer.addCell(new Cell().add(new Paragraph("Checked By: ____________"))
                    .setBorder(Border.NO_BORDER).setFontSize(10));
            footer.addCell(new Cell().add(new Paragraph("Box: __________________"))
                    .setBorder(Border.NO_BORDER).setFontSize(10));
            footer.addCell(new Cell().add(new Paragraph("Tote: __________________"))
                    .setBorder(Border.NO_BORDER).setFontSize(10));

            document.add(footer);

            // ---------------- CLOSE ----------------
            document.close();

            byte[] pdfBytes = baos.toByteArray();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=order.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}
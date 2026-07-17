package com.panij.GTDistributing.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.panij.GTDistributing.model.ContactForm;
import com.panij.GTDistributing.model.Product;
import com.panij.GTDistributing.model.RegistrationForm;
import com.panij.GTDistributing.repository.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;

import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.itextpdf.layout.Document;
import javax.mail.internet.InternetAddress;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductService {

    private final ProductRepo productRepo;
    private final JavaMailSender javaMailSender;
    private final String emailTo;
    private final String uploadDir = "/opt/tomcat/uploads/productimg/";



    @Autowired
    public ProductService(ProductRepo productRepo, JavaMailSender javaMailSender, @Value("${email.to}") String emailTo) {
        this.productRepo = productRepo;
        this.javaMailSender = javaMailSender;
        this.emailTo = emailTo;
    }

    public List<Product> getAllProductsSorted() {
        return productRepo.findAllSortedByItemRef();
    }

    public Product getProductById(int id) {
        return productRepo.findById(id).orElse(null);
    }

    public ByteArrayInputStream generatePdfForZeroQtyProducts() {
        List<Product> zeroQtyProducts = productRepo.findAll().stream()
                .filter(p -> p.getQuantity() != null && p.getQuantity() == 0)
                .toList();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        document.add(new Paragraph("Products with Quantity = 0").setBold().setFontSize(18));

        float[] columnWidths = {80f, 350f, 50f};
        Table table = new Table(columnWidths);
        table.addHeaderCell("Item Ref");
        table.addHeaderCell("Description");
        table.addHeaderCell("Category");


        for (Product product : zeroQtyProducts) {
            table.addCell(product.getItem_Ref() != null ? product.getItem_Ref() : "");
            table.addCell(product.getDescription() != null ? product.getDescription() : "");
            table.addCell(product.getCategory() != null ? product.getCategory() : "");

        }

        document.add(table);
        document.close();

        return new ByteArrayInputStream(out.toByteArray());
    }

    // Inside ProductService.java

    public void saveOrUpdateProduct(Product product, MultipartFile imageFile) throws IOException {
        if (imageFile != null && !imageFile.isEmpty()) {
            processImageFile(product, imageFile);
        } else if (product.getImage_Path() == null || product.getImage_Path().isEmpty()) {
            product.setImage_Path("/productimg/default.jpg");
        }
        productRepo.save(product);
    }

    private void processImageFile(Product product, MultipartFile imageFile) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = StringUtils.cleanPath(imageFile.getOriginalFilename());
        String fileExtension = getFileExtension(originalFilename);
        String filenameWithoutExtension = getFilenameWithoutExtension(originalFilename);

        String newFilename = filenameWithoutExtension + fileExtension;
        Path filePath = uploadPath.resolve(newFilename);
        int index = 1;

        while (Files.exists(filePath)) {
            newFilename = filenameWithoutExtension + "_" + index + fileExtension;
            filePath = uploadPath.resolve(newFilename);
            index++;
        }

        Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        product.setImage_Path("/productimg/" + newFilename);
    }
       private void deleteImageFile(String imagePath) {
        if (imagePath != null && !imagePath.isEmpty()) {
            String relativeImagePath = imagePath.replaceFirst("/productimg/", "");
            Path imageFilePath = Paths.get("/opt/tomcat/uploads/productimg/", relativeImagePath);

            try {
                if (Files.exists(imageFilePath)) {
                    Files.delete(imageFilePath);
                    System.out.println("Deleted image file: " + imageFilePath.toString());
                } else {
                    System.err.println("Image file does not exist: " + imageFilePath.toString());
                }
            } catch (IOException e) {
                System.err.println("Failed to delete image file: " + imageFilePath.toString());
                e.printStackTrace();
            }
        } else {
            System.err.println("Image path is null or empty");
        }
    }

    public String saveImage(MultipartFile imageFile) throws IOException {
        String originalFilename = imageFile.getOriginalFilename();
        return "/productimg/" + originalFilename;
    }

    public void sendOrder(String toEmail, String subject, String body, String fromEmail, boolean isHtml) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(toEmail);
        helper.setReplyTo(fromEmail);

        String fromDisplayName = "GT DISTRIBUTING";
        helper.setFrom(new InternetAddress(fromEmail, fromDisplayName));

        helper.setSubject(subject);
        helper.setText(body, isHtml);

        javaMailSender.send(message);
    }

    private String buildContactFormEmailBody(ContactForm contactForm) {
        return String.format(
                "First Name: %s\nMiddle Name: %s\nLast Name: %s\nAddress 1: %s\nAddress 2: %s\nCity: %s\nState: %s\nZip Code: %s\nDaytime Phone: %s\nEvening Phone: %s\nEmail: %s\nComments: %s",
                contactForm.getFirstName(),
                contactForm.getMiddleName(),
                contactForm.getLastName(),
                contactForm.getAddress1(),
                contactForm.getAddress2(),
                contactForm.getCity(),
                contactForm.getState(),
                contactForm.getZipcode(),
                contactForm.getDaytimePhone(),
                contactForm.getEveningPhone(),
                contactForm.getEmail(),
                contactForm.getComments()
        );
    }

    public void sendRegistrationFormEmail(RegistrationForm registrationForm, List<MultipartFile> attachments, byte[] registrationPdfBytes) throws MessagingException {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo("sales@Gtdistributing.com");
            helper.setFrom(new InternetAddress("GTSALESFLORIDA@gmail.com", "GT Distributing"));
            helper.setSubject("New Registration Form Submission");

            helper.setText("Please find attached the registration form details in PDF format.", false);

            helper.addAttachment("RegistrationForm.pdf", new ByteArrayResource(registrationPdfBytes));

            if (attachments != null && !attachments.isEmpty()) {
                for (MultipartFile attachment : attachments) {
                    if (!attachment.isEmpty()) {
                        try {
                            String fileName = attachment.getOriginalFilename();
                            helper.addAttachment(fileName, new ByteArrayResource(attachment.getBytes()));
                            System.out.println("Added attachment: " + fileName);
                        } catch (Exception e) {
                            System.err.println("Error adding attachment: " + attachment.getOriginalFilename() + " - " + e.getMessage());
                        }
                    }
                }
            }

            javaMailSender.send(message);

            SimpleMailMessage userMessage = new SimpleMailMessage();
            userMessage.setTo(registrationForm.getEmail());
            userMessage.setSubject("Registration Confirmation");
            userMessage.setText("Thank you for registering! We have received your form & attachments.");
            javaMailSender.send(userMessage);

        } catch (MessagingException e) {
            System.err.println("Error sending email: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("General Error in sendRegistrationFormEmail: " + e.getMessage());
            throw new MessagingException("General Error in sendRegistrationFormEmail", e);
        }
    }

    public List<String> getDistinctCategories() {
        return productRepo.findDistinctCategories();
    }

    public void sendOrderWithAttachment(byte[] pdfBytes, String customerEmail, String customerName) throws MessagingException, UnsupportedEncodingException {
        String fromEmail = "GTSALESFLORIDA@gmail.com";
        String fromDisplayName = "GT Distributing";
        String internalSubject = customerName + " Order Received";
        String internalBody = "Please find the attached order details.";

        MimeMessage internalMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper internalHelper = new MimeMessageHelper(internalMessage, true);
        internalHelper.setFrom(new InternetAddress(fromEmail, fromDisplayName));
        internalHelper.setTo("sales@GTDistributing.com");
        internalHelper.setCc("kevin@GTDistributing.com");
        internalHelper.addCc("GTSALESFLORIDA@gmail.com");
        internalHelper.setSubject(internalSubject);
        internalHelper.setText(internalBody, true);
        internalHelper.addAttachment("OrderDetails.pdf", new ByteArrayResource(pdfBytes));
        javaMailSender.send(internalMessage);

        if (customerEmail != null && !customerEmail.trim().isEmpty()) {
            MimeMessage customerMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper customerHelper = new MimeMessageHelper(customerMessage, true);
            customerHelper.setFrom(new InternetAddress(fromEmail, fromDisplayName));
            customerHelper.setTo(customerEmail);
            customerHelper.setSubject("Order Confirmation");
            customerHelper.setText("Thank you for your order. Attached are your order details." +
                    " Please verify your final total with your GT Rep due to limited vape inventory.", true);
            customerHelper.addAttachment("OrderDetails.pdf", new ByteArrayResource(pdfBytes));
            javaMailSender.send(customerMessage);
        } else {
            System.out.println("Customer email not provided, skipping confirmation email.");
        }
    }

    public Product validateUPC(String upc) {
        if (upc == null || upc.trim().isEmpty()) {
            System.out.println("UPC is empty or null");
            return null;
        }
        return null; // temporary return to fix compilation
    }
    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0) {
            return filename.substring(dotIndex);
        }
        return "";
    }

    private String getFilenameWithoutExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0) {
            return filename.substring(0, dotIndex);
        }
        return filename;
    }

    public void deleteProductById(Integer id) {
        // Fetch product to delete
        Optional<Product> optionalProduct = productRepo.findById(id);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            // Delete image file from server if exists
            deleteImageFile(product.getImage_Path());
            // Delete product from DB
            productRepo.deleteById(id);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with ID: " + id);
        }
    }
    public Optional<Product> getProductByUpc(String upc) {
        return productRepo.findByUpc(upc);
    }


}

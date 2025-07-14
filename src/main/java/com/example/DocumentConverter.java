package com.example;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;      


import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

public class DocumentConverter {

    // Configurable paths and options
    private final String baseDir = System.getProperty("user.dir"); 

    private String pdfDir = baseDir + File.separator + "temp_pdfs";
    private String imgDir = baseDir + File.separator + "output_images";
    private String finalPdfDir = baseDir + File.separator + "output_pdfs";

    private String templatePath = "template.jpg";
    private int scaleWidth = 900;
    private int textX = 20;
    private int textY = 200;

    // Getters
    public String getPdfDir() { return pdfDir; }
    public String getImgDir() { return imgDir; }
    public String getFinalPdfDir() { return finalPdfDir; }
    public String getTemplatePath() { return templatePath; }
    public int getScaleWidth() { return scaleWidth; }
    public int getTextX() { return textX; }
    public int getTextY() { return textY; }

    // Setters
    public void setPdfDir(String pdfDir) { this.pdfDir = pdfDir; }
    public void setImgDir(String imgDir) { this.imgDir = imgDir; }
    public void setFinalPdfDir(String finalPdfDir) { this.finalPdfDir = finalPdfDir; }
    public void setTemplatePath(String templatePath) { this.templatePath = templatePath; }
    public void setScaleWidth(int scaleWidth) { this.scaleWidth = scaleWidth; }
    public void setTextX(int textX) { this.textX = textX; }
    public void setTextY(int textY) { this.textY = textY; }

    //constructor
    public  DocumentConverter(){
        new File(pdfDir).mkdirs();
        new File(imgDir).mkdirs();
        new File(finalPdfDir).mkdirs();
    }

    // Main processing method
    public void processFiles(List<File> docFiles) {
        if (docFiles == null || docFiles.isEmpty()) {
            System.out.println("No .docx files provided.");
            return;
        }
    
        for (File docx : docFiles) {
            if (!docx.getName().endsWith(".docx")) continue;
    
            String name = docx.getName().replace(".docx", "");
            File pdf = new File(pdfDir, name + ".pdf");
            File jpg = new File(imgDir, name + ".jpg");
            File finalPdf = new File(finalPdfDir, name + ".pdf");
    
            try {
                System.out.println("Processing " + docx.getName());
                convertDocxToPdf(docx, pdf);
                overlayPdfOnTemplate(pdf, jpg);
                convertImageToPdf(jpg, finalPdf);
            } catch (Exception e) {
                System.err.println("Failed to process " + docx.getName() + ": " + e.getMessage());
            }
        }
    }
    

    private void convertDocxToPdf(File docxFile, File outputPdf) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                "libreoffice", "--headless", "--convert-to", "pdf",
                "--outdir", pdfDir, docxFile.getAbsolutePath()
        );
        Process process = pb.start();
        process.waitFor();
    }

    private void overlayPdfOnTemplate(File pdfFile, File outputImage) throws IOException, InterruptedException {
        // Convert PDF to PNG using pdftoppm
        String pngPath = new File(imgDir, "temp.png").getAbsolutePath();
        ProcessBuilder pb = new ProcessBuilder(
                "pdftoppm", "-png", "-singlefile", "-r", "400",
                pdfFile.getAbsolutePath(), pngPath.replace(".png", "")
        );
        pb.start().waitFor();

        BufferedImage template = ImageIO.read(new File(templatePath));
        BufferedImage overlay = ImageIO.read(new File(pngPath));

        int newHeight = (int) (overlay.getHeight() * (scaleWidth / (double) overlay.getWidth()));
        Image scaled = overlay.getScaledInstance(scaleWidth, newHeight, Image.SCALE_SMOOTH);

        BufferedImage combined = new BufferedImage(
                template.getWidth(), template.getHeight(), BufferedImage.TYPE_INT_RGB
        );

        Graphics2D g = combined.createGraphics();
        g.drawImage(template, 0, 0, null);
        g.drawImage(scaled, textX, textY, null);
        g.dispose();

        ImageIO.write(combined, "jpg", outputImage);
    }

    private void convertImageToPdf(File imageFile, File outputPdf) throws IOException {
        PDDocument doc = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);

        BufferedImage img = ImageIO.read(imageFile);
        PDImageXObject pdImage = LosslessFactory.createFromImage(doc, img);
        PDPageContentStream content = new PDPageContentStream(doc, page);
        content.drawImage(pdImage, 0, 0, PDRectangle.A4.getWidth(), PDRectangle.A4.getHeight());
        content.close();

        doc.save(outputPdf);
        doc.close();
    }
}

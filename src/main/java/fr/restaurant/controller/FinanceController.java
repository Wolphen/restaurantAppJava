package fr.restaurant.controller;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.fxml.FXML;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class FinanceController {

    @FXML
    private void exportPDF(){
        Document pdf = new Document();
        try {
            PdfWriter.getInstance(pdf, new FileOutputStream("finances.pdf"));
            pdf.open();
            pdf.add(new Paragraph("Finances"));
            pdf.close();
            System.out.println("pdf created");
        } catch (FileNotFoundException | DocumentException e){
            e.printStackTrace();
        }
    }

}

package fr.restaurant.controller;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import fr.restaurant.model.Order;
import javafx.fxml.FXML;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Map;

public class FinanceController {

    private int depenses;

    @FXML
    private void exportPDF(){
        Document pdf = new Document();
        SqliteController sqliteController = new SqliteController();
        try {
            PdfWriter.getInstance(pdf, new FileOutputStream("finances.pdf"));
            pdf.open();

            // insertion d'un tableau
            PdfPTable table = new PdfPTable(3);
            table.setHorizontalAlignment(Element.ALIGN_CENTER);

            pdf.add(new Paragraph("Finances"));

            // header
            ArrayList<String> headerTable = new ArrayList<String>();
            headerTable.add("Date");
            headerTable.add("Recette");
            headerTable.add("Dépenses");

            headerTable.stream().forEach(column -> {
                PdfPCell header = new PdfPCell();
                header.setPhrase(new Phrase(column));
                table.addCell(header);
            });

            var listRecettes = sqliteController.fetchGlobalPricesDishes();
            System.out.println(listRecettes);
            listRecettes.stream().forEach(price -> {
                System.out.println(price.getValue());
                table.addCell( price.getValue().toString() );
                table.addCell( price.getKey() + " €" );

                // oui j'ai mis un random sur les depenses
                int randomDepence = (int)(Math.random() * 50);
                table.addCell(Integer.toString(randomDepence) + " €");
                depenses += randomDepence;

            });

            pdf.add(table);

            // résultat total
            int sum = listRecettes.stream().mapToInt(Map.Entry::getKey).sum();
            System.out.println(sum);
            var gain = Integer.toString((sum - depenses));
            pdf.add(new Paragraph("Recette Total: " + Integer.toString(sum)));
            pdf.add(new Paragraph("Depense Total: " + depenses));
            pdf.add(new Paragraph("Gain Total: " + gain));



            pdf.close();
            System.out.println("pdf created");
        } catch (FileNotFoundException | DocumentException e){
            e.printStackTrace();
        }
    }



}

package fr.restaurant.controller;

import fr.restaurant.Utils.Chronometer;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.io.IOException;



/** Contrôleur principal : gère la barre de navigation et l’affichage des onglets */
public class RootController {

    @FXML private TabPane mainTabPane;

    @FXML private Tab dashboardTab;
    @FXML private Tab dishesTab;
    @FXML private Tab ordersTab;
    @FXML private Tab tablesTab;
    @FXML private Tab employeesTab;
    @FXML private Tab financesTab;

    @FXML private Label timerLabel;
    private Chronometer currentChrono;
    // initialisation
    @FXML
    private void initialize() {
        mainTabPane.setTabMaxHeight(0);
        openDashboard();
    }



    @FXML
    private void openDashboard() {
        System.out.println(" Ouverture onglet Dashboard");
        var url = getClass().getResource("/fr/restaurant/view/EmployeeView.fxml");
        System.out.println("URL Employee = " + url);
        dashboardTab.setContent(null);


        if (url == null) {
            System.err.println(">> FXML introuvable : vérifie l’emplacement !");
            return;
        }
        selectAndLoad(dashboardTab, "/fr/restaurant/view/DashboardView.fxml");
    }

    @FXML
    private void openDishes() {

        var url = getClass().getResource("/fr/restaurant/view/DishView.fxml");
        System.out.println("URL DishView = " + url);


        if (url == null) {
            System.err.println(">> FXML introuvable : vérifie l’emplacement !");
            return;
        }

        selectAndLoad(dishesTab, "/fr/restaurant/view/DishView.fxml");
    }


    @FXML
    private void openOrders() {
        System.out.println(" Ouverture onglet Order");
        var url = getClass().getResource("/fr/restaurant/view/OrderView.fxml");
        System.out.println("URL DishView = " + url);


        if (url == null) {
            System.err.println(">> FXML introuvable : vérifie l’emplacement !");
            return;
        }

        selectAndLoad(ordersTab, "/fr/restaurant/view/OrderView.fxml");
    }

    @FXML
    private void openTables() {
        System.out.println(" Ouverture onglet Tables");
        var url = getClass().getResource("/fr/restaurant/view/TableView.fxml");
        System.out.println("URL DishView = " + url);


        if (url == null) {
            System.err.println(">> FXML introuvable : vérifie l’emplacement !");
            return;
        }
        tablesTab.setContent(null);

        selectAndLoad(tablesTab, "/fr/restaurant/view/TableView.fxml");
    }

    @FXML
    private void openEmployees() {
        var url = getClass().getResource("/fr/restaurant/view/EmployeeView.fxml");
        System.out.println("URL Employee = " + url);


        if (url == null) {
            System.err.println(">> FXML introuvable : vérifie l’emplacement !");
            return;
        }

        selectAndLoad(employeesTab, "/fr/restaurant/view/EmployeeView.fxml");
    }


    @FXML
    private void openFinances() {
        System.out.println(" Ouverture onglet Finances");

        selectAndLoad(financesTab, "/fr/restaurant/view/FinanceView.fxml");
    }



    private void selectAndLoad(Tab tab, String fxmlPath) {

        /* Charge une seule fois */
        if (tab.getContent() == null) {
            try {
                IOException exception = new IOException("FXML introuvable : " + fxmlPath);
               exception.fillInStackTrace();
                System.out.println(exception);
                Node content = FXMLLoader.load(getClass().getResource(fxmlPath));
                tab.setContent(content);
            } catch (IOException ex) {
                ex.printStackTrace();
                tab.setContent(new Label("Erreur : " + ex.getMessage()));
            }
        }

        /* Sélectionne l’onglet cible */
        mainTabPane.getSelectionModel().select(tab);
    }

    @FXML
    private void onStartChronometer() {

        Chronometer chronometer = new Chronometer();
        chronometer.setOnUpdate(() -> {
            int totalSeconds = chronometer.getValue();
            int minutes = totalSeconds / 60;
            int seconds = totalSeconds % 60;
            timerLabel.setText(String.format("%02d:%02d", minutes, seconds));

            // Détection si moins de 15 minutes restantes
            if (totalSeconds <= 15 * 60) {
                timerLabel.setStyle("-fx-text-fill: red;");
            } else {
                timerLabel.setStyle("-fx-text-fill: black;");
            }

        });
        chronometer.startChronometer(15);}
}

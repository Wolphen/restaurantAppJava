package fr.restaurant.controller;

import fr.restaurant.model.Dish;
import fr.restaurant.model.Order;
import fr.restaurant.model.Table;
import fr.restaurant.service.DishService;
import fr.restaurant.service.TableService;
import fr.restaurant.controller.SqliteController;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.collections.ObservableList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardController {

    @FXML private ListView<Order> lastOrdersList;
    @FXML private ListView<Order> pendingOrdersList;

    @FXML private Label maxDishLabel;
    @FXML private Label minDishLabel;
    @FXML private Label empUnder30Label;
    @FXML private Label emp30to45Label;
    @FXML private Label empOver45Label;
    @FXML private Label totalMenuLabel;

    @FXML private Label presentBillsLabel;
    @FXML private Label leftBillsLabel;

    private final TableService    tableService = TableService.getInstance();
    private final DishService     dishService  = DishService.getInstance();
    private final SqliteController db          = new SqliteController();

    @FXML
    private void initialize() {
        // charger les données
        loadLastFive();
        loadPending();
        loadStats();
        loadBills();

        // formater affichage des commandes
        lastOrdersList.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Order o, boolean empty) {
                super.updateItem(o, empty);
                setText((empty || o == null) ? null : formatOrderLine(o));
            }
        });
        pendingOrdersList.setCellFactory(lastOrdersList.getCellFactory());
    }

    private String formatOrderLine(Order o) {
        return "Cmd #" + o.getId()
                + " – " + o.getGlobalPrice() + "€"
                + " – " + o.getStatus();
    }

    private void loadLastFive() {
        List<Order> orders = tableService.getTables().stream()
                .filter(Table::isOccupied)
                .flatMap(t -> tableService.getOrdersForTable(t.getId()).stream())
                .sorted(Comparator.comparingInt(Order::getId).reversed())
                .limit(5)
                .toList();
        lastOrdersList.setItems(FXCollections.observableArrayList(orders));
    }

    private void loadPending() {
        List<Order> pending = tableService.getTables().stream()
                .flatMap(t -> tableService.getOrdersForTable(t.getId()).stream())
                .filter(o -> "en cours".equalsIgnoreCase(o.getStatus()))
                .sorted(Comparator
                        .comparing((Order o) -> o.getDishes().stream()
                                .map(Dish::getName)
                                .sorted()
                                .findFirst().orElse(""))
                        .thenComparingInt(Order::getId))
                .toList();
        pendingOrdersList.setItems(FXCollections.observableArrayList(pending));
    }

    private void loadStats() {
        // plat le + cher / - cher
        List<Dish> menu = dishService.getObservableList();
        menu.stream().max(Comparator.comparingDouble(Dish::getPrice))
                .ifPresent(d -> maxDishLabel.setText(
                        "Plat le + cher : " + d.getName() + " (" + d.getPrice() + "€)"));
        menu.stream().min(Comparator.comparingDouble(Dish::getPrice))
                .ifPresent(d -> minDishLabel.setText(
                        "Plat le – cher : " + d.getName() + " (" + d.getPrice() + "€)"));

        // employés par tranche d'âge
        var emps = db.fetchEmployee();
        long u30    = emps.stream().filter(e -> e.getAge() < 30).count();
        long _30_45 = emps.stream().filter(e -> e.getAge() >= 30 && e.getAge() <= 45).count();
        long o45    = emps.stream().filter(e -> e.getAge() > 45).count();
        empUnder30Label.setText("Employés < 30 ans : " + u30);
        emp30to45Label .setText("Employés 30–45 ans : " + _30_45);
        empOver45Label .setText("Employés > 45 ans : " + o45);

        // valeur totale de la carte
        double total = menu.stream().mapToDouble(Dish::getPrice).sum();
        totalMenuLabel.setText("Valeur totale carte : " + String.format("%.2f€", total));
    }

    private void loadBills() {
        Map<Boolean, List<Table>> part = tableService.getTables().stream()
                .collect(Collectors.partitioningBy(Table::isOccupied));

        double present = part.get(true).stream()
                .flatMap(t -> tableService.getOrdersForTable(t.getId()).stream())
                .mapToDouble(Order::getGlobalPrice)
                .sum();

        double left = part.get(false).stream()
                .flatMap(t -> tableService.getOrdersForTable(t.getId()).stream())
                .mapToDouble(Order::getGlobalPrice)
                .sum();

        presentBillsLabel.setText("Tables en service : " + String.format("%.2f€", present));
        leftBillsLabel   .setText("Tables libérées    : " + String.format("%.2f€", left));
    }
}

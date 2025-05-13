package fr.restaurant.controller;

import fr.restaurant.model.Employee;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class EmployeeController {

    // injections FXML
    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee,String> nameCol;
    @FXML private TableColumn<Employee,Integer> ageCol;
    @FXML private TableColumn<Employee,String> postCol;
    @FXML private TableColumn<Employee,Double> hoursCol;

    // données démo
    private final ObservableList<Employee> data = FXCollections.observableArrayList(
            new Employee(30, 50, "Caissier",  "Jean"),
            new Employee(20, 60, "Cuisinier", "Théo"),
            new Employee(60, 90, "Serveur",  "François")
    );

    @FXML
    private void initialize() {

        // liaisons colonne -> getter
        nameCol .setCellValueFactory(new PropertyValueFactory<>("name"));
        ageCol  .setCellValueFactory(new PropertyValueFactory<>("age"));
        postCol .setCellValueFactory(new PropertyValueFactory<>("post"));
        hoursCol.setCellValueFactory(new PropertyValueFactory<>("hours"));

        // SortedList pour activer le tri au clic
        FilteredList<Employee> filtered = new FilteredList<>(data, e -> true);
        SortedList<Employee>   sorted   = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(employeeTable.comparatorProperty());
        employeeTable.setItems(sorted);

        // on force un tri mono-colonne
        employeeTable.getSortOrder().addListener(
                (ListChangeListener<TableColumn<Employee, ?>>) c -> {
                    var order = employeeTable.getSortOrder();
                    if (order.size() > 1) {
                        TableColumn<Employee, ?> last = order.get(order.size() - 1);
                        order.clear();
                        order.add(last);
                    }
                });
    }
}

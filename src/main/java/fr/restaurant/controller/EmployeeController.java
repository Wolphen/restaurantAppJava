package fr.restaurant.controller;

import fr.restaurant.model.Dish;
import fr.restaurant.model.Employee;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.SortedList;
import org.w3c.dom.ls.LSOutput;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EmployeeController {

    @FXML
    private TableView<Employee> employeeTable;

    @FXML
    private TableColumn<Employee, String> nameCol;
    @FXML
    private TableColumn<Employee, Double> priceCol;
    @FXML
    private TableColumn<Employee, String> catCol;
    @FXML
    private TableColumn<Employee, String> ingCol;


    private ObservableList<Employee> employees = FXCollections.observableArrayList(
            new Employee(30, 50, "kasier", "Jean"),
            new Employee(20, 60, "cuisinier", "Theo"),
            new Employee(60, 90, "serveur", "François")
    );
    @FXML
    private void initialize() {

        // liaison des colonnes
        nameCol.setCellValueFactory(new PropertyValueFactory<>("age"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("hours"));
        catCol.setCellValueFactory(new PropertyValueFactory<>("post"));
        ingCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        System.out.println("employees: " + employees);
        System.out.println(nameCol);
    }

    public TableView<Employee> getEmployeeTable() {
        return employeeTable;
    }

    public void setEmployeeTable(TableView<Employee> employeeTable) {
        this.employeeTable = employeeTable;
    }
}


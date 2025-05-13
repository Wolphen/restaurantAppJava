package fr.restaurant.controller;

import fr.restaurant.model.Employee;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import java.util.Arrays;

public class EmployeeController {

    // champs fxml
    @FXML private TextField searchField;
    @FXML private TextField nameField;
    @FXML private TextField ageField;
    @FXML private TextField postField;
    @FXML private Button    buttonAdd;

    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee,String>  nameCol;
    @FXML private TableColumn<Employee,Integer> ageCol;
    @FXML private TableColumn<Employee,String>  postCol;
    @FXML private TableColumn<Employee,Double>  hoursCol;
    @FXML private TableColumn<Employee,Void>    deleteCol;
    @FXML private TableColumn<Employee,Void>    addHourCol;

    // la base
    private final SqliteController db = new SqliteController();

    // on charge les salariés depuis sqlite
    private final ObservableList<Employee> data =
            FXCollections.observableArrayList(db.fetchEmployee());

    @FXML
    private void initialize() {

        // liaison colonnes -> getters
        nameCol .setCellValueFactory(new PropertyValueFactory<>("name"));
        ageCol  .setCellValueFactory(new PropertyValueFactory<>("age"));
        postCol .setCellValueFactory(new PropertyValueFactory<>("post"));
        hoursCol.setCellValueFactory(new PropertyValueFactory<>("hours"));

        // filtre + tri
        FilteredList<Employee> filtered = new FilteredList<>(data, e -> true);
        SortedList<Employee>   sorted   = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(employeeTable.comparatorProperty());
        employeeTable.setItems(sorted);

        // 1 seul critère de tri à la fois
        employeeTable.getSortOrder().addListener(
                (ListChangeListener<TableColumn<Employee, ?>>) c -> {
                    var order = employeeTable.getSortOrder();
                    if (order.size() > 1) order.setAll(order.get(order.size()-1));
                });

        // recherche sur le prénom
        searchField.textProperty().addListener((o, oldVal, newVal) -> {
            var q = newVal.trim().toLowerCase();
            filtered.setPredicate(e -> q.isEmpty() ||
                    e.getName().toLowerCase().contains(q));
        });

        // bouton supprimer
        deleteCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("supprimer");
            private final HBox box = new HBox(btn); // conteneur centré

            {
                box.setAlignment(Pos.CENTER);
                btn.setOnAction(evt -> {
                    Employee e = getTableView().getItems().get(getIndex());
                    db.deleteEmployee(e);
                    data.remove(e);
                });
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });

// bouton ajouter des heures
        addHourCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("ajout heures");
            private final HBox box = new HBox(btn); // conteneur centré

            {
                box.setAlignment(Pos.CENTER);
                btn.setOnAction(evt -> openHoursDialog(
                        getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    @FXML
    private void onAdd() {

        if (Arrays.asList(nameField, ageField, postField)
                .stream().anyMatch(tf -> tf.getText().isBlank())) return;

        int age;
        try { age = Integer.parseInt(ageField.getText()); }
        catch (NumberFormatException nope) {
            showError("âge invalide");
            return;
        }

        Employee e = new Employee(age, 0, postField.getText(), nameField.getText());
        db.addEmployee(e);
        data.add(e);

        nameField.clear(); ageField.clear(); postField.clear();
    }

    // petite fenêtre pour ajouter des heures
    private void openHoursDialog(Employee e) {

        var stage = new Stage();
        stage.setTitle("heures pour " + e.getName());

        var vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        var tf   = new TextField();
        tf.setPromptText("nombre d'heures");
        var save = new Button("enregistrer");

        save.setOnAction(a -> {
            try {
                double h = Double.parseDouble(tf.getText());
                e.setHours((float) (e.getHours() + h));
                db.updateEmployeeHours(e);
                employeeTable.refresh();
                stage.close();
            } catch (NumberFormatException nope) {
                showError("entre un nombre, pas du charabia");
            }
        });

        vbox.getChildren().addAll(
                new Label("Ajouter des heures pour " + e.getName()), tf, save);

        stage.setScene(new Scene(vbox, 250, 120));
        stage.show();
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }
}

package fr.restaurant.controller;

import fr.restaurant.model.Employee;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class EmployeeController {

    public TextField searchField;
    public TextField nameField;
    public TextField ageField;
    public TextField postField;
    public Button buttonAdd;
    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, String> nameCol;
    @FXML private TableColumn<Employee, Integer> ageCol;
    @FXML private TableColumn<Employee, String> postCol;
    @FXML private TableColumn<Employee, Double> hoursCol;
    @FXML private TableColumn<Employee, Void> deleteCol;
    @FXML private TableColumn<Employee, Void> addHourCol;

    private final ObservableList<Employee> data = FXCollections.observableArrayList(
            new Employee(30, 50, "Caissier", "Jean"),
            new Employee(20, 60, "Cuisinier", "Théo"),
            new Employee(60, 90, "Serveur", "François")
    );

    @FXML
    private void initialize() {
        // Liaison des colonnes
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        ageCol.setCellValueFactory(new PropertyValueFactory<>("age"));
        postCol.setCellValueFactory(new PropertyValueFactory<>("post"));
        hoursCol.setCellValueFactory(new PropertyValueFactory<>("hours"));

        // FilteredList pilotée par le champ searchField
        FilteredList<Employee> filtered = new FilteredList<>(data, e -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String q = newVal.trim().toLowerCase();
            filtered.setPredicate(e -> q.isEmpty() || e.getName().toLowerCase().contains(q));
        });

        // SortedList pour activer le tri au clic
        SortedList<Employee> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(employeeTable.comparatorProperty());
        employeeTable.setItems(sorted);

        // Mono-colonne : on garde la dernière colonne cliquée
        employeeTable.getSortOrder().addListener(
                (ListChangeListener<TableColumn<Employee, ?>>) c -> {
                    var order = employeeTable.getSortOrder();
                    if (order.size() > 1) {
                        TableColumn<Employee, ?> last = order.get(order.size() - 1);
                        order.clear();
                        order.add(last);
                    }
                });

        // Création du CellFactory pour le bouton supprimer
        deleteCol.setCellFactory(param -> new TableCell<Employee, Void>() {
            private final Button deleteButton = new Button("Supprimer");

            {
                deleteButton.setOnAction(event -> {
                    Employee employee = getTableView().getItems().get(getIndex());
                    data.remove(employee);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });

        // Création du CellFactory pour le bouton ajouter des heures
        addHourCol.setCellFactory(param -> new TableCell<Employee, Void>() {
            private final Button addHourButton = new Button("Ajouter des heures");

            {
                addHourButton.setOnAction(event -> {
                    Employee employee = getTableView().getItems().get(getIndex());

                    // Création de la nouvelle fenêtre
                    Stage stage = new Stage();
                    stage.setTitle("Ajouter des heures");

                    // Contenu de la fenêtre
                    VBox vbox = new VBox(10);
                    vbox.setPadding(new Insets(10));
                    Label label = new Label("Ajoutez des heures pour : " + employee.getName());
                    TextField hoursField = new TextField();
                    hoursField.setPromptText("Entrez le nombre d'heures");
                    Button saveButton = new Button("Enregistrer");

                    saveButton.setOnAction(e -> {
                        try {
                            double hours = Double.parseDouble(hoursField.getText());
                            employee.setHours((float) (employee.getHours() + hours)); // Mise à jour des heures
                            getTableView().refresh(); // Rafraîchissement de la table
                            stage.close(); // Fermeture de la fenêtre
                        } catch (NumberFormatException ex) {
                            new Alert(Alert.AlertType.ERROR, "Veuillez entrer un nombre valide.").showAndWait();
                        }
                    });

                    vbox.getChildren().addAll(label, hoursField, saveButton);

                    // Configuration de la scène et affichage
                    Scene scene = new Scene(vbox, 300, 200);
                    stage.setScene(scene);
                    stage.show();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(addHourButton);
                }
            }
        });
    }

    @FXML
    private void onAdd() {
        if (nameField.getText().isBlank() ||
                ageField.getText().isBlank() ||
                postField.getText().isBlank()) return;

        try {
            int age = Integer.parseInt(ageField.getText());
            data.add(new Employee(age, 0, postField.getText(), nameField.getText()));
            nameField.clear();
            postField.clear();
            ageField.clear();
        } catch (NumberFormatException ex) {
            new Alert(Alert.AlertType.ERROR, "Âge invalide").showAndWait();
        }
    }
}
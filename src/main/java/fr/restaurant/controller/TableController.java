package fr.restaurant.controller;

import fr.restaurant.model.Table;
import fr.restaurant.service.TableService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class TableController {

    @FXML
    private TableView<Table> tableView;
    @FXML
    private TableColumn<Table, Integer> idCol;
    @FXML
    private TableColumn<Table, Integer> sizeCol;
    @FXML
    private TableColumn<Table, Boolean> statusCol;

    @FXML
    private TextField idField;
    @FXML
    private TextField sizeField;

    private final TableService service = new TableService();
    private final ObservableList<Table> data = service.getTables();

    @FXML
    private void initialize() {

        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("size"));
        statusCol.setCellValueFactory(
                cell -> new javafx.beans.property.SimpleBooleanProperty(
                        cell.getValue().isOccupied()).asObject());

        tableView.setItems(data);
    }

    @FXML
    private void onAddTable() {
        try {
            int id = Integer.parseInt(idField.getText());
            int size = Integer.parseInt(sizeField.getText());

            if (data.stream().anyMatch(t -> t.getId() == id)) {
                new Alert(Alert.AlertType.ERROR,
                        "id déjà pris, change de numéro").showAndWait();
                return;
            }
            service.addTable(new Table(id, size, false));

            idField.clear();
            sizeField.clear();
        } catch (NumberFormatException nope) {
            new Alert(Alert.AlertType.ERROR, "chiffres uniquement").showAndWait();
        }
    }

    @FXML
    private void onAssignTable() {
        var t = tableView.getSelectionModel().getSelectedItem();
        if (t != null && service.assignTable(t.getId())) tableView.refresh();
    }

    @FXML
    private void onFreeTable() {
        var t = tableView.getSelectionModel().getSelectedItem();
        if (t != null && service.freeTable(t.getId())) tableView.refresh();
    }
}
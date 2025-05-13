package fr.restaurant.controller;

import fr.restaurant.model.Table;
import fr.restaurant.service.TableService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class TableController {

    @FXML private TableView<Table> tableView;
    @FXML private TableColumn<Table, Integer> idCol;
    @FXML private TableColumn<Table, Integer> sizeCol;
    @FXML private TableColumn<Table, Boolean> statusCol;

    @FXML private TextField idField;
    @FXML private TextField sizeField;

    private final ObservableList<Table> data = FXCollections.observableArrayList();
    private final TableService tableService = new TableService();

    @FXML
    private void initialize() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("size"));
        statusCol.setCellValueFactory(cellData ->
                new javafx.beans.property.ReadOnlyObjectWrapper<>(cellData.getValue().isOccupied()));

        tableView.setItems(data);
    }

    @FXML
    private void onAddTable() {
        try {
            int id = Integer.parseInt(idField.getText());
            int size = Integer.parseInt(sizeField.getText());

            boolean exists = data.stream().anyMatch(t -> t.getId() == id);
            if (exists) {
                new Alert(Alert.AlertType.ERROR, "A table with this ID already exists.").showAndWait();
                return;
            }

            Table newTable = new Table(id, size, false);

            tableService.addTable(newTable);
            data.add(newTable);

            idField.clear();
            sizeField.clear();
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Invalid number").showAndWait();
        }
    }

    @FXML
    private void onAssignTable() {
        Table selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null && tableService.assignTable((int) selected.getId())) {
            tableView.refresh();
        }
    }

    @FXML
    private void onFreeTable() {
        Table selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null && tableService.freeTable((int) selected.getId())) {
            tableView.refresh();
        }
    }
}

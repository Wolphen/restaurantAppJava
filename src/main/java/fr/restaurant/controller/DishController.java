package fr.restaurant.controller;

import fr.restaurant.model.Dish;
import fr.restaurant.service.DishService;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DishController {

    // éléments du fichier FXML
    @FXML private TableView<Dish> dishTable;
    @FXML private TableColumn<Dish,String>  imgCol;
    @FXML private TableColumn<Dish,String>  nameCol;
    @FXML private TableColumn<Dish,Double>  priceCol;
    @FXML private TableColumn<Dish,String>  descCol;
    @FXML private TableColumn<Dish,String>  ingCol;
    @FXML private TableColumn<Dish,Integer> countCol;
    @FXML private TableColumn<Dish,Void>    deleteCol;

    @FXML private TextField nameField;
    @FXML private TextField priceField;
    @FXML private TextField descField;
    @FXML private TextField ingField;
    @FXML private TextField searchField;
    @FXML private Button    chooseImgBtn;
    @FXML private Label     chosenImgLabel;

    // accès centralisé aux données
    private final DishService service = DishService.getInstance();
    private final FilteredList<Dish> filtered =
            new FilteredList<>(service.getObservableList(), d -> true);

    // chemin de l’image choisie (null si aucune)
    private String selectedImageUri = null;

    // image de secours s’il manque une photo
    private final Image placeholder = loadPlaceholder();

    private Image loadPlaceholder() {
        try {
            var url = getClass().getResource("/img/placeholder.png");
            if (url != null)
                return new Image(url.toExternalForm(), 40, 40, true, true);
        } catch (Exception ignored) { }
        // pixel transparent en base 64 : garantit une image valide
        return new Image(
                "data:image/png;base64,"
                        + "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAA"
                        + "AAC0lEQVR42mP8/x8AAwMCAO+/lKsAAAAASUVORK5CYII=",
                40, 40, true, true);
    }

    @FXML
    private void initialize() {

        // liaisons des colonnes avec les getters
        nameCol .setCellValueFactory(new PropertyValueFactory<>("name"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        descCol .setCellValueFactory(new PropertyValueFactory<>("description"));
        ingCol  .setCellValueFactory(new PropertyValueFactory<>("ingredientsString"));
        countCol.setCellValueFactory(new PropertyValueFactory<>("ingredientCount"));

        // colonne miniature 40 px avec gestion du placeholder si jamais on selectionne pas d'img
        imgCol.setCellValueFactory(new PropertyValueFactory<>("imageUri"));
        imgCol.setCellFactory(col -> new TableCell<>() {
            private final ImageView iv = new ImageView();
            {
                iv.setFitWidth(40);
                iv.setFitHeight(40);
                iv.setPreserveRatio(true);
            }
            @Override protected void updateItem(String uri, boolean empty) {
                super.updateItem(uri, empty);
                if (empty) { setGraphic(null); return; }
                try {
                    if (uri == null || uri.isBlank()) throw new IllegalArgumentException();
                    iv.setImage(new Image(uri, 40, 40, true, true, true));
                    if (iv.getImage().isError()) throw new IllegalArgumentException();
                } catch (Exception e) {
                    iv.setImage(placeholder);
                }
                setGraphic(iv);
            }
        });

        // tri et filtre
        SortedList<Dish> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(dishTable.comparatorProperty());
        dishTable.setItems(sorted);

        // on garde un seul critère de tri
        dishTable.getSortOrder().addListener(
                (ListChangeListener<TableColumn<Dish, ?>>) c -> {
                    var order = dishTable.getSortOrder();
                    if (order.size() > 1) order.setAll(order.get(order.size() - 1));
                });

        // filtre instantané sur les ingrédients
        searchField.textProperty().addListener((o, oldVal, newVal) -> {
            var mots = Arrays.stream(newVal.toLowerCase().trim().split("\\s+"))
                    .filter(s -> !s.isBlank())
                    .toList();
            filtered.setPredicate(d -> mots.isEmpty() ||
                    mots.stream().allMatch(m ->
                            d.getIngredients().stream()
                                    .anyMatch(ing -> ing.toLowerCase().contains(m))));
        });

        // bouton supprimer
        deleteCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("supprimer");
            {
                btn.setOnAction(e ->
                        service.deleteDish(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : btn);
            }
        });

        // double-clic = fenêtre détaillée
        dishTable.setRowFactory(tv -> {
            var row = new TableRow<Dish>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty())
                    showDetails(row.getItem());
            });
            return row;
        });
    }
    // la selection de l'image
    @FXML
    private void chooseImage() {

        Window win = dishTable.getScene().getWindow();
        var fc = new FileChooser();
        fc.setTitle("Choisir une image");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));

        File file = fc.showOpenDialog(win);
        if (file == null) return;

        try {
            Path destDir  = Path.of("images");
            Files.createDirectories(destDir);
            Path destFile = destDir.resolve(file.getName());
            Files.copy(file.toPath(), destFile,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            selectedImageUri = destFile.toUri().toString();
            chosenImgLabel.setText(file.getName());
        } catch (IOException ex) {
            showError("Impossible de copier l'image.");
            selectedImageUri = null;
        }
    }
    // ajouter un dish
    @FXML
    private void onAdd() {

        if (nameField.getText().isBlank() ||
                priceField.getText().isBlank()||
                descField.getText().isBlank() ||
                ingField.getText().isBlank()) {
            showError("Les champs nom, prix, description et ingrédients sont obligatoires.");
            return;
        }

        double price;
        try { price = Double.parseDouble(priceField.getText()); }
        catch (NumberFormatException e) {
            showError("Le prix doit être un nombre.");
            return;
        }

        List<String> ings = Arrays.stream(ingField.getText().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        String uri = (selectedImageUri != null)
                ? selectedImageUri
                : getClass().getResource("/img/placeholder.png").toExternalForm();

        service.addDish(new Dish(
                nameField.getText(), price, descField.getText(), uri, ings));

        nameField.clear(); priceField.clear();
        descField.clear(); ingField.clear();
        chosenImgLabel.setText("");
        selectedImageUri = null;
    }
    // le fameux modal du double clic
    private void showDetails(Dish d) {

        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle("Détails du plat");
        dlg.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        ImageView iv = new ImageView();
        iv.setFitWidth(200); iv.setFitHeight(200); iv.setPreserveRatio(true);
        try {
            iv.setImage(new Image(d.getImageUri(), 200, 200, true, true, true));
            if (iv.getImage().isError()) throw new IllegalArgumentException();
        } catch (Exception e) {
            iv.setImage(placeholder);
        }

        var name  = new Label(d.getName());
        name.setStyle("-fx-font-size:18; -fx-font-weight:bold;");
        var price = new Label(String.format("%.2f €", d.getPrice()));
        var desc  = new Label(d.getDescription());
        desc.setWrapText(true);

        var list = new ListView<String>();
        list.getItems().addAll(d.getIngredients());
        list.setMaxHeight(100);

        var box = new VBox(10, iv, name, price, desc,
                new Label("Ingrédients :"), list);
        box.setStyle("-fx-padding:15;");

        dlg.getDialogPane().setContent(box);
        dlg.showAndWait();
    }
    // utilitaire si jamais y'a une error
    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }
}

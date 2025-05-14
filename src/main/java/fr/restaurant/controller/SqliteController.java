package fr.restaurant.controller;

import fr.restaurant.model.Dish;
import fr.restaurant.model.Employee;
import fr.restaurant.model.Order;
import fr.restaurant.model.Table;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.util.Arrays;
import java.util.List;

public class SqliteController {

    // La base vit ici. Si sa bouge, sa suit.
    private static final String URI = "jdbc:sqlite:sample.db";

    // ------------------------------------------------------------------
    // Boot de la base : on crée tout si ça n’existe pas
    // ------------------------------------------------------------------
    public void creationTable() {

        final String sqlDish = """
            create table if not exists dish (
                id          integer primary key autoincrement,
                name        text    not null,
                price       real    not null,
                description text,
                ingredients text,
                imageUri    text
            );""";

        final String sqlEmployee = """
            create table if not exists employee (
                id     integer primary key autoincrement,
                name   text    not null,
                post   text    not null,
                age    integer not null,
                hours  real
            );""";

        final String sqlOrders = """
    CREATE TABLE if not exists orders (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      tablee INTEGER NOT NULL,
      globalPrice INTEGER NOT NULL,
      status TEXT NOT NULL,
      dateAdded DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY(tablee) REFERENCES rest_table(id)
    );
    """;

        final String sqlOrderDishes = """
            create table if not exists order_dishes (
                order_id integer not null,
                dish_id  integer not null,
                foreign key(order_id) references orders(id),
                foreign key(dish_id) references dish(id)
            );
            """;

        final String sqlRestTable = """
            create table if not exists rest_table (
                id       integer primary key,
                size     integer not null,
                occupied boolean not null
            );
            """;

        try (Connection c = DriverManager.getConnection(URI);
             Statement st = c.createStatement()) {

            st.setQueryTimeout(30); // 30 s, après on rage-quit
            st.executeUpdate(sqlDish);
            st.executeUpdate(sqlEmployee);
            st.executeUpdate(sqlOrders);
            st.executeUpdate(sqlOrderDishes);
            st.executeUpdate(sqlRestTable);

            System.out.println("tables prêtes, chef !");

        } catch (SQLException boom) {
            boom.printStackTrace(System.err);
        }
    }

    // ------------------------------------------------------------------
    // Section plats (select / insert / delete)
    // ------------------------------------------------------------------
    public ObservableList<Dish> fetchDish() {
        String sql = "select * from dish";
        var list = FXCollections.<Dish>observableArrayList();
        try (Connection c = DriverManager.getConnection(URI);
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String raw = rs.getString("ingredients");
                List<String> ing = (raw == null || raw.isBlank())
                        ? List.of()
                        : Arrays.stream(raw.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();
                list.add(new Dish(
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getString("description"),
                        rs.getString("imageUri"),
                        ing));
            }
        } catch (SQLException boom) {
            boom.printStackTrace(System.err);
        }
        return list;
    }

    public void addDish(Dish d) {
        String sql = """
            insert into dish (name, price, description, ingredients, imageUri)
            values (?,?,?,?,?)
            """;
        try (Connection c = DriverManager.getConnection(URI);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, d.getName());
            ps.setDouble(2, d.getPrice());
            ps.setString(3, d.getDescription());
            ps.setString(4, String.join(",", d.getIngredients()));
            ps.setString(5, d.getImageUri());
            ps.executeUpdate();
        } catch (SQLException boom) {
            boom.printStackTrace(System.err);
        }
    }

    public void deleteDish(Dish d) {
        String sql = "delete from dish where name = ?";
        try (Connection c = DriverManager.getConnection(URI);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, d.getName());
            ps.executeUpdate();
        } catch (SQLException boom) {
            boom.printStackTrace(System.err);
        }
    }

    // ------------------------------------------------------------------
    // Section employés (un copier-coller ? jamais !)
    // ------------------------------------------------------------------
    public ObservableList<Employee> fetchEmployee() {
        String sql = "select * from employee";
        var list = FXCollections.<Employee>observableArrayList();
        try (Connection c = DriverManager.getConnection(URI);
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Employee(
                        rs.getInt("age"),
                        rs.getFloat("hours"),
                        rs.getString("post"),
                        rs.getString("name")));
            }
        } catch (SQLException boom) {
            boom.printStackTrace(System.err);
        }
        return list;
    }

    public void addEmployee(Employee e) {
        String sql = """
            insert into employee (name, post, age, hours)
            values (?,?,?,?)
            """;
        try (Connection c = DriverManager.getConnection(URI);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, e.getName());
            ps.setString(2, e.getPost());
            ps.setInt(3, e.getAge());
            ps.setDouble(4, e.getHours());
            ps.executeUpdate();
        } catch (SQLException boom) {
            boom.printStackTrace(System.err);
        }
    }

    public void deleteEmployee(Employee e) {
        String sql = "delete from employee where name = ?";
        try (Connection c = DriverManager.getConnection(URI);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, e.getName());
            ps.executeUpdate();
        } catch (SQLException boom) {
            boom.printStackTrace(System.err);
        }
    }

    public void updateEmployeeHours(Employee e) {
        String sql = "update employee set hours = ? where name = ?";
        try (Connection c = DriverManager.getConnection(URI);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDouble(1, e.getHours());
            ps.setString(2, e.getName());
            ps.executeUpdate();
        } catch (SQLException boom) {
            boom.printStackTrace(System.err);
        }
    }

    // ------------------------------------------------------------------
    // CRUD rest_table (id, size, occupied)
    // ------------------------------------------------------------------
    public ObservableList<Table> fetchTables() {
        String sql = "select * from rest_table";
        var list = FXCollections.<Table>observableArrayList();
        try (Connection c = DriverManager.getConnection(URI);
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Table(
                        rs.getInt("id"),
                        rs.getInt("size"),
                        rs.getBoolean("occupied")));
            }
        } catch (SQLException boom) {
            boom.printStackTrace(System.err);
        }
        return list;
    }

    public void addTable(Table t) {
        String sql = "insert into rest_table (id, size, occupied) values (?,?,?)";
        try (Connection c = DriverManager.getConnection(URI);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, t.getId());
            ps.setInt(2, t.getSize());
            ps.setBoolean(3, t.isOccupied());
            ps.executeUpdate();
        } catch (SQLException boom) {
            boom.printStackTrace(System.err);
        }
    }

    public void updateTableStatus(int id, boolean occupied) {
        String sql = "update rest_table set occupied = ? where id = ?";
        try (Connection c = DriverManager.getConnection(URI);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBoolean(1, occupied);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException boom) {
            boom.printStackTrace(System.err);
        }
    }

    // ------------------------------------------------------------------
    // Order CRUD
    // ------------------------------------------------------------------
    public ObservableList<Order> fetchOrder() {
        String sqlOrders = "select * from orders";
        String sqlDishes = """
            select d.id, d.name, d.price, d.description, d.ingredients, d.imageUri
            from dish d
            join order_dishes od on d.id = od.dish_id
            where od.order_id = ?
            """;
        var list = FXCollections.<Order>observableArrayList();
        try (Connection c = DriverManager.getConnection(URI);
             Statement st = c.createStatement();
             ResultSet rsOrders = st.executeQuery(sqlOrders)) {
            while (rsOrders.next()) {
                int orderId = rsOrders.getInt("id");
                var dishes = FXCollections.<Dish>observableArrayList();
                try (PreparedStatement psDishes = c.prepareStatement(sqlDishes)) {
                    psDishes.setInt(1, orderId);
                    try (ResultSet rsDishes = psDishes.executeQuery()) {
                        while (rsDishes.next()) {
                            String rawIngredients = rsDishes.getString("ingredients");
                            List<String> ingredients = (rawIngredients == null || rawIngredients.isBlank())
                                    ? List.of()
                                    : Arrays.stream(rawIngredients.split(","))
                                    .map(String::trim)
                                    .filter(s -> !s.isEmpty())
                                    .toList();
                            dishes.add(new Dish(
                                    rsDishes.getString("name"),
                                    rsDishes.getDouble("price"),
                                    rsDishes.getString("description"),
                                    rsDishes.getString("imageUri"),
                                    ingredients
                            ));
                        }
                    }
                }
                // Utilisation de "tablee" tel que défini dans le schéma
                list.add(new Order(
                        rsOrders.getInt("tablee"),
                        rsOrders.getInt("globalPrice"),
                        rsOrders.getString("status"),
                        dishes
                ));
            }
        } catch (SQLException boom) {
            boom.printStackTrace(System.err);
        }
        return list;
    }

    // Ajout de la méthode addOrder mise à jour
    public void addOrder(Order o, List<Dish> dishes) {
        System.out.println("Tentative d'insertion de la commande...");
        String sqlOrder = """
        insert into orders (tablee, globalPrice, status)
        values (?,?,?)
        """;
        String sqlOrderDishes = """
        insert into order_dishes (order_id, dish_id)
        values (?,?)
        """;
        String sqlGetDishId = "select id from dish where name = ?";
        try (Connection c = DriverManager.getConnection(URI);
             PreparedStatement psOrder = c.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS)) {
            psOrder.setInt(1, o.getTable());
            psOrder.setInt(2, o.getGlobalPrice());
            psOrder.setString(3, "en cours"); // statut par défaut
            int rowsInserted = psOrder.executeUpdate();
            System.out.println("Lignes insérées dans orders : " + rowsInserted);
            try (ResultSet generatedKeys = psOrder.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int orderId = generatedKeys.getInt(1);
                    System.out.println("Commande générée avec id : " + orderId);
                    // Affecte l'id dans l'objet Order
                    o.setId(orderId);
                    try (PreparedStatement psOrderDishes = c.prepareStatement(sqlOrderDishes);
                         PreparedStatement psGetDishId = c.prepareStatement(sqlGetDishId)) {
                        for (Dish dish : dishes) {
                            psGetDishId.setString(1, dish.getName());
                            try (ResultSet rs = psGetDishId.executeQuery()) {
                                if (rs.next()) {
                                    int dishId = rs.getInt("id");
                                    psOrderDishes.setInt(1, orderId);
                                    psOrderDishes.setInt(2, dishId);
                                    psOrderDishes.addBatch();
                                }
                            }
                        }
                        int[] rowsDish = psOrderDishes.executeBatch();
                        System.out.println("Nombre de liaisons insérées dans order_dishes : " + rowsDish.length);
                    }
                } else {
                    System.out.println("Aucun id généré pour la commande.");
                }
            }
        } catch (SQLException boom) {
            boom.printStackTrace(System.err);
        }
    }

    // Mise à jour de completeOrder avec des logs
    public void completeOrder(int orderId) {
        String sql = "update orders set status = 'completed' where id = ?";
        try (Connection c = DriverManager.getConnection(URI);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            int rowsAffected = ps.executeUpdate();
            System.out.println("Commande " + orderId + " mise à jour en 'completed' (lignes affectées : " + rowsAffected + ")");
        } catch (SQLException boom) {
            boom.printStackTrace(System.err);
        }
    }

    // Mise à jour de cancelOrder avec des logs
    public void cancelOrder(int orderId) {
        String sql = "update orders set status = 'cancel' where id = ?";
        try (Connection c = DriverManager.getConnection(URI);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            int rowsAffected = ps.executeUpdate();
            System.out.println("Commande " + orderId + " annulée (lignes affectées : " + rowsAffected + ")");
        } catch (SQLException boom) {
            boom.printStackTrace(System.err);
        }
    }
}
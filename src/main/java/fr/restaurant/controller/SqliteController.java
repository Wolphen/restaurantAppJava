package fr.restaurant.controller;

import fr.restaurant.model.Dish;
import fr.restaurant.model.Employee;
import fr.restaurant.model.Table;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.Arrays;
import java.util.List;

public class SqliteController {

    // la base vit ici. si sa bouge, sa suit
    private static final String URI = "jdbc:sqlite:sample.db";

    // ------------------------------------------------------------------
    // boot de la base : on crée tout si ça n’existe pas
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
            create table if not exists orders (
                id      integer primary key autoincrement,
                status  text    not null,
                tablee  integer not null,
                dish_id integer,
                foreign key(dish_id) references dish(id)
            );""";

        final String sqlRestTable = """
    create table if not exists rest_table (
        id       integer primary key,
        size     integer not null,
        occupied boolean not null
    );""";

        try (Connection c = DriverManager.getConnection(URI);
             Statement  st = c.createStatement()) {

            st.setQueryTimeout(30);           // 30 s, après on rage-quit
            st.executeUpdate(sqlDish);
            st.executeUpdate(sqlEmployee);
            st.executeUpdate(sqlOrders);
            st.executeUpdate(sqlRestTable);

            System.out.println("tables prêtes, chef !");

        } catch (SQLException boom) {
            boom.printStackTrace(System.err);
        }
    }

    // ------------------------------------------------------------------
    // section plats (select / insert / delete)
    // ------------------------------------------------------------------

    public ObservableList<Dish> fetchDish() {

        String sql = "select * from dish";
        var list = FXCollections.<Dish>observableArrayList();

        try (Connection c = DriverManager.getConnection(URI);
             Statement  st = c.createStatement();
             ResultSet  rs = st.executeQuery(sql)) {

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
            values (?,?,?,?,?)""";

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
    // section employés (un copier-coller ? jamais !)
    // ------------------------------------------------------------------

    public ObservableList<Employee> fetchEmployee() {

        String sql = "select * from employee";
        var list = FXCollections.<Employee>observableArrayList();

        try (Connection c = DriverManager.getConnection(URI);
             Statement  st = c.createStatement();
             ResultSet  rs = st.executeQuery(sql)) {

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
            values (?,?,?,?)""";

        try (Connection c = DriverManager.getConnection(URI);
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, e.getName());
            ps.setString(2, e.getPost());
            ps.setInt   (3, e.getAge());
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
            ps.setString(2,  e.getName());
            ps.executeUpdate();

        } catch (SQLException boom) {
            boom.printStackTrace(System.err);
        }
    }
// ------------------------------------------------------------------
// 4) CRUD rest_table  (id, size, occupied)
// ------------------------------------------------------------------

    public ObservableList<Table> fetchTables() {

        String sql = "select * from rest_table";
        var list = FXCollections.<Table>observableArrayList();

        try (Connection c = DriverManager.getConnection(URI);
             Statement  st = c.createStatement();
             ResultSet  rs = st.executeQuery(sql)) {

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

            ps.setInt   (1, t.getId());
            ps.setInt   (2, t.getSize());
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
            ps.setInt    (2, id);
            ps.executeUpdate();

        } catch (SQLException boom) {
            boom.printStackTrace(System.err);
        }
    }
}

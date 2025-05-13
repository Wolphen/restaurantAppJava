package fr.restaurant.controller;

import fr.restaurant.model.Dish;
import fr.restaurant.model.Employee;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqliteController {

    private final String Uri = "jdbc:sqlite:sample.db";

    public SqliteController() {

    }


    // connections bdd sqlite
    public void creationTable() {
        try (
            Connection connection = DriverManager.getConnection(Uri);
            Statement statement = connection.createStatement();)
        {
            statement.setQueryTimeout(30);

            // table des plats
            statement.executeUpdate("create table if not exists dish (id integer primary key AUTOINCREMENT, name string not null, price double not null, category string, ingredients string, uri string);");

            // table des salariés
            statement.executeUpdate("create table if not exists employee (id integer primary key AUTOINCREMENT, name string not null, post string not null, age int not null, hours double);");

            // table des commandes (status: annulée, en attente, préparée | le numéro de la table et l'id du plat) (kiwi)
            statement.executeUpdate("create table if not exists orders (id integer primary key AUTOINCREMENT, status string not null, tablee int not null, dish_id int, foreign key(dish_id) references dish(id));");

            System.out.println("Les tables ont été crées");
        }
        catch(SQLException e)
        {
            e.printStackTrace(System.err);
        }
    }

    public ObservableList<Dish> fetchDish(){
        try (
                Connection connection = DriverManager.getConnection(Uri);
                Statement statement = connection.createStatement();)
        {
            statement.setQueryTimeout(30);
            ResultSet rs = statement.executeQuery("select * from dish");
            ObservableList<Dish> data = FXCollections.observableArrayList();
            while(rs.next())
            {
                List<String> ingredients = new ArrayList<>();
                Dish dish = new Dish(rs.getString("name"), rs.getDouble("price"), "", ingredients);
                data.add(dish);
            }
            System.out.println(data);
            return data;

        }
        catch(SQLException e)
        {
            e.printStackTrace(System.err);
            return null;
        }
    }

    // enregistrer des infos sur les plats
    public void addDish(Dish dish) {
        try (
                Connection connection = DriverManager.getConnection(Uri);
                Statement statement = connection.createStatement();)
        {
            statement.setQueryTimeout(30);
            String sqlInsert = "insert into dish (name, price, category, ingredients) VALUES (?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sqlInsert);
            preparedStatement.setString(1, dish.getName());
            preparedStatement.setDouble(2, dish.getPrice());
            preparedStatement.setString(3, dish.getCategory());
            preparedStatement.setString(4, dish.getIngredients().toString());
            preparedStatement.executeUpdate();
        }
        catch(SQLException e)
        {
            e.printStackTrace(System.err);
        }
    }


    public ObservableList<Employee> fetchEmployee(){
        try (
                Connection connection = DriverManager.getConnection(Uri);
                Statement statement = connection.createStatement();)
        {
            statement.setQueryTimeout(30);
            ResultSet rs = statement.executeQuery("select * from employee");
            ObservableList<Employee> data = FXCollections.observableArrayList();
            while(rs.next())
            {
                List<String> ingredients = new ArrayList<>();
                Employee employee = new Employee(rs.getInt("age"), rs.getFloat("hours"), rs.getString("post"), rs.getString("name"));
                data.add(employee);
            }
            System.out.println(data);
            return data;

        }
        catch(SQLException e)
        {
            e.printStackTrace(System.err);
            return null;
        }
    }


    public void addEmployee(Employee employee) {
        try (
                Connection connection = DriverManager.getConnection(Uri);
                Statement statement = connection.createStatement();)
        {
            statement.setQueryTimeout(30);
            String sqlInsert = "insert into employee (name, post, age, hours) VALUES (?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sqlInsert);
            preparedStatement.setString(1, employee.getName());
            preparedStatement.setString(2, employee.getPost());
            preparedStatement.setInt(3, employee.getAge());
            preparedStatement.setDouble(3, employee.getHours());
            preparedStatement.executeUpdate();
        }
        catch(SQLException e)
        {
            e.printStackTrace(System.err);
        }
    }
    //
    public void deleteEmployee(Employee employee) {
        try (
                Connection connection = DriverManager.getConnection(Uri);
                Statement statement = connection.createStatement();)
        {
            statement.setQueryTimeout(30);
            ResultSet rs = statement.executeQuery("delete from employee where name = '" + employee.getName() + "'");

        }
        catch(SQLException e)
        {
            e.printStackTrace(System.err);
        }
    }


}

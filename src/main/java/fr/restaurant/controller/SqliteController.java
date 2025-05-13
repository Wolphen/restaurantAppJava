package fr.restaurant.controller;

import fr.restaurant.model.Dish;

import java.sql.*;

public class SqliteController {

    private final String Uri = "jdbc:sqlite:sample.db";

    // connections bdd sqlite
    public void creationTable() {
        try (
            Connection connection = DriverManager.getConnection(Uri);
            Statement statement = connection.createStatement();)
        {
            statement.setQueryTimeout(30);

            // table des plats
            statement.executeUpdate("create table if not exists dish (id integer primary key AUTOINCREMENT, name string not null, price double not null, category string, ingredients string);");

            // table des salariés
            statement.executeUpdate("create table if not exists employee (id integer primary key AUTOINCREMENT, name string not null, post string not null, double hours);");

            // table des commandes (status: annulée, en attente, préparée | le numéro de la table et l'id du plat) (kiwi)
            statement.executeUpdate("create table if not exists orders (id integer primary key AUTOINCREMENT, status string not null, tablee int not null, dish_id int, foreign key(dish_id) references dish(id));");

//            statement.executeUpdate("insert into dish values('KiwiCrème', 12.0, 'dessert', 'kiwi, crème')");
//            statement.executeUpdate("insert into dish values('KiwiFraise', 23.9, 'plat', 'kiwi, fraise')");
//
//            statement.executeUpdate("insert into orders values('attente', 'table1', 1)");
//            statement.executeUpdate("insert into orders values('cancel', 'table2', 2)");

            System.out.println("Les tables ont été crées");
        }
        catch(SQLException e)
        {
            e.printStackTrace(System.err);
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
//            preparedStatement.setL(4, dish.getIngredients());
            preparedStatement.executeUpdate();
        }
        catch(SQLException e)
        {
            e.printStackTrace(System.err);
        }
    }

}

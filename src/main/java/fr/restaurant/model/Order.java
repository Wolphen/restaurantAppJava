// java
package fr.restaurant.model;

import java.util.List;

public class Order {
    private int id; // identifiant généré par la BDD
    private int table;
    private int globalPrice;
    private String status;
    private List<Dish> dishes;

    public Order(int table, int globalPrice, String status, List<Dish> dishes) {
        this.table = table;
        this.globalPrice = globalPrice;
        this.status = status;
        this.dishes = dishes;
    }

    // Getter et Setter pour id
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTable() {
        return table;
    }

    // Méthode ajoutée pour compatibilité avec TableService
    public int getTableId() {
        return table;
    }

    public int getGlobalPrice() {
        return globalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Dish> getDishes() {
        return dishes;
    }

    public void setDishes(List<Dish> dishes) {
        this.dishes = dishes;
    }
}
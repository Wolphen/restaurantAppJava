package fr.restaurant.model;

import java.util.List;

public class Order {

    private int id;               // identifiant auto-généré en base
    private int table;            // numéro de la table
    private int globalPrice;      // prix total en euros
    private String status;        // "en cours", "préparée", etc.
    private List<Dish> dishes;    // liste des plats commandés

    /**
     * Constructeur complet utilisé lors du chargement depuis la BDD.
     * @param id           identifiant auto-incrémenté
     * @param table        numéro de la table
     * @param globalPrice  prix total de la commande
     * @param status       statut de la commande
     * @param dishes       liste des plats
     */
    public Order(int id, int table, int globalPrice, String status, List<Dish> dishes) {
        this.id = id;
        this.table = table;
        this.globalPrice = globalPrice;
        this.status = status;
        this.dishes = dishes;
    }

    /**
     * Constructeur pour créer une nouvelle commande avant insertion en BDD.
     * L'id sera renseigné après l'INSERT grâce à getGeneratedKeys().
     */
    public Order(int table, int globalPrice, String status, List<Dish> dishes) {
        this(0, table, globalPrice, status, dishes);
    }

    // getters et setter pour l'id

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // getters simples

    public int getTable() {
        return table;
    }

    public int getGlobalPrice() {
        return globalPrice;
    }

    public String getStatus() {
        return status;
    }

    public List<Dish> getDishes() {
        return dishes;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", table=" + table +
                ", total=" + globalPrice +
                "€, status='" + status + '\'' +
                ", items=" + dishes.size() +
                '}';
    }

    public void setStatus(String completed) {
    }
}

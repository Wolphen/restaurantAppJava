package fr.restaurant.model;

import java.util.List;
import java.util.StringJoiner;

public class Dish {

    private String name;
    private double price;
    private String category;
    private List<String> ingredients;

    public Dish(String name, double price, String category, List<String> ingredients) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.ingredients = ingredients;
    }

    /* getters / setters */

    public int getIngredientCount() {      // utilisé pour trier la colonne #
        return ingredients.size();
    }

    public String  getName()        { return name; }
    public void    setName(String n){ this.name = n; }

    public double  getPrice()                  { return price; }
    public void    setPrice(double p)          { this.price = p; }

    public String  getCategory()              { return category; }
    public void    setCategory(String c)      { this.category = c; }

    public List<String> getIngredients()      { return ingredients; }
    public void        setIngredients(List<String> ing){ this.ingredients = ing; }

    public String getIngredientsString() {
        return String.join(", ", ingredients);
    }
}

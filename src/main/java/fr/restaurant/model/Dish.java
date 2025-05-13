package fr.restaurant.model;

import java.util.List;

public class Dish {

    private String name;
    private double price;
    private String description;        // ← nouveau
    private String imageUri;           // ← nouveau : lien ou chemin
    private List<String> ingredients;

    public Dish(String name, double price,
                String description, String imageUri,
                List<String> ingredients) {
        this.name        = name;
        this.price       = price;
        this.description = description;
        this.imageUri    = imageUri;
        this.ingredients = ingredients;
    }

    /* getters – utilisés par TableView */
    public String  getName()         { return name; }
    public double  getPrice()        { return price; }
    public String  getDescription()  { return description; }
    public String  getImageUri()     { return imageUri; }
    public List<String> getIngredients()   { return ingredients; }
    public String getIngredientsString()   { return String.join(", ", ingredients); }
    public int getIngredientCount()        { return ingredients.size(); }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }
}

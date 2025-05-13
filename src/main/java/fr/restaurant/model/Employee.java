package fr.restaurant.model;

public class Employee {
   private int age;
   private String name;
   private String post;
   private float  hours;

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public float getHours() {
        return hours;
    }

    public void setHours(float hours) {
        this.hours = hours;
    }

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Employee(int age, float hours, String post, String name) {
        this.age = age;
        this.hours = hours;
        this.post = post;
        this.name = name;
    }
}

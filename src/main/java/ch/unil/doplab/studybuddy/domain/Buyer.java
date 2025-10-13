package ch.unil.doplab.studybuddy.domain;

import java.util.ArrayList;
import java.util.List;

public class Buyer extends User {
    private double budget;
    private List<String> propertyTypesOfInterest;

    public Buyer(String firstName, String lastName, String email, String username, String password, double budget) {
        super(firstName, lastName, email, username, password);
        this.budget = budget;
        this.propertyTypesOfInterest = new ArrayList<>();
    }

    public double getBudget() { return budget; }
    public void setBudget(double budget) { this.budget = budget; }
    public List<String> getPropertyTypesOfInterest() { return propertyTypesOfInterest; }

    public Offer placeOffer(Property property, double amount) {
        if (property == null) throw new IllegalArgumentException("property is required");
        if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
        return new Offer(property.getPropertyId(), this.getUserID(), amount);
    }

    @Override
    public String getRole() {
        return "Buyer";
    }
}

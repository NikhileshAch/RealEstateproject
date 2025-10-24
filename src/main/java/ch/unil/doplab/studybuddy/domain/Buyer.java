package ch.unil.doplab.studybuddy.domain;

import java.util.ArrayList;
import java.util.List;

public class Buyer extends User {
    private double budget;
    private List<String> propertyTypesOfInterest;
    private List<String> documents;

    public Buyer(String firstName, String lastName, String email, String username, String password, double budget) {
        super(firstName, lastName, email, username, password);
        this.budget = budget;
        this.propertyTypesOfInterest = new ArrayList<>();
        this.documents = new ArrayList<>();

    }

    public double getBudget() { return budget; }
    public void setBudget(double budget) { this.budget = budget; }
    public List<String> getPropertyTypesOfInterest() { return propertyTypesOfInterest; }

    public Offer placeOffer(Property property, double amount) {
        if (property == null) throw new IllegalArgumentException("Property is required");
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        return new Offer(property.getPropertyId(), this.getUserID(), amount);
    }
    public void withdrawOffer(Offer offer) {
        if (offer != null && offer.getBuyerId().equals(this.getUserID())) {
            offer.withdraw();
        } else {
            throw new IllegalArgumentException("Cannot withdraw this offer.");
        }
    }

    public List<String> getDocuments() {
        return this.documents;
    }

    public void addDocument(String documentName) {
        if (documentName != null && !documentName.isEmpty() && !this.documents.contains(documentName)) {
            this.documents.add(documentName);
        }
    }
    public void removeDocument(String documentName) {
        this.documents.remove(documentName);
    }

    public List<String> getPropertyTypeOfInterest(){
        return this.propertyTypesOfInterest;
    }
    public void addPropertyTypeOfInterest(String propertyType) {
        if (propertyType != null && !propertyType.isEmpty() && !this.propertyTypesOfInterest.contains(propertyType)) {
            this.propertyTypesOfInterest.add(propertyType);
        }
    }
    public void removePropertyTypeOfInterest(String propertyType) {
        this.propertyTypesOfInterest.remove(propertyType);
    }

    public Review submitReview(String agentId, int rating, String comment) {
        if (agentId == null || agentId.isEmpty()) {
            throw new IllegalArgumentException("Agent ID cannot be null or empty.");
        }
        return new Review("agent", agentId, rating, comment);
    }
    public Viewing requestViewing(Property property, LocalDateTime timeSlot) {
        if (property == null || timeSlot == null) {
            throw new IllegalArgumentException("Property and time slot are required");
        }
        return new Viewing(timeSlot, property, this);
    }


    @Override
    public String getRole() {
        return "Buyer";
    }
}

package ch.unil.doplab.studybuddy.domain;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Viewing {
    public enum Status {BOOKED, CONFIRMED, CANCELLED, COMPLETED, RESCHEDULED}

    private final UUID viewingID;
    private final UUID propertyID;
    private final UUID listingID;
    private final UUID userID;
    private final UUID agentID;
    private final String location;
    private LocalDateTime timeSlot;
    private Status status;
    private String feedback;
    private final LocalDateTime createdAt;

    public Viewing(UUID propertyID, UUID listingID, UUID userID, UUID agentID, String location, LocalDateTime timeSlot) {
        if (propertyID == null) throw new IllegalArgumentException("propertyID is required");
        if (listingID == null) throw new IllegalArgumentException("listingID is required");
        if (userID == null) throw new IllegalArgumentException("userID is required");
        if (agentID == null) throw new IllegalArgumentException("agentID is required");
        if (location == null) throw new IllegalArgumentException("location is required");
        if (timeSlot == null) throw new IllegalArgumentException("timeSlot is required");

        this.viewingID = UUID.randomUUID();
        this.propertyID = propertyID;
        this.listingID = listingID;
        this.userID = userID;
        this.agentID = agentID;
        this.location = location;
        this.timeSlot = timeSlot;&
        this.status = Status.BOOKED;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getViewingID() {
        return viewingID;
    }

    public UUID getPropertyID() {
        return propertyID;
    }

    public UUID getListingID() {
        return listingID;
    }

    public UUID getUserID() {
        return userID;
    }

    public UUID getAgentID() {
        return agentID;
    }

    public String getLocation() {
        return location;
    }

    public LocalDateTime getTimeSlot() {
        return timeSlot;
    }

    public Status getStatus() {
        return status;
    }

    public String getFeedback() {
        return feedback;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Viewing viewing)) return false;
        return Objects.equals(viewingID, viewing.viewingID);
    }

    @Override
    public int hashcode() {
        return Objects.hash(viewingID);
    }

    @Override
    public String toString() {
        return String.format("Viewing{id=%s, propertyID=%s, userID=%s, time=%s, status=%s}",
                viewingID, propertyID, userID, timeSlot, status);
    }
}


//yo





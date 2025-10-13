package ch.unil.doplab.studybuddy.domain;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Offer {
    public enum Status { PENDING, ACCEPTED, REJECTED, WITHDRAWN }

    private final UUID offerId;
    private final UUID propertyId;
    private final UUID buyerId;
    private final double amount;
    private final LocalDateTime createdAt;
    private Status status;

    public Offer(UUID propertyId, UUID buyerId, double amount) {
        if (propertyId == null) throw new IllegalArgumentException("propertyId is required");
        if (buyerId == null) throw new IllegalArgumentException("buyerId is required");
        if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
        this.offerId = UUID.randomUUID();
        this.propertyId = propertyId;
        this.buyerId = buyerId;
        this.amount = amount;
        this.createdAt = LocalDateTime.now();
        this.status = Status.PENDING;
    }

    public UUID getOfferId() { return offerId; }
    public UUID getPropertyId() { return propertyId; }
    public UUID getBuyerId() { return buyerId; }
    public double getAmount() { return amount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Offer offer)) return false;
        return Objects.equals(offerId, offer.offerId);
    }

    @Override
    public int hashCode() { return Objects.hash(offerId); }
}

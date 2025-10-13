package ch.unil.doplab.studybuddy.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Seller extends User {
    private final List<Property> ownedProperties;
    private final List<Offer> receivedOffers;

    public Seller(String firstName, String lastName, String email, String username, String password) {
        super(firstName, lastName, email, username, password);
        this.ownedProperties = new ArrayList<>();
        this.receivedOffers = new ArrayList<>();
    }

    public List<Property> getOwnedProperties() { return Collections.unmodifiableList(ownedProperties); }
    public List<Offer> getReceivedOffers() { return Collections.unmodifiableList(receivedOffers); }

    public void publishProperty(Property property) {
        if (property == null) throw new IllegalArgumentException("property is required");
        ownedProperties.add(property);
        property.publish();
    }

    public void respondToOffer(Offer offer, boolean accept) {
        if (offer == null) throw new IllegalArgumentException("offer is required");
        if (!receivedOffers.contains(offer)) {
            receivedOffers.add(offer);
        }
        offer.setStatus(accept ? Offer.Status.ACCEPTED : Offer.Status.REJECTED);
    }

    @Override
    public String getRole() {
        return "Seller";
    }
}

package ch.unil.doplab.studybuddy.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class User {

    public enum Role {
        BUYER,
        SELLER, 
        RENTER,
        LANDLORD,
        AGENT
    }

    public enum DocumentType {
        PASSPORT,
        ID_CARD,
        DRIVER_LICENSE,
        RESIDENCE_PERMIT,
        OTHER
    }

    public enum MessageDirection {
        SENT,
        RECEIVED
    }

    // Core User Data
    private UUID userID;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    
    // Real Estate Specific Data
    private final List<String> preferredLocations;
    private final List<String> savedProperties; // List of property IDs
    private final List<Message> messages;
    private final List<IdentityDocument> identityDocuments;
    private UserPreferences preferences;

    public User() {
        this(null, null, null, null, null);
    }

    public User(String firstName, String lastName, String email, String username, String password) {
        this(null, firstName, lastName, email, username, password);
    }

    public User(UUID userID, String firstName, String lastName, String email, String username, String password) {
        this.userID = userID != null ? userID : UUID.randomUUID();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.password = password;
        this.preferredLocations = new ArrayList<>();
        this.savedProperties = new ArrayList<>();
        this.messages = new ArrayList<>();
        this.identityDocuments = new ArrayList<>();
        this.preferences = new UserPreferences();
    }

    public void replaceWith(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }
        this.userID = user.userID;
        this.firstName = user.firstName;
        this.lastName = user.lastName;
        this.email = user.email;
        this.username = user.username;
        this.password = user.password;
        this.preferredLocations.clear();
        this.preferredLocations.addAll(user.preferredLocations);
        this.savedProperties.clear();
        this.savedProperties.addAll(user.savedProperties);
        this.messages.clear();
        user.messages.forEach(message -> this.messages.add(new Message(message)));
        this.identityDocuments.clear();
        user.identityDocuments.forEach(document -> this.identityDocuments.add(new IdentityDocument(document)));
        this.preferences = new UserPreferences(user.preferences);
    }

    public void mergeWith(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }
        if (user.userID != null) {
            this.userID = user.userID;
        }
        if (user.firstName != null) {
            this.firstName = user.firstName;
        }
        if (user.lastName != null) {
            this.lastName = user.lastName;
        }
        if (user.email != null) {
            this.email = user.email;
        }
        if (user.username != null) {
            this.username = user.username;
        }
        if (user.password != null) {
            this.password = user.password;
        }
        if (!user.preferredLocations.isEmpty()) {
            this.preferredLocations.addAll(user.preferredLocations);
        }
        if (!user.savedProperties.isEmpty()) {
            this.savedProperties.addAll(user.savedProperties);
        }
        if (!user.messages.isEmpty()) {
            user.messages.forEach(message -> this.messages.add(new Message(message)));
        }
        if (!user.identityDocuments.isEmpty()) {
            user.identityDocuments.forEach(document -> {
                boolean exists = this.identityDocuments.stream()
                        .anyMatch(existing -> existing.getDocumentId().equals(document.getDocumentId()));
                if (!exists) {
                    this.identityDocuments.add(new IdentityDocument(document));
                }
            });
        }
        if (user.preferences != null) {
            this.preferences.merge(user.preferences);
        }
    }

    public UUID getUserID() {
        return userID;
    }

    public void setUserID(UUID userID) {
        this.userID = userID;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getPreferredLocations() {
        return Collections.unmodifiableList(preferredLocations);
    }

    public boolean addPreferredLocation(String location) {
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("Location must not be blank");
        }
        return preferredLocations.add(location.trim());
    }

    public boolean removePreferredLocation(String location) {
        if (location == null || location.isBlank()) {
            return false;
        }
        return preferredLocations.remove(location.trim());
    }

    public List<String> getSavedProperties() {
        return Collections.unmodifiableList(savedProperties);
    }

    public boolean saveProperty(String propertyId) {
        if (propertyId == null || propertyId.isBlank()) {
            throw new IllegalArgumentException("Property ID must not be blank");
        }
        return savedProperties.add(propertyId.trim());
    }

    public boolean removeSavedProperty(String propertyId) {
        if (propertyId == null || propertyId.isBlank()) {
            return false;
        }
        return savedProperties.remove(propertyId.trim());
    }

    public List<Property> searchProperties(Collection<Property> availableProperties, PropertySearchCriteria criteria) {
        Objects.requireNonNull(availableProperties, "Available properties must not be null");
        Predicate<Property> predicate = property -> true;
        if (criteria != null) {
            predicate = predicate.and(criteria.toPredicate());
        }
        return availableProperties.stream()
                .filter(predicate)
                .sorted(Comparator.comparing(Property::getPrice))
                .collect(Collectors.toList());
    }

    public List<Property> displayAvailableProperties(Collection<Property> availableProperties) {
        Objects.requireNonNull(availableProperties, "Available properties must not be null");
        return availableProperties.stream()
                .filter(property -> property.getStatus() == Property.PropertyStatus.FOR_SALE)
                .sorted(Comparator.comparing(property -> property.getPropertyId().toString()))
                .collect(Collectors.toList());
    }

    public Message sendMessage(User recipient, String subject, String content) {
        Objects.requireNonNull(recipient, "Recipient must not be null");
        Message outbound = Message.outbound(this.userID, recipient.userID, subject, content);
        this.messages.add(outbound);
        recipient.receiveMessage(Message.inbound(this.userID, recipient.userID, subject, content));
        return outbound;
    }

    private void receiveMessage(Message message) {
        this.messages.add(message);
    }

    public List<Message> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    public List<Message> getMessages(MessageDirection direction) {
        Objects.requireNonNull(direction, "Direction must not be null");
        List<Message> filtered = messages.stream()
                .filter(message -> message.getDirection() == direction)
                .map(Message::new)
                .collect(Collectors.toList());
        return Collections.unmodifiableList(filtered);
    }

    public IdentityDocument uploadIdentityDocument(String documentName, DocumentType documentType, byte[] content, LocalDate expirationDate) {
        IdentityDocument document = new IdentityDocument(documentName, documentType, content, expirationDate);
        identityDocuments.add(document);
        return document;
    }

    public boolean removeIdentityDocument(UUID documentId) {
        if (documentId == null) {
            return false;
        }
        return identityDocuments.removeIf(document -> document.getDocumentId().equals(documentId));
    }

    public List<IdentityDocument> getIdentityDocuments() {
        return Collections.unmodifiableList(identityDocuments.stream()
                .map(IdentityDocument::new)
                .collect(Collectors.toList()));
    }

    public UserPreferences getPreferences() {
        return preferences;
    }

    public void updatePreferences(Consumer<UserPreferences> updater) {
        Objects.requireNonNull(updater, "Updater must not be null");
        updater.accept(preferences);
    }

    public void updateProfile(String firstName, String lastName, String email) {
        if (firstName != null && !firstName.isBlank()) {
            this.firstName = firstName;
        }
        if (lastName != null && !lastName.isBlank()) {
            this.lastName = lastName;
        }
        if (email != null && !email.isBlank()) {
            this.email = email;
        }
    }

    public void changePassword(String currentPassword, String newPassword) {
        if (!Objects.equals(this.password, currentPassword)) {
            throw new IllegalArgumentException("Current password does not match");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("New password must not be blank");
        }
        this.password = newPassword;
    }

    public String describe() {
        return "userID=" + this.userID +
                ", firstName='" + this.firstName + "'" +
                ", lastName='" + this.lastName + "'" +
                ", username='" + this.username + "'" +
                ", email='" + this.email + "'" +
                ", preferredLocations=" + preferredLocations +
                ", savedProperties=" + savedProperties +
                ", documents=" + identityDocuments +
                ", preferences=" + preferences;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" + this.describe() + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(userID, user.userID) && Objects.equals(username, user.username) && Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userID, username, email);
    }



    // PropertyListing class removed - now using separate Property class

    public static class PropertySearchCriteria {
        private final Set<String> locations;
        private final Double minPrice;
        private final Double maxPrice;
        private final Set<String> propertyTypes;

        private PropertySearchCriteria(Builder builder) {
            this.locations = builder.locations.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(new LinkedHashSet<>(builder.locations));
            this.minPrice = builder.minPrice;
            this.maxPrice = builder.maxPrice;
            this.propertyTypes = builder.propertyTypes.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(new LinkedHashSet<>(builder.propertyTypes));
        }

        public Predicate<Property> toPredicate() {
            return property -> matchesLocation(property) && matchesPrice(property) && matchesType(property);
        }

        private boolean matchesLocation(Property property) {
            return locations.isEmpty() || (property.getLocation() != null && locations.contains(property.getLocation()));
        }

        private boolean matchesPrice(Property property) {
            boolean minOk = minPrice == null || property.getPrice() >= minPrice;
            boolean maxOk = maxPrice == null || property.getPrice() <= maxPrice;
            return minOk && maxOk;
        }

        private boolean matchesType(Property property) {
            return propertyTypes.isEmpty() || (property.getType() != null && propertyTypes.contains(property.getType().toString()));
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private final Set<String> locations = new LinkedHashSet<>();
            private Double minPrice;
            private Double maxPrice;
            private final Set<String> propertyTypes = new LinkedHashSet<>();

            public Builder addLocation(String location) {
                if (location != null && !location.isBlank()) {
                    locations.add(location.trim());
                }
                return this;
            }

            public Builder minPrice(double minPrice) {
                this.minPrice = minPrice;
                return this;
            }

            public Builder maxPrice(double maxPrice) {
                this.maxPrice = maxPrice;
                return this;
            }

            public Builder addPropertyType(String propertyType) {
                if (propertyType != null && !propertyType.isBlank()) {
                    propertyTypes.add(propertyType.trim());
                }
                return this;
            }

            public PropertySearchCriteria build() {
                if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
                    throw new IllegalArgumentException("Min price cannot exceed max price");
                }
                return new PropertySearchCriteria(this);
            }
        }
    }

    public static class ListingReference {
        private final String listingId;
        private final String title;
        private final String location;
        private final String propertyType;
        private final double price;
        private final boolean available;
        private final LocalDateTime savedAt;

        public ListingReference(String listingId, String title, String location, String propertyType, double price, boolean available, LocalDateTime savedAt) {
            if (listingId == null || listingId.isBlank()) {
                throw new IllegalArgumentException("Listing id must not be blank");
            }
            this.listingId = listingId;
            this.title = title;
            this.location = location;
            this.propertyType = propertyType;
            this.price = price;
            this.available = available;
            this.savedAt = savedAt == null ? LocalDateTime.now() : savedAt;
        }

        public ListingReference(ListingReference other) {
            this(other.listingId, other.title, other.location, other.propertyType, other.price, other.available, other.savedAt);
        }

        public static ListingReference fromProperty(Property property) {
            Objects.requireNonNull(property, "Property must not be null");
            boolean isAvailable = property.getStatus() == Property.PropertyStatus.FOR_SALE;
            return new ListingReference(
                property.getPropertyId().toString(),
                property.getTitle(),
                property.getLocation(),
                property.getType() != null ? property.getType().toString() : "UNKNOWN",
                property.getPrice(),
                isAvailable,
                LocalDateTime.now()
            );
        }

        public String getListingId() {
            return listingId;
        }

        public String getTitle() {
            return title;
        }

        public String getLocation() {
            return location;
        }

        public String getPropertyType() {
            return propertyType;
        }

        public double getPrice() {
            return price;
        }

        public boolean isAvailable() {
            return available;
        }

        public LocalDateTime getSavedAt() {
            return savedAt;
        }

        @Override
        public String toString() {
            return "ListingReference{" +
                    "listingId='" + listingId + '\'' +
                    ", title='" + title + '\'' +
                    ", location='" + location + '\'' +
                    ", propertyType='" + propertyType + '\'' +
                    ", price=" + price +
                    ", available=" + available +
                    ", savedAt=" + savedAt +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ListingReference that)) return false;
            return Objects.equals(listingId, that.listingId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(listingId);
        }
    }

    public static class Message {
        private final UUID messageId;
        private final UUID senderId;
        private final UUID recipientId;
        private final String subject;
        private final String content;
        private final LocalDateTime sentAt;
        private final MessageDirection direction;
        private boolean read;

        private Message(UUID senderId, UUID recipientId, String subject, String content, MessageDirection direction) {
            this.messageId = UUID.randomUUID();
            this.senderId = senderId;
            this.recipientId = recipientId;
            this.subject = subject;
            this.content = content;
            this.sentAt = LocalDateTime.now();
            this.direction = direction;
            this.read = direction == MessageDirection.SENT;
        }

        public Message(Message other) {
            this.messageId = other.messageId;
            this.senderId = other.senderId;
            this.recipientId = other.recipientId;
            this.subject = other.subject;
            this.content = other.content;
            this.sentAt = other.sentAt;
            this.direction = other.direction;
            this.read = other.read;
        }

        public static Message outbound(UUID senderId, UUID recipientId, String subject, String content) {
            return new Message(senderId, recipientId, subject, content, MessageDirection.SENT);
        }

        public static Message inbound(UUID senderId, UUID recipientId, String subject, String content) {
            return new Message(senderId, recipientId, subject, content, MessageDirection.RECEIVED);
        }

        public UUID getMessageId() {
            return messageId;
        }

        public UUID getSenderId() {
            return senderId;
        }

        public UUID getRecipientId() {
            return recipientId;
        }

        public String getSubject() {
            return subject;
        }

        public String getContent() {
            return content;
        }

        public LocalDateTime getSentAt() {
            return sentAt;
        }

        public MessageDirection getDirection() {
            return direction;
        }

        public boolean isRead() {
            return read;
        }

        public void markAsRead() {
            this.read = true;
        }

        @Override
        public String toString() {
            return "Message{" +
                    "messageId=" + messageId +
                    ", senderId=" + senderId +
                    ", recipientId=" + recipientId +
                    ", subject='" + subject + '\'' +
                    ", sentAt=" + sentAt +
                    ", direction=" + direction +
                    ", read=" + read +
                    '}';
        }
    }

    public static class IdentityDocument {
        private final UUID documentId;
        private final String documentName;
        private final DocumentType documentType;
        private final byte[] content;
        private final LocalDate uploadedAt;
        private final LocalDate expirationDate;

        public IdentityDocument(String documentName, DocumentType documentType, byte[] content, LocalDate expirationDate) {
            this.documentId = UUID.randomUUID();
            this.documentName = documentName == null || documentName.isBlank() ? "Unnamed Document" : documentName;
            this.documentType = documentType == null ? DocumentType.OTHER : documentType;
            this.content = content == null ? new byte[0] : content.clone();
            this.uploadedAt = LocalDate.now();
            this.expirationDate = expirationDate;
        }

        public IdentityDocument(IdentityDocument other) {
            this.documentId = other.documentId;
            this.documentName = other.documentName;
            this.documentType = other.documentType;
            this.content = other.content.clone();
            this.uploadedAt = other.uploadedAt;
            this.expirationDate = other.expirationDate;
        }

        public UUID getDocumentId() {
            return documentId;
        }

        public String getDocumentName() {
            return documentName;
        }

        public DocumentType getDocumentType() {
            return documentType;
        }

        public byte[] getContent() {
            return content.clone();
        }

        public LocalDate getUploadedAt() {
            return uploadedAt;
        }

        public LocalDate getExpirationDate() {
            return expirationDate;
        }

        @Override
        public String toString() {
            return "IdentityDocument{" +
                    "documentId=" + documentId +
                    ", documentName='" + documentName + '\'' +
                    ", documentType=" + documentType +
                    ", uploadedAt=" + uploadedAt +
                    ", expirationDate=" + expirationDate +
                    '}';
        }
    }

    public static class UserPreferences {
        private boolean notificationsEnabled;
        private boolean marketingOptIn;
        private Set<Role> preferredAgentRoles;

        public UserPreferences() {
            this.notificationsEnabled = true;
            this.marketingOptIn = false;
            this.preferredAgentRoles = new LinkedHashSet<>();
            this.preferredAgentRoles.add(Role.AGENT);
        }

        public UserPreferences(UserPreferences other) {
            this.notificationsEnabled = other.notificationsEnabled;
            this.marketingOptIn = other.marketingOptIn;
            this.preferredAgentRoles = new LinkedHashSet<>(other.preferredAgentRoles);
        }

        public boolean isNotificationsEnabled() {
            return notificationsEnabled;
        }

        public void setNotificationsEnabled(boolean notificationsEnabled) {
            this.notificationsEnabled = notificationsEnabled;
        }

        public boolean isMarketingOptIn() {
            return marketingOptIn;
        }

        public void setMarketingOptIn(boolean marketingOptIn) {
            this.marketingOptIn = marketingOptIn;
        }

        public Set<Role> getPreferredAgentRoles() {
            return Collections.unmodifiableSet(preferredAgentRoles);
        }

        public void setPreferredAgentRoles(Set<Role> preferredAgentRoles) {
            if (preferredAgentRoles == null || preferredAgentRoles.isEmpty()) {
                this.preferredAgentRoles = new LinkedHashSet<>();
                this.preferredAgentRoles.add(Role.AGENT);
            } else {
                this.preferredAgentRoles = new LinkedHashSet<>(preferredAgentRoles);
            }
        }

        private void merge(UserPreferences other) {
            if (other == null) {
                return;
            }
            this.notificationsEnabled = other.notificationsEnabled;
            this.marketingOptIn = other.marketingOptIn;
            if (other.preferredAgentRoles != null && !other.preferredAgentRoles.isEmpty()) {
                this.preferredAgentRoles = new LinkedHashSet<>(other.preferredAgentRoles);
            }
        }

        @Override
        public String toString() {
            return "UserPreferences{" +
                    "notificationsEnabled=" + notificationsEnabled +
                    ", marketingOptIn=" + marketingOptIn +
                    ", preferredAgentRoles=" + preferredAgentRoles +
                    '}';
        }
    }
}

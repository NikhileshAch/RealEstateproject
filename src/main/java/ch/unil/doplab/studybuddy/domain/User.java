package ch.unil.doplab.studybuddy.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class User {

    public enum Role {
        BUYER,
        RENTER,
        SELLER,
        LANDLORD,
        AGENT,
        ADMIN
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

    private UUID uuid;
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private String email;
    private int balance;
    private Set<String> languages;
    private Map<LocalDateTime, Lesson> lessons;

    private Role role;
    private final Set<String> preferredLocations;
    private final Map<String, ListingReference> savedListings;
    private final List<Message> messages;
    private final List<IdentityDocument> identityDocuments;
    private UserPreferences preferences;

    public User() {
        this(null, null, null, null, null);
    }

    public User(String firstName, String lastName, String email, String username, String password) {
        this(null, firstName, lastName, email, username, password);
    }

    public User(UUID uuid, String firstName, String lastName, String email, String username, String password) {
        this.uuid = uuid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.password = password;
        this.balance = 0;
        this.languages = new TreeSet<>();
        this.lessons = new TreeMap<>();
        this.role = Role.BUYER;
        this.preferredLocations = new LinkedHashSet<>();
        this.savedListings = new LinkedHashMap<>();
        this.messages = new ArrayList<>();
        this.identityDocuments = new ArrayList<>();
        this.preferences = new UserPreferences();
    }

    public void replaceWith(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }
        this.uuid = user.uuid;
        this.firstName = user.firstName;
        this.lastName = user.lastName;
        this.email = user.email;
        this.username = user.username;
        this.password = user.password;
        this.balance = user.balance;
        this.languages = new TreeSet<>(user.languages);
        this.lessons = new TreeMap<>(user.lessons);
        this.role = user.role;
        this.preferredLocations.clear();
        this.preferredLocations.addAll(user.preferredLocations);
        this.savedListings.clear();
        user.savedListings.values().forEach(listing -> this.savedListings.put(listing.getListingId(), listing));
        this.messages.clear();
        this.messages.addAll(user.messages);
        this.identityDocuments.clear();
        this.identityDocuments.addAll(user.identityDocuments);
        this.preferences = new UserPreferences(user.preferences);
    }

    public void mergeWith(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }
        if (user.uuid != null) {
            this.uuid = user.uuid;
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
        if (!user.languages.isEmpty()) {
            this.languages.addAll(user.languages);
        }
        if (!user.lessons.isEmpty()) {
            this.lessons.putAll(user.lessons);
        }
        if (user.role != null) {
            this.role = user.role;
        }
        if (!user.preferredLocations.isEmpty()) {
            this.preferredLocations.addAll(user.preferredLocations);
        }
        if (!user.savedListings.isEmpty()) {
            user.savedListings.values().forEach(listing -> this.savedListings.putIfAbsent(listing.getListingId(), listing));
        }
        if (!user.messages.isEmpty()) {
            this.messages.addAll(user.messages);
        }
        if (!user.identityDocuments.isEmpty()) {
            user.identityDocuments.forEach(document -> {
                if (this.identityDocuments.stream().noneMatch(existing -> existing.getDocumentId().equals(document.getDocumentId()))) {
                    this.identityDocuments.add(document);
                }
            });
        }
        if (user.preferences != null) {
            this.preferences.merge(user.preferences);
        }
    }

    public UUID getUUID() {
        return uuid;
    }

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
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

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public void deposit(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        this.balance += amount;
    }

    public void withdraw(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        if (this.balance < amount) {
            throw new IllegalStateException("Insufficient funds: CHF " + (amount - this.balance) + " are missing!");
        }
        this.balance -= amount;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = Objects.requireNonNull(role, "Role must not be null");
    }

    public Set<String> getPreferredLocations() {
        return Collections.unmodifiableSet(preferredLocations);
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

    public Collection<ListingReference> getSavedListings() {
        return Collections.unmodifiableCollection(savedListings.values());
    }

    public Optional<ListingReference> getSavedListing(String listingId) {
        if (listingId == null || listingId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(savedListings.get(listingId));
    }

    public boolean saveListing(PropertyListing listing) {
        Objects.requireNonNull(listing, "Listing must not be null");
        ListingReference reference = new ListingReference(listing.getListingId(), listing.getTitle(), listing.getLocation(), listing.getPropertyType(), listing.getPrice(), listing.isAvailable(), LocalDateTime.now());
        return savedListings.put(reference.getListingId(), reference) == null;
    }

    public boolean removeSavedListing(String listingId) {
        if (listingId == null || listingId.isBlank()) {
            return false;
        }
        return savedListings.remove(listingId) != null;
    }

    public List<PropertyListing> searchProperties(Collection<PropertyListing> availableProperties, PropertySearchCriteria criteria) {
        Objects.requireNonNull(availableProperties, "Available properties must not be null");
        Predicate<PropertyListing> predicate = PropertySearchCriteria.alwaysTrue();
        if (criteria != null) {
            predicate = predicate.and(criteria.toPredicate());
        }
        return availableProperties.stream()
                .filter(predicate)
                .sorted(Comparator.comparing(PropertyListing::getPrice))
                .collect(Collectors.toList());
    }

    public List<PropertyListing> displayAvailableProperties(Collection<PropertyListing> availableProperties) {
        Objects.requireNonNull(availableProperties, "Available properties must not be null");
        return availableProperties.stream()
                .filter(PropertyListing::isAvailable)
                .sorted(Comparator.comparing(PropertyListing::getListingId))
                .collect(Collectors.toList());
    }

    public Message sendMessage(User recipient, String subject, String content) {
        Objects.requireNonNull(recipient, "Recipient must not be null");
        Message outbound = Message.outbound(this.uuid, recipient.uuid, subject, content);
        this.messages.add(outbound);
        recipient.receiveMessage(Message.inbound(this.uuid, recipient.uuid, subject, content));
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
        return messages.stream()
                .filter(message -> message.getDirection() == direction)
                .collect(Collectors.toUnmodifiableList());
    }

    public IdentityDocument uploadIdentityDocument(String documentName, DocumentType documentType, byte[] content, LocalDate expirationDate) {
        IdentityDocument document = new IdentityDocument(documentName, documentType, content, expirationDate);
        identityDocuments.removeIf(existing -> existing.getDocumentId().equals(document.getDocumentId()));
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
        return Collections.unmodifiableList(identityDocuments);
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
        return "id=" + this.uuid +
                ", firstName='" + this.firstName + "'" +
                ", lastName='" + this.lastName + "'" +
                ", username='" + this.username + "'" +
                ", email='" + this.email + "'" +
                ", balance=" + this.balance +
                ", languages=" + this.languages +
                ", lessons=" + lessons +
                ", role=" + role +
                ", preferredLocations=" + preferredLocations +
                ", savedListings=" + savedListings.values() +
                ", documents=" + identityDocuments +
                ", preferences=" + preferences;
    }

    public String toString() {
        return this.getClass().getSimpleName() + "{" + this.describe() + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(uuid, user.uuid) && Objects.equals(username, user.username) && Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, username, email);
    }

    public List<String> getLanguages() {
        return languages.stream().toList();
    }

    public void addLanguage(String language) {
        this.languages.add(language);
    }

    public void removeLanguage(String language) {
        this.languages.remove(language);
    }

    public boolean canCommunicateWith(User other) {
        if (this.languages.isEmpty() || other.languages.isEmpty()) {
            return false;
        }
        for (String language : this.languages) {
            if (other.languages.contains(language)) {
                return true;
            }
        }
        return false;
    }

    public Lesson getLesson(LocalDateTime timeslot) {
        return lessons.get(timeslot);
    }

    public boolean putLesson(LocalDateTime timeslot, Lesson lesson) {
        return lessons.putIfAbsent(timeslot, lesson) != null;
    }

    public boolean removeLesson(Lesson lesson) {
        return lessons.remove(lesson.getTimeslot()) != null;
    }

    public Rating rateLesson(LocalDateTime timeslot, Rating rating) {
        if (timeslot == null) {
            throw new IllegalArgumentException("Timeslot must not be null");
        }
        if (rating == null) {
            throw new IllegalArgumentException("Rating must not be null");
        }
        var lesson = lessons.get(timeslot);
        if (lesson == null) {
            throw new IllegalStateException("No lesson found at " + timeslot);
        }
        var oldRating = lesson.getRating();
        lesson.setRatingUpdate(rating);
        lesson.updateRating();
        return oldRating;
    }

    public void setLanguages(List<String> languages) {
        this.languages = new TreeSet<>(languages);
    }

    public List<Lesson> getLessons() {
        return lessons.values().stream()
                .sorted(Comparator.comparing(Lesson::getTimeslot))
                .toList();
    }

    public void setLessons(List<Lesson> lessons) {
        this.lessons = lessons.stream().collect(Collectors.toMap(Lesson::getTimeslot, lesson -> lesson));
    }

    public static class PropertyListing {
        private final String listingId;
        private final String title;
        private final String location;
        private final String propertyType;
        private final double price;
        private final boolean available;
        private final Map<String, Object> attributes;

        public PropertyListing(String listingId, String title, String location, String propertyType, double price, boolean available, Map<String, Object> attributes) {
            if (listingId == null || listingId.isBlank()) {
                throw new IllegalArgumentException("Listing id must not be blank");
            }
            this.listingId = listingId;
            this.title = title;
            this.location = location;
            this.propertyType = propertyType;
            this.price = price;
            this.available = available;
            this.attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
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

        public Map<String, Object> getAttributes() {
            return attributes;
        }
    }

    public static class PropertySearchCriteria {
        private final Set<String> locations;
        private final Double minPrice;
        private final Double maxPrice;
        private final Set<String> propertyTypes;

        private PropertySearchCriteria(Builder builder) {
            this.locations = builder.locations.isEmpty() ? Set.of() : Set.copyOf(builder.locations);
            this.minPrice = builder.minPrice;
            this.maxPrice = builder.maxPrice;
            this.propertyTypes = builder.propertyTypes.isEmpty() ? Set.of() : Set.copyOf(builder.propertyTypes);
        }

        public Predicate<PropertyListing> toPredicate() {
            return listing -> matchesLocation(listing) && matchesPrice(listing) && matchesType(listing);
        }

        private boolean matchesLocation(PropertyListing listing) {
            return locations.isEmpty() || (listing.getLocation() != null && locations.contains(listing.getLocation()));
        }

        private boolean matchesPrice(PropertyListing listing) {
            boolean minOk = minPrice == null || listing.getPrice() >= minPrice;
            boolean maxOk = maxPrice == null || listing.getPrice() <= maxPrice;
            return minOk && maxOk;
        }

        private boolean matchesType(PropertyListing listing) {
            return propertyTypes.isEmpty() || (listing.getPropertyType() != null && propertyTypes.contains(listing.getPropertyType()));
        }

        public static Predicate<PropertyListing> alwaysTrue() {
            return listing -> true;
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
            this.read = direction == MessageDirection.SENT; // sent messages are implicitly "read"
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
            this.documentName = Objects.requireNonNullElse(documentName, "Unnamed Document");
            this.documentType = Objects.requireNonNullElse(documentType, DocumentType.OTHER);
            this.content = content == null ? new byte[0] : content.clone();
            this.uploadedAt = LocalDate.now();
            this.expirationDate = expirationDate;
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
            this.preferredAgentRoles = EnumSet.of(Role.AGENT);
        }

        public UserPreferences(UserPreferences other) {
            this.notificationsEnabled = other.notificationsEnabled;
            this.marketingOptIn = other.marketingOptIn;
            this.preferredAgentRoles = EnumSet.copyOf(other.preferredAgentRoles);
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
                this.preferredAgentRoles = EnumSet.of(Role.AGENT);
            } else {
                this.preferredAgentRoles = EnumSet.copyOf(preferredAgentRoles);
            }
        }

        private void merge(UserPreferences other) {
            if (other == null) {
                return;
            }
            this.notificationsEnabled = other.notificationsEnabled;
            this.marketingOptIn = other.marketingOptIn;
            if (other.preferredAgentRoles != null && !other.preferredAgentRoles.isEmpty()) {
                this.preferredAgentRoles = EnumSet.copyOf(other.preferredAgentRoles);
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

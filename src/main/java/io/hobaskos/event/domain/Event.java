package io.hobaskos.event.domain;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import org.hibernate.annotations.*;
import org.hibernate.annotations.Cache;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A Event.
 */
@Entity
@Table(name = "event")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "event")
public class Event implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Size(max = 256)
    @Column(name = "title", length = 256)
    private String title;

    @Size(max = 8129)
    @Column(name = "description", length = 8129)
    private String description;

    @Size(max = 512)
    @Column(name = "image_url", length = 512)
    private String imageUrl;

    @Column(name = "from_date")
    private ZonedDateTime fromDate;

    @Column(name = "to_date")
    private ZonedDateTime toDate;

    @NotNull
    @Column(name = "private_event", nullable = false)
    private Boolean privateEvent;

    @Size(max = 64)
    @Column(name = "invitation_code", length = 64)
    private String invitationCode;

    @ManyToOne
    @NotNull
    private User owner;

    @OneToMany(mappedBy = "event", fetch = FetchType.EAGER)
    @Cache(usage = CacheConcurrencyStrategy.NONE)
    @JsonManagedReference
    @Field(type = FieldType.Nested)
    private Set<Location> locations = new HashSet<>();

    @OneToMany(mappedBy = "event")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JsonManagedReference
    @JsonIgnore
    @Field(type = FieldType.Nested)
    private Set<EventUserAttending> attendings = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "event", fetch = FetchType.EAGER)
    @JsonManagedReference
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<EventPoll> polls = new HashSet<>();

    @ManyToOne
    @NotNull
    private EventCategory eventCategory;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public Event title(String title) {
        this.title = title;
        return this;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public Event description(String description) {
        this.description = description;
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Event imageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public ZonedDateTime getFromDate() {
        return fromDate;
    }

    public Event fromDate(ZonedDateTime fromDate) {
        this.fromDate = fromDate;
        return this;
    }

    public void setFromDate(ZonedDateTime fromDate) {
        this.fromDate = fromDate;
    }

    public ZonedDateTime getToDate() {
        return toDate;
    }

    public Event toDate(ZonedDateTime toDate) {
        this.toDate = toDate;
        return this;
    }

    public void setToDate(ZonedDateTime toDate) {
        this.toDate = toDate;
    }

    public Boolean isPrivateEvent() {
        return privateEvent;
    }

    public Event privateEvent(Boolean privateEvent) {
        this.privateEvent = privateEvent;
        return this;
    }

    public void setPrivateEvent(Boolean privateEvent) {
        this.privateEvent = privateEvent;
    }

    public String getInvitationCode() {
        return invitationCode;
    }

    public Event invitationCode(String invitationCode) {
        this.invitationCode = invitationCode;
        return this;
    }

    public void setInvitationCode(String invitationCode) {
        this.invitationCode = invitationCode;
    }

    public User getOwner() {
        return owner;
    }

    public Event owner(User user) {
        this.owner = user;
        return this;
    }

    public void setOwner(User user) {
        this.owner = user;
    }

    public Set<Location> getLocations() {
        return locations;
    }

    public Event locations(Set<Location> locations) {
        this.locations = locations;
        return this;
    }

    public Event addLocations(Location location) {
        locations.add(location);
        recalculateDates();
        location.setEvent(this);
        return this;
    }

    public Event removeLocations(Location location) {
        locations.remove(location);
        recalculateDates();
        location.setEvent(null);
        return this;
    }

    public void setLocations(Set<Location> locations) {
        this.locations = locations;
    }

    public Set<EventUserAttending> getAttendings() {
        return attendings;
    }

    public void setAttendings(Set<EventUserAttending> attendings) {
        this.attendings = attendings;
    }

    public Event addAttending(EventUserAttending attendance) {
        attendings.add(attendance);
        attendance.setEvent(this);
        return this;
    }

    public Event removeAttending(EventUserAttending attendance) {
        attendings.remove(attendance);
        attendance.setEvent(null);
        return this;
    }

    public Set<EventPoll> getPolls() {
        return polls;
    }

    public Event polls(Set<EventPoll> eventPolls) {
        this.polls = eventPolls;
        return this;
    }

    public Event addPolls(EventPoll eventPoll) {
        polls.add(eventPoll);
        eventPoll.setEvent(this);
        return this;
    }

    public Event removePolls(EventPoll eventPoll) {
        polls.remove(eventPoll);
        eventPoll.setEvent(null);
        return this;
    }

    public void setPolls(Set<EventPoll> eventPolls) {
        this.polls = eventPolls;
    }

    public EventCategory getEventCategory() {
        return eventCategory;
    }

    public void setEventCategory(EventCategory eventCategory) {
        this.eventCategory = eventCategory;
    }

    public Event eventCategory(EventCategory eventCategory) {
        this.eventCategory = eventCategory;
        return this;
    }

    private void recalculateDates() {
        if (locations == null || locations.isEmpty()) {
            setFromDate(null);
            setToDate(null);
            return;
        }
        List<Location> locationList = locations.stream().collect(Collectors.toList());
        locationList.sort(Comparator.comparing(Location::getFromDate));
        setFromDate(locationList.get(0).getFromDate());
        locationList.sort(Comparator.comparing(Location::getToDate).reversed());
        setToDate(locationList.get(0).getToDate());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Event event = (Event) o;
        if (event.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, event.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Event{" +
            "id=" + id +
            ", title='" + title + "'" +
            ", description='" + description + "'" +
            ", imageUrl='" + imageUrl + "'" +
            ", fromDate='" + fromDate + "'" +
            ", toDate='" + toDate + "'" +
            ", privateEvent='" + privateEvent + "'" +
            ", invitationCode='" + invitationCode + "'" +
            '}';
    }
}

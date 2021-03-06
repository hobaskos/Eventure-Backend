package io.hobaskos.event.service.mapper;

import io.hobaskos.event.domain.*;
import io.hobaskos.event.repository.EventPollRepository;
import io.hobaskos.event.repository.EventRepository;
import io.hobaskos.event.repository.EventUserAttendingRepository;
import io.hobaskos.event.service.UserService;
import io.hobaskos.event.service.dto.EventDTO;

import io.hobaskos.event.service.dto.EventUserAttendingDTO;
import org.mapstruct.*;

import javax.inject.Inject;
import java.util.List;

/**
 * Mapper for the entity Event and its DTO EventDTO.
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class,LocationMapper.class,
    EventCategoryMapper.class, EventUserAttendingMapper.class})
public abstract class EventMapper {

    @Inject
    private UserService userService;

    @Inject
    private EventUserAttendingRepository eventUserAttendingRepository;

    @Inject
    private EventPollRepository eventPollRepository;

    @Inject
    private EventUserAttendingMapper eventUserAttendingMapper;

    @Mapping(target = "image", ignore = true)
    @Mapping(target = "imageContentType", ignore = true)
    @Mapping(target = "myAttendance", ignore = true)
    @Mapping(target = "attendanceCount", ignore = true)
    @Mapping(target = "defaultPollId", ignore = true)
    @Mapping(source = "owner.login", target = "ownerLogin")
    @Mapping(source = "owner.firstName", target = "ownerFirstName")
    @Mapping(source = "owner.lastName", target = "ownerLastName")
    public abstract EventDTO eventToEventDTO(Event event);

    public abstract List<EventDTO> eventsToEventDTOs(List<Event> events);

    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "locations", ignore = true)
    @Mapping(target = "attendings", ignore = true)
    @Mapping(target = "polls", ignore = true)
    public abstract Event eventDTOToEvent(EventDTO eventDTO);

    public abstract List<Event> eventDTOsToEvents(List<EventDTO> eventDTOs);

    @AfterMapping
    protected void sideLoadMetaData(Event event, @MappingTarget EventDTO result) {
        result.setAttendanceCount(eventUserAttendingRepository.countByEventId(event.getId()));
        User user = userService.getUserWithAuthorities();

        if (user == null) return;
        eventUserAttendingRepository.findOneByEventAndUser(event, user).ifPresent(eventUserAttending ->
            result.setMyAttendance(eventUserAttendingMapper.eventUserAttendingToEventUserAttendingDTO(eventUserAttending)));
        result.setDefaultPollId(eventPollRepository.findFirstByEventId(event.getId()).getId());
    }

}

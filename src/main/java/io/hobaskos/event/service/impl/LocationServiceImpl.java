package io.hobaskos.event.service.impl;

import io.hobaskos.event.domain.Event;
import io.hobaskos.event.repository.EventRepository;
import io.hobaskos.event.repository.search.EventSearchRepository;
import io.hobaskos.event.service.LocationService;
import io.hobaskos.event.domain.Location;
import io.hobaskos.event.repository.LocationRepository;
import io.hobaskos.event.repository.search.LocationSearchRepository;
import io.hobaskos.event.service.TriggerService;
import io.hobaskos.event.service.dto.LocationDTO;
import io.hobaskos.event.service.mapper.LocationMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Service Implementation for managing Location.
 */
@Service
@Transactional
public class LocationServiceImpl implements LocationService{

    private final Logger log = LoggerFactory.getLogger(LocationServiceImpl.class);

    @Inject
    private LocationRepository locationRepository;

    @Inject
    private LocationMapper locationMapper;

    @Inject
    private LocationSearchRepository locationSearchRepository;

    @Inject
    private EventSearchRepository eventSearchRepository;

    @Inject
    private EventRepository eventRepository;

    @Inject
    private TriggerService triggerService;
    /**
     * Save a location.
     *
     * @param locationDTO the entity to save
     * @return the persisted entity
     */
    public LocationDTO save(LocationDTO locationDTO) {
        log.debug("Request to save Location : {}", locationDTO);
        boolean newLocation = locationDTO.getId() == null;

        Location location = locationMapper.locationDTOToLocation(locationDTO);
        location = locationRepository.save(location);

        Event event = eventRepository.findOne(location.getEvent().getId());
        event.addLocations(location);
        eventSearchRepository.save(event);

        LocationDTO result = locationMapper.locationToLocationDTO(location);
        locationSearchRepository.save(location);

        if (newLocation) {
            triggerService.eventLocationAdded(event, location);
        } else {
            triggerService.eventLocationChanged(event, location);
        }
        return result;
    }

    /**
     *  Get all the locations.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<LocationDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Locations");
        Page<Location> result = locationRepository.findAll(pageable);
        return result.map(location -> locationMapper.locationToLocationDTO(location));
    }

    /**
     *  Get one location by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public LocationDTO findOne(Long id) {
        log.debug("Request to get Location : {}", id);
        Location location = locationRepository.findOne(id);
        LocationDTO locationDTO = locationMapper.locationToLocationDTO(location);
        return locationDTO;
    }

    /**
     *  Delete the  location by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Location : {}", id);

        Location location = locationRepository.findOne(id);
        String locationName = location.getName();
        Event event = eventRepository.findOne(location.getEvent().getId());
        event.removeLocations(location);
        eventSearchRepository.save(event);

        locationRepository.delete(id);
        locationSearchRepository.delete(id);

        triggerService.eventLocationDeleted(event, locationName);
    }

    /**
     * Search for the location corresponding to the query.
     *
     *  @param query the query of the search
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<LocationDTO> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Locations for query {}", query);
        Page<Location> result = locationSearchRepository.search(queryStringQuery(query), pageable);
        return result.map(location -> locationMapper.locationToLocationDTO(location));
    }

    /**
     * Search for locations nearby given geoPoint
     * @param lat
     * @param lon
     * @param distance
     * @param pageable
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<LocationDTO> searchNearby(Double lat, Double lon, String distance, Pageable pageable) {
        log.debug("Request to search for a page nearby Location  lat:{},lon:{},distance:{}", lat, lon, distance);

        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder()
            .withPageable(pageable)
            .withQuery(geoDistanceQuery("geoPoint")
                        .lat(lat)
                        .lon(lon)
                        .distance(distance));

        Page<Location> result = locationSearchRepository.search(searchQueryBuilder.build());
        return result.map(location -> locationMapper.locationToLocationDTO(location));
    }

    /**
     * Get locations with event id
     * @param eventId
     * @param pageable
     * @return
     */
    public Page<LocationDTO> getLocationsWithEvent(Long eventId, Pageable pageable) {
        log.debug("Request to get locations with event id: {}", eventId);
        Page<Location> result = locationRepository.findByEventId(eventId, pageable);
        return result.map(locationMapper::locationToLocationDTO);
    }
}

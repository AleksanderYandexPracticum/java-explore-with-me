package ru.practicum.main.event.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.model.State;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> getEventsByInitiatorInAndStateInAndCategoryInAndEventDateAfterAndEventDateBefore(
            Long[] users,
            Long[] states,
            Long[] categories,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable);

    List<Event> getEventsByInitiatorId(Long userId, Pageable pageable);

    List<Event> getEventsByInitiatorIdAndId(Long userId, Long eventId, Pageable pageable);

    Event findFirstByCategoryId(Long catId);

    Event getEventsByIdAndInitiatorId(Long eventId, Long userId);

    Event getEventsById(Long eventId);

    Set<Event> getEventsByIdIn(List<Long> events);

    List<Event> getEventsByInitiatorId(Long userId);


    @Query(value = "select * from events as e " +
            "where e.state=?1 and e.category_id in ?2 and e.paid=?3 and e.participantLimit > e.confirmedRequests or e.participantLimit = 0 " +
            "and e.eventDate > ?4 and upper(e.annotation) like upper('%'?5 '%') or upper(e.description) like upper('%'?5 '%') " +
            "order by e.eventDate desc ", nativeQuery = true)
    List<Event> getAllEventSortEventDateParameters(State state,
                                                   List<Long> category,
                                                   boolean paid,
                                                   LocalDateTime time,
                                                   String text,
                                                   Pageable pageable);

    @Query(value = "select * from events as e " +
            "where e.state=?1 and e.category_id in ?2 and e.paid=?3 and e.participantLimit > e.confirmedRequests or e.participantLimit = 0 " +
            "and e.eventDate > ?4 and upper(e.annotation) like upper('%'?5 '%') or upper(e.description) like upper('%'?5 '%') " +
            "order by e.views desc ", nativeQuery = true)
    List<Event> getAllEventSortViewsParameters(State state,
                                               List<Long> category,
                                               boolean paid,
                                               LocalDateTime time,
                                               String text,
                                               Pageable pageable);


    @Query(value = "select * from events as e " +
            "where e.state=?1 and e.category_id in ?2 and e.paid=?3 " +
            "and e.eventDate > ?4 and upper(e.annotation) like upper('%'?5 '%') or upper(e.description) like upper('%'?5 '%') " +
            "order by e.eventDate desc ", nativeQuery = true)
    List<Event> getAllEventSortEventDateNoLimitParameters(State state,
                                                          List<Long> category,
                                                          boolean paid,
                                                          LocalDateTime time,
                                                          String text,
                                                          Pageable pageable);

    @Query(value = "select * from events as e " +
            "where e.state=?1 and e.category_id in ?2 and e.paid=?3 " +
            "and e.eventDate > ?4 and upper(e.annotation) like upper('%'?5 '%') or upper(e.description) like upper('%'?5 '%') " +
            "order by e.views desc ", nativeQuery = true)
    List<Event> getAllEventSortViewsNoLimitParameters(State state,
                                                      List<Long> category,
                                                      boolean paid,
                                                      LocalDateTime time,
                                                      String text,
                                                      Pageable pageable);


    @Query(value = "select * from events as e " +
            "where e.state=?1 and e.category_id in ?2 and e.paid=?3 and e.participantLimit > e.confirmedRequests or e.participantLimit = 0 " +
            "and e.eventDate => ?4 and e.eventDate <= ?5 and upper(e.annotation) like upper('%'?6 '%') or upper(e.description) like upper('%'?6 '%') " +
            "order by e.eventDate desc ", nativeQuery = true)
    List<Event> getAllEventSortEventDatePeriodDateParameters(State state,
                                                             List<Long> category,
                                                             boolean paid,
                                                             LocalDateTime timeStart,
                                                             LocalDateTime timeEnd,
                                                             String text,
                                                             Pageable pageable);

    @Query(value = "select * from events as e " +
            "where e.state=?1 and e.category_id in ?2 and e.paid=?3 and e.participantLimit > e.confirmedRequests or e.participantLimit = 0 " +
            "and e.eventDate => ?4 and e.eventDate <= ?5 and upper(e.annotation) like upper('%'?6 '%') or upper(e.description) like upper('%'?6 '%') " +
            "order by e.views desc ", nativeQuery = true)
    List<Event> getAllEventSortViewsPeriodDateParameters(State state,
                                                         List<Long> category,
                                                         boolean paid,
                                                         LocalDateTime timeStart,
                                                         LocalDateTime timeEnd,
                                                         String text,
                                                         Pageable pageable);

    @Query(value = "select * from events as e " +
            "where e.state=?1 and e.category_id in ?2 and e.paid=?3 " +
            "and e.eventDate => ?4 and e.eventDate <= ?5 and upper(e.annotation) like upper('%'?6 '%') or upper(e.description) like upper('%'?6 '%') " +
            "order by e.eventDate desc ", nativeQuery = true)
    List<Event> getAllEventSortEventDateNoLimitPeriodDateParameters(State state,
                                                                    List<Long> category,
                                                                    boolean paid,
                                                                    LocalDateTime timeStart,
                                                                    LocalDateTime timeEnd,
                                                                    String text,
                                                                    Pageable pageable);

    @Query(value = "select * from events as e " +
            "where e.state=?1 and e.category_id in ?2 and e.paid=?3 " +
            "and e.eventDate => ?4 and e.eventDate <= ?5 and upper(e.annotation) like upper('%'?6 '%') or upper(e.description) like upper('%'?6 '%') " +
            "order by e.views desc ", nativeQuery = true)
    List<Event> getAllEventSortViewsNoLimitPeriodDateParameters(State state,
                                                                List<Long> category,
                                                                boolean paid,
                                                                LocalDateTime timeStart,
                                                                LocalDateTime timeEnd,
                                                                String text,
                                                                Pageable pageable);

    Event getEventByIdAndState(Long eventId, State state);

    /////выгружаем только доступные   с "0"
//    List<Event> getAllByStateAndCategoryInAndPaidAndParticipantLimitAndEventDateAfterAndAnnotationContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrderByEventDateDesc(
//            State state,
//            List<Category> category,
//            boolean paid,
//            Integer participantLimit,
//            LocalDateTime time,
//            String text1,
//            String text2,
//            Pageable pageable);
//
//    /////ВСЕ выгружаем
//    List<Event> getAllByStateAndCategoryInAndPaidAndEventDateAfterAndAnnotationContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrderByEventDateDesc(
//            State state,
//            List<Category> category,
//            boolean paid,
//            LocalDateTime time,
//            String text1,
//            String text2,
//            Pageable pageable);
//
//
//    List<Event> getAllByStateAndEventDateAfterAndAnnotationContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
//            State state,
//            LocalDateTime time,
//            String text1,
//            String text2,
//            Pageable pageable);
//
//
//    List<Event> getAllByStateAndEventDateAfterAndEventDateBeforeAndAnnotationContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
//            State state,
//            LocalDateTime startTime,
//            LocalDateTime endtime,
//            String text1,
//            String text2,
//            Pageable pageable);

}
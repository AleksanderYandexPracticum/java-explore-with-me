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
    List<Event> getEventsByCategoryIdIn(List<Long> categories, Pageable pageable);

//    List<Event> findEventByStateIn(List<String> states, Pageable pageable);

    List<Event> getEventsByStateIn(List<State> states, Pageable pageable);

    List<Event> getEventsByInitiatorIdIn(List<Long> users, Pageable pageable);

    List<Event> getEventsByCategoryIdInAndStateIn(List<Long> categories, List<State> states, Pageable pageable);

    List<Event> getEventsByInitiatorIdInAndStateIn(List<Long> users, List<State> states, Pageable pageable);

    List<Event> getEventsByInitiatorIdInAndCategoryIdIn(List<Long> users, List<Long> categories, Pageable pageable);

    List<Event> getEventsByInitiatorIdInAndStateInAndCategoryIdIn(List<Long> users, List<State> states, List<Long> categories, Pageable pageable);

    List<Event> getEventsByEventDateAfterAndEventDateBefore(LocalDateTime start, LocalDateTime end, Pageable pageable);

    //List<Event> getEventsByInitiatorIdInAndStateInAndCategoryIdIn(Long[] users, Long[] states, Long[] categories, Pageable pageable);


    List<Event> getEventsByCategoryIdInAndEventDateAfterAndEventDateBefore(
            List<Long> categories,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable);

    List<Event> getEventsByStateInAndEventDateAfterAndEventDateBefore(
            List<State> states,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable);

    List<Event> getEventsByInitiatorIdInAndEventDateAfterAndEventDateBefore(
            List<Long> users,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable);

    List<Event> getEventsByStateInAndCategoryIdInAndEventDateAfterAndEventDateBefore(
            List<State> states,
            List<Long> categories,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable);

    List<Event> getEventsByInitiatorIdInAndStateInAndEventDateAfterAndEventDateBefore(
            List<Long> users,
            List<State> states,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable);

    List<Event> getEventsByInitiatorIdInAndCategoryIdInAndEventDateAfterAndEventDateBefore(
            List<Long> users,
            List<Long> categories,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable);

    List<Event> getEventsByInitiatorIdInAndStateInAndCategoryIdInAndEventDateAfterAndEventDateBefore(
            List<Long> users,
            List<State> stateEnum,
            List<Long> categories,
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


    @Query(value = "select e from events as e " +
            "where e.state=?1 " +
            "and e.category_id in ?2 " +
            "and e.paid=?3 " +
            "and e.participant_limit = 0 or e.participant_limit > e.confirmed_requests " +
            "and e.event_date > ?4 " +
            "and upper(e.annotation) like upper(?5) or upper(e.description) like upper(?5) " +
            "order by e.event_date desc ", nativeQuery = true)
    List<Event> getAllEventSortEventDateParameters(String state,
                                                   List<Long> category,
                                                   boolean paid,
                                                   LocalDateTime time,
                                                   String text,
                                                   Pageable pageable);

    @Query(value = "select e from events as e " +
            "where e.state=?1 " +
            "and e.category_id in ?2 " +
            "and e.paid=?3 " +
            "and e.event_date > ?4 " +
            "and upper(e.annotation) like upper(?5) or upper(e.description) like upper(?5) " +
            "order by e.event_date desc ", nativeQuery = true)
    List<Event> getAllEventSortEventDateNoLimitParameters(String state,
                                                          List<Long> category,
                                                          boolean paid,
                                                          LocalDateTime time,
                                                          String text,
                                                          Pageable pageable);

    @Query(value = "select e from events as e " +
            "where e.state=?1 " +
            "and e.category_id in ?2 " +
            "and e.paid=?3 " +
            "and e.participant_limit = 0 or e.participant_limit > e.confirmed_requests " +
            "and e.event_date > ?4 " +
            "and upper(e.annotation) like upper(?5) or upper(e.description) like upper(?5) " +
            "order by e.views desc ", nativeQuery = true)
    List<Event> getAllEventSortViewsParameters(String state,
                                               List<Long> category,
                                               boolean paid,
                                               LocalDateTime time,
                                               String text,
                                               Pageable pageable);


    @Query(value = "select e from events as e " +
            "where e.state=?1 " +
            "and e.category_id in ?2 " +
            "and e.paid=?3 " +
            "and e.event_date > ?4 " +
            "and upper(e.annotation) like upper(?5) or upper(e.description) like upper(?5) " +
            "order by e.views desc ", nativeQuery = true)
    List<Event> getAllEventSortViewsNoLimitParameters(String state,
                                                      List<Long> category,
                                                      boolean paid,
                                                      LocalDateTime time,
                                                      String text,
                                                      Pageable pageable);


    @Query(value = "select e from events as e " +
            "where e.state=?1 " +
            "and e.category_id in ?2 " +
            "and e.paid=?3 " +
            "and e.participant_limit = 0 or e.participant_limit > e.confirmed_requests " +
            "and e.event_date > ?4 " +
            "and upper(e.annotation) like upper(?5) or upper(e.description) like upper(?5) ", nativeQuery = true)
    List<Event> getAllEventSortNullParameters(String state,
                                              List<Long> category,
                                              boolean paid,
                                              LocalDateTime time,
                                              String text,
                                              Pageable pageable);

    @Query(value = "select e from events as e " +
            "where e.state=?1 " +
            "and e.category_id in ?2 " +
            "and e.paid=?3 " +
            "and e.event_date > ?4 " +
            "and upper(e.annotation) like upper(?5) or upper(e.description) like upper(?5) ", nativeQuery = true)
    List<Event> getAllEventSortNullNoLimitParameters(String state,
                                                     List<Long> category,
                                                     boolean paid,
                                                     LocalDateTime time,
                                                     String text,
                                                     Pageable pageable);


    @Query(value = "select e from events as e " +
            "where e.state=?1 " +
            "and e.category_id in ?2 " +
            "and e.paid=?3 " +
            "and e.participant_limit = 0 or e.participant_limit > e.confirmed_requests " +
            "and e.event_date => ?4 and e.event_date <= ?5 " +
            "and upper(e.annotation) like upper(?6) or upper(e.description) like upper(?6) " +
            "order by e.event_date desc ", nativeQuery = true)
    List<Event> getAllEventSortEventDatePeriodDateParameters(String state,
                                                             List<Long> category,
                                                             boolean paid,
                                                             LocalDateTime timeStart,
                                                             LocalDateTime timeEnd,
                                                             String text,
                                                             Pageable pageable);

    @Query(value = "select e from events as e " +
            "where e.state=?1 " +
            "and e.category_id in ?2 " +
            "and e.paid=?3 " +
            "and e.event_date => ?4 and e.event_date <= ?5 " +
            "and upper(e.annotation) like upper(?6) or upper(e.description) like upper(?6) " +
            "order by e.event_date desc ", nativeQuery = true)
    List<Event> getAllEventSortEventDateNoLimitPeriodDateParameters(String state,
                                                                    List<Long> category,
                                                                    boolean paid,
                                                                    LocalDateTime timeStart,
                                                                    LocalDateTime timeEnd,
                                                                    String text,
                                                                    Pageable pageable);

    @Query(value = "select e from events as e " +
            "where e.state=?1 " +
            "and e.category_id in ?2 " +
            "and e.paid=?3 " +
            "and e.participant_limit = 0 or e.participant_limit > e.confirmed_requests " +
            "and e.event_date => ?4 and e.event_date <= ?5 " +
            "and upper(e.annotation) like upper(?6) or upper(e.description) like upper(?6) " +
            "order by e.views desc ", nativeQuery = true)
    List<Event> getAllEventSortViewsPeriodDateParameters(String state,
                                                         List<Long> category,
                                                         boolean paid,
                                                         LocalDateTime timeStart,
                                                         LocalDateTime timeEnd,
                                                         String text,
                                                         Pageable pageable);

    @Query(value = "select e from events as e " +
            "where e.state=?1 " +
            "and e.category_id in ?2 " +
            "and e.paid=?3 " +
            "and e.event_date => ?4 and e.event_date <= ?5 " +
            "and upper(e.annotation) like upper(?6) or upper(e.description) like upper(?6) " +
            "order by e.views desc ", nativeQuery = true)
    List<Event> getAllEventSortViewsNoLimitPeriodDateParameters(String state,
                                                                List<Long> category,
                                                                boolean paid,
                                                                LocalDateTime timeStart,
                                                                LocalDateTime timeEnd,
                                                                String text,
                                                                Pageable pageable);

    @Query(value = "select e from events as e " +
            "where e.state=?1 " +
            "and e.category_id in ?2 " +
            "and e.paid=?3 " +
            "and e.participant_limit = 0 or e.participant_limit > e.confirmed_requests " +
            "and e.event_date => ?4 and e.event_date <= ?5 " +
            "and upper(e.annotation) like upper(?6) or upper(e.description) like upper(?6) ", nativeQuery = true)
    List<Event> getAllEventSortNullPeriodDateParameters(String state,
                                                        List<Long> category,
                                                        boolean paid,
                                                        LocalDateTime timeStart,
                                                        LocalDateTime timeEnd,
                                                        String text,
                                                        Pageable pageable);

    @Query(value = "select e from events as e " +
            "where e.state=?1 " +
            "and e.category_id in ?2 " +
            "and e.paid=?3 " +
            "and e.event_date => ?4 and e.event_date <= ?5 " +
            "and upper(e.annotation) like upper(?6) or upper(e.description) like upper(?6) ", nativeQuery = true)
    List<Event> getAllEventSortNullNoLimitPeriodDateParameters(String state,
                                                               List<Long> category,
                                                               boolean paid,
                                                               LocalDateTime timeStart,
                                                               LocalDateTime timeEnd,
                                                               String text,
                                                               Pageable pageable);


    Event getEventByIdAndState(Long eventId, State state);


//    @Query(value = "select e from events as e " +
//            "where (e.state=?1 or ?1 is null) " +
//            "and (e.category_id in ?2 or ?2 is null) " +
//            "order by e.views desc ", nativeQuery = true)
//    List<Event> NeeeeeeeeeeeeeeeeeeNNNNNNNNNNNNNNNNN(
//            State state,
//            List<Long> category,
//            Pageable pageable);

//    @Query(value = "select e from events as e " +
//            "where (e.state=?1 or ?1 is null) " +
//            "and (e.category_id in ?2 or ?2 is null) " +
//            "and (e.paid=?3 or ?3 is null ) " +
//            "and e.participant_limit = 0 or e.participant_limit > e.confirmed_requests " +
//            "and (e.event_date => ?4 or ?4 is null) " +
//            "and (e.event_date <= ?5 or ?5 is null) " +
//            "and upper(e.annotation) like upper(?6) " +
//            "or upper(e.description) like upper(?6) " +
//            "order by e.views desc ", nativeQuery = true)
//    List<Event> Neeeeeeeeeeeeeeeeee(
//            State state,
//            List<Long> category,
//            boolean paid,
//            LocalDateTime timeStart,
//            LocalDateTime timeEnd,
//            String text,
//            Pageable pageable);

//    @Query(value = "select e from events as e " +
//            "where (e.state=:state or :state is null) " +
//            "and (e.category_id in :cat or :cat is null) " +
//            "and (e.paid=:paid or :paid is null ) " +
//            "and e.participant_limit = 0 or e.participant_limit > e.confirmed_requests " +
//            "and (e.event_date => :start or :start is null) " +
//            "and (e.event_date <= :end or :end is null) " +
//            "and upper(e.annotation) like upper(:text) " +
//            "or upper(e.description) like upper(:text) " +
//            "order by e.views desc ", nativeQuery = true)
//    List<Event> Neeeeeeeeeeeeeeeeee(
//            @Param("state") State state,
//            @Param("cat")List<Long> category,
//            @Param("paid")boolean paid,
//            @Param("start")LocalDateTime timeStart,
//            @Param("end")LocalDateTime timeEnd,
//            @Param("text")String text,
//            Pageable pageable);


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
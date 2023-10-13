package ru.practicum.main.event.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.main.category.model.Category;
import ru.practicum.main.compilation.model.Compilation;
import ru.practicum.main.location.model.Location;
import ru.practicum.main.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Table(name = "events")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String annotation;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Column
    private Long confirmedRequests;

    @Column
    private LocalDateTime createdOn;

    @Column
    private String description;

    @Column
    private LocalDateTime eventDate;

    @ManyToOne
    @JoinColumn(name = "initiator_id")
    private User initiator;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;

    @Column
    private boolean paid;

    @Column
    private Integer participantLimit;

    @Column
    private LocalDateTime publishedOn;

    @Column
    private boolean requestModeration;

    @Enumerated(EnumType.STRING)
    private State state;

    @Column
    private String title;

    @Column
    private Long views;

}
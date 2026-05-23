package com.example.gestion_financement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "log_action")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "utilisateur_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Utilisateur utilisateur;

    @Column(nullable = false)
    private String action;

    private String entite;

    private Long entiteId;

    @Column(columnDefinition = "TEXT")
    private String details;

    private LocalDateTime dateAction;

    @PrePersist
    public void prePersist() {
        if (dateAction == null) {
            dateAction = LocalDateTime.now();
        }
    }
}

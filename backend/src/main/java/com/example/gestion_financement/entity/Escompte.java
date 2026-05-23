package com.example.gestion_financement.entity;

import com.example.gestion_financement.enums.StatutOperation;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Entity
@Table(name = "escompte")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Escompte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String reference;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "partenaire_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Partenaire partenaire;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "banque_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Banque banque;

    @NotNull
    @DecimalMin("0.01")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montant;

    @NotNull
    @DecimalMin("0.01")
    @Column(nullable = false, precision = 8, scale = 4)
    private BigDecimal taux;

    @NotNull
    @Min(1)
    @Column(nullable = false)
    private Integer duree;

    private LocalDate dateCreation;
    private LocalDate dateEcheance;

    @Column(precision = 15, scale = 2)
    private BigDecimal agios;

    @Column(precision = 15, scale = 2)
    private BigDecimal netRecu;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutOperation statut = StatutOperation.EN_ATTENTE;

    @PrePersist
    public void prePersist() {
        if (dateCreation == null) {
            dateCreation = LocalDate.now();
        }
        if (reference == null) {
            reference = "ESC-" + System.currentTimeMillis();
        }
        calculerAgios();
    }

    @PreUpdate
    public void preUpdate() {
        calculerAgios();
    }

    public void calculerAgios() {
        if (montant != null && taux != null && duree != null) {
            // Formule : agios = montant × taux(%) × durée(jours) / 360
            this.agios = montant
                .multiply(taux)
                .multiply(BigDecimal.valueOf(duree))
                .divide(BigDecimal.valueOf(36000), 2, RoundingMode.HALF_UP);
            this.netRecu = montant.subtract(agios).setScale(2, RoundingMode.HALF_UP);
        }
    }
}

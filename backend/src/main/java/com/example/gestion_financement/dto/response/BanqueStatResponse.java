package com.example.gestion_financement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BanqueStatResponse {
    private String     banqueNom;
    private long       count;
    private BigDecimal montantTotal;
    private BigDecimal chargesTotal;
}

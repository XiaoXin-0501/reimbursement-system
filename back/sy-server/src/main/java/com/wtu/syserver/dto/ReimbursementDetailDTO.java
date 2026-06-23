package com.wtu.syserver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ReimbursementDetailDTO {
    private String idempotentToken;

    @JsonProperty("reimbursement")
    private ReimbursementDTO reimbursementDTO;

    @JsonProperty("trips")
    private List<TripDTO> tripDTOS;

    @JsonProperty("allocations")
    private List<CostAllocationDTO> costAllocationDTOS;
}

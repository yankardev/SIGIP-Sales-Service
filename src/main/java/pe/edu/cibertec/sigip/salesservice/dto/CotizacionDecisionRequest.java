package pe.edu.cibertec.sigip.salesservice.dto;

import jakarta.validation.constraints.NotNull;

public record CotizacionDecisionRequest(@NotNull Boolean aceptada) {
}

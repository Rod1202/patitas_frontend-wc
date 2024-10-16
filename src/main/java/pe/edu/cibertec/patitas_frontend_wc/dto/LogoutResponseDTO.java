package pe.edu.cibertec.patitas_frontend_wc.dto;

import java.util.Date;

public record LogoutResponseDTO(Date fechaLogout, String errorMessage) {
}
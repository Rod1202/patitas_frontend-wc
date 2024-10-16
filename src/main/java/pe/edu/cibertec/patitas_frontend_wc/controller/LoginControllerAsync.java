package pe.edu.cibertec.patitas_frontend_wc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import pe.edu.cibertec.patitas_frontend_wc.dto.LoginRequestDTO;
import pe.edu.cibertec.patitas_frontend_wc.dto.LoginResponseDTO;
import pe.edu.cibertec.patitas_frontend_wc.dto.LogoutRequestDTO;
import pe.edu.cibertec.patitas_frontend_wc.dto.LogoutResponseDTO;
import reactor.core.publisher.Mono;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/login")
@CrossOrigin(origins = "http://localhost:5173/")
public class LoginControllerAsync {

    @Autowired
    WebClient webClientAutenticacion;

    @PostMapping("/autenticar-async")
    public Mono<LoginResponseDTO> autenticar(@RequestBody LoginRequestDTO loginRequestDTO) {

        // validar campos de entrada
        if (loginRequestDTO.tipoDocumento() == null || loginRequestDTO.tipoDocumento().trim().length() == 0 ||
                loginRequestDTO.numeroDocumento() == null || loginRequestDTO.numeroDocumento().trim().length() == 0 ||
                loginRequestDTO.password() == null || loginRequestDTO.password().trim().length() == 0){
            return Mono.just(new LoginResponseDTO("01", "Error: Debe completar correctamente sus credenciales", "", ""));
        }

        try {

            // consumir servicio backend de autenticacion
            return webClientAutenticacion.post()
                    .uri("/login")
                    .body(Mono.just(loginRequestDTO), LoginRequestDTO.class)
                    .retrieve()
                    .bodyToMono(LoginResponseDTO.class)
                    .flatMap(response -> {

                        if(response.codigo().equals("00")){
                            return Mono.just(new LoginResponseDTO("00", "", response.nombreUsuario(), ""));
                        } else {
                            return Mono.just(new LoginResponseDTO("02", "Error: Autenticación fallida", "", ""));
                        }

                    });

        } catch(Exception e) {

            System.out.println(e.getMessage());
            return Mono.just(new LoginResponseDTO("99", "Error: Ocurrió un problema en la autenticación", "", ""));

        }

    }


    @PostMapping("/cerrar-sesion-async")
    public Mono<LogoutResponseDTO> cerrarSesion(@RequestBody LogoutRequestDTO logoutRequestDTO) {
        
        return webClientAutenticacion.post()
                .uri("/logout")
                .body(Mono.just(logoutRequestDTO), LogoutRequestDTO.class)
                .retrieve()
                .bodyToMono(LogoutResponseDTO.class)
                .flatMap(logoutResponseDTO -> {
                    if (logoutResponseDTO.errorMessage() == null || logoutResponseDTO.errorMessage().isEmpty()) {
                        return Mono.just(new LogoutResponseDTO(logoutResponseDTO.fechaLogout(), "Exito"));
                    } else {
                       
                        return Mono.just(new LogoutResponseDTO(null, logoutResponseDTO.errorMessage()));
                    }
                })
                .onErrorResume(e -> {
                    System.out.println("Error al cerrar sesión: " + e.getMessage());
                    
                    return Mono.just(new LogoutResponseDTO(null, "Error: Ocurrió un problema"));
                });
    }


}

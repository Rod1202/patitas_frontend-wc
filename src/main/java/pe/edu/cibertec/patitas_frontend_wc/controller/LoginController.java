package pe.edu.cibertec.patitas_frontend_wc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.edu.cibertec.patitas_frontend_wc.dto.LogoutResponseDTO;
import pe.edu.cibertec.patitas_frontend_wc.dto.LogoutRequestDTO;
import pe.edu.cibertec.patitas_frontend_wc.dto.LoginResponseDTO;
import pe.edu.cibertec.patitas_frontend_wc.dto.LoginRequestDTO;
import pe.edu.cibertec.patitas_frontend_wc.viewmodel.LoginModel;
import reactor.core.publisher.Mono;



@Controller
@RequestMapping("/login")
public class LoginController {

    @Autowired
    WebClient webClientAutenticacion;

    @GetMapping("/inicio")
    public String inicio(Model model) {
        LoginModel loginModel = new LoginModel("00", "", "");
        model.addAttribute("loginModel", loginModel);
        return "inicio";
    }

    @GetMapping("/errorPage")
    public String errorPage(Model model) {
        LoginModel loginModel = new LoginModel("00", "", "");
        model.addAttribute("loginModel", loginModel);
        return "errorPage";
    }

    @PostMapping("/autenticar")
    public String autenticar(@RequestParam("tipoDocumento") String tipoDocumento,
                             @RequestParam("numeroDocumento") String numeroDocumento,
                             @RequestParam("password") String password,
                             Model model) {

        // Validar campos de entrada
        if (tipoDocumento == null || tipoDocumento.trim().length() == 0 ||
                numeroDocumento == null || numeroDocumento.trim().length() == 0 ||
                password == null || password.trim().length() == 0){

            LoginModel loginModel = new LoginModel("01", "Error: Debe completar correctamente sus credenciales", "");
            model.addAttribute("loginModel", loginModel);
            return "inicio";
        }

        try {

            // Invocar API de validación de usuario
            LoginRequestDTO loginRequestDTO = new LoginRequestDTO(tipoDocumento, numeroDocumento, password);

            Mono<LoginResponseDTO> monoLoginResponseDTO = webClientAutenticacion.post()
                    .uri("/login")
                    .body(Mono.just(loginRequestDTO), LoginRequestDTO.class)
                    .retrieve()
                    .bodyToMono(LoginResponseDTO.class);

           
            LoginResponseDTO loginResponseDTO = monoLoginResponseDTO.block();

            // Validar respuesta
            if (loginResponseDTO.codigo().equals("00")) {

                LoginModel loginModel = new LoginModel("00", "", loginResponseDTO.nombreUsuario());
                model.addAttribute("loginModel", loginModel);
                LoginRequestDTO requestDTO = new LoginRequestDTO(tipoDocumento, numeroDocumento, password);
                model.addAttribute("loginRequestDTO", requestDTO);
                return "principal";

            } else {

                LoginModel loginModel = new LoginModel("02", "Error: Autenticación fallida", "");
                model.addAttribute("loginModel", loginModel);
                return "inicio";

            }

        } catch(Exception e) {

            LoginModel loginModel = new LoginModel("99", "Error: Ocurrió un problema en la autenticación", "");
            model.addAttribute("loginModel", loginModel);
            System.out.println(e.getMessage());
            return "inicio";

        }

    }

    @PostMapping("/cerrar-sesion")
    public String cerrarSesion(@RequestParam("tipoDocumento") String tipoDocumento,
                               @RequestParam("numeroDocumento") String numeroDocumento,
                               Model model, RedirectAttributes redirectAttributes) {

        LogoutRequestDTO logoutRequestDTO = new LogoutRequestDTO(tipoDocumento, numeroDocumento);

        try {
            Mono<LogoutResponseDTO> monoLogoutResponseDTO = webClientAutenticacion.post()
                    .uri("/logout")
                    .body(Mono.just(logoutRequestDTO), LogoutRequestDTO.class)
                    // Obtener la respuesta
                    .retrieve()
                    .bodyToMono(LogoutResponseDTO.class); 


            LogoutResponseDTO logoutResponseDTO = monoLogoutResponseDTO.block();
            redirectAttributes.addFlashAttribute("mensaje", "Sesión cerrada correctamente.");
            return "redirect:/login/inicio";

        } catch (Exception e) {
            // Manejo de excepciones al realizar la solicitud
            model.addAttribute("error", "Error: Ocurrió un problema al intentar cerrar sesión.");
            System.out.println(e.getMessage());
            return "errorPage";
        }
    }


}

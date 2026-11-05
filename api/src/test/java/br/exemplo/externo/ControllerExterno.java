package br.exemplo.externo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/*
Controller de outro pacote, fora de com.hemovia.api, como o da documentação do springdoc.
Serve para provar que o prefixo /api/v1 não vaza para controllers que não são da nossa API.
*/
@RestController
public class ControllerExterno {

    @GetMapping("/externo")
    public String externo() {
        // Resposta fixa, o que importa é o caminho em que ela aparece
        return "externo";
    }
}

package com.hemovia.api.presentation.handler;

import com.hemovia.api.domain.exception.DomainException;
import com.hemovia.api.domain.exception.InvalidStateTransitionException;
import com.hemovia.api.domain.exception.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/*
Controller falso, só para os testes do GlobalExceptionHandler.
Cada rota provoca de propósito um tipo de erro, e o teste confere a resposta padronizada.
*/
@RestController
@RequestMapping("/falso")
class ControllerFalso {

    // Corpo de exemplo com validação, para provocar o erro 400 com erros por campo
    record DtoFalso(
            @NotBlank(message = "O nome é obrigatório") String nome,
            @NotNull(message = "A idade é obrigatória") Integer idade) { }

    // Exceção de negócio de exemplo: o código vem de fora para o teste cobrir toda a tabela 0.7
    private static final class ErroDeDominioFalso extends DomainException {

        // Guardamos o código pedido pela rota
        private final String codigo;

        ErroDeDominioFalso(String codigo) {
            super("Mensagem de negócio para " + codigo);
            this.codigo = codigo;
        }

        @Override
        public String code() {
            return codigo;
        }
    }

    @GetMapping("/nao-encontrado")
    String naoEncontrado() {
        // Recurso inexistente: o caso mais comum de 404
        throw new ResourceNotFoundException("Hospital", 123);
    }

    @PostMapping("/validar")
    String validar(@Valid @RequestBody DtoFalso corpo) {
        // Só chegamos aqui se o corpo for válido, e nos testes ele nunca é
        return "ok";
    }

    @GetMapping("/uuid/{id}")
    String uuid(@PathVariable UUID id) {
        // O Spring converte o caminho em UUID antes de chamar o método
        return id.toString();
    }

    @GetMapping("/erro-inesperado")
    String erroInesperado() {
        // A mensagem tem um segredo falso para provar que ela nunca vaza na resposta
        throw new RuntimeException("senha=segredo-interno vazou do banco");
    }

    @GetMapping("/dominio/{codigo}")
    String dominio(@PathVariable String codigo) {
        // Lança uma exceção de negócio com o código escolhido pelo teste
        throw new ErroDeDominioFalso(codigo);
    }

    @GetMapping("/acesso-negado")
    String acessoNegado() {
        // Usuário logado, mas sem permissão para a rota
        throw new AccessDeniedException("sem permissão");
    }

    @GetMapping("/nao-autenticado")
    String naoAutenticado() {
        // Falha de autenticação vinda do Spring Security
        throw new BadCredentialsException("credenciais ruins");
    }

    @GetMapping("/transicao-invalida")
    String transicaoInvalida() {
        // Mudança de estado que a regra de negócio proíbe
        throw new InvalidStateTransitionException("Não é possível ir de DISCARDED para AVAILABLE");
    }

    @GetMapping("/banco-fora")
    String bancoFora() {
        // Banco indisponível, falha de infraestrutura
        throw new DataAccessResourceFailureException("conexão recusada");
    }

    @GetMapping("/legado-argumento-invalido")
    String legadoArgumentoInvalido() {
        // Os serviços antigos ainda lançam IllegalArgumentException para entrada ruim
        throw new IllegalArgumentException("ID inválido: abc");
    }

    @GetMapping("/legado-entidade")
    String legadoEntidade() {
        // Os serviços antigos ainda lançam a exceção do JPA para recurso inexistente
        throw new EntityNotFoundException("Bolsa não encontrada: 1");
    }
}

package com.hemovia.api.controller;

import com.hemovia.api.dto.request.CreateRouteEdgeRequest;
import com.hemovia.api.dto.request.CreateRouteNodeRequest;
import com.hemovia.api.dto.response.ApiError;
import com.hemovia.api.dto.response.RouteEdgeResponse;
import com.hemovia.api.dto.response.RouteNodeResponse;
import com.hemovia.api.service.usecase.route.ManageRouteGraph;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/route-graph")
public class RouteGraphController {
    private final ManageRouteGraph graph;

    public RouteGraphController(ManageRouteGraph graph) {
        this.graph = graph;
    }

    @Operation(summary = "Cadastrar ponto da rede", description = "Restrito a ADMIN. Pontos vinculados usam as coordenadas do recurso; cruzamentos exigem latitude e longitude.",
            security = @SecurityRequirement(name = "cookieAuth"),
            parameters = @Parameter(name = "X-XSRF-TOKEN", in = ParameterIn.HEADER, required = true, description = "Token CSRF obtido em /auth/csrf"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ponto criado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RouteNodeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Sessão ausente ou inválida", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Papel ou CSRF inválido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Recurso vinculado não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Recurso já vinculado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/nodes")
    public ResponseEntity<RouteNodeResponse> createNode(@Valid @RequestBody CreateRouteNodeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(graph.createNode(request));
    }

    @Operation(summary = "Listar pontos da rede", description = "Disponível a usuários autenticados com papel de negócio.", security = @SecurityRequirement(name = "cookieAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pontos cadastrados", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = RouteNodeResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Sessão ausente ou inválida", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Papel sem acesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/nodes")
    public List<RouteNodeResponse> listNodes() {
        return graph.listNodes();
    }

    @Operation(summary = "Cadastrar ligação direcional", description = "Restrito a ADMIN. Distância, duração e geometria são obtidas do provedor de rotas.",
            security = @SecurityRequirement(name = "cookieAuth"),
            parameters = @Parameter(name = "X-XSRF-TOKEN", in = ParameterIn.HEADER, required = true, description = "Token CSRF obtido em /auth/csrf"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ligação criada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RouteEdgeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Sessão ausente ou inválida", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Papel ou CSRF inválido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Ponto ou hemocentro ativo não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Ligação já cadastrada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "422", description = "Trajeto inexistente (INVALID_ROUTE)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "503", description = "Provedor de rotas indisponível (ROUTING_PROVIDER_UNAVAILABLE)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/edges")
    public ResponseEntity<RouteEdgeResponse> createEdge(@Valid @RequestBody CreateRouteEdgeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(graph.createEdge(request));
    }

    @Operation(summary = "Listar ligações da rede", description = "Disponível a usuários autenticados com papel de negócio.", security = @SecurityRequirement(name = "cookieAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ligações cadastradas", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = RouteEdgeResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Sessão ausente ou inválida", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Papel sem acesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/edges")
    public List<RouteEdgeResponse> listEdges() {
        return graph.listEdges();
    }
}

package com.login.controller.supermarket;

import com.login.dto.supermarket.EstoqueDto;
import com.login.service.supermarket.EstoqueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/supermercado/estoques")
@Tag(name = "Estoque", description = "API para gerenciamento de estoque de produtos")
@PreAuthorize("isAuthenticated()")
public class EstoqueController {

    @Autowired
    private EstoqueService estoqueService;

    @Operation(summary = "Criar registro de estoque", description = "Cria um novo registro de estoque para um produto")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Estoque criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "409", description = "Estoque já existe para este produto")
    })
    @PostMapping
    public ResponseEntity<EstoqueDto> criar(@Valid @RequestBody EstoqueDto dto) {
        EstoqueDto estoque = estoqueService.criar(dto);
        return new ResponseEntity<>(estoque, HttpStatus.CREATED);
    }

    @Operation(summary = "Atualizar estoque", description = "Atualiza informações do estoque")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estoque atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Estoque não encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EstoqueDto> atualizar(
            @Parameter(description = "ID do estoque") @PathVariable Long id,
            @Valid @RequestBody EstoqueDto dto) {
        EstoqueDto estoque = estoqueService.atualizar(id, dto);
        return ResponseEntity.ok(estoque);
    }

    @Operation(summary = "Buscar estoque por ID", description = "Retorna um registro de estoque específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estoque encontrado"),
        @ApiResponse(responseCode = "404", description = "Estoque não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EstoqueDto> buscarPorId(
            @Parameter(description = "ID do estoque") @PathVariable Long id) {
        EstoqueDto estoque = estoqueService.buscarPorId(id);
        return ResponseEntity.ok(estoque);
    }

    @Operation(summary = "Buscar estoque por produto", description = "Retorna o estoque de um produto específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estoque encontrado"),
        @ApiResponse(responseCode = "404", description = "Estoque não encontrado para este produto")
    })
    @GetMapping("/produto/{produtoId}")
    public ResponseEntity<EstoqueDto> buscarPorProdutoId(
            @Parameter(description = "ID do produto") @PathVariable Long produtoId) {
        EstoqueDto estoque = estoqueService.buscarPorProdutoId(produtoId);
        return ResponseEntity.ok(estoque);
    }

    @Operation(summary = "Listar todos os estoques", description = "Retorna uma lista com todos os registros de estoque")
    @ApiResponse(responseCode = "200", description = "Lista de estoques retornada com sucesso")
    @GetMapping
    public ResponseEntity<List<EstoqueDto>> listarTodos() {
        List<EstoqueDto> estoques = estoqueService.listarTodos();
        return ResponseEntity.ok(estoques);
    }

    @Operation(summary = "Listar estoques baixos", description = "Retorna produtos com estoque abaixo do mínimo")
    @ApiResponse(responseCode = "200", description = "Lista de estoques baixos retornada com sucesso")
    @GetMapping("/baixo")
    public ResponseEntity<List<EstoqueDto>> listarEstoqueBaixo() {
        List<EstoqueDto> estoques = estoqueService.listarEstoqueBaixo();
        return ResponseEntity.ok(estoques);
    }

    @Operation(summary = "Listar estoques excedidos", description = "Retorna produtos com estoque acima do máximo")
    @ApiResponse(responseCode = "200", description = "Lista de estoques excedidos retornada com sucesso")
    @GetMapping("/excedido")
    public ResponseEntity<List<EstoqueDto>> listarEstoqueExcedido() {
        List<EstoqueDto> estoques = estoqueService.listarEstoqueExcedido();
        return ResponseEntity.ok(estoques);
    }

    @Operation(summary = "Listar produtos vencidos", description = "Retorna produtos com data de validade expirada")
    @ApiResponse(responseCode = "200", description = "Lista de produtos vencidos retornada com sucesso")
    @GetMapping("/vencidos")
    public ResponseEntity<List<EstoqueDto>> listarProdutosVencidos() {
        List<EstoqueDto> estoques = estoqueService.listarProdutosVencidos();
        return ResponseEntity.ok(estoques);
    }

    @Operation(summary = "Buscar estoques por localização", description = "Lista todos os estoques de uma localização específica")
    @ApiResponse(responseCode = "200", description = "Lista de estoques retornada com sucesso")
    @GetMapping("/localizacao")
    public ResponseEntity<List<EstoqueDto>> buscarPorLocalizacao(
            @Parameter(description = "Localização do estoque") @RequestParam String localizacao) {
        List<EstoqueDto> estoques = estoqueService.buscarPorLocalizacao(localizacao);
        return ResponseEntity.ok(estoques);
    }

    @Operation(summary = "Adicionar ao estoque", description = "Adiciona quantidade ao estoque de um produto")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estoque atualizado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Estoque não encontrado para este produto")
    })
    @PatchMapping("/produto/{produtoId}/adicionar")
    public ResponseEntity<EstoqueDto> adicionarEstoque(
            @Parameter(description = "ID do produto") @PathVariable Long produtoId,
            @Parameter(description = "Quantidade a adicionar") @RequestParam Integer quantidade) {
        EstoqueDto estoque = estoqueService.adicionarEstoque(produtoId, quantidade);
        return ResponseEntity.ok(estoque);
    }

    @Operation(summary = "Remover do estoque", description = "Remove quantidade do estoque de um produto")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estoque atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Quantidade insuficiente no estoque"),
        @ApiResponse(responseCode = "404", description = "Estoque não encontrado para este produto")
    })
    @PatchMapping("/produto/{produtoId}/remover")
    public ResponseEntity<EstoqueDto> removerEstoque(
            @Parameter(description = "ID do produto") @PathVariable Long produtoId,
            @Parameter(description = "Quantidade a remover") @RequestParam Integer quantidade) {
        EstoqueDto estoque = estoqueService.removerEstoque(produtoId, quantidade);
        return ResponseEntity.ok(estoque);
    }

    @Operation(summary = "Deletar estoque", description = "Remove permanentemente um registro de estoque")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Estoque deletado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Estoque não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
            @Parameter(description = "ID do estoque") @PathVariable Long id) {
        estoqueService.deletar(id);
        return ResponseEntity.noContent().build();
    }
} 
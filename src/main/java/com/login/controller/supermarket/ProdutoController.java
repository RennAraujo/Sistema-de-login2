package com.login.controller.supermarket;

import com.login.dto.supermarket.ProdutoDto;
import com.login.service.supermarket.ProdutoService;
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

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/supermercado/produtos")
@Tag(name = "Produtos", description = "API para gerenciamento de produtos do supermercado")
@PreAuthorize("isAuthenticated()")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    @Operation(summary = "Criar novo produto", description = "Cadastra um novo produto no sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Produto criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "409", description = "Código de barras já existe")
    })
    @PostMapping
    public ResponseEntity<ProdutoDto> criar(@Valid @RequestBody ProdutoDto dto) {
        ProdutoDto produto = produtoService.criar(dto);
        return new ResponseEntity<>(produto, HttpStatus.CREATED);
    }

    @Operation(summary = "Atualizar produto", description = "Atualiza um produto existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProdutoDto> atualizar(
            @Parameter(description = "ID do produto") @PathVariable Long id,
            @Valid @RequestBody ProdutoDto dto) {
        ProdutoDto produto = produtoService.atualizar(id, dto);
        return ResponseEntity.ok(produto);
    }

    @Operation(summary = "Buscar produto por ID", description = "Retorna um produto específico pelo ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Produto encontrado"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProdutoDto> buscarPorId(
            @Parameter(description = "ID do produto") @PathVariable Long id) {
        ProdutoDto produto = produtoService.buscarPorId(id);
        return ResponseEntity.ok(produto);
    }

    @Operation(summary = "Buscar produto por código de barras", description = "Retorna um produto pelo seu código de barras")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Produto encontrado"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    @GetMapping("/codigo-barras/{codigoBarras}")
    public ResponseEntity<ProdutoDto> buscarPorCodigoBarras(
            @Parameter(description = "Código de barras do produto") @PathVariable String codigoBarras) {
        ProdutoDto produto = produtoService.buscarPorCodigoBarras(codigoBarras);
        return ResponseEntity.ok(produto);
    }

    @Operation(summary = "Listar todos os produtos", description = "Retorna uma lista com todos os produtos")
    @ApiResponse(responseCode = "200", description = "Lista de produtos retornada com sucesso")
    @GetMapping
    public ResponseEntity<List<ProdutoDto>> listarTodos() {
        List<ProdutoDto> produtos = produtoService.listarTodos();
        return ResponseEntity.ok(produtos);
    }

    @Operation(summary = "Listar produtos ativos", description = "Retorna apenas os produtos ativos")
    @ApiResponse(responseCode = "200", description = "Lista de produtos ativos retornada com sucesso")
    @GetMapping("/ativos")
    public ResponseEntity<List<ProdutoDto>> listarAtivos() {
        List<ProdutoDto> produtos = produtoService.listarAtivos();
        return ResponseEntity.ok(produtos);
    }

    @Operation(summary = "Buscar produtos por nome", description = "Busca produtos que contenham o nome especificado")
    @ApiResponse(responseCode = "200", description = "Lista de produtos retornada com sucesso")
    @GetMapping("/buscar")
    public ResponseEntity<List<ProdutoDto>> buscarPorNome(
            @Parameter(description = "Nome ou parte do nome do produto") @RequestParam String nome) {
        List<ProdutoDto> produtos = produtoService.buscarPorNome(nome);
        return ResponseEntity.ok(produtos);
    }

    @Operation(summary = "Buscar produtos por categoria", description = "Lista todos os produtos de uma categoria")
    @ApiResponse(responseCode = "200", description = "Lista de produtos retornada com sucesso")
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<ProdutoDto>> buscarPorCategoria(
            @Parameter(description = "ID da categoria") @PathVariable Long categoriaId) {
        List<ProdutoDto> produtos = produtoService.buscarPorCategoria(categoriaId);
        return ResponseEntity.ok(produtos);
    }

    @Operation(summary = "Buscar produtos por faixa de preço", description = "Lista produtos dentro de uma faixa de preço")
    @ApiResponse(responseCode = "200", description = "Lista de produtos retornada com sucesso")
    @GetMapping("/preco")
    public ResponseEntity<List<ProdutoDto>> buscarPorFaixaPreco(
            @Parameter(description = "Preço mínimo") @RequestParam BigDecimal precoMin,
            @Parameter(description = "Preço máximo") @RequestParam BigDecimal precoMax) {
        List<ProdutoDto> produtos = produtoService.buscarPorFaixaPreco(precoMin, precoMax);
        return ResponseEntity.ok(produtos);
    }

    @Operation(summary = "Listar produtos em promoção", description = "Retorna todos os produtos com preço promocional")
    @ApiResponse(responseCode = "200", description = "Lista de produtos em promoção retornada com sucesso")
    @GetMapping("/promocao")
    public ResponseEntity<List<ProdutoDto>> buscarProdutosEmPromocao() {
        List<ProdutoDto> produtos = produtoService.buscarProdutosEmPromocao();
        return ResponseEntity.ok(produtos);
    }

    @Operation(summary = "Deletar produto", description = "Remove permanentemente um produto do sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Produto deletado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
            @Parameter(description = "ID do produto") @PathVariable Long id) {
        produtoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Desativar produto", description = "Desativa um produto sem removê-lo do sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Produto desativado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    @PatchMapping("/{id}/desativar")
    public ResponseEntity<Void> desativar(
            @Parameter(description = "ID do produto") @PathVariable Long id) {
        produtoService.desativar(id);
        return ResponseEntity.noContent().build();
    }
} 
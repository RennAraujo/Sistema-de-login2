package com.login.controller.supermarket;

import com.login.dto.supermarket.CategoriaDto;
import com.login.service.supermarket.CategoriaService;
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
@RequestMapping("/api/supermercado/categorias")
@Tag(name = "Categorias", description = "API para gerenciamento de categorias de produtos")
@PreAuthorize("isAuthenticated()")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @Operation(summary = "Criar nova categoria", description = "Cria uma nova categoria de produtos")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Categoria criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "409", description = "Categoria já existe")
    })
    @PostMapping
    public ResponseEntity<CategoriaDto> criar(@Valid @RequestBody CategoriaDto dto) {
        CategoriaDto categoria = categoriaService.criar(dto);
        return new ResponseEntity<>(categoria, HttpStatus.CREATED);
    }

    @Operation(summary = "Atualizar categoria", description = "Atualiza uma categoria existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Categoria atualizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Categoria não encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CategoriaDto> atualizar(
            @Parameter(description = "ID da categoria") @PathVariable Long id,
            @Valid @RequestBody CategoriaDto dto) {
        CategoriaDto categoria = categoriaService.atualizar(id, dto);
        return ResponseEntity.ok(categoria);
    }

    @Operation(summary = "Buscar categoria por ID", description = "Retorna uma categoria específica pelo ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Categoria encontrada"),
        @ApiResponse(responseCode = "404", description = "Categoria não encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaDto> buscarPorId(
            @Parameter(description = "ID da categoria") @PathVariable Long id) {
        CategoriaDto categoria = categoriaService.buscarPorId(id);
        return ResponseEntity.ok(categoria);
    }

    @Operation(summary = "Listar todas as categorias", description = "Retorna uma lista com todas as categorias")
    @ApiResponse(responseCode = "200", description = "Lista de categorias retornada com sucesso")
    @GetMapping
    public ResponseEntity<List<CategoriaDto>> listarTodas() {
        List<CategoriaDto> categorias = categoriaService.listarTodas();
        return ResponseEntity.ok(categorias);
    }

    @Operation(summary = "Listar categorias ativas", description = "Retorna apenas as categorias ativas")
    @ApiResponse(responseCode = "200", description = "Lista de categorias ativas retornada com sucesso")
    @GetMapping("/ativas")
    public ResponseEntity<List<CategoriaDto>> listarAtivas() {
        List<CategoriaDto> categorias = categoriaService.listarAtivas();
        return ResponseEntity.ok(categorias);
    }

    @Operation(summary = "Buscar categorias por nome", description = "Busca categorias que contenham o nome especificado")
    @ApiResponse(responseCode = "200", description = "Lista de categorias retornada com sucesso")
    @GetMapping("/buscar")
    public ResponseEntity<List<CategoriaDto>> buscarPorNome(
            @Parameter(description = "Nome ou parte do nome da categoria") @RequestParam String nome) {
        List<CategoriaDto> categorias = categoriaService.buscarPorNome(nome);
        return ResponseEntity.ok(categorias);
    }

    @Operation(summary = "Deletar categoria", description = "Remove permanentemente uma categoria (apenas se não houver produtos associados)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Categoria deletada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Categoria não encontrada"),
        @ApiResponse(responseCode = "409", description = "Categoria possui produtos associados")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
            @Parameter(description = "ID da categoria") @PathVariable Long id) {
        categoriaService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Desativar categoria", description = "Desativa uma categoria sem removê-la do sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Categoria desativada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Categoria não encontrada")
    })
    @PatchMapping("/{id}/desativar")
    public ResponseEntity<Void> desativar(
            @Parameter(description = "ID da categoria") @PathVariable Long id) {
        categoriaService.desativar(id);
        return ResponseEntity.noContent().build();
    }
} 
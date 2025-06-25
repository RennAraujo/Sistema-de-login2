package com.login.service.supermarket;

import com.login.dto.supermarket.CategoriaDto;
import com.login.model.supermarket.Categoria;
import com.login.repository.supermarket.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    public CategoriaDto criar(CategoriaDto dto) {
        if (categoriaRepository.existsByNome(dto.getNome())) {
            throw new IllegalArgumentException("Já existe uma categoria com este nome");
        }

        Categoria categoria = new Categoria();
        categoria.setNome(dto.getNome());
        categoria.setDescricao(dto.getDescricao());
        categoria.setAtiva(dto.isAtiva());

        categoria = categoriaRepository.save(categoria);
        return converterParaDto(categoria);
    }

    public CategoriaDto atualizar(Long id, CategoriaDto dto) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada"));

        if (categoriaRepository.existsByNomeAndIdNot(dto.getNome(), id)) {
            throw new IllegalArgumentException("Já existe outra categoria com este nome");
        }

        categoria.setNome(dto.getNome());
        categoria.setDescricao(dto.getDescricao());
        categoria.setAtiva(dto.isAtiva());

        categoria = categoriaRepository.save(categoria);
        return converterParaDto(categoria);
    }

    @Transactional(readOnly = true)
    public CategoriaDto buscarPorId(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada"));
        return converterParaDto(categoria);
    }

    @Transactional(readOnly = true)
    public List<CategoriaDto> listarTodas() {
        return categoriaRepository.findAll().stream()
                .map(this::converterParaDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoriaDto> listarAtivas() {
        return categoriaRepository.findByAtivaTrue().stream()
                .map(this::converterParaDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoriaDto> buscarPorNome(String nome) {
        return categoriaRepository.findByNomeContainingIgnoreCaseAndAtivaTrue(nome).stream()
                .map(this::converterParaDto)
                .collect(Collectors.toList());
    }

    public void deletar(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada"));

        if (!categoria.getProdutos().isEmpty()) {
            throw new IllegalArgumentException("Não é possível deletar categoria com produtos associados");
        }

        categoriaRepository.delete(categoria);
    }

    public void desativar(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada"));
        
        categoria.setAtiva(false);
        categoriaRepository.save(categoria);
    }

    private CategoriaDto converterParaDto(Categoria categoria) {
        CategoriaDto dto = new CategoriaDto();
        dto.setId(categoria.getId());
        dto.setNome(categoria.getNome());
        dto.setDescricao(categoria.getDescricao());
        dto.setAtiva(categoria.isAtiva());
        return dto;
    }
} 
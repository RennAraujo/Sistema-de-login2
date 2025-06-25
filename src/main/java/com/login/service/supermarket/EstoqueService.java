package com.login.service.supermarket;

import com.login.dto.supermarket.EstoqueDto;
import com.login.model.supermarket.Estoque;
import com.login.model.supermarket.Produto;
import com.login.repository.supermarket.EstoqueRepository;
import com.login.repository.supermarket.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EstoqueService {

    @Autowired
    private EstoqueRepository estoqueRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    public EstoqueDto criar(EstoqueDto dto) {
        if (estoqueRepository.existsByProdutoId(dto.getProdutoId())) {
            throw new IllegalArgumentException("Já existe estoque para este produto");
        }

        Produto produto = produtoRepository.findById(dto.getProdutoId())
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"));

        Estoque estoque = new Estoque();
        estoque.setProduto(produto);
        estoque.setQuantidadeAtual(dto.getQuantidadeAtual());
        estoque.setQuantidadeMinima(dto.getQuantidadeMinima());
        estoque.setQuantidadeMaxima(dto.getQuantidadeMaxima());
        estoque.setLocalizacao(dto.getLocalizacao());
        estoque.setLote(dto.getLote());
        estoque.setDataValidade(dto.getDataValidade());

        estoque = estoqueRepository.save(estoque);
        return converterParaDto(estoque);
    }

    public EstoqueDto atualizar(Long id, EstoqueDto dto) {
        Estoque estoque = estoqueRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Estoque não encontrado"));

        estoque.setQuantidadeAtual(dto.getQuantidadeAtual());
        estoque.setQuantidadeMinima(dto.getQuantidadeMinima());
        estoque.setQuantidadeMaxima(dto.getQuantidadeMaxima());
        estoque.setLocalizacao(dto.getLocalizacao());
        estoque.setLote(dto.getLote());
        estoque.setDataValidade(dto.getDataValidade());

        estoque = estoqueRepository.save(estoque);
        return converterParaDto(estoque);
    }

    @Transactional(readOnly = true)
    public EstoqueDto buscarPorId(Long id) {
        Estoque estoque = estoqueRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Estoque não encontrado"));
        return converterParaDto(estoque);
    }

    @Transactional(readOnly = true)
    public EstoqueDto buscarPorProdutoId(Long produtoId) {
        Estoque estoque = estoqueRepository.findByProdutoId(produtoId)
                .orElseThrow(() -> new IllegalArgumentException("Estoque não encontrado para este produto"));
        return converterParaDto(estoque);
    }

    @Transactional(readOnly = true)
    public List<EstoqueDto> listarTodos() {
        return estoqueRepository.findAll().stream()
                .map(this::converterParaDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EstoqueDto> listarEstoqueBaixo() {
        return estoqueRepository.findEstoquesComNivelBaixo().stream()
                .map(this::converterParaDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EstoqueDto> listarEstoqueExcedido() {
        return estoqueRepository.findEstoquesExcedidos().stream()
                .map(this::converterParaDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EstoqueDto> listarProdutosVencidos() {
        return estoqueRepository.findProdutosVencidos().stream()
                .map(this::converterParaDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EstoqueDto> buscarPorLocalizacao(String localizacao) {
        return estoqueRepository.findByLocalizacao(localizacao).stream()
                .map(this::converterParaDto)
                .collect(Collectors.toList());
    }

    public EstoqueDto adicionarEstoque(Long produtoId, Integer quantidade) {
        Estoque estoque = estoqueRepository.findByProdutoId(produtoId)
                .orElseThrow(() -> new IllegalArgumentException("Estoque não encontrado para este produto"));

        estoque.adicionarEstoque(quantidade);
        estoque = estoqueRepository.save(estoque);
        return converterParaDto(estoque);
    }

    public EstoqueDto removerEstoque(Long produtoId, Integer quantidade) {
        Estoque estoque = estoqueRepository.findByProdutoId(produtoId)
                .orElseThrow(() -> new IllegalArgumentException("Estoque não encontrado para este produto"));

        estoque.removerEstoque(quantidade);
        estoque = estoqueRepository.save(estoque);
        return converterParaDto(estoque);
    }

    public void deletar(Long id) {
        Estoque estoque = estoqueRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Estoque não encontrado"));
        estoqueRepository.delete(estoque);
    }

    private EstoqueDto converterParaDto(Estoque estoque) {
        EstoqueDto dto = new EstoqueDto();
        dto.setId(estoque.getId());
        dto.setProdutoId(estoque.getProduto().getId());
        dto.setProdutoNome(estoque.getProduto().getNome());
        dto.setQuantidadeAtual(estoque.getQuantidadeAtual());
        dto.setQuantidadeMinima(estoque.getQuantidadeMinima());
        dto.setQuantidadeMaxima(estoque.getQuantidadeMaxima());
        dto.setLocalizacao(estoque.getLocalizacao());
        dto.setLote(estoque.getLote());
        dto.setDataValidade(estoque.getDataValidade());
        dto.setUltimaEntrada(estoque.getUltimaEntrada());
        dto.setUltimaSaida(estoque.getUltimaSaida());
        dto.setEstoqueBaixo(estoque.isEstoqueBaixo());
        dto.setEstoqueExcedido(estoque.isEstoqueExcedido());
        return dto;
    }
} 
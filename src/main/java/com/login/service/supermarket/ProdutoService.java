package com.login.service.supermarket;

import com.login.dto.supermarket.ProdutoDto;
import com.login.model.supermarket.Categoria;
import com.login.model.supermarket.Estoque;
import com.login.model.supermarket.Produto;
import com.login.repository.supermarket.CategoriaRepository;
import com.login.repository.supermarket.EstoqueRepository;
import com.login.repository.supermarket.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private EstoqueRepository estoqueRepository;

    public ProdutoDto criar(ProdutoDto dto) {
        if (produtoRepository.existsByCodigoBarras(dto.getCodigoBarras())) {
            throw new IllegalArgumentException("Já existe um produto com este código de barras");
        }

        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada"));

        Produto produto = new Produto();
        produto.setNome(dto.getNome());
        produto.setDescricao(dto.getDescricao());
        produto.setCodigoBarras(dto.getCodigoBarras());
        produto.setPreco(dto.getPreco());
        produto.setPrecoPromocional(dto.getPrecoPromocional());
        produto.setUnidadeMedida(dto.getUnidadeMedida());
        produto.setCategoria(categoria);
        produto.setAtivo(dto.isAtivo());
        produto.setImagemUrl(dto.getImagemUrl());

        produto = produtoRepository.save(produto);

        // Criar estoque inicial
        Estoque estoque = new Estoque();
        estoque.setProduto(produto);
        estoque.setQuantidadeAtual(0);
        estoque.setQuantidadeMinima(10); // Valor padrão
        estoqueRepository.save(estoque);

        return converterParaDto(produto);
    }

    public ProdutoDto atualizar(Long id, ProdutoDto dto) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"));

        if (produtoRepository.existsByCodigoBarrasAndIdNot(dto.getCodigoBarras(), id)) {
            throw new IllegalArgumentException("Já existe outro produto com este código de barras");
        }

        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada"));

        produto.setNome(dto.getNome());
        produto.setDescricao(dto.getDescricao());
        produto.setCodigoBarras(dto.getCodigoBarras());
        produto.setPreco(dto.getPreco());
        produto.setPrecoPromocional(dto.getPrecoPromocional());
        produto.setUnidadeMedida(dto.getUnidadeMedida());
        produto.setCategoria(categoria);
        produto.setAtivo(dto.isAtivo());
        produto.setImagemUrl(dto.getImagemUrl());

        produto = produtoRepository.save(produto);
        return converterParaDto(produto);
    }

    @Transactional(readOnly = true)
    public ProdutoDto buscarPorId(Long id) {
        Produto produto = produtoRepository.findByIdWithEstoque(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"));
        return converterParaDto(produto);
    }

    @Transactional(readOnly = true)
    public ProdutoDto buscarPorCodigoBarras(String codigoBarras) {
        Produto produto = produtoRepository.findByCodigoBarras(codigoBarras)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"));
        return converterParaDto(produto);
    }

    @Transactional(readOnly = true)
    public List<ProdutoDto> listarTodos() {
        return produtoRepository.findAll().stream()
                .map(this::converterParaDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProdutoDto> listarAtivos() {
        return produtoRepository.findByAtivoTrue().stream()
                .map(this::converterParaDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProdutoDto> buscarPorNome(String nome) {
        return produtoRepository.findByNomeContainingIgnoreCaseAndAtivoTrue(nome).stream()
                .map(this::converterParaDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProdutoDto> buscarPorCategoria(Long categoriaId) {
        return produtoRepository.findByCategoriaIdAndAtivoTrue(categoriaId).stream()
                .map(this::converterParaDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProdutoDto> buscarPorFaixaPreco(BigDecimal precoMin, BigDecimal precoMax) {
        return produtoRepository.findByPrecoBetweenAndAtivoTrue(precoMin, precoMax).stream()
                .map(this::converterParaDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProdutoDto> buscarProdutosEmPromocao() {
        return produtoRepository.findProdutosEmPromocao().stream()
                .map(this::converterParaDto)
                .collect(Collectors.toList());
    }

    public void deletar(Long id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"));

        // Deletar estoque associado
        estoqueRepository.findByProdutoId(id).ifPresent(estoqueRepository::delete);
        
        produtoRepository.delete(produto);
    }

    public void desativar(Long id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"));
        
        produto.setAtivo(false);
        produtoRepository.save(produto);
    }

    private ProdutoDto converterParaDto(Produto produto) {
        ProdutoDto dto = new ProdutoDto();
        dto.setId(produto.getId());
        dto.setNome(produto.getNome());
        dto.setDescricao(produto.getDescricao());
        dto.setCodigoBarras(produto.getCodigoBarras());
        dto.setPreco(produto.getPreco());
        dto.setPrecoPromocional(produto.getPrecoPromocional());
        dto.setUnidadeMedida(produto.getUnidadeMedida());
        dto.setCategoriaId(produto.getCategoria().getId());
        dto.setCategoriaNome(produto.getCategoria().getNome());
        dto.setAtivo(produto.isAtivo());
        dto.setImagemUrl(produto.getImagemUrl());
        
        if (produto.getEstoque() != null) {
            dto.setQuantidadeEstoque(produto.getEstoque().getQuantidadeAtual());
            dto.setEstoqueBaixo(produto.getEstoque().isEstoqueBaixo());
        }
        
        return dto;
    }
} 
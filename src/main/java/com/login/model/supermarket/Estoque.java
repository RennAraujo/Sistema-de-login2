package com.login.model.supermarket;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "estoques")
public class Estoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", unique = true)
    @NotNull(message = "Produto é obrigatório")
    private Produto produto;

    @NotNull(message = "Quantidade atual é obrigatória")
    @Min(value = 0, message = "Quantidade não pode ser negativa")
    @Column(name = "quantidade_atual", nullable = false)
    private Integer quantidadeAtual;

    @NotNull(message = "Quantidade mínima é obrigatória")
    @Min(value = 0, message = "Quantidade mínima não pode ser negativa")
    @Column(name = "quantidade_minima", nullable = false)
    private Integer quantidadeMinima;

    @Min(value = 0, message = "Quantidade máxima não pode ser negativa")
    @Column(name = "quantidade_maxima")
    private Integer quantidadeMaxima;

    @Column(name = "localizacao")
    private String localizacao;

    @Column(name = "lote")
    private String lote;

    @Column(name = "data_validade")
    private LocalDateTime dataValidade;

    @Column(name = "ultima_entrada")
    private LocalDateTime ultimaEntrada;

    @Column(name = "ultima_saida")
    private LocalDateTime ultimaSaida;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Estoque() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Estoque(Produto produto, Integer quantidadeAtual, Integer quantidadeMinima) {
        this();
        this.produto = produto;
        this.quantidadeAtual = quantidadeAtual;
        this.quantidadeMinima = quantidadeMinima;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Métodos de negócio
    public boolean isEstoqueBaixo() {
        return quantidadeAtual <= quantidadeMinima;
    }

    public boolean isEstoqueExcedido() {
        return quantidadeMaxima != null && quantidadeAtual > quantidadeMaxima;
    }

    public void adicionarEstoque(Integer quantidade) {
        this.quantidadeAtual += quantidade;
        this.ultimaEntrada = LocalDateTime.now();
    }

    public void removerEstoque(Integer quantidade) {
        if (this.quantidadeAtual >= quantidade) {
            this.quantidadeAtual -= quantidade;
            this.ultimaSaida = LocalDateTime.now();
        } else {
            throw new IllegalArgumentException("Quantidade insuficiente no estoque");
        }
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public Integer getQuantidadeAtual() {
        return quantidadeAtual;
    }

    public void setQuantidadeAtual(Integer quantidadeAtual) {
        this.quantidadeAtual = quantidadeAtual;
    }

    public Integer getQuantidadeMinima() {
        return quantidadeMinima;
    }

    public void setQuantidadeMinima(Integer quantidadeMinima) {
        this.quantidadeMinima = quantidadeMinima;
    }

    public Integer getQuantidadeMaxima() {
        return quantidadeMaxima;
    }

    public void setQuantidadeMaxima(Integer quantidadeMaxima) {
        this.quantidadeMaxima = quantidadeMaxima;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public LocalDateTime getDataValidade() {
        return dataValidade;
    }

    public void setDataValidade(LocalDateTime dataValidade) {
        this.dataValidade = dataValidade;
    }

    public LocalDateTime getUltimaEntrada() {
        return ultimaEntrada;
    }

    public void setUltimaEntrada(LocalDateTime ultimaEntrada) {
        this.ultimaEntrada = ultimaEntrada;
    }

    public LocalDateTime getUltimaSaida() {
        return ultimaSaida;
    }

    public void setUltimaSaida(LocalDateTime ultimaSaida) {
        this.ultimaSaida = ultimaSaida;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Estoque{" +
                "id=" + id +
                ", produto=" + (produto != null ? produto.getNome() : "null") +
                ", quantidadeAtual=" + quantidadeAtual +
                ", quantidadeMinima=" + quantidadeMinima +
                ", estoqueBaixo=" + isEstoqueBaixo() +
                '}';
    }
} 
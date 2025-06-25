package com.login.dto.supermarket;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class EstoqueDto {
    
    private Long id;
    
    @NotNull(message = "Produto é obrigatório")
    private Long produtoId;
    
    private String produtoNome;
    
    @NotNull(message = "Quantidade atual é obrigatória")
    @Min(value = 0, message = "Quantidade não pode ser negativa")
    private Integer quantidadeAtual;
    
    @NotNull(message = "Quantidade mínima é obrigatória")
    @Min(value = 0, message = "Quantidade mínima não pode ser negativa")
    private Integer quantidadeMinima;
    
    @Min(value = 0, message = "Quantidade máxima não pode ser negativa")
    private Integer quantidadeMaxima;
    
    private String localizacao;
    
    private String lote;
    
    private LocalDateTime dataValidade;
    
    private LocalDateTime ultimaEntrada;
    
    private LocalDateTime ultimaSaida;
    
    private boolean estoqueBaixo;
    
    private boolean estoqueExcedido;

    // Construtores
    public EstoqueDto() {}

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProdutoId() {
        return produtoId;
    }

    public void setProdutoId(Long produtoId) {
        this.produtoId = produtoId;
    }

    public String getProdutoNome() {
        return produtoNome;
    }

    public void setProdutoNome(String produtoNome) {
        this.produtoNome = produtoNome;
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

    public boolean isEstoqueBaixo() {
        return estoqueBaixo;
    }

    public void setEstoqueBaixo(boolean estoqueBaixo) {
        this.estoqueBaixo = estoqueBaixo;
    }

    public boolean isEstoqueExcedido() {
        return estoqueExcedido;
    }

    public void setEstoqueExcedido(boolean estoqueExcedido) {
        this.estoqueExcedido = estoqueExcedido;
    }
} 
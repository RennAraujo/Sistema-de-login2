package com.login.repository.supermarket;

import com.login.model.supermarket.Estoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstoqueRepository extends JpaRepository<Estoque, Long> {
    
    Optional<Estoque> findByProdutoId(Long produtoId);
    
    @Query("SELECT e FROM Estoque e WHERE e.quantidadeAtual <= e.quantidadeMinima")
    List<Estoque> findEstoquesComNivelBaixo();
    
    @Query("SELECT e FROM Estoque e WHERE e.quantidadeMaxima IS NOT NULL AND e.quantidadeAtual > e.quantidadeMaxima")
    List<Estoque> findEstoquesExcedidos();
    
    @Query("SELECT e FROM Estoque e WHERE e.dataValidade IS NOT NULL AND e.dataValidade <= CURRENT_TIMESTAMP")
    List<Estoque> findProdutosVencidos();
    
    @Query("SELECT e FROM Estoque e WHERE e.localizacao = :localizacao")
    List<Estoque> findByLocalizacao(@Param("localizacao") String localizacao);
    
    boolean existsByProdutoId(Long produtoId);
} 
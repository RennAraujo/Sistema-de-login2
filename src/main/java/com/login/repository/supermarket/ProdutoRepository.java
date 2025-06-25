package com.login.repository.supermarket;

import com.login.model.supermarket.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    
    Optional<Produto> findByCodigoBarras(String codigoBarras);
    
    List<Produto> findByAtivoTrue();
    
    List<Produto> findByCategoriaIdAndAtivoTrue(Long categoriaId);
    
    List<Produto> findByNomeContainingIgnoreCaseAndAtivoTrue(String nome);
    
    @Query("SELECT p FROM Produto p WHERE p.preco BETWEEN :precoMin AND :precoMax AND p.ativo = true")
    List<Produto> findByPrecoBetweenAndAtivoTrue(@Param("precoMin") BigDecimal precoMin, 
                                                  @Param("precoMax") BigDecimal precoMax);
    
    @Query("SELECT p FROM Produto p WHERE p.precoPromocional IS NOT NULL AND p.ativo = true")
    List<Produto> findProdutosEmPromocao();
    
    boolean existsByCodigoBarras(String codigoBarras);
    
    boolean existsByCodigoBarrasAndIdNot(String codigoBarras, Long id);
    
    @Query("SELECT p FROM Produto p LEFT JOIN FETCH p.estoque WHERE p.id = :id")
    Optional<Produto> findByIdWithEstoque(@Param("id") Long id);
} 
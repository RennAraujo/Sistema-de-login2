package com.login.repository.supermarket;

import com.login.model.supermarket.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    
    Optional<Categoria> findByNome(String nome);
    
    List<Categoria> findByAtivaTrue();
    
    List<Categoria> findByNomeContainingIgnoreCaseAndAtivaTrue(String nome);
    
    boolean existsByNome(String nome);
    
    boolean existsByNomeAndIdNot(String nome, Long id);
} 
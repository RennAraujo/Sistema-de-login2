package com.login.repository.supermarket;

import com.login.model.supermarket.Fornecedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FornecedorRepository extends JpaRepository<Fornecedor, Long> {
    
    Optional<Fornecedor> findByCnpj(String cnpj);
    
    Optional<Fornecedor> findByEmail(String email);
    
    List<Fornecedor> findByAtivoTrue();
    
    List<Fornecedor> findByNomeContainingIgnoreCaseAndAtivoTrue(String nome);
    
    List<Fornecedor> findByCidadeAndAtivoTrue(String cidade);
    
    List<Fornecedor> findByEstadoAndAtivoTrue(String estado);
    
    boolean existsByCnpj(String cnpj);
    
    boolean existsByCnpjAndIdNot(String cnpj, Long id);
    
    boolean existsByEmail(String email);
    
    boolean existsByEmailAndIdNot(String email, Long id);
} 
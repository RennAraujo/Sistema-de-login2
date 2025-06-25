package com.login.config;

import com.login.model.supermarket.*;
import com.login.repository.supermarket.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
@Profile("!test")
public class DataInitializer {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private EstoqueRepository estoqueRepository;

    @Autowired
    private FornecedorRepository fornecedorRepository;

    @Bean
    public CommandLineRunner initDatabase() {
        return args -> {
            // Criar categorias
            Categoria alimentos = new Categoria("Alimentos", "Produtos alimentícios em geral");
            Categoria bebidas = new Categoria("Bebidas", "Bebidas diversas");
            Categoria limpeza = new Categoria("Limpeza", "Produtos de limpeza e higiene");
            Categoria hortifruti = new Categoria("Hortifruti", "Frutas, verduras e legumes");
            
            categoriaRepository.save(alimentos);
            categoriaRepository.save(bebidas);
            categoriaRepository.save(limpeza);
            categoriaRepository.save(hortifruti);

            // Criar fornecedores
            Fornecedor fornecedor1 = new Fornecedor("Distribuidora Silva", "12345678901234", "11987654321");
            fornecedor1.setEmail("contato@distsilva.com");
            fornecedor1.setCidade("São Paulo");
            fornecedor1.setEstado("SP");
            
            Fornecedor fornecedor2 = new Fornecedor("Alimentos Brasil Ltda", "98765432109876", "11912345678");
            fornecedor2.setEmail("vendas@alimentosbrasil.com");
            fornecedor2.setCidade("Rio de Janeiro");
            fornecedor2.setEstado("RJ");
            
            fornecedorRepository.save(fornecedor1);
            fornecedorRepository.save(fornecedor2);

            // Criar produtos - Alimentos
            Produto arroz = new Produto("Arroz Branco Tipo 1 5kg", "7891234567890", new BigDecimal("28.90"), alimentos);
            arroz.setDescricao("Arroz branco tipo 1, pacote de 5kg");
            arroz.setUnidadeMedida("kg");
            arroz.setPrecoPromocional(new BigDecimal("25.90"));
            
            Produto feijao = new Produto("Feijão Carioca 1kg", "7891234567891", new BigDecimal("8.50"), alimentos);
            feijao.setDescricao("Feijão carioca tipo 1, pacote de 1kg");
            feijao.setUnidadeMedida("kg");
            
            Produto macarrao = new Produto("Macarrão Espaguete 500g", "7891234567892", new BigDecimal("4.90"), alimentos);
            macarrao.setDescricao("Massa de sêmola de trigo");
            macarrao.setUnidadeMedida("g");
            
            // Criar produtos - Bebidas
            Produto refrigerante = new Produto("Refrigerante Cola 2L", "7891234567893", new BigDecimal("7.90"), bebidas);
            refrigerante.setDescricao("Refrigerante sabor cola, garrafa 2 litros");
            refrigerante.setUnidadeMedida("L");
            refrigerante.setPrecoPromocional(new BigDecimal("6.90"));
            
            Produto suco = new Produto("Suco de Laranja 1L", "7891234567894", new BigDecimal("5.50"), bebidas);
            suco.setDescricao("Suco de laranja integral");
            suco.setUnidadeMedida("L");
            
            // Criar produtos - Limpeza
            Produto detergente = new Produto("Detergente Neutro 500ml", "7891234567895", new BigDecimal("2.90"), limpeza);
            detergente.setDescricao("Detergente líquido neutro");
            detergente.setUnidadeMedida("ml");
            
            Produto sabao = new Produto("Sabão em Pó 1kg", "7891234567896", new BigDecimal("12.90"), limpeza);
            sabao.setDescricao("Sabão em pó para roupas");
            sabao.setUnidadeMedida("kg");
            
            // Criar produtos - Hortifruti
            Produto banana = new Produto("Banana Prata", "7891234567897", new BigDecimal("4.90"), hortifruti);
            banana.setDescricao("Banana prata madura");
            banana.setUnidadeMedida("kg");
            
            Produto tomate = new Produto("Tomate Salada", "7891234567898", new BigDecimal("6.90"), hortifruti);
            tomate.setDescricao("Tomate tipo salada");
            tomate.setUnidadeMedida("kg");
            
            // Salvar produtos
            produtoRepository.save(arroz);
            produtoRepository.save(feijao);
            produtoRepository.save(macarrao);
            produtoRepository.save(refrigerante);
            produtoRepository.save(suco);
            produtoRepository.save(detergente);
            produtoRepository.save(sabao);
            produtoRepository.save(banana);
            produtoRepository.save(tomate);

            // Criar estoques
            Estoque estoqueArroz = new Estoque(arroz, 150, 50);
            estoqueArroz.setQuantidadeMaxima(500);
            estoqueArroz.setLocalizacao("A1-P1");
            
            Estoque estoqueFeijao = new Estoque(feijao, 80, 30);
            estoqueFeijao.setQuantidadeMaxima(200);
            estoqueFeijao.setLocalizacao("A1-P2");
            
            Estoque estoqueMacarrao = new Estoque(macarrao, 200, 50);
            estoqueMacarrao.setQuantidadeMaxima(400);
            estoqueMacarrao.setLocalizacao("A1-P3");
            
            Estoque estoqueRefrigerante = new Estoque(refrigerante, 100, 40);
            estoqueRefrigerante.setQuantidadeMaxima(300);
            estoqueRefrigerante.setLocalizacao("B1-P1");
            
            Estoque estoqueSuco = new Estoque(suco, 60, 20);
            estoqueSuco.setQuantidadeMaxima(150);
            estoqueSuco.setLocalizacao("B1-P2");
            
            Estoque estoqueDetergente = new Estoque(detergente, 25, 30); // Estoque baixo
            estoqueDetergente.setQuantidadeMaxima(200);
            estoqueDetergente.setLocalizacao("C1-P1");
            
            Estoque estoqueSabao = new Estoque(sabao, 40, 20);
            estoqueSabao.setQuantidadeMaxima(100);
            estoqueSabao.setLocalizacao("C1-P2");
            
            Estoque estoqueBanana = new Estoque(banana, 30, 10);
            estoqueBanana.setQuantidadeMaxima(50);
            estoqueBanana.setLocalizacao("D1-P1");
            estoqueBanana.setDataValidade(LocalDateTime.now().plusDays(5));
            
            Estoque estoqueTomate = new Estoque(tomate, 20, 10);
            estoqueTomate.setQuantidadeMaxima(40);
            estoqueTomate.setLocalizacao("D1-P2");
            estoqueTomate.setDataValidade(LocalDateTime.now().plusDays(7));
            
            // Salvar estoques
            estoqueRepository.save(estoqueArroz);
            estoqueRepository.save(estoqueFeijao);
            estoqueRepository.save(estoqueMacarrao);
            estoqueRepository.save(estoqueRefrigerante);
            estoqueRepository.save(estoqueSuco);
            estoqueRepository.save(estoqueDetergente);
            estoqueRepository.save(estoqueSabao);
            estoqueRepository.save(estoqueBanana);
            estoqueRepository.save(estoqueTomate);

            // Associar fornecedores aos produtos
            arroz.getFornecedores().add(fornecedor1);
            feijao.getFornecedores().add(fornecedor1);
            macarrao.getFornecedores().add(fornecedor1);
            refrigerante.getFornecedores().add(fornecedor2);
            suco.getFornecedores().add(fornecedor2);
            
            fornecedor1.getProdutos().add(arroz);
            fornecedor1.getProdutos().add(feijao);
            fornecedor1.getProdutos().add(macarrao);
            fornecedor2.getProdutos().add(refrigerante);
            fornecedor2.getProdutos().add(suco);
            
            // Salvar associações
            produtoRepository.save(arroz);
            produtoRepository.save(feijao);
            produtoRepository.save(macarrao);
            produtoRepository.save(refrigerante);
            produtoRepository.save(suco);
            
            System.out.println("===========================================");
            System.out.println("Dados de exemplo do supermercado criados!");
            System.out.println("===========================================");
            System.out.println("Categorias: " + categoriaRepository.count());
            System.out.println("Produtos: " + produtoRepository.count());
            System.out.println("Estoques: " + estoqueRepository.count());
            System.out.println("Fornecedores: " + fornecedorRepository.count());
            System.out.println("Produtos com estoque baixo: " + estoqueRepository.findEstoquesComNivelBaixo().size());
            System.out.println("===========================================");
        };
    }
} 
package vn.edu.hcmute.fashion.catalog.repository;

import vn.edu.hcmute.fashion.catalog.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import org.springframework.stereotype.Repository;
@Repository("catalogProductRepository")
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    boolean existsByCategoryId(Long categoryId);
    boolean existsByBrandId(Long brandId);
}

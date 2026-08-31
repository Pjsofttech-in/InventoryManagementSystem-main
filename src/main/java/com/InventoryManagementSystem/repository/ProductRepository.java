package com.InventoryManagementSystem.repository;

import com.InventoryManagementSystem.DTO.TopProductDTO;
import com.InventoryManagementSystem.entity.InventoryProduct;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<InventoryProduct, Long> {

    @Query("SELECT p FROM InventoryProduct p WHERE " +
            "(:productName IS NULL OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :productName, '%'))) AND " +
            "(:productTypeId IS NULL OR p.productType.id = :productTypeId) AND " +
            "(:brandId IS NULL OR p.brand.id = :brandId) AND " +
            "(:vendorId IS NULL OR p.vendor.id = :vendorId) AND " +
            "(:unitTypeId IS NULL OR p.unitType.id = :unitTypeId) " +
            "ORDER BY p.id DESC")
    List<InventoryProduct> filterProducts(
            @Param("productName") String productName,
            @Param("productTypeId") Long productTypeId,
            @Param("brandId") Long brandId,
            @Param("vendorId") Long vendorId,
            @Param("unitTypeId") Long unitTypeId
    );

    List<InventoryProduct> findAllByOrderByIdDesc();

    @Query("SELECT COALESCE(SUM(p.stock), 0) FROM InventoryProduct p")
    Integer findTotalStockQuantity();

    @Query("SELECT p FROM InventoryProduct p WHERE p.stock <= :threshold")
    List<InventoryProduct> findLowStockProducts(@Param("threshold") Integer threshold);

    @Query("SELECT COUNT(p) FROM InventoryProduct p WHERE p.stock <= :threshold")
    Long countLowStockProducts(@Param("threshold") Integer threshold);

    // Fixed: Changed item.product.name to item.product.productName
    @Query("SELECT new com.InventoryManagementSystem.DTO.TopProductDTO(item.product.productName, SUM(item.quantity)) " +
            "FROM InventoryOrderItem item GROUP BY item.product.productName ORDER BY SUM(item.quantity) DESC")
    List<TopProductDTO> findTopSellingProducts(Pageable pageable);
}
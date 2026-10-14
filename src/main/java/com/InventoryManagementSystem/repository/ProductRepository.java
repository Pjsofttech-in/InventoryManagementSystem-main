//package com.InventoryManagementSystem.repository;
//
//import com.InventoryManagementSystem.DTO.TopProductDTO;
//import com.InventoryManagementSystem.DTO.TopCategoryDTO;
//import com.InventoryManagementSystem.entity.InventoryProduct;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
//@Repository
//public interface ProductRepository extends JpaRepository<InventoryProduct, Long> {
//
//    @Query("SELECT p FROM InventoryProduct p WHERE " +
//            "(:productName IS NULL OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :productName, '%'))) AND " +
//            "(:productTypeId IS NULL OR p.productType.id = :productTypeId) AND " +
//            "(:brandId IS NULL OR p.brand.id = :brandId) AND " +
//            "(:vendorId IS NULL OR p.vendor.id = :vendorId) AND " +
//            "(:unitTypeId IS NULL OR p.unitType.id = :unitTypeId) AND " +
//            "(:subCategoryId IS NULL OR p.subCategory.id = :subCategoryId) " +
//            "ORDER BY p.id DESC")
//    List<InventoryProduct> filterProducts(
//            @Param("productName") String productName,
//            @Param("productTypeId") Long productTypeId,
//            @Param("brandId") Long brandId,
//            @Param("vendorId") Long vendorId,
//            @Param("unitTypeId") Long unitTypeId,
//            @Param("subCategoryId") Long subCategoryId
//    );
//
//    // Active products that use a sub category (a sub category in use cannot be deleted).
//    long countBySubCategoryId(Long subCategoryId);
//
//    List<InventoryProduct> findAllByOrderByIdDesc();
//
//    @Query("SELECT COALESCE(SUM(p.stock), 0) FROM InventoryProduct p")
//    Integer findTotalStockQuantity();
//
//    @Query("SELECT p FROM InventoryProduct p WHERE p.stock <= :threshold")
//    List<InventoryProduct> findLowStockProducts(@Param("threshold") Integer threshold);
//
//    @Query("SELECT COUNT(p) FROM InventoryProduct p WHERE p.stock <= :threshold")
//    Long countLowStockProducts(@Param("threshold") Integer threshold);
//
//    // Fixed: Changed item.product.name to item.product.productName
//    @Query("SELECT new com.InventoryManagementSystem.DTO.TopProductDTO(item.product.productName, SUM(item.quantity)) " +
//            "FROM InventoryOrderItem item GROUP BY item.product.productName ORDER BY SUM(item.quantity) DESC")
//    List<TopProductDTO> findTopSellingProducts(Pageable pageable);
//
//    // Top selling product type / category, by total quantity sold.
//    @Query("SELECT new com.InventoryManagementSystem.DTO.TopCategoryDTO(item.product.productType.typeName, SUM(item.quantity)) " +
//            "FROM InventoryOrderItem item WHERE item.product.productType IS NOT NULL " +
//            "GROUP BY item.product.productType.typeName ORDER BY SUM(item.quantity) DESC")
//    List<TopCategoryDTO> findTopSellingCategories(Pageable pageable);
//}


package com.InventoryManagementSystem.repository;

import com.InventoryManagementSystem.DTO.TopProductDTO;
import com.InventoryManagementSystem.DTO.TopCategoryDTO;
import com.InventoryManagementSystem.entity.InventoryProduct;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<InventoryProduct, Long> {

    // LEFT JOINs: products without a sub category / brand / unit type must still be listed
    // when that filter is empty (a plain `p.subCategory.id` path makes Hibernate use an INNER JOIN).
    @Query("SELECT p FROM InventoryProduct p " +
            "LEFT JOIN p.productType pt LEFT JOIN p.brand b LEFT JOIN p.vendor v " +
            "LEFT JOIN p.unitType ut LEFT JOIN p.subCategory sc WHERE " +
            "(:productName IS NULL OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :productName, '%'))) AND " +
            "(:productTypeId IS NULL OR pt.id = :productTypeId) AND " +
            "(:brandId IS NULL OR b.id = :brandId) AND " +
            "(:vendorId IS NULL OR v.id = :vendorId) AND " +
            "(:unitTypeId IS NULL OR ut.id = :unitTypeId) AND " +
            "(:subCategoryId IS NULL OR sc.id = :subCategoryId) " +
            "ORDER BY p.id DESC")
    List<InventoryProduct> filterProducts(
            @Param("productName") String productName,
            @Param("productTypeId") Long productTypeId,
            @Param("brandId") Long brandId,
            @Param("vendorId") Long vendorId,
            @Param("unitTypeId") Long unitTypeId,
            @Param("subCategoryId") Long subCategoryId
    );

    // Active products that use a sub category (a sub category in use cannot be deleted).
    long countBySubCategoryId(Long subCategoryId);

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

    // Top selling product type / category, by total quantity sold.
    @Query("SELECT new com.InventoryManagementSystem.DTO.TopCategoryDTO(item.product.productType.typeName, SUM(item.quantity)) " +
            "FROM InventoryOrderItem item WHERE item.product.productType IS NOT NULL " +
            "GROUP BY item.product.productType.typeName ORDER BY SUM(item.quantity) DESC")
    List<TopCategoryDTO> findTopSellingCategories(Pageable pageable);
}
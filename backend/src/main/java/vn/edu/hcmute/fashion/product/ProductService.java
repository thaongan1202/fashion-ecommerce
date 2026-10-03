package vn.edu.hcmute.fashion.product;

import java.util.List;
import java.math.BigDecimal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service("customerProductService")
public class ProductService {
    private final ProductRepository repository;
    public ProductService(ProductRepository repository) { this.repository = repository; }

    public ProductPage list(String query, Long categoryId, Long brandId, BigDecimal minPrice,
            BigDecimal maxPrice, int page, int size, String sort) {
        if ((minPrice != null && minPrice.signum() < 0) || (maxPrice != null && maxPrice.signum() < 0)
                || (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid price range");
        }
        if (page < 0 || size < 1 || size > 100) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page phải từ 0; size từ 1 đến 100");
        String[] parts = sort == null ? new String[] {"createdAt", "desc"} : sort.split(",", -1);
        if (parts.length != 2 || !List.of("createdAt", "name", "price").contains(parts[0])
                || !List.of("asc", "desc").contains(parts[1].toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sort phải là createdAt,name hoặc price kết hợp asc|desc");
        }
        String column = switch (parts[0]) { case "name" -> "p.name"; case "price" -> "min_price"; default -> "p.created_at"; };
        String direction = parts[1].toUpperCase();
        long total = repository.count(query, categoryId, brandId, minPrice, maxPrice);
        int offset = Math.toIntExact((long) page * size);
        var items = repository.findPage(query, categoryId, brandId, minPrice, maxPrice, column, direction, size, offset);
        return new ProductPage(items, total, size, page,
                total == 0 ? 0 : (int) Math.ceil((double) total / size));
    }

    public List<CategoryOption> categories() { return repository.findCategories(); }
    public PriceRange priceRange() { return repository.findPriceRange(); }

    public ProductResponse get(long id) {
        var product = repository.findById(id);
        if (product == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm");
        return product;
    }

    public List<ProductResponse> related(long id) {
        get(id);
        return repository.findRelated(id, 8);
    }

    public record ProductPage(List<ProductResponse> content, long totalElements, int size, int number, int totalPages) {}
    public record CategoryOption(long id, String name) {}
    public record PriceRange(BigDecimal min, BigDecimal max) {}
}

package gift.repository;

import gift.entity.Wish;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class WishlistRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public WishlistRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("wishlist_items")
                .usingGeneratedKeyColumns("id");
    }

    public Wish addProduct(Wish wish) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("member_id", wish.getMemberId());
        parameters.put("product_id", wish.getProductId());
        parameters.put("product_number", wish.getProductNumber());
        Number newId = simpleJdbcInsert.executeAndReturnKey(parameters);
        wish.setId(newId.longValue());
        return wish;
    }

    public Optional<Wish> findWishByMemberIdAndProductId(Long memberId, Long productId) {
        String sql = "SELECT * FROM wishlist_items WHERE member_id = ? AND product_id = ?";
        List<Wish> wishes = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Long id = rs.getLong("id");
            int productNumber = rs.getInt("product_number");
            return new Wish(id, memberId, productId, productNumber);
        }, memberId, productId);

        return wishes.stream().findFirst();
    }

    public Wish addOrUpdateProduct(Wish wish) {
        Optional<Wish> existingWish = findWishByMemberIdAndProductId(wish.getMemberId(), wish.getProductId());

        if (existingWish.isPresent()) {
            Wish foundWish = existingWish.get();
            String updateSql = "UPDATE wishlist_items SET product_number = ? WHERE id = ?";
            jdbcTemplate.update(updateSql, foundWish.getProductNumber() + wish.getProductNumber(), foundWish.getId());
            foundWish.setProductNumber(foundWish.getProductNumber() + wish.getProductNumber());
            return foundWish;
        } else {
            return addProduct(wish);
        }
    }

    public List<Wish> getProductsByMemberId(Long memberId) {
        String sql = "SELECT * FROM wishlist_items WHERE member_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Long id = rs.getLong("id");
            Long productId = rs.getLong("product_id");
            int productNumber = rs.getInt("product_number");
            return new Wish(id, memberId, productId, productNumber);
        }, memberId);
    }

    public void deleteItem(Long id) {
        String sql = "DELETE FROM wishlist_items WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}

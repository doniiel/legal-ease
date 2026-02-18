package kz.legeal.ease.backend.repository.specification;

import org.springframework.data.jpa.domain.Specification;

public class GenericSpecification {

    public static <T> Specification<T> equalsTo(String field, Object value) {
        return (root, query, cb) -> cb.equal(root.get(field), value);
    }

    public static <T> Specification<T> notEqualsTo(String field, Object value) {
        return (root, query, cb) -> cb.notEqual(root.get(field), value);
    }

    public static <T> Specification<T> likeIgnoreCase(String field, String value) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get(field)), "%" + value.toLowerCase() + "%");
    }

    public static <T, Y extends Comparable<? super Y>> Specification<T> greaterOrEqual(String field, Y value) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get(field), value);
    }

    public static <T, Y extends Comparable<? super Y>> Specification<T> lessOrEqual(String field, Y value) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get(field), value);
    }

    public static <T> Specification<T> in(String field, Object... values) {
        return (root, query, cb) -> root.get(field).in(values);
    }

    public static <T> Specification<T> notIn(String field, Object... values) {
        return (root, query, cb) -> cb.not(root.get(field).in(values));
    }
}

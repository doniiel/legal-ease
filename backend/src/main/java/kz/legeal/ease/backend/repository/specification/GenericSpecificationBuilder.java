package kz.legeal.ease.backend.repository.specification;

import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class GenericSpecificationBuilder<T> {

    private final List<Specification<T>> specs = new ArrayList<>();

    public GenericSpecificationBuilder<T> eq(String field, Object value) {
        if (value != null) specs.add(GenericSpecification.equalsTo(field, value));
        return this;
    }

    public GenericSpecificationBuilder<T> notEq(String field, Object value) {
        if (value != null) specs.add(GenericSpecification.notEqualsTo(field, value));
        return this;
    }

    public GenericSpecificationBuilder<T> like(String field, String value) {
        if (value != null && !value.isEmpty()) specs.add(GenericSpecification.likeIgnoreCase(field, value));
        return this;
    }

    public <Y extends Comparable<? super Y>> GenericSpecificationBuilder<T> gte(String field, Y value) {
        if (value != null) specs.add(GenericSpecification.greaterOrEqual(field, value));
        return this;
    }

    public <Y extends Comparable<? super Y>> GenericSpecificationBuilder<T> lte(String field, Y value) {
        if (value != null) specs.add(GenericSpecification.lessOrEqual(field, value));
        return this;
    }

    public GenericSpecificationBuilder<T> in(String field, Object... values) {
        if (values != null && values.length > 0) specs.add(GenericSpecification.in(field, values));
        return this;
    }

    public GenericSpecificationBuilder<T> notIn(String field, Object... values) {
        if (values != null && values.length > 0) specs.add(GenericSpecification.notIn(field, values));
        return this;
    }

    public Specification<T> build() {
        Specification<T> result = null;
        for (Specification<T> s : specs) {
            result = result == null ? s : result.and(s);
        }
        return result;
    }
}

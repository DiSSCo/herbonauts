package services;


import org.hibernate.metamodel.domain.PluralAttribute;
import play.db.jpa.JPA;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

public class JPAUtils {

    public static interface PredicateBuilder<T> {
        Predicate buildPredicate(CriteriaBuilder cb, Root<T> root);
    }

    public static interface Converter<S, D> {
        D convert(S o);
    }

    public static <S, D> Page<D> convertPage(Class<D> destType, Page<S> srcPage, Converter<S, D> converter) {

        List<D> destData = new ArrayList<D>();
        for (S o : srcPage.getList()) {
            destData.add(converter.convert(o));
        }

        return new PageImpl(destData, srcPage.getTotalRowCount(),
                srcPage.getPageIndex(), srcPage.getPageSize(), srcPage.getSortBy(),
                srcPage.getOrder());
    }


    public static <T> Page<T> getPage(Class<T> type, Integer page, Integer pageSize) {
        return getPage(type, page, pageSize, null, null, null);
    }

    public static <T> Page<T> getPage(Class<T> type, Integer page, Integer pageSize, String sortBy, String order) {
        return getPage(type, page, pageSize, null, sortBy, order);
    }

    public static <T> Page<T> getPage(Class<T> type, Integer page, Integer pageSize, PredicateBuilder predicateBuilder) {
        return getPage(type, page, pageSize, predicateBuilder, null, null);
    }

    public static <T> Page<T> getPage(Class<T> type, Integer page, Integer pageSize, PredicateBuilder predicateBuilder, String sortBy, String order) {

        if(page < 1) {
            page = 1;
        }

        Root<T> from;
        Predicate predicate = null;
        List<T> data;
        {
            CriteriaBuilder builder = JPA.em().getCriteriaBuilder();

            CriteriaQuery<T> cq = builder.createQuery(type);
            from = cq.from(type);

            if (predicateBuilder != null) {
                predicate = predicateBuilder.buildPredicate(builder, from);

            }

            // Select query
            CriteriaQuery<T> select = cq.select(from);
            if (predicate != null) {
                cq.where(predicate);
            }

            Path<Object> sortPath = null;
            if (sortBy != null) {
                String[] sortPathRaw = sortBy.split("\\.");
                sortPath = from.get(sortPathRaw[0]);

                if (sortPathRaw.length > 1) {
                    for (int i = 1; i < sortPathRaw.length; i++) {
                        sortPath = sortPath.get(sortPathRaw[i]);
                    }
                }
            }

            if (sortPath != null) {
                if ("desc".equals(order)) {
                    cq.orderBy(builder.desc(sortPath)); //from.get(sortBy)));
                } else {
                    cq.orderBy(builder.asc(sortPath)); //from.get(sortBy)));
                }
            }

            // Execute select
            data = JPA.em()
                    .createQuery(select)
                    .setFirstResult((page - 1) * pageSize)
                    .setMaxResults(pageSize)
                    .getResultList();

        }

        Long total;
        {
            CriteriaBuilder builder = JPA.em().getCriteriaBuilder();

            CriteriaQuery<Long> cq = builder.createQuery(Long.class);

            // Count query
            CriteriaQuery<Long> count = cq.select(builder.count(cq.from(type)));
            if (predicate != null) {
                cq.where(predicate);
            }

            // Execute select
            total = JPA.em()
                    .createQuery(count)
                    .getSingleResult();

        }

        return new PageImpl(data, total, page, pageSize, sortBy, order);
    }

}

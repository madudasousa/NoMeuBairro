package com.meubairro.api.specification;

import com.meubairro.api.domain.estab.Estab;
import com.meubairro.api.dto.request.FiltroEstabRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class EstabSpecification {

    //monta o filtro basenado nos campos que o frontend enviou
    public static Specification<Estab> comfiltros (FiltroEstabRequest filtro){
        return (root, query, cb) ->{
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isTrue(root.get("active")));

            if (filtro.name() != null && !filtro.name().isBlank()){
                predicates.add(cb.like(
                        cb.lower(root.get("name")), "%" + filtro.name().toLowerCase() + "%"
                ));
            }
            if (filtro.categorySlug() != null && !filtro.categorySlug().isBlank()){
                predicates.add(cb.equal(
                        root.get("category").get("slug"), filtro.categorySlug()
                ));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}

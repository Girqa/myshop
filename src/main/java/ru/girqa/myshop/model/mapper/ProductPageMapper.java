package ru.girqa.myshop.model.mapper;

import java.util.ArrayList;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.Named;
import ru.girqa.myshop.model.domain.Product;
import ru.girqa.myshop.model.domain.ProductPageRequest;
import ru.girqa.myshop.model.domain.Product_;
import ru.girqa.myshop.model.domain.search.LikeValueSearch;
import ru.girqa.myshop.model.domain.search.SearchCriteria;
import ru.girqa.myshop.model.domain.sort.ProductSort;
import ru.girqa.myshop.model.domain.sort.ProductSortParam;
import ru.girqa.myshop.model.dto.product.ProductPageRequestDto;

@Mapper(componentModel = ComponentModel.SPRING, uses = ProductMapper.class)
public interface ProductPageMapper {

  @Mapping(target = "sorts", source = "dto", qualifiedByName = "extractSorts")
  @Mapping(target = "filters", source = "dto", qualifiedByName = "extractProductFilters")
  @Mapping(target = "page", expression = "java(dto.getPage() - 1)")
  ProductPageRequest toDomain(ProductPageRequestDto dto);

  @Named("extractSorts")
  default List<ProductSort> extractSorts(ProductPageRequestDto dto) {
    List<ProductSort> sorts = new ArrayList<>();
    if (dto.getNameSort() != null) {
      sorts.add(new ProductSort(ProductSortParam.NAME, dto.getNameSort()));
    }
    if (dto.getPriceSort() != null) {
      sorts.add(new ProductSort(ProductSortParam.PRICE, dto.getPriceSort()));
    }
    return sorts;
  }

  @Named("extractProductFilters")
  default List<SearchCriteria<Product>> extractProductFilters(ProductPageRequestDto dto) {
    List<SearchCriteria<Product>> filters = new ArrayList<>();
    if (dto.getSearchName() != null && !dto.getSearchName().isBlank()) {
      filters.add(new LikeValueSearch<>(Product_.name.getName(), dto.getSearchName().trim()));
    }
    return filters;
  }
}

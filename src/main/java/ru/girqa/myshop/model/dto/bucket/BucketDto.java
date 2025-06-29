package ru.girqa.myshop.model.dto.bucket;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.girqa.myshop.model.dto.product.ProductPreviewDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BucketDto {

  private Long bucketId;
  private List<ProductPreviewDto> products;
}

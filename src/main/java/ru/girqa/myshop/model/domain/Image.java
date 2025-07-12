package ru.girqa.myshop.model.domain;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("images")
@Getter
@Setter
@SuperBuilder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Image extends BaseEntity {

  @Size(min = 1)
  @Column("image_name")
  private String name;

  @Positive
  @Column("image_size")
  private Long size;

  @Column("image_data")
  private byte[] data;

}

package ru.girqa.myshop.model.domain;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.builder.ToStringExclude;

@Entity
@Table(name = "images")
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Image extends BaseEntity {

  @Size(min = 1)
  @Column(name = "image_name", nullable = false)
  private String name;

  @Positive
  @Column(name = "image_size", nullable = false)
  private Long size;

  @ToStringExclude
  @Basic(fetch = FetchType.LAZY)
  @Column(name = "image_data", nullable = false)
  private byte[] data;

}

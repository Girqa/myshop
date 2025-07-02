package ru.girqa.myshop.model.mapper;

import java.io.IOException;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;
import org.springframework.web.multipart.MultipartFile;
import ru.girqa.myshop.exception.InvalidImageException;
import ru.girqa.myshop.model.domain.Image;

@Mapper(componentModel = ComponentModel.SPRING)
public interface ImageMapper {

  default Image toDomain(MultipartFile file) {
    try {
      return Image.builder()
          .name(file.getOriginalFilename())
          .size(file.getSize())
          .data(file.getBytes())
          .build();
    } catch (IOException e) {
      throw new InvalidImageException();
    }
  }

}

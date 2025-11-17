package com.oneday.core.dto.classes;

import com.oneday.core.entity.Classes;

public record ClassMainResponseDto(
    int classId,
    String className,
    String teacherName,
    int price,
    String representativeImageUrl,
    String location,
    String categoryName
) {
    public static ClassMainResponseDto of(Classes entity, String imageUrl) {
        return new ClassMainResponseDto(
            entity.getClassId(),
            entity.getClassName(),
            entity.getTeacher().getName(),
            entity.getPrice(),
            imageUrl,
            entity.getLocation(),
            entity.getCategory().getCategory()
        );
    }
}

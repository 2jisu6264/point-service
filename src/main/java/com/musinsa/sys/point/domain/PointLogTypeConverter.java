package com.musinsa.sys.point.domain;
import com.musinsa.sys.point.domain.PointLogType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class PointLogTypeConverter
        implements AttributeConverter<PointLogType, String> {

    @Override
    public String convertToDatabaseColumn(PointLogType attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public PointLogType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : PointLogType.from(dbData);
    }
}

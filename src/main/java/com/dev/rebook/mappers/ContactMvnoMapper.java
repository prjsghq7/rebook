package com.dev.rebook.mappers;

import com.dev.rebook.entities.ContactMvnoEntity;
import com.dev.rebook.entities.UserEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ContactMvnoMapper {
    ContactMvnoEntity[] selectAll();

    int selectCountByCode(String code);
}

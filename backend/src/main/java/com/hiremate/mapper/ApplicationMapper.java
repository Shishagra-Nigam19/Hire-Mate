package com.hiremate.mapper;

import com.hiremate.domain.entity.Application;
import com.hiremate.dto.application.ApplicationResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {JobMapper.class, UserMapper.class})
public interface ApplicationMapper {

    ApplicationResponse toApplicationResponse(Application application);
}

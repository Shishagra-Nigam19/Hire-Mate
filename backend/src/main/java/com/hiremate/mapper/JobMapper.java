package com.hiremate.mapper;

import com.hiremate.domain.entity.Job;
import com.hiremate.dto.job.JobCreateRequest;
import com.hiremate.dto.job.JobResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface JobMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "postedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Job toEntity(JobCreateRequest request);

    JobResponse toJobResponse(Job job);
}

package com.taskmanager.repo;

import com.taskmanager.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    /**
     * Use explicit JPQL: {@code Containing} on a collection of {@link Long} can map to SQL {@code LIKE}
     * and return no rows. {@code MEMBER OF} is correct for element-collection membership.
     */
    @Query("SELECT p FROM Project p WHERE :userId MEMBER OF p.memberIds")
    List<Project> findAllWhereMember(@Param("userId") Long userId);
}

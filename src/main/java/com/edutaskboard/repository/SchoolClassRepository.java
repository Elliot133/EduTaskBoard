package com.edutaskboard.repository;

import com.edutaskboard.model.SchoolClass;
import com.edutaskboard.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;

@Repository
public interface SchoolClassRepository extends JpaRepository<SchoolClass, Long> {
    List<SchoolClass> findByClassTeacher(User teacher);

    @Query("SELECT c FROM SchoolClass c LEFT JOIN FETCH c.classTeacher")
    List<SchoolClass> findAllWithTeacher();
}

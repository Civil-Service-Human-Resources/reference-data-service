package uk.gov.cshr.rds.repository;

import java.util.Optional;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.rds.model.Department;

@Repository
public interface DepartmentRepository extends PagingAndSortingRepository<Department, Long> {

    default Optional<Department> findById(Long id) {
        return Optional.ofNullable(this.findOne(id));
    }
}

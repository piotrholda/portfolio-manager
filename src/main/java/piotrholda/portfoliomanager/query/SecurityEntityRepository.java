package piotrholda.portfoliomanager.query;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecurityEntityRepository extends JpaRepository<SecurityEntity, String> {

}

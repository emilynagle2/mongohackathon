package mongoHackathon.Main.repository;

import jakarta.transaction.Transactional;
import mongoHackathon.Main.model.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;

@Transactional
public interface CustomerRepository extends MongoRepository<Customer, String> {
}

package mongoHackathon.Main.service;

import lombok.extern.slf4j.Slf4j;
import mongoHackathon.Main.model.Customer;
import mongoHackathon.Main.repository.CustomerRepository;
import org.jvnet.hk2.annotations.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
@Slf4j
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;
    // Assuming you have a CustomerRepository interface

    public Customer createCustomer(Customer customer) {
        // Implement logic to create a new customer
        return customerRepository.save(customer);
    }

    public Customer getCustomerById(String customerId) {
        return customerRepository.findById(customerId).orElse(null);
    }

    public Customer updateCustomer(String customerId, Customer updatedCustomer) {
        // Implement logic to update a customer
        Customer existingCustomer = customerRepository.findById(customerId).orElse(null);
        if (existingCustomer != null) {
            // Update the customer properties
            return customerRepository.save(existingCustomer);
        }
        return null; // Customer not found
    }
    
}

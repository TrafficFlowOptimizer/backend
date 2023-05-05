package app.backend.service;

import app.backend.entity.Customer;
import app.backend.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository repository;

    public void addCustomer() {
        this.repository.save(new Customer("Adam", "Something"));
    }

    public void addCustomer(String firstName, String lastName) {
        this.repository.save(new Customer(firstName, lastName));
    }

    public void getAll() {
        for(Customer customer : this.repository.findAll()) {
            System.out.println(customer);
        }
    }

}

package app.backend.controller.customer;

import app.backend.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @PostMapping(value="/customers")
    public String upload(@RequestParam("firstName") String firstName, @RequestParam("lastName")String lastName) {
        this.customerService.addCustomer(firstName, lastName);

        return firstName + lastName;
    }

    @GetMapping(value="/customers")
    public String list() {
        this.customerService.getAll();

        return "listed";
    }
}

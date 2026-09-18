package com.ecom.inventory.controller;

import com.ecom.inventory.dto.common.PaginationDto;
import com.ecom.inventory.dto.common.ResponseDto;
import com.ecom.inventory.dto.customer.CustomerQ;
import com.ecom.inventory.dto.customer.CustomerSaveDto;
import com.ecom.inventory.dto.customer.CustomerStatusDto;
import com.ecom.inventory.dto.product.ProductQ;
import com.ecom.inventory.entity.Customer;
import com.ecom.inventory.exception.UserInputValidationException;
import com.ecom.inventory.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

//    @GetMapping
//    public Page<Customer> list(@RequestParam(required = false) String keyword,
//                               @PageableDefault(size = 20, sort = "customerName") Pageable pageable) {
//        return customerService.getCustomerList(keyword, pageable);
//    }

    @GetMapping
    public ResponseDto getProductList(CustomerQ params) throws UserInputValidationException, Exception {
        ResponseDto ret = new ResponseDto();
//        Long campusId = jwtDecoder.getJwtDetail(request).getCampusId();
        PaginationDto retList = customerService.getCustomerList(params);
        ret.setData(retList);
        return ret;
    }

    @GetMapping("/{id}")
    public ResponseDto getById(@PathVariable Long id) throws UserInputValidationException {
        ResponseDto ret = new ResponseDto();
        Customer customer = customerService.getCustomerById(id);
        ret.setData(customer);
        return ret;
    }

    @PostMapping
    public String create(@RequestBody CustomerSaveDto dto) throws UserInputValidationException {
        return customerService.saveOrUpdateCustomer(dto);
    }

    @PutMapping("/{id}")
    public String update(@PathVariable Long id, @RequestBody CustomerSaveDto dto)
            throws UserInputValidationException {
        dto.setId(id);
        return customerService.saveOrUpdateCustomer(dto);
    }

    @PatchMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestBody CustomerStatusDto dto)
            throws UserInputValidationException {
        dto.setId(id);
        return customerService.updateCustomerStatus(dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) throws UserInputValidationException {
        customerService.deleteCustomer(id);
    }
}

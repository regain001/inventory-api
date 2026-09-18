package com.ecom.inventory.service;

import com.ecom.inventory.dto.common.PaginationDto;
import com.ecom.inventory.dto.customer.CustomerDto;
import com.ecom.inventory.dto.customer.CustomerQ;
import com.ecom.inventory.dto.customer.CustomerSaveDto;
import com.ecom.inventory.dto.customer.CustomerStatusDto;
import com.ecom.inventory.entity.Customer;
import com.ecom.inventory.exception.UserInputValidationException;
import com.ecom.inventory.repository.CustomerRepository;
import com.ecom.inventory.repository.SalesOrderRepository;
import com.ecom.inventory.util.db.DtoResultTransformer;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final SalesOrderRepository salesOrderRepository; // usage check on delete
    private final EntityManager em;

    @Transactional
    public String saveOrUpdateCustomer(CustomerSaveDto dto) throws UserInputValidationException {
        // TODO: role check (ADMIN only)

        if (dto.getCustomerName() == null || dto.getCustomerName().trim().isEmpty()) {
            throw new UserInputValidationException("Customer name cannot be empty");
        }

        Customer customer;

        if (dto.getId() != null && dto.getId() > 0) {
            customer = customerRepository.findById(dto.getId())
                    .orElseThrow(() -> new UserInputValidationException("Customer not found with id: " + dto.getId()));
        } else {
            customer = new Customer();
            customer.setCreatedAt(LocalDateTime.now());
        }

        customer.setCustomerName(dto.getCustomerName().trim());
        customer.setCustomerType(dto.getCustomerType().trim().toUpperCase());
        customer.setPhone(dto.getPhone());
        customer.setAddress(dto.getAddress());
        customer.setActive(dto.getActive() != null ? dto.getActive() : true);
        customer.setUpdatedAt(LocalDateTime.now());

        customerRepository.save(customer);
        return "Customer saved successfully";
    }


    public PaginationDto getCustomerList(CustomerQ params) throws UserInputValidationException, Exception {
        Session session = em.unwrap(Session.class);
        validateGetCustomerList(params);

        PaginationDto ret = new PaginationDto();

        try {
            String queryStr = "SELECT c.id customerId,\n" +
                    "c.customer_name customerName,\n" +
                    "c.customer_type customerType,\n" +
                    "c.phone phone,\n" +
                    "c.address address,\n" +
                    "c.active active,\n" +
                    "c.created_at createdAt,\n" +
                    "c.updated_at updatedAt\n";

            queryStr += getCustomerListQueryBody(params);
            queryStr += "ORDER BY c.customer_name ASC LIMIT " + params.getLimit() + " OFFSET " + params.getStart();

            Integer totalRecords = getCustomerListCount(params);

            List<CustomerDto> list = session.createNativeQuery(queryStr)
                    .setResultTransformer(new DtoResultTransformer(CustomerDto.class))
                    .list();

            ret.setTotalRecords(totalRecords);
            ret.setFetchedRecords(list.size());
            ret.setStart(params.getStart());
            ret.setLimit(params.getLimit());
            ret.setRecords(list);
            return ret;

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private String getCustomerListQueryBody(CustomerQ params) {
        String queryStr = "FROM customer c\n" +
                "WHERE 1=1 \n";

        if (params.getKeyword() != null && !params.getKeyword().trim().isEmpty()) {
            String kw = params.getKeyword().trim();
            queryStr += "AND (c.customer_name ILIKE '%" + kw + "%' OR c.phone ILIKE '%" + kw + "%' OR c.address ILIKE '%" + kw + "%') \n";
        }
        if (params.getCustomerType() != null && !params.getCustomerType().trim().isEmpty()) {
            queryStr += "AND c.customer_type = '" + params.getCustomerType().trim() + "' \n";
        }
        if (params.getActive() != null) {
            queryStr += "AND c.active = " + params.getActive() + " \n";
        }

        return queryStr;
    }

    public Integer getCustomerListCount(CustomerQ params) {
        try {
            String queryStr = "SELECT CAST(COUNT(1) AS INTEGER) AS totalCount\n";
            queryStr += getCustomerListQueryBody(params);

            Session session = em.unwrap(Session.class);
            NativeQuery query = session.createNativeQuery(queryStr);
            Object count = query.uniqueResult();
            if (count == null) {
                return 0;
            }
            return Integer.parseInt(count.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void validateGetCustomerList(CustomerQ params) throws UserInputValidationException {
        UserInputValidationException e = new UserInputValidationException();
        if (params.getStart() == null) {
            e.addErrorMessage("Customer", "Start can't be empty");
        }
        if (params.getLimit() == null) {
            e.addErrorMessage("Customer", "Limit can't be empty");
        }
        if (e.isValidationErrorOccured()) {
            throw e;
        }
    }

    public Page<Customer> getCustomerList(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return customerRepository.findAll(pageable);
        }
        return customerRepository.findByCustomerNameContainingIgnoreCase(keyword.trim(), pageable);
    }

    public Customer getCustomerById(Long id) throws UserInputValidationException {
        return customerRepository.findById(id)
                .orElseThrow(() -> new UserInputValidationException("Customer not found with id: " + id));
    }

    @Transactional
    public String updateCustomerStatus(CustomerStatusDto dto) throws UserInputValidationException {
        // TODO: role check (ADMIN only)
        if (dto.getActive() == null) {
            throw new UserInputValidationException("active flag is required");
        }
        Customer customer = customerRepository.findById(dto.getId())
                .orElseThrow(() -> new UserInputValidationException("Customer not found with id: " + dto.getId()));
        customer.setActive(dto.getActive());
        customerRepository.save(customer);
        return dto.getActive() ? "Customer activated successfully." : "Customer deactivated successfully.";
    }

    @Transactional
    public void deleteCustomer(Long id) throws UserInputValidationException {
        // TODO: role check (ADMIN only)
        if (customerRepository.findById(id).isEmpty()) {
            throw new UserInputValidationException("Customer not found with id: " + id);
        }

        long usageCount = salesOrderRepository.countByCustomerId(id);
        if (usageCount > 0) {
            throw new UserInputValidationException(
                    "This customer has " + usageCount + " sales order(s). Deactivate it instead.");
        }

        customerRepository.deleteById(id);
    }
}
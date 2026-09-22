package com.ecom.inventory.controller;

// package + imports for your own project classes omitted
import com.ecom.inventory.dto.common.PaginationDto;
import com.ecom.inventory.dto.common.ResponseDto;
import com.ecom.inventory.dto.sales_order.SalesOrderQ;
import com.ecom.inventory.dto.sales_order.SalesOrderSaveDto;
import com.ecom.inventory.exception.UserInputValidationException;
import com.ecom.inventory.service.SalesOrderService;
import com.ecom.inventory.util.db.PojoTransformerException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales-orders")
@RequiredArgsConstructor
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    @GetMapping
    public PaginationDto list(SalesOrderQ params) throws UserInputValidationException, PojoTransformerException {
        return salesOrderService.getSalesOrderList(params);
    }

    @GetMapping("/{id}")
    public ResponseDto getById(@PathVariable Long id) throws UserInputValidationException, PojoTransformerException {
        ResponseDto ret = new ResponseDto();
        ret.setData(salesOrderService.getSalesOrderById(id));
        return ret;
    }

    @PostMapping
    public ResponseDto create(@RequestBody SalesOrderSaveDto dto) throws UserInputValidationException {
        dto.setSalesOrderId(null);
        Long id = salesOrderService.saveOrUpdateSalesOrder(dto);
        ResponseDto ret = new ResponseDto();
        ret.setMessage("Sales order saved successfully");
        ret.setData(id);
        return ret;
    }

    @PutMapping("/{id}")
    public ResponseDto update(@PathVariable Long id, @RequestBody SalesOrderSaveDto dto)
            throws UserInputValidationException {
        dto.setSalesOrderId(id);
        Long savedId = salesOrderService.saveOrUpdateSalesOrder(dto);
        ResponseDto ret = new ResponseDto();
        ret.setMessage("Sales order updated successfully");
        ret.setData(savedId);
        return ret;
    }

    @PostMapping("/{id}/complete")
    public ResponseDto complete(@PathVariable Long id) throws UserInputValidationException {
        ResponseDto ret = new ResponseDto();
        ret.setMessage(salesOrderService.completeSalesOrder(id));
        return ret;
    }

    @DeleteMapping("/{id}")
    public ResponseDto delete(@PathVariable Long id) throws UserInputValidationException {
        salesOrderService.deleteSalesOrder(id);
        ResponseDto ret = new ResponseDto();
        ret.setMessage("Sales order deleted successfully");
        return ret;
    }
}
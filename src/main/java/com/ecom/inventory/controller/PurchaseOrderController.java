package com.ecom.inventory.controller;

import com.ecom.inventory.dto.purchase_order.PurchaseOrderDto;
import com.ecom.inventory.dto.purchase_order.PurchaseOrderQ;
import com.ecom.inventory.dto.purchase_order.PurchaseOrderSaveDto;
import com.ecom.inventory.service.PurchaseOrderService;
import org.springframework.web.bind.annotation.RestController;
import com.ecom.inventory.dto.common.PaginationDto;
import com.ecom.inventory.dto.common.ResponseDto;
import com.ecom.inventory.dto.product.ProductQ;
import com.ecom.inventory.dto.product.ProductSaveDto;
import com.ecom.inventory.dto.product.ProductStatusDto;
import com.ecom.inventory.entity.Product;
import com.ecom.inventory.exception.UserInputValidationException;
import com.ecom.inventory.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;


    @GetMapping
    public PaginationDto getPurchaseOrderList(PurchaseOrderQ params) throws UserInputValidationException, Exception {
        return purchaseOrderService.getPurchaseOrderList(params);
    }

    @PostMapping
    public ResponseDto create(@RequestBody PurchaseOrderSaveDto dto) throws UserInputValidationException {
        Long id = purchaseOrderService.saveOrUpdatePurchaseOrder(dto);
        ResponseDto ret = new ResponseDto();
        ret.setMessage("Purchase order saved successfully");
        ret.setData(id);
        return ret;
    }

    @PutMapping("/{id}")
    public ResponseDto update(@PathVariable Long id, @RequestBody PurchaseOrderSaveDto dto) throws UserInputValidationException {
        dto.setPurchaseOrderId(id);
        Long savedId = purchaseOrderService.saveOrUpdatePurchaseOrder(dto);
        ResponseDto ret = new ResponseDto();
        ret.setMessage("Purchase order updated successfully");
        ret.setData(savedId);
        return ret;
    }

    @GetMapping("/{id}")
    public ResponseDto getById(@PathVariable Long id) throws UserInputValidationException {
        ResponseDto ret = new ResponseDto();
        PurchaseOrderDto po = purchaseOrderService.getPurchaseOrderById(id);
        ret.setData(po);
        return ret;
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) throws UserInputValidationException {
        purchaseOrderService.deletePurchaseOrder(id);
        return "Purchase order deleted successfully";
    }
}

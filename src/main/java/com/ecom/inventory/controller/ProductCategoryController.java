package com.ecom.inventory.controller;

import com.ecom.inventory.dto.common.ResponseDto;
import com.ecom.inventory.dto.product.ProductCategorySaveDto;
import com.ecom.inventory.dto.product.ProductCategoryStatusDto;
import com.ecom.inventory.entity.Product;
import com.ecom.inventory.entity.ProductCategory;
import com.ecom.inventory.exception.UserInputValidationException;
import com.ecom.inventory.service.ProductCategoryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-categories")
@RequiredArgsConstructor
public class ProductCategoryController {

    private final ProductCategoryService productCategoryService;


    @GetMapping
    public ResponseDto getList(HttpServletRequest request)
            throws UserInputValidationException {

        ResponseDto ret = new ResponseDto();

        try {
            List<ProductCategory> data = productCategoryService.getCategoryList();
            ret.setData(data);
            return ret;
        } catch (Exception e) {
            ret.setError(e);
            e.printStackTrace();
        }

        return ret;
    }

    @GetMapping("/{id}")
    public ResponseDto getById(@PathVariable Long id, HttpServletRequest request)
            throws UserInputValidationException {

        ResponseDto ret = new ResponseDto();

        try {
            ProductCategory data = productCategoryService.getCategoryById(id);
            ret.setData(data);
            return ret;
        } catch (Exception e) {
            ret.setError(e);
            e.printStackTrace();
        }

        return ret;
    }

    @PostMapping
    public ResponseDto create(@RequestBody ProductCategorySaveDto dto, HttpServletRequest request)
            throws UserInputValidationException {

        ResponseDto ret = new ResponseDto();

        try {
            String msg = productCategoryService.saveOrUpdateCategory(dto);

            ret.setSuccess();
            ret.setMessage(msg);
            return ret;
        } catch (Exception e) {
            ret.setError(e);
            e.printStackTrace();
        }

        return ret;
    }

    @PutMapping("/{id}")
    public ResponseDto update(@PathVariable Long id,
                              @RequestBody ProductCategorySaveDto dto,
                              HttpServletRequest request)
            throws UserInputValidationException {

        ResponseDto ret = new ResponseDto();

        try {
            dto.setId(id);

            String msg = productCategoryService.saveOrUpdateCategory(dto);

            ret.setSuccess();
            ret.setMessage(msg);
            return ret;
        } catch (Exception e) {
            ret.setError(e);
            e.printStackTrace();
        }

        return ret;
    }

    @PatchMapping("/{id}/status")
    public ResponseDto updateStatus(@PathVariable Long id,
                                    @RequestBody ProductCategoryStatusDto dto,
                                    HttpServletRequest request)
            throws UserInputValidationException {

        ResponseDto ret = new ResponseDto();

        try {
            dto.setId(id);

            String msg = productCategoryService.updateCategoryStatus(dto);

            ret.setSuccess();
            ret.setMessage(msg);
            return ret;
        } catch (Exception e) {
            ret.setError(e);
            e.printStackTrace();
        }

        return ret;
    }

    @DeleteMapping("/{id}")
    public ResponseDto delete(@PathVariable Long id, HttpServletRequest request)
            throws UserInputValidationException {

        ResponseDto ret = new ResponseDto();

        try {
            productCategoryService.deleteCategory(id);

            ret.setSuccess();
            ret.setMessage("Product Category deleted Successfully");
            return ret;
        } catch (Exception e) {
            ret.setError(e);
            e.printStackTrace();
        }

        return ret;
    }
}

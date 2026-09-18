package com.ecom.inventory.controller;

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
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

//    @GetMapping
//    public ResponseDto list(@RequestParam(required = false) String keyword,
//                            @PageableDefault(size = 20, sort = "productName") Pageable pageable,
//                            HttpServletRequest request) throws UserInputValidationException {
//
//        ResponseDto ret = new ResponseDto();
//
//        try {
//            Page<Product> data = productService.getProductList(keyword, pageable);
//            ret.setData(data);
//            return ret;
//        } catch (Exception e) {
//            ret.setError(e);
//            e.printStackTrace();
//        }
//
//        return ret;
//    }

    @GetMapping
    public ResponseDto getProductList(ProductQ params) throws UserInputValidationException, Exception {
        ResponseDto ret = new ResponseDto();
//        Long campusId = jwtDecoder.getJwtDetail(request).getCampusId();
        PaginationDto retList = productService.getProductList(params);
        ret.setData(retList);
        return ret;
    }

    @GetMapping("/{id}")
    public ResponseDto getById(@PathVariable Long id,
                               HttpServletRequest request) throws UserInputValidationException {

        ResponseDto ret = new ResponseDto();

        try {
            Product data = productService.getProductById(id);
            ret.setData(data);
            return ret;
        } catch (Exception e) {
            ret.setError(e);
            e.printStackTrace();
        }

        return ret;
    }

    @PostMapping
    public ResponseDto create(@RequestBody ProductSaveDto dto,
                              HttpServletRequest request) throws UserInputValidationException {

        ResponseDto ret = new ResponseDto();

        try {
            String msg = productService.saveOrUpdateProduct(dto);

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
                              @RequestBody ProductSaveDto dto,
                              HttpServletRequest request) throws UserInputValidationException {

        ResponseDto ret = new ResponseDto();

        try {
            dto.setId(id);

            String msg = productService.saveOrUpdateProduct(dto);

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
                                    @RequestBody ProductStatusDto dto,
                                    HttpServletRequest request) throws UserInputValidationException {

        ResponseDto ret = new ResponseDto();

        try {
            dto.setId(id);

            String msg = productService.updateProductStatus(dto);

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
    public ResponseDto delete(@PathVariable Long id,
                              HttpServletRequest request) throws UserInputValidationException {

        ResponseDto ret = new ResponseDto();

        try {
            productService.deleteProduct(id);

            ret.setSuccess();
            ret.setMessage("Product deleted Successfully");
            return ret;
        } catch (Exception e) {
            ret.setError(e);
            e.printStackTrace();
        }

        return ret;
    }
}

package com.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reggie.common.R;
import com.example.reggie.dto.DishDto;
import com.example.reggie.entity.Category;
import com.example.reggie.entity.Dish;
import com.example.reggie.service.CategoryService;
import com.example.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        dishService.saveWithFlavor(dishDto);
        return R.success("Add success");
    }

    @GetMapping("/page")
    public R<Page<DishDto>> page(int page, int pageSize, String name) {
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null, Dish::getName, name);
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        dishService.page(pageInfo, queryWrapper);

        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");
        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list = records.stream().map((item) ->{
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());
        dishDtoPage.setRecords(list);
        return R.success(dishDtoPage);
    }

    @GetMapping("/{id}")
    public R<DishDto> getById(@PathVariable Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        if(dishDto != null){
            return R.success(dishDto);
        } else {
            return R.error("Dish not exist");
        }
    }

    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        dishService.updateWithFlavor(dishDto);
        return R.success("Add success");
    }

    @PostMapping("/status/0")
    public R<String> closeDish(String ids) {
        if(ids.contains(",")){
            String[] temp = ids.split(",");
            for(String id : temp){
                Dish dish = dishService.getById(Long.valueOf(id));
                dish.setStatus(0);
                dishService.updateById(dish);
            }
        } else {
            Dish dish = dishService.getById(ids);
            dish.setStatus(0);
            dishService.updateById(dish);
        }
        return R.success("Update success");
    }

    @PostMapping("/status/1")
    public R<String> sellDish(String ids) {
        if(ids.contains(",")){
            String[] temp = ids.split(",");
            for(String id : temp){
                Dish dish = dishService.getById(Long.valueOf(id));
                dish.setStatus(1);
                dishService.updateById(dish);
            }
        } else {
            Dish dish = dishService.getById(ids);
            dish.setStatus(1);
            dishService.updateById(dish);
        }
        return R.success("Update success");
    }
}

package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@Slf4j
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;
    @ApiOperation("添加菜品")
    @PostMapping
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品：{}",dishDTO);
        dishService.saveWithFlavour(dishDTO);
        String key="dish_"+dishDTO.getCategoryId();
        redisTemplate.delete(key);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页查询 {}",dishPageQueryDTO);
        PageResult pageResult= dishService.pageQuery(dishPageQueryDTO);

        return Result.success(pageResult);
    }
    @DeleteMapping
    @ApiOperation("菜品批量删除")
    public Result deleteById(@RequestParam List<Long> ids){
        log.info("菜品批量删除：{}",ids);
        dishService.deleteBatch(ids);
        //查询以dish_开头的key
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);
        return Result.success();
    }
    @GetMapping("/{id}")
    @ApiOperation("根据id查菜品")
    public Result<DishVO> getById(@PathVariable Long id){
        log.info("根据id查询菜品及口味,{}",id);
        DishVO dishVO=dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }
    @PutMapping
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DishDTO dishDTO){
        dishService.updateWithFlavor(dishDTO);
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);
        return Result.success();
    }
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result getByCategoryId(Long categoryId){
        List<Dish> dishes=dishService.getByCategoryId(categoryId);
        return Result.success(dishes);
    }

    @PostMapping("/status/{status}")
    @ApiOperation("启用禁用员工账号")
    public Result startOrStop(@PathVariable Integer status,Long id){
        log.info("启用禁用菜品账号：{},{}",id,status);
        dishService.startOrStop(status,id);
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);
        return Result.success();
    }

}

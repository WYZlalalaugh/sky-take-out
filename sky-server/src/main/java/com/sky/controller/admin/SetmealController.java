package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("admin/setmeal")
@Api(tags = "套餐相关接口")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    /**
     * 新增套餐
     * @param setmealDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增套餐")
    @CacheEvict(cacheNames = "stmealCache",key = "#setmealDTO.categoryId")
    public Result save(@RequestBody SetmealDTO setmealDTO) {
        setmealService.saveWithDish(setmealDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("分页查询套餐")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO){
        PageResult page=setmealService.page(setmealPageQueryDTO);
        return Result.success(page);
    }
    @DeleteMapping
    @ApiOperation("套餐批量删除")
    @CacheEvict(cacheNames = "stmealCache",allEntries = true)
    public Result deleteById(@RequestParam List<Long> ids){
        log.info("菜品批量删除：{}",ids);
        setmealService.deleteBatch(ids);
        return Result.success();
    }
    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐")
    public Result<SetmealVO> getById(@PathVariable Long id){
        SetmealVO setmealVO=setmealService.getByIdWithDish(id);
        return Result.success(setmealVO);
    }
    @PutMapping
    @ApiOperation("修改套餐")
    @CacheEvict(cacheNames = "stmealCache",allEntries = true)
    public Result update(@RequestBody SetmealDTO setmealDTO){
        setmealService.update(setmealDTO);
        return Result.success();
    }
    @PostMapping("/status/{status}")
    @ApiOperation("停用或启用套餐")
    @CacheEvict(cacheNames = "stmealCache",allEntries = true)
    public Result startOrStop(@PathVariable Integer  status,Long id){
        setmealService.startOrStop(status,id);
        return Result.success();
    }
}

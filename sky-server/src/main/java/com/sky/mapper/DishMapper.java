package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);
    //想菜品表插入1条数据
    @AutoFill(value= OperationType.INSERT)
    void insert(Dish dish);

    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);
    @Select("SELECT * from dish where id=#{id}")
    Dish getById(Long id);

    void deleteById(List<Long> ids);
    @AutoFill(value = OperationType.UPDATE)
    void update(Dish dish);

    List<Dish> getByCategoryId(Dish dish);
    @Select("select dish.* from dish left join setmeal_dish sd on dish.id = sd.dish_id " +
            "where sd.setmeal_id=#{setmealId}")
    List<Dish> getBySetmealId(Long id);

    List<Dish> list(Dish dish);
}

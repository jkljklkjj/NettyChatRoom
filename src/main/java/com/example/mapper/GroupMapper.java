package com.example.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import com.example.model.mysql.Group;

@Mapper
public interface GroupMapper {
    /**
     * 根据ID查询群聊
     * @param id 群聊ID
     * @return 群聊信息
     */
    @Select("SELECT * FROM `groups` WHERE id = #{id}")
    Group selectGroup(int id);

    /**
     * 根据名称查询群聊
     * @param name 群聊名称
     * @return 群聊信息
     */
    @Select("SELECT * FROM `groups` WHERE name = #{name}")
    Group selectGroupByName(String name);

    /**
     * 新建群聊
     * @param group 群聊信息
     * @return id 自动生成的群聊ID
     */
    @Insert("INSERT INTO `groups` (name, description) VALUES (#{name}, #{description})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int register(Group group);

    /**
     * 根据ID删除群聊
     * @param id 群聊ID
     * @return 群聊信息
     */
    @Select("DELETE FROM `groups` WHERE id = #{id}")
    Group delGroup(int id);
}

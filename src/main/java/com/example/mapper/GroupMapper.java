package com.example.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.model.mysql.Group;

@Mapper
public interface GroupMapper {
    /**
     * 根据ID查询群聊
     * @param id 群聊ID
     * @return 群聊信息
     */
    Group selectGroup(int id);

    /**
     * 根据ID列表查询群聊
     * @param groups
     * @return
     */
    List<Group> selectGroups(List<Integer> groups);

    /**
     * 根据名称查询群聊
     * @param name 群聊名称
     * @return 群聊信息
     */
    Group selectGroupByName(String name);

    /**
     * 新建群聊
     * @param group 群聊信息
     * @return id 自动生成的群聊ID
     */
    int register(Group group);

    /**
     * 根据ID删除群聊
     * @param id 群聊ID
     * @return 群聊信息
     */
    int delGroup(int id);
}

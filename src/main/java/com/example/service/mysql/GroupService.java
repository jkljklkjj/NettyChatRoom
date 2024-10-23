package com.example.service.mysql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.mapper.GroupMapper;
import com.example.model.mysql.Group;

@Service
public class GroupService {
    @Autowired
    private GroupMapper groupMapper;

    /**
     * 注册群聊
     * @param group 群聊信息
     * @return id 自动生成的群聊ID
     */
    public int register(Group group) {
        return groupMapper.register(group);
    }

    /**
     * 根据ID查询群聊
     * @param id 群聊的ID
     * @return 群聊的信息
     */
    public Group selectGroup(int id) {
        return groupMapper.selectGroup(id);
    }

    /**
     * 根据名称查询群聊
     * @param name 群聊的名称
     * @return 群聊的信息
     */
    public Group selectGroupByName(String name) {
        return groupMapper.selectGroupByName(name);
    }

    /**
     * 根据ID删除群聊
     * @param id 群聊的ID
     * @return 群聊的信息
     */
    public int delGroup(int id) {
        return groupMapper.delGroup(id);
    }
}

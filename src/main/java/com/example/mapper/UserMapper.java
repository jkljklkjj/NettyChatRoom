package com.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.example.model.mysql.User;

import java.util.List;

@Mapper
public interface UserMapper {
    User selectUser(int id);

    User selectUserByName(String name);

    int insertUser(User user);

    int updateUser(User user);

    List<User> selectFriends(@Param("idList")  List<Integer> ids);
}

package com.example.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import com.example.model.mysql.User;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM users WHERE id = #{id}")
    User selectUser(int id);

    @Select("SELECT * FROM users WHERE username = #{name}")
    User selectUserByName(String name);

    @Insert("INSERT INTO users(username, password, email, phone) VALUES(#{username}, #{password}, #{email}, #{phone})")
    @Options(useGeneratedKeys = true, keyProperty = "id",keyColumn = "id")
    int insertUser(User user);
}

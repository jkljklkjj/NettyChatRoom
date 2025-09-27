package com.example.mapper;

import com.example.model.mysql.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageMapper {

    void insertMessage(Message message);

    List<Message> getOfflineMessages(@Param("target") String target, @Param("limit") int limit);

    void markMessagesAsReceived(@Param("ids") List<String> ids);

}

package com.dev.rebook.services.uesr;

import com.dev.rebook.entities.UserEntity;
import com.dev.rebook.mappers.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
//    private final UserMapper userMapper;

//    @Autowired
//    public UserService(UserMapper userMapper) {
//        this.userMapper = userMapper;
//    }

    public UserEntity selectByProviderId(String provider, String providerId) {
//        return this.userMapper.selectUserByProviderId(provider, providerId);
        return null;
    }
}

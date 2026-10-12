package com.aichain.service;

import com.aichain.dto.UserLoginDTO;
import com.aichain.entity.User;

/**
 * @author Yultuz
 */
public interface UserService {
    User wxLogin(UserLoginDTO userLoginDTO);
}

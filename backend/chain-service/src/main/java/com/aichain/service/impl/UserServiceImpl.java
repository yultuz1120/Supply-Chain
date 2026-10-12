package com.aichain.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aichain.constant.MessageConstant;
import com.aichain.dto.UserLoginDTO;
import com.aichain.entity.User;
import com.aichain.exception.LoginFailedException;
import com.aichain.mapper.UserMapper;
import com.aichain.properties.WeChatProperties;
import com.aichain.service.UserService;
import com.aichain.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;

/**
 * @author Yultuz
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {
    //微信服务接口地址
    public static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";
    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;

    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {
        //获取微信用户的openid
        String openid = getOpenid(userLoginDTO.getCode());
        //判断openid是否真的获取到，是否为空，空抛出业务异常
        if(openid==null){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        // 首次访问时自动创建用户档案
        User user = userMapper.getByOpenid(openid);
        //是新用户自动完成注册
        if(user==null){
           user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
           userMapper.insert(user);
        }
        //返回User对象
        return user;
    }
    //获取微信用户的openid
    private String getOpenid(String code){
        HashMap<String, String> map = new HashMap<>();
        map.put("appid",weChatProperties.getAppid());
        map.put("secret",weChatProperties.getSecret());
        map.put("js_code",code);
        map.put("grant_type","authorization_code");
        String json = HttpClientUtil.doGet(WX_LOGIN, map);
        log.info("微信服务器返回的真实数据：{}", json);
        JSONObject jsonObject = JSON.parseObject(json);
        String openid = jsonObject.getString("openid");
        return openid;
    }
}

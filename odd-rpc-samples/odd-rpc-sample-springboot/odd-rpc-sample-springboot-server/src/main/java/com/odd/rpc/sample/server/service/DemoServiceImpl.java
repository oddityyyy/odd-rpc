package com.odd.rpc.sample.server.service;

import com.odd.rpc.core.remoting.provider.annotation.OddRpcService;
import com.odd.rpc.sample.api.DemoService;
import com.odd.rpc.sample.api.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;

/**
 *
 * @author oddity
 * @create 2023-11-20 18:03
 */

@OddRpcService
@Service
public class DemoServiceImpl implements DemoService {

    //此记录器将用于在“DemoServiceImpl”类中进行日志记录。
    // 使用此记录器，您可以在“DemoServiceImpl”类中记录不同级别的消息
    // （例如，“logger.debug（）”、“logger.info（）”、“logger.error（）”等），
    // 以跟踪执行流程、错误或其他相关信息。
    private static Logger logger = LoggerFactory.getLogger(DemoServiceImpl.class);

    @Override
    public UserDTO sayHi(String name) {

        String word = MessageFormat.format("Hi {0}, from {1} as {2}", name,
                DemoServiceImpl.class.getName(), String.valueOf(System.currentTimeMillis()));

        if ("error".equalsIgnoreCase(name)) {
            throw new RuntimeException("test exception.");
        }

        UserDTO userDTO = new UserDTO(name, word);
        logger.info(userDTO.toString());

        return userDTO;
    }
}

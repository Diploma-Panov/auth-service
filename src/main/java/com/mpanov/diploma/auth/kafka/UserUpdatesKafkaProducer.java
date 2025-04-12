package com.mpanov.diploma.auth.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mpanov.diploma.auth.controller.Mapper;
import com.mpanov.diploma.auth.dao.ServiceUserDao;
import com.mpanov.diploma.auth.kafka.dto.KafkaUserUpdateDto;
import com.mpanov.diploma.auth.model.ServiceUser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import static com.mpanov.diploma.auth.config.KafkaConfig.USER_UPDATES_TOPIC_NAME;

@Slf4j
@Service
@AllArgsConstructor
public class UserUpdatesKafkaProducer {

    private Mapper mapper;

    private KafkaTemplate<String, String> kafkaTemplate;

    private ObjectMapper objectMapper;

    private ServiceUserDao serviceUserDao;

    private void sendUserUpdateSafe(Long userId) {
        try {
            ServiceUser user = serviceUserDao.getServiceUserByIdThrowable(userId);
            KafkaUserUpdateDto updateDto = mapper.toKafkaUserUpdateDto(user);
            String payload = objectMapper.writeValueAsString(updateDto);
            log.info("Sending user update {} to kafka topic {}", payload, USER_UPDATES_TOPIC_NAME);
            kafkaTemplate.send(USER_UPDATES_TOPIC_NAME, payload);
            log.info("Successfully sent user update to topic {}, for userId={}", USER_UPDATES_TOPIC_NAME, userId);
        } catch (Exception e) {
            log.error("Could not send user update to kafka, for userId={}", userId, e);
        }
    }

    @Async("kafkaUpdatesExecutor")
    public void sendUserUpdateAsync(Long userId) {
        this.sendUserUpdateSafe(userId);
    }


}

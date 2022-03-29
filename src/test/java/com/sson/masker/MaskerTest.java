package com.sson.masker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sson.model.Person;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class MaskerTest {

    @Test
    void nameMasker() throws JsonProcessingException {

        Person person = Person.of("emp_0001", "ssonsh_01", "010-2057-4164");
        log.info("person : {}", person);

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonPerson = objectMapper.writeValueAsString(person);

        log.info("jsonPerson : {}", jsonPerson);
    }
}

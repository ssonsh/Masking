package com.sson.masker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sson.model.Person;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class MaskerTest {

    @Test
    void nameMasker() throws JsonProcessingException {

        Person person1 = Person.of("emp_0001", "손성", "010-2057-4164");
        log.info("person[1] : {}", person1);
        Person maskedPerson1 = new ObjectMapper().readValue(new ObjectMapper().writeValueAsString(person1), new TypeReference<Person>() {});
        log.info("maskedPerson[1] : {}", maskedPerson1.getName());


        Person person2 = Person.of("emp_0001", "손성현", "010-2057-4164");
        log.info("person[2] : {}", person2);
        Person maskedPerson2 = new ObjectMapper().readValue(new ObjectMapper().writeValueAsString(person2), new TypeReference<Person>() {});
        log.info("maskedPerson[2] : {}", maskedPerson2.getName());



        Person person3 = Person.of("emp_0001", "손성현임", "010-2057-4164");
        log.info("person[3] : {}", person3);
        Person maskedPerson3 = new ObjectMapper().readValue(new ObjectMapper().writeValueAsString(person3), new TypeReference<Person>() {});
        log.info("maskedPerson[3] : {}", maskedPerson3.getName());

        Assertions.assertAll(
            () -> Assertions.assertEquals(maskedPerson1.getName(), "손*"),
            () -> Assertions.assertEquals(maskedPerson2.getName(), "손성*"),
            () -> Assertions.assertEquals(maskedPerson3.getName(), "손성**")
        );
    }
}

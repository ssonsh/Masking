package com.sson.model;

import com.sson.masker.MaskRequired;
import com.sson.masker.MaskType;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Person {

    private String employeeId;

    @MaskRequired(type = MaskType.NAME)
    private String name;

    @MaskRequired(type = MaskType.PHONE_NUMBER)
    private String phoneNumber;

    public Person(String employeeId, String name, String phoneNumber) {
        this.employeeId = employeeId;
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public static Person of(String employeeId, String name, String phoneNumber) {
        return new Person(employeeId, name, phoneNumber);
    }
}

package com.sson.model;

import com.sson.masker.MaskRequired;
import com.sson.masker.MaskingType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
public class Person {

    private String employeeId;

    @MaskRequired(type = MaskingType.NAME)
    private String name;

    @MaskRequired(type = MaskingType.PHONE_NUMBER)
    private String phoneNumber;

    @MaskRequired(type = MaskingType.EMAIL_ADDRESS)
    private String emailAddress;

    public Person(String employeeId, String name, String phoneNumber, String emailAddress) {
        this.employeeId = employeeId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.emailAddress = emailAddress;
    }

    public static Person of(String employeeId, String name, String phoneNumber, String emailAddress) {
        return new Person(employeeId, name, phoneNumber, emailAddress);
    }
}

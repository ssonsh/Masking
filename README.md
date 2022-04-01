# Spring, Annotation 기반에 개인정보 마스킹 처리 

# 애노테이션 기반으로 개인정보 마스킹하기

### 비즈니스 요구사항

- 특정 정보들은 마스킹 규칙에 따라 마스킹되어 반환되어야 한다.

**방법**

- 어떻게 할 수 있을까? Response 응답으로 반환하는 모델을 만들고 반환하기 전에
- 마스킹 해야하는 필드들을 정하고 그 필드들을 변환시켜야 할 것이다.
- 각 필드들을 origin 정보를 바탕으로 maskedValue 로 변환하는 로직을 일일이 수작업으로 진행할 수도 있을 것이다.
    - 그러나...? 생각만 해도 끔찍하다.
- 마스킹 대상이라고 정의할 수 있는 방법을 지정하고
- 지정된 마스킹 대상을 손쉽게 적용할 수 있어야 할 것 같다.
- 또한, 마스킹 로직이 해당 객체안에 있는 것이 아니라 모듈화 되어 있어야 할 것 같다.

**마스킹 타입**

- 우선 이름과 전화번호, 이메일 주소를 마스킹하기 위해 아래와 같이 마스킹 타입을 enum으로 지정하였다.

```java
package com.sson.masker;

public enum MaskingType {
    NAME,
    PHONE_NUMBER,
    EMAIL_ADDRESS
}
```

**마스킹 적용을 위한 커스텀 애노테이션**

- 마스킹을 적용할 필드에 작성할 커스텀 애노테이션으로 @MaskRequired 를 구성하였고
    - 필드에 적용할 것임으로 Target을 FIELD로 정의해주었다.
- 인자로 위에서 정의한 MaskingType을 주입받을 수 있도록 하였다.
- JacksonAnnotationInside, JsonSerialize 애노테이션을 차차 알아보자.

```java
package com.sson.masker;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = MaskingPropertySerializer.class)
public @interface MaskRequired {
    MaskingType type();
}
```

**Json Serialize 할 때 위에 정의한 MaskRequired 애노테이션을 적용해보자.**

- 특정 필드에 @MaskRequired 가 있을 떄 자동으로 마스킹 처리를 하기 위한 커스텀 Serializer를 구성한다.
- **Jackson 라이브러리**는 객체를 원하는대로 Serialize하기 위해 “**Serializer**” 라는 인터페이스를 제공하고 있다.
- 이 인터페이스를 구현한 커스텀 Serializer를 Jackson 에게 알려주면 원하는대로 JSON의 결과값을 핸들링 할 수 있게 된다.

**Serializer 에서 어떻게 프로퍼티, 필드에 대한 메타정보를 읽을 수 있을까?**

- **ContextualSerializer** 를 이용한다.
- **ContextualSerializer** 는 아래 설명에서 볼 수 있듯이 **어떤 Serializer를 사용해야 할 지**에 대한 선택하기 위해 사용되는 인터페이스이다.
- serializer를 선택하기 위한 메타 정보를 우리에게 알려준다.

**이제 우리가 원하는 커스텀 Json Serializer를 만들어보자.**

MaskingPropertySerializer.java

- extends StdSerializer<String>
- implements ContextualSerializer

```java
package com.sson.masker;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class MaskingPropertySerializer extends StdSerializer<String> implements ContextualSerializer {
    MaskingType maskingType;

    protected MaskingPropertySerializer() {
        super(String.class);
    }

    protected MaskingPropertySerializer(MaskingType maskingType) {
        super(String.class);
        this.maskingType = maskingType;
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(Masking.mask(maskingType, value));
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
        MaskingType maskingTypeValue = null;
        MaskRequired ann = null;
        if (property != null) {
            ann = property.getAnnotation(MaskRequired.class);
        }
        if (ann != null) {
            maskingTypeValue = ann.type();
        }
        return new MaskingPropertySerializer(maskingTypeValue);
    }
}
```

**Override Method**

`public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)`

- createContextual 메소드는
- BeanProperty 매개변수로 넘어온 property를 이용한다.
    - `property.getAnnotation(MaskRequired.class);`
    - getAnnotation으로 원하는 커스텀 애노테이션인 MaskRequired 를 가져오게 되고
    - 가져온 MaskRequired 애노테이션에 주입된 MaskingType을 판단한다.
- 이렇게 판단된 MaskingType을 이용해 MaskingPropertySerializer 생성자를 생성한다.

**Override Method**

`public void serialize(String value, JsonGenerator gen, SerializerProvider provider) throws IOException`

- 실제 Json 이 Serialize 할 때 호출되는 메소드라고 보면된다.
- 이 로직에서는 위에서 판단된 MaskRequired 애노테이션의 MaskingType에 따라 마스킹 처리 구체 클래스를 판단해내고 writeString 을 호출하여 Serialize 하게 된다.
    
    `gen.writeString(Masking.*mask*(maskingType, value));`
    

**Masking.java**

```java
package com.sson.masker;

import com.sson.masker.factory.Masker;
import com.sson.masker.factory.MaskingFactory;

public class Masking {
    public static String mask(MaskingType type, String value){
        Masker masker = MaskingFactory.get(type);
        return masker.mask(value);
    }
}
```

- Masking 클래스의 mask 메소드는 전달된 MaskingType과 마스킹 대상인 value를 매개변수로 전달받는다.
- MaskingFactory를 통해 특정 마스킹 구체 클래스를 판단해내고
- mask(value) 메소드를 호출하여 결과를 return 한다.

**MaskingFactory와 관련한 구조**

![image](https://user-images.githubusercontent.com/18654358/161171498-3126871e-b1b1-4849-ae5a-6b6fe6c6186b.png)

**MaskingFactory.java**

```java
package com.sson.masker.factory;

import com.sson.masker.MaskingType;

public class MaskingFactory {
    public static Masker get(MaskingType type) {
        switch (type){
            case NAME:
                return new NameMasker();
            case PHONE_NUMBER:
                return new PhoneNumberMasker();
            case EMAIL_ADDRESS:
                return new EmailAddressMasker();
            default:
                throw new IllegalArgumentException("not found match type");
        }
    }
}
```

**Masker.java**

```java
package com.sson.masker.factory;

public interface Masker {
    String mask(String value);
}
```

NameMasker.java

```java

package com.sson.masker.factory;

import org.springframework.util.StringUtils;

public class NameMasker implements Masker{

    private static final String NAME_MASK_ONE_PATTERN = "(?<=.{1}).";
    private static final String NAME_MASK_TWO_PATTERN = "(?<=.{2}).";

    @Override
    public String mask(String value) {
        if(!StringUtils.hasText(value)){
            return "";
        }

        if(value.length() < 3){
            return value.replaceAll(NAME_MASK_ONE_PATTERN, "*");
        }

        return value.replaceAll(NAME_MASK_TWO_PATTERN, "*");
    }
}
```

PhoneNumberMasker.java

```java
package com.sson.masker.factory;

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhoneNumberMasker implements Masker{

    private static final String PHONE_NUMBER_PATTERN_WITH_HYPEON = "(\\d{2,3})-?(\\d{3,4})-?(\\d{4})$";
    private static final String PHONE_NUMBER_PATTERN_WITHOUT_HYPEON = "(\\d{2,3})?(\\d{3,4})?(\\d{4})$";
    @Override
    public String mask(String value) {
        if(!StringUtils.hasText(value)){
            return "";
        }

        if(isExistHyphen(value)){
            return maskingByPattern(PHONE_NUMBER_PATTERN_WITH_HYPEON, value);
        }

        return maskingByPattern(PHONE_NUMBER_PATTERN_WITHOUT_HYPEON, value);
    }

    private String maskingByPattern(String pattern, String value) {
        Matcher matcher = Pattern.compile(pattern).matcher(value);
        if(!matcher.find()){
            return value;
        }
        String target = matcher.group(2);
        int length = target.length();
        char[] c = new char[length];
        Arrays.fill(c, '*');

        return value.replace(target, String.valueOf(c));
    }

    private boolean isExistHyphen(String value) {
        return value.split("-").length == 3;
    }
}
```

EmailAddressMasker.java

```java
package com.sson.masker.factory;

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailAddressMasker implements Masker{

    private static final String PATTERN = "^(.+)@(.+)$";
    @Override
    public String mask(String value) {
        if(!StringUtils.hasText(value)){
            return "";
        }

        return maskingByPattern(value);
    }

    private String maskingByPattern(String value) {
        Matcher matcher = Pattern.compile(PATTERN).matcher(value);
        if(!matcher.find()){
            return value;
        }

        String target = matcher.group(1);
        int length = target.length();
        char[] c;
        if(length > 3){
            c = new char[length - 3];
        }else{
            c = new char[length - 1];
        }

        Arrays.fill(c, '*');
        return value.replaceAll(target, target.substring(0, length > 3 ? 3 : 1) + String.valueOf(c));
    }
}
```

---

이렇게 구성된 모듈을 사용하기 위한 Response 모델 객체를 정의하고

필요한 마스킹 타입에 따라 MaskRequired 애노테이션을 정의한다.

**Person.java**

- name 필드는 MaskingType.NAME 으로 마스킹 될 것을 기대한다.
- phoneNumber 필드는 MaskingType.PHONE_NUMBER 으로 마스킹 될 것을 기대한다.
- emailAddress 필드는 MaskingType.EMAIL_ADDRESS 으로 마스킹 될 것을 기대한다.

```java
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
```

테스트코드 

**NameTest**

```java
@Test
void nameMasker() throws JsonProcessingException {

    Person person1 = Person.of("emp_0001", "손성", "01020574164", "");
    log.info("person[1] : {}", person1);
    Person maskedPerson1 = new ObjectMapper().readValue(new ObjectMapper().writeValueAsString(person1), new TypeReference<Person>() {});
    log.info("maskedPerson[1] : {}", maskedPerson1);

    Person person2 = Person.of("emp_0001", "손성현", "010-2057-4164", "");
    log.info("person[2] : {}", person2);
    Person maskedPerson2 = new ObjectMapper().readValue(new ObjectMapper().writeValueAsString(person2), new TypeReference<Person>() {});
    log.info("maskedPerson[2] : {}", maskedPerson2);

    Person person3 = Person.of("emp_0001", "손성현임", "010-257-4164", "");
    log.info("person[3] : {}", person3);
    Person maskedPerson3 = new ObjectMapper().readValue(new ObjectMapper().writeValueAsString(person3), new TypeReference<Person>() {});
    log.info("maskedPerson[3] : {}", maskedPerson3);

    Assertions.assertAll(
        () -> Assertions.assertEquals(maskedPerson1.getName(), "손*"),
        () -> Assertions.assertEquals(maskedPerson2.getName(), "손성*"),
        () -> Assertions.assertEquals(maskedPerson3.getName(), "손성**")
    );
}
```

PhoneNumberTest

```java
@Test
    void phoneNumberMasker() throws JsonProcessingException {

        Person person1 = Person.of("emp_0001", "손성", "01020574164", "");
        log.info("person[1] : {}", person1);
        Person maskedPerson1 = new ObjectMapper().readValue(new ObjectMapper().writeValueAsString(person1), new TypeReference<Person>() {});
        log.info("maskedPerson[1] : {}", maskedPerson1);

        Person person2 = Person.of("emp_0001", "손성현", "010-2057-4164", "");
        log.info("person[2] : {}", person2);
        Person maskedPerson2 = new ObjectMapper().readValue(new ObjectMapper().writeValueAsString(person2), new TypeReference<Person>() {});
        log.info("maskedPerson[2] : {}", maskedPerson2);

        Person person3 = Person.of("emp_0001", "손성현임", "010-257-4164", "");
        log.info("person[3] : {}", person3);
        Person maskedPerson3 = new ObjectMapper().readValue(new ObjectMapper().writeValueAsString(person3), new TypeReference<Person>() {});
        log.info("maskedPerson[3] : {}", maskedPerson3);

        Assertions.assertAll(
            () -> Assertions.assertEquals(maskedPerson1.getPhoneNumber(), "010****4164"),
            () -> Assertions.assertEquals(maskedPerson2.getPhoneNumber(), "010-****-4164"),
            () -> Assertions.assertEquals(maskedPerson3.getPhoneNumber(), "010-***-4164")
        );
    }
```

EmailAddressTest

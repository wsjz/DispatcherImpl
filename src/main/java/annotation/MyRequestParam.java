package annotation;

import java.lang.annotation.*;

/**
 * @author Created by Darling
 * @version CreatedDate: 2018/4/23 at 14:40
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyRequestParam {
    String value();
}

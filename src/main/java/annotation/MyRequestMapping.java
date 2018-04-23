package annotation;

import java.lang.annotation.*;

/**
 * @author Created by Darling
 * @version CreatedDate: 2018/4/23 at 14:39
 */
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyRequestMapping {
    String value() default "";
}

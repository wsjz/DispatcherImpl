package annotation;

import java.lang.annotation.*;

/**
 * @author Created by Darling
 * @version CreatedDate: 2018/4/23 at 14:36
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyController {
    String value() default "";
}

package org.smu.randsome.randsomeback.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.smu.randsome.randsomeback.security.TestAdminSecurityContextFactory;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.METHOD, ElementType.TYPE })
@WithSecurityContext(factory = TestAdminSecurityContextFactory.class, setupBefore = TestExecutionEvent.TEST_EXECUTION)
public @interface TestAdmin {

    long id() default 1L;

    String role() default "ROLE_ADMIN";

}
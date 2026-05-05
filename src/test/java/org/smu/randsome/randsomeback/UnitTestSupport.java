package org.smu.randsome.randsomeback;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(value = MockitoExtension.class)
public class UnitTestSupport {

}
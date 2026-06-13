package com.loan_org.identity_and_access_management.mdc;

import com.loan_org.identity_and_access_management.middleware.filter.MdcHeaderFilter;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

class MdcHeaderFilterTest {

    private MdcHeaderFilter filter;
    private FilterChain filterChain;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    private static final String HEADER_NAME = "X-Correlation-ID";
    private static final String MDC_KEY = "traceId";

    @BeforeEach
    void setUp() {
        filter = new MdcHeaderFilter();
        filterChain = Mockito.mock(FilterChain.class);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        // Manually inject the properties normally supplied by @Value
        ReflectionTestUtils.setField(filter, "mdcHeader", HEADER_NAME);
        ReflectionTestUtils.setField(filter, "mdcKey", MDC_KEY);
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void should_UseProvidedHeader_And_PopulateMdc() throws Exception {
        String expectedTraceId = "test-trace-id-123";
        request.addHeader(HEADER_NAME, expectedTraceId);

        // We stub the chain to check that the MDC is actually set *during* processing
        Mockito.doAnswer(invocation -> {
            assertThat(MDC.get(MDC_KEY)).isEqualTo(expectedTraceId);
            return null;
        }).when(filterChain).doFilter(request, response);

        // Execute the filter directly via its public method
        filter.doFilter(request, response, filterChain);

        // Verify downstream was called and the outbound header matches
        verify(filterChain).doFilter(request, response);
        assertThat(response.getHeader(HEADER_NAME)).isEqualTo(expectedTraceId);

        // Verify final cleanup happened
        assertThat(MDC.get(MDC_KEY)).isNull();
    }

    @Test
    void should_GenerateUuid_When_HeaderIsMissing() throws Exception {
        Mockito.doAnswer(invocation -> {
            String capturedMdc = MDC.get(MDC_KEY);
            assertThat(capturedMdc).isNotBlank().hasSize(36);
            return null;
        }).when(filterChain).doFilter(request, response);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(response.getHeader(HEADER_NAME)).isNotBlank().hasSize(36);
        assertThat(MDC.get(MDC_KEY)).isNull();
    }

    @Test
    void should_GenerateUuid_When_HeaderIsBlankWhitespace() throws Exception {
        request.addHeader(HEADER_NAME, "     ");

        Mockito.doAnswer(invocation -> {
            String capturedMdc = MDC.get(MDC_KEY);
            assertThat(capturedMdc).isNotBlank().hasSize(36);
            return null;
        }).when(filterChain).doFilter(request, response);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(response.getHeader(HEADER_NAME)).isNotBlank().hasSize(36);
        assertThat(MDC.get(MDC_KEY)).isNull();
    }

    @Test
    void should_ClearMdc_EvenWhen_DownstreamThrowsException() throws Exception {
        request.addHeader(HEADER_NAME, "error-trace-id");

        // Simulate an exception deep inside the application layers
        doThrow(new RuntimeException("Downstream crash"))
                .when(filterChain).doFilter(request, response);

        assertThatThrownBy(() -> filter.doFilter(request, response, filterChain))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Downstream crash");

        // Verify that the finally block executed and completely cleaned up the MDC thread
        assertThat(MDC.get(MDC_KEY)).isNull();
    }
}
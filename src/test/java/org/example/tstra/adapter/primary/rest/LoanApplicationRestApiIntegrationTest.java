package org.example.tstra.adapter.primary.rest;

import org.example.tstra.application.usecase.CreateLoanApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class LoanApplicationRestApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateLoanApplication createLoanApplication;

    @Test
    public void itShouldCreateALoanApplication() throws Exception {
        this.mockMvc.perform(
            post("/api/v1/loan-applications")
        ).andExpect(status().isCreated());
    }
}

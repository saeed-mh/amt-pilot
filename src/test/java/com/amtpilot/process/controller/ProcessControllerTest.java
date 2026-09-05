package com.amtpilot.process.controller;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.amtpilot.common.web.TraceIdFilter;
import com.amtpilot.process.dto.ProcessResponse;
import com.amtpilot.process.dto.RequirementResponse;
import com.amtpilot.process.service.ProcessService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProcessControllerTest {

        @Mock
        private ProcessService processService;

        private MockMvc mockMvc;

        @BeforeEach
        void setUp() {
                ProcessController processController = new ProcessController(processService);

                mockMvc = MockMvcBuilders
                                .standaloneSetup(processController)
                                .addFilters(new TraceIdFilter())
                                .build();
        }

        @Test
        void returnsProcessesForRequestedCity() throws Exception {
                UUID processId = UUID.randomUUID();
                UUID authorityId = UUID.randomUUID();

                ProcessResponse process = new ProcessResponse(
                                processId,
                                "DO_STUDENT_RESIDENCE_EXTENSION",
                                "Student Residence Permit Extension",
                                "Dortmund",
                                "IMMIGRATION",
                                1,
                                authorityId,
                                "Dortmund Immigration Office");

                when(processService.getProcessesByCity("Dortmund"))
                                .thenReturn(List.of(process));

                mockMvc.perform(
                                get("/api/v1/processes")
                                                .param("city", "Dortmund"))
                                .andExpect(status().isOk())
                                .andExpect(header().exists(
                                                TraceIdFilter.TRACE_ID_HEADER))
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data[0].id")
                                                .value(processId.toString()))
                                .andExpect(jsonPath("$.data[0].code")
                                                .value("DO_STUDENT_RESIDENCE_EXTENSION"))
                                .andExpect(jsonPath("$.data[0].title")
                                                .value("Student Residence Permit Extension"))
                                .andExpect(jsonPath("$.data[0].city")
                                                .value("Dortmund"))
                                .andExpect(jsonPath("$.data[0].domain")
                                                .value("IMMIGRATION"))
                                .andExpect(jsonPath("$.data[0].version").value(1))
                                .andExpect(jsonPath("$.data[0].authorityId")
                                                .value(authorityId.toString()))
                                .andExpect(jsonPath("$.data[0].authorityName")
                                                .value("Dortmund Immigration Office"))
                                .andExpect(jsonPath("$.traceId").isNotEmpty());

                verify(processService).getProcessesByCity("Dortmund");
        }

        @Test
        void returnsRequirementsForProcess() throws Exception {
                UUID processId = UUID.randomUUID();
                UUID requirementId = UUID.randomUUID();
                UUID sourceId = UUID.randomUUID();

                RequirementResponse requirement = new RequirementResponse(
                                requirementId,
                                "PASSPORT",
                                "Valid passport",
                                true,
                                1,
                                sourceId,
                                "Dortmund Address Registration",
                                "https://www.dortmund.de");

                when(processService.getRequirements(processId))
                                .thenReturn(List.of(requirement));

                mockMvc.perform(
                                get("/api/v1/processes/{processId}/requirements",
                                                processId))
                                .andExpect(status().isOk())
                                .andExpect(header().exists(
                                                TraceIdFilter.TRACE_ID_HEADER))
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data[0].id")
                                                .value(requirementId.toString()))
                                .andExpect(jsonPath("$.data[0].code")
                                                .value("PASSPORT"))
                                .andExpect(jsonPath("$.data[0].title")
                                                .value("Valid passport"))
                                .andExpect(jsonPath("$.data[0].required")
                                                .value(true))
                                .andExpect(jsonPath("$.data[0].version")
                                                .value(1))
                                .andExpect(jsonPath("$.data[0].sourceId")
                                                .value(sourceId.toString()))
                                .andExpect(jsonPath("$.data[0].sourceTitle")
                                                .value("Dortmund Address Registration"))
                                .andExpect(jsonPath("$.data[0].sourceUrl")
                                                .value("https://www.dortmund.de"))
                                .andExpect(jsonPath("$.traceId").isNotEmpty());

                verify(processService).getRequirements(processId);
        }

        @Test
        void usesDortmundWhenCityIsNotProvided() throws Exception {
                when(processService.getProcessesByCity("Dortmund"))
                                .thenReturn(List.of());

                mockMvc.perform(get("/api/v1/processes"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data").isEmpty());

                verify(processService).getProcessesByCity("Dortmund");
        }
}
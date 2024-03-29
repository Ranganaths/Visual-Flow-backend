/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package by.iba.vfapi.controllers;

import by.iba.vfapi.dto.pipelines.CronPipelineDto;
import by.iba.vfapi.dto.pipelines.PipelineOverviewDto;
import by.iba.vfapi.dto.pipelines.PipelineOverviewListDto;
import by.iba.vfapi.dto.pipelines.PipelineRequestDto;
import by.iba.vfapi.dto.pipelines.PipelineResponseDto;
import by.iba.vfapi.services.PipelineService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PipelineControllerTest {
    @Mock
    private PipelineService pipelineService;
    private PipelineController pipelineController;

    @BeforeEach
    void setUp() {
        pipelineController = new PipelineController(pipelineService);
    }

    @Test
    void testCreate() throws JsonProcessingException {

        when(pipelineService.create("projectId",
                                    "name",
                                    new ObjectMapper().readTree("{\"graph\":[]}"))).thenReturn("id");
        JsonNode graph = new ObjectMapper().readTree("{\"graph\":[]}");
        ResponseEntity<String> response =
            pipelineController.create("projectId", new PipelineRequestDto("name", graph));

        assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Status must be OK");
        assertEquals("id", response.getBody(), "Body must be equals to Id");
    }

    @Test
    void testGet() throws IOException {
        PipelineResponseDto dto =
            ((PipelineResponseDto) new PipelineResponseDto().lastModified("lastModified").name("name")).definition(
                new ObjectMapper().readTree("{\"graph\":[]}".getBytes()));

        when(pipelineService.getById("projectId", "id")).thenReturn(dto);
        PipelineResponseDto response = pipelineController.get("projectId", "id");

        assertEquals(dto, response, "Response must be equals to dto");
    }

    @Test
    void testUpdate() throws JsonProcessingException {
        doNothing()
            .when(pipelineService)
            .update("projectName", "name", new ObjectMapper().readTree("{\"graph\":[]}"), "newName");
        JsonNode graph = new ObjectMapper().readTree("{\"graph\":[]}");
        pipelineController.update("projectName", "name", new PipelineRequestDto("newName", graph));

        verify(pipelineService).update(anyString(), anyString(), any(JsonNode.class), anyString());
    }

    @Test
    void testDelete() {
        doNothing().when(pipelineService).delete("projectName", "name");
        ResponseEntity<String> response = pipelineController.delete("projectName", "name");
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode(), "Status must be 204");
        verify(pipelineService).delete(anyString(), anyString());
    }

    @Test
    void testGetAll() {
        when(pipelineService.getAll("project1")).thenReturn(PipelineOverviewListDto
                                                                .builder()
                                                                .pipelines(List.of(
                                                                    new PipelineOverviewDto(),
                                                                    new PipelineOverviewDto()))
                                                                .editable(true)
                                                                .build());

        PipelineOverviewListDto response = pipelineController.getAll("project1");

        assertEquals(2, response.getPipelines().size(), "Size must be equals to 2");
        assertTrue(response.isEditable(), "Must be true");

        verify(pipelineService).getAll(anyString());
    }

    @Test
    void testRun() {
        doNothing().when(pipelineService).run("projectId", "id");

        pipelineController.run("projectId", "id");

        verify(pipelineService).run(anyString(), anyString());
    }

    @Test
    void testStop() {
        doNothing().when(pipelineService).stop("projectId", "id");

        pipelineController.stop("projectId", "id");

        verify(pipelineService).stop(anyString(), anyString());
    }

    @Test
    void testResume() {
        doNothing().when(pipelineService).resume("projectId", "id");

        pipelineController.resume("projectId", "id");

        verify(pipelineService).resume(anyString(), anyString());
    }

    @Test
    void testCreateCron() {

        CronPipelineDto cronPipelineDto = new CronPipelineDto();
        doNothing().when(pipelineService).createCron("projectId", "id", cronPipelineDto);

        pipelineController.createCron("projectId", "id", cronPipelineDto);

        verify(pipelineService).createCron(anyString(), anyString(), any(CronPipelineDto.class));
    }

    @Test
    void testDeleteCron() {
        doNothing().when(pipelineService).deleteCron("projectId", "id");

        pipelineController.deleteCron("projectId", "id");

        verify(pipelineService).deleteCron(anyString(), anyString());
    }

    @Test
    void testGetCronPipeline() {
        CronPipelineDto dto = CronPipelineDto.builder().build();

        when(pipelineService.getCronById("projectId", "id")).thenReturn(dto);
        CronPipelineDto response = pipelineController.getCronPipeline("projectId", "id");

        assertEquals(dto, response, "Response must be equals to dto");
    }
}

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

package by.iba.vfapi.services;

import by.iba.vfapi.dto.Constants;
import by.iba.vfapi.dto.exporting.ExportRequestDto;
import by.iba.vfapi.dto.exporting.ExportResponseDto;
import by.iba.vfapi.dto.importing.ImportResponseDto;
import by.iba.vfapi.exceptions.BadRequestException;
import by.iba.vfapi.model.argo.Arguments;
import by.iba.vfapi.model.argo.DagTask;
import by.iba.vfapi.model.argo.DagTemplate;
import by.iba.vfapi.model.argo.Parameter;
import by.iba.vfapi.model.argo.Template;
import by.iba.vfapi.model.argo.WorkflowTemplate;
import by.iba.vfapi.model.argo.WorkflowTemplateSpec;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.client.utils.Serialization;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {
    @Mock
    private ArgoKubernetesService argoKubernetesService;
    @Mock
    private JobService jobService;
    @Mock
    private PipelineService pipelineService;
    private TransferService transferService;

    @BeforeEach
    void setUp() {
        transferService = new TransferService(argoKubernetesService, jobService, pipelineService);
    }

    @Test
    void testExporting() {
        WorkflowTemplate workflowTemplate = new WorkflowTemplate();
        workflowTemplate.setMetadata(new ObjectMetaBuilder()
                                         .withName("pipelineId")
                                         .addToLabels(Constants.NAME, "name")
                                         .addToLabels(Constants.TYPE, "pipeline")
                                         .addToAnnotations(Constants.DEFINITION,
                                                           Base64.encodeBase64String("GRAPH".getBytes()))
                                         .addToAnnotations(Constants.LAST_MODIFIED, "lastModified")
                                         .build());
        workflowTemplate.setSpec(new WorkflowTemplateSpec().templates(List.of(new Template()
                                                                                  .name("dagTemplate")
                                                                                  .dag(new DagTemplate().addTasksItem(
                                                                                      new DagTask().arguments(new Arguments()
                                                                                                                  .addParametersItem(
                                                                                                                      new Parameter()
                                                                                                                          .name(
                                                                                                                              K8sUtils.CONFIGMAP)
                                                                                                                          .value(
                                                                                                                              "jobFromPipeline"))))))));


        when(argoKubernetesService.getWorkflowTemplate("projectId", "pipelineId")).thenReturn(workflowTemplate);

        ConfigMap configMap1 = new ConfigMapBuilder()
            .addToData(Map.of(Constants.EXECUTOR_MEMORY,
                              "1G",
                              Constants.DRIVER_MEMORY,
                              "1G",
                              Constants.JOB_CONFIG_FIELD,
                              "{\"nodes\":[], \"edges\":[]}"))
            .withNewMetadata()
            .withName("jobFromPipeline")
            .addToLabels(Constants.NAME, "name1")
            .addToLabels(Constants.TYPE, Constants.TYPE_JOB)
            .addToAnnotations(Constants.DEFINITION, Base64.encodeBase64String("GRAPH".getBytes()))
            .addToAnnotations(Constants.LAST_MODIFIED, "lastModified")
            .endMetadata()
            .build();

        ConfigMap configMap2 = new ConfigMapBuilder()
            .addToData(Map.of(Constants.EXECUTOR_MEMORY,
                              "1G",
                              Constants.DRIVER_MEMORY,
                              "1G",
                              Constants.JOB_CONFIG_FIELD,
                              "{\"nodes\":[], \"edges\":[]}"))
            .withNewMetadata()
            .withName("jobId1")
            .addToLabels(Constants.NAME, "name1")
            .addToLabels(Constants.TYPE, Constants.TYPE_JOB)
            .addToAnnotations(Constants.DEFINITION, Base64.encodeBase64String("GRAPH".getBytes()))
            .addToAnnotations(Constants.LAST_MODIFIED, "lastModified")
            .endMetadata()
            .build();
        when(argoKubernetesService.getConfigMap("projectId", "jobId1")).thenReturn(configMap2);
        when(argoKubernetesService.getConfigMap("projectId", "jobFromPipeline")).thenReturn(configMap1);

        ExportResponseDto exporting = transferService.exporting("projectId",
                                                                Set.of("jobId1"),
                                                                Set.of(new ExportRequestDto.PipelineRequest(
                                                                    "pipelineId",
                                                                    true)));

        ExportResponseDto expected = ExportResponseDto
            .builder()
            .jobs(Set.of(Serialization.asJson(configMap1), Serialization.asJson(configMap2)))
            .pipelines(Set.of(Serialization.asJson(workflowTemplate)))
            .build();

        assertEquals(expected, exporting);
    }

    @Test
    void testImporting() {
        ConfigMap configMap1 = new ConfigMapBuilder()
            .addToData(Map.of(Constants.EXECUTOR_MEMORY,
                              "1G",
                              Constants.DRIVER_MEMORY,
                              "1G",
                              Constants.JOB_CONFIG_FIELD,
                              "{\"nodes\":[], \"edges\":[]}"))
            .withNewMetadata()
            .withName("jobId1")
            .addToLabels(Constants.NAME, "jobName1")
            .addToLabels(Constants.TYPE, Constants.TYPE_JOB)
            .addToAnnotations(Constants.DEFINITION, Base64.encodeBase64String("GRAPH".getBytes()))
            .addToAnnotations(Constants.LAST_MODIFIED, "lastModified")
            .endMetadata()
            .build();

        ConfigMap configMap2 = new ConfigMapBuilder()
            .addToData(Map.of(Constants.EXECUTOR_MEMORY,
                              "1G",
                              Constants.DRIVER_MEMORY,
                              "1G",
                              Constants.JOB_CONFIG_FIELD,
                              "{\"nodes\":[], \"edges\":[]}"))
            .withNewMetadata()
            .withName("jobId2")
            .addToLabels(Constants.NAME, "jobName2")
            .addToLabels(Constants.TYPE, Constants.TYPE_JOB)
            .addToAnnotations(Constants.DEFINITION, Base64.encodeBase64String("GRAPH".getBytes()))
            .addToAnnotations(Constants.LAST_MODIFIED, "lastModified")
            .endMetadata()
            .build();

        ConfigMap configMap3 = new ConfigMapBuilder()
            .addToData(Map.of(Constants.EXECUTOR_MEMORY,
                              "1G",
                              Constants.DRIVER_MEMORY,
                              "1G",
                              Constants.JOB_CONFIG_FIELD,
                              "{\"nodes\":[], \"edges\":[]}"))
            .withNewMetadata()
            .withName("jobId3")
            .addToLabels(Constants.NAME, "jobName3")
            .addToLabels(Constants.TYPE, Constants.TYPE_JOB)
            .addToAnnotations(Constants.DEFINITION, Base64.encodeBase64String("GRAPH".getBytes()))
            .addToAnnotations(Constants.LAST_MODIFIED, "lastModified")
            .endMetadata()
            .build();

        WorkflowTemplate workflowTemplate1 = new WorkflowTemplate();
        workflowTemplate1.setMetadata(new ObjectMetaBuilder()
                                          .withName("pipelineId1")
                                          .addToLabels(Constants.NAME, "pipelineName1")
                                          .addToAnnotations(Constants.DEFINITION,
                                                            Base64.encodeBase64String("GRAPH".getBytes()))
                                          .addToAnnotations(Constants.LAST_MODIFIED, "lastModified")
                                          .build());
        workflowTemplate1.setSpec(new WorkflowTemplateSpec().templates(List.of(new Template()
                                                                                   .name("dagTemplate")
                                                                                   .dag(new DagTemplate().addTasksItem(
                                                                                       new DagTask())))));
        WorkflowTemplate workflowTemplate2 = new WorkflowTemplate();
        workflowTemplate2.setMetadata(new ObjectMetaBuilder()
                                          .withName("pipelineId2")
                                          .addToLabels(Constants.NAME, "pipelineName2")
                                          .addToAnnotations(Constants.DEFINITION,
                                                            Base64.encodeBase64String("GRAPH".getBytes()))
                                          .addToAnnotations(Constants.LAST_MODIFIED, "lastModified")
                                          .build());
        workflowTemplate2.setSpec(new WorkflowTemplateSpec().templates(List.of(new Template()
                                                                                   .name("dagTemplate")
                                                                                   .dag(new DagTemplate().addTasksItem(
                                                                                       new DagTask())))));
        WorkflowTemplate workflowTemplate3 = new WorkflowTemplate();
        workflowTemplate3.setMetadata(new ObjectMetaBuilder()
                                          .withName("pipelineId3")
                                          .addToLabels(Constants.NAME, "pipelineName3")
                                          .addToAnnotations(Constants.DEFINITION,
                                                            Base64.encodeBase64String("GRAPH".getBytes()))
                                          .addToAnnotations(Constants.LAST_MODIFIED, "lastModified")
                                          .build());
        workflowTemplate3.setSpec(new WorkflowTemplateSpec().templates(List.of(new Template()
                                                                                   .name("dagTemplate")
                                                                                   .dag(new DagTemplate().addTasksItem(
                                                                                       new DagTask())))));

        doNothing().when(jobService).checkJobName("projectId", "jobId1", "jobName1");
        doThrow(BadRequestException.class).when(jobService).checkJobName("projectId", "jobId2", "jobName2");
        doNothing().when(jobService).checkJobName("projectId", "jobId3", "jobName3");


        doNothing().when(pipelineService).checkPipelineName("projectId", "pipelineId1", "pipelineName1");
        doThrow(BadRequestException.class)
            .when(pipelineService)
            .checkPipelineName("projectId", "pipelineId2", "pipelineName2");
        doNothing().when(pipelineService).checkPipelineName("projectId", "pipelineId3", "pipelineName3");

        doNothing().when(argoKubernetesService).createOrReplaceConfigMap(eq("projectId"), any(ConfigMap.class));
        doNothing()
            .when(argoKubernetesService)
            .createOrReplaceWorkflowTemplate(eq("projectId"), any(WorkflowTemplate.class));


        ImportResponseDto importing =
            transferService.importing("projectId",
                                      new TreeSet<>(Set.of(Serialization.asJson(configMap1),
                                                           Serialization.asJson(configMap2),
                                                           Serialization.asJson(configMap3))),
                                      new TreeSet<>(Set.of(Serialization.asJson(workflowTemplate1),
                                                           Serialization.asJson(workflowTemplate2),
                                                           Serialization.asJson(workflowTemplate3))));

        ImportResponseDto expected = ImportResponseDto
            .builder()
            .notImportedPipelines(List.of("pipelineId2"))
            .notImportedJobs(List.of("jobId2"))
            .build();

        assertEquals(expected, importing);
    }

    @Test
    void testCheckAccess() {
        String projectId = "projectId";
        when(argoKubernetesService.isAccessible(projectId, "configmaps", "", Constants.CREATE_ACTION)).thenReturn(
            true);
        assertTrue(transferService.checkImportAccess(projectId));
        verify(argoKubernetesService).isAccessible(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testCopyJob() {
        ConfigMap configMap1 = new ConfigMapBuilder()
            .addToData(Map.of(Constants.EXECUTOR_MEMORY,
                              "1G",
                              Constants.DRIVER_MEMORY,
                              "1G",
                              Constants.JOB_CONFIG_FIELD,
                              "{\"nodes\":[], \"edges\":[]}"))
            .withNewMetadata()
            .withName("jobId1")
            .addToLabels(Constants.NAME, "jobName2")
            .addToLabels(Constants.TYPE, Constants.TYPE_JOB)
            .addToAnnotations(Constants.DEFINITION, Base64.encodeBase64String("GRAPH".getBytes()))
            .addToAnnotations(Constants.LAST_MODIFIED, "lastModified")
            .endMetadata()
            .build();

        ConfigMap configMap2 = new ConfigMapBuilder()
            .addToData(Map.of(Constants.EXECUTOR_MEMORY,
                              "1G",
                              Constants.DRIVER_MEMORY,
                              "1G",
                              Constants.JOB_CONFIG_FIELD,
                              "{\"nodes\":[], \"edges\":[]}"))
            .withNewMetadata()
            .withName("jobId2")
            .addToLabels(Constants.NAME, "jobName2-Copy")
            .addToLabels(Constants.TYPE, Constants.TYPE_JOB)
            .addToAnnotations(Constants.DEFINITION, Base64.encodeBase64String("GRAPH".getBytes()))
            .addToAnnotations(Constants.LAST_MODIFIED, "lastModified")
            .endMetadata()
            .build();
        ConfigMap configMap3 = new ConfigMapBuilder()
            .addToData(Map.of(Constants.EXECUTOR_MEMORY,
                              "1G",
                              Constants.DRIVER_MEMORY,
                              "1G",
                              Constants.JOB_CONFIG_FIELD,
                              "{\"nodes\":[], \"edges\":[]}"))
            .withNewMetadata()
            .withName("jobId3")
            .addToLabels(Constants.NAME, "jobName2-Copy2")
            .addToLabels(Constants.TYPE, Constants.TYPE_JOB)
            .addToAnnotations(Constants.DEFINITION, Base64.encodeBase64String("GRAPH".getBytes()))
            .addToAnnotations(Constants.LAST_MODIFIED, "lastModified")
            .endMetadata()
            .build();
        ConfigMap configMap4 = new ConfigMapBuilder()
            .addToData(Map.of(Constants.EXECUTOR_MEMORY,
                              "1G",
                              Constants.DRIVER_MEMORY,
                              "1G",
                              Constants.JOB_CONFIG_FIELD,
                              "{\"nodes\":[], \"edges\":[]}"))
            .withNewMetadata()
            .withName("jobId4")
            .addToLabels(Constants.NAME, "jobName2-Copy3")
            .addToLabels(Constants.TYPE, Constants.TYPE_JOB)
            .addToAnnotations(Constants.DEFINITION, Base64.encodeBase64String("GRAPH".getBytes()))
            .addToAnnotations(Constants.LAST_MODIFIED, "lastModified")
            .endMetadata()
            .build();
        List<ConfigMap> configMaps = new ArrayList<>();
        configMaps.add(configMap1);
        when(argoKubernetesService.getAllConfigMaps(anyString())).thenReturn(configMaps);

        for (ConfigMap wf : List.of(configMap2, configMap3)) {
            ConfigMap original = SerializationUtils.clone(configMap1);
            when(argoKubernetesService.getConfigMap("projectId", original.getMetadata().getName())).thenReturn(
                original);
            transferService.copyJob("projectId", original.getMetadata().getName());
            assertEquals(
                wf.getMetadata().getLabels().get(Constants.NAME),
                original.getMetadata().getLabels().get(Constants.NAME),
                "Copy suffix should be exactly the same");
            assertNotEquals(
                wf.getMetadata().getName(),
                original.getMetadata().getName(),
                "Ids should be different");
            configMaps.add(wf);
        }
        configMaps.remove(1);
        ConfigMap original = SerializationUtils.clone(configMap1);
        when(argoKubernetesService.getConfigMap("projectId", original.getMetadata().getName())).thenReturn(
            original);
        transferService.copyJob("projectId", original.getMetadata().getName());
        assertEquals(
            configMap4.getMetadata().getLabels().get(Constants.NAME),
            original.getMetadata().getLabels().get(Constants.NAME),
            "Copy suffix should be exactly the same even after deletion");
        assertNotEquals(
            configMap4.getMetadata().getName(),
            original.getMetadata().getName(),
            "Ids should be different even after deletion");
        configMaps.add(configMap4);
    }

    @Test
    void testCopyPipeline() {
        WorkflowTemplate workflowTemplate1 = new WorkflowTemplate();
        workflowTemplate1.setMetadata(new ObjectMetaBuilder()
                                          .withName("pipelineId1")
                                          .addToLabels(Constants.NAME, "pipelineName1")
                                          .addToAnnotations(Constants.DEFINITION,
                                                            Base64.encodeBase64String("GRAPH".getBytes()))
                                          .addToAnnotations(Constants.LAST_MODIFIED, "lastModified")
                                          .build());
        workflowTemplate1.setSpec(new WorkflowTemplateSpec().templates(List.of(new Template()
                                                                                   .name("dagTemplate")
                                                                                   .dag(new DagTemplate().addTasksItem(
                                                                                       new DagTask().arguments(new Arguments()
                                                                                                                   .addParametersItem(
                                                                                                                       new Parameter()
                                                                                                                           .name(
                                                                                                                               K8sUtils.CONFIGMAP)
                                                                                                                           .value(
                                                                                                                               "jobFromPipeline"))))))));
        WorkflowTemplate workflowTemplate2 = new WorkflowTemplate();
        workflowTemplate2.setMetadata(new ObjectMetaBuilder()
                                          .withName("pipelineId2")
                                          .addToLabels(Constants.NAME, "pipelineName1-Copy")
                                          .addToAnnotations(Constants.DEFINITION,
                                                            Base64.encodeBase64String("GRAPH".getBytes()))
                                          .addToAnnotations(Constants.LAST_MODIFIED, "lastModified")
                                          .build());
        workflowTemplate2.setSpec(new WorkflowTemplateSpec().templates(List.of(new Template()
                                                                                   .name("dagTemplate")
                                                                                   .dag(new DagTemplate().addTasksItem(
                                                                                       new DagTask().arguments(new Arguments()
                                                                                                                   .addParametersItem(
                                                                                                                       new Parameter()
                                                                                                                           .name(
                                                                                                                               K8sUtils.CONFIGMAP)
                                                                                                                           .value(
                                                                                                                               "jobFromPipeline"))))))));

        WorkflowTemplate workflowTemplate3 = new WorkflowTemplate();
        workflowTemplate3.setMetadata(new ObjectMetaBuilder()
                                          .withName("pipelineId3")
                                          .addToLabels(Constants.NAME, "pipelineName1-Copy2")
                                          .addToAnnotations(Constants.DEFINITION,
                                                            Base64.encodeBase64String("GRAPH".getBytes()))
                                          .addToAnnotations(Constants.LAST_MODIFIED, "lastModified")
                                          .build());
        workflowTemplate3.setSpec(new WorkflowTemplateSpec().templates(List.of(new Template()
                                                                                   .name("dagTemplate")
                                                                                   .dag(new DagTemplate().addTasksItem(
                                                                                       new DagTask().arguments(new Arguments()
                                                                                                                   .addParametersItem(
                                                                                                                       new Parameter()
                                                                                                                           .name(
                                                                                                                               K8sUtils.CONFIGMAP)
                                                                                                                           .value(
                                                                                                                               "jobFromPipeline"))))))));
        List<WorkflowTemplate> workflowTemplates = new ArrayList<>();
        workflowTemplates.add(workflowTemplate1);
        when(argoKubernetesService.getAllWorkflowTemplates(anyString())).thenReturn(workflowTemplates);

        for (WorkflowTemplate wf : List.of(workflowTemplate2, workflowTemplate3)) {
            WorkflowTemplate original = SerializationUtils.clone(workflowTemplate1);
            when(argoKubernetesService.getWorkflowTemplate("projectId",
                                                           original.getMetadata().getName())).thenReturn(original);
            transferService.copyPipeline("projectId", original.getMetadata().getName());
            assertEquals(
                wf.getMetadata().getLabels().get(Constants.NAME),
                original.getMetadata().getLabels().get(Constants.NAME),
                "Copy suffix should be exactly the same");
            assertNotEquals(
                wf.getMetadata().getName(),
                original.getMetadata().getName(),
                "Ids should be different");
            workflowTemplates.add(wf);
        }

    }
}

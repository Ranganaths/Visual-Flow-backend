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

package by.iba.vfapi.dto;

import java.time.format.DateTimeFormatter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Constants class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {
    public static final DateTimeFormatter DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z");
    public static final String NAME_PATTERN = "[A-Za-z0-9 \\-_]{3,40}";
    public static final int MAX_DESCRIPTION_LENGTH = 500;
    public static final String PARAM_KEY_PATTERN = "[A-Za-z0-9\\-_]{1,50}";
    public static final String JOB_CONFIG_FIELD = "JOB_CONFIG";
    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String TYPE_JOB = "job";
    public static final String DEFINITION = "definition";
    public static final String LAST_MODIFIED = "lastModified";

    public static final String DESCRIPTION_FIELD = "description";
    public static final String NAME_FIELD = "projectName";

    public static final String JOB_ID_LABEL = "jobId";
    public static final String SPARK_ROLE_LABEL = "spark-role";
    public static final String SPARK_ROLE_EXEC = "executor";
    public static final String WORKFLOW_POD_LABEL = "workflows.argoproj.io/workflow";
    public static final String PIPELINE_JOB_ID_LABEL = "pipelineJobId";
    public static final String NOT_PIPELINE_FLAG = "notPipeline";

    public static final String LIMITS_CPU = "limits.cpu";
    public static final String REQUESTS_CPU = "requests.cpu";
    public static final String LIMITS_MEMORY = "limits.memory";
    public static final String REQUESTS_MEMORY = "requests.memory";
    public static final String QUOTA_NAME = "quota";
    public static final String GIGABYTE_QUANTITY = "G";
    public static final int USAGE_ACCURACY = 2;

    public static final String EXECUTOR_MEMORY = "EXECUTOR_MEMORY";

    public static final String CPU_FIELD = "cpu";
    public static final String MEMORY_FIELD = "memory";
    public static final String DRIVER_CORES = "DRIVER_CORES";
    public static final String DRIVER_MEMORY = "DRIVER_MEMORY";
    public static final String DRIVER_REQUEST_CORES = "DRIVER_REQUEST_CORES";
    public static final double MEMORY_OVERHEAD_FACTOR = 1.1;
    public static final String DAG_TEMPLATE_NAME = "dagTemplate";

    public static final String UPDATE_ACTION = "update";
    public static final String CREATE_ACTION = "create";

    public static final String NODE_TYPE_POD = "Pod";
}

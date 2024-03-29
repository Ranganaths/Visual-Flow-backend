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

package by.iba.vfapi.dto.projects;

import by.iba.vfapi.dto.Constants;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceQuota;
import java.math.RoundingMode;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Resource quota response DTO class.
 */
@Getter
@Setter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class ResourceQuotaResponseDto {
    private Float limitsCpu;
    private Float requestsCpu;
    private Float limitsMemory;
    private Float requestsMemory;
    private boolean editable;

    /**
     * Creating ResourceQuotaDtoBuilder with limits from ResourceQuota.
     *
     * @param resourceQuota ResourceQuota
     * @return ResourceQuotaDtoBuilder
     */
    public static ResourceQuotaResponseDto.ResourceQuotaResponseDtoBuilder limitsFromResourceQuota(
        ResourceQuota resourceQuota) {
        return ResourceQuotaResponseDto
            .builder()
            .limitsCpu(Float.valueOf(resourceQuota.getStatus().getHard().get(Constants.LIMITS_CPU).getAmount()))
            .requestsCpu(Float.valueOf(resourceQuota
                                           .getStatus()
                                           .getHard()
                                           .get(Constants.REQUESTS_CPU)
                                           .getAmount()))
            .limitsMemory(Float.valueOf(resourceQuota
                                            .getStatus()
                                            .getHard()
                                            .get(Constants.LIMITS_MEMORY)
                                            .getAmount()))
            .requestsMemory(Float.valueOf(resourceQuota
                                              .getStatus()
                                              .getHard()
                                              .get(Constants.REQUESTS_MEMORY)
                                              .getAmount()));
    }

    /**
     * Creating ResourceQuotaDtoBuilder with usage from ResourceQuota.
     *
     * @param resourceQuota ResourceQuota
     * @return ResourceQuotaDtoBuilder
     */
    public static ResourceQuotaResponseDto.ResourceQuotaResponseDtoBuilder usageFromResourceQuota(
        ResourceQuota resourceQuota) {
        return ResourceQuotaResponseDto
            .builder()
            .limitsCpu(Quantity
                           .getAmountInBytes(resourceQuota.getStatus().getUsed().get(Constants.LIMITS_CPU))
                           .divide(
                               Quantity.getAmountInBytes(resourceQuota
                                                             .getStatus()
                                                             .getHard()
                                                             .get(Constants.LIMITS_CPU)),
                               Constants.USAGE_ACCURACY,
                               RoundingMode.UP)
                           .floatValue())
            .limitsMemory(Quantity
                              .getAmountInBytes(resourceQuota.getStatus().getUsed().get(Constants.LIMITS_MEMORY))
                              .divide(
                                  Quantity.getAmountInBytes(resourceQuota
                                                                .getStatus()
                                                                .getHard()
                                                                .get(Constants.LIMITS_MEMORY)),
                                  Constants.USAGE_ACCURACY,
                                  RoundingMode.UP)
                              .floatValue())
            .requestsCpu(Quantity
                             .getAmountInBytes(resourceQuota.getStatus().getUsed().get(Constants.REQUESTS_CPU))
                             .divide(
                                 Quantity.getAmountInBytes(resourceQuota
                                                               .getStatus()
                                                               .getHard()
                                                               .get(Constants.REQUESTS_CPU)),
                                 Constants.USAGE_ACCURACY,
                                 RoundingMode.UP)
                             .floatValue())
            .requestsMemory(Quantity
                                .getAmountInBytes(resourceQuota
                                                      .getStatus()
                                                      .getUsed()
                                                      .get(Constants.REQUESTS_MEMORY))
                                .divide(
                                    Quantity.getAmountInBytes(resourceQuota
                                                                  .getStatus()
                                                                  .getHard()
                                                                  .get(Constants.REQUESTS_MEMORY)),
                                    Constants.USAGE_ACCURACY,
                                    RoundingMode.UP)
                                .floatValue());
    }
}

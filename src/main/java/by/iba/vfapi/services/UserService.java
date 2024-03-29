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

import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.rbac.ClusterRole;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * UserService class.
 */
@Service
@RequiredArgsConstructor
public class UserService {
    private final KubernetesService kubernetesService;

    /**
     * Gets application users.
     *
     * @return users with their roles.
     */
    public List<Map<String, String>> getUsers() {
        return kubernetesService
            .getServiceAccounts()
            .stream()
            .map((ServiceAccount sa) -> sa.getMetadata().getAnnotations())
            .collect(Collectors.toList());
    }

    /**
     * Gets application role names.
     *
     * @return list of role names.
     */
    public List<String> getRoleNames() {
        return kubernetesService
            .getRoles()
            .stream()
            .map((ClusterRole role) -> role.getMetadata().getName())
            .collect(Collectors.toList());
    }
}

/*
 * Copyright 2024 Ant Group Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.secretflow.secretpad.service.model.model.export;

import org.secretflow.secretpad.service.model.project.ProjectGraphDomainDataSourceVO;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

/**
 * model export status request
 *
 * @author yutu
 * @date 2024/01/29
 */
@Setter
@Getter
@ToString
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ModelPartyPathResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 87654321L;

    @NotBlank
    @JsonProperty("nodeId")
    private String nodeId;

    @NotBlank
    @JsonProperty("nodeName")
    private String nodeName;

    @JsonProperty("dataSources")
    private Set<ProjectGraphDomainDataSourceVO.DataSource> dataSources;
}
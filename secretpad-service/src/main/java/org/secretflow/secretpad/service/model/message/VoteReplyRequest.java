/*
 * Copyright 2023 Ant Group Co., Ltd.
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

package org.secretflow.secretpad.service.model.message;

import org.secretflow.secretpad.common.annotation.OneOfType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * VoteReplyRequest.
 *
 * @author cml
 * @date 2023/09/20
 */
@Getter
@Setter
public class VoteReplyRequest {

    @OneOfType(types = {"APPROVED", "REJECTED"})
    private String action;

    @Size(max = 50, message = "reason must less then 50 characters")
    private String reason;

    @NotBlank
    private String voteId;

    @NotBlank
    private String voteParticipantId;
}

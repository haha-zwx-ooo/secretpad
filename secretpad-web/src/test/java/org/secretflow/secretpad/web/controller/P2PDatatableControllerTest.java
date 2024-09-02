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

package org.secretflow.secretpad.web.controller;

import org.secretflow.secretpad.common.constant.DomainConstants;
import org.secretflow.secretpad.common.constant.resource.ApiResourceCodeConstants;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.kuscia.v1alpha1.service.impl.KusciaGrpcClientAdapter;
import org.secretflow.secretpad.persistence.entity.FeatureTableDO;
import org.secretflow.secretpad.persistence.entity.InstDO;
import org.secretflow.secretpad.persistence.entity.NodeDO;
import org.secretflow.secretpad.persistence.entity.ProjectFeatureTableDO;
import org.secretflow.secretpad.persistence.repository.FeatureTableRepository;
import org.secretflow.secretpad.persistence.repository.InstRepository;
import org.secretflow.secretpad.persistence.repository.NodeRepository;
import org.secretflow.secretpad.persistence.repository.ProjectFeatureTableRepository;
import org.secretflow.secretpad.service.model.datatable.CreateDatatableRequest;
import org.secretflow.secretpad.service.model.datatable.ListDatatableRequest;
import org.secretflow.secretpad.web.utils.FakerUtils;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.secretflow.v1alpha1.common.Common;
import org.secretflow.v1alpha1.kusciaapi.DomainOuterClass;
import org.secretflow.v1alpha1.kusciaapi.Domaindata;
import org.secretflow.v1alpha1.kusciaapi.Domaindatasource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.*;

/**
 * @author guanxi
 * @date 2024/07/11
 */
@TestPropertySource(properties = {
        "secretpad.gateway=127.0.0.1:9001",
        "secretpad.datasync.p2p=true",
        "secretpad.datasync.center=false",
        "secretpad.platform-type=AUTONOMY",
        "secretpad.node-id=nodeId"
})
public class P2PDatatableControllerTest extends ControllerTest {


    @MockBean
    private KusciaGrpcClientAdapter kusciaGrpcClientAdapter;

    @MockBean
    private FeatureTableRepository featureTableRepository;

    @MockBean
    private NodeRepository nodeRepository;
    @MockBean
    private ProjectFeatureTableRepository projectFeatureTableRepository;

    @MockBean
    private InstRepository instRepository;

    /**
     * createDatable test in p2p
     *
     * @throws Exception
     */
    @Test
    public void createDatable() throws Exception {
        CreateDatatableRequest request = FakerUtils.fake(CreateDatatableRequest.class);
        request.setDatasourceType("OSS");
        request.setDatasourceName("ossDatasource");
        request.setNodeIds(Collections.singletonList("alice"));
        request.setOwnerId("nodeId");
        Mockito.when(nodeRepository.findByNodeId("nodeId")).thenReturn(FakerUtils.fake(NodeDO.class));
        UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.DATATABLE_CREATE));
        Domaindata.CreateDomainDataResponse response = Domaindata.CreateDomainDataResponse.newBuilder()
                .setData(Domaindata.CreateDomainDataResponseData.newBuilder()
                        .setDomaindataId(request.getOwnerId())
                        .build())
                .build();
        Mockito.when(kusciaGrpcClientAdapter.createDomainData(
                Mockito.any())).thenReturn(response);
        Mockito.when(kusciaGrpcClientAdapter.queryDomainDataSource(Mockito.any(), Mockito.any())).thenReturn(Domaindatasource.QueryDomainDataSourceResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(0).build()).build());
        // Act & Assert
        assertResponse(() -> {
            return MockMvcRequestBuilders.post(getMappingUrl(DatatableController.class, "createDataTable", CreateDatatableRequest.class))
                    .content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void listDatatables() throws Exception {
        assertResponse(() -> {
            ListDatatableRequest request = FakerUtils.fake(ListDatatableRequest.class);
            request.setPageSize(10);
            request.setPageNumber(1);
            request.setOwnerId("test");

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.DATATABLE_LIST));

            Domaindata.ListDomainDataResponse response = Domaindata.ListDomainDataResponse.newBuilder()
                    .setData(Domaindata.DomainDataList.newBuilder().build()).build();
            Mockito.when(kusciaGrpcClientAdapter.listDomainData(Mockito.any())).thenReturn(response);
            FeatureTableDO featureTableDO = FakerUtils.fake(FeatureTableDO.class);
            ProjectFeatureTableDO projectFeatureTableDO = FakerUtils.fake(ProjectFeatureTableDO.class);
            Mockito.when(projectFeatureTableRepository.findByNodeIdAndFeatureTableIds(request.getOwnerId(), Lists.newArrayList(featureTableDO.getUpk().getFeatureTableId()))).thenReturn(Collections.singletonList(projectFeatureTableDO));
            Mockito.when(featureTableRepository.findByNodeId(request.getOwnerId())).thenReturn(Collections.singletonList(featureTableDO));
            NodeDO node = FakerUtils.fake(NodeDO.class);
            node.setInstId("nodeId");
            Mockito.when(nodeRepository.findByNodeId(Mockito.any())).thenReturn(node);

            Mockito.when(kusciaGrpcClientAdapter.isDomainRegistered(Mockito.any())).thenReturn(true);

            Mockito.when(kusciaGrpcClientAdapter.queryDomain(Mockito.any(), Mockito.any())).thenReturn(buildQueryDomainResponse(0));

            InstDO instDO = new InstDO();
            Mockito.when(instRepository.findById(Mockito.any())).thenReturn(Optional.of(instDO));
            NodeDO alice = NodeDO.builder().nodeId("alice").build();
            List<NodeDO> list = new ArrayList<>();
            list.add(alice);
            Mockito.when(nodeRepository.findByInstId(Mockito.any())).thenReturn(list);

            return MockMvcRequestBuilders.post(getMappingUrl(DatatableController.class, "listDatatables", ListDatatableRequest.class))
                    .content(JsonUtils.toJSONString(request));
        });
    }

    private DomainOuterClass.QueryDomainResponse buildQueryDomainResponse(Integer code) {
        return DomainOuterClass.QueryDomainResponse.newBuilder()
                .setStatus(Common.Status.newBuilder().setCode(code).build())
                .setData(DomainOuterClass.QueryDomainResponseData.newBuilder().addDeployTokenStatuses(
                                DomainOuterClass.DeployTokenStatus.newBuilder().setToken("123").setState("used").buildPartial())
                        .addNodeStatuses(DomainOuterClass.NodeStatus.newBuilder().setStatus(DomainConstants.DomainStatusEnum.Ready.name()).build())
                        .build())
                .build();
    }
}
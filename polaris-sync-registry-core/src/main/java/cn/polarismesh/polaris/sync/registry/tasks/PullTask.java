/*
 * Tencent is pleased to support the open source community by making Polaris available.
 *
 * Copyright (C) 2019 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package cn.polarismesh.polaris.sync.registry.tasks;

import cn.polarismesh.polaris.sync.extension.registry.Service;
import cn.polarismesh.polaris.sync.extension.utils.StatusCodes;
import cn.polarismesh.polaris.sync.registry.pb.RegistryProto;
import cn.polarismesh.polaris.sync.registry.tasks.TaskEngine.NamedRegistryCenter;
import com.tencent.polaris.client.pb.ResponseProto.DiscoverResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PullTask extends CommonTask implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(PullTask.class);

    public PullTask(NamedRegistryCenter source, NamedRegistryCenter destination, RegistryProto.Match match) {
        super(source, destination, match, new Service(match.getNamespace(), match.getService()));
    }

    @Override
    public void run() {
        //pull the instances from source
        DiscoverResponse srcInstanceResponse = source.getRegistry().listInstances(service);
        if (srcInstanceResponse.getCode().getValue() != StatusCodes.SUCCESS) {
            LOG.warn("[Core][Pull] fail to list service in source {}, code is {}",
                    source.getName(), srcInstanceResponse.getCode().getValue());
            return;
        }
        //pull the instances from destination
        DiscoverResponse dstInstanceResponse = destination.getRegistry().listInstances(service);
        if (dstInstanceResponse.getCode().getValue() != StatusCodes.SUCCESS) {
            LOG.warn("[Core][Pull] fail to list service in destination {}, code is {}",
                    destination.getName(), dstInstanceResponse.getCode().getValue());
            return;
        }
        // diff the deleted instances and new added instances
        changeInstances(srcInstanceResponse.getInstancesList(), dstInstanceResponse.getInstancesList());
    }

    public Service getService() {
        return service;
    }
}

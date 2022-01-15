/*
 * Copyright (C) 2020~2022 dinstone<dinstone@163.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dinstone.agate.gateway.service;

import java.util.ArrayList;
import java.util.List;

import com.dinstone.agate.gateway.options.RouteOptions;

public class FixedServiceAddressListSupplier implements ServiceAddressListSupplier {

    private final List<ServiceAddress> addresses = new ArrayList<ServiceAddress>();

    private final String serviceId;

    public FixedServiceAddressListSupplier(RouteOptions routeOptions) {
        this.serviceId = routeOptions.getRoute();

        for (String url : routeOptions.getRouting().getUrls()) {
            addresses.add(new DefaultServiceAddress(url));
        }
    }

    @Override
    public List<ServiceAddress> get() {
        return addresses;
    }

    @Override
    public String getServiceId() {
        return serviceId;
    }

    @Override
    public void close() {
        addresses.clear();
    }

}

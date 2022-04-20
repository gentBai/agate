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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.agate.gateway.context.ApplicationContext;
import com.dinstone.agate.gateway.options.RouteOptions;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.consul.ConsulClient;
import io.vertx.ext.consul.Service;
import io.vertx.ext.consul.ServiceList;

public class DiscoveryServiceAddressListSupplier implements ServiceAddressListSupplier {

    private static final Logger LOG = LoggerFactory.getLogger(DiscoveryServiceAddressListSupplier.class);

    private List<ServiceAddress> addresses = new CopyOnWriteArrayList<>();

    private Vertx vertx;

    private long jobId;

    private RouteOptions routeOptions;

    private ConsulClient consulClient;

    public DiscoveryServiceAddressListSupplier(Vertx vertx, ApplicationContext appContext, RouteOptions routeOptions) {
        this.vertx = vertx;
        this.routeOptions = routeOptions;

        this.consulClient = appContext.getConsulClient(vertx);

        refresh();

        jobId = vertx.setPeriodic(5000, id -> {
            refresh();
        });
    }

    @SuppressWarnings({ "rawtypes" })
    public void refresh() {
        LOG.debug("{} service discovery url parse", routeOptions.getRoute());

        List<ServiceAddress> serverUrls = new ArrayList<>();
        List<Future> futureList = new ArrayList<>();
        for (String rawUrl : routeOptions.getRouting().getUrls()) {
            try {
                URL rurl = new URL(rawUrl);
                String serviceName = rurl.getHost();
                Future<ServiceList> f = consulClient.catalogServiceNodes(serviceName).onSuccess(ar -> {
                    for (Service s : ar.getList()) {
                        String address = s.getAddress() + ":" + s.getPort();
                        serverUrls.add(new DefaultServiceAddress(rawUrl.replaceFirst(serviceName, address)));
                    }
                });
                futureList.add(f);
            } catch (Exception e) {
                LOG.debug("service discovery url parse error, {} : {}", rawUrl, e.getMessage());
            }
        }
        CompositeFuture.join(futureList).onSuccess(cf -> {
            addresses.clear();
            addresses.addAll(serverUrls);
        });
    }

    @Override
    public List<ServiceAddress> get() {
        return addresses;
    }

    @Override
    public String getServiceId() {
        return routeOptions.getRoute();
    }

    @Override
    public void close() {
        vertx.cancelTimer(jobId);
    }

}
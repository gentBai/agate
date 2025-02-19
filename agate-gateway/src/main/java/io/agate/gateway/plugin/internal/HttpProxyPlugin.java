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

package io.agate.gateway.plugin.internal;

import io.agate.gateway.handler.OperationHandler;
import io.agate.gateway.handler.internal.HttpProxyHandler;
import io.agate.gateway.options.RouteOptions;
import io.agate.gateway.plugin.PluginOptions;
import io.agate.gateway.plugin.RoutePlugin;
import io.agate.gateway.service.ConsulServiceAddressSupplier;
import io.agate.gateway.service.FixedServiceAddressSupplier;
import io.agate.gateway.service.Loadbalancer;
import io.agate.gateway.service.RoundRobinLoadBalancer;
import io.agate.gateway.service.ServiceAddressSupplier;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;

public class HttpProxyPlugin extends RoutePlugin {

    private HttpClient httpClient;

    private Loadbalancer loadBalancer;

    private ServiceAddressSupplier supplier;

    public HttpProxyPlugin(RouteOptions routeOptions, PluginOptions pluginOptions) {
        super(routeOptions, pluginOptions);
    }

    @Override
    public void destory() {
        synchronized (this) {
            if (httpClient != null) {
                httpClient.close();
            }
            if (supplier != null) {
                supplier.close();
            }
        }
    }

    @Override
    public OperationHandler createHandler(Vertx vertx) {
        HttpClient httpClient = createHttpClient(vertx, routeOptions);
        Loadbalancer loadbalancer = createLoadbalancer(vertx, routeOptions);
        return new HttpProxyHandler(routeOptions, httpClient, loadbalancer);
    }

    public Loadbalancer createLoadbalancer(Vertx vertx, RouteOptions routeOptions) {
        synchronized (this) {
            if (loadBalancer == null) {
                // service discovery
                if (pluginOptions.getOptions().getInteger("type", 0) == 1) {
                    supplier = new ConsulServiceAddressSupplier(vertx, routeOptions, pluginOptions);
                    loadBalancer = new RoundRobinLoadBalancer(routeOptions, supplier);
                } else {
                    supplier = new FixedServiceAddressSupplier(routeOptions, pluginOptions);
                    loadBalancer = new RoundRobinLoadBalancer(routeOptions, supplier);
                }
            }

            return loadBalancer;
        }
    }

    public HttpClient createHttpClient(Vertx vertx, RouteOptions routeOptions) {
        synchronized (this) {
            if (httpClient == null) {
                HttpClientOptions clientOptions = new HttpClientOptions();
                clientOptions.setKeepAlive(true);
                clientOptions.setConnectTimeout(2000);
                // clientOptions.setMaxWaitQueueSize(1000);
                clientOptions.setIdleTimeout(10);
                clientOptions.setMaxPoolSize(100);
                // clientOptions.setTracingPolicy(TracingPolicy.PROPAGATE);
                httpClient = vertx.createHttpClient(clientOptions);
            }
            return httpClient;
        }
    }

}

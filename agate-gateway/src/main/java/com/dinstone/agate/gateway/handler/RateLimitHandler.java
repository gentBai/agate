/*
 * Copyright (C) 2019~2021 dinstone<dinstone@163.com>
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
package com.dinstone.agate.gateway.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.agate.gateway.options.RouteOptions;
import com.dinstone.agate.gateway.spi.BeforeHandler;
import com.google.common.util.concurrent.RateLimiter;

import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.HttpException;

/**
 * rate limit handler.
 * 
 * @author dinstone
 *
 */
public class RateLimitHandler implements BeforeHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RateLimitHandler.class);

    private RateLimiter rateLimiter;

    private RouteOptions routeOptions;

    public RateLimitHandler(RouteOptions routeOptions) {
        this.routeOptions = routeOptions;
        // if (api.getHandlers().getPermitsPerSecond() > 0) {
        // limiter = RateLimiter.create(api.getHandlers().getPermitsPerSecond());
        // }
    }

    @Override
    public void handle(RoutingContext rc) {
        if (rateLimiter == null) {
            rc.next();
            return;
        }

        if (rateLimiter.tryAcquire()) {
            rc.next();
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("{} rate limit than {}/s", routeOptions.getRoute(), rateLimiter.getRate());
            }
            rc.fail(new HttpException(429, "too many requests, rate limit " + rateLimiter.getRate() + "/s"));
        }
    }

}

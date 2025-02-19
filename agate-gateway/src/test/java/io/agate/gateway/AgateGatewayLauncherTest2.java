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

package io.agate.gateway;

import io.agate.gateway.AgateGatewayLauncher;
import io.agate.gateway.context.AgateVerticleFactory;
import io.agate.gateway.verticle.LaunchVerticle;

class AgateGatewayLauncherTest2 {

    public static void main(String[] args) throws Exception {
        AgateGatewayLauncher.main(new String[] { "run", AgateVerticleFactory.verticleName(LaunchVerticle.class),
                "-conf=src/test/resources/config2.json" });
    }

}

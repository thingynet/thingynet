/*
 * MongoConfig.java
 *
 * Copyright 2014 Jason Crossley
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

package thingynet.mongo;

import com.mongodb.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.ArrayList;

@Configuration
class MongoConfig {

    @Value("${mongo.hosts}")
    private String[] mongoHosts;

    @Value("${mongo.ports}")
    private int[] mongoPorts;

    @Value("${mongo.username}")
    private String mongoUsername;

    @Value("${mongo.password}")
    private String mongoPassword;

    @Value("${mongo.read.preference}")
    private String mongoReadPreference;

    @Value("${mongo.write.concern}")
    private String mongoWriteConcern;

    @Value("${mongo.connections.per.host}")
    private int mongoConnectionsPerHost;

    @Bean
    MongoClient mongoClient() throws IOException {
        ArrayList<ServerAddress> servers = new ArrayList<>();
        for (int i = 0; i < mongoHosts.length; i++) {
            servers.add(new ServerAddress(mongoHosts[i], mongoPorts[i]));
        }
        WriteConcern writeConcern = WriteConcern.valueOf(mongoWriteConcern);
        ReadPreference readPreference = ReadPreference.valueOf(mongoReadPreference);

        MongoClientOptions options = new MongoClientOptions.Builder()
                .connectionsPerHost(mongoConnectionsPerHost)
                .writeConcern(writeConcern)
                .readPreference(readPreference)
                .build();

        return new MongoClient(servers, options);
    }
}
